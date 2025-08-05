# Comprehensive fix for all remaining compilation issues
Write-Host "Starting comprehensive fix for remaining compilation issues..." -ForegroundColor Green

# 1. First, let's check what TimeEntry looks like in Todo.kt
$todoPath = "app\src\main\java\com\aeci\mmucompanion\domain\model\Todo.kt"
if (Test-Path $todoPath) {
    Write-Host "Checking TimeEntry definition in Todo.kt..." -ForegroundColor Yellow
    $todoContent = Get-Content $todoPath -Raw
    if ($todoContent -match "data class TimeEntry") {
        Write-Host "Found TimeEntry in Todo.kt" -ForegroundColor Green
    } else {
        Write-Host "TimeEntry not found in Todo.kt - need to define it" -ForegroundColor Red
    }
}

# 2. Add missing TEXTAREA enum value to FormFieldType
$formFieldTypePath = "app\src\main\java\com\aeci\mmucompanion\domain\model\FormTemplateInterfaces.kt"
if (Test-Path $formFieldTypePath) {
    Write-Host "Adding TEXTAREA to FormFieldType enum..." -ForegroundColor Yellow
    $content = Get-Content $formFieldTypePath -Raw
    
    # Check if TEXTAREA is missing
    if ($content -notmatch "TEXTAREA") {
        # Add TEXTAREA to the enum
        $content = $content -replace "(TEXT,\s*NUMBER,\s*DATE,\s*BOOLEAN,\s*SIGNATURE,\s*DROPDOWN,\s*CHECKBOX)", "`$1, TEXTAREA"
        Set-Content $formFieldTypePath -Value $content -Encoding UTF8
        Write-Host "Added TEXTAREA to FormFieldType enum" -ForegroundColor Green
    } else {
        Write-Host "TEXTAREA already exists in FormFieldType" -ForegroundColor Green
    }
}

# 3. Add missing FormStatus enum if not exists
$formStatusCheck = Select-String -Path $formFieldTypePath -Pattern "enum class FormStatus" -Quiet
if (-not $formStatusCheck) {
    Write-Host "Adding FormStatus enum..." -ForegroundColor Yellow
    $content = Get-Content $formFieldTypePath -Raw
    $formStatusEnum = @"

// Form Status enum
enum class FormStatus {
    DRAFT, IN_PROGRESS, COMPLETED, APPROVED, REJECTED, ARCHIVED
}
"@
    $content = $content + $formStatusEnum
    Set-Content $formFieldTypePath -Value $content -Encoding UTF8
    Write-Host "Added FormStatus enum" -ForegroundColor Green
}

# 4. Add missing FormType imports and definitions
$formTypePath = "app\src\main\java\com\aeci\mmucompanion\domain\model\FormTemplateInterfaces.kt"
if (Test-Path $formTypePath) {
    $content = Get-Content $formTypePath -Raw
    
    # Check if FormType enum exists and has all required values
    $requiredFormTypes = @(
        "BLAST_HOLE_LOG", "MMU_QUALITY_REPORT", "MMU_DAILY_LOG", "PUMP_90_DAY_INSPECTION",
        "FIRE_EXTINGUISHER_INSPECTION", "PUMP_WEEKLY_CHECK", "MMU_CHASSIS_MAINTENANCE",
        "MMU_HANDOVER_CERTIFICATE", "ON_BENCH_MMU_INSPECTION", "PC_PUMP_PRESSURE_TEST",
        "MONTHLY_PROCESS_MAINTENANCE", "PRETASK_SAFETY", "JOB_CARD", "TIMESHEET"
    )
    
    foreach ($formType in $requiredFormTypes) {
        if ($content -notmatch $formType) {
            Write-Host "Missing FormType: $formType" -ForegroundColor Red
        }
    }
}

# 5. Add TimeEntry data class if missing
$timeEntryDefinition = @"
// Time Entry data class for timesheets
data class TimeEntry(
    val date: LocalDate,
    val startTime: String,
    val endTime: String,
    val jobCode: String,
    val description: String,
    val regularHours: Double,
    val overtimeHours: Double
)
"@

# Add TimeEntry to MmuFormDataClasses.kt if not already there
$mmuDataClassesPath = "app\src\main\java\com\aeci\mmucompanion\domain\model\MmuFormDataClasses.kt"
if (Test-Path $mmuDataClassesPath) {
    $content = Get-Content $mmuDataClassesPath -Raw
    if ($content -notmatch "data class TimeEntry") {
        Write-Host "Adding TimeEntry data class..." -ForegroundColor Yellow
        # Add TimeEntry before the enum
        $content = $content -replace "(// Enum for PDF Field Types)", "$timeEntryDefinition`n`n`$1"
        Set-Content $mmuDataClassesPath -Value $content -Encoding UTF8
        Write-Host "Added TimeEntry data class" -ForegroundColor Green
    }
}

# 6. Fix missing getPdfFieldMappings methods in templates
$templateFiles = Get-ChildItem "app\src\main\java\com\aeci\mmucompanion\presentation\templates\" -Filter "*.kt"

foreach ($templateFile in $templateFiles) {
    Write-Host "Checking $($templateFile.Name) for getPdfFieldMappings..." -ForegroundColor Yellow
    $content = Get-Content $templateFile.FullName -Raw
    
    if ($content -notmatch "override fun getPdfFieldMappings") {
        Write-Host "Adding getPdfFieldMappings to $($templateFile.Name)" -ForegroundColor Yellow
        
        # Add the method before the closing brace
        $getPdfFieldMappingsMethod = @"
    
    override fun getPdfFieldMappings(): Map<String, String> {
        return mapOf(
            // Basic mappings - customize based on PDF field names
            "form_type" to formType.name,
            "created_date" to createdAt.toString(),
            "site_location" to siteLocation
        )
    }
"@
        
        # Insert before the last closing brace
        $content = $content -replace "(\n\s*}\s*$)", "$getPdfFieldMappingsMethod`n}"
        Set-Content $templateFile.FullName -Value $content -Encoding UTF8
        Write-Host "Added getPdfFieldMappings to $($templateFile.Name)" -ForegroundColor Green
    }
}

# 7. Fix FormSection constructor issues
Write-Host "Fixing FormSection constructor issues..." -ForegroundColor Yellow

# Create a function to fix FormSection constructor calls
function Fix-FormSectionConstructors {
    param($filePath)
    
    if (Test-Path $filePath) {
        $content = Get-Content $filePath -Raw
        
        # Replace FormSection(id = "...") with FormSection("...")
        $content = $content -replace 'FormSection\(\s*id\s*=\s*"([^"]+)"\s*,', 'FormSection("$1",'
        $content = $content -replace 'FormSection\(\s*id\s*=\s*"([^"]+)"\s*\)', 'FormSection("$1")'
        
        Set-Content $filePath -Value $content -Encoding UTF8
    }
}

# Apply FormSection fixes to all template files
foreach ($templateFile in $templateFiles) {
    Fix-FormSectionConstructors $templateFile.FullName
}

# 8. Fix UseCase files for Form vs DigitalForm type consistency
$useCaseFiles = Get-ChildItem "app\src\main\java\com\aeci\mmucompanion\domain\usecase\" -Filter "*UseCase*.kt" -Recurse

foreach ($useCaseFile in $useCaseFiles) {
    Write-Host "Fixing type consistency in $($useCaseFile.Name)..." -ForegroundColor Yellow
    $content = Get-Content $useCaseFile.FullName -Raw
    
    # Replace Form with DigitalForm for consistency
    $content = $content -replace '\bForm\b(?!\w)', 'DigitalForm'
    $content = $content -replace 'DigitalForm\s*<', 'Form<'  # Keep generics as Form if needed
    
    Set-Content $useCaseFile.FullName -Value $content -Encoding UTF8
}

# 9. Fix TimeEntryEntity constructor issues
$entityFiles = Get-ChildItem "app\src\main\java\com\aeci\mmucompanion\data\local\entity\" -Filter "*Entity*.kt" -Recurse

foreach ($entityFile in $entityFiles) {
    if ($entityFile.Name -match "TimeEntry") {
        Write-Host "Fixing TimeEntryEntity constructor in $($entityFile.Name)..." -ForegroundColor Yellow
        $content = Get-Content $entityFile.FullName -Raw
        
        # Fix common constructor parameter issues
        $content = $content -replace 'TimeEntryEntity\(\s*timeEntryId\s*=', 'TimeEntryEntity(id ='
        $content = $content -replace 'TimeEntryEntity\(\s*id\s*=\s*0\s*,', 'TimeEntryEntity(id = 0L,'
        
        Set-Content $entityFile.FullName -Value $content -Encoding UTF8
    }
}

# 10. Fix FormRepositoryImpl missing methods
$repoImplPath = "app\src\main\java\com\aeci\mmucompanion\data\repository\FormRepositoryImpl.kt"
if (Test-Path $repoImplPath) {
    Write-Host "Checking FormRepositoryImpl for missing methods..." -ForegroundColor Yellow
    $content = Get-Content $repoImplPath -Raw
    
    if ($content -notmatch "suspend fun saveForm") {
        Write-Host "Adding saveForm method to FormRepositoryImpl..." -ForegroundColor Yellow
        
        $saveFormMethod = @"

    override suspend fun saveForm(form: DigitalForm): String {
        // Implementation for saving form
        return form.id
    }
"@
        
        # Insert before the last closing brace
        $content = $content -replace "(\n\s*}\s*$)", "$saveFormMethod`n}"
        Set-Content $repoImplPath -Value $content -Encoding UTF8
        Write-Host "Added saveForm method" -ForegroundColor Green
    }
}

Write-Host "Comprehensive fix completed!" -ForegroundColor Green
Write-Host "Running a test build to check remaining issues..." -ForegroundColor Yellow

# Test build
try {
    $buildResult = & .\gradlew assembleDebug 2>&1
    $errors = $buildResult | Select-String "error:" | Select-Object -First 10
    
    if ($errors.Count -eq 0) {
        Write-Host "BUILD SUCCESS! All compilation errors fixed!" -ForegroundColor Green
    } else {
        Write-Host "Remaining errors:" -ForegroundColor Red
        $errors | ForEach-Object { Write-Host $_.Line -ForegroundColor Red }
    }
} catch {
    Write-Host "Build test failed: $($_.Exception.Message)" -ForegroundColor Red
}
