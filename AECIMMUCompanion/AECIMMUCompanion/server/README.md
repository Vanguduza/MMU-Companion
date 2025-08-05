# AECI MMU Companion Server

A Node.js Express server for the AECI MMU Companion Android application, providing authentication, user management, and data synchronization services.

## 🚀 Quick Start

### Prerequisites

- Node.js (v16 or higher)
- npm or yarn
- Git

### Installation

1. **Navigate to the server directory:**
   ```bash
   cd server
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Set up environment variables:**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

4. **Start the server:**
   ```bash
   # Development mode with auto-restart
   npm run dev
   
   # Production mode
   npm start
   ```

5. **Verify server is running:**
   ```bash
   curl http://localhost:3000/api/health
   ```

## 📱 Android Connection

### For Android Emulator
- Use IP: `10.0.2.2:3000`
- The server automatically listens on `0.0.0.0` for emulator access

### For Physical Device
- Ensure both device and computer are on the same network
- Use your computer's local IP address (e.g., `192.168.1.100:3000`)

## 🔐 Default Admin Credentials

When the server starts for the first time, it automatically creates a default admin user:

- **Username:** `admin`
- **Password:** `AECIAdmin2025!`
- **Email:** `admin@aeci.com`

⚠️ **Important:** Change this password immediately after first login!

## 📋 API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/password-reset/request` - Request password reset
- `POST /api/auth/password-reset/verify` - Verify reset token
- `POST /api/auth/password-reset/complete` - Complete password reset

### User Management (Admin only)
- `GET /api/users` - Get all users
- `POST /api/users` - Create new user

### Health Check
- `GET /api/health` - Server health status

## 🛠️ Configuration

### Environment Variables

Copy `.env.example` to `.env` and configure:

```bash
# Server Configuration
PORT=3000
JWT_SECRET=your-secret-key-here

# Email Configuration (for password reset)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASS=your-app-password
```

### Database

The server uses SQLite for simplicity. The database file (`aeci_mmu.db`) is automatically created on first run.

## 🔒 Security Features

- **JWT Authentication:** Secure token-based authentication
- **Password Hashing:** bcrypt with salt rounds
- **Rate Limiting:** Prevents brute force attacks
- **CORS:** Configured for mobile app access
- **Helmet:** Security headers
- **Input Validation:** Prevents injection attacks

## 📧 Email Setup (Password Reset)

To enable password reset emails, configure SMTP settings in `.env`:

### Gmail Setup
1. Enable 2-factor authentication
2. Generate an app password
3. Use the app password in `SMTP_PASS`

### Other Providers
Update `SMTP_HOST`, `SMTP_PORT`, and credentials accordingly.

## 🧪 Testing

### Test Login
```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "AECIAdmin2025!",
    "deviceId": "test-device"
  }'
```

### Test Password Reset
```bash
curl -X POST http://localhost:3000/api/auth/password-reset/request \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@aeci.com"
  }'
```

## 🔧 Development

### File Structure
```
server/
├── server.js          # Main server file
├── package.json       # Dependencies and scripts
├── .env.example       # Environment template
├── .env               # Your environment config
└── aeci_mmu.db       # SQLite database (created automatically)
```

### Scripts
- `npm start` - Start production server
- `npm run dev` - Start development server with auto-restart

## 🚀 Deployment

### Local Development
The server is ready for local development and testing.

### Production Deployment
For production deployment:

1. **Update environment variables:**
   - Change `JWT_SECRET` to a secure random string
   - Configure real SMTP settings
   - Set `NODE_ENV=production`

2. **Use process manager:**
   ```bash
   npm install -g pm2
   pm2 start server.js --name "aeci-server"
   ```

3. **Set up reverse proxy (Nginx):**
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;
       
       location /api/ {
           proxy_pass http://localhost:3000;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
   }
   ```

## 📊 Database Schema

### Users Table
- User authentication and profile information
- Role-based permissions
- Password reset tracking

### Password Reset Tokens
- Secure token management
- Expiration tracking
- Usage prevention

### Forms (Future)
- Form submissions and history
- User associations

### Equipment (Future)
- Equipment registry
- Maintenance tracking

## 🆘 Troubleshooting

### Common Issues

1. **Server won't start:**
   - Check if port 3000 is available
   - Verify Node.js version (v16+)

2. **Android app can't connect:**
   - Use `10.0.2.2:3000` for emulator
   - Check firewall settings
   - Ensure server is running

3. **Database errors:**
   - Check file permissions
   - Ensure SQLite3 module is installed

4. **Email not sending:**
   - Verify SMTP credentials
   - Check spam folder
   - Review server logs

### Logs
Server logs are printed to console. In production, consider using:
- Winston for structured logging
- Log rotation for file management

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is part of the AECI MMU Companion system. All rights reserved.

---

**Need Help?** Check the server logs or contact the development team.
