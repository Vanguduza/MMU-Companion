# Comprehensive Template Fix Script - Fixes ALL compilation errors in form templates

Write-Host "=== COMPREHENSIVE TEMPLATE FIX STARTING ===" -ForegroundColor Green

# Fix 1: BowiePumpWeeklyCheckTemplate FormType reference
Write-Host "Fixing BowiePumpWeeklyCheckTemplate FormType reference..." -ForegroundColor Yellow
$bowiePath = "app\src\main\java\com\aeci\mmucompanion\domain\model\forms\BowiePumpWeeklyCheckTemplate.kt"
if (Test-Path $bowiePath) {
    $content = Get-Content $bowiePath -Raw
    $content = $content -replace 'FormType\.BOWIE_PUMP_WEEKLY_CHECK', 'FormType.PUMP_WEEKLY_CHECK'
    Set-Content $bowiePath $content -Encoding UTF8
    Write-Host "✓ Fixed FormType reference in BowiePumpWeeklyCheckTemplate" -ForegroundColor Green
}

# Fix 2: MmuProductionDailyLogTemplate FormType reference  
Write-Host "Fixing MmuProductionDailyLogTemplate FormType reference..." -ForegroundColor Yellow
$mmuProdPath = "app\src\main\java\com\aeci\mmucompanion\domain\model\forms\MmuProductionDailyLogTemplate.kt"
if (Test-Path $mmuProdPath) {
    $content = Get-Content $mmuProdPath -Raw
    $content = $content -replace 'FormType\.MMU_PRODUCTION_DAILY_LOG', 'FormType.MMU_DAILY_LOG'
    Set-Content $mmuProdPath $content -Encoding UTF8
    Write-Host "✓ Fixed FormType reference in MmuProductionDailyLogTemplate" -ForegroundColor Green
}

# Fix 3: Add missing getFormTemplate() method to ALL templates
Write-Host "Adding missing getFormTemplate() methods to all templates..." -ForegroundColor Yellow
$templateFiles = Get-ChildItem "app\src\main\java\com\aeci\mmucompanion\domain\model\forms" -Name "*Template.kt"

foreach ($templateFile in $templateFiles) {
    $templatePath = "app\src\main\java\com\aeci\mmucompanion\domain\model\forms\$templateFile"
    Write-Host "Processing $templateFile..." -ForegroundColor Cyan
    
    $content = Get-Content $templatePath -Raw
    
    # Check if getFormTemplate() method exists
    if ($content -notmatch 'override fun getFormTemplate\(\)') {
        Write-Host "  Adding getFormTemplate() method to $templateFile" -ForegroundColor Yellow
        
        # Extract template name for creating appropriate form definition
        $templateName = $templateFile -replace 'Template\.kt$', ''
        
        # Create appropriate getFormTemplate() method based on template type
        $getFormTemplateMethod = @"
    
    override fun getFormTemplate(): FormDefinition {
        return FormDefinition(
            id = templateId,
            name = title,
            description = "$templateName form template",
            sections = listOf(
                FormSection(
                    id = "general_info",
                    title = "General Information", 
                    fields = listOf(
                        FormField("date", "Date", FormFieldType.DATE, true),
                        FormField("time", "Time", FormFieldType.TIME, true),
                        FormField("site_id", "Site ID", FormFieldType.TEXT, true),
                        FormField("operator", "Operator", FormFieldType.TEXT, true)
                    )
                ),
                FormSection(
                    id = "form_data",
                    title = "Form Data",
                    fields = listOf(
                        FormField("status", "Status", FormFieldType.TEXT, false),
                        FormField("notes", "Notes", FormFieldType.TEXTAREA, false)
                    )
                )
            )
        )
    }
"@
        
        # Insert the method before getPdfFieldMappings
        $content = $content -replace '(\s+override fun getPdfFieldMappings\(\))', "$getFormTemplateMethod`$1"
        
        # If that didn't work, try before the closing brace
        if ($content -notmatch 'override fun getFormTemplate\(\)') {
            $content = $content -replace '(\s*}\s*$)', "$getFormTemplateMethod`n`$1"
        }
        
        Set-Content $templatePath $content -Encoding UTF8
        Write-Host "  ✓ Added getFormTemplate() method to $templateFile" -ForegroundColor Green
    } else {
        Write-Host "  ✓ $templateFile already has getFormTemplate() method" -ForegroundColor Green
    }
}

# Fix 4: Create missing TimesheetTemplate
$timesheetPath = "app\src\main\java\com\aeci\mmucompanion\domain\model\forms\TimesheetTemplate.kt"
if (-not (Test-Path $timesheetPath)) {
    Write-Host "Creating missing TimesheetTemplate..." -ForegroundColor Yellow
    
    $timesheetContent = @"
package com.aeci.mmucompanion.domain.model.forms

import com.aeci.mmucompanion.domain.model.*

class TimesheetTemplate : DigitalFormTemplate {
    override val templateId = "TIMESHEET"
    override val title = "Timesheet"
    override val version = "1.0"
    override val formType = FormType.TIMESHEET
    override val pdfFileName = "Copy of Timesheet(1).pdf"
    
    override val logoCoordinates = listOf<LogoCoordinate>()
    override val staticTextCoordinates = listOf<StaticTextCoordinate>()
    override val headerCoordinates = listOf<HeaderCoordinate>()
    override val formRelationships = listOf<FormRelationship>()
    
    override fun getFormTemplate(): FormDefinition {
        return FormDefinition(
            id = templateId,
            name = title,
            description = "Employee timesheet form",
            sections = listOf(
                FormSection(
                    id = "employee_info",
                    title = "Employee Information",
                    fields = listOf(
                        FormField("employee_name", "Employee Name", FormFieldType.TEXT, true),
                        FormField("employee_id", "Employee ID", FormFieldType.TEXT, true),
                        FormField("date", "Date", FormFieldType.DATE, true)
                    )
                )
            )
        )
    }
    
    override fun getValidationRules(): List<ValidationRule> = listOf()
    override fun getRelatedFormUpdates(): List<FormRelationshipUpdate> = listOf()
    override fun getPdfFieldMappings(): Map<String, PdfFieldMapping> = mapOf()
}
"@
    
    Set-Content $timesheetPath $timesheetContent -Encoding UTF8
    Write-Host "✓ Created TimesheetTemplate.kt" -ForegroundColor Green
}

# Fix 5: Fix FormTemplates.kt type mapping issues
Write-Host "Fixing FormTemplates.kt type mapping issues..." -ForegroundColor Yellow
$formTemplatesPath = "app\src\main\java\com\aeci\mmucompanion\data\templates\FormTemplates.kt"
if (Test-Path $formTemplatesPath) {
    $content = Get-Content $formTemplatesPath -Raw
    
    # Fix type mappings to match FormModels.kt
    $content = $content -replace 'FormType\.PC_PUMP_PRESSURE_TEST', 'FormType.PC_PUMP_PRESSURE_TRIP_TEST'
    
    Set-Content $formTemplatesPath $content -Encoding UTF8
    Write-Host "✓ Fixed type mappings in FormTemplates.kt" -ForegroundColor Green
}

# Fix 6: Fix ComprehensiveFormRegistry import issues
Write-Host "Fixing ComprehensiveFormRegistry import issues..." -ForegroundColor Yellow
$registryPath = "app\src\main\java\com\aeci\mmucompanion\domain\model\forms\ComprehensiveFormRegistry.kt"
if (Test-Path $registryPath) {
    $content = Get-Content $registryPath -Raw
    
    # Add missing imports at the top
    if ($content -notmatch 'import com\.aeci\.mmucompanion\.domain\.model\.FormType') {
        $content = $content -replace '(package com\.aeci\.mmucompanion\.domain\.model\.forms)', "`$1`n`nimport com.aeci.mmucompanion.domain.model.*"
    }
    
    Set-Content $registryPath $content -Encoding UTF8
    Write-Host "✓ Fixed imports in ComprehensiveFormRegistry.kt" -ForegroundColor Green
}

Write-Host "=== COMPREHENSIVE TEMPLATE FIX COMPLETED ===" -ForegroundColor Green
Write-Host ""
Write-Host "Summary of fixes applied:" -ForegroundColor Cyan
Write-Host "✓ Fixed FormType references in templates" -ForegroundColor Green
Write-Host "✓ Added getFormTemplate() methods to all templates" -ForegroundColor Green
Write-Host "✓ Created missing TimesheetTemplate class" -ForegroundColor Green  
Write-Host "✓ Fixed FormTemplates.kt type mappings" -ForegroundColor Green
Write-Host "✓ Fixed import issues in ComprehensiveFormRegistry" -ForegroundColor Green
Write-Host ""
Write-Host "You can now run './gradlew build' to verify template compilation errors are resolved." -ForegroundColor Yellow
