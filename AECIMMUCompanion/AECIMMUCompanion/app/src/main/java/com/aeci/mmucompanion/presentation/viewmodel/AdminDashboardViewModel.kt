package com.aeci.mmucompanion.presentation.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.model.ActivityType
import com.aeci.mmucompanion.domain.model.HealthStatus
import com.aeci.mmucompanion.domain.model.SystemActivity
import com.aeci.mmucompanion.domain.model.Equipment
import com.aeci.mmucompanion.domain.model.User
import com.aeci.mmucompanion.domain.model.Priority
import com.aeci.mmucompanion.domain.repository.EquipmentRepository
import com.aeci.mmucompanion.domain.repository.UserRepository
import com.aeci.mmucompanion.domain.usecase.GetRecentActivitiesUseCase
import com.aeci.mmucompanion.domain.usecase.GetSystemHealthUseCase
import com.aeci.mmucompanion.domain.usecase.GetSystemStatsUseCase
import com.aeci.mmucompanion.domain.usecase.CreateWorkOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class AdminDashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val activeUsers: Int = 0,
    val formsToday: Int = 0,
    val systemLoad: Float = 0f,
    val storageUsed: Float = 0f,
    val lastBackupTime: String = "N/A",
    val recentActivities: List<ActivityLog> = emptyList(),
    val databaseHealth: HealthStatus = HealthStatus.UNKNOWN,
    val syncServiceHealth: HealthStatus = HealthStatus.UNKNOWN,
    val storageHealth: HealthStatus = HealthStatus.UNKNOWN,
    val networkHealth: HealthStatus = HealthStatus.UNKNOWN,
    val showCreateTaskDialog: Boolean = false,
    val isTaskCreationLoading: Boolean = false,
    val showCreateTechnicianDialog: Boolean = false,
    val isTechnicianCreationLoading: Boolean = false,
    val sites: List<String> = emptyList(),
    val selectedSite: String? = null,
    val technicians: List<User> = emptyList(),
    val equipment: List<Equipment> = emptyList()
)

data class ActivityLog(
    val id: String,
    val description: String,
    val timestamp: String,
    val icon: ImageVector,
    val color: Color
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val getSystemStatsUseCase: GetSystemStatsUseCase,
    private val getRecentActivitiesUseCase: GetRecentActivitiesUseCase,
    private val getSystemHealthUseCase: GetSystemHealthUseCase,
    private val equipmentRepository: EquipmentRepository,
    private val userRepository: UserRepository,
    private val createWorkOrderUseCase: CreateWorkOrderUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }
    
    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val systemStats = getSystemStatsUseCase()
                val recentActivities = getRecentActivitiesUseCase()
                val systemHealth = getSystemHealthUseCase()
                
                _uiState.update {
                    it.copy(
                    isLoading = false,
                    activeUsers = systemStats.activeUsers,
                    formsToday = systemStats.formsToday,
                    systemLoad = systemStats.systemLoad,
                    storageUsed = systemStats.storageUsed,
                    lastBackupTime = systemStats.lastBackupTime,
                        recentActivities = mapToActivityLogs(recentActivities),
                    databaseHealth = systemHealth.databaseHealth,
                    syncServiceHealth = systemHealth.syncServiceHealth,
                    storageHealth = systemHealth.storageHealth,
                    networkHealth = systemHealth.networkHealth
                )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load admin dashboard data"
                )
                }
            }
        }
    }
    
    fun refreshData() {
        loadDashboardData()
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onNavigateToEquipmentManagement() {
        // TODO: Implement navigation to Equipment Management screen
        _uiState.update {
            it.copy(
                error = "Navigation to Equipment Management screen will be implemented"
            )
        }
    }

    fun onShowCreateTaskDialog() {
        viewModelScope.launch {
            val allEquipment = equipmentRepository.getAllActiveEquipment().first()
            val sites = allEquipment.map { it.location }.distinct()
            _uiState.update { it.copy(showCreateTaskDialog = true, sites = sites) }
        }
    }

    fun onSiteSelected(site: String) {
        viewModelScope.launch {
            val technicians = userRepository.getUsersByDepartment(site).first()
            val equipment = equipmentRepository.getEquipmentByLocation(site).first()
            _uiState.update {
                it.copy(
                    selectedSite = site,
                    technicians = technicians,
                    equipment = equipment
                )
            }
        }
    }
    
    fun onDismissCreateTaskDialog() {
        _uiState.update { it.copy(showCreateTaskDialog = false, selectedSite = null, technicians = emptyList(), equipment = emptyList()) }
    }

    fun createTask(
        site: String,
        assignedTo: String,
        equipmentId: String,
        description: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isTaskCreationLoading = true) }
                
                val result = createWorkOrderUseCase(
                    equipmentId = equipmentId,
                    assignedTo = assignedTo,
                    description = description,
                    priority = Priority.MEDIUM,
                    site = site
                )
                
                if (result.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            isTaskCreationLoading = false,
                            showCreateTaskDialog = false,
                            selectedSite = null,
                            technicians = emptyList(),
                            equipment = emptyList()
                        )
                    }
                    // TODO: Show success message or refresh data
                } else {
                    _uiState.update { 
                        it.copy(
                            isTaskCreationLoading = false,
                            error = result.exceptionOrNull()?.message ?: "Failed to create task"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isTaskCreationLoading = false,
                        error = e.message ?: "Failed to create task"
                    )
                }
            }
        }
    }

    fun onShowCreateTechnicianDialog() {
        _uiState.update { it.copy(showCreateTechnicianDialog = true) }
    }

    fun onDismissCreateTechnicianDialog() {
        _uiState.update { it.copy(showCreateTechnicianDialog = false) }
    }

    fun createTechnician(
        fullName: String,
        email: String,
        employeeId: String,
        username: String,
        password: String,
        confirmPassword: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isTechnicianCreationLoading = true) }
                
                if (password != confirmPassword) {
                    _uiState.update { 
                        it.copy(
                            isTechnicianCreationLoading = false,
                            error = "Passwords do not match"
                        )
                    }
                    return@launch
                }
                
                val result = userRepository.createTechnician(
                    name = fullName,
                    employeeId = employeeId,
                    department = "Technical Services", // Default department
                    shiftPattern = "Day Shift" // Default shift pattern
                )
                
                if (result.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            isTechnicianCreationLoading = false,
                            showCreateTechnicianDialog = false
                        )
                    }
                    // TODO: Show success message or refresh data
                } else {
                    _uiState.update { 
                        it.copy(
                            isTechnicianCreationLoading = false,
                            error = result.exceptionOrNull()?.message ?: "Failed to create technician"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isTechnicianCreationLoading = false,
                        error = e.message ?: "Failed to create technician"
                    )
                }
            }
        }
    }

    private fun mapToActivityLogs(activities: List<SystemActivity>): List<ActivityLog> {
        return activities.map { activity ->
            ActivityLog(
                id = activity.id,
                description = activity.action,
                timestamp = formatTimestamp(activity.timestamp),
                icon = getIconForActivityType(activity.type),
                color = getColorForActivityType(activity.type)
            )
        }
    }
    
    private fun getIconForActivityType(type: ActivityType): ImageVector {
        return when (type) {
            ActivityType.USER_LOGIN -> Icons.Default.Login
            ActivityType.USER_LOGOUT -> Icons.Default.Logout
            ActivityType.FORM_SUBMITTED -> Icons.Filled.Assignment
            ActivityType.EQUIPMENT_UPDATED -> Icons.Default.Settings
            ActivityType.SYSTEM_BACKUP -> Icons.Default.Backup
            ActivityType.SYSTEM_ERROR -> Icons.Default.Error
            ActivityType.DATA_SYNC -> Icons.Default.Sync
            ActivityType.USER_CREATED -> Icons.Default.PersonAdd
            ActivityType.USER_UPDATED -> Icons.Default.Person
            ActivityType.SYSTEM_MAINTENANCE -> Icons.Default.Build
        }
    }
    
    private fun getColorForActivityType(type: ActivityType): Color {
        return when (type) {
            ActivityType.USER_LOGIN -> Color(0xFF4CAF50)
            ActivityType.USER_LOGOUT -> Color(0xFF757575)
            ActivityType.FORM_SUBMITTED -> Color(0xFF2196F3)
            ActivityType.EQUIPMENT_UPDATED -> Color(0xFFFF9800)
            ActivityType.SYSTEM_BACKUP -> Color(0xFF9C27B0)
            ActivityType.SYSTEM_ERROR -> Color(0xFFF44336)
            ActivityType.DATA_SYNC -> Color(0xFF00BCD4)
            ActivityType.USER_CREATED -> Color(0xFF4CAF50)
            ActivityType.USER_UPDATED -> Color(0xFF2196F3)
            ActivityType.SYSTEM_MAINTENANCE -> Color(0xFFFF9800)
        }
    }
    
    private fun formatTimestamp(timestamp: Date): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp.time
        
        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            else -> SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(timestamp)
        }
    }
}
