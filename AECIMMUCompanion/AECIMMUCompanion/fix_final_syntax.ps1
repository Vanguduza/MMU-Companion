# PowerShell script to fix remaining syntax issues in template files

# Define the template files to fix
$templateFiles = @(
    "app\src\main\java\com\aeci\mmucompanion\domain\model\forms\AvailabilityUtilizationTemplate.kt",
    "app\src\main\java\com\aeci\mmucompanion\domain\model\forms\FireExtinguisherInspectionTemplate.kt",
    "app\src\main\java\com\aeci\mmucompanion\domain\model\forms\MmuHandoverCertificateTemplate.kt",
    "app\src\main\java\com\aeci\mmucompanion\domain\model\forms\MmuQualityReportTemplate.kt",
    "app\src\main\java\com\aeci\mmucompanion\domain\model\forms\MonthlyProcessMaintenanceTemplate.kt",
    "app\src\main\java\com\aeci\mmucompanion\domain\model\forms\OnBenchMmuInspectionTemplate.kt",
    "app\src\main\java\com\aeci\mmucompanion\domain\model\forms\PcPumpPressureTripTestTemplate.kt",
    "app\src\main\java\com\aeci\mmucompanion\domain\model\forms\PreTaskTemplate.kt"
)

foreach ($file in $templateFiles) {
    if (Test-Path $file) {
        Write-Host "Fixing syntax issues in $file..."
        
        # Read the file content
        $content = Get-Content $file -Raw
        
        # Fix orphaned ValidationRule entries (missing comma before ValidationRule)
        $content = $content -replace '(\)\s*\r?\n\s*)ValidationRule\(', '$1,`r`n        ValidationRule('
        
        # Fix misplaced closing parentheses with coordinate parameters
        $content = $content -replace 'isRequired = true\),\s*\r?\n\s*x = (\d+\.?\d*)f, y = (\d+\.?\d*)f, width = (\d+\.?\d*)f, height = (\d+\.?\d*)f', 'isRequired = true,`r`n                            x = $1f, y = $2f, width = $3f, height = $4f'
        
        # Fix patterns where coordinate parameters are on separate lines after closing parenthesis
        $content = $content -replace '(\s+)(\w+\s*=\s*[^,\r\n]+),\s*\r?\n(\s+)x\s*=\s*(\d+\.?\d*)f,\s*y\s*=\s*(\d+\.?\d*)f,\s*width\s*=\s*(\d+\.?\d*)f,\s*height\s*=\s*(\d+\.?\d*)f', '$1$2,`r`n$3x = $4f, y = $5f, width = $6f, height = $7f'
        
        # Fix specific pattern: isRequired = true), x = ... (missing comma and wrong parenthesis)
        $content = $content -replace 'isRequired = true\),\s*\r?\n\s*x = ', 'isRequired = true,`r`n                            x = '
        
        # Fix ValidationRule entries that are missing commas at the end
        $content = $content -replace '(\)\s*\r?\n\s*)ValidationRule\(', '$1,`r`n        ValidationRule('
        
        # Fix trailing ValidationRule without comma
        $content = $content -replace '(\)\s*\r?\n\s*}\s*\r?\n)', '$1`r`n    )'
        
        # Write the fixed content back to the file
        $content | Set-Content $file -NoNewline
        
        Write-Host "Fixed syntax issues in $file"
    } else {
        Write-Host "File not found: $file"
    }
}

Write-Host "Syntax fix complete!"
