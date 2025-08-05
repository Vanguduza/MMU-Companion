package com.aeci.mmucompanion.domain.repository

import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface FormRepository {
    
    // Digital Form Operations (New System)
    suspend fun saveForm(form: DigitalForm): Result<String>
    suspend fun updateForm(form: DigitalForm): Result<Unit>
    suspend fun deleteForm(formId: String): Result<Unit>
    suspend fun getFormById(formId: String): DigitalForm?
    
    // Query operations
    suspend fun getFormsByType(formType: FormType): List<DigitalForm>
    suspend fun getFormsByUser(userId: String): List<DigitalForm>
    suspend fun getFormsBySite(siteId: String): List<DigitalForm>
    suspend fun getFormsByStatus(status: FormStatus): List<DigitalForm>
    
    // Advanced queries
    suspend fun getFormsBySiteAndType(siteId: String, formType: FormType): List<DigitalForm>
    suspend fun getFormsByUserAndType(userId: String, formType: FormType): List<DigitalForm>
    suspend fun getFormsByDateRange(startDate: LocalDate, endDate: LocalDate): List<DigitalForm>
    suspend fun getFormsBySiteAndDateRange(
        siteId: String, 
        formType: FormType, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): List<DigitalForm>
    
    // Search and filter operations
    suspend fun searchForms(query: String): List<DigitalForm>
    suspend fun getFormsByFilters(filters: FormFilters): List<DigitalForm>
    
    // Analytics and reporting
    suspend fun getFormStatistics(siteId: String? = null, dateRange: DateRange? = null): FormStatistics
    suspend fun getFormCompletionRates(siteId: String, formType: FormType): FormCompletionStats
    suspend fun getOverdueForms(): List<DigitalForm>
    suspend fun getFormsRequiringApproval(): List<DigitalForm>
    
    // Relationship operations
    suspend fun getRelatedForms(formId: String): List<DigitalForm>
    suspend fun getFormDependencies(formId: String): List<FormDependency>
    
    // Bulk operations
    suspend fun saveForms(forms: List<DigitalForm>): Result<List<String>>
    suspend fun bulkUpdateFormStatus(formIds: List<String>, status: FormStatus): Result<Unit>
    suspend fun exportForms(filters: FormFilters): Result<ByteArray>
    
    // Legacy Form Support (Backward Compatibility)
    suspend fun createForm(type: FormType, userId: String, equipmentId: String?, shiftId: String?, locationId: String?): String
    suspend fun saveForm(formId: String, formData: Map<String, Any>)
    suspend fun submitForm(formId: String)
    suspend fun getFormTemplateById(templateId: String): FormTemplate?
    suspend fun exportFormToPdf(formId: String, coordinates: List<FieldCoordinate>, pdfTemplatePath: String): Result<String>
    suspend fun exportFormToExcel(formId: String): Result<String>
    
    // Maintenance Forms (Legacy)
    suspend fun saveMaintenanceForm(form: MaintenanceReportForm): MaintenanceReportForm
    suspend fun getMaintenanceFormById(id: String): MaintenanceReportForm?
    suspend fun getMaintenanceFormsByUser(userId: String): List<MaintenanceReportForm>
    suspend fun getMaintenanceFormsByEquipment(equipmentId: String): List<MaintenanceReportForm>
    suspend fun updateMaintenanceForm(form: MaintenanceReportForm): MaintenanceReportForm
    suspend fun deleteMaintenanceForm(id: String): Boolean
    
    // Inspection Forms (Legacy)
    suspend fun saveInspectionForm(form: InspectionReportForm): InspectionReportForm
    suspend fun getInspectionFormById(id: String): InspectionReportForm?
    suspend fun getInspectionFormsByUser(userId: String): List<InspectionReportForm>
    suspend fun getInspectionFormsByEquipment(equipmentId: String): List<InspectionReportForm>
    suspend fun updateInspectionForm(form: InspectionReportForm): InspectionReportForm
    suspend fun deleteInspectionForm(id: String): Boolean
    
    // Safety Forms (Legacy)
    suspend fun saveSafetyForm(form: SafetyReportForm): SafetyReportForm
    suspend fun getSafetyFormById(id: String): SafetyReportForm?
    suspend fun getSafetyFormsByUser(userId: String): List<SafetyReportForm>
    suspend fun updateSafetyForm(form: SafetyReportForm): SafetyReportForm
    suspend fun deleteSafetyForm(id: String): Boolean
    
    // Generic Operations (Legacy)
    suspend fun getAllForms(): List<DigitalForm>
    suspend fun getAllFormsByUser(userId: String): List<FormData>
    suspend fun getFormsByDateRange(startDate: String, endDate: String): List<FormData>
    
    // Offline sync operations
    suspend fun getPendingForms(): List<DigitalForm>
    suspend fun markFormAsSynced(formId: String): Result<Unit>
    suspend fun getFormsSinceLastSync(lastSyncTime: Long): List<DigitalForm>
    suspend fun getPendingFormSubmissions(): List<FormData>
    suspend fun syncFormSubmission(form: FormData): Result<Unit>
    suspend fun syncPendingForms()
    
    // Real-time updates
    fun getFormsFlow(): Flow<List<FormData>>
    fun getFormsByUserFlow(userId: String): Flow<List<FormData>>
    fun getDigitalFormsFlow(): Flow<List<DigitalForm>>
    fun getDigitalFormsByUserFlow(userId: String): Flow<List<DigitalForm>>
    
    // Export operations
    suspend fun bulkExportForms(formIds: List<String>, format: ExportFormat): Result<String>
    
    // Template operations
    suspend fun downloadLatestFormTemplates(): Result<List<FormTemplate>>
    suspend fun cacheFormTemplates(templates: List<FormTemplate>)
}

data class FormFilters(
    val formTypes: List<FormType>? = null,
    val siteIds: List<String>? = null,
    val userIds: List<String>? = null,
    val statuses: List<FormStatus>? = null,
    val dateRange: DateRange? = null,
    val searchQuery: String? = null,
    val tags: List<String>? = null,
    val priority: FormPriority? = null,
    val hasIssues: Boolean? = null,
    val requiresApproval: Boolean? = null
)

data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class FormStatistics(
    val totalForms: Int,
    val formsByType: Map<FormType, Int>,
    val formsByStatus: Map<FormStatus, Int>,
    val formsBySite: Map<String, Int>,
    val completionRate: Double,
    val averageCompletionTime: Double, // in hours
    val formsWithIssues: Int,
    val overdueForms: Int,
    val pendingApprovalForms: Int
)

data class FormCompletionStats(
    val formType: FormType,
    val siteId: String,
    val totalRequired: Int,
    val totalCompleted: Int,
    val completionRate: Double,
    val averageDaysToComplete: Double,
    val overdueCount: Int,
    val lastCompletedDate: LocalDate?
)

data class FormDependency(
    val dependentFormId: String,
    val requiredFormType: FormType,
    val requiredFormId: String?,
    val dependencyType: DependencyType,
    val isRequired: Boolean,
    val description: String
)

enum class DependencyType {
    PREREQUISITE,       // Must be completed before
    DATA_SOURCE,        // Provides data for calculations
    APPROVAL_CHAIN,     // Part of approval workflow
    RELATED_ACTIVITY,   // Related but not blocking
    QUALITY_CHECK      // Quality verification dependency
}

enum class FormPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL,
    EMERGENCY
}







