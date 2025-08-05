# Fix enum value mismatches in template files
$templateFiles = Get-ChildItem -Path "app/src/main/java/com/aeci/mmucompanion/domain/model/forms" -Filter "*Template.kt" -Recurse

foreach ($file in $templateFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Fix specific enum value mismatches based on build errors
    $content = $content -replace 'FormType\.PUMP_INSPECTION_90DAY', 'FormType.PUMP_90_DAY_INSPECTION'
    $content = $content -replace 'FormType\.PUMP_INSPECTION_90_DAY', 'FormType.PUMP_90_DAY_INSPECTION'
    $content = $content -replace 'FormType\.MMU_PRODUCTION_DAILY_LOG', 'FormType.MMU_DAILY_LOG'
    $content = $content -replace 'FormType\.BOWIE_PUMP_WEEKLY', 'FormType.PUMP_WEEKLY_CHECK'
    $content = $content -replace 'FormType\.FIRE_EXTINGUISHER_INSPECTION', 'FormType.FIRE_EXTINGUISHER_INSPECTION'
    $content = $content -replace 'FormType\.PC_PUMP_PRESSURE_TEST', 'FormType.PC_PUMP_PRESSURE_TEST'
    $content = $content -replace 'FormType\.PRE_TASK_SAFETY_ASSESSMENT', 'FormType.PRETASK_SAFETY'
    $content = $content -replace 'FormType\.ON_BENCH_MMU_INSPECTION', 'FormType.ON_BENCH_MMU_INSPECTION'
    $content = $content -replace 'FormType\.MMU_CHASSIS_MAINTENANCE', 'FormType.MMU_CHASSIS_MAINTENANCE'
    $content = $content -replace 'FormType\.MMU_HANDOVER_CERTIFICATE', 'FormType.MMU_HANDOVER_CERTIFICATE'
    $content = $content -replace 'FormType\.MONTHLY_PROCESS_MAINTENANCE', 'FormType.MONTHLY_PROCESS_MAINTENANCE'
    $content = $content -replace 'FormType\.AVAILABILITY_UTILIZATION', 'FormType.AVAILABILITY_UTILIZATION'
    
    Set-Content -Path $file.FullName -Value $content -NoNewline
    Write-Host "Fixed enums in: $($file.Name)"
}

Write-Host "All enum mismatches have been fixed."
