package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.model.FormData
import com.aeci.mmucompanion.domain.model.FormStatus
import com.aeci.mmucompanion.domain.model.FormType
import com.aeci.mmucompanion.domain.usecase.*
import com.aeci.mmucompanion.presentation.screen.Issue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val formRepository: com.aeci.mmucompanion.domain.repository.FormRepository,
    private val equipmentRepository: com.aeci.mmucompanion.domain.repository.EquipmentRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = getCurrentUserUseCase()
                val forms = formRepository.getAllFormsByUser(currentUser?.id ?: "current_user")
                val equipment = equipmentRepository.getAllActiveEquipment().first()
                
                val formStats = calculateFormStats(forms)
                val equipmentStats = calculateEquipmentStats(equipment)
                val equipmentStatus = calculateEquipmentStatusOverview(equipment)
                val (productionForms, maintenanceForms) = categorizeFormsByType(forms)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentUser = currentUser,
                    recentForms = forms.take(5),
                    formStats = formStats,
                    equipmentStats = equipmentStats,
                    equipmentStatus = equipmentStatus,
                    productionForms = productionForms,
                    maintenanceForms = maintenanceForms,
                    totalEquipment = equipment.size,
                    operationalEquipment = equipment.count { it.status == com.aeci.mmucompanion.domain.model.EquipmentStatus.OPERATIONAL }
                )
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to load dashboard data"
                )
            }
        }
    }
    
    fun refreshData() {
        loadDashboardData()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun calculateFormStats(forms: List<FormData>): FormStats {
        val today = LocalDateTime.now()
        val yesterday = today.minusDays(1)
        
        return FormStats(
            totalForms = forms.size,
            completedForms = forms.count { it.status == FormStatus.COMPLETED },
            pendingForms = forms.count { it.status == FormStatus.IN_PROGRESS },
            todayForms = forms.count { it.createdAt.isAfter(yesterday) },
            formsByType = forms.groupBy { it.formType }.mapValues { it.value.size }
        )
    }
    
    private fun calculateEquipmentStats(equipment: List<com.aeci.mmucompanion.domain.model.Equipment>): EquipmentStats {
        return EquipmentStats(
            totalEquipment = equipment.size,
            operationalEquipment = equipment.count { it.status == com.aeci.mmucompanion.domain.model.EquipmentStatus.OPERATIONAL },
            maintenanceEquipment = equipment.count { it.status == com.aeci.mmucompanion.domain.model.EquipmentStatus.MAINTENANCE },
            offlineEquipment = equipment.count { it.status == com.aeci.mmucompanion.domain.model.EquipmentStatus.OFFLINE },
            equipmentByType = equipment.groupBy { it.type }.mapValues { it.value.size }
        )
    }
    
    private fun calculateEquipmentStatusOverview(equipment: List<com.aeci.mmucompanion.domain.model.Equipment>): EquipmentStatusOverview {
        return EquipmentStatusOverview(
            operational = equipment.count { it.status == com.aeci.mmucompanion.domain.model.EquipmentStatus.OPERATIONAL },
            warning = equipment.count { it.status == com.aeci.mmucompanion.domain.model.EquipmentStatus.MAINTENANCE },
            critical = equipment.count { it.status == com.aeci.mmucompanion.domain.model.EquipmentStatus.OFFLINE }
        )
    }
    
    private fun categorizeFormsByType(forms: List<FormData>): Pair<Int, Int> {
        val productionTypes = setOf(
            FormType.MMU_DAILY_LOG,
            FormType.BLAST_HOLE_LOG,
            FormType.AVAILABILITY_UTILIZATION
        )
        
        val maintenanceTypes = setOf(
            FormType.MAINTENANCE,
            FormType.INSPECTION,
            FormType.EQUIPMENT_CHECK,
            FormType.SAFETY
        )
        
        val productionCount = forms.count { it.formType in productionTypes }
        val maintenanceCount = forms.count { it.formType in maintenanceTypes }
        
        return Pair(productionCount, maintenanceCount)
    }
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val currentUser: com.aeci.mmucompanion.domain.model.User? = null,
    val recentForms: List<FormData> = emptyList(),
    val recentIssues: List<Issue> = emptyList(),
    val formStats: FormStats = FormStats(),
    val equipmentStats: EquipmentStats = EquipmentStats(),
    val equipmentStatus: EquipmentStatusOverview = EquipmentStatusOverview(),
    val equipmentByStatus: Map<String, List<com.aeci.mmucompanion.domain.model.Equipment>> = emptyMap(),
    val productionForms: Int = 0,
    val maintenanceForms: Int = 0,
    val totalEquipment: Int = 0,
    val operationalEquipment: Int = 0,
    val error: String? = null
)

data class FormStats(
    val totalForms: Int = 0,
    val completedForms: Int = 0,
    val pendingForms: Int = 0,
    val todayForms: Int = 0,
    val formsByType: Map<FormType, Int> = emptyMap()
)

data class EquipmentStats(
    val totalEquipment: Int = 0,
    val operationalEquipment: Int = 0,
    val maintenanceEquipment: Int = 0,
    val offlineEquipment: Int = 0,
    val equipmentByType: Map<com.aeci.mmucompanion.domain.model.EquipmentType, Int> = emptyMap()
)

data class EquipmentStatusOverview(
    val operational: Int = 0,  // Green
    val warning: Int = 0,      // Amber  
    val critical: Int = 0      // Red
)
