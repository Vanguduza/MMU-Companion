# PowerShell script to clean up all remaining syntax artifacts

# Find all template files
$templateFiles = Get-ChildItem -Path "app\src\main\java\com\aeci\mmucompanion\domain\model\forms" -Filter "*.kt" -Recurse

foreach ($file in $templateFiles) {
    Write-Host "Cleaning artifacts in $($file.Name)..."
    
    # Read the file content
    $content = Get-Content $file.FullName -Raw
    
    # Remove all `r`n artifacts
    $content = $content -replace '`r`n', "`r`n"
    
    # Fix double newlines that might have been created
    $content = $content -replace '\r\n\r\n\r\n', "`r`n`r`n"
    
    # Fix any remaining validation rule syntax issues
    $content = $content -replace '\)\s*,\s*\r\n\s*ValidationRule\(', '),`r`n        ValidationRule('
    
    # Fix orphaned ValidationRule entries
    $content = $content -replace '\)\s*\r\n\s*ValidationRule\(', '),`r`n        ValidationRule('
    
    # Fix any remaining parenthesis issues at end of validation rules
    $content = $content -replace 'ValidationRule\([^)]*\)\s*\r\n\s*\}\s*\r\n\s*\)', 'ValidationRule($1)`r`n    )'
    
    # Fix any double closing braces
    $content = $content -replace '\}\s*\r\n\s*\}\s*\r\n\s*\}', '}`r`n}'
    
    # Write the cleaned content back to the file
    $content | Set-Content $file.FullName -NoNewline
    
    Write-Host "Cleaned artifacts in $($file.Name)"
}

Write-Host "Artifact cleanup complete!"
