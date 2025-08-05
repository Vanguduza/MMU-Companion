package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.model.Equipment
import com.aeci.mmucompanion.domain.repository.EquipmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EquipmentCategory(
    val id: String,
    val name: String
)

data class EquipmentManagementUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val categories: List<EquipmentCategory> = emptyList(),
    val selectedCategory: EquipmentCategory? = null,
    val equipment: List<Equipment> = emptyList(),
    val filteredEquipment: List<Equipment> = emptyList(),
    val selectedEquipment: Equipment? = null,
    val showAddCategoryDialog: Boolean = false,
    val showEquipmentDetail: Boolean = false
)

@HiltViewModel
class EquipmentManagementViewModel @Inject constructor(
    private val equipmentRepository: EquipmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EquipmentManagementUiState())
    val uiState: StateFlow<EquipmentManagementUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Load default categories
                val defaultCategories = listOf(
                    EquipmentCategory("pumps", "PUMPS"),
                    EquipmentCategory("mmus", "MMUs")
                )
                
                // For now, use only default categories
                // TODO: Implement custom categories in repository
                val allCategories = defaultCategories
                
                // Load all equipment
                val allEquipment = equipmentRepository.getAllActiveEquipment().first()
                
                // Set default selected category to PUMPS
                val defaultSelected = defaultCategories.first()
                val filtered = filterEquipmentByCategory(allEquipment, defaultSelected)
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        categories = allCategories,
                        selectedCategory = defaultSelected,
                        equipment = allEquipment,
                        filteredEquipment = filtered
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load equipment data"
                    )
                }
            }
        }
    }

    fun onCategorySelected(category: EquipmentCategory) {
        _uiState.update {
            val filtered = filterEquipmentByCategory(it.equipment, category)
            it.copy(
                selectedCategory = category,
                filteredEquipment = filtered
            )
        }
    }

    fun onShowAddCategoryDialog() {
        _uiState.update { it.copy(showAddCategoryDialog = true) }
    }

    fun onDismissAddCategoryDialog() {
        _uiState.update { it.copy(showAddCategoryDialog = false) }
    }

    fun addCategory(categoryName: String) {
        // TODO: Implement custom category creation in repository
        _uiState.update {
            it.copy(
                error = "Custom category creation will be implemented soon"
            )
        }
    }

    fun onViewEquipment(equipment: Equipment) {
        _uiState.update {
            it.copy(
                selectedEquipment = equipment,
                showEquipmentDetail = true
            )
        }
    }

    fun onDismissEquipmentDetail() {
        _uiState.update {
            it.copy(
                selectedEquipment = null,
                showEquipmentDetail = false
            )
        }
    }

    fun onEditEquipment(equipment: Equipment) {
        // TODO: Navigate to edit screen
        // For now, just show a placeholder
        _uiState.update {
            it.copy(
                error = "Edit functionality will be implemented soon"
            )
        }
    }

    fun onDeleteEquipment(equipment: Equipment) {
        viewModelScope.launch {
            try {
                val result = equipmentRepository.deleteEquipment(equipment.id)
                if (result.isSuccess) {
                    // Reload data to reflect deletion
                    loadInitialData()
                } else {
                    _uiState.update {
                        it.copy(
                            error = result.exceptionOrNull()?.message ?: "Failed to delete equipment"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to delete equipment"
                    )
                }
            }
        }
    }

    fun onUpdateEquipmentStatus(equipment: Equipment) {
        // TODO: Implement equipment status update with proper enum handling
        _uiState.update {
            it.copy(
                error = "Equipment status update will be implemented soon"
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun filterEquipmentByCategory(equipment: List<Equipment>, category: EquipmentCategory): List<Equipment> {
        return when (category.name) {
            "PUMPS" -> equipment.filter { it.name.contains("PUMP", ignoreCase = true) }
            "MMUs" -> equipment.filter { it.name.contains("MMU", ignoreCase = true) }
            else -> equipment // Return all equipment for custom categories for now
        }
    }
} 
