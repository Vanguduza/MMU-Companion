# PowerShell script to fix structural issues in template files

# Find all template files
$templateFiles = Get-ChildItem -Path "app\src\main\java\com\aeci\mmucompanion\domain\model\forms" -Filter "*Template.kt" -Recurse

foreach ($file in $templateFiles) {
    Write-Host "Fixing structural issues in $($file.Name)..."
    
    # Read the file content
    $content = Get-Content $file.FullName -Raw
    
    # Remove orphaned closing parentheses
    $content = $content -replace '\)\s*\r\n\s*\)\s*\r\n\s*override', ')`r`n`r`n    override'
    
    # Fix missing closing braces for validation rules method
    $content = $content -replace '(\)\s*\r\n\s*)\s*override fun getRelatedFormUpdates', '$1`r`n    }`r`n`r`n    override fun getRelatedFormUpdates'
    
    # Fix orphaned parentheses before override methods
    $content = $content -replace '\s*\)\s*\r\n\s*override', '`r`n`r`n    override'
    
    # Fix missing closing braces at end of validation rules
    $content = $content -replace 'ValidationRule\([^)]*\)\s*\r\n\s*override', 'ValidationRule($1)`r`n    }`r`n`r`n    override'
    
    # Fix validation rules method structure
    $content = $content -replace 'override fun getValidationRules\(\): List<ValidationRule> \{\s*\r\n\s*return listOf\(\s*\r\n(.+?)\s*\r\n\s*\}\s*\r\n\s*override', 'override fun getValidationRules(): List<ValidationRule> {`r`n        return listOf(`r`n$1`r`n        )`r`n    }`r`n`r`n    override'
    
    # Fix orphaned content that should be inside validation rules
    $content = $content -replace '\}\s*\r\n\s*override fun getValidationRules\(\): List<ValidationRule> \{\s*\r\n\s*return listOf\(\s*\r\n(.+?)\s*override', '}`r`n`r`n    override fun getValidationRules(): List<ValidationRule> {`r`n        return listOf(`r`n$1        )`r`n    }`r`n`r`n    override'
    
    # Write the fixed content back to the file
    $content | Set-Content $file.FullName -NoNewline
    
    Write-Host "Fixed structural issues in $($file.Name)"
}

Write-Host "Structural fix complete!"
