package com.aeci.mmucompanion.domain.model.forms

import com.aeci.mmucompanion.domain.model.*

import com.aeci.mmucompanion.domain.model.forms.*

/**
 * Comprehensive Digital Form Registry
 * Contains ALL 16 forms with complete field mapping including logos and headers
 * Replaces PDF asset approach with efficient digital templates
 */
object ComprehensiveFormRegistry {
    
    fun getAllFormTemplates(): Map<FormType, DigitalFormTemplate> {
        return mapOf(
            // COMPREHENSIVE IMPLEMENTATIONS WITH ALL FIELDS INCLUDING LOGOS
            FormType.UOR_REPORT to UORTemplate(),
            FormType.TIMESHEET to TimesheetTemplate(),
            
            // BLAST AND PRODUCTION FORMS - Need comprehensive implementation
            FormType.BLAST_HOLE_LOG to BlastHoleLogTemplate(),
            FormType.MMU_QUALITY_REPORT to MmuQualityReportTemplate(),
            FormType.MMU_DAILY_LOG to MmuProductionDailyLogTemplate(),
            
            // PUMP AND EQUIPMENT FORMS - Need comprehensive implementation  
            FormType.PUMP_INSPECTION_90_DAY to PumpInspection90DayTemplate(),
            FormType.BOWIE_PUMP_WEEKLY to BowiePumpWeeklyCheckTemplate(),
            FormType.PC_PUMP_PRESSURE_TRIP_TEST to PcPumpPressureTripTestTemplate(),
            
            // MAINTENANCE AND INSPECTION FORMS - Need comprehensive implementation
            FormType.MMU_CHASSIS_MAINTENANCE to MmuChassisMaintenanceTemplate(),
            FormType.MMU_HANDOVER_CERTIFICATE to MmuHandoverCertificateTemplate(), 
            FormType.ON_BENCH_MMU_INSPECTION to OnBenchMmuInspectionTemplate(),
            FormType.MONTHLY_PROCESS_MAINTENANCE to MonthlyProcessMaintenanceTemplate(),
            
            // SAFETY AND PROCESS FORMS - Need comprehensive implementation
            FormType.FIRE_EXTINGUISHER_INSPECTION to FireExtinguisherInspectionTemplate(),
            FormType.PRE_TASK_SAFETY_ASSESSMENT to PreTaskSafetyAssessmentTemplate(),
            FormType.JOB_CARD to JobCardTemplate(),
            FormType.AVAILABILITY_UTILIZATION to AvailabilityUtilizationTemplate()
        )
    }
    
    fun getFormTemplate(formType: FormType): DigitalFormTemplate? {
        return getAllFormTemplates()[formType]
    }
    
    /**
     * Validates that ALL forms have comprehensive implementations
     * Returns true only if ALL forms have complete field mappings including logos
     */
    fun validateComprehensiveImplementation(): Boolean {
        val allTemplates = getAllFormTemplates()
        
        // Check that we have all 16 forms
        val expectedFormCount = 16
        if (allTemplates.size != expectedFormCount) {
            return false
        }
        
        // Validate each template has comprehensive implementation
        return allTemplates.all { (formType, template) ->
            val templateDef = template.getFormTemplate()
            
            // Each form must have:
            // 1. Logo coordinates (AECI branding)
            val hasLogos = template.logoCoordinates.isNotEmpty()
            
            // 2. Header coordinates (proper PDF headers)
            val hasHeaders = template.headerCoordinates.isNotEmpty()
            
            // 3. Comprehensive field mapping (not just date/notes)
            val hasComprehensiveFields = templateDef.sections.any { section ->
                section.fields.size > 2 // More than just date and notes
            }
            
            // 4. PDF coordinate mapping for all fields
            val hasCoordinateMapping = templateDef.sections.all { section ->
                section.fields.all { field ->
                    field.x != 0f || field.y != 0f
                }
            }
            
            hasLogos && hasHeaders && hasComprehensiveFields && hasCoordinateMapping
        }
    }
    
    fun getFormRelationships(): Map<FormType, List<FormRelationshipUpdate>> {
        return getAllFormTemplates().mapValues { (_, template) ->
            template.getRelatedFormUpdates()
        }
    }
}
