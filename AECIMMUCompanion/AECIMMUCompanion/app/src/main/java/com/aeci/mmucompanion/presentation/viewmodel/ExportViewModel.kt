package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.model.ExportFormat
import com.aeci.mmucompanion.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ExportUiState(
    val isExporting: Boolean = false,
    val exportProgress: String = "",
    val error: String? = null,
    val successMessage: String? = null,
    val exportHistory: List<ExportHistoryItem> = emptyList()
)

data class ExportHistoryItem(
    val id: String,
    val filename: String,
    val category: String,
    val format: String,
    val createdDate: String,
    val fileSize: String
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exportFormsUseCase: ExportFormsUseCase,
    private val exportEquipmentUseCase: ExportEquipmentUseCase,
    private val exportUsersUseCase: ExportUsersUseCase,
    private val exportReportsUseCase: ExportReportsUseCase,
    private val exportMaintenanceUseCase: ExportMaintenanceUseCase,
    private val exportAuditLogUseCase: ExportAuditLogUseCase,
    private val getExportHistoryUseCase: GetExportHistoryUseCase,
    private val downloadExportUseCase: DownloadExportUseCase,
    private val shareExportUseCase: ShareExportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    init {
        // loadExportHistory() // Commented out until domain model exists
    }

    fun exportForms(format: ExportFormat) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportProgress = "Preparing forms data...", error = null) }
            try {
                // TODO: Need a way to select form IDs from the UI
                val result = exportFormsUseCase(emptyList(), format)
                result.fold(
                    onSuccess = { path ->
                        _uiState.update { it.copy(isExporting = false, successMessage = "Forms exported to $path") }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isExporting = false, error = error.message ?: "Export failed") }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, error = e.message) }
            }
        }
    }

    fun exportEquipment(format: ExportFormat) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportProgress = "Preparing equipment data...", error = null) }
            try {
                val result = exportEquipmentUseCase(emptyList(), format)
                 result.fold(
                    onSuccess = { path ->
                        _uiState.update { it.copy(isExporting = false, successMessage = "Equipment exported to $path") }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isExporting = false, error = error.message ?: "Export failed") }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, error = e.message) }
            }
        }
    }

    fun exportUsers(format: ExportFormat) {
        viewModelScope.launch {
             _uiState.update { it.copy(isExporting = true, exportProgress = "Preparing user data...", error = null) }
            try {
                val result = exportUsersUseCase(emptyList(), format)
                 result.fold(
                    onSuccess = { path ->
                        _uiState.update { it.copy(isExporting = false, successMessage = "Users exported to $path") }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isExporting = false, error = error.message ?: "Export failed") }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, error = e.message) }
            }
        }
    }

    fun exportReports(format: ExportFormat) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportProgress = "Generating reports...", error = null) }
            try {
                val result = exportReportsUseCase("SUMMARY", format)
                result.fold(
                    onSuccess = { path ->
                        _uiState.update { it.copy(isExporting = false, successMessage = "Reports exported to $path") }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isExporting = false, error = error.message ?: "Export failed") }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, error = e.message) }
            }
        }
    }

    fun exportMaintenance(format: ExportFormat) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportProgress = "Preparing maintenance data...", error = null) }
            try {
                val result = exportMaintenanceUseCase(emptyList(), format)
                result.fold(
                    onSuccess = { path ->
                        _uiState.update { it.copy(isExporting = false, successMessage = "Maintenance data exported to $path") }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isExporting = false, error = error.message ?: "Export failed") }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, error = e.message) }
            }
        }
    }

    fun exportAuditLog(format: ExportFormat) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportProgress = "Preparing audit log...", error = null) }
            try {
                val result = exportAuditLogUseCase(
                    format = format,
                    startDate = System.currentTimeMillis() - 86400000 * 30, // Last 30 days
                    endDate = System.currentTimeMillis()
                )
                result.fold(
                    onSuccess = { path ->
                        _uiState.update { it.copy(isExporting = false, successMessage = "Audit log exported to $path") }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isExporting = false, error = error.message ?: "Export failed") }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, error = e.message) }
            }
        }
    }

    fun downloadExport(exportId: String) {
        viewModelScope.launch {
            try {
                downloadExportUseCase(exportId)
                _uiState.update { it.copy(successMessage = "Download started") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to download export") }
            }
        }
    }

    fun shareExport(exportId: String) {
        viewModelScope.launch {
            try {
                shareExportUseCase(exportId)
                _uiState.update { it.copy(successMessage = "Share initiated") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to share export") }
            }
        }
    }

    /*
    private fun loadExportHistory() {
        viewModelScope.launch {
            try {
                val history = getExportHistoryUseCase()
                val exportHistory = history.map { export ->
                    ExportHistoryItem(
                        id = export.id,
                        filename = export.filename,
                        category = export.category,
                        format = mapToExportFormat(export.format),
                        createdDate = formatDate(export.createdDate),
                        fileSize = formatFileSize(export.fileSize)
                    )
                }
                
                _uiState.value = _uiState.value.copy(exportHistory = exportHistory)
            } catch (e: Exception) {
                // Silently fail for export history loading
            }
        }
    }
    */
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearSuccessMessage() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _uiState.update { it.copy(successMessage = null) }
        }
    }
}
