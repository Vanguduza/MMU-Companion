# AECI MMU Companion - Manual Server Setup Instructions

## Overview

This guide provides step-by-step manual instructions for setting up the mobile server for your AECI MMU Companion app. There are two approaches available:

1. **On-Device Server (Recommended)** - Runs directly on your Samsung S24
2. **Port Forwarding Alternative** - Runs on your computer with ADB forwarding

---

## Approach 1: On-Device Server (Samsung S24)

### Prerequisites

1. **Samsung S24 Device** (Device ID: RFCX2054F5W)
2. **Termux App** - Install from F-Droid or GitHub (NOT Play Store)
3. **USB Debugging enabled**
4. **Computer with ADB tools**

### Step 1: Install Termux

**Option A: F-Droid (Recommended)**
1. Install F-Droid from https://f-droid.org/
2. Open F-Droid and search for "Termux"
3. Install Termux

**Option B: GitHub Direct**
1. Go to https://github.com/termux/termux-app/releases
2. Download the latest APK file
3. Install the APK (enable "Install from unknown sources")

⚠️ **Important**: Do NOT use the Play Store version - it's outdated and won't work.

### Step 2: Setup Termux Environment

1. Open Termux on your Samsung S24
2. Update packages:
   ```bash
   pkg update && pkg upgrade
   ```

3. Install Node.js and required packages:
   ```bash
   pkg install nodejs npm git
   ```

4. Install additional tools:
   ```bash
   pkg install openssh termux-api
   ```

### Step 3: Create Server Directory

```bash
mkdir ~/aeci-server
cd ~/aeci-server
```

### Step 4: Create Server Files

**Create package.json:**
```bash
cat > package.json << 'EOF'
{
  "name": "aeci-mmu-mobile-server",
  "version": "1.0.0",
  "description": "Mobile server for AECI MMU Companion",
  "main": "server.js",
  "scripts": {
    "start": "node server.js",
    "dev": "node server.js"
  },
  "dependencies": {
    "express": "^4.18.2",
    "cors": "^2.8.5",
    "sqlite3": "^5.1.6",
    "bcryptjs": "^2.4.3",
    "jsonwebtoken": "^9.0.2",
    "multer": "^1.4.5-lts.1",
    "os": "^0.1.2"
  }
}
EOF
```

**Create config.env:**
```bash
cat > config.env << 'EOF'
# AECI MMU Mobile Server Configuration
PORT=3000
HOSTNAME=aeci-s24-server
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production
ADMIN_PASSWORD=admin123
SYNC_INTERVAL=300000
BACKUP_INTERVAL=3600000
DATA_RETENTION_DAYS=30
EOF
```

### Step 5: Install Dependencies

```bash
npm install
```

### Step 6: Create Server Script

You'll need to transfer the `server.js` file from your computer to the phone. Use one of these methods:

**Method A: ADB Push**
1. On your computer, navigate to the mobile-server directory
2. Connect your Samsung S24 via USB
3. Run:
   ```powershell
   adb push mobile-server/server.js /storage/emulated/0/Download/
   ```
4. On your phone in Termux:
   ```bash
   cp /storage/emulated/0/Download/server.js ~/aeci-server/
   ```

**Method B: Manual Copy**
1. Copy the contents of `mobile-server/server.js` from your computer
2. In Termux, create the file:
   ```bash
   nano server.js
   ```
3. Paste the content and save (Ctrl+X, Y, Enter)

### Step 7: Start the Server

```bash
cd ~/aeci-server
npm start
```

The server should start and display:
```
🚀 AECI MMU Mobile Server starting...
📱 Device: Samsung S24 (aeci-s24-server)
🌐 Server running on: http://aeci-s24-server:3000
💾 Database initialized successfully
✅ Server ready for connections!
```

### Step 8: Test Connection

1. Keep the server running in Termux
2. Open your AECI MMU Companion app
3. The app should automatically detect and connect to the server
4. Look for the green connection indicator in the notification bar

---

## Approach 2: Port Forwarding Alternative

This approach runs the server on your computer and forwards the connection to your phone.

### Prerequisites

1. **Node.js** installed on your computer
2. **ADB tools** configured
3. **Samsung S24** connected via USB
4. **USB Debugging enabled**

### Step 1: Setup Computer Environment

1. Open PowerShell as Administrator
2. Navigate to your project directory:
   ```powershell
   cd "C:\Users\VanGuduza\AndroidStudioProjects\AECIMMUCompanion"
   ```

### Step 2: Install Server Dependencies

```powershell
cd mobile-server
npm install
```

### Step 3: Configure Environment

Create or verify `config.env`:
```
PORT=3000
HOSTNAME=localhost
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production
ADMIN_PASSWORD=admin123
SYNC_INTERVAL=300000
BACKUP_INTERVAL=3600000
DATA_RETENTION_DAYS=30
```

### Step 4: Setup ADB Port Forwarding

1. Connect your Samsung S24 via USB
2. Verify connection:
   ```powershell
   adb devices
   ```
   Should show: `RFCX2054F5W device`

3. Setup port forwarding:
   ```powershell
   adb reverse tcp:3000 tcp:3000
   ```

### Step 5: Start the Server

```powershell
npm start
```

### Step 6: Test Connection

1. The server runs on your computer
2. Your phone accesses it via localhost:3000 (forwarded through ADB)
3. Open the AECI MMU Companion app
4. The app should detect the server as "localhost"

---

## Troubleshooting

### Common Issues

**1. "Package not debuggable" error**
- This means Termux isn't installed or is the wrong version
- Install Termux from F-Droid or GitHub, not Play Store

**2. "Device not found"**
- Enable USB Debugging in Developer Options
- Try different USB cable or port
- Run `adb kill-server && adb start-server`

**3. "Connection refused"**
- Check if server is running
- Verify port 3000 isn't blocked
- For on-device: Check WiFi connection
- For port forwarding: Verify ADB reverse command

**4. App doesn't detect server**
- Wait 30 seconds for automatic discovery
- Check notification bar for connection status
- Restart the app
- For manual connection, use: `aeci-mmu://server/connect`

### Server Status Indicators

- 🟢 **Green**: Connected to mobile server
- 🟡 **Yellow**: Searching for server
- 🔴 **Red**: Connection error, trying cloud fallback
- ⚫ **Gray**: Offline mode

### Manual Connection

If automatic discovery fails, you can manually trigger connection:

1. Open a browser on your phone
2. Navigate to: `aeci-mmu://server/connect`
3. The app should open and attempt connection

---

## Server Management

### Starting/Stopping Server

**On-Device (Termux):**
```bash
# Start
cd ~/aeci-server && npm start

# Stop
Ctrl+C

# Background mode
nohup npm start > server.log 2>&1 &
```

**Port Forwarding:**
```powershell
# Start
cd mobile-server
npm start

# Stop
Ctrl+C
```

### Checking Server Status

Visit `http://localhost:3000/health` (or `http://aeci-s24-server:3000/health`) to see:
- Server uptime
- Database status
- Connected devices
- System resources

### Data Management

- **Database**: SQLite file stored in server directory
- **Uploads**: Files stored in `uploads/` directory
- **Backups**: Automatic backups created every hour
- **Logs**: Server logs show connection attempts and errors

---

## Security Notes

1. **Change default passwords** in `config.env`
2. **Use strong JWT secrets**
3. **Keep Termux updated**
4. **Monitor server logs** for suspicious activity
5. **Backup data regularly**

---

## Support

If you encounter issues:

1. Check the server logs
2. Verify device connectivity
3. Restart both server and app
4. Check firewall settings
5. Ensure all dependencies are installed correctly

The server includes comprehensive logging to help diagnose connection issues. 