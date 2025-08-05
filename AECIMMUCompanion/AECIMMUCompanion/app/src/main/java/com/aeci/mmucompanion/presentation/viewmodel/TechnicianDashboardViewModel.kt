package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.domain.usecase.GetCurrentUserUseCase
import com.aeci.mmucompanion.domain.usecase.GetAllEquipmentUseCase
import com.aeci.mmucompanion.domain.usecase.form.GetFormsByUserUseCase
import com.aeci.mmucompanion.domain.usecase.jobcard.GetJobCardsByUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDate

@HiltViewModel
class TechnicianDashboardViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getAllEquipmentUseCase: GetAllEquipmentUseCase,
    private val getFormsByUserUseCase: GetFormsByUserUseCase,
    private val getJobCardsByUserUseCase: GetJobCardsByUserUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TechnicianDashboardUiState())
    val uiState: StateFlow<TechnicianDashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Load current user
                val currentUser = getCurrentUserUseCase() ?: throw IllegalStateException("No current user found")
                
                // Load equipment list
                val equipmentList = getAllEquipmentUseCase().first()
                
                // Load user's job cards
                val jobCards = getJobCardsByUserUseCase(currentUser.id)
                
                // Load user's recent forms
                val forms = getFormsByUserUseCase(currentUser.id)
                
                // Generate sample timesheet summary
                val timesheetSummary = TimesheetSummary(
                    currentWeekHours = 35.5,
                    currentMonthHours = 142.0,
                    pendingTimesheets = 1,
                    lastSubmittedDate = LocalDate.now().minusWeeks(1)
                )
                
                // Calculate equipment status counts
                val equipmentStatus = EquipmentStatusCounts(
                    operational = equipmentList.filter { it.status == EquipmentStatus.OPERATIONAL }.size,
                    maintenance = equipmentList.filter { it.status == EquipmentStatus.MAINTENANCE }.size,
                    breakdown = equipmentList.filter { it.status == EquipmentStatus.BREAKDOWN }.size
                )
                
                _uiState.value = _uiState.value.copy(
                    currentUser = currentUser,
                    equipmentList = equipmentList,
                    equipmentStatus = equipmentStatus,
                    myJobCards = jobCards,
                    recentForms = forms,
                    timesheetSummary = timesheetSummary,
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load dashboard data: ${e.message}",
                    isLoading = false
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
}

data class TechnicianDashboardUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val equipmentList: List<Equipment> = emptyList(),
    val equipmentStatus: EquipmentStatusCounts = EquipmentStatusCounts(),
    val myJobCards: List<JobCard> = emptyList(),
    val recentForms: List<FormData> = emptyList(),
    val timesheetSummary: TimesheetSummary? = null,
    val error: String? = null
)

data class EquipmentStatusCounts(
    val operational: Int = 0,
    val maintenance: Int = 0,
    val breakdown: Int = 0
) 
