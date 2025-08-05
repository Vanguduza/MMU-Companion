package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.domain.usecase.*
import com.aeci.mmucompanion.domain.repository.FormRepository
import com.aeci.mmucompanion.domain.service.ExcelExportService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@HiltViewModel
class FormViewModel @Inject constructor(
    private val formRepository: FormRepository,
    private val submitFormUseCase: SubmitFormUseCase,
    private val createFormUseCase: CreateFormUseCase,
    private val saveFormUseCase: SaveFormUseCase,
    private val getFormUseCase: GetFormUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormUiState())
    val uiState: StateFlow<FormUiState> = _uiState.asStateFlow()

    private val _forms = MutableStateFlow<List<Form>>(emptyList())
    val forms: StateFlow<List<Form>> = _forms.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFormType = MutableStateFlow<FormType?>(null)
    val selectedFormType: StateFlow<FormType?> = _selectedFormType.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentForm = MutableStateFlow<Form?>(null)
    val currentForm: StateFlow<Form?> = _currentForm.asStateFlow()

    private val _validationErrors = MutableStateFlow<List<ValidationError>>(emptyList())
    val validationErrors: StateFlow<List<ValidationError>> = _validationErrors.asStateFlow()

    private val _formData = MutableStateFlow<Map<String, Any>>(emptyMap())
    val formData: StateFlow<Map<String, Any>> = _formData.asStateFlow()

    private val _formTemplate = MutableStateFlow<FormTemplate?>(null)
    val formTemplate: StateFlow<FormTemplate?> = _formTemplate.asStateFlow()

    private val _exportPath = MutableStateFlow<String?>(null)
    val exportPath: StateFlow<String?> = _exportPath.asStateFlow()

    private val _showReportActionDialog = MutableStateFlow(false)
    val showReportActionDialog: StateFlow<Boolean> = _showReportActionDialog.asStateFlow()

    init {
        loadForms()
    }

    fun loadForms() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allForms = formRepository.getAllForms()
                _forms.value = allForms
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load forms"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFormById(formId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = getFormUseCase(formId)
                result.fold(
                    onSuccess = { form ->
                        _currentForm.value = form
                        // Load form data if form exists
                        form?.let { 
                            _formData.value = form.data
                        }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load form"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load form"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createNewForm(formType: FormType) {
        viewModelScope.launch {
            try {
                val result = createFormUseCase(
                    type = formType,
                    userId = "current_user", // This would come from auth state
                    equipmentId = null,
                    shiftId = null,
                    locationId = null
                )
                
                result.fold(
                    onSuccess = { formId ->
                        // Load the newly created form
                        loadFormById(formId)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Failed to create form"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to create form"
                )
            }
        }
    }

    fun updateFormField(fieldId: String, value: Any) {
        val currentData = _formData.value.toMutableMap()
        currentData[fieldId] = value
        _formData.value = currentData
        
        // Clear validation errors for this field
        val currentErrors = _validationErrors.value.toMutableList()
        currentErrors.removeAll { it.field == fieldId }
        _validationErrors.value = currentErrors
        
        // Update current form if it exists
        _currentForm.value?.let { form ->
            _currentForm.value = form.copy(
                data = currentData,
                updatedAt = LocalDateTime.now()
            )
        }
    }

    fun validateForm(): Boolean {
        // For now, return true as basic validation
        // TODO: Implement proper validation when ValidateFormUseCase is available
        return true
    }

    fun submitForm() {
        val form = _currentForm.value ?: return
        
        if (!validateForm()) {
            _uiState.value = _uiState.value.copy(
                error = "Please fix validation errors before submitting"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            
            try {
                // First save the form data
                val saveResult = saveFormUseCase(form.id, _formData.value)
                saveResult.fold(
                    onSuccess = {
                        // Then submit the form
                        val submitResult = submitFormUseCase(form.id)
                        submitResult.fold(
                            onSuccess = { pdfPath ->
                                _uiState.value = _uiState.value.copy(
                                    isSubmitting = false,
                                    isSubmitted = true,
                                    exportedFilePath = pdfPath,
                                    error = null
                                )
                                loadForms() // Refresh the forms list
                            },
                            onFailure = { error ->
                                _uiState.value = _uiState.value.copy(
                                    isSubmitting = false,
                                    error = error.message ?: "Failed to submit form"
                                )
                            }
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            error = error.message ?: "Failed to save form data"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = e.message ?: "Submission error"
                )
            }
        }
    }

    fun saveDraft() {
        val form = _currentForm.value ?: return
        
        viewModelScope.launch {
            try {
                val result = saveFormUseCase(form.id, _formData.value)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(error = null)
                        loadForms() // Refresh the forms list
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Failed to save draft"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to save draft"
                )
            }
        }
    }

    fun deleteForm(formId: String) {
        viewModelScope.launch {
            try {
                // TODO: Implement when DeleteFormUseCase is available
                // For now, just remove from local state
                _uiState.value = _uiState.value.copy(error = "Delete functionality not yet implemented")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Delete error"
                )
            }
        }
    }

    fun searchForms(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun filterByFormType(formType: FormType?) {
        _selectedFormType.value = formType
        applyFilters()
    }

    private fun applyFilters() {
        viewModelScope.launch {
            try {
                val allForms = formRepository.getAllForms()
                var filteredForms = allForms
                
                // Apply search query filter
                val query = _searchQuery.value
                if (query.isNotBlank()) {
                    filteredForms = filteredForms.filter { form ->
                        form.id.contains(query, ignoreCase = true) ||
                        form.type.toString().contains(query, ignoreCase = true) ||
                        form.equipmentId?.contains(query, ignoreCase = true) == true
                    }
                }
                
                // Apply form type filter
                val selectedType = _selectedFormType.value
                if (selectedType != null) {
                    filteredForms = filteredForms.filter { it.type == selectedType }
                }
                
                _forms.value = filteredForms
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to filter forms"
                )
            }
        }
    }

    fun exportToExcel(forms: List<Form>) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isExporting = true)
                
                // TODO: Implement Excel export when ExcelExportService is available
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = "Excel export functionality not yet implemented"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = e.message ?: "Failed to export to Excel"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearCurrentForm() {
        _currentForm.value = null
        _formData.value = emptyMap()
        _validationErrors.value = emptyList()
        _uiState.value = _uiState.value.copy(
            isSubmitted = false,
            exportedFilePath = null
        )
    }

    fun resetSubmissionState() {
        _uiState.value = _uiState.value.copy(isSubmitted = false)
    }

    fun initializeForm(formType: FormType) {
        viewModelScope.launch {
            try {
                // Create a basic form template for the given type
                val template = FormTemplate(
                    id = UUID.randomUUID().toString(),
                    name = formType.displayName,
                    description = "Form for ${formType.displayName}",
                    formType = formType,
                    templateFile = formType.templateFile,
                    pdfTemplate = null,
                    fieldMappings = emptyList(),
                    sections = emptyList(),
                    fields = emptyList(),
                    version = "1.0",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                _formTemplate.value = template
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to initialize form: ${e.message}")
            }
        }
    }

    fun updateField(fieldId: String, value: Any) {
        val currentData = _formData.value.toMutableMap()
        currentData[fieldId] = value
        _formData.value = currentData
    }

    fun setFieldError(fieldId: String, error: String?) {
        val currentErrors = _validationErrors.value.toMutableList()
        val existingErrorIndex = currentErrors.indexOfFirst { it.field == fieldId }
        
        if (error != null) {
            val validationError = ValidationError(field = fieldId, message = error)
            if (existingErrorIndex >= 0) {
                currentErrors[existingErrorIndex] = validationError
            } else {
                currentErrors.add(validationError)
            }
        } else {
            if (existingErrorIndex >= 0) {
                currentErrors.removeAt(existingErrorIndex)
            }
        }
        
        _validationErrors.value = currentErrors
    }

    fun loadForm(formId: String) {
        loadFormById(formId)
    }

    fun exportToPdf(formId: String): String? {
        // Mock implementation - return a dummy path
        val path = "/storage/emulated/0/Documents/form_$formId.pdf"
        _exportPath.value = path
        return path
    }

    fun exportToExcel(formId: String = "current_form"): String? {
        // Mock implementation - return a dummy path
        val path = "/storage/emulated/0/Documents/form_$formId.xlsx"
        _exportPath.value = path
        return path
    }

    fun dismissReportActionDialog() {
        _showReportActionDialog.value = false
    }

    suspend fun getAllForms(): List<Form> {
        return _forms.value
    }
}

data class FormUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val isExporting: Boolean = false,
    val error: String? = null,
    val exportedFilePath: String? = null
)
