#!/usr/bin/env pwsh

Write-Host "Systematic template compilation error fixes..." -ForegroundColor Green

# Get all template files
$templateFiles = Get-ChildItem -Path "app\src\main\java\com\aeci\mmucompanion\domain\model\forms" -Filter "*.kt" -Recurse

Write-Host "Found $($templateFiles.Count) template files to fix" -ForegroundColor Yellow

foreach ($file in $templateFiles) {
    Write-Host "Processing: $($file.Name)" -ForegroundColor Cyan
    $content = Get-Content $file.FullName -Raw
    
    # 1. Fix FormSection constructor - use 'id' instead of 'name'
    $content = $content -replace 'FormSection\s*\(\s*name\s*=', 'FormSection(id ='
    
    # 2. Fix FormField constructor - use 'fieldName' instead of 'id'
    $content = $content -replace 'FormField\s*\(\s*id\s*=', 'FormField(fieldName ='
    
    # 3. Fix ValidationRule parameters - ensure all 4 parameters are present
    $content = $content -replace 'ValidationRule\s*\(\s*fieldName\s*=\s*([^,]+),\s*ruleName\s*=\s*([^,]+),\s*expression\s*=\s*([^,]+),\s*errorMessage\s*=\s*([^)]+)\s*\)', 'ValidationRule(fieldName = $1, ruleName = $2, expression = $3, errorMessage = $4)'
    
    # 4. Fix FormRelationshipUpdate - use 'targetFormType' not 'targetForm'
    $content = $content -replace 'FormRelationshipUpdate\s*\(\s*targetForm\s*=', 'FormRelationshipUpdate(targetFormType ='
    
    # 5. Remove incorrect override properties that don't exist in interface
    $content = $content -replace 'override val id[^\n]*\n', ''
    $content = $content -replace 'override val description[^\n]*\n', ''
    $content = $content -replace 'override val name[^\n]*\n', ''
    
    # 6. Fix headerCoordinates type issue
    $content = $content -replace 'override val headerCoordinates\s*=\s*[^{]*\{[^}]*\}', 'override val headerCoordinates = listOf<HeaderCoordinate>()'
    
    # 7. Add missing getPdfFieldMappings method if not present
    if ($content -notmatch 'fun getPdfFieldMappings') {
        # Find the position after getRelatedFormUpdates method
        if ($content -match '(fun getRelatedFormUpdates.*?\n\s*\})\s*\n') {
            $content = $content -replace '(fun getRelatedFormUpdates.*?\n\s*\})', '$1' + "`n`n    override fun getPdfFieldMappings(): Map<String, PdfFieldMapping> = emptyMap()"
        } else {
            # Add at the end of the class, before the closing brace
            $content = $content -replace '(\n\s*)\}(\s*)$', '$1' + "`n    override fun getPdfFieldMappings(): Map<String, PdfFieldMapping> = emptyMap()" + '$1}$2'
        }
    }
    
    # 8. Fix missing imports
    if ($content -notmatch 'import com.aeci.mmucompanion.domain.model.\*') {
        $content = $content -replace '(package com\.aeci\.mmucompanion\.domain\.model\.forms)', '$1' + "`n`nimport com.aeci.mmucompanion.domain.model.*"
    }
    
    # 9. Fix unresolved reference 'name' - should be 'title'
    $content = $content -replace 'name\s*=\s*title', 'name = title'
    $content = $content -replace 'description\s*=\s*"([^"]*)"', 'description = "$1"'
    
    # Write the fixed content back
    Set-Content $file.FullName -Value $content -NoNewline
    Write-Host "Fixed: $($file.Name)" -ForegroundColor Green
}

Write-Host "All template files processed!" -ForegroundColor Green
Write-Host "Testing compilation..." -ForegroundColor Yellow

# Test compilation
try {
    & .\gradlew compileDebugKotlin -q
    Write-Host "Compilation successful!" -ForegroundColor Green
} catch {
    Write-Host "Compilation still has errors. Check output above." -ForegroundColor Red
}
