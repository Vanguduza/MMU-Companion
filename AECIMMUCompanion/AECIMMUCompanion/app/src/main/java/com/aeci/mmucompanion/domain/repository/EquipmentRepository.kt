package com.aeci.mmucompanion.domain.repository

import com.aeci.mmucompanion.domain.model.Equipment
import com.aeci.mmucompanion.domain.model.EquipmentStatus
import com.aeci.mmucompanion.domain.model.EquipmentType
import com.aeci.mmucompanion.domain.model.MaintenanceTask
import com.aeci.mmucompanion.domain.model.Priority
import com.aeci.mmucompanion.domain.model.WorkOrder
import kotlinx.coroutines.flow.Flow

interface EquipmentRepository {
    suspend fun getEquipmentById(id: String): Equipment?
    
    fun getAllActiveEquipment(): Flow<List<Equipment>>
    
    fun getEquipmentByType(type: EquipmentType): Flow<List<Equipment>>
    
    fun getEquipmentByLocation(location: String): Flow<List<Equipment>>
    
    suspend fun getEquipmentBySite(siteId: String): List<Equipment>
    
    suspend fun getEquipmentAtSite(siteId: String): List<Equipment>
    
    fun getEquipmentByStatus(status: EquipmentStatus): Flow<List<Equipment>>
    
    fun getEquipmentDueForMaintenance(date: Long): Flow<List<Equipment>>
    
    suspend fun createEquipment(equipment: Equipment): Result<String>
    
    suspend fun updateEquipment(equipment: Equipment): Result<Unit>
    
    suspend fun updateEquipmentStatus(id: String, status: EquipmentStatus): Result<Unit>
    
    suspend fun updateEquipmentStatusAndCondition(
        id: String, 
        statusIndicator: com.aeci.mmucompanion.domain.model.EquipmentStatusIndicator, 
        conditionDescription: String,
        conditionImageUri: String? = null,
        modifiedBy: String
    ): Result<Unit>
    
    suspend fun updateMaintenanceDates(
        id: String, 
        lastMaintenance: Long, 
        nextMaintenance: Long
    ): Result<Unit>
    
    suspend fun deleteEquipment(id: String): Result<Unit>
    
    suspend fun syncEquipment(): Result<Unit>
    
    suspend fun getEquipmentHistory(id: String): Flow<List<EquipmentEvent>>
    
    suspend fun addEquipmentEvent(event: EquipmentEvent): Result<Unit>

    suspend fun getMaintenanceTasks(): List<MaintenanceTask>

    suspend fun getWorkOrders(): List<WorkOrder>
    
    suspend fun createWorkOrder(
        equipmentId: String,
        assignedTo: String,
        description: String,
        priority: Priority = Priority.MEDIUM,
        site: String? = null
    ): Result<String>
    
    // Sync operations
    suspend fun getPendingEquipmentUpdates(): List<Equipment>
    
    suspend fun syncEquipmentUpdate(equipment: Equipment): Result<Unit>
    
    suspend fun markEquipmentAsSynced(equipmentId: String)
    
    suspend fun downloadLatestEquipment(): Result<List<Equipment>>
    
    suspend fun cacheEquipmentData(equipment: List<Equipment>)
}

data class EquipmentEvent(
    val id: String,
    val equipmentId: String,
    val eventType: EquipmentEventType,
    val description: String,
    val timestamp: Long,
    val userId: String,
    val formId: String? = null,
    val severity: EventSeverity = EventSeverity.INFO
)

enum class EquipmentEventType {
    MAINTENANCE,
    BREAKDOWN,
    REPAIR,
    INSPECTION,
    STATUS_CHANGE,
    PARAMETER_UPDATE,
    ALARM,
    WARNING
}

enum class EventSeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}
