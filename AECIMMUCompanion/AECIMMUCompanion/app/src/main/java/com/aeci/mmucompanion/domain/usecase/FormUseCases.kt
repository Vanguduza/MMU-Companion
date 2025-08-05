package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.FieldCoordinate
import com.aeci.mmucompanion.domain.model.Form
import com.aeci.mmucompanion.domain.model.FormType
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.domain.repository.FormRepository
import javax.inject.Inject
import javax.inject.Singleton

// Comprehensive Form Use Cases (New System)
@Singleton
class FormUseCases @Inject constructor(
    private val formRepository: FormRepository,
    private val formRelationshipManager: FormRelationshipManager,
    private val formValidationService: FormValidationService,
    private val siteAutoPopulationService: SiteAutoPopulationService,
    private val pdfGenerationService: PdfGenerationService
) {
    
    /**
     * Creates a new Form with auto-populated fields
     */
    suspend fun createForm(
        formType: FormType,
        userId: String,
        siteId: String? = null
    ): Result<Form> {
        return try {
            // Get default Form values based on user and Form type
            // Auto-populate site and user fields
            val defaultValues = siteAutoPopulationService.getDefaultFormValues(userId, formType)
            val populatedForm = DigitalForm(
                id = java.util.UUID.randomUUID().toString(),
                formType = formType,
                createdAt = java.time.LocalDateTime.now(),
                updatedAt = java.time.LocalDateTime.now(),
                createdBy = userId,
                status = FormStatus.DRAFT,
                siteId = defaultValues["siteId"] as? String ?: "",
                siteLocation = defaultValues["siteLocation"] as? String ?: "",
                data = defaultValues
            )
            
            // Save the Form
            val formId = formRepository.saveForm(populatedForm)
            
            if (formId.isSuccess) {
                Result.success(populatedForm as Form)
            } else {
                Result.failure(formId.exceptionOrNull() ?: Exception("Failed to save Form"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Saves a Form with validation and relationship processing
     */
    suspend fun saveForm(form: DigitalForm): Result<DigitalForm> {
        return try {
            // Validate the Form
            val validationResult = formValidationService.validateForm(form)
            
            if (validationResult is FormValidationService.ValidationResult.Invalid) {
                val criticalErrors = validationResult.errors.filter { 
                    it.severity == FormValidationService.ErrorSeverity.CRITICAL 
                }
                if (criticalErrors.isNotEmpty()) {
                    return Result.failure(Exception("Critical validation errors: ${criticalErrors.joinToString { it.message }}"))
                }
            }
            
            // Save the form
            val saveResult = formRepository.saveForm(form)
            
            if (saveResult.isSuccess) {
                // Process form relationships (if relationship manager is available)
                // formRelationshipManager.processFormRelationships(form)
                
                Result.success(form)
            } else {
                Result.failure(saveResult.exceptionOrNull() ?: Exception("Failed to save form"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Updates an existing Form
     */
    suspend fun updateForm(form: DigitalForm): Result<DigitalForm> {
        return try {
            // Validate the Form
            val validationResult = formValidationService.validateForm(form)
            
            if (validationResult is FormValidationService.ValidationResult.Invalid) {
                val criticalErrors = validationResult.errors.filter { 
                    it.severity == FormValidationService.ErrorSeverity.CRITICAL 
                }
                if (criticalErrors.isNotEmpty()) {
                    return Result.failure(Exception("Critical validation errors: ${criticalErrors.joinToString { it.message }}"))
                }
            }
            
            // Update the form with new timestamp
            val formToUpdate = form.copy(updatedAt = java.time.LocalDateTime.now())
            val updateResult = formRepository.updateForm(formToUpdate)
            
            if (updateResult.isSuccess) {
                // Process form relationships for updates (if relationship manager is available)
                // formRelationshipManager.processFormRelationships(updatedForm)
                
                Result.success(formToUpdate)
            } else {
                Result.failure(updateResult.exceptionOrNull() ?: Exception("Failed to update form"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Submits a Form for approval/completion
     */
    suspend fun submitForm(formId: String): Result<DigitalForm> {
        return try {
            val form = formRepository.getFormById(formId)
                ?: return Result.failure(Exception("Form not found"))
            
            // Validate Form before submission
            val validationResult = formValidationService.validateForm(form)
            
            if (validationResult is FormValidationService.ValidationResult.Invalid) {
                val errors = validationResult.errors.filter { 
                    it.severity != FormValidationService.ErrorSeverity.WARNING 
                }
                if (errors.isNotEmpty()) {
                    return Result.failure(Exception("Form validation failed: ${errors.joinToString { it.message }}"))
                }
            }
            
            // Update Form status to submitted
            val submittedForm = form.copy(
                status = FormStatus.SUBMITTED,
                updatedAt = java.time.LocalDateTime.now()
            )
            
            val updateResult = formRepository.updateForm(submittedForm)
            
            if (updateResult.isSuccess) {
                // Process any final Form relationships by saving the form
                formRelationshipManager.saveForm(submittedForm)
                
                Result.success(submittedForm)
            } else {
                Result.failure(updateResult.exceptionOrNull() ?: Exception("Failed to submit Form"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generates PDF for a Form
     */
    suspend fun generateFormPdf(formId: String): Result<ByteArray> {
        return try {
            val form = formRepository.getFormById(formId)
                ?: return Result.failure(Exception("Form not found"))
            
            pdfGenerationService.generatePdf(form)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Gets aggregated data for dashboard/reporting
     */
    suspend fun getAggregatedFormData(
        siteId: String,
        date: java.time.LocalDate
    ): Result<AggregatedFormData> {
        return try {
            val data = formRelationshipManager.getAggregatedFormData(siteId, date)
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Gets forms requiring attention (overdue, pending approval, etc.)
     */
    suspend fun getFormsRequiringAttention(): Result<FormsRequiringAttention> {
        return try {
            val overdueForms = formRepository.getOverdueForms()
            val pendingApprovalForms = formRepository.getFormsRequiringApproval()
            val rejectedForms = formRepository.getFormsByStatus(FormStatus.REJECTED)
            
            val result = FormsRequiringAttention(
                overdueForms = overdueForms,
                pendingApproval = pendingApprovalForms,
                rejected = rejectedForms,
                totalRequiringAttention = overdueForms.size + pendingApprovalForms.size + rejectedForms.size
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun createFormInstance(
        formType: FormType,
        userId: String,
        defaultValues: Map<String, Any>
    ): Form {
        val baseId = java.util.UUID.randomUUID().toString()
        val baseTime = java.time.LocalDateTime.now()
        
        return when (formType) {
            FormType.BLAST_HOLE_LOG -> BlastHoleLogForm(
                id = baseId,
                createdAt = baseTime,
                updatedAt = baseTime,
                createdBy = userId,
                siteId = defaultValues["siteId"] as? String ?: "",
                status = FormStatus.DRAFT,
                siteLocation = defaultValues["siteLocation"] as? String ?: "",
                blastNumber = defaultValues["blastNumber"] as? String ?: "",
                blastDate = defaultValues["blastDate"] as? java.time.LocalDate ?: java.time.LocalDate.now(),
                blastTime = defaultValues["blastTime"] as? java.time.LocalDateTime,
                siteName = defaultValues["siteName"] as? String ?: "",
                operatorName = defaultValues["operatorName"] as? String ?: "",
                holes = emptyList(),
                totalEmulsionUsed = 0.0,
                blastQualityGrade = defaultValues["blastQualityGrade"] as? String ?: "",
                notes = defaultValues["notes"] as? String
            )
            
            FormType.MMU_QUALITY_REPORT -> MmuQualityReportForm(
                id = baseId,
                createdAt = baseTime,
                updatedAt = baseTime,
                createdBy = userId,
                siteId = defaultValues["siteId"] as? String ?: "",
                siteLocation = defaultValues["siteLocation"] as? String ?: "",
                status = FormStatus.DRAFT,
                reportDate = defaultValues["reportDate"] as? java.time.LocalDate ?: java.time.LocalDate.now(),
                reportNumber = defaultValues["reportNumber"] as? String ?: "",
                shiftPattern = defaultValues["shiftPattern"] as? String ?: "",
                shiftSupervisor = defaultValues["shiftSupervisor"] as? String ?: "",
                targetEmulsionProduction = 0.0,
                actualEmulsionProduction = 0.0,
                emulsionUsedToday = 0.0,
                qualityGrade = "",
                densityTests = emptyList(),
                viscosityReading = 0.0,
                temperatureReading = 0.0,
                phLevel = 0.0,
                qualityIssues = emptyList(),
                recommendations = "",
                qualityTechnician = defaultValues["qualityTechnician"] as? String ?: "",
                approvedBy = ""
            )
            
            FormType.MMU_DAILY_LOG -> MmuProductionDailyLogForm(
                id = baseId,
                createdAt = baseTime,
                updatedAt = baseTime,
                createdBy = userId,
                siteId = defaultValues["siteId"] as? String ?: "",
                siteLocation = defaultValues["siteLocation"] as? String ?: "",
                status = FormStatus.DRAFT,
                logDate = defaultValues["logDate"] as? java.time.LocalDate ?: java.time.LocalDate.now(),
                shiftDetails = "",
                operatorName = defaultValues["operatorName"] as? String ?: "",
                supervisorName = defaultValues["supervisorName"] as? String ?: "",
                startTime = "06:00",
                endTime = "18:00",
                totalOperatingHours = 0.0,
                totalEmulsionConsumed = 0.0,
                qualityGradeAchieved = "",
                productionTarget = 0.0,
                actualProduction = 0.0,
                operatingTemperature = 0.0,
                equipmentCondition = "",
                maintenancePerformed = emptyList(),
                safetyObservations = emptyList(),
                operatorComments = "",
                supervisorComments = ""
            )
            
            FormType.PUMP_INSPECTION_90_DAY -> PumpInspection90DayForm(
                id = baseId,
                createdAt = baseTime,
                updatedAt = baseTime,
                createdBy = userId,
                siteId = defaultValues["siteId"] as? String ?: "",
                siteLocation = defaultValues["siteLocation"] as? String ?: "",
                status = FormStatus.DRAFT,
                inspectionDate = defaultValues["inspectionDate"] as? java.time.LocalDate ?: java.time.LocalDate.now(),
                inspectorName = defaultValues["inspectorName"] as? String ?: "",
                siteName = defaultValues["siteName"] as? String ?: "",
                pumpSerialNumber = "",
                equipmentLocation = "",
                lastInspectionDate = null,
                nextInspectionDue = java.time.LocalDate.now().plusMonths(3),
                visualInspectionItems = emptyList(),
                pressureTests = emptyList(),
                overallStatus = "",
                recommendedActions = emptyList(),
                inspectorSignature = "",
                supervisorApproval = ""
            )
            
            // Add other Form types as needed
            else -> throw IllegalArgumentException("Unsupported Form type: $formType")
        }
    }
}

data class FormsRequiringAttention(
    val overdueForms: List<Form>,
    val pendingApproval: List<Form>,
    val rejected: List<Form>,
    val totalRequiringAttention: Int
)

// Legacy Use Cases (Backward Compatibility)
class CreateFormUseCase @Inject constructor(
    private val formRepository: FormRepository
) {
    suspend operator fun invoke(
        type: FormType,
        userId: String,
        equipmentId: String? = null,
        shiftId: String? = null,
        locationId: String? = null
    ): Result<String> {
        return try {
            val formId = formRepository.createForm(type, userId, equipmentId, shiftId, locationId)
            Result.success(formId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class SaveFormUseCase @Inject constructor(
    private val formRepository: FormRepository
) {
    suspend operator fun invoke(formId: String, formData: Map<String, Any>): Result<Unit> {
        return try {
            formRepository.saveForm(formId, formData)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class SubmitFormUseCase @Inject constructor(
    private val formRepository: FormRepository
) {
    suspend operator fun invoke(formId: String): Result<String> {
        return try {
            // Submit the Form
            formRepository.submitForm(formId)
            
            // Export to PDF and return the path
            val exportResult = formRepository.exportFormToPdf(
                formId = formId,
                coordinates = emptyList(), // TODO: Get coordinates from template if needed
                pdfTemplatePath = "" // TODO: Get template path if needed
            )
            
            exportResult.fold(
                onSuccess = { path ->
                    Result.success(path)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GetFormUseCase @Inject constructor(
    private val formRepository: FormRepository
) {
    suspend operator fun invoke(formId: String): Result<Form?> {
        return try {
            val Form = formRepository.getFormById(formId)
            Result.success(Form)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class ExportFormUseCase @Inject constructor(
    private val formRepository: FormRepository
) {
    suspend fun exportToPdf(formId: String): Result<String> {
        return try {
            val form = formRepository.getFormById(formId)
                ?: return Result.failure(Exception("Form not found"))

            val template = formRepository.getFormTemplateById(form.formType.name)
                ?: return Result.failure(Exception("Form template not found for type: ${form.formType}"))
            
            val pdfTemplatePath = template.pdfTemplate
                ?: return Result.failure(Exception("PDF template path not defined for form: ${template.name}"))

            val coordinates = template.fields.map { field ->
                FieldCoordinate(
                    fieldName = field.fieldName,
                    x = field.x.toInt(),
                    y = field.y.toInt(),
                    width = field.width.toInt(),
                    height = field.height.toInt(),
                    fieldType = field.fieldType.name,
                    isRequired = field.isRequired,
                    placeholder = field.placeholder ?: ""
                )
            }

            formRepository.exportFormToPdf(formId, coordinates, pdfTemplatePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun exportToExcel(formId: String): Result<String> {
        return formRepository.exportFormToExcel(formId)
    }
}

class SyncFormsUseCase @Inject constructor(
    private val formRepository: FormRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            formRepository.syncPendingForms()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}




