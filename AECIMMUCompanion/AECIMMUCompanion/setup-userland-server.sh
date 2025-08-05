#!/bin/bash

# AECI MMU Companion Server Setup Script for Userland
# This script sets up a complete Node.js server environment on Android using Userland

echo "🚀 AECI MMU Companion Server Setup for Userland"
echo "================================================"

# Update package list
echo "📦 Updating package list..."
apt update

# Install Node.js and npm
echo "📦 Installing Node.js and npm..."
apt install -y nodejs npm sqlite3

# Create app directory
echo "📁 Creating application directory..."
mkdir -p /home/mmu-companion-server
cd /home/mmu-companion-server

# Initialize package.json
echo "📋 Initializing package.json..."
cat > package.json << 'EOF'
{
  "name": "aeci-mmu-companion-server",
  "version": "2.0.0",
  "description": "AECI MMU Companion Server with comprehensive todo management",
  "main": "server.js",
  "scripts": {
    "start": "node server.js",
    "dev": "nodemon server.js",
    "setup": "node setup.js"
  },
  "dependencies": {
    "express": "^4.18.2",
    "cors": "^2.8.5",
    "bcryptjs": "^2.4.3",
    "jsonwebtoken": "^9.0.2",
    "nodemailer": "^6.9.7",
    "sqlite3": "^5.1.6",
    "uuid": "^9.0.1",
    "body-parser": "^1.20.2",
    "helmet": "^7.1.0",
    "express-rate-limit": "^7.1.5",
    "multer": "^1.4.5-lts.1",
    "dotenv": "^16.3.1"
  },
  "devDependencies": {
    "nodemon": "^3.0.2"
  },
  "keywords": ["aeci", "mmu", "companion", "server", "api"],
  "author": "AECI Development Team",
  "license": "ISC"
}
EOF

# Create .env file
echo "🔐 Creating environment configuration..."
cat > .env << 'EOF'
# Server Configuration
PORT=3000
NODE_ENV=production

# JWT Configuration
JWT_SECRET=AECIMMUCompanion2025SuperSecretKey!@#$%^&*()
JWT_EXPIRES_IN=24h

# Database Configuration
DB_PATH=./aeci_mmu_companion.db

# Email Configuration (Optional)
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USER=your-email@gmail.com
EMAIL_PASS=your-app-password

# CORS Configuration
ALLOWED_ORIGINS=*

# Rate Limiting
RATE_LIMIT_WINDOW_MS=900000
RATE_LIMIT_MAX_REQUESTS=100
EOF

# Install dependencies
echo "📦 Installing Node.js dependencies..."
npm install

# Create the main server file
echo "⚙️ Creating server configuration..."
cat > server.js << 'EOF'
const express = require('express');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const nodemailer = require('nodemailer');
const sqlite3 = require('sqlite3').verbose();
const { v4: uuidv4 } = require('uuid');
const bodyParser = require('body-parser');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const fs = require('fs');
const path = require('path');
const multer = require('multer');
require('dotenv').config();

// Create reports directory if it doesn't exist
const reportsDir = path.join(__dirname, 'reports');
if (!fs.existsSync(reportsDir)) {
    fs.mkdirSync(reportsDir, { recursive: true });
}

// Configure multer for file uploads
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, reportsDir);
    },
    filename: function (req, file, cb) {
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        cb(null, file.fieldname + '-' + uniqueSuffix + path.extname(file.originalname));
    }
});

const upload = multer({ 
    storage: storage,
    limits: {
        fileSize: 50 * 1024 * 1024 // 50MB limit
    },
    fileFilter: function (req, file, cb) {
        const allowedTypes = ['.pdf', '.xlsx', '.xls', '.csv', '.jpg', '.jpeg', '.png'];
        const ext = path.extname(file.originalname).toLowerCase();
        if (allowedTypes.includes(ext)) {
            cb(null, true);
        } else {
            cb(new Error('Invalid file type. Only PDF, Excel, CSV, and image files are allowed.'));
        }
    }
});

const app = express();
const PORT = process.env.PORT || 3000;

// Database setup
const dbPath = process.env.DB_PATH || './aeci_mmu_companion.db';
const db = new sqlite3.Database(dbPath, (err) => {
    if (err) {
        console.error('❌ Error opening database:', err.message);
        process.exit(1);
    }
    console.log('✅ Connected to SQLite database:', dbPath);
});

// Enable WAL mode for better performance
db.run('PRAGMA journal_mode=WAL;');
db.run('PRAGMA synchronous=NORMAL;');
db.run('PRAGMA cache_size=1000;');
db.run('PRAGMA temp_store=MEMORY;');

// Security middleware
app.use(helmet({
    crossOriginResourcePolicy: { policy: "cross-origin" }
}));

// Rate limiting
const limiter = rateLimit({
    windowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS) || 15 * 60 * 1000, // 15 minutes
    max: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS) || 100, // limit each IP to 100 requests per windowMs
    message: {
        success: false,
        message: 'Too many requests from this IP, please try again later.'
    }
});

app.use('/api/', limiter);

// CORS configuration
const corsOptions = {
    origin: function (origin, callback) {
        const allowedOrigins = process.env.ALLOWED_ORIGINS?.split(',') || ['*'];
        if (allowedOrigins.includes('*') || !origin || allowedOrigins.includes(origin)) {
            callback(null, true);
        } else {
            callback(new Error('Not allowed by CORS'));
        }
    },
    credentials: true,
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With']
};

app.use(cors(corsOptions));

// Body parsing middleware
app.use(bodyParser.json({ limit: '50mb' }));
app.use(bodyParser.urlencoded({ extended: true, limit: '50mb' }));

// Static file serving
app.use('/reports', express.static(reportsDir));

// JWT token verification middleware
function authenticateToken(req, res, next) {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (!token) {
        return res.status(401).json({
            success: false,
            message: 'Access token required'
        });
    }

    jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
        if (err) {
            return res.status(403).json({
                success: false,
                message: 'Invalid or expired token'
            });
        }
        req.user = user;
        next();
    });
}

// Initialize database tables
function initializeDatabase() {
    // Users table
    db.run(`CREATE TABLE IF NOT EXISTS users (
        id TEXT PRIMARY KEY,
        username TEXT UNIQUE NOT NULL,
        email TEXT UNIQUE NOT NULL,
        password_hash TEXT NOT NULL,
        full_name TEXT NOT NULL,
        role TEXT NOT NULL DEFAULT 'OPERATOR',
        department TEXT,
        shift_pattern TEXT,
        permissions TEXT,
        is_active BOOLEAN DEFAULT 1,
        requires_password_change BOOLEAN DEFAULT 0,
        last_login_at INTEGER,
        created_at INTEGER DEFAULT (strftime('%s', 'now')),
        biometric_enabled BOOLEAN DEFAULT 0
    )`, function(err) {
        if (err) {
            console.error('Error creating users table:', err);
            return;
        }
        
        // Password reset tokens table
        db.run(`CREATE TABLE IF NOT EXISTS password_reset_tokens (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id TEXT NOT NULL,
            token TEXT UNIQUE NOT NULL,
            expires_at INTEGER NOT NULL,
            used BOOLEAN DEFAULT 0,
            created_at INTEGER DEFAULT (strftime('%s', 'now')),
            FOREIGN KEY (user_id) REFERENCES users (id)
        )`, function(err) {
            if (err) {
                console.error('Error creating password_reset_tokens table:', err);
                return;
            }
            
            // Forms table
            db.run(`CREATE TABLE IF NOT EXISTS forms (
                id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                equipment_id TEXT,
                form_type TEXT NOT NULL,
                form_data TEXT NOT NULL,
                status TEXT DEFAULT 'DRAFT',
                submitted_at INTEGER,
                created_at INTEGER DEFAULT (strftime('%s', 'now')),
                updated_at INTEGER DEFAULT (strftime('%s', 'now')),
                FOREIGN KEY (user_id) REFERENCES users (id)
            )`, function(err) {
                if (err) {
                    console.error('Error creating forms table:', err);
                    return;
                }
                
                // Equipment table
                db.run(`CREATE TABLE IF NOT EXISTS equipment (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL,
                    model TEXT,
                    serial_number TEXT,
                    location TEXT,
                    status TEXT DEFAULT 'OPERATIONAL',
                    last_maintenance INTEGER,
                    next_maintenance INTEGER,
                    created_at INTEGER DEFAULT (strftime('%s', 'now'))
                )`, function(err) {
                    if (err) {
                        console.error('Error creating equipment table:', err);
                        return;
                    }
                    
                    // Report history table
                    db.run(`CREATE TABLE IF NOT EXISTS report_history (
                        id TEXT PRIMARY KEY,
                        report_type TEXT NOT NULL,
                        report_title TEXT NOT NULL,
                        generated_by TEXT NOT NULL,
                        completion_date INTEGER NOT NULL,
                        file_path TEXT NOT NULL,
                        file_name TEXT NOT NULL,
                        file_size INTEGER DEFAULT 0,
                        format TEXT NOT NULL,
                        parameters TEXT,
                        form_ids TEXT,
                        status TEXT DEFAULT 'COMPLETED',
                        download_count INTEGER DEFAULT 0,
                        last_downloaded INTEGER,
                        created_at INTEGER DEFAULT (strftime('%s', 'now')),
                        FOREIGN KEY (generated_by) REFERENCES users (id)
                    )`, function(err) {
                        if (err) {
                            console.error('Error creating report_history table:', err);
                            return;
                        }
                        
                        // Report downloads tracking
                        db.run(`CREATE TABLE IF NOT EXISTS report_downloads (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            report_id TEXT NOT NULL,
                            downloaded_by TEXT NOT NULL,
                            download_date INTEGER DEFAULT (strftime('%s', 'now')),
                            ip_address TEXT,
                            user_agent TEXT,
                            FOREIGN KEY (report_id) REFERENCES report_history (id),
                            FOREIGN KEY (downloaded_by) REFERENCES users (id)
                        )`, function(err) {
                            if (err) {
                                console.error('Error creating report_downloads table:', err);
                                return;
                            }
                            
                            // Todos table
                            db.run(`CREATE TABLE IF NOT EXISTS todos (
                                id TEXT PRIMARY KEY,
                                title TEXT NOT NULL,
                                description TEXT DEFAULT '',
                                priority TEXT DEFAULT 'MEDIUM',
                                category TEXT DEFAULT 'GENERAL',
                                status TEXT DEFAULT 'PENDING',
                                progress_percentage INTEGER DEFAULT 0,
                                estimated_hours REAL DEFAULT 0,
                                actual_hours REAL DEFAULT 0,
                                time_spent_minutes INTEGER DEFAULT 0,
                                due_date INTEGER,
                                reminder_date INTEGER,
                                start_date INTEGER,
                                completed_at INTEGER,
                                assigned_to_user_id TEXT,
                                created_by_user_id TEXT NOT NULL,
                                site_id TEXT,
                                equipment_id TEXT,
                                form_id TEXT,
                                job_card_number TEXT,
                                tags TEXT DEFAULT '[]',
                                notes TEXT DEFAULT '',
                                checklist_items TEXT DEFAULT '[]',
                                depends_on_todo_ids TEXT DEFAULT '[]',
                                blocked_by TEXT DEFAULT '',
                                recurrence_pattern TEXT,
                                skill_level TEXT DEFAULT 'BEGINNER',
                                estimated_cost REAL DEFAULT 0,
                                actual_cost REAL DEFAULT 0,
                                custom_fields TEXT DEFAULT '{}',
                                sync_status TEXT DEFAULT 'LOCAL',
                                is_completed BOOLEAN DEFAULT 0,
                                is_archived BOOLEAN DEFAULT 0,
                                created_at INTEGER DEFAULT (strftime('%s', 'now')),
                                updated_at INTEGER DEFAULT (strftime('%s', 'now')),
                                FOREIGN KEY (assigned_to_user_id) REFERENCES users (id),
                                FOREIGN KEY (created_by_user_id) REFERENCES users (id),
                                FOREIGN KEY (equipment_id) REFERENCES equipment (id),
                                FOREIGN KEY (form_id) REFERENCES forms (id)
                            )`, function(err) {
                                if (err) {
                                    console.error('Error creating todos table:', err);
                                    return;
                                }
                                
                                // Todo comments table
                                db.run(`CREATE TABLE IF NOT EXISTS todo_comments (
                                    id TEXT PRIMARY KEY,
                                    todo_id TEXT NOT NULL,
                                    comment TEXT NOT NULL,
                                    comment_type TEXT DEFAULT 'PUBLIC',
                                    created_by_user_id TEXT NOT NULL,
                                    created_at INTEGER DEFAULT (strftime('%s', 'now')),
                                    FOREIGN KEY (todo_id) REFERENCES todos (id) ON DELETE CASCADE,
                                    FOREIGN KEY (created_by_user_id) REFERENCES users (id)
                                )`, function(err) {
                                    if (err) {
                                        console.error('Error creating todo_comments table:', err);
                                        return;
                                    }
                                    
                                    // Todo attachments table
                                    db.run(`CREATE TABLE IF NOT EXISTS todo_attachments (
                                        id TEXT PRIMARY KEY,
                                        todo_id TEXT NOT NULL,
                                        file_name TEXT NOT NULL,
                                        file_path TEXT NOT NULL,
                                        file_size INTEGER DEFAULT 0,
                                        mime_type TEXT,
                                        uploaded_by_user_id TEXT NOT NULL,
                                        uploaded_at INTEGER DEFAULT (strftime('%s', 'now')),
                                        FOREIGN KEY (todo_id) REFERENCES todos (id) ON DELETE CASCADE,
                                        FOREIGN KEY (uploaded_by_user_id) REFERENCES users (id)
                                    )`, function(err) {
                                        if (err) {
                                            console.error('Error creating todo_attachments table:', err);
                                            return;
                                        }
                                        
                                        // Todo time entries table
                                        db.run(`CREATE TABLE IF NOT EXISTS todo_time_entries (
                                            id TEXT PRIMARY KEY,
                                            todo_id TEXT NOT NULL,
                                            user_id TEXT NOT NULL,
                                            start_time INTEGER NOT NULL,
                                            end_time INTEGER,
                                            duration_minutes INTEGER DEFAULT 0,
                                            description TEXT DEFAULT '',
                                            is_break BOOLEAN DEFAULT 0,
                                            created_at INTEGER DEFAULT (strftime('%s', 'now')),
                                            FOREIGN KEY (todo_id) REFERENCES todos (id) ON DELETE CASCADE,
                                            FOREIGN KEY (user_id) REFERENCES users (id)
                                        )`, function(err) {
                                            if (err) {
                                                console.error('Error creating todo_time_entries table:', err);
                                                return;
                                            }
                                            
                                            console.log('✅ Database tables initialized.');
                                            
                                            // Create default admin user if none exists
                                            createDefaultAdminUser();
                                        });
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    });
}

// Create default admin user
async function createDefaultAdminUser() {
    return new Promise((resolve, reject) => {
        db.get("SELECT COUNT(*) as count FROM users WHERE role = 'ADMIN'", async (err, row) => {
            if (err) {
                console.error('Error checking admin users:', err);
                reject(err);
                return;
            }
            
            if (row.count === 0) {
                const adminPassword = 'AECIAdmin2025!';
                const hashedPassword = await bcrypt.hash(adminPassword, 12);
                const adminId = 'admin-default-001';
                
                const permissions = JSON.stringify([
                    'SYSTEM_ADMIN', 'MANAGE_USERS', 'VIEW_USERS', 
                    'MANAGE_EQUIPMENT', 'VIEW_EQUIPMENT', 'VIEW_FORMS', 
                    'CREATE_FORMS', 'EDIT_FORMS', 'DELETE_FORMS', 
                    'SUBMIT_FORMS', 'APPROVE_FORMS', 'EXPORT_DATA', 'VIEW_REPORTS',
                    'MANAGE_TODOS', 'VIEW_TODOS', 'CREATE_TODOS', 'EDIT_TODOS', 'DELETE_TODOS'
                ]);
                
                db.run(`INSERT INTO users (
                    id, username, email, password_hash, full_name, role, 
                    department, shift_pattern, permissions, is_active
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`, 
                [
                    adminId,
                    'admin',
                    'admin@aeci.com',
                    hashedPassword,
                    'System Administrator',
                    'ADMIN',
                    'IT Administration',
                    'Day Shift',
                    permissions,
                    1
                ], (err) => {
                    if (err) {
                        console.error('Error creating default admin user:', err);
                        reject(err);
                        return;
                    }
                    
                    console.log('✅ Default admin user created:');
                    console.log('   Username: admin');
                    console.log('   Password: AECIAdmin2025!');
                    console.log('   ⚠️  Please change the password after first login!');
                    resolve();
                });
            } else {
                console.log('✅ Admin user already exists');
                resolve();
            }
        });
    });
}

// Initialize database on startup
console.log('🔧 Initializing database...');
initializeDatabase();

// Health check endpoint
app.get('/api/health', (req, res) => {
    res.json({
        success: true,
        message: 'AECI MMU Companion Server is running',
        version: '2.0.0',
        timestamp: new Date().toISOString(),
        uptime: process.uptime()
    });
});

// Authentication endpoints
app.post('/api/auth/login', async (req, res) => {
    try {
        const { username, password } = req.body;
        
        if (!username || !password) {
            return res.status(400).json({
                success: false,
                message: 'Username and password are required'
            });
        }
        
        db.get("SELECT * FROM users WHERE username = ? AND is_active = 1", [username], async (err, user) => {
            if (err) {
                console.error('Database error:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Internal server error'
                });
            }
            
            if (!user) {
                return res.status(401).json({
                    success: false,
                    message: 'Invalid credentials'
                });
            }
            
            const validPassword = await bcrypt.compare(password, user.password_hash);
            if (!validPassword) {
                return res.status(401).json({
                    success: false,
                    message: 'Invalid credentials'
                });
            }
            
            // Update last login
            db.run("UPDATE users SET last_login_at = ? WHERE id = ?", 
                   [Math.floor(Date.now() / 1000), user.id]);
            
            // Generate JWT token
            const token = jwt.sign(
                { 
                    id: user.id, 
                    username: user.username, 
                    role: user.role,
                    permissions: JSON.parse(user.permissions || '[]')
                },
                process.env.JWT_SECRET,
                { expiresIn: process.env.JWT_EXPIRES_IN || '24h' }
            );
            
            res.json({
                success: true,
                message: 'Login successful',
                token,
                user: {
                    id: user.id,
                    username: user.username,
                    email: user.email,
                    full_name: user.full_name,
                    role: user.role,
                    department: user.department,
                    shift_pattern: user.shift_pattern,
                    permissions: JSON.parse(user.permissions || '[]'),
                    requires_password_change: Boolean(user.requires_password_change),
                    biometric_enabled: Boolean(user.biometric_enabled)
                }
            });
        });
        
    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({
            success: false,
            message: 'Internal server error'
        });
    }
});

// Change password endpoint
app.post('/api/auth/change-password', authenticateToken, async (req, res) => {
    try {
        const { currentPassword, newPassword } = req.body;
        const userId = req.user.id;
        
        if (!currentPassword || !newPassword) {
            return res.status(400).json({
                success: false,
                message: 'Current password and new password are required'
            });
        }
        
        if (newPassword.length < 8) {
            return res.status(400).json({
                success: false,
                message: 'New password must be at least 8 characters long'
            });
        }
        
        db.get("SELECT password_hash FROM users WHERE id = ?", [userId], async (err, user) => {
            if (err || !user) {
                return res.status(500).json({
                    success: false,
                    message: 'Internal server error'
                });
            }
            
            const validCurrentPassword = await bcrypt.compare(currentPassword, user.password_hash);
            if (!validCurrentPassword) {
                return res.status(400).json({
                    success: false,
                    message: 'Current password is incorrect'
                });
            }
            
            const hashedNewPassword = await bcrypt.hash(newPassword, 12);
            
            db.run(`UPDATE users SET password_hash = ?, requires_password_change = 0 WHERE id = ?`,
                   [hashedNewPassword, userId], (err) => {
                if (err) {
                    console.error('Error updating password:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Internal server error'
                    });
                }
                
                res.json({
                    success: true,
                    message: 'Password changed successfully'
                });
            });
        });
        
    } catch (error) {
        console.error('Change password error:', error);
        res.status(500).json({
            success: false,
            message: 'Internal server error'
        });
    }
});

// Get current user info
app.get('/api/auth/me', authenticateToken, (req, res) => {
    const userId = req.user.id;
    
    db.get(`SELECT id, username, email, full_name, role, department, 
                   shift_pattern, permissions, requires_password_change, 
                   biometric_enabled, last_login_at, created_at 
            FROM users WHERE id = ?`, [userId], (err, user) => {
        if (err || !user) {
            return res.status(500).json({
                success: false,
                message: 'Internal server error'
            });
        }
        
        res.json({
            success: true,
            user: {
                ...user,
                permissions: JSON.parse(user.permissions || '[]'),
                requires_password_change: Boolean(user.requires_password_change),
                biometric_enabled: Boolean(user.biometric_enabled)
            }
        });
    });
});

// Get all users (admin only)
app.get('/api/users', authenticateToken, (req, res) => {
    // Check if user is admin
    if (req.user.role !== 'ADMIN') {
        return res.status(403).json({
            success: false,
            message: 'Admin access required'
        });
    }

    db.all("SELECT id, username, email, full_name, role, department, shift_pattern, permissions, is_active, last_login_at, created_at FROM users ORDER BY full_name", 
           (err, users) => {
        if (err) {
            console.error('Database error:', err);
            return res.status(500).json({
                success: false,
                message: 'Internal server error'
            });
        }

        res.json({
            success: true,
            users: users.map(user => ({
                ...user,
                permissions: JSON.parse(user.permissions || '[]')
            }))
        });
    });
});

// Create user (admin only)
app.post('/api/users', authenticateToken, async (req, res) => {
    if (req.user.role !== 'ADMIN') {
        return res.status(403).json({
            success: false,
            message: 'Admin access required'
        });
    }

    try {
        const { 
            username, email, fullName, role, department, 
            shiftPattern, permissions, tempPassword 
        } = req.body;

        if (!username || !email || !fullName || !tempPassword) {
            return res.status(400).json({
                success: false,
                message: 'Required fields: username, email, fullName, tempPassword'
            });
        }

        const userId = uuidv4();
        const hashedPassword = await bcrypt.hash(tempPassword, 12);

        db.run(`INSERT INTO users (
            id, username, email, password_hash, full_name, role, 
            department, shift_pattern, permissions, requires_password_change
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`, 
        [
            userId, username, email, hashedPassword, fullName, role || 'OPERATOR',
            department, shiftPattern, JSON.stringify(permissions || []), 1
        ], (err) => {
            if (err) {
                if (err.code === 'SQLITE_CONSTRAINT') {
                    return res.status(400).json({
                        success: false,
                        message: 'Username or email already exists'
                    });
                }
                console.error('Error creating user:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Internal server error'
                });
            }

            res.status(201).json({
                success: true,
                message: 'User created successfully',
                userId
            });
        });
    } catch (error) {
        console.error('User creation error:', error);
        res.status(500).json({
            success: false,
            message: 'Internal server error'
        });
    }
});

// Include report management endpoints
const reportEndpoints = require('./report-endpoints');
reportEndpoints(app, db, authenticateToken, upload);

// Include todo management endpoints
const todoEndpoints = require('./todo-endpoints');
todoEndpoints(app, db, authenticateToken, upload);

// Get server network information
app.get('/api/network-info', (req, res) => {
    const os = require('os');
    const interfaces = os.networkInterfaces();
    const addresses = [];
    
    for (const k in interfaces) {
        for (const k2 in interfaces[k]) {
            const address = interfaces[k][k2];
            if (address.family === 'IPv4' && !address.internal) {
                addresses.push({
                    interface: k,
                    address: address.address,
                    url: `http://${address.address}:${PORT}`
                });
            }
        }
    }
    
    res.json({
        success: true,
        port: PORT,
        addresses: addresses,
        hostname: os.hostname()
    });
});

// Start server
const server = app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 AECI MMU Companion Server running on port ${PORT}`);
    console.log(`📋 Health check: http://localhost:${PORT}/api/health`);
    console.log(`🔐 Login endpoint: http://localhost:${PORT}/api/auth/login`);
    console.log(`📊 Network info: http://localhost:${PORT}/api/network-info`);
    console.log(`\n📱 For Android devices, use your phone's IP address`);
    console.log(`   Example: http://192.168.1.100:${PORT}`);
    console.log(`   Check /api/network-info for available addresses`);
});

// Graceful shutdown
process.on('SIGINT', () => {
    console.log('\n🛑 Shutting down server...');
    server.close(() => {
        db.close((err) => {
            if (err) {
                console.error('Error closing database:', err.message);
            } else {
                console.log('📦 Database connection closed.');
            }
            process.exit(0);
        });
    });
});

module.exports = { app, db };
EOF

echo "✅ Server setup complete!"
echo ""
echo "📋 Next Steps:"
echo "1. Run: npm start"
echo "2. Check your server's IP address at: http://localhost:3000/api/network-info"
echo "3. Use that IP address in your Android app"
echo ""
echo "🔐 Default Admin Credentials:"
echo "   Username: admin"
echo "   Password: AECIAdmin2025!"
echo ""
echo "📱 Server will be accessible from your network at your phone's IP address"
echo "   Example: http://192.168.1.100:3000"
