package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.domain.usecase.GetEquipmentByIdUseCase
import com.aeci.mmucompanion.domain.usecase.form.SaveMaintenanceFormUseCase
import com.aeci.mmucompanion.domain.usecase.GetCurrentUserUseCase
import com.aeci.mmucompanion.presentation.component.PDFTemplateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaintenanceFormViewModel @Inject constructor(
    private val getEquipmentByIdUseCase: GetEquipmentByIdUseCase,
    private val saveMaintenanceFormUseCase: SaveMaintenanceFormUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val pdfTemplateManager: PDFTemplateManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MaintenanceFormUiState())
    val uiState: StateFlow<MaintenanceFormUiState> = _uiState.asStateFlow()
    
    init {
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val currentUser = getCurrentUserUseCase()
                _uiState.value = _uiState.value.copy(
                    currentUser = currentUser
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load user: ${e.message}"
                )
            }
        }
    }
    
    fun loadEquipmentData(equipmentId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val equipment = getEquipmentByIdUseCase(equipmentId)
                _uiState.value = _uiState.value.copy(
                    equipment = equipment,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load equipment: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun saveForm(form: MaintenanceReportForm) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Save the form
                val savedForm = saveMaintenanceFormUseCase(form)
                
                // Generate PDF report
                val pdfBytes = pdfTemplateManager.generatePDF(savedForm)
                
                _uiState.value = _uiState.value.copy(
                    savedForm = savedForm,
                    isLoading = false,
                    successMessage = "Maintenance report saved successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save form: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}

data class MaintenanceFormUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val equipment: Equipment? = null,
    val savedForm: MaintenanceReportForm? = null,
    val error: String? = null,
    val successMessage: String? = null
) 
