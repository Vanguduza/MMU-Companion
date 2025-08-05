# AECI MMU Companion - Complete Setup Guide

This guide will help you set up the complete AECI MMU Companion system with default admin access, server backend, and password reset functionality.

## 🚀 Quick Setup Summary

### Default Admin Credentials
- **Username:** `admin`
- **Password:** `AECIAdmin2025!`
- **Email:** `admin@aeci.com`

⚠️ **IMPORTANT:** You will be prompted to change this password on first login!

## 📋 Prerequisites

### For Development
- **Android Studio** (Arctic Fox or later)
- **Node.js** (v16 or higher)
- **Git**
- **Android SDK** (API level 33+)

### For Testing
- **Android Emulator** or **Physical Android Device**
- **Computer with network connectivity**

## 🔧 Step-by-Step Setup

### Step 1: Clone and Setup Android Project

1. **Open Android Studio**
2. **Open the project:** `c:\Users\VanGuduza\AndroidStudioProjects\AECIMMUCompanion`
3. **Sync project** and resolve any dependencies
4. **Build the project** to ensure everything compiles

### Step 2: Start the Backend Server

1. **Open terminal/command prompt**
2. **Navigate to server directory:**
   ```bash
   cd c:\Users\VanGuduza\AndroidStudioProjects\AECIMMUCompanion\server
   ```

3. **Install dependencies:**
   ```bash
   npm install
   ```

4. **Start the server:**
   ```bash
   npm run dev
   ```

5. **Verify server is running:**
   - You should see: `🚀 AECI MMU Companion Server running on port 3000`
   - Default admin user creation message will appear

### Step 3: Configure Android App for Local Development

The app is already configured to connect to:
- **Android Emulator:** `http://10.0.2.2:3000`
- **Physical Device:** Your computer's local IP

### Step 4: Test the Complete System

1. **Build and run the Android app**
2. **Login with default credentials:**
   - Username: `admin`
   - Password: `AECIAdmin2025!`
3. **You'll be prompted to change the password**
4. **Set a new secure password**
5. **Access the dashboard**

## 🌐 Network Configuration

### For Android Emulator
- **Server URL:** `http://10.0.2.2:3000`
- **No additional configuration needed**

### For Physical Android Device
1. **Find your computer's IP address:**
   ```bash
   # Windows
   ipconfig
   
   # Mac/Linux
   ifconfig
   ```

2. **Ensure both device and computer are on same WiFi network**

3. **Update the API base URL in the app if needed**

## 🔐 Security Features Implemented

### 1. Default Admin User Creation
- ✅ Automatically created on server startup
- ✅ Secure default password
- ✅ Forced password change on first login

### 2. Password Management
- ✅ Password complexity requirements
- ✅ Secure password hashing (bcrypt)
- ✅ Password change enforcement
- ✅ Password reset via email

### 3. Authentication & Authorization
- ✅ JWT token-based authentication
- ✅ Role-based access control
- ✅ Session management
- ✅ Secure token storage

### 4. Network Security
- ✅ HTTPS support (production)
- ✅ CORS configuration
- ✅ Rate limiting
- ✅ Input validation

## 📧 Email Configuration (Optional)

To enable password reset emails:

1. **Edit server/.env:**
   ```bash
   SMTP_HOST=smtp.gmail.com
   SMTP_PORT=587
   SMTP_USER=your-email@gmail.com
   SMTP_PASS=your-app-password
   ```

2. **For Gmail:**
   - Enable 2-factor authentication
   - Generate an app password
   - Use the app password in `SMTP_PASS`

## 🛠️ API Endpoints Available

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/password-reset/request` - Request password reset
- `POST /api/auth/password-reset/verify` - Verify reset token
- `POST /api/auth/password-reset/complete` - Complete password reset

### User Management (Admin Only)
- `GET /api/users` - List all users
- `POST /api/users` - Create new user

### System
- `GET /api/health` - Server health check

## 🧪 Testing the System

### 1. Test Server Health
```bash
curl http://localhost:3000/api/health
```

### 2. Test Login API
```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "AECIAdmin2025!",
    "deviceId": "test-device"
  }'
```

### 3. Test Password Reset
```bash
curl -X POST http://localhost:3000/api/auth/password-reset/request \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@aeci.com"
  }'
```

## 🚨 Troubleshooting

### Common Issues and Solutions

#### 1. App Can't Connect to Server
**Problem:** Network connection errors

**Solutions:**
- ✅ Verify server is running (`npm run dev`)
- ✅ Check IP address configuration
- ✅ Ensure firewall allows port 3000
- ✅ For emulator, use `10.0.2.2:3000`
- ✅ For device, use computer's local IP

#### 2. Build Errors in Android Studio
**Problem:** Compilation failures

**Solutions:**
- ✅ Clean and rebuild project
- ✅ Sync project with Gradle files
- ✅ Check Android SDK versions
- ✅ Verify all dependencies are installed

#### 3. Server Won't Start
**Problem:** Node.js server startup issues

**Solutions:**
- ✅ Check Node.js version (v16+)
- ✅ Run `npm install` again
- ✅ Verify port 3000 is available
- ✅ Check for permission issues

#### 4. Default Admin User Not Created
**Problem:** No admin user in database

**Solutions:**
- ✅ Delete `server/aeci_mmu.db` and restart server
- ✅ Check server logs for errors
- ✅ Verify database permissions

## 📱 Mobile App Features

### Implemented Features
- ✅ **User Authentication** (Username/Password, Biometric, PIN)
- ✅ **Password Management** (Change, Reset, Complexity validation)
- ✅ **Role-Based Access** (Admin, Supervisor, Operator, Maintenance)
- ✅ **Offline Capability** (Local data storage, Background sync)
- ✅ **Form Management** (14 industry-specific forms)
- ✅ **Equipment Tracking** (Registry, maintenance, status)
- ✅ **Reporting & Analytics** (PDF/Excel export, dashboards)
- ✅ **Camera Integration** (Photo capture, signatures)
- ✅ **Modern UI** (Material 3, AECI branding)

### User Roles and Permissions
- **ADMIN:** Full system access, user management
- **SUPERVISOR:** Team oversight, form approval
- **MAINTENANCE:** Equipment maintenance, inspections
- **OPERATOR:** Daily operations, form submission

## 🔄 Workflow

### First-Time Setup Workflow
1. **Server starts** → Creates default admin user
2. **User logs in** with default credentials
3. **System prompts** for password change
4. **User sets** new secure password
5. **User accesses** full application features
6. **Admin can create** additional users

### Daily Usage Workflow
1. **User logs in** with credentials
2. **App syncs** data with server
3. **User completes** forms and tasks
4. **Data saves** locally and syncs to server
5. **Reports generated** and exported as needed

## 🚀 Production Deployment

### For Production Use
1. **Update server configuration:**
   - Change JWT_SECRET to secure random string
   - Configure real SMTP settings
   - Set up proper database (PostgreSQL/MySQL)
   - Enable HTTPS with SSL certificates

2. **Deploy server:**
   - Use process manager (PM2)
   - Set up reverse proxy (Nginx)
   - Configure domain and SSL

3. **Update mobile app:**
   - Change API base URL to production server
   - Build signed APK/AAB
   - Test on multiple devices

## 📞 Support

### Need Help?
- **Check server logs** for error messages
- **Review Android Studio logs** for app issues
- **Verify network connectivity** between app and server
- **Ensure all dependencies** are properly installed

### Development Team Contact
- For technical issues, review the implementation files
- For custom configurations, modify the appropriate config files
- For production deployment, follow the deployment guidelines above

---

**🎉 Congratulations!** Your AECI MMU Companion system is now fully set up with:
- ✅ Default admin access
- ✅ Complete backend server
- ✅ Password reset functionality
- ✅ Network connectivity
- ✅ Production-ready security features

**Next Steps:**
1. Test the complete login flow
2. Create additional users as needed
3. Customize forms and workflows
4. Deploy to production environment
