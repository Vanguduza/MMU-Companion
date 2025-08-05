# Critical Compilation Error Fix Script
Write-Host "Starting critical compilation error fixes..." -ForegroundColor Green

$projectRoot = "c:\Users\tapiw\StudioProjects\MMU-Companion-\AECIMMUCompanion\AECIMMUCompanion"

# 1. Fix TimeTrackingRepositoryImpl - Add missing import
Write-Host "1. Fixing TimeTrackingRepositoryImpl imports..." -ForegroundColor Yellow

$timeTrackingFile = "$projectRoot\app\src\main\java\com\aeci\mmucompanion\data\repository\TimeTrackingRepositoryImpl.kt"
if (Test-Path $timeTrackingFile) {
    $content = Get-Content $timeTrackingFile -Raw
    
    if ($content -notmatch "import.*JobCodeSummary") {
        $content = $content -replace "(package com\.aeci\.mmucompanion\.data\.repository)", "`$1`n`nimport com.aeci.mmucompanion.domain.repository.JobCodeSummary"
        Set-Content $timeTrackingFile -Value $content -NoNewline
        Write-Host "  Added JobCodeSummary import" -ForegroundColor Green
    }
}

# 2. Fix FormTemplates.kt - Add missing 'id' parameters
Write-Host "2. Fixing FormTemplates.kt missing 'id' parameters..." -ForegroundColor Yellow

$formTemplatesFile = "$projectRoot\app\src\main\java\com\aeci\mmucompanion\data\templates\FormTemplates.kt"
if (Test-Path $formTemplatesFile) {
    $content = Get-Content $formTemplatesFile -Raw
    
    # Fix FormField constructors missing id parameter
    $content = $content -replace 'FormField\(\s*fieldName\s*=\s*"([^"]+)"', 'FormField(id = "$1", fieldName = "$1"'
    
    Set-Content $formTemplatesFile -Value $content -NoNewline
    Write-Host "  Fixed FormField constructor 'id' parameters" -ForegroundColor Green
}

Write-Host "Critical compilation error fixes completed!" -ForegroundColor Green
