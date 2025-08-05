package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.domain.model.forms.*
import com.aeci.mmucompanion.domain.repository.FormRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FormRelationshipManager @Inject constructor(
    private val formRepository: FormRepository
) {
    
    /**
     * Processes DigitalForm relationships and updates related forms automatically
     */
    suspend fun saveForm(form: DigitalForm): String {
        // Get the template for this DigitalForm type to access relationships
        val template = getTemplateForFormType(form.formType)
        val relationships = template?.getRelatedFormUpdates() ?: emptyList()
        
        relationships.forEach { relationship ->
            updateRelatedForms(form, relationship)
        }
        
        // Save the form and return its ID
        val result = formRepository.saveForm(form)
        return result.getOrElse { "error" }
    }
    
    private fun getTemplateForFormType(formType: FormType): DigitalFormTemplate? {
        return when (formType) {
            FormType.FIRE_EXTINGUISHER_INSPECTION -> FireExtinguisherInspectionTemplate()
            FormType.MMU_QUALITY_REPORT -> MmuQualityReportTemplate() 
            FormType.MMU_DAILY_LOG -> MmuProductionDailyLogTemplate()
            FormType.BLAST_HOLE_LOG -> BlastHoleLogTemplate()
            // Add other DigitalForm types as needed
            else -> null
        }
    }
    
    private suspend fun updateRelatedForms(
        sourceForm: DigitalForm,
        relationship: FormRelationshipUpdate
    ) {
        when (relationship.targetFormType) {
            FormType.MMU_QUALITY_REPORT -> {
                updateMmuQualityReport(sourceForm, relationship)
            }
            FormType.MMU_DAILY_LOG -> {
                updateMmuProductionDailyLog(sourceForm, relationship)
            }
            FormType.BLAST_HOLE_LOG -> {
                updateBlastHoleLog(sourceForm, relationship)
            }
            else -> {
                // Handle other DigitalForm types as needed
            }
        }
    }
    
    private suspend fun updateMmuQualityReport(
        sourceForm: DigitalForm,
        relationship: FormRelationshipUpdate
    ) {
        // Find existing MMU Quality Report for the same site and date
        val existingForms = formRepository.getFormsBySiteAndType(
            sourceForm.siteId,
            FormType.MMU_QUALITY_REPORT
        )
        
        // Find today's report or create a new one
        val targetForm = existingForms.find { form ->
            when (form) {
                is MmuQualityReportForm -> {
                    form.reportDate == java.time.LocalDate.now()
                }
                else -> false
            }
        } as? MmuQualityReportForm
        
        if (targetForm != null) {
            val updatedForm = if (relationship.fieldMappings.containsKey("emulsion_used_today")) {
                targetForm.copy(
                    emulsionUsedToday = relationship.fieldMappings["emulsion_used_today"]?.toDoubleOrNull() ?: targetForm.emulsionUsedToday,
                    updatedAt = java.time.LocalDateTime.now()
                )
            } else {
                targetForm
            }
            // Convert to DigitalForm for repository
            val digitalForm = DigitalForm(
                id = updatedForm.id,
                formType = updatedForm.formType,
                createdAt = updatedForm.createdAt,
                updatedAt = updatedForm.updatedAt,
                createdBy = updatedForm.createdBy,
                status = updatedForm.status,
                siteId = updatedForm.siteId,
                siteLocation = updatedForm.siteLocation,
                data = mapOf(
                    "emulsionUsedToday" to updatedForm.emulsionUsedToday,
                    "reportNumber" to updatedForm.reportNumber,
                    "reportDate" to updatedForm.reportDate.toString(),
                    "shiftPattern" to updatedForm.shiftPattern,
                    "qualityTechnician" to updatedForm.qualityTechnician
                )
            )
            formRepository.updateForm(digitalForm)
        } else {
            // Create new quality report with the updated field
            createNewQualityReportWithUpdate(sourceForm, relationship)
        }
    }
    
    private suspend fun updateMmuProductionDailyLog(
        sourceForm: DigitalForm,
        relationship: FormRelationshipUpdate
    ) {
        val existingForms = formRepository.getFormsBySiteAndType(
            sourceForm.siteId,
            FormType.MMU_DAILY_LOG
        )
        
        val targetForm = existingForms.find { form ->
            when (form) {
                is MmuProductionDailyLogForm -> {
                    form.logDate == java.time.LocalDate.now()
                }
                else -> false
            }
        } as? MmuProductionDailyLogForm
        
        if (targetForm != null) {
            val updatedForm = if (relationship.fieldMappings.containsKey("total_emulsion_consumed")) {
                targetForm.copy(
                    totalEmulsionConsumed = relationship.fieldMappings["total_emulsion_consumed"]?.toDoubleOrNull() ?: targetForm.totalEmulsionConsumed,
                    updatedAt = java.time.LocalDateTime.now()
                )
            } else if (relationship.fieldMappings.containsKey("quality_grade_achieved")) {
                targetForm.copy(
                    qualityGradeAchieved = relationship.fieldMappings["quality_grade_achieved"] ?: targetForm.qualityGradeAchieved,
                    updatedAt = java.time.LocalDateTime.now()
                )
            } else {
                targetForm
            }
            // Convert to DigitalForm for repository
            val digitalForm = DigitalForm(
                id = updatedForm.id,
                formType = updatedForm.formType,
                createdAt = updatedForm.createdAt,
                updatedAt = updatedForm.updatedAt,
                createdBy = updatedForm.createdBy,
                status = updatedForm.status,
                siteId = updatedForm.siteId,
                siteLocation = updatedForm.siteLocation,
                data = mapOf(
                    "totalEmulsionConsumed" to updatedForm.totalEmulsionConsumed,
                    "qualityGradeAchieved" to updatedForm.qualityGradeAchieved,
                    "logDate" to updatedForm.logDate.toString(),
                    "operatorName" to updatedForm.operatorName,
                    "supervisorName" to updatedForm.supervisorName
                )
            )
            formRepository.updateForm(digitalForm)
        } else {
            createNewProductionLogWithUpdate(sourceForm, relationship)
        }
    }
    
    private suspend fun updateBlastHoleLog(
        sourceForm: DigitalForm,
        relationship: FormRelationshipUpdate
    ) {
        // Handle blast hole log updates if needed
    }
    
    private suspend fun createNewQualityReportWithUpdate(
        sourceForm: DigitalForm,
        relationship: FormRelationshipUpdate
    ) {
        val newQualityReport = MmuQualityReportForm(
            id = java.util.UUID.randomUUID().toString(),
            createdAt = java.time.LocalDateTime.now(),
            updatedAt = java.time.LocalDateTime.now(),
            createdBy = sourceForm.createdBy,
            siteId = sourceForm.siteId,
            siteLocation = sourceForm.siteLocation,
            status = FormStatus.DRAFT,
            reportDate = java.time.LocalDate.now(),
            reportNumber = generateReportNumber(),
            shiftPattern = "",
            shiftSupervisor = "",
            qualityTechnician = "",
            qualityGrade = "",
            targetEmulsionProduction = 0.0,
            actualEmulsionProduction = 0.0,
            emulsionUsedToday = if (relationship.fieldMappings.containsKey("emulsion_used_today")) {
                relationship.fieldMappings["emulsion_used_today"]?.toDoubleOrNull() ?: 0.0
            } else 0.0,
            viscosityReading = 0.0,
            temperatureReading = 0.0,
            phLevel = 0.0,
            densityTests = emptyList(),
            qualityIssues = emptyList(),
            recommendations = "",
            approvedBy = ""
        )
        
        // Convert to DigitalForm for repository
        val digitalForm = DigitalForm(
            id = newQualityReport.id,
            formType = newQualityReport.formType,
            createdAt = newQualityReport.createdAt,
            updatedAt = newQualityReport.updatedAt,
            createdBy = newQualityReport.createdBy,
            status = newQualityReport.status,
            siteId = newQualityReport.siteId,
            siteLocation = newQualityReport.siteLocation,
            data = mapOf(
                "emulsionUsedToday" to newQualityReport.emulsionUsedToday,
                "reportNumber" to newQualityReport.reportNumber,
                "reportDate" to newQualityReport.reportDate.toString(),
                "shiftPattern" to newQualityReport.shiftPattern,
                "qualityTechnician" to newQualityReport.qualityTechnician
            )
        )
        formRepository.saveForm(digitalForm)
    }
    
    private suspend fun createNewProductionLogWithUpdate(
        sourceForm: DigitalForm,
        relationship: FormRelationshipUpdate
    ) {
        val newProductionLog = MmuProductionDailyLogForm(
            id = java.util.UUID.randomUUID().toString(),
            createdAt = java.time.LocalDateTime.now(),
            updatedAt = java.time.LocalDateTime.now(),
            createdBy = sourceForm.createdBy,
            siteId = sourceForm.siteId,
            siteLocation = sourceForm.siteLocation,
            status = FormStatus.DRAFT,
            logDate = java.time.LocalDate.now(),
            shiftDetails = "",
            operatorName = "",
            supervisorName = "",
            startTime = "",
            endTime = "",
            totalOperatingHours = 0.0,
            totalEmulsionConsumed = if (relationship.fieldMappings.containsKey("total_emulsion_consumed")) {
                relationship.fieldMappings["total_emulsion_consumed"]?.toDoubleOrNull() ?: 0.0
            } else 0.0,
            qualityGradeAchieved = if (relationship.fieldMappings.containsKey("quality_grade_achieved")) {
                relationship.fieldMappings["quality_grade_achieved"] ?: ""
            } else "",
            productionTarget = 0.0,
            actualProduction = 0.0,
            operatingTemperature = 0.0,
            equipmentCondition = "",
            maintenancePerformed = emptyList(),
            safetyObservations = emptyList(),
            operatorComments = "",
            supervisorComments = ""
        )
        
        // Convert to DigitalForm for repository
        val digitalForm = DigitalForm(
            id = newProductionLog.id,
            formType = newProductionLog.formType,
            createdAt = newProductionLog.createdAt,
            updatedAt = newProductionLog.updatedAt,
            createdBy = newProductionLog.createdBy,
            status = newProductionLog.status,
            siteId = newProductionLog.siteId,
            siteLocation = newProductionLog.siteLocation,
            data = mapOf(
                "totalEmulsionConsumed" to newProductionLog.totalEmulsionConsumed,
                "qualityGradeAchieved" to newProductionLog.qualityGradeAchieved,
                "logDate" to newProductionLog.logDate.toString(),
                "operatorName" to newProductionLog.operatorName,
                "supervisorName" to newProductionLog.supervisorName
            )
        )
        formRepository.saveForm(digitalForm)
    }
    
    private fun generateReportNumber(): String {
        val date = java.time.LocalDate.now()
        val timestamp = System.currentTimeMillis()
        return "QR-${date.year}${date.monthValue.toString().padStart(2, '0')}${date.dayOfMonth.toString().padStart(2, '0')}-${timestamp % 10000}"
    }
    
    /**
     * Gets aggregated data for reports that depend on multiple forms
     */
    suspend fun getAggregatedFormData(siteId: String, date: java.time.LocalDate): AggregatedFormData {
        val blastHoleForms = formRepository.getFormsBySiteAndDateRange(
            siteId, FormType.BLAST_HOLE_LOG, date, date
        ).filterIsInstance<BlastHoleLogForm>()
        
        val qualityReports = formRepository.getFormsBySiteAndDateRange(
            siteId, FormType.MMU_QUALITY_REPORT, date, date
        ).filterIsInstance<MmuQualityReportForm>()
        
        val productionLogs = formRepository.getFormsBySiteAndDateRange(
            siteId, FormType.MMU_DAILY_LOG, date, date
        ).filterIsInstance<MmuProductionDailyLogForm>()
        
        return AggregatedFormData(
            date = date,
            siteId = siteId,
            totalEmulsionUsed = blastHoleForms.sumOf { it.totalEmulsionUsed },
            totalBlasts = blastHoleForms.size,
            averageQualityGrade = qualityReports.mapNotNull { it.qualityGrade.toDoubleOrNull() }.average(),
            totalProductionHours = productionLogs.sumOf { it.totalOperatingHours },
            blastHoleLogs = blastHoleForms,
            qualityReports = qualityReports,
            productionLogs = productionLogs
        )
    }
}

data class AggregatedFormData(
    val date: java.time.LocalDate,
    val siteId: String,
    val totalEmulsionUsed: Double,
    val totalBlasts: Int,
    val averageQualityGrade: Double,
    val totalProductionHours: Double,
    val blastHoleLogs: List<BlastHoleLogForm>,
    val qualityReports: List<MmuQualityReportForm>,
    val productionLogs: List<MmuProductionDailyLogForm>
)


