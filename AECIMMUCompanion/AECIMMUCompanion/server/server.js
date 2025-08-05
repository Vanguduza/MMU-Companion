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
        const allowedTypes = ['.pdf', '.xlsx', '.xls', '.csv'];
        const ext = path.extname(file.originalname).toLowerCase();
        if (allowedTypes.includes(ext)) {
            cb(null, true);
        } else {
            cb(new Error('Invalid file type. Only PDF, Excel, and CSV files are allowed.'));
        }
    }
});

const app = express();
const PORT = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET || 'aeci-mmu-companion-secret-key-2025';

// Security middleware
app.use(helmet());
app.use(cors({
    origin: ['http://localhost:3000', 'http://10.0.2.2:3000'], // Android emulator
    credentials: true
}));

// Rate limiting
const limiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 100 // limit each IP to 100 requests per windowMs
});
app.use('/api/', limiter);

// Stricter rate limiting for auth endpoints
const authLimiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 10 // limit each IP to 10 auth requests per windowMs
});

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Initialize SQLite database
const db = new sqlite3.Database('./aeci_mmu.db', (err) => {
    if (err) {
        console.error('Error opening database:', err.message);
    } else {
        console.log('Connected to SQLite database.');
        initializeDatabase();
    }
});

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
                                            
                                            console.log('Database tables initialized.');
                                            
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
                    'SUBMIT_FORMS', 'APPROVE_FORMS', 'EXPORT_DATA', 'VIEW_REPORTS'
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
                        console.error('Error creating default admin:', err);
                        reject(err);
                    } else {
                        console.log('✅ Default admin user created successfully!');
                        console.log('   Username: admin');
                        console.log('   Password: AECIAdmin2025!');
                        console.log('   Email: admin@aeci.com');
                        resolve();
                    }
                });
            } else {
                console.log('Admin user already exists.');
                resolve();
            }
        });
    });
}

// Email transporter setup (you'll need to configure with real SMTP settings)
const emailTransporter = nodemailer.createTransport({
    host: process.env.SMTP_HOST || 'smtp.gmail.com',
    port: process.env.SMTP_PORT || 587,
    secure: false,
    auth: {
        user: process.env.SMTP_USER || 'your-email@gmail.com',
        pass: process.env.SMTP_PASS || 'your-app-password'
    }
});

// Middleware to verify JWT token
function authenticateToken(req, res, next) {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (!token) {
        return res.status(401).json({ success: false, message: 'Access token required' });
    }

    jwt.verify(token, JWT_SECRET, (err, user) => {
        if (err) {
            return res.status(403).json({ success: false, message: 'Invalid or expired token' });
        }
        req.user = user;
        next();
    });
}

// Routes

// Health check
app.get('/api/health', (req, res) => {
    res.json({ 
        success: true, 
        message: 'AECI MMU Companion Server is running',
        timestamp: new Date().toISOString(),
        version: '1.0.0'
    });
});

// Authentication endpoints
app.post('/api/auth/login', authLimiter, async (req, res) => {
    try {
        const { username, password, deviceId } = req.body;

        if (!username || !password) {
            return res.status(400).json({
                success: false,
                message: 'Username and password are required'
            });
        }

        // Find user by username
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

            // Verify password
            const isValidPassword = await bcrypt.compare(password, user.password_hash);
            if (!isValidPassword) {
                return res.status(401).json({
                    success: false,
                    message: 'Invalid credentials'
                });
            }

            // Update last login
            db.run("UPDATE users SET last_login_at = ? WHERE id = ?", 
                [Date.now(), user.id]);

            // Generate JWT tokens
            const token = jwt.sign(
                { userId: user.id, username: user.username, role: user.role },
                JWT_SECRET,
                { expiresIn: '24h' }
            );

            const refreshToken = jwt.sign(
                { userId: user.id, type: 'refresh' },
                JWT_SECRET,
                { expiresIn: '7d' }
            );

            // Return user data and tokens
            res.json({
                success: true,
                message: 'Login successful',
                token,
                refreshToken,
                expiresIn: 24 * 60 * 60 * 1000, // 24 hours in milliseconds
                user: {
                    id: user.id,
                    username: user.username,
                    fullName: user.full_name,
                    email: user.email,
                    role: user.role,
                    department: user.department,
                    shiftPattern: user.shift_pattern,
                    permissions: JSON.parse(user.permissions || '[]'),
                    isActive: user.is_active,
                    lastLoginAt: user.last_login_at,
                    biometricEnabled: user.biometric_enabled,
                    requiresPasswordChange: user.requires_password_change
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

// Password reset request
app.post('/api/auth/password-reset/request', authLimiter, (req, res) => {
    try {
        const { email, resetToken, deviceId } = req.body;

        if (!email) {
            return res.status(400).json({
                success: false,
                message: 'Email is required'
            });
        }

        // Find user by email
        db.get("SELECT * FROM users WHERE email = ? AND is_active = 1", [email], (err, user) => {
            if (err) {
                console.error('Database error:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Internal server error'
                });
            }

            if (!user) {
                // Don't reveal if email exists
                return res.json({
                    success: true,
                    message: 'If the email exists, a reset link has been sent',
                    emailSent: false
                });
            }

            // Generate reset token
            const token = resetToken || uuidv4();
            const expiresAt = Date.now() + (24 * 60 * 60 * 1000); // 24 hours

            // Store reset token
            db.run(`INSERT INTO password_reset_tokens (user_id, token, expires_at) 
                    VALUES (?, ?, ?)`, [user.id, token, expiresAt], (err) => {
                if (err) {
                    console.error('Error storing reset token:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Internal server error'
                    });
                }

                // Send email (in production, use real email service)
                const resetUrl = `http://localhost:3000/reset-password?token=${token}`;
                const mailOptions = {
                    from: process.env.SMTP_USER || 'noreply@aeci.com',
                    to: email,
                    subject: 'AECI MMU Companion - Password Reset',
                    html: `
                        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                            <h2 style="color: #1976d2;">Password Reset Request</h2>
                            <p>Hello ${user.full_name},</p>
                            <p>You have requested to reset your password for the AECI MMU Companion app.</p>
                            <p>Your reset token is: <strong>${token}</strong></p>
                            <p>Or click the link below to reset your password:</p>
                            <a href="${resetUrl}" style="background-color: #1976d2; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; display: inline-block;">Reset Password</a>
                            <p>This link will expire in 24 hours.</p>
                            <p>If you did not request this reset, please ignore this email.</p>
                            <hr>
                            <p style="color: #666; font-size: 12px;">AECI Mining Solutions</p>
                        </div>
                    `
                };

                // For development, log the reset token instead of sending email
                console.log(`🔑 Password reset token for ${email}: ${token}`);
                console.log(`🔗 Reset URL: ${resetUrl}`);

                res.json({
                    success: true,
                    message: 'Password reset email sent successfully',
                    emailSent: true
                });

                // Uncomment to actually send email in production
                /*
                emailTransporter.sendMail(mailOptions, (error, info) => {
                    if (error) {
                        console.error('Email sending error:', error);
                    } else {
                        console.log('Password reset email sent:', info.response);
                    }
                });
                */
            });
        });
    } catch (error) {
        console.error('Password reset request error:', error);
        res.status(500).json({
            success: false,
            message: 'Internal server error'
        });
    }
});

// Verify password reset token
app.post('/api/auth/password-reset/verify', (req, res) => {
    try {
        const { token } = req.body;

        if (!token) {
            return res.status(400).json({
                success: false,
                message: 'Reset token is required'
            });
        }

        db.get(`SELECT * FROM password_reset_tokens 
                WHERE token = ? AND used = 0 AND expires_at > ?`, 
                [token, Date.now()], (err, resetToken) => {
            if (err) {
                console.error('Database error:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Internal server error'
                });
            }

            res.json({
                success: true,
                valid: !!resetToken,
                message: resetToken ? 'Token is valid' : 'Invalid or expired token'
            });
        });
    } catch (error) {
        console.error('Token verification error:', error);
        res.status(500).json({
            success: false,
            message: 'Internal server error'
        });
    }
});

// Complete password reset
app.post('/api/auth/password-reset/complete', authLimiter, async (req, res) => {
    try {
        const { token, newPassword } = req.body;

        if (!token || !newPassword) {
            return res.status(400).json({
                success: false,
                message: 'Token and new password are required'
            });
        }

        if (newPassword.length < 8) {
            return res.status(400).json({
                success: false,
                message: 'Password must be at least 8 characters long'
            });
        }

        db.get(`SELECT * FROM password_reset_tokens 
                WHERE token = ? AND used = 0 AND expires_at > ?`, 
                [token, Date.now()], async (err, resetToken) => {
            if (err) {
                console.error('Database error:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Internal server error'
                });
            }

            if (!resetToken) {
                return res.status(400).json({
                    success: false,
                    message: 'Invalid or expired reset token'
                });
            }

            try {
                // Hash new password
                const hashedPassword = await bcrypt.hash(newPassword, 12);

                // Update user password and mark as not requiring password change
                db.run(`UPDATE users 
                        SET password_hash = ?, requires_password_change = 0 
                        WHERE id = ?`, 
                        [hashedPassword, resetToken.user_id], (err) => {
                    if (err) {
                        console.error('Error updating password:', err);
                        return res.status(500).json({
                            success: false,
                            message: 'Internal server error'
                        });
                    }

                    // Mark reset token as used
                    db.run("UPDATE password_reset_tokens SET used = 1 WHERE token = ?", 
                           [token]);

                    res.json({
                        success: true,
                        message: 'Password reset successfully'
                    });
                });
            } catch (hashError) {
                console.error('Password hashing error:', hashError);
                res.status(500).json({
                    success: false,
                    message: 'Internal server error'
                });
            }
        });
    } catch (error) {
        console.error('Password reset completion error:', error);
        res.status(500).json({
            success: false,
            message: 'Internal server error'
        });
    }
});

// Get users (admin only)
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

// Start server
app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 AECI MMU Companion Server running on port ${PORT}`);
    console.log(`📋 Health check: http://localhost:${PORT}/api/health`);
    console.log(`🔐 Login endpoint: http://localhost:${PORT}/api/auth/login`);
    console.log(`\n📱 For Android emulator, use: http://10.0.2.2:${PORT}`);
});

// Graceful shutdown
process.on('SIGINT', () => {
    console.log('\n🛑 Shutting down server...');
    db.close((err) => {
        if (err) {
            console.error('Error closing database:', err.message);
        } else {
            console.log('📦 Database connection closed.');
        }
        process.exit(0);
    });
});

module.exports = app;
