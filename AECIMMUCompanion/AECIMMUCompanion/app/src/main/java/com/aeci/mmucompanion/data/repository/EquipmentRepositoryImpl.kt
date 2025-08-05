package com.aeci.mmucompanion.data.repository

import com.aeci.mmucompanion.data.local.dao.EquipmentDao
import com.aeci.mmucompanion.data.local.entity.EquipmentEntity
import com.aeci.mmucompanion.data.remote.api.AECIApiService
import com.aeci.mmucompanion.domain.model.Equipment
import com.aeci.mmucompanion.domain.model.EquipmentStatus
import com.aeci.mmucompanion.domain.model.EquipmentType
import com.aeci.mmucompanion.domain.model.MaintenanceTask
import com.aeci.mmucompanion.domain.model.Priority
import com.aeci.mmucompanion.domain.model.WorkOrder
import com.aeci.mmucompanion.domain.model.WorkOrderStatus
import com.aeci.mmucompanion.domain.repository.EquipmentEvent
import com.aeci.mmucompanion.domain.repository.EquipmentEventType
import com.aeci.mmucompanion.domain.repository.EquipmentRepository
import com.aeci.mmucompanion.domain.repository.EventSeverity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EquipmentRepositoryImpl @Inject constructor(
    private val equipmentDao: EquipmentDao,
    private val apiService: AECIApiService,
    private val gson: Gson
) : EquipmentRepository {
    
    override suspend fun getEquipmentById(id: String): Equipment? {
        return equipmentDao.getEquipmentById(id)?.toEquipment()
    }
    
    override fun getAllActiveEquipment(): Flow<List<Equipment>> {
        return equipmentDao.getAllActiveEquipment().map { entities ->
            entities.map { it.toEquipment() }
        }
    }
    
    override fun getEquipmentByType(type: EquipmentType): Flow<List<Equipment>> {
        return equipmentDao.getEquipmentByType(type.name).map { entities ->
            entities.map { it.toEquipment() }
        }
    }
    
    override fun getEquipmentByLocation(location: String): Flow<List<Equipment>> {
        return equipmentDao.getEquipmentByLocation(location).map { entities ->
            entities.map { it.toEquipment() }
        }
    }
    
    override fun getEquipmentByStatus(status: EquipmentStatus): Flow<List<Equipment>> {
        return equipmentDao.getEquipmentByStatus(status.name).map { entities ->
            entities.map { it.toEquipment() }
        }
    }
    
    override suspend fun getEquipmentBySite(siteId: String): List<Equipment> {
        return equipmentDao.getEquipmentBySite(siteId).map { it.toEquipment() }
    }
    
    override suspend fun getEquipmentAtSite(siteId: String): List<Equipment> {
        return getEquipmentBySite(siteId)
    }
    
    override fun getEquipmentDueForMaintenance(date: Long): Flow<List<Equipment>> {
        return equipmentDao.getEquipmentDueForMaintenance(date).map { entities ->
            entities.map { it.toEquipment() }
        }
    }
    
    override suspend fun createEquipment(equipment: Equipment): Result<String> {
        return try {
            val entity = EquipmentEntity(
                id = equipment.id,
                name = equipment.name,
                type = equipment.type.name,
                model = equipment.model,
                serialNumber = equipment.serialNumber,
                location = equipment.location,
                siteId = equipment.siteId ?: "DEFAULT_SITE",
                status = equipment.status.name,
                manufacturer = equipment.manufacturer,
                installationDate = equipment.installationDate,
                lastMaintenanceDate = equipment.lastMaintenanceDate,
                nextMaintenanceDate = equipment.nextMaintenanceDate,
                specifications = gson.toJson(equipment.specifications),
                operatingParameters = gson.toJson(equipment.operatingParameters),
                isActive = equipment.isActive,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            equipmentDao.insertEquipment(entity)
            Result.success(equipment.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateEquipment(equipment: Equipment): Result<Unit> {
        return try {
            val existingEntity = equipmentDao.getEquipmentById(equipment.id)
            if (existingEntity != null) {
                val updatedEntity = existingEntity.copy(
                    name = equipment.name,
                    type = equipment.type.name,
                    model = equipment.model,
                    serialNumber = equipment.serialNumber,
                    location = equipment.location,
                    status = equipment.status.name,
                    manufacturer = equipment.manufacturer,
                    installationDate = equipment.installationDate,
                    lastMaintenanceDate = equipment.lastMaintenanceDate,
                    nextMaintenanceDate = equipment.nextMaintenanceDate,
                    specifications = gson.toJson(equipment.specifications),
                    operatingParameters = gson.toJson(equipment.operatingParameters),
                    isActive = equipment.isActive,
                    updatedAt = System.currentTimeMillis()
                )
                
                equipmentDao.updateEquipment(updatedEntity)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Equipment not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateEquipmentStatus(id: String, status: EquipmentStatus): Result<Unit> {
        return try {
            equipmentDao.updateEquipmentStatus(
                id = id,
                status = status.name,
                updatedAt = System.currentTimeMillis()
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateEquipmentStatusAndCondition(
        id: String, 
        statusIndicator: com.aeci.mmucompanion.domain.model.EquipmentStatusIndicator, 
        conditionDescription: String,
        conditionImageUri: String?,
        modifiedBy: String
    ): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()
            equipmentDao.updateEquipmentStatusAndCondition(
                id = id,
                statusIndicator = statusIndicator.name,
                conditionDescription = conditionDescription,
                conditionImageUri = conditionImageUri,
                modifiedBy = modifiedBy,
                modifiedAt = currentTime,
                updatedAt = currentTime
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateMaintenanceDates(
        id: String,
        lastMaintenance: Long,
        nextMaintenance: Long
    ): Result<Unit> {
        return try {
            equipmentDao.updateMaintenanceDates(
                id,
                lastMaintenance,
                nextMaintenance,
                System.currentTimeMillis()
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteEquipment(id: String): Result<Unit> {
        return try {
            val equipment = equipmentDao.getEquipmentById(id)
            if (equipment != null) {
                val deactivatedEquipment = equipment.copy(
                    isActive = false,
                    updatedAt = System.currentTimeMillis()
                )
                equipmentDao.updateEquipment(deactivatedEquipment)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Equipment not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun syncEquipment(): Result<Unit> {
        return try {
            // This would typically fetch equipment from the server
            // For now, it's a placeholder implementation
            
            // val response = apiService.getEquipment("Bearer $token")
            // if (response.isSuccessful && response.body()?.success == true) {
            //     val apiEquipment = response.body()!!.equipment
            //     apiEquipment.forEach { apiEq ->
            //         val entity = apiEq.toEquipmentEntity()
            //         equipmentDao.insertEquipment(entity)
            //     }
            // }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getEquipmentHistory(id: String): Flow<List<EquipmentEvent>> {
        // This would typically query a separate equipment_events table
        // For now, returning empty flow as placeholder
        return flowOf(emptyList())
    }
    
    override suspend fun addEquipmentEvent(event: EquipmentEvent): Result<Unit> {
        return try {
            // This would typically insert into equipment_events table
            // and sync with server
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMaintenanceTasks(): List<MaintenanceTask> {
        // Dummy data
        return listOf(
            MaintenanceTask("task1", "mmu1", "Daily Inspection", Priority.HIGH, Date()),
            MaintenanceTask("task2", "pump2", "Oil Change", Priority.MEDIUM, Date(System.currentTimeMillis() + 86400000), Date()),
            MaintenanceTask("task3", "crusher3", "Replace Bearings", Priority.HIGH, Date(System.currentTimeMillis() + 86400000 * 2))
        )
    }

    override suspend fun getWorkOrders(): List<WorkOrder> {
        // Dummy data
        return listOf(
            WorkOrder("wo1", "mmu1", "MMU not starting", Date(), WorkOrderStatus.OPEN, Priority.HIGH),
            WorkOrder("wo2", "pump2", "Leaking hydraulic fluid", Date(System.currentTimeMillis() - 86400000), WorkOrderStatus.IN_PROGRESS, Priority.MEDIUM),
            WorkOrder("wo3", "crusher3", "Excessive vibration", Date(System.currentTimeMillis() - 86400000 * 2), WorkOrderStatus.COMPLETED, Priority.LOW)
        )
    }
    
    override suspend fun createWorkOrder(
        equipmentId: String,
        assignedTo: String,
        description: String,
        priority: Priority,
        site: String?
    ): Result<String> {
        return try {
            val workOrderId = "wo_${System.currentTimeMillis()}"
            val workOrder = WorkOrder(
                id = workOrderId,
                equipmentId = equipmentId,
                description = description,
                createdDate = Date(),
                status = WorkOrderStatus.OPEN,
                priority = priority,
                assignedTo = assignedTo
            )
            
            // TODO: Save to database
            // workOrderDao.insert(workOrder.toEntity())
            
            // TODO: Sync with API
            // apiService.createWorkOrder(workOrder)
            
            Result.success(workOrderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPendingEquipmentUpdates(): List<Equipment> {
        // For now, return empty list - this can be implemented when equipment sync is needed
        return emptyList()
    }

    override suspend fun syncEquipmentUpdate(equipment: Equipment): Result<Unit> {
        return try {
            // Convert to entity and update locally first
            val entity = equipment.toEntity()
            equipmentDao.updateEquipment(entity)
            
            // Then sync to server if online
            // This would typically check network connectivity and sync to API
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markEquipmentAsSynced(equipmentId: String) {
        // Mark equipment as synced in local database
        // This would typically update a sync status field
    }

    override suspend fun downloadLatestEquipment(): Result<List<Equipment>> {
        return try {
            // Download from API service - placeholder implementation
            // val response = apiService.getAllEquipment()
            // Result.success(response.map { it.toEquipment() })
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cacheEquipmentData(equipment: List<Equipment>) {
        // Cache equipment data locally
        val entities = equipment.map { it.toEntity() }
        equipmentDao.insertAll(entities)
    }

    private fun EquipmentEntity.toEquipment(): Equipment {
        val specificationsMap = try {
            val mapType = object : TypeToken<Map<String, Any>>() {}.type
            gson.fromJson<Map<String, Any>>(specifications, mapType)
        } catch (e: Exception) {
            emptyMap<String, Any>()
        }
        
        val operatingParametersMap = try {
            val mapType = object : TypeToken<Map<String, Any>>() {}.type
            gson.fromJson<Map<String, Any>>(operatingParameters, mapType)
        } catch (e: Exception) {
            emptyMap<String, Any>()
        }
        
        val recordedIssuesList = try {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(recordedIssues, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList<String>()
        }
        
        return Equipment(
            id = id,
            name = name,
            type = try { EquipmentType.valueOf(type) } catch (e: Exception) { EquipmentType.OTHER },
            model = model,
            serialNumber = serialNumber,
            location = location,
            siteId = siteId,
            status = try { EquipmentStatus.valueOf(status) } catch (e: Exception) { EquipmentStatus.OFFLINE },
            statusIndicator = try { 
                com.aeci.mmucompanion.domain.model.EquipmentStatusIndicator.valueOf(statusIndicator) 
            } catch (e: Exception) { 
                com.aeci.mmucompanion.domain.model.EquipmentStatusIndicator.GREEN 
            },
            conditionDescription = conditionDescription,
            manufacturer = manufacturer,
            installationDate = installationDate,
            lastMaintenanceDate = lastMaintenanceDate,
            nextMaintenanceDate = nextMaintenanceDate,
            specifications = specificationsMap,
            operatingParameters = operatingParametersMap,
            isActive = isActive,
            lastModifiedBy = lastModifiedBy,
            lastModifiedAt = lastModifiedAt,
            imageUri = imageUri,
            conditionImageUri = conditionImageUri,
            recordedIssues = recordedIssuesList
        )
    }

    private fun Equipment.toEntity(): EquipmentEntity {
        return EquipmentEntity(
            id = id,
            name = name,
            type = type.name,
            model = model,
            serialNumber = serialNumber,
            location = location,
            siteId = siteId,
            status = status.name,
            statusIndicator = statusIndicator.name,
            conditionDescription = conditionDescription,
            manufacturer = manufacturer,
            installationDate = installationDate,
            lastMaintenanceDate = lastMaintenanceDate,
            nextMaintenanceDate = nextMaintenanceDate,
            specifications = gson.toJson(specifications),
            operatingParameters = gson.toJson(operatingParameters),
            isActive = isActive,
            lastModifiedBy = lastModifiedBy,
            lastModifiedAt = lastModifiedAt,
            imageUri = imageUri,
            conditionImageUri = conditionImageUri,
            recordedIssues = gson.toJson(recordedIssues),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}
