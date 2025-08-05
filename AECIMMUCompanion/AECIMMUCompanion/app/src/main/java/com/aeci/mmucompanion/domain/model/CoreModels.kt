package com.aeci.mmucompanion.domain.model

data class User(
    val id: String,
    val username: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val department: String,
    val shiftPattern: String,
    val permissions: List<Permission>,
    val isActive: Boolean,
    val lastLoginAt: Long? = null,
    val biometricEnabled: Boolean = false,
    val profileImageUri: String? = null,
    val siteId: String, // Current site assignment
    val availableSites: List<String> = listOf(siteId) // Sites user can access
)

enum class UserRole {
    OPERATOR,
    SUPERVISOR,
    MAINTENANCE,
    ADMIN
}

enum class Permission {
    // Form permissions
    VIEW_FORMS,
    CREATE_FORMS,
    EDIT_FORMS,
    DELETE_FORMS,
    SUBMIT_FORMS,
    APPROVE_FORMS,
    
    // Equipment permissions
    VIEW_EQUIPMENT,
    MANAGE_EQUIPMENT,
    
    // User management
    VIEW_USERS,
    MANAGE_USERS,
    
    // Reports and exports
    VIEW_REPORTS,
    EXPORT_DATA,
    
    // System administration
    SYSTEM_ADMIN,
    SYNC_DATA,
    MANAGE_SHIFTS,
    
    // Quality and safety
    QUALITY_REVIEW,
    SAFETY_AUDIT
}

data class Equipment(
    val id: String,
    val name: String,
    val type: EquipmentType,
    val model: String,
    val serialNumber: String,
    val location: String,
    val siteId: String, // Site where equipment is located
    val status: EquipmentStatus,
    val statusIndicator: EquipmentStatusIndicator = EquipmentStatusIndicator.GREEN,
    val conditionDescription: String = "",
    val manufacturer: String,
    val installationDate: Long,
    val lastMaintenanceDate: Long? = null,
    val nextMaintenanceDate: Long? = null,
    val specifications: Map<String, Any>,
    val operatingParameters: Map<String, Any>,
    val isActive: Boolean = true,
    val lastModifiedBy: String? = null,
    val lastModifiedAt: Long? = null,
    val imageUri: String? = null, // Equipment image
    val conditionImageUri: String? = null, // Current condition evidence photo
    val recordedIssues: List<String> = emptyList() // List of recorded issues including calibration warnings
)

enum class EquipmentType {
    MMU,
    PUMP,
    CRUSHER,
    CONVEYOR,
    SEPARATOR,
    CLASSIFIER,
    OTHER
}

enum class EquipmentStatus {
    OPERATIONAL,
    MAINTENANCE,
    BREAKDOWN,
    OFFLINE,
    STANDBY
}

enum class EquipmentStatusIndicator {
    GREEN,    // Good condition
    AMBER,    // Requires attention
    RED       // Critical/Poor condition
}

data class EquipmentGroup(
    val id: String,
    val name: String,
    val description: String,
    val type: EquipmentType,
    val siteId: String,
    val isActive: Boolean = true,
    val createdBy: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class Shift(
    val id: String,
    val name: String,
    val startTime: String,
    val endTime: String,
    val duration: Int,
    val type: ShiftType,
    val isActive: Boolean = true,
    val supervisorId: String? = null,
    val location: String
)

enum class ShiftType {
    DAY,
    NIGHT,
    BACK_SHIFT
}

data class DashboardStats(
    val formsCompleted: Int = 0,
    val equipmentInspected: Int = 0,
    val pendingTasks: Int = 0,
    val alertsCount: Int = 0
)

data class EquipmentAlert(
    val equipmentId: String,
    val message: String,
    val severity: AlertSeverity,
    val timestamp: Long
)

enum class AlertSeverity {
    LOW, MEDIUM, HIGH
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val currentShift: Shift? = null,
    val lastSyncTime: String? = null,
    val todayStats: DashboardStats = DashboardStats(),
    val recentForms: List<Form> = emptyList(),
    val equipmentAlerts: List<EquipmentAlert> = emptyList(),
    val error: String? = null
)

// Blast Report Models
data class BlastReport(
    val id: String,
    val dateRange: String,
    val fromDate: String, // ISO date string
    val toDate: String,   // ISO date string
    val bcmBlasted: Double,
    val totalHolesCharged: Int,
    val averageHoleDepth: Double,
    val averageBurden: Double,
    val averageSpacing: Double,
    val averageStemmingLength: Double,
    val averageFlowRate: Double,
    val averageDeliveryRate: Double,
    val averageFinalCupDensity: Double,
    val averagePumpingPressure: Double,
    val totalEmulsionUsed: Double,
    val powderFactor: Double,
    val emulsionSource: String, // "Weighbridge" or "Blast Hole Log"
    val weighbridgeTotal: Double?,
    val blastHoleTotal: Double?,
    val discrepancyPercent: Double?,
    val needsCalibration: Boolean,
    val machineHours: Double,
    val breakdownNotes: List<String>,
    val generatedBy: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val siteId: String
)

data class WeighbridgeTicket(
    val ticketNumber: String,
    val weight: Double,
    val date: String, // ISO date string
    val vehicle: String = ""
)

data class BlastReportInput(
    val fromDate: String, // ISO date string
    val toDate: String,   // ISO date string
    val bcmBlasted: Double,
    val weighbridgeTickets: List<WeighbridgeTicket> = emptyList(),
    val fallbackToBlastHoleLog: Boolean = false,
    val siteId: String,
    val generatedBy: String
)

// Error handling models
data class ValidationError(
    val field: String,
    val message: String,
    val code: String? = null
)

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError> = emptyList()
)

// Task models
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val assignedTo: String?,
    val dueDate: Long?,
    val completedDate: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val siteId: String
)

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
