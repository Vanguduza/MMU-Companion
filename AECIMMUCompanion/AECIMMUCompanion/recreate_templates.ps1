# PowerShell script to recreate all corrupted template files with proper syntax

Write-Host "Recreating all template files with correct syntax..."

# Function to create a clean template file structure
function New-TemplateFile {
    param(
        [string]$FileName,
        [string]$ClassName,
        [string]$FormId,
        [string]$FormName,
        [string]$Description,
        [string]$FormType,
        [string]$PdfFileName
    )
    
    $content = @"
package com.aeci.mmucompanion.domain.model.forms

import com.aeci.mmucompanion.domain.model.*

/**
 * Template for $FormName
 * $Description
 */
class $ClassName : DigitalFormTemplate {
    
    override val id = "$FormId"
    override val name = "$FormName"
    override val description = "$Description"
    override val formType = FormType.$FormType
    override val pdfFileName = "$PdfFileName"
    
    override val logoCoordinates = listOf(
        LogoCoordinate(
            logoType = "aeci_main_logo",
            x = 50f, y = 750f, width = 120f, height = 60f
        ),
        LogoCoordinate(
            logoType = "mining_explosives_logo", 
            x = 450f, y = 750f, width = 140f, height = 60f
        )
    )
    
    override val staticTextCoordinates = listOf(
        StaticTextCoordinate(
            text = "$FormName".uppercase(),
            x = 200f, y = 720f, fontSize = 16f, fontWeight = "bold"
        )
    )
    
    override val headerCoordinates = mapOf(
        "title" to FormCoordinate(x = 200f, y = 720f, width = 200f, height = 20f),
        "form_number" to FormCoordinate(x = 450f, y = 720f, width = 100f, height = 15f)
    )

    override fun getFormTemplate(): FormDefinition {
        return FormDefinition(
            id = id,
            name = name,
            description = description,
            sections = listOf(
                FormSection(
                    id = "basic_info",
                    title = "Basic Information", 
                    fields = listOf(
                        FormField(
                            fieldName = "date",
                            fieldType = FormFieldType.DATE,
                            label = "Date",
                            isRequired = true,
                            x = 120f, y = 670f, width = 100f, height = 20f
                        ),
                        FormField(
                            fieldName = "operator_name",
                            fieldType = FormFieldType.TEXT,
                            label = "Operator Name",
                            isRequired = true,
                            x = 250f, y = 670f, width = 150f, height = 20f
                        )
                    )
                )
            )
        )
    }

    override val formRelationships = listOf<FormRelationship>()

    override fun getValidationRules(): List<ValidationRule> {
        return listOf(
            ValidationRule(
                field = "date",
                rule = "date <= TODAY",
                message = "Date cannot be in the future"
            )
        )
    }

    override fun getRelatedFormUpdates(): List<FormRelationshipUpdate> {
        return listOf()
    }
}
"@
    
    return $content
}

# Define all template files that need to be recreated
$templates = @(
    @{
        File = "BlastHoleLogTemplate.kt"
        Class = "BlastHoleLogTemplate"
        Id = "BLAST_HOLE_LOG"
        Name = "Blast Hole Log"
        Description = "Blast hole drilling and logging form"
        Type = "BLAST_HOLE_LOG"
        Pdf = "blast hole log.pdf"
    },
    @{
        File = "BowiePumpWeeklyCheckTemplate.kt"
        Class = "BowiePumpWeeklyCheckTemplate"
        Id = "BOWIE_PUMP_WEEKLY_CHECK"
        Name = "Bowie Pump Weekly Check"
        Description = "Weekly maintenance check for Bowie pumps"
        Type = "BOWIE_PUMP_WEEKLY_CHECK"
        Pdf = "Bowie Pump Weekly check list.pdf"
    },
    @{
        File = "FireExtinguisherInspectionTemplate.kt"
        Class = "FireExtinguisherInspectionTemplate"
        Id = "FIRE_EXTINGUISHER_INSPECTION"
        Name = "Fire Extinguisher Inspection"
        Description = "Fire extinguisher safety inspection checklist"
        Type = "FIRE_EXTINGUISHER_INSPECTION"
        Pdf = "FIRE EXTINGUISHER INSPECTION CHECKLIST.pdf"
    },
    @{
        File = "JobCardTemplate.kt"
        Class = "JobCardTemplate"
        Id = "JOB_CARD"
        Name = "Job Card"
        Description = "Work order and maintenance job card"
        Type = "JOB_CARD"
        Pdf = "job card.pdf"
    },
    @{
        File = "MmuChassisMaintenanceTemplate.kt"
        Class = "MmuChassisMaintenanceTemplate"
        Id = "MMU_CHASSIS_MAINTENANCE"
        Name = "MMU Chassis Maintenance"
        Description = "MMU chassis maintenance record"
        Type = "MMU_CHASSIS_MAINTENANCE"
        Pdf = "MMU CHASSIS MAINTENANCE RECORD.pdf"
    },
    @{
        File = "MmuHandoverCertificateTemplate.kt"
        Class = "MmuHandoverCertificateTemplate"
        Id = "MMU_HANDOVER_CERTIFICATE"
        Name = "MMU Handover Certificate"
        Description = "Equipment handover certification form"
        Type = "MMU_HANDOVER_CERTIFICATE"
        Pdf = "MMU HANDOVER CERTIFICATE.pdf"
    },
    @{
        File = "MmuProductionDailyLogTemplate.kt"
        Class = "MmuProductionDailyLogTemplate"
        Id = "MMU_PRODUCTION_DAILY_LOG"
        Name = "MMU Production Daily Log"
        Description = "Daily production logging and tracking"
        Type = "MMU_PRODUCTION_DAILY_LOG"
        Pdf = "mmu production daily log.pdf"
    },
    @{
        File = "MmuQualityReportTemplate.kt"
        Class = "MmuQualityReportTemplate"
        Id = "MMU_QUALITY_REPORT"
        Name = "MMU Quality Report"
        Description = "Quality control and assurance reporting"
        Type = "MMU_QUALITY_REPORT"
        Pdf = "mmu quality report.pdf"
    },
    @{
        File = "MonthlyProcessMaintenanceTemplate.kt"
        Class = "MonthlyProcessMaintenanceTemplate"
        Id = "MONTHLY_PROCESS_MAINTENANCE"
        Name = "Monthly Process Maintenance"
        Description = "Monthly process maintenance record"
        Type = "MONTHLY_PROCESS_MAINTENANCE"
        Pdf = "MONTHLY PROCESS MAINTENANCE RECORD.pdf"
    },
    @{
        File = "OnBenchMmuInspectionTemplate.kt"
        Class = "OnBenchMmuInspectionTemplate"
        Id = "ON_BENCH_MMU_INSPECTION"
        Name = "On Bench MMU Inspection"
        Description = "On-bench MMU inspection checklist"
        Type = "ON_BENCH_MMU_INSPECTION"
        Pdf = "ON BENCH MMU INSPECTION.pdf"
    },
    @{
        File = "PcPumpPressureTripTestTemplate.kt"
        Class = "PcPumpPressureTripTestTemplate"
        Id = "PC_PUMP_PRESSURE_TRIP_TEST"
        Name = "PC Pump Pressure Trip Test"
        Description = "PC pump high/low pressure trip testing"
        Type = "PC_PUMP_PRESSURE_TRIP_TEST"
        Pdf = "PC PUMP HIGH LOW PRESSURE TRIP TEST.pdf"
    },
    @{
        File = "PreTaskTemplate.kt"
        Class = "PreTaskTemplate"
        Id = "PRE_TASK"
        Name = "Pre-Task Safety Check"
        Description = "Pre-task safety and risk assessment"
        Type = "PRE_TASK"
        Pdf = "pre-task.pdf"
    },
    @{
        File = "PumpInspection90DayTemplate.kt"
        Class = "PumpInspection90DayTemplate"
        Id = "PUMP_INSPECTION_90_DAY"
        Name = "90 Day Pump System Inspection"
        Description = "Quarterly pump system inspection checklist"
        Type = "PUMP_INSPECTION_90_DAY"
        Pdf = "90 DAY PUMP SYSTEM INSPECTION CHECKLIST.pdf"
    },
    @{
        File = "TimesheetTemplate.kt"
        Class = "TimesheetTemplate"
        Id = "TIMESHEET"
        Name = "Timesheet"
        Description = "Employee time tracking and recording"
        Type = "TIMESHEET"
        Pdf = "Copy of Timesheet(1).pdf"
    },
    @{
        File = "UORTemplate.kt"
        Class = "UORTemplate"
        Id = "UOR"
        Name = "Unusual Occurrence Report"
        Description = "Unusual occurrence incident reporting"
        Type = "UOR"
        Pdf = "uor.pdf"
    }
)

# Create each template file
foreach ($template in $templates) {
    $filePath = "app\src\main\java\com\aeci\mmucompanion\domain\model\forms\$($template.File)"
    Write-Host "Creating $($template.File)..."
    
    $content = New-TemplateFile -FileName $template.File -ClassName $template.Class -FormId $template.Id -FormName $template.Name -Description $template.Description -FormType $template.Type -PdfFileName $template.Pdf
    
    $content | Set-Content -Path $filePath -Encoding UTF8
    Write-Host "Created $($template.File)"
}

Write-Host "All template files have been recreated with proper syntax!"
Write-Host "You may need to customize the specific fields for each template as needed."
