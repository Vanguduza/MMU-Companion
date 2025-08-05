package com.aeci.mmucompanion.data.local.dao

import androidx.room.*
import com.aeci.mmucompanion.data.local.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    
    @Query("SELECT * FROM reports ORDER BY completionDate DESC")
    fun getAllReports(): Flow<List<ReportEntity>>
    
    @Query("SELECT * FROM reports WHERE id = :reportId")
    suspend fun getReportById(reportId: String): ReportEntity?
    
    @Query("SELECT * FROM reports ORDER BY completionDate DESC LIMIT :limit OFFSET :offset")
    suspend fun getReportsPaginated(limit: Int, offset: Int): List<ReportEntity>
    
    @Query("SELECT COUNT(*) FROM reports")
    suspend fun getReportCount(): Int
    
    @Query("SELECT * FROM reports WHERE reportType = :reportType ORDER BY completionDate DESC")
    fun getReportsByType(reportType: String): Flow<List<ReportEntity>>
    
    @Query("SELECT * FROM reports WHERE generatedById = :userId ORDER BY completionDate DESC")
    fun getReportsByUser(userId: String): Flow<List<ReportEntity>>
    
    @Query("SELECT * FROM reports WHERE completionDate BETWEEN :startDate AND :endDate ORDER BY completionDate DESC")
    fun getReportsByDateRange(startDate: Long, endDate: Long): Flow<List<ReportEntity>>
    
    @Query("SELECT * FROM reports WHERE reportTitle LIKE '%' || :query || '%' OR reportType LIKE '%' || :query || '%' ORDER BY completionDate DESC")
    fun searchReports(query: String): Flow<List<ReportEntity>>
    
    @Query("SELECT * FROM reports WHERE isDownloaded = 1 ORDER BY completionDate DESC")
    fun getDownloadedReports(): Flow<List<ReportEntity>>
    
    @Query("SELECT * FROM reports WHERE status = :status ORDER BY completionDate DESC")
    fun getReportsByStatus(status: String): Flow<List<ReportEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<ReportEntity>)
    
    @Update
    suspend fun updateReport(report: ReportEntity)
    
    @Query("UPDATE reports SET isDownloaded = :isDownloaded, localFilePath = :localFilePath WHERE id = :reportId")
    suspend fun updateDownloadStatus(reportId: String, isDownloaded: Boolean, localFilePath: String?)
    
    @Query("UPDATE reports SET downloadCount = :downloadCount, lastDownloaded = :lastDownloaded WHERE id = :reportId")
    suspend fun updateDownloadInfo(reportId: String, downloadCount: Int, lastDownloaded: Long?)
    
    @Query("UPDATE reports SET lastSynced = :lastSynced WHERE id = :reportId")
    suspend fun updateSyncTime(reportId: String, lastSynced: Long)
    
    @Delete
    suspend fun deleteReport(report: ReportEntity)
    
    @Query("DELETE FROM reports WHERE id = :reportId")
    suspend fun deleteReport(reportId: String)
    
    @Query("DELETE FROM reports WHERE generatedById = :userId")
    suspend fun deleteReportsByUser(userId: String)
    
    @Query("DELETE FROM reports WHERE reportType = :reportType")
    suspend fun deleteReportsByType(reportType: String)
    
    @Query("DELETE FROM reports")
    suspend fun clearAllReports()
    
    @Query("DELETE FROM reports WHERE lastSynced < :cutoffTime")
    suspend fun deleteOldReports(cutoffTime: Long)
    
    // Statistics queries
    @Query("SELECT reportType, COUNT(*) as count FROM reports GROUP BY reportType")
    suspend fun getReportCountByType(): List<ReportTypeCount>
    
    @Query("SELECT format, COUNT(*) as count FROM reports GROUP BY format")
    suspend fun getReportCountByFormat(): List<FormatCount>
    
    @Query("SELECT generatedByName, COUNT(*) as count FROM reports GROUP BY generatedById ORDER BY count DESC LIMIT :limit")
    suspend fun getTopReportGenerators(limit: Int = 5): List<GeneratorCount>
    
    @Query("SELECT SUM(downloadCount) as totalDownloads FROM reports")
    suspend fun getTotalDownloads(): Int
    
    @Query("SELECT * FROM reports WHERE lastSynced < :cutoffTime")
    suspend fun getOutdatedReports(cutoffTime: Long): List<ReportEntity>
    
    @Query("SELECT * FROM reports WHERE isDownloaded = 0 AND status = 'COMPLETED'")
    suspend fun getPendingDownloads(): List<ReportEntity>
}

// Data classes for query results
data class ReportTypeCount(
    val reportType: String,
    val count: Int
)

data class FormatCount(
    val format: String,
    val count: Int
)

data class GeneratorCount(
    val generatedByName: String,
    val count: Int
) 