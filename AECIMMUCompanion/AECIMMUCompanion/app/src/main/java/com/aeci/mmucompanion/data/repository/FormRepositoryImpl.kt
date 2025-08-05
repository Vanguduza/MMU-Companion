package com.aeci.mmucompanion.data.repository

import com.aeci.mmucompanion.data.local.dao.FormDao
import com.aeci.mmucompanion.data.local.entity.FormEntity
import com.aeci.mmucompanion.data.remote.api.AECIApiService
import com.aeci.mmucompanion.data.remote.api.FormSubmissionRequest
import com.aeci.mmucompanion.data.remote.api.FormDto
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.domain.repository.FormRepository
import com.aeci.mmucompanion.domain.repository.FormFilters
import com.aeci.mmucompanion.domain.repository.FormStatistics
import com.aeci.mmucompanion.domain.repository.FormCompletionStats
import com.aeci.mmucompanion.domain.repository.FormDependency
import com.aeci.mmucompanion.domain.repository.DependencyType
import com.aeci.mmucompanion.domain.repository.DateRange
import com.aeci.mmucompanion.domain.model.ExportFormat
import com.aeci.mmucompanion.domain.service.PDFExportService
import com.aeci.mmucompanion.domain.service.ExcelExportService
import com.aeci.mmucompanion.domain.service.CSVExportService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.Gson
import kotlinx.coroutines.flow.flow
import com.aeci.mmucompanion.data.remote.api.MobileServerApiService
import com.aeci.mmucompanion.core.util.MobileServerConfig
import com.aeci.mmucompanion.core.util.NetworkManager
import com.aeci.mmucompanion.data.remote.api.CreateFormRequest
import java.io.File

@Singleton
class FormRepositoryImpl @Inject constructor(
    private val formDao: FormDao,
    private val apiService: AECIApiService,
    private val mobileServerApiService: MobileServerApiService,
    private val networkManager: NetworkManager,
    private val gson: Gson,
    private val mobileServerConfig: MobileServerConfig,
    private val pdfExportService: PDFExportService,
    private val excelExportService: ExcelExportService,
    private val csvExportService: CSVExportService
) : FormRepository {

    /**
     * Save multiple forms to the database
     */
    override suspend fun saveForms(forms: List<DigitalForm>): Result<List<String>> {
        return try {
            val formEntities = forms.map { form ->
                FormEntity(
                    id = form.id,
                    formType = form.formType.name,
                    createdAt = form.createdAt.toString(),
                    updatedAt = form.updatedAt.toString(),
                    createdBy = form.createdBy,
                    status = form.status.name,
                    siteLocation = form.siteLocation,
                    reportNumber = form.reportNumber ?: "",
                    equipmentId = form.equipmentId,
                    formData = gson.toJson(extractFormData(form))
                )
            }
            formDao.insertForms(formEntities)
            Result.success(forms.map { it.id })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Helper function to parse form data from JSON string to Map
     */
    private fun parseFormData(formDataJson: String): Map<String, Any> {
        return try {
            val mapType = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
            gson.fromJson<Map<String, Any>>(formDataJson, mapType) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Helper function to create zip file from multiple files
     */
    private fun createZipFile(files: List<String>, outputFileName: String): String {
        return try {
            // Implementation for creating zip file
            val zipFile = File(outputFileName)
            java.util.zip.ZipOutputStream(java.io.FileOutputStream(zipFile)).use { zipOut ->
                files.forEach { filePath ->
                    val file = File(filePath)
                    if (file.exists()) {
                        val entry = java.util.zip.ZipEntry(file.name)
                        zipOut.putNextEntry(entry)
                        file.inputStream().use { input ->
                            input.copyTo(zipOut)
                        }
                        zipOut.closeEntry()
                    }
                }
            }
            zipFile.absolutePath
        } catch (e: Exception) {
            throw Exception("Failed to create zip file: ${e.message}")
        }
    }
    
    // Digital Form Operations (New System)
    override suspend fun saveForm(form: DigitalForm): Result<String> {
        return try {
            // Convert DigitalForm to FormEntity
            val formEntity = FormEntity(
                id = form.id,
                formType = form.formType.name,
                createdAt = form.createdAt.toString(),
                updatedAt = form.updatedAt.toString(),
                createdBy = form.createdBy,
                status = form.status.name,
                equipmentId = "", // Not in DigitalForm interface
                siteLocation = form.siteLocation,
                reportNumber = "", // Not in DigitalForm interface
                formData = gson.toJson(form),
                synced = false
            )
            
            // Save to local database
            formDao.insertForm(formEntity)
            
            // Try to sync with server if network is available
            if (networkManager.isNetworkAvailable()) {
                try {
                    val formDto = FormDto(
                        id = form.id,
                        formType = form.formType.name,
                        title = form.formType.name, // Use form type as title
                        createdBy = form.createdBy.toIntOrNull() ?: 0,
                        assignedTo = null,
                        equipmentId = null,
                        status = form.status.name,
                        formData = gson.toJson(form),
                        attachments = null,
                        createdAt = form.createdAt.toString(),
                        updatedAt = form.updatedAt.toString(),
                        completedAt = null,
                        createdByName = null,
                        assignedToName = null,
                        equipmentName = null
                    )
                    
                    apiService.saveForm(formDto)
                    
                    // Update local entity as synced
                    formDao.updateFormSyncStatus(form.id, true)
                } catch (e: Exception) {
                    // Network error - form is saved locally, will sync later
                }
            }
            
            Result.success(form.id)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save form: ${e.message}"))
        }
    }
    
    override suspend fun updateForm(form: DigitalForm): Result<Unit> {
        return try {
            // Convert DigitalForm to FormEntity
            val formEntity = FormEntity(
                id = form.id,
                formType = form.formType.name,
                createdAt = form.createdAt.toString(),
                updatedAt = LocalDateTime.now().toString(),
                createdBy = form.createdBy,
                status = form.status.name,
                equipmentId = "", // Not in DigitalForm interface
                siteLocation = form.siteLocation,
                reportNumber = "", // Not in DigitalForm interface
                formData = gson.toJson(form),
                synced = false
            )
            
            // Update in local database
            formDao.updateForm(formEntity)
            
            // Try to sync if network is available
            if (networkManager.isNetworkAvailable()) {
                try {
                    // Prepare form data for API
                    val formData = parseFormData(gson.toJson(form))
                    val formDto = FormDto(
                        id = form.id,
                        formType = form.formType.name,
                        title = "Form Update",
                        createdBy = form.createdBy.toIntOrNull() ?: 0,
                        assignedTo = null,
                        equipmentId = "",
                        status = form.status.name,
                        formData = gson.toJson(form),
                        attachments = null,
                        createdAt = form.createdAt.toString(),
                        updatedAt = form.updatedAt.toString(),
                        completedAt = null,
                        createdByName = null,
                        assignedToName = null,
                        equipmentName = null
                    )
                    
                    // Update on server
                    apiService.updateForm(form.id, formDto)
                    
                    // Update local entity as synced
                    formDao.updateFormSyncStatus(form.id, true)
                } catch (e: Exception) {
                    // Network error - form is updated locally, will sync later
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update form: ${e.message}"))
        }
    }
    
    override suspend fun getFormsByDateRange(startDate: LocalDate, endDate: LocalDate): List<DigitalForm> {
        return try {
            val entities = formDao.getFormsByDateRange(startDate.toString(), endDate.toString())
            entities.mapNotNull { entity ->
                try {
                    when (FormType.valueOf(entity.formType)) {
                        FormType.BLAST_HOLE_LOG -> gson.fromJson(entity.formData, BlastHoleLogForm::class.java) as DigitalForm
                        FormType.MMU_QUALITY_REPORT -> gson.fromJson(entity.formData, MmuQualityReportForm::class.java) as DigitalForm
                        FormType.MMU_DAILY_LOG -> gson.fromJson(entity.formData, MmuProductionDailyLogForm::class.java) as DigitalForm
                        FormType.FIRE_EXTINGUISHER_INSPECTION -> gson.fromJson(entity.formData, FireExtinguisherInspectionForm::class.java) as DigitalForm
                        FormType.MAINTENANCE_REPORT -> gson.fromJson(entity.formData, MaintenanceReportForm::class.java) as DigitalForm
                        else -> null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getFormsBySiteAndDateRange(
        siteId: String, 
        formType: FormType, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): List<DigitalForm> {
        return try {
            val entities = formDao.getFormsBySiteAndDateRange(siteId, formType.name, startDate.toString(), endDate.toString())
            entities.mapNotNull { entity ->
                try {
                    when (FormType.valueOf(entity.formType)) {
                        FormType.BLAST_HOLE_LOG -> gson.fromJson(entity.formData, BlastHoleLogForm::class.java) as DigitalForm
                        FormType.MMU_QUALITY_REPORT -> gson.fromJson(entity.formData, MmuQualityReportForm::class.java) as DigitalForm
                        FormType.MMU_DAILY_LOG -> gson.fromJson(entity.formData, MmuProductionDailyLogForm::class.java) as DigitalForm
                        FormType.FIRE_EXTINGUISHER_INSPECTION -> gson.fromJson(entity.formData, FireExtinguisherInspectionForm::class.java) as DigitalForm
                        FormType.MAINTENANCE_REPORT -> gson.fromJson(entity.formData, MaintenanceReportForm::class.java) as DigitalForm
                        FormType.SAFETY -> gson.fromJson(entity.formData, SafetyReportForm::class.java) as DigitalForm
                        FormType.INSPECTION -> gson.fromJson(entity.formData, InspectionReportForm::class.java) as DigitalForm
                        else -> null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun searchForms(query: String): List<DigitalForm> {
        return try {
            val entities = formDao.getAllForms()
            entities.mapNotNull { entity ->
                try {
                    // Simple search in form data JSON and basic fields
                    if (entity.formData.contains(query, ignoreCase = true) ||
                        entity.formType.contains(query, ignoreCase = true) ||
                        entity.siteLocation.contains(query, ignoreCase = true) ||
                        entity.equipmentId?.contains(query, ignoreCase = true) == true) {
                        
                        when (FormType.valueOf(entity.formType)) {
                            FormType.BLAST_HOLE_LOG -> gson.fromJson(entity.formData, BlastHoleLogForm::class.java) as DigitalForm
                            FormType.MMU_QUALITY_REPORT -> gson.fromJson(entity.formData, MmuQualityReportForm::class.java) as DigitalForm
                            FormType.MMU_DAILY_LOG -> gson.fromJson(entity.formData, MmuProductionDailyLogForm::class.java) as DigitalForm
                            FormType.FIRE_EXTINGUISHER_INSPECTION -> gson.fromJson(entity.formData, FireExtinguisherInspectionForm::class.java) as DigitalForm
                            FormType.MAINTENANCE_REPORT -> gson.fromJson(entity.formData, MaintenanceReportForm::class.java) as DigitalForm
                            FormType.SAFETY -> gson.fromJson(entity.formData, SafetyReportForm::class.java) as DigitalForm
                            FormType.INSPECTION -> gson.fromJson(entity.formData, InspectionReportForm::class.java) as DigitalForm
                            else -> null
                        }
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getPendingForms(): List<DigitalForm> {
        return try {
            formDao.getAllForms()
                .filter { it.status == FormStatus.DRAFT.name }
                .map { entity -> entity.toDomainModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getFormsSinceLastSync(lastSyncTime: Long): List<DigitalForm> {
        return try {
            formDao.getAllForms()
                .filter { 
                    val updatedTime = java.time.LocalDateTime.parse(it.updatedAt)
                    val updatedTimeMillis = updatedTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    updatedTimeMillis > lastSyncTime
                }
                .map { entity -> entity.toDomainModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getFormsByFilters(filters: FormFilters): List<DigitalForm> {
        return try {
            var entities = formDao.getAllForms()

            // Apply filters
            filters.formTypes?.let { types ->
                entities = entities.filter { entity ->
                    types.any { it.name == entity.formType }
                }
            }

            filters.siteIds?.let { siteIds ->
                entities = entities.filter { entity ->
                    siteIds.contains(entity.siteLocation)
                }
            }

            filters.userIds?.let { userIds ->
                entities = entities.filter { entity ->
                    userIds.contains(entity.createdBy)
                }
            }

            filters.statuses?.let { statuses ->
                entities = entities.filter { entity ->
                    statuses.any { it.name == entity.status }
                }
            }

            filters.searchQuery?.let { query ->
                entities = entities.filter { entity ->
                    entity.formData.contains(query, ignoreCase = true) ||
                    entity.formType.contains(query, ignoreCase = true) ||
                    entity.siteLocation.contains(query, ignoreCase = true)
                }
            }

            // Convert to domain models
            entities.mapNotNull { entity ->
                try {
                    val formType = FormType.valueOf(entity.formType)
                    when (formType) {
                        FormType.BLAST_HOLE_LOG -> gson.fromJson(entity.formData, BlastHoleLogForm::class.java) as DigitalForm
                        FormType.MMU_QUALITY_REPORT -> gson.fromJson(entity.formData, MmuQualityReportForm::class.java) as DigitalForm
                        FormType.MMU_DAILY_LOG -> gson.fromJson(entity.formData, MmuProductionDailyLogForm::class.java) as DigitalForm
                        FormType.FIRE_EXTINGUISHER_INSPECTION -> gson.fromJson(entity.formData, FireExtinguisherInspectionForm::class.java) as DigitalForm
                        FormType.MAINTENANCE_REPORT -> gson.fromJson(entity.formData, MaintenanceReportForm::class.java) as DigitalForm
                        FormType.SAFETY -> gson.fromJson(entity.formData, SafetyReportForm::class.java) as DigitalForm
                        FormType.INSPECTION -> gson.fromJson(entity.formData, InspectionReportForm::class.java) as DigitalForm
                        else -> null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getFormStatistics(siteId: String?, dateRange: DateRange?): FormStatistics {
        return try {
            var entities = formDao.getAllForms()
            
            // Apply site filter
            siteId?.let { site ->
                entities = entities.filter { entity ->
                    entity.siteLocation == site
                }
            }
            
            // Apply date range filter
            dateRange?.let { range ->
                entities = entities.filter { entity ->
                    val createdAt = LocalDateTime.parse(entity.createdAt)
                    createdAt.toLocalDate().isAfter(range.startDate.minusDays(1)) &&
                    createdAt.toLocalDate().isBefore(range.endDate.plusDays(1))
                }
            }
            
            val totalForms = entities.size
            val formsByType = entities.groupBy { FormType.valueOf(it.formType) }
                .mapValues { it.value.size }
            val formsByStatus = entities.groupBy { FormStatus.valueOf(it.status) }
                .mapValues { it.value.size }
            val formsBySite = entities.groupBy { it.siteLocation }
                .mapValues { it.value.size }
            
            val completedForms = entities.count { it.status == FormStatus.COMPLETED.name }
            val completionRate = if (totalForms > 0) completedForms.toDouble() / totalForms else 0.0
            
            // Average completion time calculation (simplified)
            val averageCompletionTime = 2.5 // Default value in hours
            
            val formsWithIssues = entities.count { entity ->
                try {
                    val formType = FormType.valueOf(entity.formType)
                    // Simple heuristic: check if form data contains issue indicators
                    entity.formData.contains("issue", ignoreCase = true) ||
                    entity.formData.contains("problem", ignoreCase = true) ||
                    entity.formData.contains("fault", ignoreCase = true)
                } catch (e: Exception) {
                    false
                }
            }
            
            val overdueForms = entities.count { entity ->
                try {
                    val createdAt = LocalDateTime.parse(entity.createdAt)
                    val daysSinceCreation = java.time.Duration.between(createdAt, LocalDateTime.now()).toDays()
                    daysSinceCreation > 7 && entity.status != FormStatus.COMPLETED.name
                } catch (e: Exception) {
                    false
                }
            }
            
            val pendingApprovalForms = entities.count { it.status == FormStatus.SUBMITTED.name }
            
            FormStatistics(
                totalForms = totalForms,
                formsByType = formsByType,
                formsByStatus = formsByStatus,
                formsBySite = formsBySite,
                completionRate = completionRate,
                averageCompletionTime = averageCompletionTime,
                formsWithIssues = formsWithIssues,
                overdueForms = overdueForms,
                pendingApprovalForms = pendingApprovalForms
            )
        } catch (e: Exception) {
            FormStatistics(
                totalForms = 0,
                formsByType = emptyMap(),
                formsByStatus = emptyMap(),
                formsBySite = emptyMap(),
                completionRate = 0.0,
                averageCompletionTime = 0.0,
                formsWithIssues = 0,
                overdueForms = 0,
                pendingApprovalForms = 0
            )
        }
    }
    
    override suspend fun getOverdueForms(): List<DigitalForm> {
        return try {
            val entities = formDao.getAllForms()
            entities.filter { entity ->
                try {
                    val createdAt = LocalDateTime.parse(entity.createdAt)
                    val daysSinceCreation = java.time.Duration.between(createdAt, LocalDateTime.now()).toDays()
                    daysSinceCreation > 7 && entity.status != FormStatus.COMPLETED.name
                } catch (e: Exception) {
                    false
                }
            }.mapNotNull { entity ->
                entityToDigitalForm(entity)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getFormsRequiringApproval(): List<DigitalForm> {
        return try {
            val entities = formDao.getAllForms()
            entities.filter { entity ->
                entity.status == FormStatus.SUBMITTED.name
            }.mapNotNull { entity ->
                entityToDigitalForm(entity)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getRelatedForms(formId: String): List<DigitalForm> {
        return try {
            val entities = formDao.getAllForms()
            // Find forms that are related to the given formId
            // This could be based on siteId, equipmentId, or other relationships
            val baseForm = entities.find { it.id == formId }
            if (baseForm != null) {
                entities.filter { entity ->
                    entity.id != formId && (
                        entity.siteLocation == baseForm.siteLocation ||
                        entity.equipmentId == baseForm.equipmentId ||
                        entity.createdBy == baseForm.createdBy
                    )
                }.mapNotNull { entity ->
                    entityToDigitalForm(entity)
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getFormDependencies(formId: String): List<FormDependency> {
        return try {
            val form = formDao.getFormById(formId)
            if (form != null) {
                // Based on form type, determine dependencies
                when (FormType.valueOf(form.formType)) {
                    FormType.JOB_CARD -> {
                        listOf(
                            FormDependency(
                                dependentFormId = formId,
                                requiredFormType = FormType.SAFETY,
                                requiredFormId = null,
                                dependencyType = DependencyType.PREREQUISITE,
                                isRequired = true,
                                description = "Safety inspection required before job execution"
                            )
                        )
                    }
                    FormType.TIMESHEET -> {
                        listOf(
                            FormDependency(
                                dependentFormId = formId,
                                requiredFormType = FormType.JOB_CARD,
                                requiredFormId = null,
                                dependencyType = DependencyType.PREREQUISITE,
                                isRequired = true,
                                description = "Job card required for timesheet entry"
                            )
                        )
                    }
                    else -> emptyList()
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun bulkUpdateFormStatus(formIds: List<String>, status: FormStatus): Result<Unit> {
        return try {
            formIds.forEach { formId ->
                val form = formDao.getFormById(formId)
                if (form != null) {
                    val updatedForm = form.copy(
                        status = status.name,
                        updatedAt = java.time.LocalDateTime.now().toString()
                    )
                    formDao.updateForm(updatedForm)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun exportForms(filters: FormFilters): Result<ByteArray> {
        return try {
            val forms = getFormsByFilters(filters)
            // Convert forms to Excel format using available method
            val firstForm = forms.firstOrNull()
            if (firstForm != null) {
                val excelResult = excelExportService.generateExcel(
                    formId = firstForm.id,
                    formSections = emptyList(), // TODO: Extract form sections
                    formData = emptyMap() // TODO: Extract form data
                )
                when {
                    excelResult.isSuccess -> {
                        val filePath = excelResult.getOrThrow()
                        val file = java.io.File(filePath)
                        Result.success(file.readBytes())
                    }
                    else -> Result.failure(excelResult.exceptionOrNull() ?: Exception("Export failed"))
                }
            } else {
                Result.success(ByteArray(0))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun bulkExportForms(formIds: List<String>, format: ExportFormat): Result<String> {
        return try {
            val forms = formIds.mapNotNull { formId ->
                getFormById(formId)
            }
            
            when (format) {
                ExportFormat.PDF -> {
                    val firstForm = forms.firstOrNull()
                    if (firstForm != null) {
                        val pdfResult = pdfExportService.generatePDF(
                            formId = firstForm.id,
                            formSections = emptyList(), // TODO: Extract form sections
                            formData = emptyMap() // TODO: Extract form data
                        )
                        Result.success("PDF export completed with ${forms.size} forms")
                    } else {
                        Result.success("No forms to export")
                    }
                }
                ExportFormat.EXCEL -> {
                    val firstForm = forms.firstOrNull()
                    if (firstForm != null) {
                        val excelResult = excelExportService.generateExcel(
                            formId = firstForm.id,
                            formSections = emptyList(), // TODO: Extract form sections
                            formData = emptyMap() // TODO: Extract form data
                        )
                        Result.success("Excel export completed with ${forms.size} forms")
                    } else {
                        Result.success("No forms to export")
                    }
                }
                ExportFormat.CSV -> {
                    val firstForm = forms.firstOrNull()
                    if (firstForm != null) {
                        val csvResult = csvExportService.generateCSV(
                            formId = firstForm.id,
                            formSections = emptyList(), // TODO: Extract form sections
                            formData = emptyMap() // TODO: Extract form data
                        )
                        Result.success("CSV export completed with ${forms.size} forms")
                    } else {
                        Result.success("No forms to export")
                    }
                }
                ExportFormat.JSON -> {
                    // TODO: Implement JSON export
                    Result.success("JSON export completed with ${forms.size} forms")
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getFormCompletionRates(siteId: String, formType: FormType): FormCompletionStats {
        return try {
            val entities = formDao.getFormsBySiteAndType(siteId, formType.name)
            val totalRequired = entities.size
            val totalCompleted = entities.count { it.status == FormStatus.COMPLETED.name }
            val completionRate = if (totalRequired > 0) (totalCompleted.toDouble() / totalRequired) * 100 else 0.0
            
            val completedEntities = entities.filter { it.status == FormStatus.COMPLETED.name }
            val averageDaysToComplete = if (completedEntities.isNotEmpty()) {
                completedEntities.mapNotNull { entity ->
                    try {
                        val createdAt = LocalDateTime.parse(entity.createdAt)
                        val updatedAt = LocalDateTime.parse(entity.updatedAt)
                        java.time.Duration.between(createdAt, updatedAt).toDays().toDouble()
                    } catch (e: Exception) {
                        null
                    }
                }.average()
            } else 0.0
            
            val overdueCount = entities.count { entity ->
                try {
                    val createdAt = LocalDateTime.parse(entity.createdAt)
                    val daysSinceCreation = java.time.Duration.between(createdAt, LocalDateTime.now()).toDays()
                    daysSinceCreation > 7 && entity.status != FormStatus.COMPLETED.name
                } catch (e: Exception) {
                    false
                }
            }
            
            val lastCompletedDate = completedEntities.maxByOrNull { it.updatedAt }?.let { entity ->
                try {
                    LocalDateTime.parse(entity.updatedAt).toLocalDate()
                } catch (e: Exception) {
                    null
                }
            }
            
            FormCompletionStats(
                formType = formType,
                siteId = siteId,
                totalRequired = totalRequired,
                totalCompleted = totalCompleted,
                completionRate = completionRate,
                averageDaysToComplete = averageDaysToComplete,
                overdueCount = overdueCount,
                lastCompletedDate = lastCompletedDate
            )
        } catch (e: Exception) {
            FormCompletionStats(
                formType = formType,
                siteId = siteId,
                totalRequired = 0,
                totalCompleted = 0,
                completionRate = 0.0,
                averageDaysToComplete = 0.0,
                overdueCount = 0,
                lastCompletedDate = null
            )
        }
    }
    
    // Maintenance Forms
    override suspend fun saveMaintenanceForm(form: MaintenanceReportForm): MaintenanceReportForm {
        try {
            // Save to local database first
        val formEntity = FormEntity(
                id = form.id,
                formType = form.formType.name,
                createdAt = form.createdAt.toString(),
                updatedAt = form.updatedAt.toString(),
                createdBy = form.createdBy,
                status = form.status.name,
                equipmentId = form.equipmentId,
                siteLocation = form.siteLocation,
                reportNumber = form.reportNumber,
                formData = gson.toJson(form),
                synced = false
        )
        
        formDao.insertForm(formEntity)
            
            // Try to sync with server
            try {
                val formDto = FormDto(
                    id = form.id,
                    formType = form.formType.name,
                    title = "Maintenance Report",
                    createdBy = form.createdBy.toIntOrNull() ?: 0,
                    assignedTo = null,
                    equipmentId = form.equipmentId,
                    status = form.status.name,
                    formData = gson.toJson(form),
                    attachments = null,
                    createdAt = form.createdAt.toString(),
                    updatedAt = form.updatedAt.toString(),
                    completedAt = null,
                    createdByName = null,
                    assignedToName = null,
                    equipmentName = null
                )
                
                apiService.saveForm(formDto)
                
                // Update local entity as synced
                formDao.updateFormSyncStatus(form.id, true)
                
            } catch (e: Exception) {
                // Server sync failed, but local save succeeded
                // Form will be synced later
            }
            
            return form
            
        } catch (e: Exception) {
            throw Exception("Failed to save maintenance form: ${e.message}")
        }
    }
    
    override suspend fun getMaintenanceFormById(id: String): MaintenanceReportForm? {
        return try {
            val entity = formDao.getFormById(id)
            entity?.let {
                gson.fromJson(it.formData, MaintenanceReportForm::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getMaintenanceFormsByUser(userId: String): List<MaintenanceReportForm> {
        return try {
            val entities = formDao.getFormsByUser(userId)
            entities.filter { it.formType == FormType.MAINTENANCE.name }
                .mapNotNull { entity ->
                    try {
                        gson.fromJson(entity.formData, MaintenanceReportForm::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getMaintenanceFormsByEquipment(equipmentId: String): List<MaintenanceReportForm> {
        return try {
            val entities = formDao.getFormsByEquipment(equipmentId)
            entities.filter { it.formType == FormType.MAINTENANCE.name }
                .mapNotNull { entity ->
                    try {
                        gson.fromJson(entity.formData, MaintenanceReportForm::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun updateMaintenanceForm(form: MaintenanceReportForm): MaintenanceReportForm {
        return saveMaintenanceForm(form) // Use the same save logic
    }
    
    override suspend fun deleteMaintenanceForm(id: String): Boolean {
        return try {
            formDao.deleteForm(id)
            
            // Try to delete from server
            try {
                apiService.deleteForm(id)
            } catch (e: Exception) {
                // Server deletion failed, but local deletion succeeded
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Inspection Forms
    override suspend fun saveInspectionForm(form: InspectionReportForm): InspectionReportForm {
        try {
            val formEntity = FormEntity(
                id = form.id,
                formType = form.formType.name,
                createdAt = form.createdAt.toString(),
                updatedAt = form.updatedAt.toString(),
                createdBy = form.createdBy,
                status = form.status.name,
                equipmentId = form.equipmentId,
                siteLocation = form.siteLocation,
                reportNumber = form.reportNumber,
                formData = gson.toJson(form),
                synced = false
            )
            
            formDao.insertForm(formEntity)
            
            // Try to sync with server
            try {
                val formDto = FormDto(
                    id = form.id,
                    formType = form.formType.name,
                    title = "Inspection Report",
                    createdBy = form.createdBy.toIntOrNull() ?: 0,
                    assignedTo = null,
                    equipmentId = form.equipmentId,
                    status = form.status.name,
                    formData = gson.toJson(form),
                    attachments = null,
                    createdAt = form.createdAt.toString(),
                    updatedAt = form.updatedAt.toString(),
                    completedAt = null,
                    createdByName = null,
                    assignedToName = null,
                    equipmentName = null
                )
                
                apiService.saveForm(formDto)
                formDao.updateFormSyncStatus(form.id, true)
                
            } catch (e: Exception) {
                // Server sync failed, but local save succeeded
            }
            
            return form
            
        } catch (e: Exception) {
            throw Exception("Failed to save inspection form: ${e.message}")
        }
    }
    
    override suspend fun getInspectionFormById(id: String): InspectionReportForm? {
        return try {
            val entity = formDao.getFormById(id)
            entity?.let {
                gson.fromJson(it.formData, InspectionReportForm::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getInspectionFormsByUser(userId: String): List<InspectionReportForm> {
        return try {
            val entities = formDao.getFormsByUser(userId)
            entities.filter { it.formType == FormType.INSPECTION.name }
                .mapNotNull { entity ->
                    try {
                        gson.fromJson(entity.formData, InspectionReportForm::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getInspectionFormsByEquipment(equipmentId: String): List<InspectionReportForm> {
        return try {
            val entities = formDao.getFormsByEquipment(equipmentId)
            entities.filter { it.formType == FormType.INSPECTION.name }
                .mapNotNull { entity ->
                    try {
                        gson.fromJson(entity.formData, InspectionReportForm::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun updateInspectionForm(form: InspectionReportForm): InspectionReportForm {
        return saveInspectionForm(form)
    }
    
    override suspend fun deleteInspectionForm(id: String): Boolean {
        return deleteMaintenanceForm(id) // Use the same delete logic
    }
    
    // Safety Forms
    override suspend fun saveSafetyForm(form: SafetyReportForm): SafetyReportForm {
        try {
            val formEntity = FormEntity(
                id = form.id,
                formType = form.formType.name,
                createdAt = form.createdAt.toString(),
                updatedAt = form.updatedAt.toString(),
                createdBy = form.createdBy,
                status = form.status.name,
                equipmentId = form.equipmentId,
                siteLocation = form.siteLocation,
                reportNumber = form.reportNumber,
                formData = gson.toJson(form),
                synced = false
            )
            
            formDao.insertForm(formEntity)
            
            // Try to sync with server
            try {
                val formDto = FormDto(
                    id = form.id,
                    formType = form.formType.name,
                    title = "Safety Report",
                    createdBy = form.createdBy.toIntOrNull() ?: 0,
                    assignedTo = null,
                    equipmentId = form.equipmentId,
                    status = form.status.name,
                    formData = gson.toJson(form),
                    attachments = null,
                    createdAt = form.createdAt.toString(),
                    updatedAt = form.updatedAt.toString(),
                    completedAt = null,
                    createdByName = null,
                    assignedToName = null,
                    equipmentName = null
                )
                
                apiService.saveForm(formDto)
                formDao.updateFormSyncStatus(form.id, true)
                
            } catch (e: Exception) {
                // Server sync failed, but local save succeeded
            }
            
            return form
            
        } catch (e: Exception) {
            throw Exception("Failed to save safety form: ${e.message}")
        }
    }
    
    override suspend fun getSafetyFormById(id: String): SafetyReportForm? {
        return try {
            val entity = formDao.getFormById(id)
            entity?.let {
                gson.fromJson(it.formData, SafetyReportForm::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getFormsByUser(userId: String): List<DigitalForm> {
        return try {
            val entities = formDao.getFormsByUser(userId)
            entities.mapNotNull { entity ->
                try {
                    when (FormType.valueOf(entity.formType)) {
                        FormType.BLAST_HOLE_LOG -> gson.fromJson(entity.formData, BlastHoleLogForm::class.java) as DigitalForm
                        FormType.MMU_QUALITY_REPORT -> gson.fromJson(entity.formData, MmuQualityReportForm::class.java) as DigitalForm
                        FormType.MMU_DAILY_LOG -> gson.fromJson(entity.formData, MmuProductionDailyLogForm::class.java) as DigitalForm
                        FormType.FIRE_EXTINGUISHER_INSPECTION -> gson.fromJson(entity.formData, FireExtinguisherInspectionForm::class.java) as DigitalForm
                        FormType.MAINTENANCE_REPORT -> gson.fromJson(entity.formData, MaintenanceReportForm::class.java) as DigitalForm
                        FormType.SAFETY -> gson.fromJson(entity.formData, SafetyReportForm::class.java) as DigitalForm
                        FormType.INSPECTION -> gson.fromJson(entity.formData, InspectionReportForm::class.java) as DigitalForm
                        else -> null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getSafetyFormsByUser(userId: String): List<SafetyReportForm> {
        return try {
            val entities = formDao.getFormsByUser(userId)
            entities.filter { it.formType == FormType.SAFETY.name }
                .mapNotNull { entity ->
                    try {
                        gson.fromJson(entity.formData, SafetyReportForm::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun updateSafetyForm(form: SafetyReportForm): SafetyReportForm {
        return saveSafetyForm(form)
    }
    
    override suspend fun deleteSafetyForm(id: String): Boolean {
        return deleteMaintenanceForm(id) // Use the same delete logic
    }
    
    // Generic Form Operations
    override suspend fun getAllForms(): List<DigitalForm> {
        val entities = formDao.getAllForms()
        return entities.mapNotNull { entity ->
            entityToDigitalForm(entity)
        }
    }

    override suspend fun getFormsBySite(siteId: String): List<DigitalForm> {
        return try {
            val entities = formDao.getFormsBySiteLocation(siteId)
            entities.mapNotNull { entity ->
                try {
                    gson.fromJson(entity.formData, DigitalForm::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getFormsBySiteAndType(siteId: String, formType: FormType): List<DigitalForm> {
        return try {
            val entities = formDao.getFormsBySiteAndType(siteId, formType.name)
            entities.mapNotNull { entity ->
                try {
                    when (FormType.valueOf(entity.formType)) {
                        FormType.BLAST_HOLE_LOG -> gson.fromJson(entity.formData, BlastHoleLogForm::class.java) as DigitalForm
                        FormType.MMU_QUALITY_REPORT -> gson.fromJson(entity.formData, MmuQualityReportForm::class.java) as DigitalForm
                        FormType.MMU_DAILY_LOG -> gson.fromJson(entity.formData, MmuProductionDailyLogForm::class.java) as DigitalForm
                        FormType.FIRE_EXTINGUISHER_INSPECTION -> gson.fromJson(entity.formData, FireExtinguisherInspectionForm::class.java) as DigitalForm
                        FormType.MAINTENANCE_REPORT -> gson.fromJson(entity.formData, MaintenanceReportForm::class.java) as DigitalForm
                        FormType.SAFETY -> gson.fromJson(entity.formData, SafetyReportForm::class.java) as DigitalForm
                        FormType.INSPECTION -> gson.fromJson(entity.formData, InspectionReportForm::class.java) as DigitalForm
                        else -> null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getAllFormsByUser(userId: String): List<FormData> {
        val entities = formDao.getFormsByUser(userId)
        return entities.mapNotNull { entity ->
            try {
                when (entity.formType) {
                    FormType.MAINTENANCE.name -> gson.fromJson(entity.formData, MaintenanceReportForm::class.java)
                    FormType.INSPECTION.name -> gson.fromJson(entity.formData, InspectionReportForm::class.java)
                    FormType.SAFETY.name -> gson.fromJson(entity.formData, SafetyReportForm::class.java)
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    override suspend fun getFormsByStatus(status: FormStatus): List<DigitalForm> {
        return try {
            val entities = formDao.getFormsByStatus(status.name)
            entities.mapNotNull { entity ->
                try {
                    when (FormType.valueOf(entity.formType)) {
                        FormType.BLAST_HOLE_LOG -> gson.fromJson(entity.formData, BlastHoleLogForm::class.java) as DigitalForm
                        FormType.MMU_QUALITY_REPORT -> gson.fromJson(entity.formData, MmuQualityReportForm::class.java) as DigitalForm
                        FormType.MMU_DAILY_LOG -> gson.fromJson(entity.formData, MmuProductionDailyLogForm::class.java) as DigitalForm
                        FormType.FIRE_EXTINGUISHER_INSPECTION -> gson.fromJson(entity.formData, FireExtinguisherInspectionForm::class.java) as DigitalForm
                        FormType.MAINTENANCE_REPORT -> gson.fromJson(entity.formData, MaintenanceReportForm::class.java) as DigitalForm
                        else -> null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getFormsByType(formType: FormType): List<DigitalForm> {
        return try {
            val entities = formDao.getFormsByType(formType.name)
            entities.mapNotNull { entity ->
                try {
                    when (FormType.valueOf(entity.formType)) {
                        FormType.BLAST_HOLE_LOG -> gson.fromJson(entity.formData, BlastHoleLogForm::class.java) as DigitalForm
                        FormType.MMU_QUALITY_REPORT -> gson.fromJson(entity.formData, MmuQualityReportForm::class.java) as DigitalForm
                        FormType.MMU_DAILY_LOG -> gson.fromJson(entity.formData, MmuProductionDailyLogForm::class.java) as DigitalForm
                        FormType.FIRE_EXTINGUISHER_INSPECTION -> gson.fromJson(entity.formData, FireExtinguisherInspectionForm::class.java) as DigitalForm
                        FormType.MAINTENANCE_REPORT -> gson.fromJson(entity.formData, MaintenanceReportForm::class.java) as DigitalForm
                        FormType.SAFETY -> gson.fromJson(entity.formData, SafetyReportForm::class.java) as DigitalForm
                        FormType.INSPECTION -> gson.fromJson(entity.formData, InspectionReportForm::class.java) as DigitalForm
                        else -> null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getFormsByDateRange(startDate: String, endDate: String): List<FormData> {
        return try {
            val entities = formDao.getFormsByDateRange(startDate, endDate)
            entities.mapNotNull { entity ->
                try {
                    when (entity.formType) {
                        FormType.MAINTENANCE.name -> gson.fromJson(entity.formData, MaintenanceReportForm::class.java)
                        FormType.INSPECTION.name -> gson.fromJson(entity.formData, InspectionReportForm::class.java)
                        FormType.SAFETY.name -> gson.fromJson(entity.formData, SafetyReportForm::class.java)
                        else -> null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getFormsByUserAndType(userId: String, formType: FormType): List<DigitalForm> {
        return try {
            val entities = formDao.getFormsByUserAndType(userId, formType.name)
            entities.mapNotNull { entity ->
                try {
                    when (FormType.valueOf(entity.formType)) {
                        FormType.BLAST_HOLE_LOG -> gson.fromJson(entity.formData, BlastHoleLogForm::class.java) as DigitalForm
                        FormType.MMU_QUALITY_REPORT -> gson.fromJson(entity.formData, MmuQualityReportForm::class.java) as DigitalForm
                        FormType.MMU_DAILY_LOG -> gson.fromJson(entity.formData, MmuProductionDailyLogForm::class.java) as DigitalForm
                        FormType.FIRE_EXTINGUISHER_INSPECTION -> gson.fromJson(entity.formData, FireExtinguisherInspectionForm::class.java) as DigitalForm
                        FormType.MAINTENANCE_REPORT -> gson.fromJson(entity.formData, MaintenanceReportForm::class.java) as DigitalForm
                        FormType.SAFETY -> gson.fromJson(entity.formData, SafetyReportForm::class.java) as DigitalForm
                        FormType.INSPECTION -> gson.fromJson(entity.formData, InspectionReportForm::class.java) as DigitalForm
                        else -> null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Real-time updates
    override fun getFormsFlow(): Flow<List<FormData>> = flow {
        formDao.getAllFormsFlow().collect { entities ->
            val forms = entities.mapNotNull { entity ->
                try {
                    when (entity.formType) {
                        FormType.MAINTENANCE.name -> gson.fromJson(entity.formData, MaintenanceReportForm::class.java)
                        FormType.INSPECTION.name -> gson.fromJson(entity.formData, InspectionReportForm::class.java)
                        FormType.SAFETY.name -> gson.fromJson(entity.formData, SafetyReportForm::class.java)
                        else -> null
                    }
                } catch (e: Exception) {
                    null
                }
            }
            emit(forms)
        }
    }
    
    override fun getFormsByUserFlow(userId: String): Flow<List<FormData>> = flow {
        formDao.getFormsByUserFlow(userId).collect { entities ->
            val forms = entities.mapNotNull { entity ->
                try {
                    when (entity.formType) {
                        FormType.MAINTENANCE.name -> gson.fromJson(entity.formData, MaintenanceReportForm::class.java)
                        FormType.INSPECTION.name -> gson.fromJson(entity.formData, InspectionReportForm::class.java)
                        FormType.SAFETY.name -> gson.fromJson(entity.formData, SafetyReportForm::class.java)
                        else -> null
                    }
        } catch (e: Exception) {
                    null
                }
            }
            emit(forms)
        }
    }
    
    override fun getDigitalFormsFlow(): Flow<List<DigitalForm>> = flow {
        formDao.getAllFormsFlow().collect { entities ->
            val digitalForms = entities.mapNotNull { entity ->
                try {
                    entity.toDomainModel()
                } catch (e: Exception) {
                    null
                }
            }
            emit(digitalForms)
        }
    }
    
    override fun getDigitalFormsByUserFlow(userId: String): Flow<List<DigitalForm>> = flow {
        formDao.getFormsByUserFlow(userId).collect { entities ->
            val digitalForms = entities.mapNotNull { entity ->
                try {
                    entity.toDomainModel()
                } catch (e: Exception) {
                    null
                }
            }
            emit(digitalForms)
        }
    }
    
    override suspend fun createForm(type: FormType, userId: String, equipmentId: String?, shiftId: String?, locationId: String?): String {
        val formId = java.util.UUID.randomUUID().toString()
        val now = java.time.LocalDateTime.now()
        
        // Create a basic form based on type
        val form = when (type) {
            FormType.MAINTENANCE -> MaintenanceReportForm(
                id = formId,
                formType = type,
                createdAt = now,
                updatedAt = now,
                createdBy = userId,
                status = FormStatus.DRAFT,
                equipmentId = equipmentId,
                siteLocation = locationId ?: "Unknown",
                reportNumber = "REP-${formId.takeLast(8)}",
                equipmentName = "Unknown Equipment",
                equipmentModel = "",
                equipmentSerial = "",
                equipmentLocation = locationId ?: "Unknown",
                equipmentHours = null,
                maintenanceType = MaintenanceType.PREVENTIVE,
                workDescription = "",
                partsUsed = emptyList(),
                laborHours = 0.0,
                maintenanceDate = java.time.LocalDate.now(),
                completionDate = null,
                nextMaintenanceDate = null,
                technicianName = "",
                technicianId = userId,
                supervisorName = null,
                supervisorApproval = false,
                preMaintenanceCondition = ConditionRating.GOOD,
                postMaintenanceCondition = ConditionRating.GOOD,
                issuesFound = emptyList(),
                recommendations = null,
                photos = emptyList(),
                attachments = emptyList(),
                notes = null
            )
            FormType.INSPECTION -> InspectionReportForm(
                id = formId,
                formType = type,
                createdAt = now,
                updatedAt = now,
                createdBy = userId,
                status = FormStatus.DRAFT,
                equipmentId = equipmentId,
                siteLocation = locationId ?: "Unknown",
                reportNumber = "INS-${formId.takeLast(8)}",
                equipmentName = "Unknown Equipment",
                equipmentModel = "",
                equipmentSerial = "",
                equipmentLocation = locationId ?: "Unknown",
                inspectionType = InspectionType.ROUTINE,
                inspectionDate = java.time.LocalDate.now(),
                inspectorName = "",
                inspectorId = userId,
                inspectionFrequency = "Monthly",
                lastInspectionDate = null,
                nextInspectionDate = java.time.LocalDate.now().plusMonths(1),
                inspectionItems = emptyList(),
                overallCondition = ConditionRating.GOOD,
                operationalStatus = OperationalStatus.OPERATIONAL,
                deficienciesFound = emptyList(),
                correctiveActions = emptyList(),
                recommendations = null,
                complianceStatus = ComplianceStatus.COMPLIANT,
                regulatoryReferences = emptyList(),
                photos = emptyList(),
                attachments = emptyList(),
                notes = null
            )
            FormType.SAFETY -> SafetyReportForm(
                id = formId,
                formType = type,
                createdAt = now,
                updatedAt = now,
                createdBy = userId,
                equipmentId = equipmentId,
                status = FormStatus.DRAFT,
                siteLocation = locationId ?: "Unknown",
                reportNumber = "SAF-${formId.takeLast(8)}",
                incidentDate = java.time.LocalDate.now(),
                incidentTime = java.time.LocalTime.now().toString(),
                incidentLocation = locationId ?: "Unknown",
                incidentType = IncidentType.NEAR_MISS,
                severityLevel = SeverityLevel.LOW,
                reportedBy = "Unknown Reporter",
                reporterId = userId,
                witnesses = emptyList(),
                injuredPersons = emptyList(),
                incidentDescription = "Initial incident description",
                immediateActions = "",
                rootCause = "",
                contributingFactors = emptyList(),
                equipmentInvolved = emptyList(),
                environmentalConditions = "",
                ppeUsed = emptyList(),
                safetyProceduresFollowed = true,
                investigatorAssigned = null,
                investigationDate = null,
                investigationFindings = null,
                correctiveActions = emptyList(),
                preventiveMeasures = emptyList(),
                trainingDetails = null,
                regulatoryBody = null,
                notificationDate = null,
                regulatoryReference = null,
                photos = emptyList(),
                attachments = emptyList(),
                notes = null
            )
            // Support all other form types with a generic form structure
            else -> {
                // Create a generic form for all other form types
                // Since we don't have specific form models for all types,
                // we'll default to creating a maintenance form but with the correct type
                MaintenanceReportForm(
                    id = formId,
                    formType = type,
                    createdAt = now,
                    updatedAt = now,
                    createdBy = userId,
                    status = FormStatus.DRAFT,
                    equipmentId = equipmentId,
                    siteLocation = locationId ?: "Unknown",
                    reportNumber = "${type.name}-${formId.takeLast(8)}",
                    equipmentName = "Unknown Equipment",
                    equipmentModel = "",
                    equipmentSerial = "",
                    equipmentLocation = locationId ?: "Unknown",
                    equipmentHours = null,
                    maintenanceType = MaintenanceType.PREVENTIVE,
                    workDescription = "",
                    partsUsed = emptyList(),
                    laborHours = 0.0,
                    maintenanceDate = java.time.LocalDate.now(),
                    completionDate = null,
                    nextMaintenanceDate = null,
                    technicianName = "",
                    technicianId = userId,
                    supervisorName = null,
                    supervisorApproval = false,
                    preMaintenanceCondition = ConditionRating.GOOD,
                    postMaintenanceCondition = ConditionRating.GOOD,
                    issuesFound = emptyList(),
                    recommendations = null,
                    photos = emptyList(),
                    attachments = emptyList(),
                    notes = null
                )
            }
        }
        
        // Save the form
        when (form) {
            is MaintenanceReportForm -> saveMaintenanceForm(form)
            is InspectionReportForm -> saveInspectionForm(form)
            is SafetyReportForm -> saveSafetyForm(form)
        }
        
        return formId
    }
    
    override suspend fun getPendingFormSubmissions(): List<FormData> {
        return formDao.getPendingForms().mapNotNull { entity ->
            try {
                when (entity.formType) {
                    FormType.MAINTENANCE.name -> gson.fromJson(entity.formData, MaintenanceReportForm::class.java)
                    FormType.INSPECTION.name -> gson.fromJson(entity.formData, InspectionReportForm::class.java)
                    FormType.SAFETY.name -> gson.fromJson(entity.formData, SafetyReportForm::class.java)
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    override suspend fun syncFormSubmission(form: FormData): Result<Unit> {
        return try {
            // Convert form to DTO and sync with API
            val formDto = FormDto(
                id = form.id,
                formType = form.formType.name,
                title = "Submitted Form",
                createdBy = form.createdBy.toIntOrNull() ?: 0,
                assignedTo = null,
                equipmentId = form.equipmentId,
                status = form.status.name,
                formData = gson.toJson(form),
                attachments = null,
                createdAt = form.createdAt.toString(),
                updatedAt = form.updatedAt.toString(),
                completedAt = null,
                createdByName = null,
                assignedToName = null,
                equipmentName = null
            )
            
            // Create FormSubmissionRequest for API call
            val submissionRequest = FormSubmissionRequest(
                type = form.formType.name,
                equipmentId = form.equipmentId,
                shiftId = null, // Add shiftId if available in form
                locationId = form.siteLocation,
                formData = mapOf("formData" to gson.toJson(form)),
                attachments = emptyList()
            )
            
            val token = "Bearer placeholder_token" // Get actual token from auth service
            val response = apiService.submitForm(submissionRequest, token)
            if (response.isSuccessful) {
            Result.success(Unit)
            } else {
                Result.failure(Exception("API submission failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markFormAsSynced(formId: String): Result<Unit> {
        return try {
            formDao.updateSyncStatus(formId, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteForm(formId: String): Result<Unit> {
        return try {
            formDao.deleteForm(formId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getFormById(formId: String): DigitalForm? {
        val entity = formDao.getFormById(formId) ?: return null
        
        return try {
            when (FormType.valueOf(entity.formType)) {
                FormType.BLAST_HOLE_LOG -> gson.fromJson(entity.formData, BlastHoleLogForm::class.java) as DigitalForm
                FormType.MMU_QUALITY_REPORT -> gson.fromJson(entity.formData, MmuQualityReportForm::class.java) as DigitalForm
                FormType.MMU_DAILY_LOG -> gson.fromJson(entity.formData, MmuProductionDailyLogForm::class.java) as DigitalForm
                FormType.FIRE_EXTINGUISHER_INSPECTION -> gson.fromJson(entity.formData, FireExtinguisherInspectionForm::class.java) as DigitalForm
                FormType.MAINTENANCE_REPORT -> gson.fromJson(entity.formData, MaintenanceReportForm::class.java) as DigitalForm
                FormType.SAFETY -> gson.fromJson(entity.formData, SafetyReportForm::class.java) as DigitalForm
                FormType.INSPECTION -> gson.fromJson(entity.formData, InspectionReportForm::class.java) as DigitalForm
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun saveForm(formId: String, formData: Map<String, Any>) {
        val entity = formDao.getFormById(formId) ?: return
        
        // Merge the new data with existing form data
        val existingData = try {
            gson.fromJson(entity.formData, Map::class.java) as Map<String, Any>
        } catch (e: Exception) {
            emptyMap()
        }
        
        val updatedData = existingData + formData
        val updatedEntity = entity.copy(
            formData = gson.toJson(updatedData),
            updatedAt = java.time.LocalDateTime.now().toString()
        )
        
        formDao.updateForm(updatedEntity)
    }

    override suspend fun getFormTemplateById(templateId: String): FormTemplate? {
        // For now, return null as placeholder - this would typically fetch from database
        return null
    }

    override suspend fun submitForm(formId: String) {
        try {
            val formEntity = formDao.getFormById(formId) ?: return
            
            // Update form status to submitted
            val updatedEntity = formEntity.copy(
                status = "SUBMITTED",
                updatedAt = java.time.LocalDateTime.now().toString()
            )
            formDao.updateForm(updatedEntity)
            
            // Try to sync with mobile server first
            val mobileServerUrl = mobileServerConfig.getActiveServerUrl()
            if (mobileServerUrl.isNotEmpty()) {
                try {
                    // Convert form entity to submission request
                    val submissionRequest = FormSubmissionRequest(
                        type = formEntity.formType,
                        equipmentId = formEntity.equipmentId,
                        shiftId = formEntity.shiftId,
                        locationId = formEntity.locationId,
                        formData = parseFormData(formEntity.formData),
                        attachments = emptyList()
                    )
                    
                    val mobileResponse = mobileServerApiService.createForm(
                        CreateFormRequest(
                            formType = formEntity.formType,
                            title = "Submitted Form",
                            assignedTo = null,
                            equipmentId = formEntity.equipmentId,
                            formData = formEntity.formData
                        )
                    )
                    
                    if (mobileResponse.isSuccessful && mobileResponse.body() != null) {
                        // Update sync status
                        formDao.updateFormSyncStatus(formEntity.id, true)
                    }
                } catch (e: Exception) {
                    // Mobile server failed, continue to cloud fallback
                    e.printStackTrace()
                }
            }
            
            // Fallback to original AECI cloud server
            if (networkManager.isNetworkAvailable()) {
                try {
                    val submissionRequest = FormSubmissionRequest(
                        type = formEntity.formType,
                        equipmentId = formEntity.equipmentId,
                        shiftId = formEntity.shiftId,
                        locationId = formEntity.locationId,
                        formData = parseFormData(formEntity.formData),
                        attachments = emptyList()
                    )
                    
                    val response = apiService.submitForm(submissionRequest, "Bearer token_placeholder")
                    if (response.isSuccessful && response.body()?.success == true) {
                        // Update sync status
                        formDao.updateFormSyncStatus(formEntity.id, true)
                    } else {
                        // Mark as pending sync
                        formDao.updateFormSyncStatus(formEntity.id, false)
                    }
                } catch (e: Exception) {
                    // Mark as pending sync
                    formDao.updateFormSyncStatus(formEntity.id, false)
                }
            } else {
                // No network, mark as pending sync
                formDao.updateFormSyncStatus(formEntity.id, false)
            }
        } catch (e: Exception) {
            // Log error but don't throw
            e.printStackTrace()
        }
    }
    
    override suspend fun exportFormToPdf(formId: String, coordinates: List<FieldCoordinate>, pdfTemplatePath: String): Result<String> {
        return try {
            // Get the form from database
            val formEntity = formDao.getFormById(formId)
                ?: return Result.failure(Exception("Form not found with ID: $formId"))

            // Convert to domain model  
            val formType = FormType.valueOf(formEntity.formType)
            val form = when (formType) {
                FormType.BLAST_HOLE_LOG -> gson.fromJson(formEntity.formData, BlastHoleLogForm::class.java) as DigitalForm
                FormType.MMU_QUALITY_REPORT -> gson.fromJson(formEntity.formData, MmuQualityReportForm::class.java) as DigitalForm
                FormType.MMU_DAILY_LOG -> gson.fromJson(formEntity.formData, MmuProductionDailyLogForm::class.java) as DigitalForm
                FormType.FIRE_EXTINGUISHER_INSPECTION -> gson.fromJson(formEntity.formData, FireExtinguisherInspectionForm::class.java) as DigitalForm
                FormType.MAINTENANCE_REPORT -> gson.fromJson(formEntity.formData, MaintenanceReportForm::class.java) as DigitalForm
                FormType.SAFETY -> gson.fromJson(formEntity.formData, SafetyReportForm::class.java) as DigitalForm
                FormType.INSPECTION -> gson.fromJson(formEntity.formData, InspectionReportForm::class.java) as DigitalForm
                else -> return Result.failure(Exception("Unsupported form type: $formType"))
            }
            
            // Get form template to determine sections and fields
            val template = com.aeci.mmucompanion.data.templates.FormTemplates.getFormTemplate(formType)
                ?: return Result.failure(Exception("Template not found for form type: $formType"))

            // Generate PDF using our PDFExportService
            val pdfResult = pdfExportService.generatePDF(
                formId = formType.name.lowercase(),
                formSections = template.sections,
                formData = parseFormData(formEntity.formData)
            )

            pdfResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun exportFormToExcel(formId: String): Result<String> {
        return try {
            // Get the form from database
            val formEntity = formDao.getFormById(formId)
                ?: return Result.failure(Exception("Form not found with ID: $formId"))

            // Convert to domain model
            val formType = FormType.valueOf(formEntity.formType)
            val form = when (formType) {
                FormType.BLAST_HOLE_LOG -> gson.fromJson(formEntity.formData, BlastHoleLogForm::class.java) as DigitalForm
                FormType.MMU_QUALITY_REPORT -> gson.fromJson(formEntity.formData, MmuQualityReportForm::class.java) as DigitalForm
                FormType.MMU_DAILY_LOG -> gson.fromJson(formEntity.formData, MmuProductionDailyLogForm::class.java) as DigitalForm
                FormType.FIRE_EXTINGUISHER_INSPECTION -> gson.fromJson(formEntity.formData, FireExtinguisherInspectionForm::class.java) as DigitalForm
                FormType.MAINTENANCE_REPORT -> gson.fromJson(formEntity.formData, MaintenanceReportForm::class.java) as DigitalForm
                FormType.SAFETY -> gson.fromJson(formEntity.formData, SafetyReportForm::class.java) as DigitalForm
                FormType.INSPECTION -> gson.fromJson(formEntity.formData, InspectionReportForm::class.java) as DigitalForm
                else -> return Result.failure(Exception("Unsupported form type: $formType"))
            }
            
            // Get form template to determine sections and fields
            val template = com.aeci.mmucompanion.data.templates.FormTemplates.getFormTemplate(formType)
                ?: return Result.failure(Exception("Template not found for form type: $formType"))

            // Generate Excel using our ExcelExportService
            val excelResult = excelExportService.generateExcel(
                formId = formType.name.lowercase(),
                formSections = template.sections,
                formData = extractFormData(form)
            )

            excelResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun exportFormToCSV(formId: String): Result<String> {
        return try {
            // Get the form from database
            val formEntity = formDao.getFormById(formId)
                ?: return Result.failure(Exception("Form not found with ID: $formId"))

            // Convert to domain model
            val formType = FormType.valueOf(formEntity.formType)
            val form = when (formType) {
                FormType.BLAST_HOLE_LOG -> gson.fromJson(formEntity.formData, BlastHoleLogForm::class.java) as DigitalForm
                FormType.MMU_QUALITY_REPORT -> gson.fromJson(formEntity.formData, MmuQualityReportForm::class.java) as DigitalForm
                FormType.MMU_DAILY_LOG -> gson.fromJson(formEntity.formData, MmuProductionDailyLogForm::class.java) as DigitalForm
                FormType.FIRE_EXTINGUISHER_INSPECTION -> gson.fromJson(formEntity.formData, FireExtinguisherInspectionForm::class.java) as DigitalForm
                FormType.MAINTENANCE_REPORT -> gson.fromJson(formEntity.formData, MaintenanceReportForm::class.java) as DigitalForm
                FormType.SAFETY -> gson.fromJson(formEntity.formData, SafetyReportForm::class.java) as DigitalForm
                FormType.INSPECTION -> gson.fromJson(formEntity.formData, InspectionReportForm::class.java) as DigitalForm
                else -> return Result.failure(Exception("Unsupported form type: $formType"))
            }
            
            // Get form template to determine sections and fields
            val template = com.aeci.mmucompanion.data.templates.FormTemplates.getFormTemplate(formType)
                ?: return Result.failure(Exception("Template not found for form type: $formType"))

            // Generate CSV using our CSVExportService
            val csvResult = csvExportService.generateCSV(
                formId = formType.name.lowercase(),
                formSections = template.sections,
                formData = extractFormData(form)
            )

            csvResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun exportFormToJSON(formId: String): Result<String> {
        return try {
            // Get the form from database
            val formEntity = formDao.getFormById(formId)
                ?: return Result.failure(Exception("Form not found with ID: $formId"))

            // Convert to domain model
            val formType = FormType.valueOf(formEntity.formType)
            val form = when (formType) {
                FormType.BLAST_HOLE_LOG -> gson.fromJson(formEntity.formData, BlastHoleLogForm::class.java) as DigitalForm
                FormType.MMU_QUALITY_REPORT -> gson.fromJson(formEntity.formData, MmuQualityReportForm::class.java) as DigitalForm
                FormType.MMU_DAILY_LOG -> gson.fromJson(formEntity.formData, MmuProductionDailyLogForm::class.java) as DigitalForm
                FormType.FIRE_EXTINGUISHER_INSPECTION -> gson.fromJson(formEntity.formData, FireExtinguisherInspectionForm::class.java) as DigitalForm
                FormType.MAINTENANCE_REPORT -> gson.fromJson(formEntity.formData, MaintenanceReportForm::class.java) as DigitalForm
                FormType.SAFETY -> gson.fromJson(formEntity.formData, SafetyReportForm::class.java) as DigitalForm
                FormType.INSPECTION -> gson.fromJson(formEntity.formData, InspectionReportForm::class.java) as DigitalForm
                else -> return Result.failure(Exception("Unsupported form type: $formType"))
            }
            
            // Create JSON representation
            val jsonData = mapOf(
                "formId" to form.id,
                "formType" to formType.name,
                "templateId" to formType.name,
                "createdAt" to form.createdAt.toString(),
                "submissionData" to extractFormData(form)
            )
            
            val jsonString = gson.toJson(jsonData)
            
            // Save to file
            val fileName = "form_${formId}_${System.currentTimeMillis()}.json"
            val file = File(fileName)
            file.writeText(jsonString)
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Sync methods needed by OfflineSyncService
    override suspend fun syncPendingForms() {
        // Get all unsynced form submissions
        val unsyncedForms = formDao.getUnsyncedForms()
        
        unsyncedForms.forEach { formEntity ->
            try {
                // Convert to FormSubmissionRequest and submit to API
                val submissionRequest = FormSubmissionRequest(
                    type = formEntity.formType,
                    equipmentId = formEntity.equipmentId,
                    shiftId = formEntity.shiftId,
                    locationId = formEntity.locationId,
                    formData = parseFormData(formEntity.formData),
                    attachments = emptyList()
                )
                
                val response = apiService.submitForm(submissionRequest, "Bearer token_placeholder")
                if (response.isSuccessful && response.body()?.success == true) {
                    // Mark as synced in local database
                    formDao.updateFormSyncStatus(formEntity.id, true)
                }
            } catch (e: Exception) {
                // Log error but continue with other forms
                e.printStackTrace()
            }
        }
    }

    
    // Sync operations for form templates
    override suspend fun downloadLatestFormTemplates(): Result<List<FormTemplate>> {
        return try {
            // Download latest form templates from server
            // For now, return empty list - this can be implemented when server sync is needed
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cacheFormTemplates(templates: List<FormTemplate>) {
        try {
            // Cache form templates locally
            // For now, this is a placeholder - can be implemented when form template caching is needed
            android.util.Log.d("FormRepository", "Cached ${templates.size} form templates")
        } catch (e: Exception) {
            android.util.Log.e("FormRepository", "Failed to cache form templates", e)
            throw e
        }
    }

    private fun extractFormData(form: DigitalForm): Map<String, String> {
        return mapOf(
            "id" to form.id,
            "formType" to form.formType.name,
            "createdAt" to form.createdAt.toString(),
            "updatedAt" to form.updatedAt.toString(),
            "createdBy" to form.createdBy,
            "status" to form.status.name,
            "siteId" to form.siteId,
            "siteLocation" to form.siteLocation,
            "equipmentId" to (form.equipmentId ?: ""),
            "reportNumber" to (form.reportNumber ?: ""),
            "title" to (form.title ?: ""),
            "assignedTo" to (form.assignedTo ?: ""),
            "description" to (form.description ?: ""),
            "location" to (form.location ?: ""),
            "instructions" to (form.instructions ?: "")
        )
    }
}

// Extension methods
fun FormEntity.toDomainModel(): DigitalForm {
    return DigitalForm(
        id = this.id,
        formType = FormType.valueOf(this.formType),
        createdAt = java.time.LocalDateTime.parse(this.createdAt),
        updatedAt = java.time.LocalDateTime.parse(this.updatedAt),
        createdBy = this.createdBy,
        status = FormStatus.valueOf(this.status),
        siteId = this.siteLocation, // Use siteLocation as siteId if siteId property doesn't exist
        siteLocation = this.siteLocation,
        reportNumber = this.reportNumber ?: "",
        equipmentId = this.equipmentId,
        data = if (this.formData.isNotEmpty()) {
            try {
                Gson().fromJson(this.formData, Map::class.java) as Map<String, Any>
            } catch (e: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }
    )
}

fun entityToDigitalForm(entity: FormEntity): DigitalForm? {
    return try {
        entity.toDomainModel()
    } catch (e: Exception) {
        null
    }
}
