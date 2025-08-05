# AECI MMU Companion - Alternative Mobile Server Setup
# Uses ADB port forwarding instead of running server on phone

param(
    [string]$Action = "start",
    [string]$DeviceId = "RFCX2054F5W"
)

# Color functions
function Write-Success { param([string]$msg) Write-Host $msg -ForegroundColor Green }
function Write-Error { param([string]$msg) Write-Host $msg -ForegroundColor Red }
function Write-Info { param([string]$msg) Write-Host $msg -ForegroundColor Cyan }
function Write-Warning { param([string]$msg) Write-Host $msg -ForegroundColor Yellow }

Write-Info "=== AECI MMU Companion - Alternative Mobile Server ==="
Write-Info "Using ADB port forwarding for phone-to-computer connection"

$adbPath = "C:\Users\VanGuduza\AppData\Local\Android\Sdk\platform-tools\adb.exe"

switch ($Action.ToLower()) {
    "start" {
        Write-Info "=== Starting Alternative Mobile Server ==="
        
        # Start the Node.js server on computer
        Write-Info "Starting Node.js server on computer..."
        if (Test-Path "server\server.js") {
            # Check if Node.js is installed
            try {
                $nodeVersion = node --version
                Write-Info "Using Node.js: $nodeVersion"
                
                # Install dependencies if needed
                if (-not (Test-Path "server\node_modules")) {
                    Write-Info "Installing Node.js dependencies..."
                    Set-Location server
                    npm install
                    Set-Location ..
                }
                
                # Start server in background
                Write-Info "Starting server on port 3000..."
                $serverJob = Start-Job -ScriptBlock {
                    Set-Location $args[0]
                    cd server
                    npm start
                } -ArgumentList (Get-Location)
                
                # Wait for server to start
                Start-Sleep -Seconds 3
                
                # Setup port forwarding from phone to computer
                Write-Info "Setting up ADB port forwarding..."
                & $adbPath -s $DeviceId reverse tcp:3000 tcp:3000
                
                # Test connection
                try {
                    $response = Invoke-RestMethod -Uri "http://localhost:3000/api/health" -TimeoutSec 5
                    Write-Success "Server is running and accessible!"
                    Write-Info "Server Job ID: $($serverJob.Id)"
                    Write-Info ""
                    Write-Success "=== Mobile Server Ready! ==="
                    Write-Info "Phone can access server at: http://localhost:3000"
                    Write-Info "Computer can access at: http://localhost:3000"
                    Write-Info "Health check: http://localhost:3000/api/health"
                    Write-Info ""
                    Write-Info "Your Android app will automatically connect to http://localhost:3000"
                    Write-Info "Port forwarding: Phone:3000 -> Computer:3000"
                } catch {
                    Write-Error "Server failed to start: $($_.Exception.Message)"
                }
            } catch {
                Write-Error "Node.js not found. Please install Node.js from: https://nodejs.org/"
                exit 1
            }
        } else {
            Write-Error "Server files not found in 'server' directory"
        }
    }
    
    "stop" {
        Write-Info "=== Stopping Alternative Mobile Server ==="
        
        # Remove port forwarding
        & $adbPath -s $DeviceId reverse --remove tcp:3000
        
        # Stop server jobs
        Get-Job | Where-Object { $_.State -eq "Running" } | Stop-Job
        Get-Job | Remove-Job
        
        Write-Success "Mobile server stopped and port forwarding removed"
    }
    
    "status" {
        Write-Info "=== Alternative Mobile Server Status ==="
        
        # Check server
        try {
            $response = Invoke-RestMethod -Uri "http://localhost:3000/api/health" -TimeoutSec 5
            Write-Success "Server is running"
            Write-Info "Response: $($response | ConvertTo-Json -Compress)"
        } catch {
            Write-Warning "Server not responding"
        }
        
        # Check port forwarding
        $portForward = & $adbPath -s $DeviceId reverse --list
        if ($portForward -match "tcp:3000") {
            Write-Success "Port forwarding active"
            Write-Info $portForward
        } else {
            Write-Warning "Port forwarding not active"
        }
        
        # Check jobs
        $jobs = Get-Job | Where-Object { $_.State -eq "Running" }
        if ($jobs) {
            Write-Success "Server job running (ID: $($jobs.Id -join ', '))"
        } else {
            Write-Warning "No server jobs running"
        }
    }
    
    "test" {
        Write-Info "=== Testing Alternative Mobile Server ==="
        
        # Test from computer
        try {
            $response = Invoke-RestMethod -Uri "http://localhost:3000/api/health" -TimeoutSec 5
            Write-Success "Computer can access server"
        } catch {
            Write-Error "Computer cannot access server"
        }
        
        # Test port forwarding by checking ADB
        $portForward = & $adbPath -s $DeviceId reverse --list
        if ($portForward -match "tcp:3000") {
            Write-Success "Phone port forwarding is active"
            Write-Info "Phone can access server at: http://localhost:3000"
        } else {
            Write-Error "Phone port forwarding not active"
        }
    }
    
    default {
        Write-Info "Available actions:"
        Write-Info "  start  - Start server with port forwarding"
        Write-Info "  stop   - Stop server and remove port forwarding"
        Write-Info "  status - Check server and port forwarding status"
        Write-Info "  test   - Test server connectivity"
        Write-Info ""
        Write-Info "Examples:"
        Write-Info "  .\deploy-alternative.ps1 -Action start"
        Write-Info "  .\deploy-alternative.ps1 -Action status"
    }
}

Write-Info "=== Operation Complete ===" 