# Fix pdfCoordinates parameter in all template files
$templateFiles = Get-ChildItem -Path "app/src/main/java/com/aeci/mmucompanion/domain/model/forms" -Filter "*Template.kt" -Recurse

foreach ($file in $templateFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Replace pdfCoordinates = PDFCoordinate(x, y, width, height) with individual parameters
    $content = $content -replace 'pdfCoordinates = PDFCoordinate\(([^,]+),\s*([^,]+),\s*([^,]+),\s*([^)]+)\)', 'x = $1, y = $2, width = $3, height = $4'
    
    # Also remove any remaining references to isCalculated parameter if it's causing issues
    $content = $content -replace ',\s*isCalculated = [^,\)]+', ''
    
    # Remove validationRules parameter if it's causing issues
    $content = $content -replace ',\s*validationRules = [^,\)]+', ''
    
    Set-Content -Path $file.FullName -Value $content -NoNewline
    Write-Host "Fixed: $($file.Name)"
}

Write-Host "All template files have been fixed."
