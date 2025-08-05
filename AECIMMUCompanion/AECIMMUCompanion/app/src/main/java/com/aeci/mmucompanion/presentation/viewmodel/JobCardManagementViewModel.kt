package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.domain.repository.JobCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobCardManagementViewModel @Inject constructor(
    private val jobCardRepository: JobCardRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(JobCardManagementUiState())
    val uiState: StateFlow<JobCardManagementUiState> = _uiState.asStateFlow()
    
    init {
        loadJobCards()
        loadStatistics()
    }
    
    private fun loadJobCards() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val jobCards = if (_uiState.value.filters == JobCardFilters()) {
                    // Load all job cards if no filters
                    jobCardRepository.getAllJobCards()
                } else {
                    // Apply filters
                    jobCardRepository.getJobCardsByFilters(_uiState.value.filters)
                }
                
                // Apply search filter
                val filteredJobCards = if (_uiState.value.searchQuery.isNotEmpty()) {
                    jobCards.filter { jobCard ->
                        jobCard.title.contains(_uiState.value.searchQuery, ignoreCase = true) ||
                        jobCard.description.contains(_uiState.value.searchQuery, ignoreCase = true) ||
                        jobCard.equipmentName.contains(_uiState.value.searchQuery, ignoreCase = true) ||
                        jobCard.siteLocation.contains(_uiState.value.searchQuery, ignoreCase = true)
                    }
                } else {
                    jobCards
                }
                
                _uiState.value = _uiState.value.copy(
                    jobCards = filteredJobCards,
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load job cards: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                val statistics = jobCardRepository.getJobCardStatistics()
                _uiState.value = _uiState.value.copy(statistics = statistics)
            } catch (e: Exception) {
                // Statistics loading failed, but don't block the UI
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadJobCards()
    }
    
    fun updateFilters(filters: JobCardFilters) {
        _uiState.value = _uiState.value.copy(filters = filters)
        loadJobCards()
    }
    
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(filters = JobCardFilters())
        loadJobCards()
    }
    
    fun refreshData() {
        loadJobCards()
        loadStatistics()
    }
    
    fun bulkAssignJobCards(jobCardIds: List<String>, assignedTo: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val success = jobCardRepository.bulkAssignJobCards(jobCardIds, assignedTo)
                
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Successfully assigned ${jobCardIds.size} job cards",
                        isLoading = false
                    )
                    loadJobCards()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to assign job cards",
                        isLoading = false
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to assign job cards: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun bulkUpdateStatus(jobCardIds: List<String>, status: JobCardStatus) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val success = jobCardRepository.bulkUpdateStatus(jobCardIds, status)
                
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Successfully updated status for ${jobCardIds.size} job cards",
                        isLoading = false
                    )
                    loadJobCards()
                    loadStatistics()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update job card status",
                        isLoading = false
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update job card status: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun bulkDeleteJobCards(jobCardIds: List<String>) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val success = jobCardRepository.bulkDeleteJobCards(jobCardIds)
                
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Successfully deleted ${jobCardIds.size} job cards",
                        isLoading = false
                    )
                    loadJobCards()
                    loadStatistics()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to delete job cards",
                        isLoading = false
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete job cards: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun exportJobCards(jobCardIds: List<String>) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val exportRequest = JobCardExportRequest(
                    jobCardIds = jobCardIds,
                    format = ExportFormat.PDF,
                    includeAttachments = true
                )
                
                val exportPath = jobCardRepository.exportJobCards(exportRequest)
                
                _uiState.value = _uiState.value.copy(
                    successMessage = "Job cards exported successfully to: $exportPath",
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to export job cards: ${e.message}",
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

data class JobCardManagementUiState(
    val isLoading: Boolean = false,
    val jobCards: List<JobCard> = emptyList(),
    val statistics: JobCardStatistics = JobCardStatistics(
        totalJobCards = 0,
        pendingJobCards = 0,
        inProgressJobCards = 0,
        completedJobCards = 0,
        cancelledJobCards = 0,
        overdueJobCards = 0,
        averageCompletionTime = null,
        totalHours = 0.0,
        totalCost = 0.0,
        completionRate = 0.0
    ),
    val searchQuery: String = "",
    val filters: JobCardFilters = JobCardFilters(),
    val error: String? = null,
    val successMessage: String? = null
) 
