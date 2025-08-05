package com.aeci.mmucompanion.data.local.dao

import androidx.room.*
import com.aeci.mmucompanion.data.local.entity.FormEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FormDao {
    
    @Query("SELECT * FROM forms WHERE id = :id")
    suspend fun getFormById(id: String): FormEntity?
    
    @Query("SELECT * FROM forms ORDER BY createdAt DESC")
    suspend fun getAllForms(): List<FormEntity>
    
    @Query("SELECT * FROM forms WHERE createdBy = :userId ORDER BY createdAt DESC")
    suspend fun getFormsByUser(userId: String): List<FormEntity>
    
    @Query("SELECT * FROM forms WHERE createdBy = :userId ORDER BY createdAt DESC")
    fun getFormsByUserFlow(userId: String): Flow<List<FormEntity>>
    
    @Query("SELECT * FROM forms WHERE formType = :formType ORDER BY createdAt DESC")
    suspend fun getFormsByType(formType: String): List<FormEntity>
    
    @Query("SELECT * FROM forms WHERE status = :status ORDER BY createdAt DESC")
    suspend fun getFormsByStatus(status: String): List<FormEntity>
    
    @Query("SELECT * FROM forms WHERE equipmentId = :equipmentId ORDER BY createdAt DESC")
    suspend fun getFormsByEquipment(equipmentId: String): List<FormEntity>
    
    @Query("SELECT * FROM forms WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    suspend fun getFormsByDateRange(startDate: String, endDate: String): List<FormEntity>
    
    @Query("SELECT * FROM forms ORDER BY createdAt DESC")
    fun getAllFormsFlow(): Flow<List<FormEntity>>
    
    @Query("SELECT * FROM forms WHERE synced = 0")
    suspend fun getUnsyncedForms(): List<FormEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForm(form: FormEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForms(forms: List<FormEntity>)
    
    @Update
    suspend fun updateForm(form: FormEntity)
    
    @Query("UPDATE forms SET synced = :synced WHERE id = :id")
    suspend fun updateFormSyncStatus(id: String, synced: Boolean)
    
    @Query("DELETE FROM forms WHERE id = :id")
    suspend fun deleteForm(id: String)
    
    @Query("DELETE FROM forms WHERE createdBy = :userId")
    suspend fun deleteFormsByUser(userId: String)
    
    @Query("DELETE FROM forms WHERE formType = :formType")
    suspend fun deleteFormsByType(formType: String)
    
    @Query("DELETE FROM forms")
    suspend fun deleteAllForms()
    
    @Query("SELECT COUNT(*) FROM forms WHERE createdBy = :userId")
    suspend fun getFormsCountByUser(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM forms WHERE formType = :formType")
    suspend fun getFormsCountByType(formType: String): Int
    
    @Query("SELECT COUNT(*) FROM forms WHERE status = :status")
    suspend fun getFormsCountByStatus(status: String): Int
    
    @Query("SELECT COUNT(*) FROM forms WHERE synced = 0")
    suspend fun getUnsyncedFormsCount(): Int
    
    @Query("SELECT * FROM forms WHERE synced = 0 ORDER BY createdAt DESC")
    suspend fun getPendingForms(): List<FormEntity>
    
    @Query("UPDATE forms SET synced = 1, lastSyncAttempt = :timestamp WHERE id = :formId")
    suspend fun updateSyncStatus(formId: String, timestamp: Long)
    
    // Additional query methods needed by FormRepositoryImpl
    @Query("SELECT * FROM forms WHERE siteLocation = :siteLocation ORDER BY createdAt DESC")
    suspend fun getFormsBySiteLocation(siteLocation: String): List<FormEntity>
    
    @Query("SELECT * FROM forms WHERE siteLocation = :siteLocation AND formType = :formType ORDER BY createdAt DESC")
    suspend fun getFormsBySiteAndType(siteLocation: String, formType: String): List<FormEntity>
    
    @Query("SELECT * FROM forms WHERE createdBy = :userId AND formType = :formType ORDER BY createdAt DESC")
    suspend fun getFormsByUserAndType(userId: String, formType: String): List<FormEntity>
    
    @Query("SELECT * FROM forms WHERE siteLocation = :siteLocation AND formType = :formType AND createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    suspend fun getFormsBySiteAndDateRange(siteLocation: String, formType: String, startDate: String, endDate: String): List<FormEntity>
}
