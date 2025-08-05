package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.model.Equipment
import com.aeci.mmucompanion.domain.model.EquipmentGroup
import com.aeci.mmucompanion.domain.model.EquipmentStatus
import com.aeci.mmucompanion.domain.model.EquipmentType
import com.aeci.mmucompanion.domain.model.Site
import com.aeci.mmucompanion.domain.repository.EquipmentRepository
import com.aeci.mmucompanion.domain.repository.SiteFilteringService
import com.aeci.mmucompanion.domain.repository.SiteRepository
import com.aeci.mmucompanion.domain.usecase.EquipmentGroupUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EquipmentScreenState(
    val equipment: List<Equipment> = emptyList(),
    val sites: List<Site> = emptyList(),
    val equipmentGroups: List<EquipmentGroup> = emptyList(),
    val selectedSiteId: String? = null,
    val selectedType: String = "All",
    val selectedStatus: String = "All",  
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUserSite: Site? = null
)

@HiltViewModel
class EquipmentScreenViewModel @Inject constructor(
    private val siteRepository: SiteRepository,
    private val equipmentRepository: EquipmentRepository,
    private val siteFilteringService: SiteFilteringService,
    private val equipmentGroupUseCases: EquipmentGroupUseCases
) : ViewModel() {
    
    private val _state = MutableStateFlow(EquipmentScreenState())
    val state: StateFlow<EquipmentScreenState> = _state.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    private val _selectedType = MutableStateFlow("All")
    private val _selectedStatus = MutableStateFlow("All")
    private val _selectedSiteId = MutableStateFlow<String?>(null)
    
    init {
        loadInitialData()
        observeFilters()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                // Load sites
                siteRepository.getAllSites().collect { sites ->
                    _state.value = _state.value.copy(sites = sites)
                    
                    // Set current user's site (mock - in real app get from user session)
                    val currentUserSite = sites.firstOrNull { it.id == "site_001" }
                    _state.value = _state.value.copy(currentUserSite = currentUserSite)
                    _selectedSiteId.value = currentUserSite?.id
                }
                
                // Load equipment groups  
                equipmentGroupUseCases.getAllEquipmentGroups().collect { groups ->
                    _state.value = _state.value.copy(equipmentGroups = groups)
                }
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to load data: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    private fun observeFilters() {
        viewModelScope.launch {
            combine(
                _searchQuery,
                _selectedType,
                _selectedStatus,
                _selectedSiteId
            ) { query, type, status, siteId ->
                loadEquipment(query, type, status, siteId)
            }.collect()
        }
    }
    
    private suspend fun loadEquipment(
        searchQuery: String,
        selectedType: String,
        selectedStatus: String,
        selectedSiteId: String?
    ) {
        try {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            // Get equipment for the current site
            val equipment = if (selectedSiteId != null) {
                equipmentRepository.getEquipmentAtSite(selectedSiteId)
            } else {
                // Mock data for demonstration
                getMockEquipment()
            }
            
            // Apply filters
            val filteredEquipment = equipment
                .filter { eq ->
                    // Site filter (already applied above)
                    val siteMatch = selectedSiteId == null || eq.siteId == selectedSiteId
                    
                    // Type filter
                    val typeMatch = selectedType == "All" || eq.type.name == selectedType
                    
                    // Status filter  
                    val statusMatch = selectedStatus == "All" || eq.status.name == selectedStatus
                    
                    // Search query filter
                    val searchMatch = searchQuery.isBlank() || 
                        eq.name.contains(searchQuery, ignoreCase = true) ||
                        eq.serialNumber.contains(searchQuery, ignoreCase = true) ||
                        eq.location.contains(searchQuery, ignoreCase = true)
                    
                    siteMatch && typeMatch && statusMatch && searchMatch
                }
            
            _state.value = _state.value.copy(
                equipment = filteredEquipment,
                searchQuery = searchQuery,
                selectedType = selectedType,
                selectedStatus = selectedStatus,
                selectedSiteId = selectedSiteId,
                isLoading = false
            )
            
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = "Failed to load equipment: ${e.message}",
                isLoading = false
            )
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun updateSelectedType(type: String) {
        _selectedType.value = type
    }
    
    fun updateSelectedStatus(status: String) {
        _selectedStatus.value = status
    }
    
    fun updateSelectedSite(siteId: String?) {
        _selectedSiteId.value = siteId
    }
    
    fun refreshData() {
        loadInitialData()
    }
    
    private fun getMockEquipment(): List<Equipment> {
        return listOf(
            Equipment(
                id = "MMU001",
                name = "Mobile Mining Unit 001",
                type = EquipmentType.MMU,
                model = "MMU-2024",
                serialNumber = "SN001234",
                location = "Zone A",
                siteId = "site_001",
                status = EquipmentStatus.OPERATIONAL,
                manufacturer = "AECI",
                installationDate = System.currentTimeMillis() - 31536000000L,
                lastMaintenanceDate = System.currentTimeMillis() - 86400000L,
                nextMaintenanceDate = System.currentTimeMillis() + 604800000L,
                specifications = mapOf("capacity" to "500 t/h", "power" to "200 kW"),
                operatingParameters = mapOf("speed" to "1800 rpm", "temperature" to "65°C"),
                imageUri = null,
                conditionImageUri = null
            ),
            Equipment(
                id = "PUMP002", 
                name = "Hydraulic Pump 002",
                type = EquipmentType.PUMP,
                model = "HP-2023",
                serialNumber = "SN005678",
                location = "Zone B",
                siteId = "site_001",
                status = EquipmentStatus.MAINTENANCE,
                manufacturer = "PumpCorp",
                installationDate = System.currentTimeMillis() - 15768000000L,
                lastMaintenanceDate = System.currentTimeMillis() - 172800000L,
                nextMaintenanceDate = System.currentTimeMillis() - 86400000L,
                specifications = mapOf("capacity" to "200 L/min", "pressure" to "350 bar"),
                operatingParameters = mapOf("flow_rate" to "180 L/min", "pressure" to "320 bar"),
                imageUri = null,
                conditionImageUri = null
            ),
            Equipment(
                id = "EXTINGUISHER003",
                name = "Fire Extinguisher 003",
                type = EquipmentType.OTHER,
                model = "FE-ABC-2024",
                serialNumber = "SN009012",
                location = "Zone C",
                siteId = "site_001",
                status = EquipmentStatus.OPERATIONAL,
                manufacturer = "SafetyFirst",
                installationDate = System.currentTimeMillis() - 7776000000L,
                lastMaintenanceDate = System.currentTimeMillis() - 259200000L,
                nextMaintenanceDate = System.currentTimeMillis() + 2592000000L,
                specifications = mapOf("type" to "ABC Dry Chemical", "capacity" to "9 kg"),
                operatingParameters = mapOf("pressure" to "12 bar", "weight" to "8.5 kg"),
                imageUri = null,
                conditionImageUri = null
            )
        )
    }
}
