# AECI MMU Companion Phone Server Setup Guide

## Overview
This guide will help you set up a complete server on your Android phone using the Userland app, which will host all the todo management features and API endpoints for the AECI MMU Companion app.

## Prerequisites
- Android phone with Userland app installed
- Stable WiFi connection
- At least 2GB free storage space

## Step 1: Install and Setup Userland

1. **Install Userland from Google Play Store**
   - Search for "UserLAnd" and install it
   - Open the app and create an account if needed

2. **Create Ubuntu Session**
   - Tap "Ubuntu" from the list of distributions
   - Choose "SSH" as the service type
   - Set up username and password (remember these!)
   - Wait for the installation to complete (this may take 10-15 minutes)

## Step 2: Connect to Your Ubuntu Environment

1. **Start SSH Session**
   - Once installation is complete, tap "Ubuntu" to start
   - You'll see a terminal interface
   - You're now in a full Ubuntu Linux environment on your phone!

## Step 3: Install the AECI MMU Companion Server

1. **Download the setup script**
   ```bash
   wget https://raw.githubusercontent.com/your-repo/setup-userland-server.sh
   chmod +x setup-userland-server.sh
   ```

   **OR** if you don't have the script online, create it manually:
   ```bash
   nano setup-server.sh
   ```
   Then copy the entire setup script content (from the file we created) and paste it.

2. **Run the setup script**
   ```bash
   ./setup-userland-server.sh
   ```
   
   This will:
   - Install Node.js and npm
   - Create the server directory
   - Install all dependencies
   - Set up the database schema
   - Create default admin user

## Step 4: Configure Your Server

1. **Edit the environment configuration**
   ```bash
   cd /home/mmu-companion-server
   nano .env
   ```

2. **Update server settings as needed**
   - The default port is 3000
   - JWT secret is pre-configured
   - You can modify email settings if needed

## Step 5: Start Your Server

1. **Start the server**
   ```bash
   npm start
   ```

2. **The server will start and display:**
   ```
   🚀 AECI MMU Companion Server running on port 3000
   📋 Health check: http://localhost:3000/api/health
   🔐 Login endpoint: http://localhost:3000/api/auth/login
   📊 Network info: http://localhost:3000/api/network-info
   
   📱 For Android devices, use your phone's IP address
      Example: http://192.168.1.100:3000
      Check /api/network-info for available addresses
   
   ✅ Default admin user created:
      Username: admin
      Password: AECIAdmin2025!
      ⚠️  Please change the password after first login!
   ```

## Step 6: Find Your Phone's IP Address

1. **Get network information** (in a new terminal tab or after the server starts):
   ```bash
   curl http://localhost:3000/api/network-info
   ```

2. **Or use system commands**:
   ```bash
   ip addr show | grep "inet " | grep -v 127.0.0.1
   ```

3. **Example output**:
   ```json
   {
     "success": true,
     "port": 3000,
     "addresses": [
       {
         "interface": "wlan0",
         "address": "192.168.1.100",
         "url": "http://192.168.1.100:3000"
       }
     ],
     "hostname": "localhost"
   }
   ```

## Step 7: Configure the Android App

1. **Update the app configuration**
   - In the Android project, open `ApiConfig.kt`
   - Replace `192.168.1.100` with your phone's actual IP address
   - Build and install the updated app

2. **Example configuration**:
   ```kotlin
   private const val DEFAULT_BASE_URL = "http://192.168.1.150:3000"  // Your phone's IP
   ```

## Step 8: Test the Connection

1. **Test from another device on the same network**:
   ```bash
   curl http://192.168.1.100:3000/api/health
   ```

2. **Expected response**:
   ```json
   {
     "success": true,
     "message": "AECI MMU Companion Server is running",
     "version": "2.0.0",
     "timestamp": "2025-01-27T10:30:00.000Z",
     "uptime": 123.45
   }
   ```

## Step 9: Using the Server

### Default Admin Credentials
- **Username**: `admin`
- **Password**: `AECIAdmin2025!`
- **⚠️ Important**: Change this password immediately after first login!

### API Endpoints Available
- `GET /api/health` - Server health check
- `POST /api/auth/login` - User authentication
- `GET /api/todos` - Get all todos
- `POST /api/todos` - Create new todo
- `PUT /api/todos/{id}` - Update todo
- `DELETE /api/todos/{id}` - Delete todo
- `GET /api/todos/analytics` - Get analytics
- And many more...

### Database Features
- ✅ Complete todo management with 30+ fields
- ✅ Time tracking and progress monitoring
- ✅ Comments and attachments
- ✅ Job card integration
- ✅ Advanced analytics
- ✅ User management
- ✅ Equipment tracking
- ✅ Form management

## Troubleshooting

### Server Won't Start
1. Check if port 3000 is already in use:
   ```bash
   lsof -i :3000
   ```
2. Kill any existing process:
   ```bash
   pkill -f node
   ```
3. Try starting again:
   ```bash
   npm start
   ```

### Can't Connect from Android App
1. Ensure both devices are on the same WiFi network
2. Check your phone's firewall settings
3. Verify the IP address is correct
4. Try pinging the server:
   ```bash
   ping 192.168.1.100
   ```

### Database Issues
1. Check database permissions:
   ```bash
   ls -la aeci_mmu_companion.db
   ```
2. Reset database if needed:
   ```bash
   rm aeci_mmu_companion.db
   npm start  # Will recreate the database
   ```

### Performance Optimization
1. Keep your phone plugged in and connected to power
2. Ensure stable WiFi connection
3. Consider using a phone cooling fan for extended usage
4. Monitor memory usage with:
   ```bash
   htop
   ```

## Keeping the Server Running

### Run in Background
```bash
nohup npm start > server.log 2>&1 &
```

### Auto-restart on Crash
```bash
npm install -g pm2
pm2 start server.js --name aeci-server
pm2 startup
pm2 save
```

### Monitor Server Logs
```bash
tail -f server.log
# or with pm2:
pm2 logs aeci-server
```

## Security Considerations

1. **Change Default Password**: Immediately change the admin password
2. **Network Security**: Only use on trusted networks
3. **Regular Updates**: Keep the server dependencies updated
4. **Backup Data**: Regularly backup your database file

## Advanced Configuration

### Custom Port
Edit `.env` file:
```env
PORT=8080
```

### Email Notifications (Optional)
Update `.env` with your email settings:
```env
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USER=your-email@gmail.com
EMAIL_PASS=your-app-password
```

### SSL/HTTPS (Advanced)
For production use, consider setting up SSL certificates.

## Support and Updates

- Check server logs for errors: `tail -f server.log`
- Monitor performance: `htop` or `pm2 monit`
- Update dependencies: `npm update`
- Backup database: `cp aeci_mmu_companion.db backup-$(date +%Y%m%d).db`

---

**🎉 Congratulations!** Your phone is now running a complete server with all AECI MMU Companion features. The app can now sync todos, track time, manage tasks, and provide comprehensive analytics - all hosted on your own device!
