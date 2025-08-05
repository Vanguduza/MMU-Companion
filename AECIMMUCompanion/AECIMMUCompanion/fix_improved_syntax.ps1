# Fix remaining syntax errors in template files
$templateFiles = Get-ChildItem -Path "app/src/main/java/com/aeci/mmucompanion/domain/model/forms" -Filter "*Template.kt" -Recurse

foreach ($file in $templateFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Fix specific pattern: ), "validation string", "error message"),
    $content = $content -replace '\)\s*,\s*"[^"]*",\s*"[^"]*"\)\s*,', '),'
    
    # Fix pattern: ValidationRule("name", "rule", "message")
    $content = $content -replace ',\s*ValidationRule\([^)]*\)\s*,?', ''
    
    # Fix hanging ValidationRule at end of lists
    $content = $content -replace 'ValidationRule\([^)]*\)\s*\)', ''
    
    # Fix malformed relationship structure with leftover validation
    $content = $content -replace '\)\s*,\s*"[^"]*",\s*"[^"]*"\)\s*,\s*ValidationRule[^}]*\s*\)\s*\}', ')
        )
    }'
    
    # Fix double closing parentheses with validation text
    $content = $content -replace '\)\s*,\s*"[^"]*"\)\s*,', '),'
    
    # Fix FormRelationship mixed with FormRelationshipUpdate
    $content = $content -replace 'FormRelationship\(\s*targetFormType', 'FormRelationshipUpdate(
                targetFormType'
    
    # Remove any trailing validation rules at the end of sections
    $content = $content -replace ',\s*ValidationRule\([^)]*\)\s*\s*\)\s*\}', '
        )
    }'
    
    Set-Content -Path $file.FullName -Value $content -NoNewline
    Write-Host "Fixed improved syntax in: $($file.Name)"
}

Write-Host "All improved syntax fixes applied."
