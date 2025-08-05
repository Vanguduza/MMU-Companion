# WORKING COMPREHENSIVE FIX SCRIPT
Write-Host "Starting comprehensive form system fix..." -ForegroundColor Green

# Step 1: Remove conflicting files
Write-Host "Removing conflicting files..." -ForegroundColor Yellow
$conflictFiles = @(
    "app\src\main\java\com\aeci\mmucompanion\domain\model\forms\FormTemplateInterfaces.kt",
    "app\src\main\java\com\aeci\mmucompanion\domain\model\forms\DigitalFormTemplates.kt"
)

foreach ($file in $conflictFiles) {
    if (Test-Path $file) {
        Remove-Item $file -Force
        Write-Host "Removed: $file" -ForegroundColor Green
    }
}

# Step 2: Fix FormRepository.kt
Write-Host "Fixing FormRepository.kt..." -ForegroundColor Yellow
$repoPath = "app\src\main\java\com\aeci\mmucompanion\domain\repository\FormRepository.kt"
if (Test-Path $repoPath) {
    $content = Get-Content $repoPath -Raw
    $content = $content -replace 'import com\.aeci\.mmucompanion\.domain\.model\.forms\.FormType', 'import com.aeci.mmucompanion.domain.model.FormType'
    $content = $content -replace 'import com\.aeci\.mmucompanion\.domain\.model\.forms\.FormStatus', 'import com.aeci.mmucompanion.domain.model.FormStatus'
    $content = $content -replace 'enum class SyncStatus[^}]+}', ''
    $content | Set-Content -Path $repoPath -Encoding UTF8 -Force
    Write-Host "Fixed FormRepository.kt" -ForegroundColor Green
}

# Step 3: Test compilation
Write-Host "Testing compilation..." -ForegroundColor Yellow
& ".\gradlew" "compileDebugKotlin"

Write-Host "Comprehensive fix completed!" -ForegroundColor Green
