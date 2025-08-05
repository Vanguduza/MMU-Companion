# PowerShell script to fix all remaining artifacts and structural issues

# Find all template files
$templateFiles = Get-ChildItem -Path "app\src\main\java\com\aeci\mmucompanion\domain\model\forms" -Filter "*Template.kt" -Recurse

foreach ($file in $templateFiles) {
    Write-Host "Fixing final issues in $($file.Name)..."
    
    # Read the file content
    $content = Get-Content $file.FullName -Raw
    
    # Remove all remaining `r`n artifacts
    $content = $content -replace '`r`n', "`r`n"
    
    # Fix validation rules that use override val instead of override fun
    $content = $content -replace 'override val validationRules = listOf\(', 'override fun getValidationRules(): List<ValidationRule> {`r`n        return listOf('
    
    # Fix missing closing for validation rules method when followed by override
    $content = $content -replace '(\)\s*)\s*override fun get', '        )`r`n    }`r`n`r`n    override fun get'
    
    # Fix missing closing parenthesis for listOf in validation rules
    $content = $content -replace 'ValidationRule\(([^)]*)\)\s*\)\s*\}\s*override', 'ValidationRule($1)`r`n        )`r`n    }`r`n`r`n    override'
    
    # Fix missing return statement closing
    $content = $content -replace 'return listOf\(\s*\r\n(.+?)\r\n\s*\)\s*override', 'return listOf(`r`n$1`r`n        )`r`n    }`r`n`r`n    override'
    
    # Fix missing closing brace for validation rules method
    $content = $content -replace '(\)\s*\r\n\s*)\s*override fun getRelatedFormUpdates', '        )`r`n    }`r`n`r`n    override fun getRelatedFormUpdates'
    
    # Fix orphaned closing parentheses
    $content = $content -replace '\)\s*\r\n\s*\)\s*\r\n\s*override', ')`r`n`r`n    override'
    
    # Write the fixed content back to the file
    $content | Set-Content $file.FullName -NoNewline
    
    Write-Host "Fixed final issues in $($file.Name)"
}

Write-Host "Final fix complete!"
