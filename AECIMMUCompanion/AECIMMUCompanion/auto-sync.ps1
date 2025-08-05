# GitHub Auto-Sync Script for MMU Companion
# This script automatically detects changes, commits, and pushes to GitHub

param(
    [string]$CommitMessage = "Auto-sync: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')",
    [switch]$Force,
    [switch]$Verbose
)

Write-Host "🔄 MMU Companion - GitHub Auto-Sync" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green

# Change to project directory
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot

# Function to write colored output
function Write-ColorOutput($ForegroundColor, $Message) {
    Write-Host $Message -ForegroundColor $ForegroundColor
}

try {
    # Check if we're in a git repository
    $gitStatus = git status --porcelain 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-ColorOutput "Red" "❌ Error: Not in a git repository"
        exit 1
    }

    # Check for changes
    if ([string]::IsNullOrWhiteSpace($gitStatus)) {
        Write-ColorOutput "Yellow" "ℹ️  No changes detected"
        if (-not $Force) {
            Write-ColorOutput "Cyan" "💡 Use -Force to push anyway"
            exit 0
        }
    } else {
        Write-ColorOutput "Cyan" "📝 Changes detected:"
        git status --short
    }

    # Pull latest changes first
    Write-ColorOutput "Blue" "⬇️  Pulling latest changes..."
    git pull origin main
    if ($LASTEXITCODE -ne 0) {
        Write-ColorOutput "Red" "❌ Error pulling changes"
        exit 1
    }

    # Add all changes
    Write-ColorOutput "Blue" "➕ Adding changes..."
    git add -A
    
    # Check if there's anything to commit after adding
    $statusAfterAdd = git diff --cached --name-only
    if ([string]::IsNullOrWhiteSpace($statusAfterAdd) -and -not $Force) {
        Write-ColorOutput "Yellow" "ℹ️  No staged changes to commit"
        exit 0
    }

    # Commit changes
    Write-ColorOutput "Blue" "💾 Committing changes..."
    git commit -m $CommitMessage
    if ($LASTEXITCODE -ne 0 -and -not $Force) {
        Write-ColorOutput "Red" "❌ Error committing changes"
        exit 1
    }

    # Push to GitHub
    Write-ColorOutput "Blue" "⬆️  Pushing to GitHub..."
    git push origin main
    if ($LASTEXITCODE -ne 0) {
        Write-ColorOutput "Red" "❌ Error pushing to GitHub"
        exit 1
    }

    Write-ColorOutput "Green" "✅ Successfully synced to GitHub!"
    
    # Show remote repository info
    $remoteUrl = git remote get-url origin
    Write-ColorOutput "Cyan" "🔗 Repository: $remoteUrl"
    
    # Show latest commit
    $latestCommit = git log -1 --oneline
    Write-ColorOutput "Cyan" "📋 Latest commit: $latestCommit"

} catch {
    Write-ColorOutput "Red" "❌ Unexpected error: $($_.Exception.Message)"
    exit 1
}

Write-Host ""
Write-ColorOutput "Green" "🎉 Auto-sync completed successfully!"
