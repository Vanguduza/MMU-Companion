#!/usr/bin/env pwsh

Write-Host "Fixing all compilation errors systematically..." -ForegroundColor Green

# Fix 1: Replace TEXTAREA with MULTILINE_TEXT in all template files
Write-Host "`n1. Fixing FormFieldType.TEXTAREA references..." -ForegroundColor Yellow

$templateFiles = Get-ChildItem -Path "app\src\main\java\com\aeci\mmucompanion\domain\model\forms" -Filter "*.kt" -Recurse
foreach ($file in $templateFiles) {
    $content = Get-Content $file.FullName -Raw
    if ($content -match "FormFieldType\.TEXTAREA") {
        Write-Host "Fixing TEXTAREA in $($file.Name)..."
        $content = $content -replace "FormFieldType\.TEXTAREA", "FormFieldType.MULTILINE_TEXT"
        Set-Content $file.FullName -Value $content -NoNewline
    }
}

# Fix 2: Add missing templateId property to all template classes
Write-Host "`n2. Adding missing templateId property to template classes..." -ForegroundColor Yellow

foreach ($file in $templateFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Check if the file has a class implementing DigitalFormTemplate but missing templateId
    if ($content -match "class \w+.*:\s*DigitalFormTemplate" -and $content -match "override val id\s*=" -and $content -notmatch "override val templateId\s*=") {
        Write-Host "Adding templateId to $($file.Name)..."
        
        # Add templateId after the id property
        $content = $content -replace "(override val id\s*=\s*[`"']([^`"']+)[`"'])", "`$1`n    override val templateId = `"`$2`""
        
        # Also need to change name to title and add version
        if ($content -notmatch "override val title\s*=") {
            $content = $content -replace "(override val name\s*=)", "override val title ="
        }
        
        if ($content -notmatch "override val version\s*=") {
            $content = $content -replace "(override val title\s*=\s*[`"']([^`"']+)[`"'])", "`$1`n    override val version = `"1.0`""
        }
        
        Set-Content $file.FullName -Value $content -NoNewline
    }
}

# Fix 3: Fix ValidationRule parameter names
Write-Host "`n3. Fixing ValidationRule parameter names..." -ForegroundColor Yellow

foreach ($file in $templateFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Fix ValidationRule constructor calls
    $content = $content -replace "ValidationRule\s*\(\s*field\s*=", "ValidationRule(fieldName ="
    $content = $content -replace "ValidationRule\s*\(\s*rule\s*=", "ValidationRule(ruleName ="
    $content = $content -replace "ValidationRule\s*\(\s*message\s*=", "ValidationRule(errorMessage ="
    $content = $content -replace "(\w+)\s*=\s*ValidationRule\s*\(\s*([`"'][^`"']+[`"']),\s*([`"'][^`"']+[`"']),\s*([`"'][^`"']+[`"'])\s*\)", "`$1 = ValidationRule(fieldName = `$2, ruleName = `$3, expression = `$4, errorMessage = `$4)"
    
    Set-Content $file.FullName -Value $content -NoNewline
}

# Fix 4: Fix FormRelationship parameter names
Write-Host "`n4. Fixing FormRelationship parameter names..." -ForegroundColor Yellow

foreach ($file in $templateFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Fix FormRelationship constructor calls
    $content = $content -replace "FormRelationship\s*\(\s*sourceForm\s*=", "FormRelationship(sourceFormType ="
    $content = $content -replace "FormRelationship\s*\(\s*targetForm\s*=", "FormRelationship(targetFormType ="
    $content = $content -replace "FormRelationship\s*\(\s*relationshipType\s*=", "FormRelationship(relationshipType ="
    
    Set-Content $file.FullName -Value $content -NoNewline
}

# Fix 5: Fix UseCase files - replace DigitalForm with Form
Write-Host "`n5. Fixing UseCase files - DigitalForm to Form type mismatches..." -ForegroundColor Yellow

$useCaseFiles = Get-ChildItem -Path "app\src\main\java\com\aeci\mmucompanion\domain\usecase" -Filter "*.kt" -Recurse
foreach ($file in $useCaseFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Replace DigitalForm with Form where appropriate
    $content = $content -replace "DigitalForm\s*>", "Form>"
    $content = $content -replace "DigitalForm\s*\)", "Form)"
    $content = $content -replace "DigitalForm\s*,", "Form,"
    $content = $content -replace ": DigitalForm", ": Form"
    
    Set-Content $file.FullName -Value $content -NoNewline
}

# Fix 6: Fix service files
Write-Host "`n6. Fixing service files..." -ForegroundColor Yellow

$serviceFiles = Get-ChildItem -Path "app\src\main\java\com\aeci\mmucompanion" -Filter "*Service.kt" -Recurse
foreach ($file in $serviceFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Replace DigitalForm with Form where appropriate
    $content = $content -replace "DigitalForm\s*>", "Form>"
    $content = $content -replace "DigitalForm\s*\)", "Form)"
    $content = $content -replace "DigitalForm\s*,", "Form,"
    $content = $content -replace ": DigitalForm", ": Form"
    
    Set-Content $file.FullName -Value $content -NoNewline
}

Write-Host "`nAll compilation error fixes applied!" -ForegroundColor Green
Write-Host "Running test compilation to verify fixes..." -ForegroundColor Yellow

# Test compilation
& .\gradlew compileDebugKotlin
