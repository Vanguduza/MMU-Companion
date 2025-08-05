package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.model.Site
import com.aeci.mmucompanion.domain.model.SiteWithStats
import com.aeci.mmucompanion.domain.usecase.SiteUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SiteManagementUiState(
    val sites: List<SiteWithStats> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedSite: Site? = null
)

@HiltViewModel
class SiteManagementViewModel @Inject constructor(
    private val siteUseCases: SiteUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(SiteManagementUiState())
    val uiState: StateFlow<SiteManagementUiState> = _uiState.asStateFlow()

    init {
        loadSites()
    }

    private fun loadSites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                siteUseCases.getAllSitesWithStats()
                    .collect { sites ->
                        _uiState.update { 
                            it.copy(
                                sites = sites.sortedWith(
                                    compareByDescending<SiteWithStats> { site -> site.site.isHeadOffice }
                                        .thenBy { site -> site.site.name }
                                ),
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load sites: ${e.localizedMessage}"
                    ) 
                }
            }
        }
    }

    fun addSite(site: Site) {
        viewModelScope.launch {
            try {
                siteUseCases.addSite(site)
                // Sites will be automatically updated through the Flow
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to add site: ${e.localizedMessage}")
                }
            }
        }
    }

    fun updateSite(site: Site) {
        viewModelScope.launch {
            try {
                siteUseCases.updateSite(site)
                // Sites will be automatically updated through the Flow
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to update site: ${e.localizedMessage}")
                }
            }
        }
    }

    fun deleteSite(siteId: String) {
        viewModelScope.launch {
            try {
                siteUseCases.deleteSite(siteId)
                // Sites will be automatically updated through the Flow
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to delete site: ${e.localizedMessage}")
                }
            }
        }
    }

    fun toggleSiteStatus(siteId: String) {
        viewModelScope.launch {
            try {
                val currentSite = _uiState.value.sites.find { it.site.id == siteId }?.site
                if (currentSite != null) {
                    val updatedSite = currentSite.copy(
                        isActive = !currentSite.isActive,
                        updatedAt = System.currentTimeMillis()
                    )
                    siteUseCases.updateSite(updatedSite)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to toggle site status: ${e.localizedMessage}")
                }
            }
        }
    }

    fun selectSite(site: Site?) {
        _uiState.update { it.copy(selectedSite = site) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun getActiveSites(): List<Site> {
        return _uiState.value.sites
            .filter { it.site.isActive }
            .map { it.site }
            .sortedWith(
                compareByDescending<Site> { it.isHeadOffice }
                    .thenBy { it.name }
            )
    }

    fun getHeadOffice(): Site? {
        return _uiState.value.sites
            .find { it.site.isHeadOffice && it.site.isActive }
            ?.site
    }

    fun getSiteById(siteId: String): Site? {
        return _uiState.value.sites.find { it.site.id == siteId }?.site
    }

    fun getSiteByCode(siteCode: String): Site? {
        return _uiState.value.sites.find { it.site.code == siteCode }?.site
    }
}
