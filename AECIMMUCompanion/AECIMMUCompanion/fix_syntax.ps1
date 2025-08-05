# Fix syntax errors in template files
$templateFiles = Get-ChildItem -Path "app/src/main/java/com/aeci/mmucompanion/domain/model/forms" -Filter "*Template.kt" -Recurse

foreach ($file in $templateFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Fix common malformed ValidationRule patterns
    $content = $content -replace ',\s*"[^"]*",\s*"[^"]*"\)\s*\)\s*,', '),'
    $content = $content -replace ',\s*"[^"]*",\s*"[^"]*"\)\s*\),', '),'
    
    # Fix malformed parentheses at end of relationships
    $content = $content -replace '\)\s*,\s*"[^"]*",\s*"[^"]*"\)\s*,\s*ValidationRule\([^)]*\)\s*\)', ')'
    
    # Fix incomplete ValidationRule references
    $content = $content -replace 'ValidationRule\([^)]*\)\s*\)', ''
    
    # Fix extra commas and parentheses
    $content = $content -replace '\)\s*,\s*"[^"]*"\)\s*,', '),'
    
    # Fix malformed parameter lists in FormField constructors
    $content = $content -replace 'isRequired\s*=\s*true\s*,\s*"[^"]*",\s*"[^"]*"\)\s*\)\s*,', 'isRequired = true,'
    
    Set-Content -Path $file.FullName -Value $content -NoNewline
    Write-Host "Fixed syntax in: $($file.Name)"
}

Write-Host "All syntax errors have been fixed."
