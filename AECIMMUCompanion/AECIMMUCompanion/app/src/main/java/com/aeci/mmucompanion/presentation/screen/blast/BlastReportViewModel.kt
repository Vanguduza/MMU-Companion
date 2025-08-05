package com.aeci.mmucompanion.presentation.screen.blast

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.data.model.*
import com.aeci.mmucompanion.domain.usecase.GenerateBlastReportUseCase
import com.aeci.mmucompanion.domain.usecase.GetBlastReportsUseCase
import com.aeci.mmucompanion.domain.usecase.DeleteBlastReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class BlastReportViewModel @Inject constructor(
    private val generateBlastReportUseCase: GenerateBlastReportUseCase,
    private val getBlastReportsUseCase: GetBlastReportsUseCase,
    private val deleteBlastReportUseCase: DeleteBlastReportUseCase
) : ViewModel() {

    var uiState by mutableStateOf(BlastReportUiState())
        private set

    private val _blastReports = MutableStateFlow<List<BlastReport>>(emptyList())
    val blastReports: StateFlow<List<BlastReport>> = _blastReports.asStateFlow()

    fun loadBlastReports(siteId: String) {
        viewModelScope.launch {
            getBlastReportsUseCase(siteId)
                .catch { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load blast reports"
                    )
                }
                .collect { reports ->
                    _blastReports.value = reports
                    uiState = uiState.copy(isLoading = false)
                }
        }
    }

    fun showGenerateDialog() {
        uiState = uiState.copy(showGenerateDialog = true)
    }

    fun hideGenerateDialog() {
        uiState = uiState.copy(
            showGenerateDialog = false,
            generationState = BlastReportGenerationState()
        )
    }

    fun updateFromDate(date: LocalDate) {
        uiState = uiState.copy(
            generationState = uiState.generationState.copy(fromDate = date)
        )
    }

    fun updateToDate(date: LocalDate) {
        uiState = uiState.copy(
            generationState = uiState.generationState.copy(toDate = date)
        )
    }

    fun updateBcmBlasted(bcm: String) {
        val bcmValue = bcm.toDoubleOrNull() ?: 0.0
        uiState = uiState.copy(
            generationState = uiState.generationState.copy(bcmBlasted = bcmValue)
        )
    }

    fun addWeighbridgeTicket(ticketNumber: String, weight: String, date: LocalDate) {
        val weightValue = weight.toDoubleOrNull() ?: 0.0
        if (weightValue > 0) {
            val newTicket = WeighbridgeTicket(ticketNumber, weightValue, date)
            val updatedTickets = uiState.generationState.weighbridgeTickets + newTicket
            uiState = uiState.copy(
                generationState = uiState.generationState.copy(weighbridgeTickets = updatedTickets)
            )
        }
    }

    fun removeWeighbridgeTicket(index: Int) {
        val updatedTickets = uiState.generationState.weighbridgeTickets.toMutableList()
        if (index in updatedTickets.indices) {
            updatedTickets.removeAt(index)
            uiState = uiState.copy(
                generationState = uiState.generationState.copy(weighbridgeTickets = updatedTickets)
            )
        }
    }

    fun toggleFallbackToBlastHoleLog() {
        uiState = uiState.copy(
            generationState = uiState.generationState.copy(
                fallbackToBlastHoleLog = !uiState.generationState.fallbackToBlastHoleLog
            )
        )
    }

    fun generateBlastReport(siteId: String, generatedBy: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isGenerating = true, error = null)

            val input = BlastReportInput(
                fromDate = uiState.generationState.fromDate,
                toDate = uiState.generationState.toDate,
                bcmBlasted = uiState.generationState.bcmBlasted,
                weighbridgeTickets = uiState.generationState.weighbridgeTickets,
                fallbackToBlastHoleLog = uiState.generationState.fallbackToBlastHoleLog,
                siteId = siteId,
                generatedBy = generatedBy
            )

            generateBlastReportUseCase(input).fold(
                onSuccess = { report ->
                    uiState = uiState.copy(
                        isGenerating = false,
                        showGenerateDialog = false,
                        generatedReport = report,
                        generationState = BlastReportGenerationState()
                    )
                    loadBlastReports(siteId) // Refresh the list
                },
                onFailure = { error ->
                    uiState = uiState.copy(
                        isGenerating = false,
                        error = error.message ?: "Failed to generate blast report"
                    )
                }
            )
        }
    }

    fun deleteBlastReport(reportId: String, siteId: String) {
        viewModelScope.launch {
            deleteBlastReportUseCase(reportId).fold(
                onSuccess = {
                    loadBlastReports(siteId) // Refresh the list
                },
                onFailure = { error ->
                    uiState = uiState.copy(
                        error = error.message ?: "Failed to delete blast report"
                    )
                }
            )
        }
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }

    fun clearGeneratedReport() {
        uiState = uiState.copy(generatedReport = null)
    }
}

data class BlastReportUiState(
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val showGenerateDialog: Boolean = false,
    val generatedReport: BlastReport? = null,
    val error: String? = null,
    val generationState: BlastReportGenerationState = BlastReportGenerationState()
)

data class BlastReportGenerationState(
    val fromDate: LocalDate = LocalDate.now().minusDays(1),
    val toDate: LocalDate = LocalDate.now(),
    val bcmBlasted: Double = 0.0,
    val weighbridgeTickets: List<WeighbridgeTicket> = emptyList(),
    val fallbackToBlastHoleLog: Boolean = false
)
