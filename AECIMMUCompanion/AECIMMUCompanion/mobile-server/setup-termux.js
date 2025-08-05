#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

console.log('🚀 Setting up AECI MMU Mobile Server for Termux...');

// Create necessary directories
const dirs = ['data', 'uploads', 'backups', 'logs', 'public'];
dirs.forEach(dir => {
    const dirPath = path.join(__dirname, dir);
    if (!fs.existsSync(dirPath)) {
        fs.mkdirSync(dirPath, { recursive: true });
        console.log(`✅ Created directory: ${dir}`);
    }
});

// Create Termux boot script
const bootScript = `#!/data/data/com.termux/files/usr/bin/bash

# AECI MMU Mobile Server Auto-Start Script
export PATH=$PATH:$PREFIX/bin

# Wait for network
sleep 10

# Change to server directory
cd /data/data/com.termux/files/home/aeci-mmu-server

# Start the server
node server.js > logs/server.log 2>&1 &

echo "AECI MMU Mobile Server started at $(date)" >> logs/startup.log
`;

fs.writeFileSync(path.join(__dirname, 'start-server.sh'), bootScript);
execSync('chmod +x start-server.sh');
console.log('✅ Created start-server.sh script');

// Create service management script
const serviceScript = `#!/data/data/com.termux/files/usr/bin/bash

case "$1" in
    start)
        echo "Starting AECI MMU Mobile Server..."
        cd /data/data/com.termux/files/home/aeci-mmu-server
        node server.js > logs/server.log 2>&1 &
        echo $! > server.pid
        echo "Server started with PID $(cat server.pid)"
        ;;
    stop)
        echo "Stopping AECI MMU Mobile Server..."
        if [ -f server.pid ]; then
            kill $(cat server.pid)
            rm server.pid
            echo "Server stopped"
        else
            echo "Server not running or PID file not found"
        fi
        ;;
    restart)
        $0 stop
        sleep 2
        $0 start
        ;;
    status)
        if [ -f server.pid ] && kill -0 $(cat server.pid) 2>/dev/null; then
            echo "Server is running with PID $(cat server.pid)"
            echo "Server URL: http://$(ifconfig wlan0 | grep 'inet ' | awk '{print $2}'):3000"
        else
            echo "Server is not running"
        fi
        ;;
    logs)
        tail -f logs/server.log
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status|logs}"
        exit 1
        ;;
esac
`;

fs.writeFileSync(path.join(__dirname, 'server-control.sh'), serviceScript);
execSync('chmod +x server-control.sh');
console.log('✅ Created server-control.sh script');

// Create web interface
const webInterface = `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AECI MMU Mobile Server</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { 
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }
        .container {
            background: white;
            border-radius: 15px;
            box-shadow: 0 20px 40px rgba(0,0,0,0.1);
            padding: 40px;
            max-width: 600px;
            width: 100%;
        }
        .header {
            text-align: center;
            margin-bottom: 30px;
        }
        .logo {
            font-size: 2.5em;
            color: #667eea;
            margin-bottom: 10px;
        }
        h1 {
            color: #333;
            margin-bottom: 10px;
        }
        .subtitle {
            color: #666;
            margin-bottom: 30px;
        }
        .status-card {
            background: #f8f9fa;
            border-radius: 10px;
            padding: 20px;
            margin-bottom: 20px;
            border-left: 4px solid #28a745;
        }
        .status-online {
            border-left-color: #28a745;
        }
        .status-offline {
            border-left-color: #dc3545;
        }
        .status-text {
            font-weight: bold;
            color: #28a745;
        }
        .status-offline .status-text {
            color: #dc3545;
        }
        .info-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .info-item {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 8px;
            text-align: center;
        }
        .info-label {
            font-size: 0.9em;
            color: #666;
            margin-bottom: 5px;
        }
        .info-value {
            font-size: 1.2em;
            font-weight: bold;
            color: #333;
        }
        .buttons {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
            gap: 10px;
        }
        .btn {
            padding: 12px 20px;
            border: none;
            border-radius: 8px;
            font-weight: bold;
            cursor: pointer;
            transition: all 0.3s ease;
            text-decoration: none;
            text-align: center;
        }
        .btn-primary {
            background: #667eea;
            color: white;
        }
        .btn-success {
            background: #28a745;
            color: white;
        }
        .btn-danger {
            background: #dc3545;
            color: white;
        }
        .btn-info {
            background: #17a2b8;
            color: white;
        }
        .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        }
        .footer {
            text-align: center;
            margin-top: 30px;
            color: #666;
            font-size: 0.9em;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <div class="logo">🏭</div>
            <h1>AECI MMU Mobile Server</h1>
            <p class="subtitle">Credential Verification & Data Sync Server</p>
        </div>

        <div class="status-card" id="statusCard">
            <div class="status-text" id="statusText">Checking status...</div>
            <div id="statusDetails"></div>
        </div>

        <div class="info-grid" id="infoGrid">
            <!-- Server info will be loaded here -->
        </div>

        <div class="buttons">
            <button class="btn btn-success" onclick="startServer()">Start</button>
            <button class="btn btn-danger" onclick="stopServer()">Stop</button>
            <button class="btn btn-primary" onclick="restartServer()">Restart</button>
            <button class="btn btn-info" onclick="viewLogs()">Logs</button>
        </div>

        <div class="footer">
            <p>AECI MMU Companion Mobile Server v1.0</p>
            <p>Running on Samsung Galaxy S24</p>
        </div>
    </div>

    <script>
        async function checkStatus() {
            try {
                const response = await fetch('/api/server/status');
                const data = await response.json();
                updateStatusDisplay(true, data);
            } catch (error) {
                updateStatusDisplay(false, null);
            }
        }

        function updateStatusDisplay(online, data) {
            const statusCard = document.getElementById('statusCard');
            const statusText = document.getElementById('statusText');
            const statusDetails = document.getElementById('statusDetails');
            const infoGrid = document.getElementById('infoGrid');

            if (online) {
                statusCard.className = 'status-card status-online';
                statusText.textContent = 'Server Online';
                statusDetails.innerHTML = \`
                    <small>Port: \${data.port} | Uptime: \${Math.floor(data.uptime / 3600)}h \${Math.floor((data.uptime % 3600) / 60)}m</small>
                \`;
                
                infoGrid.innerHTML = \`
                    <div class="info-item">
                        <div class="info-label">Memory Usage</div>
                        <div class="info-value">\${Math.round(data.memory.rss / 1024 / 1024)}MB</div>
                    </div>
                    <div class="info-item">
                        <div class="info-label">Platform</div>
                        <div class="info-value">\${data.platform}</div>
                    </div>
                    <div class="info-item">
                        <div class="info-label">Node Version</div>
                        <div class="info-value">\${data.nodeVersion}</div>
                    </div>
                    <div class="info-item">
                        <div class="info-label">Last Updated</div>
                        <div class="info-value">\${new Date(data.timestamp).toLocaleTimeString()}</div>
                    </div>
                \`;
            } else {
                statusCard.className = 'status-card status-offline';
                statusText.textContent = 'Server Offline';
                statusDetails.innerHTML = '<small>Server is not responding</small>';
                infoGrid.innerHTML = '<div class="info-item"><div class="info-value">Server Unavailable</div></div>';
            }
        }

        async function startServer() {
            try {
                await fetch('/api/server/start', { method: 'POST' });
                setTimeout(checkStatus, 2000);
            } catch (error) {
                alert('Failed to start server');
            }
        }

        async function stopServer() {
            try {
                await fetch('/api/server/stop', { method: 'POST' });
                setTimeout(checkStatus, 2000);
            } catch (error) {
                alert('Failed to stop server');
            }
        }

        async function restartServer() {
            try {
                await fetch('/api/server/restart', { method: 'POST' });
                setTimeout(checkStatus, 3000);
            } catch (error) {
                alert('Failed to restart server');
            }
        }

        function viewLogs() {
            window.open('/logs', '_blank');
        }

        // Check status every 5 seconds
        setInterval(checkStatus, 5000);
        checkStatus();
    </script>
</body>
</html>`;

const publicDir = path.join(__dirname, 'public');
if (!fs.existsSync(publicDir)) {
    fs.mkdirSync(publicDir);
}
fs.writeFileSync(path.join(publicDir, 'index.html'), webInterface);
console.log('✅ Created web interface');

// Create README for setup instructions
const readme = `# AECI MMU Mobile Server Setup

## Installation Instructions

### 1. Install Termux on your Samsung S24
Download Termux from F-Droid or GitHub releases.

### 2. Setup Termux Environment
\`\`\`bash
# Update packages
pkg update && pkg upgrade

# Install Node.js and essential tools
pkg install nodejs npm git openssh

# Install Termux:Boot for auto-start functionality
# Download from F-Droid

# Setup storage access
termux-setup-storage
\`\`\`

### 3. Install Server
\`\`\`bash
# Navigate to home directory
cd ~

# Create server directory
mkdir aeci-mmu-server
cd aeci-mmu-server

# Copy server files (automated via deploy script)
# Files will be copied by the deployment script

# Install dependencies
npm run install-termux

# Run setup
npm run setup-termux
\`\`\`

### 4. Configure Auto-Start
\`\`\`bash
# Create boot directory for Termux:Boot
mkdir -p ~/.termux/boot

# Copy start script to boot directory
cp start-server.sh ~/.termux/boot/

# Make it executable
chmod +x ~/.termux/boot/start-server.sh
\`\`\`

## Server Management

### Manual Control
\`\`\`bash
# Start server
./server-control.sh start

# Stop server
./server-control.sh stop

# Restart server
./server-control.sh restart

# Check status
./server-control.sh status

# View logs
./server-control.sh logs
\`\`\`

### Access Server
- Web Interface: http://[phone-ip]:3000
- API Endpoint: http://[phone-ip]:3000/api
- Health Check: http://[phone-ip]:3000/health

## Network Configuration

The server will automatically bind to all network interfaces (0.0.0.0:3000).
To find your phone's IP address:
\`\`\`bash
ifconfig wlan0 | grep 'inet '
\`\`\`

## Default Credentials
- Username: admin
- Password: admin123

**Important: Change default credentials after first login!**

## Troubleshooting

### Server Won't Start
1. Check if port 3000 is available
2. Ensure Node.js is properly installed
3. Check logs: \`tail -f logs/server.log\`

### Network Issues
1. Ensure phone and app device are on same network
2. Check firewall settings
3. Verify IP address in app configuration

### Auto-Start Issues
1. Ensure Termux:Boot is installed and permissions granted
2. Check boot script: \`ls -la ~/.termux/boot/\`
3. Restart phone to test auto-start

## Security Notes

- Server runs on local network only
- Change default admin password
- Regular backups are created automatically
- Use HTTPS proxy for external access (not recommended)

## Support

For issues with the mobile server:
1. Check server logs
2. Verify network connectivity
3. Ensure all dependencies are installed
4. Restart server if needed
`;

fs.writeFileSync(path.join(__dirname, 'README.md'), readme);
console.log('✅ Created README.md');

console.log('\n🎉 Setup complete!');
console.log('\nNext steps:');
console.log('1. Install dependencies: npm run install-termux');
console.log('2. Configure auto-start (see README.md)');
console.log('3. Start server: ./server-control.sh start');
console.log('4. Access web interface at http://[phone-ip]:3000'); 