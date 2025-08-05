# Fix FormType imports and usage in template files
$templateFiles = Get-ChildItem -Path "app/src/main/java/com/aeci/mmucompanion/domain/model/forms" -Filter "*Template.kt" -Recurse

foreach ($file in $templateFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Add specific import for the main FormType if not present
    if ($content -notlike "*import com.aeci.mmucompanion.domain.model.FormType*") {
        $content = $content -replace "(package com\.aeci\.mmucompanion\.domain\.model\.forms)", "`$1`n`nimport com.aeci.mmucompanion.domain.model.FormType"
    }
    
    # Update enum values to match the main FormType enum values (from FormModels.kt)
    $content = $content -replace 'FormType\.PUMP_90_DAY_INSPECTION', 'FormType.PUMP_90_DAY_INSPECTION'
    $content = $content -replace 'FormType\.MMU_DAILY_LOG', 'FormType.MMU_DAILY_LOG'
    $content = $content -replace 'FormType\.PUMP_WEEKLY_CHECK', 'FormType.PUMP_WEEKLY_CHECK'
    $content = $content -replace 'FormType\.PRETASK_SAFETY', 'FormType.PRETASK_SAFETY'
    
    Set-Content -Path $file.FullName -Value $content -NoNewline
    Write-Host "Fixed imports in: $($file.Name)"
}

Write-Host "All FormType import issues have been fixed."
