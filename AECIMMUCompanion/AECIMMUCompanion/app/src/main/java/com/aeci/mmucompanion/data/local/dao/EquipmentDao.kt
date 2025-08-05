package com.aeci.mmucompanion.data.local.dao

import androidx.room.*
import com.aeci.mmucompanion.data.local.entity.EquipmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveEquipment(): Flow<List<EquipmentEntity>>
    
    @Query("SELECT * FROM equipment WHERE id = :id")
    suspend fun getEquipmentById(id: String): EquipmentEntity?
    
    @Query("SELECT * FROM equipment WHERE type = :type AND isActive = 1")
    fun getEquipmentByType(type: String): Flow<List<EquipmentEntity>>
    
    @Query("SELECT * FROM equipment WHERE location = :location AND isActive = 1")
    fun getEquipmentByLocation(location: String): Flow<List<EquipmentEntity>>
    
    @Query("SELECT * FROM equipment WHERE status = :status AND isActive = 1")
    fun getEquipmentByStatus(status: String): Flow<List<EquipmentEntity>>
    
    @Query("SELECT * FROM equipment WHERE siteId = :siteId AND isActive = 1")
    suspend fun getEquipmentBySite(siteId: String): List<EquipmentEntity>
    
    @Query("SELECT * FROM equipment WHERE nextMaintenanceDate <= :date AND isActive = 1")
    fun getEquipmentDueForMaintenance(date: Long): Flow<List<EquipmentEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(equipment: EquipmentEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(equipment: List<EquipmentEntity>)
    
    @Update
    suspend fun updateEquipment(equipment: EquipmentEntity)
    
    @Delete
    suspend fun deleteEquipment(equipment: EquipmentEntity)
    
    @Query("UPDATE equipment SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateEquipmentStatus(id: String, status: String, updatedAt: Long)
    
    @Query("UPDATE equipment SET statusIndicator = :statusIndicator, conditionDescription = :conditionDescription, conditionImageUri = :conditionImageUri, lastModifiedBy = :modifiedBy, lastModifiedAt = :modifiedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateEquipmentStatusAndCondition(
        id: String, 
        statusIndicator: String, 
        conditionDescription: String,
        conditionImageUri: String?, 
        modifiedBy: String, 
        modifiedAt: Long, 
        updatedAt: Long
    )
    
    @Query("UPDATE equipment SET lastMaintenanceDate = :date, nextMaintenanceDate = :nextDate, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateMaintenanceDates(id: String, date: Long, nextDate: Long, updatedAt: Long)
    
    @Query("SELECT COUNT(*) FROM equipment WHERE isActive = 1")
    suspend fun getActiveEquipmentCount(): Int
    
    @Query("SELECT COUNT(*) FROM equipment WHERE status = :status AND isActive = 1")
    suspend fun getEquipmentCountByStatus(status: String): Int
}
