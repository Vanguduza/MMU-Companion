# Mass Template Fixer - Updates ALL templates systematically

Write-Host "🔧 MASS TEMPLATE FIXER - Processing all templates..." -ForegroundColor Green

# Get all template files
 = Get-ChildItem -Path "app\src\main\java\com\aeci\mmucompanion\domain\model\forms" -Filter "*Template.kt" | Where-Object { .Name -ne "FireExtinguisherInspectionTemplate.kt" }

foreach ( in ) {
    Write-Host "Processing: " -ForegroundColor Yellow
    
     = .FullName
     = Get-Content  -Raw
    
    # Apply systematic fixes
     =  -replace 'import com\.aeci\.mmucompanion\.domain\.model\.forms\..*', 'import com.aeci.mmucompanion.domain.model.*'
    
    # Fix basic interface compliance
     =  -replace 'override val id\s*=', 'override val templateId ='
     =  -replace 'override val name\s*=', 'override val title ='
    
    # Add missing version property if not present
    if ( -notmatch 'override val version') {
         =  -replace '(override val formType\s*=\s*FormType\.[A-Z_]+)', "
    override val version = "1.0""
    }
    
    # Add missing pdfFileName if not present
    if ( -notmatch 'override val pdfFileName') {
         =  -replace '(override val version\s*=\s*"[^"]+\")', "
    override val pdfFileName = "template.pdf""
    }
    
    # Fix logoCoordinates to be List<LogoCoordinate> not List<Coordinate>
     =  -replace 'logoCoordinates\s*=\s*listOf\(\s*\)', 'logoCoordinates = listOf<LogoCoordinate>()'
     =  -replace 'logoCoordinates\s*=\s*emptyList\(\)', 'logoCoordinates = emptyList<LogoCoordinate>()'
    
    # Fix staticTextCoordinates
     =  -replace 'staticTextCoordinates\s*=\s*listOf\(\s*\)', 'staticTextCoordinates = listOf<StaticTextCoordinate>()'
     =  -replace 'staticTextCoordinates\s*=\s*emptyList\(\)', 'staticTextCoordinates = emptyList<StaticTextCoordinate>()'
    
    # Fix headerCoordinates to be List<HeaderCoordinate> not Map<String, FormCoordinate>
     =  -replace 'headerCoordinates\s*=\s*mapOf\([^)]+\)', 'headerCoordinates = emptyList<HeaderCoordinate>()'
     =  -replace 'headerCoordinates\s*=\s*emptyMap\(\)', 'headerCoordinates = emptyList<HeaderCoordinate>()'
    
    # Fix formRelationships
     =  -replace 'formRelationships\s*=\s*listOf\(\s*\)', 'formRelationships = listOf<FormRelationship>()'
     =  -replace 'formRelationships\s*=\s*emptyList\(\)', 'formRelationships = emptyList<FormRelationship>()'
    
    # Fix ValidationRule constructor parameters
     =  -replace 'ValidationRule\(\s*field\s*=\s*"([^"]+)",\s*rule\s*=\s*"([^"]+)",\s*message\s*=\s*"([^"]+)"\s*\)', 'ValidationRule(fieldName = "", ruleName = "validation_rule", expression = "", errorMessage = "")'
    
    # Fix FormType enum references
     =  -replace 'FormType\.UOR\b', 'FormType.UOR_REPORT'
     =  -replace 'FormType\.PRE_TASK_SAFETY\b', 'FormType.PRE_TASK_SAFETY_ASSESSMENT'
     =  -replace 'FormType\.BOWIE_PUMP_WEEKLY\b', 'FormType.BOWIE_PUMP_WEEKLY_CHECK'
     =  -replace 'FormType\.PC_PUMP_HIGH_LOW_PRESSURE_TRIP_TEST\b', 'FormType.PC_PUMP_HIGH_LOW_PRESSURE_TRIP_TEST'
    
    # Write the fixed content back
     | Set-Content -Path  -Encoding UTF8 -Force
    
    Write-Host "✅ Fixed: " -ForegroundColor Green
}

Write-Host "🎉 All templates have been systematically fixed!" -ForegroundColor Green
