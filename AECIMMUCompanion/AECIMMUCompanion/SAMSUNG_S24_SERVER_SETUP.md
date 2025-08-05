# Samsung S24 Server Setup Guide
## AECI MMU Companion Server Configuration

This guide provides detailed instructions for setting up the AECI MMU Companion server on your Samsung S24 phone to act as a local server for database, credential verification, data syncing, and other cloud functions.

## Prerequisites

- Samsung S24 with Android 14+
- At least 2GB free storage space
- WiFi or mobile data connection for initial setup
- USB cable (optional, for file transfer)

## Step 1: Install Termux

### Option A: F-Droid (Recommended)
1. Download F-Droid APK from https://f-droid.org/
2. Install F-Droid and open it
3. Search for "Termux" and install it
4. Search for "Termux:API" and install it

### Option B: Google Play Store
1. Open Google Play Store
2. Search for "Termux" and install it
3. Note: Play Store version may have limitations

## Step 2: Configure Termux

1. **Open Termux** and run these commands:

```bash
# Update package list
pkg update -y

# Install required packages
pkg install -y nodejs npm git curl wget

# Install additional tools
pkg install -y sqlite python

# Install process manager
npm install -g pm2

# Verify installations
node --version
npm --version
sqlite3 --version
```

## Step 3: Set Up Server Directory

```bash
# Create server directory
mkdir -p ~/aeci-server
cd ~/aeci-server

# Create necessary subdirectories
mkdir -p reports logs data
```

## Step 4: Transfer Server Files

### Option A: Using Git (Recommended)
```bash
# Clone the repository (if you have it on GitHub/GitLab)
git clone https://your-repository-url.git ~/aeci-server

# Or download from a direct link
wget https://your-server-files-url.zip
unzip server-files.zip -d ~/aeci-server/
```

### Option B: Using ADB (if you have developer options enabled)
```bash
# On your computer, connect phone via USB and run:
adb push AECIMMUCompanion/server/ /sdcard/aeci-server/

# Then in Termux:
cp -r /sdcard/aeci-server/* ~/aeci-server/
```

### Option C: Manual File Transfer
1. Copy server files to your phone's Downloads folder
2. In Termux, run:
```bash
cp -r /sdcard/Download/AECIMMUCompanion/server/* ~/aeci-server/
```

## Step 5: Install Server Dependencies

```bash
cd ~/aeci-server

# Install Node.js dependencies
npm install

# Install additional dependencies if needed
npm install -g nodemon
```

## Step 6: Configure Environment

```bash
# Create environment file
cat > .env << EOF
PORT=3000
JWT_SECRET=aeci-mmu-companion-secret-key-2025-samsung-s24
NODE_ENV=production
DB_PATH=./data/aeci_mmu.db
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USER=your-email@gmail.com
EMAIL_PASS=your-app-password
LOG_LEVEL=info
ENABLE_CORS=true
CORS_ORIGIN=*
EOF
```

## Step 7: Configure Network Access

### Get Your Phone's IP Address
```bash
# Get WiFi IP address
ip addr show wlan0 | grep "inet " | awk '{print $2}' | cut -d/ -f1

# Or get all network interfaces
ip addr show | grep "inet " | grep -v "127.0.0.1"
```

### Configure Firewall (if needed)
```bash
# Allow incoming connections on port 3000
# This depends on your phone's security settings
# Most Android phones don't have a firewall by default
```

## Step 8: Start the Server

### Option A: Direct Start
```bash
# Start the server
node server.js
```

### Option B: Using PM2 (Recommended for production)
```bash
# Start with PM2
pm2 start server.js --name "aeci-server"

# Save PM2 configuration
pm2 save

# Set up auto-start
pm2 startup

# Monitor the process
pm2 status
pm2 logs aeci-server
```

### Option C: Using Nodemon (for development)
```bash
# Start with auto-restart
nodemon server.js
```

## Step 9: Test Server Connection

### From Termux
```bash
# Test health endpoint
curl http://localhost:3000/api/health

# Test from your phone's IP
curl http://YOUR_PHONE_IP:3000/api/health
```

### From Another Device
```bash
# Test from your computer or another device
curl http://YOUR_PHONE_IP:3000/api/health
```

## Step 10: Configure App to Connect

1. **Open the AECI MMU Companion app**
2. **Go to Settings → Server Configuration**
3. **Enter your phone's IP address**: `http://YOUR_PHONE_IP:3000/api/`
4. **Test the connection**
5. **Save the configuration**

## Step 11: Set Up Auto-Start (Optional)

### Using Termux Boot
```bash
# Install Termux:Boot
# Download from F-Droid: https://f-droid.org/packages/com.termux.boot/

# Create boot script
mkdir -p ~/.termux/boot
cat > ~/.termux/boot/start-aeci-server.sh << EOF
#!/data/data/com.termux/files/usr/bin/sh
cd ~/aeci-server
pm2 start server.js --name "aeci-server"
EOF

# Make script executable
chmod +x ~/.termux/boot/start-aeci-server.sh
```

## Step 12: Monitor and Maintain

### Check Server Status
```bash
# Check if server is running
pm2 status

# View logs
pm2 logs aeci-server

# Monitor resources
pm2 monit
```

### Backup Database
```bash
# Create backup directory
mkdir -p ~/aeci-backups

# Backup database
cp ~/aeci-server/data/aeci_mmu.db ~/aeci-backups/aeci_mmu_$(date +%Y%m%d_%H%M%S).db
```

### Update Server
```bash
# Stop server
pm2 stop aeci-server

# Update code (if using git)
cd ~/aeci-server
git pull

# Update dependencies
npm install

# Start server
pm2 start aeci-server
```

## Troubleshooting

### Server Won't Start
```bash
# Check if port is in use
netstat -tulpn | grep :3000

# Check Node.js version
node --version

# Check logs
pm2 logs aeci-server
```

### Connection Issues
```bash
# Check if server is listening
netstat -tulpn | grep :3000

# Test local connection
curl http://localhost:3000/api/health

# Check firewall settings
# Most Android phones don't block local connections
```

### Database Issues
```bash
# Check database file
ls -la ~/aeci-server/data/

# Repair database (if needed)
sqlite3 ~/aeci-server/data/aeci_mmu.db "VACUUM;"
```

### Performance Issues
```bash
# Monitor resource usage
pm2 monit

# Check available memory
free -h

# Check disk space
df -h
```

## Security Considerations

1. **Change Default JWT Secret**: Update the JWT_SECRET in .env file
2. **Use HTTPS in Production**: Consider setting up SSL certificates
3. **Restrict Network Access**: Only allow connections from trusted devices
4. **Regular Backups**: Set up automated database backups
5. **Monitor Logs**: Regularly check server logs for suspicious activity

## Network Configuration

### Common IP Addresses
- **WiFi IP**: Usually `192.168.1.x` or `192.168.0.x`
- **Mobile Hotspot**: Usually `192.168.43.x` or `192.168.42.x`
- **USB Tethering**: Usually `192.168.42.x`

### Port Configuration
- **Default Port**: 3000
- **Change Port**: Update PORT in .env file and app configuration
- **Firewall**: Ensure port is accessible on your network

## Advanced Configuration

### SSL/HTTPS Setup
```bash
# Install certbot (if available)
pkg install -y certbot

# Generate self-signed certificate
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes
```

### Load Balancing
```bash
# Install nginx (if available)
pkg install -y nginx

# Configure nginx for load balancing
# (Advanced setup - not covered in this guide)
```

### Database Optimization
```bash
# Optimize SQLite database
sqlite3 ~/aeci-server/data/aeci_mmu.db "PRAGMA optimize;"
sqlite3 ~/aeci-server/data/aeci_mmu.db "VACUUM;"
```

## Support and Maintenance

### Regular Tasks
- [ ] Monitor server logs weekly
- [ ] Backup database monthly
- [ ] Update dependencies quarterly
- [ ] Check disk space monthly
- [ ] Test app connectivity weekly

### Emergency Procedures
1. **Server Down**: Restart with `pm2 restart aeci-server`
2. **Database Corrupted**: Restore from backup
3. **Network Issues**: Check IP address and firewall
4. **App Connection Issues**: Verify server URL in app settings

## Conclusion

Your Samsung S24 is now configured as a local server for the AECI MMU Companion app. The server will:

- ✅ Handle user authentication and credential verification
- ✅ Store and sync data between devices
- ✅ Generate and manage reports
- ✅ Provide real-time equipment status updates
- ✅ Support offline-first operation with automatic sync

The app will automatically connect to your phone server whenever internet connectivity is available, providing a seamless experience for all users in your network.

## Quick Reference

### Common Commands
```bash
# Start server
pm2 start aeci-server

# Stop server
pm2 stop aeci-server

# Restart server
pm2 restart aeci-server

# View logs
pm2 logs aeci-server

# Check status
pm2 status

# Get IP address
ip addr show wlan0 | grep "inet " | awk '{print $2}' | cut -d/ -f1
```

### Default URLs
- **Health Check**: `http://YOUR_IP:3000/api/health`
- **API Base**: `http://YOUR_IP:3000/api/`
- **Admin Interface**: `http://YOUR_IP:3000/admin/`

### Default Credentials
- **Admin Username**: `admin`
- **Admin Password**: `AECIAdmin2025!`
- **Database**: `~/aeci-server/data/aeci_mmu.db` 