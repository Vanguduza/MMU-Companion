# AECI MMU Companion - Mobile Server Deployment Script
# Enhanced version with automatic connection setup

param(
    [string]$Action = "deploy",
    [string]$DeviceId = "",
    [switch]$AutoStart,
    [switch]$SetupHostname
)

# Color functions for output
function Write-Success { 
    param([string]$msg) 
    Write-Host $msg -ForegroundColor Green 
}

function Write-Error { 
    param([string]$msg) 
    Write-Host $msg -ForegroundColor Red 
}

function Write-Info { 
    param([string]$msg) 
    Write-Host $msg -ForegroundColor Cyan 
}

function Write-Warning { 
    param([string]$msg) 
    Write-Host $msg -ForegroundColor Yellow 
}

Write-Info "=== AECI MMU Companion - Mobile Server Deployment ==="
Write-Info "Enhanced with automatic connection setup"

# Find ADB executable
$adbPaths = @(
    "adb", # Try PATH first
    "C:\Users\VanGuduza\AppData\Local\Android\Sdk\platform-tools\adb.exe",
    "C:\Android\Sdk\platform-tools\adb.exe",
    "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk\platform-tools\adb.exe"
)

$adbPath = $null
foreach ($path in $adbPaths) {
    try {
        if ($path -eq "adb") {
            $null = Get-Command "adb" -ErrorAction Stop
            $adbPath = "adb"
            break
        } else {
            if (Test-Path $path) {
                $adbPath = $path
                break
            }
        }
    }
    catch {
        continue
    }
}

if (-not $adbPath) {
    Write-Error "ADB not found. Please install Android SDK Platform Tools."
    Write-Info "Download from: https://developer.android.com/studio/releases/platform-tools"
    Write-Info "Or install Android Studio which includes platform tools."
    exit 1
}

Write-Info "Using ADB: $adbPath"

# Function to run ADB commands
function Invoke-ADB {
    param([string]$arguments)
    return & $adbPath $arguments.Split(' ')
}

# List connected devices
function Get-ConnectedDevices {
    $deviceOutput = Invoke-ADB "devices"
    if ($deviceOutput) {
        $devices = $deviceOutput | Select-String -Pattern "device$" | ForEach-Object {
            ($_.ToString() -split "\t")[0]
        }
        return $devices
    }
    return @()
}

# Select device
$devices = Get-ConnectedDevices
if ($devices.Count -eq 0) {
    Write-Error "No Android devices connected via USB debugging."
    Write-Info "Please ensure:"
    Write-Info "1. USB debugging is enabled on your device"
    Write-Info "2. Device is connected via USB"
    Write-Info "3. Device is authorized for debugging"
    Write-Info "4. Try running: $adbPath devices"
    exit 1
}

if ($DeviceId -and ($DeviceId -notin $devices)) {
    Write-Error "Device ID '$DeviceId' not found in connected devices."
    Write-Info "Available devices: $($devices -join ', ')"
    exit 1
}

if (-not $DeviceId) {
    if ($devices.Count -eq 1) {
        $DeviceId = $devices[0]
        Write-Info "Using device: $DeviceId"
    } else {
        Write-Info "Multiple devices found:"
        for ($i = 0; $i -lt $devices.Count; $i++) {
            Write-Host "[$($i+1)] $($devices[$i])"
        }
        do {
            $selection = Read-Host "Select device (1-$($devices.Count))"
            $selectionNum = [int]$selection
        } while ($selectionNum -lt 1 -or $selectionNum -gt $devices.Count)
        
        $DeviceId = $devices[$selectionNum - 1]
    }
}

# Device info
function Get-DeviceInfo {
    param([string]$deviceId)
    
    $brand = (Invoke-ADB "-s $deviceId shell getprop ro.product.brand")
    $model = (Invoke-ADB "-s $deviceId shell getprop ro.product.model")
    $android = (Invoke-ADB "-s $deviceId shell getprop ro.build.version.release")
    
    return @{
        Brand = if ($brand) { $brand.Trim() } else { "Unknown" }
        Model = if ($model) { $model.Trim() } else { "Unknown" }
        Android = if ($android) { $android.Trim() } else { "Unknown" }
    }
}

$deviceInfo = Get-DeviceInfo $DeviceId
Write-Info "Target Device: $($deviceInfo.Brand) $($deviceInfo.Model) (Android $($deviceInfo.Android))"

# Enhanced actions
switch ($Action.ToLower()) {
    "deploy" {
        Write-Info "=== Deploying Mobile Server to Device ==="
        
        # Install Termux if not already installed
        Write-Info "Checking Termux installation..."
        $termuxCheck = Invoke-ADB "-s $DeviceId shell pm list packages" | Select-String "com.termux"
        if (-not $termuxCheck) {
            Write-Warning "Termux not found. Please install Termux from F-Droid or GitHub releases."
            Write-Info "Download from: https://github.com/termux/termux-app/releases"
            exit 1
        }
        
        # Create server directory
        Write-Info "Creating server directory..."
        Invoke-ADB "-s $DeviceId shell run-as com.termux mkdir -p /data/data/com.termux/files/home/aeci-server"
        
        # Upload server files
        Write-Info "Uploading server files..."
        Invoke-ADB "-s $DeviceId push server/package.json /sdcard/aeci-server/"
        Invoke-ADB "-s $DeviceId push server/server.js /sdcard/aeci-server/"
        
        # Check if config files exist
        if (Test-Path "server/config.env") {
            Invoke-ADB "-s $DeviceId push server/config.env /sdcard/aeci-server/"
        }
        if (Test-Path "server/setup-termux.js") {
            Invoke-ADB "-s $DeviceId push server/setup-termux.js /sdcard/aeci-server/"
        }
        
        # Move files to Termux directory
        Write-Info "Moving files to Termux directory..."
        Invoke-ADB "-s $DeviceId shell run-as com.termux cp -r /sdcard/aeci-server/* /data/data/com.termux/files/home/aeci-server/"
        Invoke-ADB "-s $DeviceId shell rm -rf /sdcard/aeci-server"
        
        # Setup Termux environment
        Write-Info "Setting up Termux environment..."
        Invoke-ADB "-s $DeviceId shell run-as com.termux sh -c 'cd /data/data/com.termux/files/home && pkg update -y && pkg install -y nodejs npm'"
        
        # Install Node.js dependencies
        Write-Info "Installing Node.js dependencies..."
        Invoke-ADB "-s $DeviceId shell run-as com.termux sh -c 'cd /data/data/com.termux/files/home/aeci-server && npm install'"
        
        # Setup automatic startup
        if ($AutoStart) {
            Write-Info "Configuring automatic startup..."
            if (Test-Path "server/setup-termux.js") {
                Invoke-ADB "-s $DeviceId shell run-as com.termux sh -c 'cd /data/data/com.termux/files/home/aeci-server && node setup-termux.js'"
            }
        }
        
        # Setup hostname for automatic discovery
        if ($SetupHostname) {
            Write-Info "Setting up hostname for automatic discovery..."
            Invoke-ADB "-s $DeviceId shell run-as com.termux sh -c 'echo aeci-s24-server > /data/data/com.termux/files/home/.hostname'"
        }
        
        Write-Success "Mobile server deployed successfully!"
        Write-Info "Server will be accessible at:"
        Write-Info "- Local network: http://[device-ip]:3000"
        Write-Info "- Hostname: http://aeci-s24-server:3000"
        Write-Info "- Embedded link: aeci-mmu://server"
    }
    
    "start" {
        Write-Info "=== Starting Mobile Server ==="
        $startJob = Start-Job -ScriptBlock {
            param($adbPath, $deviceId)
            & $adbPath -s $deviceId shell "run-as com.termux sh -c 'cd /data/data/com.termux/files/home/aeci-server && npm start'"
        } -ArgumentList $adbPath, $DeviceId
        
        Write-Success "Mobile server started in background (Job ID: $($startJob.Id))"
    }
    
    "stop" {
        Write-Info "=== Stopping Mobile Server ==="
        Invoke-ADB "-s $DeviceId shell run-as com.termux pkill -f 'node.*server.js'"
        Write-Success "Mobile server stopped"
    }
    
    "status" {
        Write-Info "=== Mobile Server Status ==="
        $processes = Invoke-ADB "-s $DeviceId shell run-as com.termux ps aux" | Select-String "node"
        if ($processes -and $processes.Count -gt 0) {
            Write-Success "Mobile server is running"
            $processes | ForEach-Object { Write-Info $_.ToString() }
        } else {
            Write-Warning "Mobile server is not running"
        }
        
        # Check network info
        $ipOutput = Invoke-ADB "-s $DeviceId shell ip route get 1.1.1.1" | Select-String "src" | ForEach-Object {
            if ($_ -match "src (\d+\.\d+\.\d+\.\d+)") { $matches[1] }
        }
        if ($ipOutput) {
            $ip = $ipOutput
            Write-Info "Device IP: $ip"
            Write-Info "Server accessible at: http://$ip`:3000"
        }
    }
    
    "logs" {
        Write-Info "=== Mobile Server Logs ==="
        Write-Info "Press Ctrl+C to stop viewing logs"
        Invoke-ADB "-s $DeviceId shell run-as com.termux sh -c 'cd /data/data/com.termux/files/home/aeci-server && tail -f *.log 2>/dev/null || echo No log files found'"
    }
    
    "network" {
        Write-Info "=== Network Information ==="
        $ipOutput = Invoke-ADB "-s $DeviceId shell ip route get 1.1.1.1" | Select-String "src" | ForEach-Object {
            if ($_ -match "src (\d+\.\d+\.\d+\.\d+)") { $matches[1] }
        }
        
        if ($ipOutput) {
            $ip = $ipOutput
            Write-Info "Device IP: $ip"
            Write-Info "Server URL: http://$ip`:3000"
            Write-Info "Health Check: http://$ip`:3000/api/health"
        }
        
        Write-Info ""
        Write-Info "=== Connection URLs for Android App ==="
        Write-Info "Primary: http://aeci-s24-server:3000"
        if ($ipOutput) {
            Write-Info "IP-based: http://$ipOutput`:3000"
        }
        Write-Info "Embedded: aeci-mmu://server"
    }
    
    "test" {
        Write-Info "=== Testing Mobile Server Connection ==="
        $ipOutput = Invoke-ADB "-s $DeviceId shell ip route get 1.1.1.1" | Select-String "src" | ForEach-Object {
            if ($_ -match "src (\d+\.\d+\.\d+\.\d+)") { $matches[1] }
        }
        if ($ipOutput) {
            $ip = $ipOutput
            $url = "http://$ip`:3000/api/health"
            Write-Info "Testing connection to: $url"
            
            try {
                $response = Invoke-RestMethod -Uri $url -TimeoutSec 10 -ErrorAction Stop
                Write-Success "Server is responding!"
                Write-Info "Response: $($response | ConvertTo-Json -Compress)"
            } catch {
                Write-Error "Server not responding: $($_.Exception.Message)"
            }
        } else {
            Write-Error "Could not determine device IP address"
        }
    }
    
    "auto-setup" {
        Write-Info "=== Automatic Setup for AECI MMU Companion ==="
        
        # Deploy server
        Write-Info "Step 1: Deploying server..."
        & $PSCommandPath -Action deploy -DeviceId $DeviceId -AutoStart -SetupHostname
        
        if ($LASTEXITCODE -eq 0) {
            # Start server
            Write-Info "Step 2: Starting server..."
            & $PSCommandPath -Action start -DeviceId $DeviceId
            
            # Wait a moment for server to start
            Write-Info "Step 3: Waiting for server to start..."
            Start-Sleep -Seconds 5
            
            # Test connection
            Write-Info "Step 4: Testing connection..."
            & $PSCommandPath -Action test -DeviceId $DeviceId
            
            # Show network info
            Write-Info "Step 5: Network information..."
            & $PSCommandPath -Action network -DeviceId $DeviceId
            
            Write-Success "=== Auto-setup Complete! ==="
            Write-Info "Your Samsung S24 mobile server is ready for automatic connection."
            Write-Info "The Android app will automatically discover and connect to the server."
        } else {
            Write-Error "Auto-setup failed during deployment step."
        }
    }
    
    default {
        Write-Info "Available actions:"
        Write-Info "  deploy     - Deploy server files to device"
        Write-Info "  start      - Start the mobile server"
        Write-Info "  stop       - Stop the mobile server"
        Write-Info "  status     - Check server status"
        Write-Info "  logs       - View server logs"
        Write-Info "  network    - Show network information"
        Write-Info "  test       - Test server connection"
        Write-Info "  auto-setup - Complete automatic setup"
        Write-Info ""
        Write-Info "Options:"
        Write-Info "  -DeviceId   - Specify device ID"
        Write-Info "  -AutoStart  - Configure automatic startup"
        Write-Info "  -SetupHostname - Configure hostname for discovery"
        Write-Info ""
        Write-Info "Examples:"
        Write-Info "  .\deploy-to-phone.ps1 -Action auto-setup"
        Write-Info "  .\deploy-to-phone.ps1 -Action deploy -AutoStart -SetupHostname"
        Write-Info "  .\deploy-to-phone.ps1 -Action status"
    }
}

Write-Info "=== Operation Complete ===" 