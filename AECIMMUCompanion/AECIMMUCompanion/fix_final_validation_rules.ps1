#!/usr/bin/env pwsh

Write-Host "Final compilation error fixes..." -ForegroundColor Green

# Fix ValidationRule parameters more comprehensively
Write-Host "Fixing ValidationRule parameters..." -ForegroundColor Yellow

$templateFiles = Get-ChildItem -Path "app\src\main\java\com\aeci\mmucompanion\domain\model\forms" -Filter "*.kt" -Recurse
foreach ($file in $templateFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Fix ValidationRule constructor with proper parameters
    $content = $content -replace 'ValidationRule\s*\(\s*fieldName\s*=\s*([^,]+),\s*rule\s*=\s*([^,]+),\s*message\s*=\s*([^)]+)\)', 'ValidationRule(fieldName = $1, ruleName = "validation", expression = $2, errorMessage = $3)'
    
    # Alternative pattern
    $content = $content -replace 'ValidationRule\s*\(\s*fieldName\s*=\s*([^,]+),\s*ruleName\s*=\s*([^,]+),\s*rule\s*=\s*([^,]+),\s*message\s*=\s*([^)]+)\)', 'ValidationRule(fieldName = $1, ruleName = $2, expression = $3, errorMessage = $4)'
    
    Set-Content $file.FullName -Value $content -NoNewline
}

# Fix FormRelationship parameters
Write-Host "Fixing FormRelationship parameters..." -ForegroundColor Yellow

foreach ($file in $templateFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Fix FormRelationship parameters - correct parameter names
    $content = $content -replace 'FormRelationship\s*\(\s*sourceFormType\s*=', 'FormRelationship(sourceField ='
    $content = $content -replace 'FormRelationship\s*\(\s*targetFormType\s*=', 'FormRelationship(sourceField = "field_name", targetForm ='
    $content = $content -replace ',\s*sourceFormType\s*=', ', sourceField ='
    $content = $content -replace ',\s*targetFormType\s*=', ', targetForm ='
    
    # Fix specific cases where targetFormType is used incorrectly
    $content = $content -replace 'targetFormType\s*=\s*FormType\.', 'targetForm = FormType.'
    
    Set-Content $file.FullName -Value $content -NoNewline
}

Write-Host "Testing compilation..." -ForegroundColor Yellow
& .\gradlew compileDebugKotlin
