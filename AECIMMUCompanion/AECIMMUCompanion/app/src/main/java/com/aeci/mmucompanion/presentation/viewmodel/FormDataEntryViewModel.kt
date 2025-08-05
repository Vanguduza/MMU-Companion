package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.repository.FormRepository
import com.aeci.mmucompanion.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.aeci.mmucompanion.domain.service.PDFExportService
import java.io.File
import java.time.LocalDateTime

data class FormDataEntryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val formTitle: String = "",
    val formSections: List<FormSection> = emptyList(),
    val formData: Map<String, String> = emptyMap(),
    val fieldErrors: Map<String, String> = emptyMap(),
    val canExport: Boolean = false,
    val isSaving: Boolean = false,
    val isExporting: Boolean = false
)

@HiltViewModel
class FormDataEntryViewModel @Inject constructor(
    private val pdfExportService: PDFExportService
    // TODO: Inject form repository when implemented
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormDataEntryUiState())
    val uiState: StateFlow<FormDataEntryUiState> = _uiState.asStateFlow()

    fun loadForm(formType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val formTemplate = getFormTemplate(formType)
                val initialData = formTemplate.fields.map { field ->
                    field.fieldName to (field.defaultValue ?: "")
                }.toMap()
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        formTitle = formTemplate.name,
                        formSections = listOf(FormSection(title = formTemplate.name, fields = formTemplate.fields)),
                        formData = initialData
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load form"
                    )
                }
            }
        }
    }

    fun updateFieldValue(fieldName: String, value: String) {
        _uiState.update { state ->
            val newFormData = state.formData.toMutableMap()
            newFormData[fieldName] = value
            
            // Clear field error if it exists
            val newFieldErrors = state.fieldErrors.toMutableMap()
            newFieldErrors.remove(fieldName)
            
            state.copy(
                formData = newFormData,
                fieldErrors = newFieldErrors,
                canExport = validateForm(state.formSections, newFormData).isEmpty()
            )
        }
    }

    fun saveForm() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                val validationErrors = validateForm(uiState.value.formSections, uiState.value.formData)
                
                if (validationErrors.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            fieldErrors = validationErrors,
                            error = "Please fix the validation errors"
                        )
                    }
                    return@launch
                }
                
                // TODO: Save form data to repository
                // For now, just simulate saving
                kotlinx.coroutines.delay(1000)
                
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = null
                    )
                }
                
                // TODO: Show success message
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save form"
                    )
                }
            }
        }
    }

    fun exportPDF() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            
            try {
                val validationErrors = validateForm(uiState.value.formSections, uiState.value.formData)
                
                if (validationErrors.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            fieldErrors = validationErrors,
                            error = "Please fix the validation errors before exporting"
                        )
                    }
                    return@launch
                }
                
                // Generate PDF with form data overlaid on template
                val currentState = uiState.value
                val outputPath = "${File.separator}storage${File.separator}emulated${File.separator}0${File.separator}Download${File.separator}${currentState.formTitle.replace(" ", "_")}.pdf"
                
                val result = pdfExportService.exportFormToPDF(
                    formId = currentState.formSections.firstOrNull()?.title?.lowercase()?.replace(" ", "_") ?: "unknown",
                    formTitle = currentState.formTitle,
                    formSections = currentState.formSections,
                    formData = currentState.formData,
                    outputPath = outputPath
                )
                
                if (result.isFailure) {
                    throw result.exceptionOrNull() ?: Exception("PDF export failed")
                }
                
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        error = null
                    )
                }
                
                // TODO: Show success message and offer to share PDF
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        error = e.message ?: "Failed to export PDF"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun validateForm(sections: List<FormSection>, formData: Map<String, String>): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        
        sections.forEach { section ->
            section.fields.forEach { field ->
                val value = formData[field.fieldName] ?: ""
                
                // Required field validation
                if (field.isRequired && value.isBlank()) {
                    errors[field.fieldName] = "This field is required"
                    return@forEach
                }
                
                // Skip validation for empty optional fields
                if (value.isBlank()) return@forEach
                
                // Type-specific validation
                when (field.fieldType) {
                    FormFieldType.NUMBER -> {
                        value.toDoubleOrNull() ?: run {
                            errors[field.fieldName] = "Please enter a valid number"
                        }
                    }
                    FormFieldType.INTEGER -> {
                        value.toIntOrNull() ?: run {
                            errors[field.fieldName] = "Please enter a valid integer"
                        }
                    }
                    FormFieldType.DATE -> {
                        if (!isValidDate(value)) {
                            errors[field.fieldName] = "Please enter a valid date (DD/MM/YYYY)"
                        }
                    }
                    FormFieldType.TIME -> {
                        if (!isValidTime(value)) {
                            errors[field.fieldName] = "Please enter a valid time (HH:MM)"
                        }
                    }
                    else -> {
                        // No additional validation needed for other types
                    }
                }
                
                // Custom validation rules
                field.validation?.let { validation ->
                    validation.minLength?.let { minLength ->
                        if (value.length < minLength) {
                            errors[field.fieldName] = "Must be at least $minLength characters"
                        }
                    }
                    
                    validation.maxLength?.let { maxLength ->
                        if (value.length > maxLength) {
                            errors[field.fieldName] = "Must be at most $maxLength characters"
                        }
                    }
                    
                    validation.pattern?.let { pattern ->
                        if (!value.matches(Regex(pattern))) {
                            errors[field.fieldName] = validation.customMessage ?: "Invalid format"
                        }
                    }
                }
            }
        }
        
        return errors
    }

    private fun isValidDate(dateString: String): Boolean {
        // Simple date validation for DD/MM/YYYY format
        val datePattern = Regex("^\\d{2}/\\d{2}/\\d{4}$")
        return datePattern.matches(dateString)
    }

    private fun isValidTime(timeString: String): Boolean {
        // Simple time validation for HH:MM format
        val timePattern = Regex("^\\d{2}:\\d{2}$")
        return timePattern.matches(timeString)
    }

    private fun getFormTemplate(formType: String): FormTemplate {
        // For now, return a sample form template based on the PDF coordinate maps
        return when (formType) {
            "pump_inspection_90day" -> createPumpInspectionForm()
            "availability_utilization" -> createAvailabilityUtilizationForm()
            "blast_hole_log" -> createBlastHoleLogForm()
            "bowie_weekly" -> createBowieWeeklyForm()
            "fire_extinguisher" -> createFireExtinguisherForm()
            "job_card" -> createJobCardForm()
            "chassis_maintenance" -> createChassisMaintenanceForm()
            "handover_certificate" -> createHandoverCertificateForm()
            "production_log" -> createProductionLogForm()
            "quality_report" -> createQualityReportForm()
            "monthly_maintenance" -> createMonthlyMaintenanceForm()
            "bench_inspection" -> createBenchInspectionForm()
            "pressure_trip_test" -> createPressureTripTestForm()
            "pretask_assessment" -> createPretaskAssessmentForm()
            else -> createDefaultForm()
        }
    }

    private fun createPumpInspectionForm(): FormTemplate {
        return FormTemplate(
            id = "pump_inspection_template",
            name = "90 Day Pump System Inspection Checklist",
            description = "Comprehensive pump inspection form with multiple sections for visual checks, pressure tests, and component assessments",
            formType = FormType.PUMP_90_DAY_INSPECTION,
            templateFile = "pump_90_day_inspection_template.pdf",
            pdfTemplate = "pump_90_day_inspection_template.pdf",
            fieldMappings = emptyList(),
            fields = listOf(
                FormField(
                    fieldName = "inspection_date",
                    label = "Inspection Date",
                    fieldType = FormFieldType.DATE,
                    isRequired = true
                ),
                FormField(
                    fieldName = "equipment_id",
                    label = "Equipment ID",
                    fieldType = FormFieldType.TEXT,
                    isRequired = true
                ),
                FormField(
                    fieldName = "pump_model",
                    label = "Pump Model",
                    fieldType = FormFieldType.TEXT,
                    isRequired = true
                ),
                FormField(
                    fieldName = "serial_number",
                    label = "Serial Number",
                    fieldType = FormFieldType.TEXT,
                    isRequired = true
                ),
                FormField(
                    fieldName = "operating_hours",
                    label = "Operating Hours",
                    fieldType = FormFieldType.NUMBER,
                    isRequired = false
                ),
                FormField(
                    fieldName = "inspector_name",
                    label = "Inspector Name",
                    fieldType = FormFieldType.TEXT,
                    isRequired = true
                ),
                FormField(
                    fieldName = "visual_inspection_ok",
                    label = "Visual Inspection OK",
                    fieldType = FormFieldType.CHECKBOX,
                    isRequired = true
                ),
                FormField(
                    fieldName = "pressure_test_result",
                    label = "Pressure Test Result",
                    fieldType = FormFieldType.NUMBER,
                    isRequired = false,
                    unit = "PSI"
                ),
                FormField(
                    fieldName = "overall_condition",
                    label = "Overall Condition",
                    fieldType = FormFieldType.DROPDOWN,
                    isRequired = true,
                    options = listOf("Excellent", "Good", "Fair", "Poor")
                ),
                FormField(
                    fieldName = "notes",
                    label = "Additional Notes",
                    fieldType = FormFieldType.MULTILINE_TEXT,
                    isRequired = false
                ),
                FormField(
                    fieldName = "inspector_signature",
                    label = "Inspector Signature",
                    fieldType = FormFieldType.SIGNATURE,
                    isRequired = true
                )
            ),
            version = "1.0",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createAvailabilityUtilizationForm(): FormTemplate {
        return FormTemplate(
            id = "availability_utilization_template",
            name = "Availability & Utilization Report",
            description = "Equipment availability and utilization tracking form",
            formType = FormType.AVAILABILITY_UTILIZATION,
            templateFile = "availability_utilization_template.pdf",
            pdfTemplate = "availability_utilization_template.pdf",
            fieldMappings = emptyList(),
            fields = listOf(
                FormField(
                    fieldName = "report_date",
                    label = "Report Date",
                    fieldType = FormFieldType.DATE,
                    isRequired = true
                ),
                FormField(
                    fieldName = "equipment_id",
                    label = "Equipment ID",
                    fieldType = FormFieldType.TEXT,
                    isRequired = true
                ),
                FormField(
                    fieldName = "availability_percentage",
                    label = "Availability %",
                    fieldType = FormFieldType.NUMBER,
                    isRequired = true
                )
            ),
            version = "1.0",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    // Add other form creation methods as needed
    private fun createBlastHoleLogForm(): FormTemplate {
        return FormTemplate(
            id = "blast_hole_log_template",
            name = "Blast Hole Log",
            description = "Log for recording blast hole drilling information",
            formType = FormType.BLAST_HOLE_LOG,
            templateFile = "blast_hole_log_template.pdf",
            pdfTemplate = "blast_hole_log_template.pdf",
            fieldMappings = emptyList(),
            fields = listOf(
                FormField(
                    fieldName = "log_date",
                    label = "Log Date",
                    fieldType = FormFieldType.DATE,
                    isRequired = true
                ),
                FormField(
                    fieldName = "hole_number",
                    label = "Hole Number",
                    fieldType = FormFieldType.TEXT,
                    isRequired = true
                ),
                FormField(
                    fieldName = "drill_depth",
                    label = "Drill Depth",
                    fieldType = FormFieldType.NUMBER,
                    isRequired = true,
                    unit = "m"
                ),
                FormField(
                    fieldName = "operator_name",
                    label = "Operator Name",
                    fieldType = FormFieldType.TEXT,
                    isRequired = true
                )
            ),
            version = "1.0",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createBowieWeeklyForm(): FormTemplate {
        return FormTemplate(
            id = "bowie_weekly",
            name = "Bowie Pump Weekly Check List",
            description = "Weekly checklist for Bowie pump maintenance and inspection",
            formType = FormType.PUMP_WEEKLY_CHECK,
            templateFile = "bowie_weekly_template.pdf",
            pdfTemplate = "bowie_weekly_template.pdf",
            fieldMappings = emptyList(),
            fields = listOf(
                FormField(
                    fieldName = "check_date",
                    label = "Check Date",
                    fieldType = FormFieldType.DATE,
                    isRequired = true
                ),
                FormField(
                    fieldName = "pump_id",
                    label = "Pump ID",
                    fieldType = FormFieldType.EQUIPMENT_ID,
                    isRequired = true
                ),
                FormField(
                    fieldName = "oil_level",
                    label = "Oil Level",
                    fieldType = FormFieldType.DROPDOWN,
                    isRequired = true,
                    options = listOf("Low", "Medium", "High")
                ),
                FormField(
                    fieldName = "filter_condition",
                    label = "Filter Condition",
                    fieldType = FormFieldType.DROPDOWN,
                    isRequired = true,
                    options = listOf("Clean", "Dirty", "Replaced")
                ),
                FormField(
                    fieldName = "belt_tightness",
                    label = "Belt Tightness",
                    fieldType = FormFieldType.DROPDOWN,
                    isRequired = true,
                    options = listOf("Tight", "Loose")
                ),
                FormField(
                    fieldName = "pump_alignment",
                    label = "Pump Alignment",
                    fieldType = FormFieldType.DROPDOWN,
                    isRequired = true,
                    options = listOf("Correct", "Off-center")
                ),
                FormField(
                    fieldName = "vibration_level",
                    label = "Vibration Level",
                    fieldType = FormFieldType.DROPDOWN,
                    isRequired = true,
                    options = listOf("Low", "Medium", "High")
                ),
                FormField(
                    fieldName = "noise_level",
                    label = "Noise Level",
                    fieldType = FormFieldType.DROPDOWN,
                    isRequired = true,
                    options = listOf("Quiet", "Moderate", "Noisy")
                ),
                FormField(
                    fieldName = "notes",
                    label = "Additional Notes",
                    fieldType = FormFieldType.MULTILINE_TEXT,
                    isRequired = false
                )
            ),
            version = "1.0",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createFireExtinguisherForm(): FormTemplate {
        return FormTemplate(
            id = "fire_extinguisher_template",
            name = "Fire Extinguisher Inspection",
            description = "Monthly fire extinguisher inspection checklist",
            formType = FormType.FIRE_EXTINGUISHER_INSPECTION,
            templateFile = "fire_extinguisher_inspection_template.pdf",
            pdfTemplate = "fire_extinguisher_inspection_template.pdf",
            fieldMappings = emptyList(),
            fields = listOf(
                FormField(
                    fieldName = "inspection_date",
                    label = "Inspection Date",
                    fieldType = FormFieldType.DATE,
                    isRequired = true
                ),
                FormField(
                    fieldName = "extinguisher_id",
                    label = "Extinguisher ID",
                    fieldType = FormFieldType.TEXT,
                    isRequired = true
                ),
                FormField(
                    fieldName = "condition_ok",
                    label = "Condition OK",
                    fieldType = FormFieldType.CHECKBOX,
                    isRequired = true
                )
            ),
            version = "1.0",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createJobCardForm(): FormTemplate {
        return FormTemplate(
            id = "job_card_template",
            name = "Job Card",
            description = "Work order and job tracking card",
            formType = FormType.JOB_CARD,
            templateFile = "job_card_template.pdf",
            pdfTemplate = "job_card_template.pdf",
            fieldMappings = emptyList(),
            fields = listOf(
                FormField(
                    fieldName = "job_number",
                    label = "Job Number",
                    fieldType = FormFieldType.TEXT,
                    isRequired = true
                ),
                FormField(
                    fieldName = "job_description",
                    label = "Job Description",
                    fieldType = FormFieldType.MULTILINE_TEXT,
                    isRequired = true
                )
            ),
            version = "1.0",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createChassisMaintenanceForm(): FormTemplate {
        return FormTemplate(
            id = "chassis_maintenance",
            name = "MMU Chassis Maintenance Record",
            description = "Record of maintenance activities performed on the Mobile Mining Unit chassis",
            formType = FormType.MMU_CHASSIS_MAINTENANCE,
            templateFile = "chassis_maintenance_template.pdf",
            pdfTemplate = "chassis_maintenance_template.pdf",
            fieldMappings = emptyList(),
            fields = listOf(
                FormField(
                    fieldName = "maintenance_date",
                    label = "Maintenance Date",
                    fieldType = FormFieldType.DATE,
                    isRequired = true
                ),
                FormField(
                    fieldName = "mmu_id",
                    label = "MMU ID",
                    fieldType = FormFieldType.EQUIPMENT_ID,
                    isRequired = true
                ),
                FormField(
                    fieldName = "maintenance_type",
                    label = "Maintenance Type",
                    fieldType = FormFieldType.DROPDOWN,
                    isRequired = true,
                    options = listOf("Daily", "Weekly", "Monthly", "Annual")
                ),
                FormField(
                    fieldName = "tasks_completed",
                    label = "Tasks Completed",
                    fieldType = FormFieldType.MULTILINE_TEXT,
                    isRequired = true
                ),
                FormField(
                    fieldName = "next_maintenance_date",
                    label = "Next Maintenance Date",
                    fieldType = FormFieldType.DATE,
                    isRequired = true
                )
            ),
            version = "1.0",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createHandoverCertificateForm(): FormTemplate {
        return FormTemplate(
            id = "handover_certificate",
            name = "MMU Handover Certificate",
            description = "Certificate of handover for new or transferred Mobile Mining Units",
            formType = FormType.MMU_HANDOVER_CERTIFICATE,
            templateFile = "handover_certificate_template.pdf",
            pdfTemplate = "handover_certificate_template.pdf",
            fieldMappings = emptyList(),
            fields = listOf(
                FormField(
                    fieldName = "handover_date",
                    label = "Handover Date",
                    fieldType = FormFieldType.DATE,
                    isRequired = true
                ),
                FormField(
                    fieldName = "mmu_id",
                    label = "MMU ID",
                    fieldType = FormFieldType.EQUIPMENT_ID,
                    isRequired = true
                ),
                FormField(
                    fieldName = "handover_notes",
                    label = "Handover Notes",
                    fieldType = FormFieldType.MULTILINE_TEXT,
                    isRequired = false
                )
            ),
            version = "1.0",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createProductionLogForm(): FormTemplate {
        return FormTemplate(
            id = "production_log",
            name = "MMU Production Daily Log",
            description = "Daily log for tracking production activities and equipment status",
            formType = FormType.MMU_DAILY_LOG,
            templateFile = "production_log_template.pdf",
            pdfTemplate = "production_log_template.pdf",
            fieldMappings = emptyList(),
            fields = listOf(
                FormField(
                    fieldName = "production_date",
                    label = "Production Date",
                    fieldType = FormFieldType.DATE,
                    isRequired = true
                ),
                FormField(
                    fieldName = "mmu_id",
                    label = "MMU ID",
                    fieldType = FormFieldType.EQUIPMENT_ID,
                    isRequired = true
                ),
                FormField(
                    fieldName = "production_shift",
                    label = "Production Shift",
                    fieldType = FormFieldType.DROPDOWN,
                    isRequired = true,
                    options = listOf("Day", "Night")
                ),
                FormField(
                    fieldName = "production_output",
                    label = "Production Output",
                    fieldType = FormFieldType.NUMBER,
                    isRequired = true,
                    unit = "Tons"
                ),
                FormField(
                    fieldName = "equipment_issues",
                    label = "Equipment Issues",
                    fieldType = FormFieldType.MULTILINE_TEXT,
                    isRequired = false
                ),
                FormField(
                    fieldName = "next_maintenance_required",
                    label = "Next Maintenance Required",
                    fieldType = FormFieldType.CHECKBOX,
                    isRequired = false
                )
            ),
            version = "1.0",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createQualityReportForm(): FormTemplate {
        return FormTemplate(
            id = "quality_report",
            name = "MMU Quality Report",
            description = "Report detailing quality of Mobile Mining Unit components and overall performance",
            formType = FormType.MMU_QUALITY_REPORT,
            templateFile = "quality_report_template.pdf",
            pdfTemplate = "quality_report_template.pdf",
            fieldMappings = emptyList(),
            fields = listOf(
                FormField(
                    fieldName = "report_date",
                    label = "Report Date",
                    fieldType = FormFieldType.DATE,
                    isRequired = true
                ),
                FormField(
                    fieldName = "mmu_id",
                    label = "MMU ID",
                    fieldType = FormFieldType.EQUIPMENT_ID,
                    isRequired = true
                ),
                FormField(
                    fieldName = "quality_rating",
                    label = "Quality Rating",
                    fieldType = FormFieldType.DROPDOWN,
                    isRequired = true,
                    options = listOf("Excellent", "Good", "Fair", "Poor")
                ),
                FormField(
                    fieldName = "issues_found",
                    label = "Issues Found",
                    fieldType = FormFieldType.MULTILINE_TEXT,
                    isRequired = true
                ),
                FormField(
                    fieldName = "recommendations",
                    label = "Recommendations",
                    fieldType = FormFieldType.MULTILINE_TEXT,
                    isRequired = false
                )
            ),
            version = "1.0",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createMonthlyMaintenanceForm(): FormTemplate {
        return FormTemplate(
            id = "monthly_maintenance",
            name = "Monthly Process Maintenance Record",
            description = "Record of monthly maintenance activities and equipment status",
            formType = FormType.MONTHLY_PROCESS_MAINTENANCE,
            templateFile = "monthly_maintenance_template.pdf",
            pdfTemplate = "monthly_maintenance_template.pdf",
            fieldMappings = emptyList(),
            fields = listOf(
                FormField(
                    fieldName = "maintenance_date",
                    label = "Maintenance Date",
                    fieldType = FormFieldType.DATE,
                    isRequired = true
                ),
                FormField(
                    fieldName = "equipment_id",
                    label = "Equipment ID",
                    fieldType = FormFieldType.EQUIPMENT_ID,
                    isRequired = true
                ),
                FormField(
                    fieldName = "maintenance_type",
                    label = "Maintenance Type",
                    fieldType = FormFieldType.DROPDOWN,
                    isRequired = true,
                    options = listOf("Daily", "Weekly", "Monthly", "Annual")
                ),
                FormField(
                    fieldName = "tasks_completed",
                    label = "Tasks Completed",
                    fieldType = FormFieldType.MULTILINE_TEXT,
                    isRequired = true
                ),
                FormField(
                    fieldName = "next_maintenance_date",
                    label = "Next Maintenance Date",
                    fieldType = FormFieldType.DATE,
                    isRequired = true
                )
            ),
            version = "1.0",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createBenchInspectionForm(): FormTemplate {
        return FormTemplate(
            id = "bench_inspection",
            name = "On Bench MMU Inspection",
            description = "Detailed inspection of Mobile Mining Unit while on bench",
            formType = FormType.ON_BENCH_MMU_INSPECTION,
            templateFile = "on_bench_mmu_inspection_template.pdf",
            pdfTemplate = "on_bench_mmu_inspection_template.pdf",
            fieldMappings = emptyList(),
            fields = listOf(
                FormField(
                    fieldName = "inspection_date",
                    label = "Inspection Date",
                    fieldType = FormFieldType.DATE,
                    isRequired = true
                ),
                FormField(
                    fieldName = "mmu_id",
                    label = "MMU ID",
                    fieldType = FormFieldType.EQUIPMENT_ID,
                    isRequired = true
                )
            ),
            version = "1.0",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createPressureTripTestForm(): FormTemplate {
        return FormTemplate(
            id = "pressure_trip_test",
            name = "PC Pump High/Low Pressure Trip Test",
            description = "Test to check the high and low pressure trip settings of the PC pump",
            formType = FormType.PC_PUMP_PRESSURE_TEST,
            templateFile = "pressure_trip_test_template.pdf",
            pdfTemplate = "pressure_trip_test_template.pdf",
            fieldMappings = emptyList(),
            fields = listOf(
                FormField(
                    fieldName = "test_date",
                    label = "Test Date",
                    fieldType = FormFieldType.DATE,
                    isRequired = true
                ),
                FormField(
                    fieldName = "pump_id",
                    label = "Pump ID",
                    fieldType = FormFieldType.EQUIPMENT_ID,
                    isRequired = true
                ),
                FormField(
                    fieldName = "high_pressure_trip",
                    label = "High Pressure Trip",
                    fieldType = FormFieldType.NUMBER,
                    isRequired = true,
                    unit = "PSI"
                ),
                FormField(
                    fieldName = "low_pressure_trip",
                    label = "Low Pressure Trip",
                    fieldType = FormFieldType.NUMBER,
                    isRequired = true,
                    unit = "PSI"
                ),
                FormField(
                    fieldName = "test_notes",
                    label = "Test Notes",
                    fieldType = FormFieldType.MULTILINE_TEXT,
                    isRequired = false
                )
            ),
            version = "1.0",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createPretaskAssessmentForm(): FormTemplate {
        return FormTemplate(
            id = "pretask_assessment",
            name = "Pre-Task Safety Assessment",
            description = "Assessment of potential hazards and safety measures before starting a task",
            formType = FormType.PRETASK_SAFETY,
            templateFile = "pretask_assessment_template.pdf",
            pdfTemplate = "pretask_assessment_template.pdf",
            fieldMappings = emptyList(),
            fields = listOf(
                FormField(
                    fieldName = "assessment_date",
                    label = "Assessment Date",
                    fieldType = FormFieldType.DATE,
                    isRequired = true
                ),
                FormField(
                    fieldName = "task_description",
                    label = "Task Description",
                    fieldType = FormFieldType.MULTILINE_TEXT,
                    isRequired = true
                ),
                FormField(
                    fieldName = "safety_officer",
                    label = "Safety Officer",
                    fieldType = FormFieldType.TEXT,
                    isRequired = true
                ),
                FormField(
                    fieldName = "hazards_identified",
                    label = "Hazards Identified",
                    fieldType = FormFieldType.MULTILINE_TEXT,
                    isRequired = true
                ),
                FormField(
                    fieldName = "recommendations",
                    label = "Recommendations",
                    fieldType = FormFieldType.MULTILINE_TEXT,
                    isRequired = false
                )
            ),
            version = "1.0",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createDefaultForm(): FormTemplate {
        return FormTemplate(
            id = "default",
            name = "Default Form",
            description = "A generic form template for new forms",
            formType = FormType.MAINTENANCE,
            templateFile = "default_template.pdf",
            pdfTemplate = "default_template.pdf",
            fieldMappings = emptyList(),
            fields = listOf(
                FormField(
                    fieldName = "form_date",
                    label = "Form Date",
                    fieldType = FormFieldType.DATE,
                    isRequired = true
                ),
                FormField(
                    fieldName = "form_notes",
                    label = "Notes",
                    fieldType = FormFieldType.MULTILINE_TEXT,
                    placeholder = "Enter any additional notes here"
                )
            ),
            version = "1.0",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
} 
