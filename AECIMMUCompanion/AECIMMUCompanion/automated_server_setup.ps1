# AECI MMU Companion - Automated Server Setup Script
# This script automates the complete server setup on Samsung S24 via ADB
# Enhanced with automatic IP detection and comprehensive error monitoring

param(
    [string]$PhoneIP = "",
    [string]$Domain = "",
    [switch]$SkipTermuxInstall = $false,
    [switch]$SkipServerFiles = $false,
    [switch]$EnableDynamicDNS = $false,
    [int]$MaxRetries = 5
)

# Set error action preference
$ErrorActionPreference = "Stop"

# Colors for output
$Red = "Red"
$Green = "Green"
$Yellow = "Yellow"
$Blue = "Blue"
$Cyan = "Cyan"
$Magenta = "Magenta"

# Global variables for monitoring
$Global:ServerStatus = "stopped"
$Global:LastError = ""
$Global:RetryCount = 0
$Global:DetectedIP = ""

function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

function Write-Status {
    param([string]$Message, [string]$Status = "info")
    $timestamp = Get-Date -Format "HH:mm:ss"
    $statusSymbol = switch ($Status) {
        "success" { "SUCCESS" }
        "error" { "ERROR" }
        "warning" { "WARNING" }
        "info" { "INFO" }
        "progress" { "PROGRESS" }
        default { "INFO" }
    }
    Write-Host "[$timestamp] $statusSymbol $Message" -ForegroundColor $(switch ($Status) {
        "success" { $Green }
        "error" { $Red }
        "warning" { $Yellow }
        "info" { $Blue }
        "progress" { $Cyan }
        default { $Blue }
    })
}

function Test-ADBConnection {
    Write-Status "Checking ADB connection..." "info"
    
    try {
        $devices = adb devices
        if ($devices -match "device$") {
            $deviceId = ($devices | Select-String "device$" | ForEach-Object { $_.ToString().Split()[0] })
            Write-Status "ADB connected to device: $deviceId" "success"
            return $true
        } else {
            Write-Status "No devices connected via ADB" "error"
            return $false
        }
    } catch {
        Write-Status "ADB not found in PATH. Please ensure Android SDK is installed." "error"
        return $false
    }
}

function Detect-PhoneIP {
    Write-Status "Detecting phone's public IP address..." "info"
    
    try {
        # First, ensure Termux is available
        $termuxAvailable = adb shell pm list packages | Select-String "com.termux"
        if (-not $termuxAvailable) {
            Write-Status "Termux not available. Installing first..." "warning"
            if (-not (Install-Termux)) {
                Write-Status "Failed to install Termux for IP detection" "error"
                return $false
            }
        }
        
        # Create IP detection script using single quotes to avoid PowerShell parsing
        $detectIPScript = @'
#!/data/data/com.termux/files/usr/bin/sh
curl -s https://ipinfo.io/ip
'@
        
        # Write IP detection script to phone
        $detectIPScript | adb shell "cat > /sdcard/detect-ip.sh"
        adb shell "chmod +x /sdcard/detect-ip.sh"
        
        # Execute and capture result
        $result = adb shell "cd /sdcard && ./detect-ip.sh"
        
        # Parse the result
        if ($result -match "^\d+\.\d+\.\d+\.\d+$") {
            $detectedIP = $result.Trim()
            $Global:DetectedIP = $detectedIP
            Write-Status "Detected public IP: $detectedIP" "success"
            return $detectedIP
        } else {
            Write-Status "Failed to detect public IP. Using local network detection..." "warning"
            
            # Fallback to local network IP using single quotes
            $localIP = adb shell 'ip route get 1.1.1.1 | grep -o "src [^ ]*" | cut -d" " -f2'
            if ($localIP -match "^\d+\.\d+\.\d+\.\d+$") {
                $Global:DetectedIP = $localIP
                Write-Status "Using local IP: $localIP (Note: Only accessible on same network)" "warning"
                return $localIP
            }
            
            Write-Status "Could not detect any IP address" "error"
            return $false
        }
    } catch {
        Write-Status "Error during IP detection: $($_.Exception.Message)" "error"
        return $false
    }
}

function Install-Termux {
    Write-Status "Installing Termux..." "info"
    
    # Check if Termux is already installed
    $termuxInstalled = adb shell pm list packages | Select-String "com.termux"
    if ($termuxInstalled) {
        Write-Status "Termux is already installed" "success"
        return $true
    }
    
    # Download Termux APK if not exists
    $termuxUrl = "https://github.com/termux/termux-app/releases/download/v0.118.0/termux-app-v0.118.0+github-debug_arm64-v8a_arm64-v8a.apk"
    $termuxPath = "$env:TEMP\termux-app.apk"
    
    if (-not (Test-Path $termuxPath)) {
        Write-Status "Downloading Termux APK..." "progress"
        try {
            Invoke-WebRequest -Uri $termuxUrl -OutFile $termuxPath
        } catch {
            Write-Status "Failed to download Termux. Please download manually from F-Droid." "error"
            return $false
        }
    }
    
    # Install Termux
    Write-Status "Installing Termux APK..." "progress"
    try {
        adb install -r $termuxPath
        Write-Status "Termux installed successfully" "success"
        return $true
    } catch {
        Write-Status "Failed to install Termux" "error"
        return $false
    }
}

function Install-TermuxBoot {
    Write-Status "Installing Termux:Boot for auto-start..." "info"
    
    # Download Termux:Boot APK
    $bootUrl = "https://f-droid.org/repo/com.termux.boot_0.118.0.apk"
    $bootPath = "$env:TEMP\termux-boot.apk"
    
    if (-not (Test-Path $bootPath)) {
        Write-Status "Downloading Termux:Boot APK..." "progress"
        try {
            Invoke-WebRequest -Uri $bootUrl -OutFile $bootPath
        } catch {
            Write-Status "Failed to download Termux:Boot. You'll need to install it manually from F-Droid." "warning"
            return $false
        }
    }
    
    # Install Termux:Boot
    try {
        adb install -r $bootPath
        Write-Status "Termux:Boot installed successfully" "success"
        return $true
    } catch {
        Write-Status "Failed to install Termux:Boot. Install manually from F-Droid." "warning"
        return $false
    }
}

function Push-ServerFiles {
    Write-Status "Pushing server files to phone..." "info"
    
    try {
        # Create server directory on phone
        adb shell mkdir -p /sdcard/aeci-server
        
        # Push server files
        adb push server /sdcard/aeci-server/
        
        Write-Status "Server files pushed successfully" "success"
        return $true
    } catch {
        Write-Status "Failed to push server files" "error"
        return $false
    }
}

function Setup-ServerEnvironment {
    Write-Status "Setting up server environment..." "info"
    
    # Use detected IP or provided IP
    $serverIP = if ($Global:DetectedIP) { $Global:DetectedIP } else { $PhoneIP }
    
    # Create environment file
    $envContent = @"
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
SERVER_IP=$serverIP
AUTO_RESTART=true
HEALTH_CHECK_INTERVAL=30000
"@

    # Write environment file to phone
    $envContent | adb shell "cat > /sdcard/aeci-server/.env"
    
    Write-Status "Environment file created with IP: $serverIP" "success"
}

function Install-ServerDependencies {
    Write-Status "Installing server dependencies..." "info"
    
    # Create installation script using single quotes to avoid PowerShell parsing
    $installScript = @'
#!/data/data/com.termux/files/usr/bin/sh
set -e

echo "=== AECI MMU Server Installation ==="
echo "Timestamp: $(date)"

echo "Updating package list..."
pkg update -y

echo "Installing Node.js and dependencies..."
pkg install -y nodejs npm git curl wget sqlite python

echo "Installing PM2 globally..."
npm install -g pm2

echo "Navigating to server directory..."
cd /sdcard/aeci-server

echo "Installing server dependencies..."
npm install

echo "Creating data directory..."
mkdir -p data

echo "Creating auto-start script..."
mkdir -p ~/.termux/boot

cat > ~/.termux/boot/start-aeci-server.sh << 'EOF'
#!/data/data/com.termux/files/usr/bin/sh
cd /sdcard/aeci-server
pm2 start server.js --name "aeci-server"
echo "AECI MMU Server started at $(date)" >> ~/aeci-server.log
EOF

chmod +x ~/.termux/boot/start-aeci-server.sh

echo "Creating manual control scripts..."
cat > ~/start-server.sh << 'EOF'
#!/data/data/com.termux/files/usr/bin/sh
cd /sdcard/aeci-server
pm2 start server.js --name "aeci-server"
echo "Server started manually at $(date)"
EOF

cat > ~/stop-server.sh << 'EOF'
#!/data/data/com.termux/files/usr/bin/sh
pm2 stop aeci-server
echo "Server stopped manually at $(date)"
EOF

cat > ~/restart-server.sh << 'EOF'
#!/data/data/com.termux/files/usr/bin/sh
pm2 restart aeci-server
echo "Server restarted manually at $(date)"
EOF

cat > ~/server-status.sh << 'EOF'
#!/data/data/com.termux/files/usr/bin/sh
pm2 status
echo "Server logs:"
pm2 logs aeci-server --lines 10
EOF

chmod +x ~/start-server.sh ~/stop-server.sh ~/restart-server.sh ~/server-status.sh

echo "Installation completed successfully!"
'@

    # Write and execute installation script
    $installScript | adb shell "cat > /sdcard/install-server.sh"
    adb shell "chmod +x /sdcard/install-server.sh"
    
    Write-Status "Executing installation script in Termux..." "progress"
    adb shell "am start -n com.termux/.app.TermuxActivity"
    Start-Sleep -Seconds 3
    
    # Execute the installation script using single quotes
    adb shell 'input text "cd /sdcard && ./install-server.sh"'
    adb shell "input keyevent 66"  # Enter key
    
    Write-Status "Installation in progress... This may take several minutes." "info"
    Write-Status "You can monitor progress in the Termux app on your phone." "info"
}

function Setup-InternetAccess {
    $serverIP = if ($Global:DetectedIP) { $Global:DetectedIP } else { $PhoneIP }
    
    if ($serverIP -or $Domain) {
        Write-Status "Setting up internet access..." "info"
        
        $serverUrl = if ($Domain) { "https://$Domain/api/" } else { "http://$serverIP:3000/api/" }
        
        # Update app configuration
        $networkModulePath = "app\src\main\java\com\aeci\mmucompanion\core\di\NetworkModule.kt"
        if (Test-Path $networkModulePath) {
            $content = Get-Content $networkModulePath -Raw
            $updatedContent = $content -replace 'private const val EMBEDDED_SERVER_URL = "https://your-s24-server\.com/api/"', "private const val EMBEDDED_SERVER_URL = `"$serverUrl`""
            Set-Content $networkModulePath $updatedContent
            Write-Status "App configuration updated with server URL: $serverUrl" "success"
        }
        
        Write-Status "Internet setup completed. Server accessible at: $serverUrl" "success"
    }
}

function Test-ServerConnection {
    Write-Status "Testing server connection..." "info"
    
    $maxAttempts = 10
    $attempt = 0
    
    while ($attempt -lt $maxAttempts) {
        $attempt++
        Write-Status "Connection attempt $attempt/$maxAttempts..." "progress"
        
        Start-Sleep -Seconds 10
        
        try {
            $result = adb shell "curl -s --connect-timeout 10 http://localhost:3000/api/health"
            if ($result -match "status.*ok" -or $result -match "ok" -or $result -match "running") {
                Write-Status "Server is running and responding!" "success"
                $Global:ServerStatus = "running"
            return $true
        } else {
                Write-Status "Server response: $result" "warning"
            }
        } catch {
            Write-Status "Connection attempt $attempt failed" "warning"
        }
        
        # Try to restart server if it's not responding
        if ($attempt -gt 3) {
            Write-Status "Attempting to restart server..." "progress"
            adb shell "pm2 restart aeci-server" 2>$null
            Start-Sleep -Seconds 5
        }
    }
    
    Write-Status "Server connection test failed after $maxAttempts attempts" "error"
    $Global:ServerStatus = "failed"
    return $false
}

function Monitor-ServerHealth {
    Write-Status "Starting server health monitoring..." "info"
    
    # Create monitoring script using single quotes
    $monitorScript = @'
#!/data/data/com.termux/files/usr/bin/sh
cd /sdcard/aeci-server

echo "Starting server health monitoring..."
echo "Monitoring started at $(date)" >> ~/server-monitor.log

while true; do
    if ! pm2 list | grep -q "aeci-server.*online"; then
        echo "[$(date)] Server is offline, restarting..." >> ~/server-monitor.log
        pm2 restart aeci-server
    fi
    
    sleep 30
done
'@

    $monitorScript | adb shell "cat > /sdcard/monitor-server.sh"
    adb shell "chmod +x /sdcard/monitor-server.sh"
    
    # Start monitoring in background
    adb shell "nohup /sdcard/monitor-server.sh > /dev/null 2>&1 &"
    Write-Status "Server health monitoring started" "success"
}

function Show-ManualCommands {
    Write-Status "Manual Server Control Commands:" "info"
    Write-Status "----------------------------------------" "info"
    Write-Status "Start server:   adb shell 'cd /sdcard/aeci-server; pm2 start server.js --name aeci-server'" "info"
    Write-Status "Stop server:    adb shell 'pm2 stop aeci-server'" "info"
    Write-Status "Restart server: adb shell 'pm2 restart aeci-server'" "info"
    Write-Status "Check status:   adb shell 'pm2 status'" "info"
    Write-Status "View logs:      adb shell 'pm2 logs aeci-server'" "info"
    Write-Status "Monitor health: adb shell '~/monitor-server.sh'" "info"
    Write-Status "----------------------------------------" "info"
}

function Show-UsageInstructions {
    Write-Status "Usage Instructions:" "info"
    Write-Status "----------------------------------------" "info"
    Write-Status "1. Open Termux app on your phone" "info"
    Write-Status "2. Use these commands for manual control:" "info"
    Write-Status "   - Start:   ~/start-server.sh" "info"
    Write-Status "   - Stop:    ~/stop-server.sh" "info"
    Write-Status "   - Restart: ~/restart-server.sh" "info"
    Write-Status "   - Status:  ~/server-status.sh" "info"
    Write-Status "   - Monitor: ~/monitor-server.sh" "info"
    Write-Status "3. Server will auto-start on device boot" "info"
    Write-Status "4. Health monitoring is active" "info"
    Write-Status "5. Test connection: curl http://localhost:3000/api/health" "info"
    Write-Status "----------------------------------------" "info"
}

# Main execution
Write-Status "AECI MMU Companion - Enhanced Automated Server Setup" "info"
Write-Status "==================================================" "info"

# Check ADB connection
if (-not (Test-ADBConnection)) {
    Write-Status "Setup failed: No ADB connection" "error"
    exit 1
}

# Detect phone IP if not provided
if (-not $PhoneIP) {
    $detectedIP = Detect-PhoneIP
    if ($detectedIP) {
        $PhoneIP = $detectedIP
        Write-Status "Using detected IP: $PhoneIP" "success"
    } else {
        Write-Status "Warning: Could not detect IP. Some features may be limited." "warning"
    }
}

# Install Termux if needed
if (-not $SkipTermuxInstall) {
    if (-not (Install-Termux)) {
        Write-Status "Setup failed: Could not install Termux" "error"
        exit 1
    }
    
    if (-not (Install-TermuxBoot)) {
        Write-Status "Warning: Could not install Termux:Boot" "warning"
    }
}

# Push server files if needed
if (-not $SkipServerFiles) {
    if (-not (Push-ServerFiles)) {
        Write-Status "Setup failed: Could not push server files" "error"
        exit 1
    }
}

# Setup environment
Setup-ServerEnvironment

# Install dependencies
Install-ServerDependencies

# Setup internet access if IP is available
if ($PhoneIP -or $Domain) {
    Setup-InternetAccess
}

# Test connection with retries
$connectionSuccess = $false
for ($i = 1; $i -le $MaxRetries; $i++) {
    Write-Status "Connection test attempt $i/$MaxRetries..." "progress"
    if (Test-ServerConnection) {
        $connectionSuccess = $true
        break
    }
    Start-Sleep -Seconds 30
}

if (-not $connectionSuccess) {
    Write-Status "Warning: Server connection test failed. Starting health monitoring..." "warning"
}

# Start health monitoring
Monitor-ServerHealth

# Show manual commands
Show-ManualCommands

# Show usage instructions
Show-UsageInstructions

Write-Status "Enhanced automated setup completed!" "success"
Write-Status "Check the Termux app on your phone for installation progress." "info"
Write-Status "Server health monitoring is active and will auto-fix issues." "success"
Write-Status "Use the manual commands above to control the server." "info" 