#!/usr/bin/env pwsh

Write-Host "Fixing SiteAutoPopulationService syntax errors..." -ForegroundColor Green

$filePath = "app\src\main\java\com\aeci\mmucompanion\domain\usecase\SiteAutoPopulationService.kt"

if (Test-Path $filePath) {
    $content = Get-Content $filePath -Raw
    
    # Fix malformed data = mapOf patterns with missing closing parentheses
    # Replace patterns like: data = mapOf("key" to value, with proper structure
    
    # Pattern 1: Fix incomplete mapOf calls
    $content = $content -replace '(\s+)data = mapOf\("([^"]+)" to ([^,]+),\s*(\w+) = ([^)]+)\)', '$1$4 = $5'
    
    # Pattern 2: Fix currentdata assignment
    $content = $content -replace 'currentdata = mapOf', 'data = mapOf'
    
    # Pattern 3: Fix malformed map entries that mix data assignment with property assignment
    $content = $content -replace '(\s+)data = mapOf\("siteId" to ([^,]+),\s*(\w+) = ([^)]+)\s*\)', '$1siteId = $2'
    
    # Pattern 4: Fix lines that end with comma but no closing parenthesis
    $lines = $content -split "`n"
    $fixedLines = @()
    $inMapOf = $false
    
    foreach ($line in $lines) {
        if ($line -match 'data = mapOf\(') {
            $inMapOf = $true
            # Convert to direct property assignment
            if ($line -match 'data = mapOf\("(\w+)" to ([^,]+),?') {
                $property = $matches[1]
                $value = $matches[2]
                $fixedLines += $line -replace 'data = mapOf\("(\w+)" to ([^,]+),?', "$property = $value"
            } else {
                $fixedLines += $line
            }
        } elseif ($inMapOf -and $line -match '^\s*(\w+) = (.+)') {
            # This is a property assignment that should not be inside mapOf
            $fixedLines += $line
            $inMapOf = $false
        } elseif ($inMapOf -and $line -match '^\s*"(\w+)" to (.+),?') {
            # Convert map entry to property assignment
            $property = $matches[1]
            $value = $matches[2] -replace ',$', ''
            $fixedLines += $line -replace '"(\w+)" to (.+),?', "$property = $value"
        } else {
            $fixedLines += $line
            if ($line -match '\)$' -and $inMapOf) {
                $inMapOf = $false
            }
        }
    }
    
    $content = $fixedLines -join "`n"
    
    # Additional cleanup for common syntax errors
    $content = $content -replace ',\s*\)', ')'
    $content = $content -replace '\s+\)', ')'
    
    Set-Content $filePath -Value $content -NoNewline
    Write-Host "Fixed SiteAutoPopulationService.kt" -ForegroundColor Yellow
} else {
    Write-Host "File not found: $filePath" -ForegroundColor Red
}

Write-Host "SiteAutoPopulationService fixes complete!" -ForegroundColor Green
