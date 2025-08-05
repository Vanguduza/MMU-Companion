package com.aeci.mmucompanion.data.local.dao

import androidx.room.*
import com.aeci.mmucompanion.data.local.entity.JobCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobCardDao {
    
    @Query("SELECT * FROM job_cards WHERE id = :id")
    suspend fun getJobCardById(id: String): JobCardEntity?
    
    @Query("SELECT * FROM job_cards WHERE assignedTo = :userId ORDER BY createdAt DESC")
    suspend fun getJobCardsByUser(userId: String): List<JobCardEntity>
    
    @Query("SELECT * FROM job_cards WHERE assignedTo = :userId ORDER BY createdAt DESC")
    fun getJobCardsByUserFlow(userId: String): Flow<List<JobCardEntity>>
    
    @Query("SELECT * FROM job_cards WHERE equipmentId = :equipmentId ORDER BY createdAt DESC")
    suspend fun getJobCardsByEquipment(equipmentId: String): List<JobCardEntity>
    
    @Query("SELECT * FROM job_cards WHERE status = :status ORDER BY createdAt DESC")
    suspend fun getJobCardsByStatus(status: String): List<JobCardEntity>
    
    @Query("SELECT * FROM job_cards WHERE status = :status ORDER BY createdAt DESC")
    fun getJobCardsByStatusFlow(status: String): Flow<List<JobCardEntity>>
    
    @Query("SELECT * FROM job_cards WHERE priority = :priority ORDER BY createdAt DESC")
    suspend fun getJobCardsByPriority(priority: String): List<JobCardEntity>
    
    @Query("SELECT * FROM job_cards WHERE category = :category ORDER BY createdAt DESC")
    suspend fun getJobCardsByCategory(category: String): List<JobCardEntity>
    
    @Query("SELECT * FROM job_cards WHERE dueDate < date('now') AND status IN ('PENDING', 'IN_PROGRESS') ORDER BY dueDate ASC")
    suspend fun getOverdueJobCards(): List<JobCardEntity>
    
    @Query("SELECT * FROM job_cards WHERE dueDate BETWEEN date('now') AND date('now', '+' || :days || ' days') AND status IN ('PENDING', 'IN_PROGRESS') ORDER BY dueDate ASC")
    suspend fun getDueSoonJobCards(days: Int): List<JobCardEntity>
    
    @Query("SELECT * FROM job_cards ORDER BY createdAt DESC")
    suspend fun getAllJobCards(): List<JobCardEntity>
    
    @Query("SELECT * FROM job_cards ORDER BY createdAt DESC")
    fun getAllJobCardsFlow(): Flow<List<JobCardEntity>>
    
    @Query("SELECT * FROM job_cards WHERE synced = 0")
    suspend fun getUnsyncedJobCards(): List<JobCardEntity>
    
    @Query("SELECT * FROM job_cards WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR equipmentName LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    suspend fun searchJobCards(query: String): List<JobCardEntity>
    
    @Query("SELECT * FROM job_cards WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    suspend fun getJobCardsByDateRange(startDate: String, endDate: String): List<JobCardEntity>
    
    @Query("SELECT * FROM job_cards WHERE siteLocation = :location ORDER BY createdAt DESC")
    suspend fun getJobCardsByLocation(location: String): List<JobCardEntity>
    
    @Query("SELECT * FROM job_cards WHERE relatedTaskId = :taskId ORDER BY createdAt DESC")
    suspend fun getJobCardsByTask(taskId: String): List<JobCardEntity>
    
    // Complex filter query
    @Query("""
        SELECT * FROM job_cards 
        WHERE (:statuses IS NULL OR status IN (:statuses))
        AND (:priorities IS NULL OR priority IN (:priorities))
        AND (:categories IS NULL OR category IN (:categories))
        AND (:assignedTo IS NULL OR assignedTo = :assignedTo)
        AND (:createdBy IS NULL OR createdBy = :createdBy)
        AND (:equipmentId IS NULL OR equipmentId = :equipmentId)
        AND (:siteLocation IS NULL OR siteLocation = :siteLocation)
        AND (:dateFrom IS NULL OR createdAt >= :dateFrom)
        AND (:dateTo IS NULL OR createdAt <= :dateTo)
        ORDER BY createdAt DESC
    """)
    suspend fun getJobCardsByFilters(
        statuses: List<String>? = null,
        priorities: List<String>? = null,
        categories: List<String>? = null,
        assignedTo: String? = null,
        createdBy: String? = null,
        equipmentId: String? = null,
        siteLocation: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): List<JobCardEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobCard(jobCard: JobCardEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobCards(jobCards: List<JobCardEntity>)
    
    @Update
    suspend fun updateJobCard(jobCard: JobCardEntity)
    
    @Query("UPDATE job_cards SET synced = :synced WHERE id = :id")
    suspend fun updateJobCardSyncStatus(id: String, synced: Boolean)
    
    @Query("DELETE FROM job_cards WHERE id = :id")
    suspend fun deleteJobCard(id: String)
    
    @Query("DELETE FROM job_cards WHERE assignedTo = :userId")
    suspend fun deleteJobCardsByUser(userId: String)
    
    @Query("DELETE FROM job_cards WHERE equipmentId = :equipmentId")
    suspend fun deleteJobCardsByEquipment(equipmentId: String)
    
    @Query("DELETE FROM job_cards")
    suspend fun deleteAllJobCards()
    
    // Statistics queries
    @Query("SELECT COUNT(*) FROM job_cards")
    suspend fun getTotalJobCardsCount(): Int
    
    @Query("SELECT COUNT(*) FROM job_cards WHERE status = :status")
    suspend fun getJobCardsCountByStatus(status: String): Int
    
    @Query("SELECT COUNT(*) FROM job_cards WHERE assignedTo = :userId")
    suspend fun getJobCardsCountByUser(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM job_cards WHERE equipmentId = :equipmentId")
    suspend fun getJobCardsCountByEquipment(equipmentId: String): Int
    
    @Query("SELECT COUNT(*) FROM job_cards WHERE dueDate < date('now') AND status IN ('PENDING', 'IN_PROGRESS')")
    suspend fun getOverdueJobCardsCount(): Int
    
    @Query("SELECT AVG(actualHours) FROM job_cards WHERE status = 'COMPLETED' AND actualHours IS NOT NULL")
    suspend fun getAverageCompletionHours(): Double?
    
    @Query("SELECT SUM(actualHours) FROM job_cards WHERE actualHours IS NOT NULL")
    suspend fun getTotalHours(): Double?
    
    @Query("SELECT COUNT(*) FROM job_cards WHERE synced = 0")
    suspend fun getUnsyncedJobCardsCount(): Int
} 