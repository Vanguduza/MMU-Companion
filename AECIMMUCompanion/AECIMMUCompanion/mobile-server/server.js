const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const compression = require('compression');
const morgan = require('morgan');
const rateLimit = require('express-rate-limit');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const sqlite3 = require('sqlite3').verbose();
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const cron = require('node-cron');
const { v4: uuidv4 } = require('uuid');
const { body, validationResult } = require('express-validator');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET || 'aeci-mmu-companion-secret-key-2024';

// Create necessary directories
const dataDir = path.join(__dirname, 'data');
const uploadsDir = path.join(__dirname, 'uploads');
const backupsDir = path.join(__dirname, 'backups');

[dataDir, uploadsDir, backupsDir].forEach(dir => {
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
    }
});

// Initialize SQLite database
const db = new sqlite3.Database(path.join(dataDir, 'aeci_mmu.db'));

// Middleware
app.use(helmet());
app.use(compression());
app.use(cors({
    origin: ['http://localhost:3000', 'http://127.0.0.1:3000', '*'],
    credentials: true
}));
app.use(morgan('combined'));
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true, limit: '50mb' }));

// Rate limiting
const limiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 100 // limit each IP to 100 requests per windowMs
});
app.use(limiter);

// Multer configuration for file uploads
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, uploadsDir);
    },
    filename: (req, file, cb) => {
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        cb(null, file.fieldname + '-' + uniqueSuffix + path.extname(file.originalname));
    }
});
const upload = multer({ 
    storage: storage,
    limits: { fileSize: 10 * 1024 * 1024 } // 10MB limit
});

// Initialize database tables
db.serialize(() => {
    // Users table
    db.run(`CREATE TABLE IF NOT EXISTS users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT UNIQUE NOT NULL,
        password_hash TEXT NOT NULL,
        full_name TEXT NOT NULL,
        role TEXT NOT NULL DEFAULT 'technician',
        department TEXT,
        phone TEXT,
        biometric_enabled BOOLEAN DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        last_login DATETIME,
        is_active BOOLEAN DEFAULT 1
    )`);

    // Equipment table
    db.run(`CREATE TABLE IF NOT EXISTS equipment (
        id TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        type TEXT NOT NULL,
        location TEXT,
        status TEXT DEFAULT 'operational',
        last_maintenance DATETIME,
        next_maintenance DATETIME,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )`);

    // Forms table
    db.run(`CREATE TABLE IF NOT EXISTS forms (
        id TEXT PRIMARY KEY,
        form_type TEXT NOT NULL,
        title TEXT NOT NULL,
        created_by INTEGER,
        assigned_to INTEGER,
        equipment_id TEXT,
        status TEXT DEFAULT 'draft',
        form_data TEXT,
        attachments TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        completed_at DATETIME,
        FOREIGN KEY (created_by) REFERENCES users (id),
        FOREIGN KEY (assigned_to) REFERENCES users (id),
        FOREIGN KEY (equipment_id) REFERENCES equipment (id)
    )`);

    // Shifts table
    db.run(`CREATE TABLE IF NOT EXISTS shifts (
        id TEXT PRIMARY KEY,
        shift_date DATE NOT NULL,
        shift_type TEXT NOT NULL,
        supervisor_id INTEGER,
        team_members TEXT,
        notes TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (supervisor_id) REFERENCES users (id)
    )`);

    // Sync log table
    db.run(`CREATE TABLE IF NOT EXISTS sync_log (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        table_name TEXT NOT NULL,
        operation TEXT NOT NULL,
        record_id TEXT NOT NULL,
        data TEXT,
        timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
    )`);

    // Create default admin user
    const adminPassword = bcrypt.hashSync('admin123', 10);
    db.run(`INSERT OR IGNORE INTO users (username, password_hash, full_name, role) 
            VALUES ('admin', ?, 'System Administrator', 'admin')`, [adminPassword]);
});

// Authentication middleware
const authenticateToken = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (!token) {
        return res.status(401).json({ error: 'Access token required' });
    }

    jwt.verify(token, JWT_SECRET, (err, user) => {
        if (err) {
            return res.status(403).json({ error: 'Invalid or expired token' });
        }
        req.user = user;
        next();
    });
};

// Validation middleware
const validateRequest = (req, res, next) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({ errors: errors.array() });
    }
    next();
};

// Routes

// Health check
app.get('/health', (req, res) => {
    res.json({ 
        status: 'healthy', 
        timestamp: new Date().toISOString(),
        uptime: process.uptime(),
        version: '1.0.0'
    });
});

// Authentication routes
app.post('/api/auth/login', [
    body('username').notEmpty().withMessage('Username is required'),
    body('password').notEmpty().withMessage('Password is required')
], validateRequest, (req, res) => {
    const { username, password } = req.body;

    db.get('SELECT * FROM users WHERE username = ? AND is_active = 1', [username], (err, user) => {
        if (err) {
            return res.status(500).json({ error: 'Database error' });
        }

        if (!user || !bcrypt.compareSync(password, user.password_hash)) {
            return res.status(401).json({ error: 'Invalid credentials' });
        }

        // Update last login
        db.run('UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?', [user.id]);

        const token = jwt.sign(
            { 
                id: user.id, 
                username: user.username, 
                role: user.role 
            }, 
            JWT_SECRET, 
            { expiresIn: '24h' }
        );

        res.json({
            token,
            user: {
                id: user.id,
                username: user.username,
                fullName: user.full_name,
                role: user.role,
                department: user.department
            }
        });
    });
});

// User management routes
app.get('/api/users', authenticateToken, (req, res) => {
    db.all('SELECT id, username, full_name, role, department, phone, created_at, last_login, is_active FROM users ORDER BY full_name', (err, users) => {
        if (err) {
            return res.status(500).json({ error: 'Database error' });
        }
        res.json(users);
    });
});

app.post('/api/users', authenticateToken, [
    body('username').notEmpty().withMessage('Username is required'),
    body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters'),
    body('fullName').notEmpty().withMessage('Full name is required'),
    body('role').isIn(['admin', 'supervisor', 'technician', 'millwright']).withMessage('Invalid role')
], validateRequest, (req, res) => {
    const { username, password, fullName, role, department, phone } = req.body;
    const passwordHash = bcrypt.hashSync(password, 10);

    db.run('INSERT INTO users (username, password_hash, full_name, role, department, phone) VALUES (?, ?, ?, ?, ?, ?)',
        [username, passwordHash, fullName, role, department, phone], function(err) {
            if (err) {
                if (err.code === 'SQLITE_CONSTRAINT') {
                    return res.status(400).json({ error: 'Username already exists' });
                }
                return res.status(500).json({ error: 'Database error' });
            }
            res.status(201).json({ id: this.lastID, message: 'User created successfully' });
        });
});

// Equipment routes
app.get('/api/equipment', authenticateToken, (req, res) => {
    db.all('SELECT * FROM equipment ORDER BY name', (err, equipment) => {
        if (err) {
            return res.status(500).json({ error: 'Database error' });
        }
        res.json(equipment);
    });
});

app.post('/api/equipment', authenticateToken, [
    body('name').notEmpty().withMessage('Equipment name is required'),
    body('type').notEmpty().withMessage('Equipment type is required')
], validateRequest, (req, res) => {
    const { name, type, location, status } = req.body;
    const id = uuidv4();

    db.run('INSERT INTO equipment (id, name, type, location, status) VALUES (?, ?, ?, ?, ?)',
        [id, name, type, location, status || 'operational'], function(err) {
            if (err) {
                return res.status(500).json({ error: 'Database error' });
            }
            res.status(201).json({ id, message: 'Equipment created successfully' });
        });
});

// Forms routes
app.get('/api/forms', authenticateToken, (req, res) => {
    const { status, formType, equipmentId } = req.query;
    let query = `SELECT f.*, u1.full_name as created_by_name, u2.full_name as assigned_to_name, e.name as equipment_name 
                 FROM forms f 
                 LEFT JOIN users u1 ON f.created_by = u1.id 
                 LEFT JOIN users u2 ON f.assigned_to = u2.id 
                 LEFT JOIN equipment e ON f.equipment_id = e.id 
                 WHERE 1=1`;
    const params = [];

    if (status) {
        query += ' AND f.status = ?';
        params.push(status);
    }
    if (formType) {
        query += ' AND f.form_type = ?';
        params.push(formType);
    }
    if (equipmentId) {
        query += ' AND f.equipment_id = ?';
        params.push(equipmentId);
    }

    query += ' ORDER BY f.created_at DESC';

    db.all(query, params, (err, forms) => {
        if (err) {
            return res.status(500).json({ error: 'Database error' });
        }
        res.json(forms);
    });
});

app.post('/api/forms', authenticateToken, [
    body('formType').notEmpty().withMessage('Form type is required'),
    body('title').notEmpty().withMessage('Form title is required')
], validateRequest, upload.array('attachments', 10), (req, res) => {
    const { formType, title, equipmentId, assignedTo, formData } = req.body;
    const id = uuidv4();
    const attachments = req.files ? JSON.stringify(req.files.map(f => f.filename)) : null;

    db.run('INSERT INTO forms (id, form_type, title, created_by, assigned_to, equipment_id, form_data, attachments) VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
        [id, formType, title, req.user.id, assignedTo, equipmentId, formData, attachments], function(err) {
            if (err) {
                return res.status(500).json({ error: 'Database error' });
            }
            res.status(201).json({ id, message: 'Form created successfully' });
        });
});

// Data sync routes
app.get('/api/sync/status', authenticateToken, (req, res) => {
    const { since } = req.query;
    const sinceDate = since ? new Date(since) : new Date(Date.now() - 24 * 60 * 60 * 1000); // Last 24 hours

    db.all('SELECT * FROM sync_log WHERE timestamp > ? ORDER BY timestamp DESC LIMIT 100', 
        [sinceDate.toISOString()], (err, logs) => {
            if (err) {
                return res.status(500).json({ error: 'Database error' });
            }
            res.json({
                lastSync: new Date().toISOString(),
                changes: logs
            });
        });
});

app.post('/api/sync/push', authenticateToken, (req, res) => {
    const { table, operation, recordId, data } = req.body;
    
    // Log the sync operation
    db.run('INSERT INTO sync_log (table_name, operation, record_id, data) VALUES (?, ?, ?, ?)',
        [table, operation, recordId, JSON.stringify(data)], (err) => {
            if (err) {
                return res.status(500).json({ error: 'Sync log error' });
            }
            
            // Process the actual data change based on table and operation
            // This would contain the sync logic for different tables
            res.json({ success: true, message: 'Data synced successfully' });
        });
});

// File upload and management
app.post('/api/upload', authenticateToken, upload.single('file'), (req, res) => {
    if (!req.file) {
        return res.status(400).json({ error: 'No file uploaded' });
    }
    
    res.json({
        filename: req.file.filename,
        originalName: req.file.originalname,
        size: req.file.size,
        path: `/uploads/${req.file.filename}`
    });
});

app.get('/uploads/:filename', (req, res) => {
    const filename = req.params.filename;
    const filePath = path.join(uploadsDir, filename);
    
    if (fs.existsSync(filePath)) {
        res.sendFile(filePath);
    } else {
        res.status(404).json({ error: 'File not found' });
    }
});

// Server management routes
app.get('/api/server/status', (req, res) => {
    res.json({
        status: 'running',
        port: PORT,
        timestamp: new Date().toISOString(),
        uptime: process.uptime(),
        memory: process.memoryUsage(),
        platform: process.platform,
        nodeVersion: process.version
    });
});

app.post('/api/server/backup', authenticateToken, (req, res) => {
    const backupFile = path.join(backupsDir, `backup-${Date.now()}.db`);
    
    db.backup(backupFile, (err) => {
        if (err) {
            return res.status(500).json({ error: 'Backup failed' });
        }
        res.json({ message: 'Backup created successfully', file: backupFile });
    });
});

// Automatic backup every 6 hours
cron.schedule('0 */6 * * *', () => {
    const backupFile = path.join(backupsDir, `auto-backup-${Date.now()}.db`);
    db.backup(backupFile, (err) => {
        if (err) {
            console.error('Auto backup failed:', err);
        } else {
            console.log('Auto backup created:', backupFile);
        }
    });
});

// Static files for web interface
app.use(express.static(path.join(__dirname, 'public')));

// Catch-all route for SPA
app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// Error handling middleware
app.use((err, req, res, next) => {
    console.error(err.stack);
    res.status(500).json({ error: 'Something went wrong!' });
});

// Graceful shutdown
process.on('SIGINT', () => {
    console.log('\nShutting down server...');
    db.close((err) => {
        if (err) {
            console.error(err.message);
        }
        console.log('Database connection closed.');
        process.exit(0);
    });
});

// Start server
app.listen(PORT, '0.0.0.0', () => {
    console.log(`AECI MMU Mobile Server running on port ${PORT}`);
    console.log(`Server time: ${new Date().toISOString()}`);
    console.log(`Access server at: http://localhost:${PORT}`);
}); 