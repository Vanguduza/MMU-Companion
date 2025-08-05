package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.domain.repository.ReportRepository
import com.aeci.mmucompanion.domain.usecase.GetCurrentUserUseCase
import com.aeci.mmucompanion.presentation.screen.ActiveFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AdminReportsViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdminReportsUiState())
    val uiState: StateFlow<AdminReportsUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    private val _currentFilter = MutableStateFlow(ReportFilter())
    
    init {
        // Combine search query and filter to trigger data loading
        combine(
            _searchQuery.debounce(300),
            _currentFilter
        ) { query, filter ->
            loadReports(query, filter)
        }.launchIn(viewModelScope)
    }
    
    fun loadReports(searchQuery: String = "", filter: ReportFilter = ReportFilter()) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = reportRepository.getReportHistory(
                    page = _uiState.value.pagination.page,
                    limit = 20,
                    filter = filter.copy(searchQuery = searchQuery.takeIf { it.isNotBlank() })
                )
                
                result.fold(
                    onSuccess = { reportHistory ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            reports = reportHistory.reports,
                            pagination = reportHistory.pagination,
                            searchQuery = searchQuery,
                            filter = filter,
                            activeFilters = generateActiveFilters(filter)
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load reports"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }
    
    fun loadStatistics() {
        viewModelScope.launch {
            try {
                val result = reportRepository.getReportStatistics()
                result.fold(
                    onSuccess = { statistics ->
                        _uiState.value = _uiState.value.copy(statistics = statistics)
                    },
                    onFailure = { 
                        // Silently fail statistics loading
                    }
                )
            } catch (e: Exception) {
                // Silently fail statistics loading
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
        _uiState.value = _uiState.value.copy(searchQuery = "")
    }
    
    fun applyFilter(filter: ReportFilter) {
        _currentFilter.value = filter
        _uiState.value = _uiState.value.copy(
            filter = filter,
            pagination = _uiState.value.pagination.copy(page = 1) // Reset to first page
        )
    }
    
    fun removeFilter(activeFilter: ActiveFilter) {
        val currentFilter = _currentFilter.value
        val newFilter = when (activeFilter.key) {
            "reportType" -> currentFilter.copy(reportType = null)
            "format" -> currentFilter.copy(format = null)
            "status" -> currentFilter.copy(status = null)
            "generatedBy" -> currentFilter.copy(generatedBy = null)
            else -> currentFilter
        }
        applyFilter(newFilter)
    }
    
    fun loadPage(page: Int) {
        if (page != _uiState.value.pagination.page) {
            _uiState.value = _uiState.value.copy(
                pagination = _uiState.value.pagination.copy(page = page)
            )
            loadReports(_uiState.value.searchQuery, _uiState.value.filter)
        }
    }
    
    fun refreshReports() {
        loadReports(_uiState.value.searchQuery, _uiState.value.filter)
        loadStatistics()
    }
    
    fun downloadReport(reportId: String) {
        viewModelScope.launch {
            // Add to downloading list
            _uiState.value = _uiState.value.copy(
                downloadingReports = _uiState.value.downloadingReports + reportId
            )
            
            try {
                val result = reportRepository.downloadReport(reportId)
                result.fold(
                    onSuccess = { file ->
                        _uiState.value = _uiState.value.copy(
                            downloadingReports = _uiState.value.downloadingReports - reportId,
                            successMessage = "Report downloaded: ${file.name}"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            downloadingReports = _uiState.value.downloadingReports - reportId,
                            error = "Failed to download report: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    downloadingReports = _uiState.value.downloadingReports - reportId,
                    error = "Download failed: ${e.message}"
                )
            }
        }
    }
    
    fun deleteReport(reportId: String) {
        viewModelScope.launch {
            try {
                val result = reportRepository.deleteReport(reportId)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            reports = _uiState.value.reports.filter { it.id != reportId },
                            successMessage = "Report deleted successfully"
                        )
                        // Refresh statistics after deletion
                        loadStatistics()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to delete report: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Delete failed: ${e.message}"
                )
            }
        }
    }
    
    fun generateReport(
        reportType: ReportType,
        format: ExportFormat,
        parameters: Map<String, Any>? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true)
            
            try {
                val request = ReportGenerationRequest(
                    reportType = reportType,
                    reportTitle = "${reportType.displayName} - ${java.time.LocalDateTime.now()}",
                    format = format,
                    parameters = parameters
                )
                
                val result = reportRepository.generateReport(request)
                result.fold(
                    onSuccess = { generationResult ->
                        if (generationResult.success) {
                            _uiState.value = _uiState.value.copy(
                                isGenerating = false,
                                successMessage = "Report generated successfully"
                            )
                            // Refresh the reports list
                            refreshReports()
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isGenerating = false,
                                error = generationResult.error ?: "Report generation failed"
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isGenerating = false,
                            error = "Failed to generate report: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    error = "Generation failed: ${e.message}"
                )
            }
        }
    }
    
    fun exportReportsBundle(reportIds: List<String>, bundleName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            
            try {
                val result = reportRepository.exportReportsBundle(reportIds, bundleName)
                result.fold(
                    onSuccess = { bundleFile ->
                        _uiState.value = _uiState.value.copy(
                            isExporting = false,
                            successMessage = "Reports bundle exported: ${bundleFile.name}"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isExporting = false,
                            error = "Failed to export bundle: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = "Export failed: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
    
    private fun generateActiveFilters(filter: ReportFilter): List<ActiveFilter> {
        val filters = mutableListOf<ActiveFilter>()
        
        filter.reportType?.let { type ->
            filters.add(ActiveFilter("reportType", "Type: ${type.displayName}"))
        }
        
        filter.format?.let { format ->
            filters.add(ActiveFilter("format", "Format: ${format.name}"))
        }
        
        filter.status?.let { status ->
            filters.add(ActiveFilter("status", "Status: ${status.name}"))
        }
        
        filter.generatedBy?.let { userId ->
            filters.add(ActiveFilter("generatedBy", "Generated by: $userId"))
        }
        
        filter.startDate?.let { 
            filters.add(ActiveFilter("dateRange", "Date range applied"))
        }
        
        return filters
    }
}

data class AdminReportsUiState(
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val isExporting: Boolean = false,
    val reports: List<Report> = emptyList(),
    val statistics: ReportStatistics? = null,
    val pagination: PaginationInfo = PaginationInfo(1, 20, 0, 0),
    val searchQuery: String = "",
    val filter: ReportFilter = ReportFilter(),
    val activeFilters: List<ActiveFilter> = emptyList(),
    val downloadingReports: Set<String> = emptySet(),
    val selectedReports: Set<String> = emptySet(),
    val error: String? = null,
    val successMessage: String? = null
) 
