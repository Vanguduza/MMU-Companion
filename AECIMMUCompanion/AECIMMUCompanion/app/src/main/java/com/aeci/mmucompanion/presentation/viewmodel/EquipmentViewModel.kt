package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.model.Equipment
import com.aeci.mmucompanion.domain.model.EquipmentStatusIndicator
import com.aeci.mmucompanion.domain.usecase.GetAllEquipmentUseCase
import com.aeci.mmucompanion.domain.usecase.GetEquipmentByIdUseCase
import com.aeci.mmucompanion.domain.usecase.UpdateEquipmentStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EquipmentViewModel @Inject constructor(
    private val getAllEquipmentUseCase: GetAllEquipmentUseCase,
    private val getEquipmentByIdUseCase: GetEquipmentByIdUseCase,
    private val updateEquipmentStatusUseCase: UpdateEquipmentStatusUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EquipmentUiState())
    val uiState: StateFlow<EquipmentUiState> = _uiState.asStateFlow()
    
    private val _equipmentList = MutableStateFlow<List<Equipment>>(emptyList())
    val equipmentList: StateFlow<List<Equipment>> = _equipmentList.asStateFlow()
    
    init {
        loadEquipment()
    }
    
    fun loadEquipment() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            getAllEquipmentUseCase()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
                .collect { equipment ->
                    _equipmentList.value = equipment
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
        }
    }
    
    fun updateEquipmentStatus(
        equipmentId: String,
        statusIndicator: EquipmentStatusIndicator,
        conditionDescription: String,
        conditionImageUri: String? = null,
        modifiedBy: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true)
            
            updateEquipmentStatusUseCase(
                equipmentId = equipmentId,
                statusIndicator = statusIndicator,
                conditionDescription = conditionDescription,
                conditionImageUri = conditionImageUri,
                modifiedBy = modifiedBy
            ).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        successMessage = "Equipment status updated successfully"
                    )
                    // Refresh the equipment list
                    loadEquipment()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = exception.message ?: "Failed to update equipment status"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
    
    fun refreshEquipment() {
        loadEquipment()
    }
}

data class EquipmentUiState(
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
