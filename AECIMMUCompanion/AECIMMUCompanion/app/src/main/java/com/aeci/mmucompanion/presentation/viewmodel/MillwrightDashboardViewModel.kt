package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.model.Equipment
import com.aeci.mmucompanion.domain.usecase.GetAllEquipmentUseCase
import com.aeci.mmucompanion.domain.usecase.GenerateBlastReportUseCase
import com.aeci.mmucompanion.data.model.BlastReportInput
import com.aeci.mmucompanion.data.model.WeighbridgeTicket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.aeci.mmucompanion.domain.model.FormData
import com.aeci.mmucompanion.domain.repository.FormRepository
import com.aeci.mmucompanion.domain.model.FormType
import java.time.LocalDate

@HiltViewModel
class MillwrightDashboardViewModel @Inject constructor(
    private val getAllEquipmentUseCase: GetAllEquipmentUseCase,
    private val generateBlastReportUseCase: GenerateBlastReportUseCase,
    private val formRepository: FormRepository // Inject FormRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MillwrightDashboardUiState())
    val uiState: StateFlow<MillwrightDashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadEquipmentList()
    }

    private fun loadEquipmentList() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getAllEquipmentUseCase()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
                .collect { equipment ->
                    _uiState.value = _uiState.value.copy(isLoading = false, equipmentList = equipment)
                }
        }
    }

    fun onGenerateTimesheetClicked() {
        // TODO: Navigate to Timesheet form screen
    }

    fun onGenerateUorClicked() {
        // TODO: Navigate to UOR form screen
    }

    fun onGenerateBlastReportClicked() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBlastDialogLoading = true, showBlastReportDialog = true)
            try {
                // Using a placeholder userId - in real implementation, get from auth state
                val userId = "current_user_id"
                
                val blastHoleLogs = formRepository.getFormsByType(FormType.BLAST_HOLE_LOG)
                val dailyLogs = formRepository.getFormsByType(FormType.MMU_DAILY_LOG)
                val qualityReports = formRepository.getFormsByType(FormType.MMU_QUALITY_REPORT)
                val pretasks = formRepository.getFormsByType(FormType.PRETASK)
                
                _uiState.value = _uiState.value.copy(
                    isBlastDialogLoading = false,
                    blastHoleLogs = blastHoleLogs,
                    dailyLogs = dailyLogs,
                    qualityReports = qualityReports,
                    pretasks = pretasks
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isBlastDialogLoading = false, error = e.message)
            }
        }
    }

    fun onDismissBlastReportDialog() {
        _uiState.value = _uiState.value.copy(showBlastReportDialog = false)
    }
    
    fun generateBlastReport(blastHoleLogId: String, dailyLogId: String, qualityReportId: String, pretaskId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBlastDialogLoading = true)
            
            // Create a basic blast report input
            val input = BlastReportInput(
                fromDate = LocalDate.now().minusDays(1),
                toDate = LocalDate.now(),
                bcmBlasted = 0.0, // This would be calculated from forms
                weighbridgeTickets = emptyList(),
                fallbackToBlastHoleLog = true,
                siteId = "default_site", // This would come from user context
                generatedBy = "current_user" // This would come from auth state
            )
            
            generateBlastReportUseCase(input).fold(
                onSuccess = { report ->
                    _uiState.value = _uiState.value.copy(
                        isBlastDialogLoading = false, 
                        showBlastReportDialog = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isBlastDialogLoading = false, 
                        error = e.message
                    )
                }
            )
        }
    }
}

data class MillwrightDashboardUiState(
    val isLoading: Boolean = false,
    val equipmentList: List<Equipment> = emptyList(),
    val error: String? = null,
    val showBlastReportDialog: Boolean = false,
    val isBlastDialogLoading: Boolean = false,
    val blastHoleLogs: List<FormData> = emptyList(),
    val dailyLogs: List<FormData> = emptyList(),
    val qualityReports: List<FormData> = emptyList(),
    val pretasks: List<FormData> = emptyList()
)
