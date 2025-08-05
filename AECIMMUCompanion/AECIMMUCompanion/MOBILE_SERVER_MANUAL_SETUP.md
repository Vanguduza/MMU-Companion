# AECI MMU Companion - Mobile Server Manual Setup Guide

This guide provides step-by-step instructions for setting up the mobile server for your AECI MMU Companion app. You have two options:

1. **Option A**: On-device server using Termux (Recommended - True mobile server)
2. **Option B**: Alternative setup using ADB port forwarding (Backup solution)

---

## Option A: On-Device Server Using Termux (Recommended)

This approach runs the server directly on your Samsung S24, making it a true mobile server.

### Prerequisites
- Samsung Galaxy S24 with Android 15
- Computer with ADB access
- Wi-Fi network

### Step 1: Install Termux on Your Phone

1. **DO NOT use Google Play Store version** (it's outdated and restricted)
2. Install from one of these sources:
   - **F-Droid**: https://f-droid.org/en/packages/com.termux/
   - **GitHub**: https://github.com/termux/termux-app/releases

3. Also install **Termux:Boot** for auto-start functionality:
   - **F-Droid**: https://f-droid.org/en/packages/com.termux/
   - **GitHub**: https://github.com/termux/termux-boot/releases

### Step 2: Setup Termux Environment

Open Termux on your phone and run these commands:

```bash
# Update package lists
pkg update && pkg upgrade

# Install essential packages
pkg install nodejs npm git openssh sqlite

# Setup storage access (allows access to phone storage)
termux-setup-storage

# Grant storage permission when prompted
```

### Step 3: Transfer Server Files to Phone

**Method 1: Using ADB (Automated)**
```powershell
# Run this on your computer in the project directory
.\deploy-to-phone.ps1 -Action deploy
```

**Method 2: Manual Transfer**
1. Connect phone to computer via USB
2. Enable USB debugging
3. Copy the entire `mobile-server` folder to your phone's storage
4. In Termux, copy files to proper location:
```bash
cp -r /sdcard/mobile-server ~/aeci-mmu-server
cd ~/aeci-mmu-server
```

### Step 4: Install Dependencies and Setup

In Termux on your phone:

```bash
# Navigate to server directory
cd ~/aeci-mmu-server

# Install Node.js dependencies
npm install

# Run the setup script
node setup-termux.js

# Make scripts executable
chmod +x server-control.sh
chmod +x start-server.sh
```

### Step 5: Configure Auto-Start

```bash
# Create boot directory for Termux:Boot
mkdir -p ~/.termux/boot

# Copy start script to boot directory
cp start-server.sh ~/.termux/boot/

# Make boot script executable
chmod +x ~/.termux/boot/start-server.sh
```

### Step 6: Start the Server

```bash
# Start the server
./server-control.sh start

# Check if it's running
./server-control.sh status

# View logs
./server-control.sh logs
```

### Step 7: Find Your Phone's IP Address

```bash
# Get your phone's IP address
ifconfig wlan0 | grep 'inet '
```

The output will show something like `inet 192.168.1.xxx`. Note this IP address.

### Step 8: Test the Server

1. **From your phone's browser**: Go to `http://localhost:3000`
2. **From computer on same network**: Go to `http://[phone-ip]:3000`
3. **API test**: `http://[phone-ip]:3000/api/health`

### Step 9: Configure Your Android App

The app will automatically discover the server, but you can also manually configure:
- Server URL: `http://[phone-ip]:3000`
- Or hostname: `aeci-s24-server` (if hostname resolution works)

---

## Option B: Alternative Setup Using ADB Port Forwarding

This approach runs the server on your computer but makes it accessible to your phone via port forwarding.

### Prerequisites
- Node.js installed on your computer
- ADB installed and accessible
- Samsung S24 connected via USB with debugging enabled

### Step 1: Install Node.js on Your Computer

Download and install Node.js from: https://nodejs.org/

### Step 2: Setup Server on Computer

```powershell
# Navigate to your project directory
cd "C:\Users\VanGuduza\AndroidStudioProjects\AECIMMUCompanion"

# Install server dependencies
cd server
npm install
cd ..
```

### Step 3: Enable USB Debugging on Phone

1. Go to Settings > About phone
2. Tap "Build number" 7 times to enable Developer options
3. Go to Settings > Developer options
4. Enable "USB debugging"
5. Connect phone to computer via USB
6. Accept debugging permission when prompted

### Step 4: Test ADB Connection

```powershell
# Check if ADB can see your device
adb devices
# Should show: RFCX2054F5W    device
```

### Step 5: Start Alternative Server

```powershell
# Start the alternative server setup
.\deploy-alternative.ps1 -Action start
```

This will:
- Start Node.js server on your computer (port 3000)
- Setup port forwarding from phone port 3000 to computer port 3000
- Your phone can access the server at `http://localhost:3000`

### Step 6: Test the Setup

```powershell
# Check status
.\deploy-alternative.ps1 -Action status

# Test connectivity
.\deploy-alternative.ps1 -Action test
```

### Step 7: Access the Server

- **From computer**: `http://localhost:3000`
- **From phone**: `http://localhost:3000` (via port forwarding)
- **API health check**: `http://localhost:3000/api/health`

---

## Server Management Commands

### Termux Server (Option A)
```bash
# Start server
./server-control.sh start

# Stop server
./server-control.sh stop

# Restart server
./server-control.sh restart

# Check status
./server-control.sh status

# View real-time logs
./server-control.sh logs

# View startup logs
cat logs/startup.log
```

### Alternative Server (Option B)
```powershell
# Start server and port forwarding
.\deploy-alternative.ps1 -Action start

# Stop everything
.\deploy-alternative.ps1 -Action stop

# Check status
.\deploy-alternative.ps1 -Action status

# Test connectivity
.\deploy-alternative.ps1 -Action test
```

---

## Default Server Configuration

### Access Credentials
- **Username**: admin
- **Password**: admin123
- **⚠️ IMPORTANT**: Change these default credentials after first login!

### Network Configuration
- **Port**: 3000
- **Binding**: All interfaces (0.0.0.0)
- **Protocol**: HTTP (HTTPS available with reverse proxy)

### API Endpoints
- Health check: `/api/health`
- Authentication: `/api/auth/login`
- User management: `/api/users`
- Form sync: `/api/forms`
- Equipment data: `/api/equipment`

---

## Troubleshooting

### Termux Issues
1. **"Package not found"**: Update packages with `pkg update`
2. **"Permission denied"**: Check script permissions with `chmod +x`
3. **"Node not found"**: Reinstall with `pkg install nodejs`
4. **"Server won't start"**: Check logs with `./server-control.sh logs`

### Network Issues
1. **App can't connect**: Verify phone and app device are on same network
2. **Server not accessible**: Check firewall settings on both devices
3. **Wrong IP address**: Get current IP with `ifconfig wlan0`

### ADB Issues (Alternative Setup)
1. **"Device not found"**: Enable USB debugging and accept permissions
2. **"Port forwarding failed"**: Try different USB port or cable
3. **"ADB not found"**: Add ADB to PATH or use full path

### Performance Issues
1. **Slow response**: Check server resources with status command
2. **Memory issues**: Restart server to clear memory
3. **Storage full**: Clean old backups in `backups/` directory

---

## Security Notes

- Server runs on local network only (not accessible from internet)
- Change default admin password immediately
- Regular database backups are created automatically
- SSL/HTTPS not enabled by default (use reverse proxy if needed)
- No external dependencies required for basic operation

---

## Auto-Start Configuration

### Termux Auto-Start (Option A)
The server will automatically start when your phone boots if you:
1. Installed Termux:Boot app
2. Copied start script to `~/.termux/boot/`
3. Granted Termux:Boot necessary permissions

### Alternative Auto-Start (Option B)
Create a Windows startup script to run the alternative setup automatically:
1. Create batch file with: `powershell -File "C:\path\to\deploy-alternative.ps1" -Action start`
2. Add to Windows startup folder

---

## Next Steps

1. **Choose your preferred option** (A or B)
2. **Follow the setup steps** for your chosen option
3. **Test the connection** using the provided commands
4. **Change default passwords** for security
5. **Test with your Android app** to ensure everything works

Your AECI MMU Companion app is configured to automatically discover and connect to the mobile server once it's running.

---

## Support

If you encounter issues:
1. Check the troubleshooting section above
2. Review server logs for error messages
3. Verify network connectivity between devices
4. Ensure all dependencies are properly installed

The server includes a web interface for monitoring and management at `http://[server-address]:3000` 