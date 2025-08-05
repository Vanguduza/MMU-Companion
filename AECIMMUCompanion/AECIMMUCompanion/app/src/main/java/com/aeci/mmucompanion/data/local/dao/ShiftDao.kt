package com.aeci.mmucompanion.data.local.dao

import androidx.room.*
import com.aeci.mmucompanion.data.local.entity.ShiftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shifts WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveShifts(): Flow<List<ShiftEntity>>
    
    @Query("SELECT * FROM shifts WHERE id = :id")
    suspend fun getShiftById(id: String): ShiftEntity?
    
    @Query("SELECT * FROM shifts WHERE type = :type AND isActive = 1")
    fun getShiftsByType(type: String): Flow<List<ShiftEntity>>
    
    @Query("SELECT * FROM shifts WHERE location = :location AND isActive = 1")
    fun getShiftsByLocation(location: String): Flow<List<ShiftEntity>>
    
    @Query("SELECT * FROM shifts WHERE supervisorId = :supervisorId AND isActive = 1")
    fun getShiftsBySupervisor(supervisorId: String): Flow<List<ShiftEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: ShiftEntity): Long
    
    @Update
    suspend fun updateShift(shift: ShiftEntity)
    
    @Delete
    suspend fun deleteShift(shift: ShiftEntity)
    
    @Query("UPDATE shifts SET supervisorId = :supervisorId WHERE id = :id")
    suspend fun updateShiftSupervisor(id: String, supervisorId: String?)
    
    @Query("SELECT COUNT(*) FROM shifts WHERE isActive = 1")
    suspend fun getActiveShiftCount(): Int
}
