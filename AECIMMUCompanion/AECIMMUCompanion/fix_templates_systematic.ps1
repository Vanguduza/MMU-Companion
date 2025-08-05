# Template Fixer - Updates all templates to use new interface

Write-Host "Fixing all template files to use new DigitalFormTemplate interface..." -ForegroundColor Green

# List of all template files that need updating
 = @(
    "PumpInspection90DayTemplate.kt",
    "BowiePumpWeeklyCheckTemplate.kt", 
    "UORTemplate.kt",
    "TimesheetTemplate.kt",
    "BlastHoleLogTemplate.kt",
    "MmuQualityReportTemplate.kt",
    "MmuProductionDailyLogTemplate.kt",
    "MmuChassisMaintenanceTemplate.kt",
    "MmuHandoverCertificateTemplate.kt",
    "OnBenchMmuInspectionTemplate.kt",
    "PcPumpPressureTripTestTemplate.kt",
    "MonthlyProcessMaintenanceTemplate.kt",
    "PreTaskTemplate.kt",
    "JobCardTemplate.kt",
    "AvailabilityUtilizationTemplate.kt"
)

foreach ( in ) {
     = "app\src\main\java\com\aeci\mmucompanion\domain\model\forms\"
    
    if (Test-Path ) {
        Write-Host "Fixing ..." -ForegroundColor Yellow
        
        # Read current content
         = Get-Content  -Raw
        
        # Apply systematic fixes
         =  -replace 'override val id =', 'override val templateId =' 
         =  -replace 'override val name =', 'override val title ='
         =  -replace 'FormType\.PUMP_INSPECTION_90_DAY', 'FormType.PUMP_INSPECTION_90_DAY'
         =  -replace 'FormType\.UOR', 'FormType.UOR_REPORT'
         =  -replace 'FormType\.MMU_PRODUCTION_DAILY_LOG', 'FormType.MMU_PRODUCTION_DAILY_LOG'
         =  -replace 'FormType\.PRE_TASK_SAFETY', 'FormType.PRE_TASK_SAFETY_ASSESSMENT'
         =  -replace 'FormType\.BOWIE_PUMP_WEEKLY', 'FormType.BOWIE_PUMP_WEEKLY_CHECK'
         =  -replace 'FormType\.PC_PUMP_HIGH_LOW_PRESSURE_TRIP_TEST', 'FormType.PC_PUMP_HIGH_LOW_PRESSURE_TRIP_TEST'
        
        # Add missing version property if not present
        if ( -notmatch 'override val version') {
             =  -replace '(override val formType = FormType\.[A-Z_]+)', "
    override val version = "1.0""
        }
        
        # Fix ValidationRule constructor calls
         =  -replace 'ValidationRule\(\s*field\s*=\s*"([^"]+)",\s*rule\s*=\s*"([^"]+)",\s*message\s*=\s*"([^"]+)"\s*\)', 'ValidationRule(fieldName = "", ruleName = "validation_rule", expression = "", errorMessage = "")'
        
        # Write fixed content back
         | Set-Content -Path  -Encoding UTF8 -Force
        
        Write-Host " fixed!" -ForegroundColor Green
    } else {
        Write-Host "Warning:  not found!" -ForegroundColor Red
    }
}

Write-Host "All template files have been systematically fixed!" -ForegroundColor Green
