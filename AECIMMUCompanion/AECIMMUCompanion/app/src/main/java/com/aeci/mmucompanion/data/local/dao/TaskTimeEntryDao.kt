package com.aeci.mmucompanion.data.local.dao

import androidx.room.*
import com.aeci.mmucompanion.data.local.entity.TaskTimeEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskTimeEntryDao {
    
    // Basic CRUD Operations
    @Query("SELECT * FROM task_time_entries WHERE id = :id")
    suspend fun getTimeEntryById(id: String): TaskTimeEntryEntity?
    
    @Query("SELECT * FROM task_time_entries ORDER BY date DESC, startTime DESC")
    suspend fun getAllTimeEntries(): List<TaskTimeEntryEntity>
    
    @Query("SELECT * FROM task_time_entries WHERE date = :date ORDER BY startTime ASC")
    suspend fun getTimeEntriesByDate(date: String): List<TaskTimeEntryEntity>
    
    @Query("SELECT * FROM task_time_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, startTime DESC")
    suspend fun getTimeEntriesByDateRange(startDate: String, endDate: String): List<TaskTimeEntryEntity>
    
    @Query("SELECT * FROM task_time_entries WHERE jobCode = :jobCode ORDER BY date DESC, startTime DESC")
    suspend fun getTimeEntriesByJobCode(jobCode: String): List<TaskTimeEntryEntity>
    
    @Query("SELECT * FROM task_time_entries WHERE userId = :userId ORDER BY date DESC, startTime DESC")
    suspend fun getTimeEntriesByUser(userId: String): List<TaskTimeEntryEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeEntry(timeEntry: TaskTimeEntryEntity)
    
    @Update
    suspend fun updateTimeEntry(timeEntry: TaskTimeEntryEntity)
    
    @Query("DELETE FROM task_time_entries WHERE id = :id")
    suspend fun deleteTimeEntry(id: String)
    
    @Delete
    suspend fun deleteTimeEntry(timeEntry: TaskTimeEntryEntity)
    
    // Active Time Tracking
    @Query("SELECT * FROM task_time_entries WHERE userId = :userId AND isActive = 1 LIMIT 1")
    suspend fun getActiveTimeEntry(userId: String): TaskTimeEntryEntity?
    
    @Query("SELECT * FROM task_time_entries WHERE isActive = 1")
    suspend fun getAllActiveTimeEntries(): List<TaskTimeEntryEntity>
    
    // Todo Integration
    @Query("SELECT * FROM task_time_entries WHERE todoId = :todoId ORDER BY date DESC, startTime DESC")
    suspend fun getTimeEntriesForTodo(todoId: String): List<TaskTimeEntryEntity>
    
    @Query("SELECT * FROM task_time_entries WHERE todoId = :todoId AND isActive = 1 LIMIT 1")
    suspend fun getActiveTimeEntryForTodo(todoId: String): TaskTimeEntryEntity?
    
    @Query("SELECT SUM(regularHours + overtimeHours) FROM task_time_entries WHERE todoId = :todoId")
    suspend fun getTotalHoursForTodo(todoId: String): Double?
    
    // Job Card Integration
    @Query("SELECT * FROM task_time_entries WHERE jobCardId = :jobCardId ORDER BY date DESC, startTime DESC")
    suspend fun getTimeEntriesForJobCard(jobCardId: String): List<TaskTimeEntryEntity>
    
    @Query("SELECT * FROM task_time_entries WHERE jobCardId = :jobCardId AND isActive = 1 LIMIT 1")
    suspend fun getActiveTimeEntryForJobCard(jobCardId: String): TaskTimeEntryEntity?
    
    @Query("SELECT SUM(regularHours + overtimeHours) FROM task_time_entries WHERE jobCardId = :jobCardId")
    suspend fun getTotalHoursForJobCard(jobCardId: String): Double?
    
    // Timesheet Operations
    @Query("SELECT * FROM task_time_entries WHERE userId = :userId AND date = :date ORDER BY startTime ASC")
    suspend fun getTimesheetForDate(userId: String, date: String): List<TaskTimeEntryEntity>
    
    @Query("SELECT * FROM task_time_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC, startTime ASC")
    suspend fun getTimesheetForPeriod(userId: String, startDate: String, endDate: String): List<TaskTimeEntryEntity>
    
    // Statistics Queries
    @Query("SELECT SUM(regularHours + overtimeHours) FROM task_time_entries WHERE jobCode = :jobCode")
    suspend fun getTotalHoursForJob(jobCode: String): Double?
    
    @Query("SELECT SUM(regularHours + overtimeHours) FROM task_time_entries WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalHoursInPeriod(startDate: String, endDate: String): Double?
    
    @Query("SELECT SUM(regularHours) FROM task_time_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getRegularHoursForUser(userId: String, startDate: String, endDate: String): Double?
    
    @Query("SELECT SUM(overtimeHours) FROM task_time_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getOvertimeHoursForUser(userId: String, startDate: String, endDate: String): Double?
    
    // Sync Operations
    @Query("SELECT * FROM task_time_entries WHERE synced = 0")
    suspend fun getUnsyncedTimeEntries(): List<TaskTimeEntryEntity>
    
    @Query("UPDATE task_time_entries SET synced = 1 WHERE id = :timeEntryId")
    suspend fun markTimeEntryAsSynced(timeEntryId: String)
    
    @Query("UPDATE task_time_entries SET synced = 0 WHERE id = :timeEntryId")
    suspend fun markTimeEntryAsUnsynced(timeEntryId: String)
    
    // Bulk Operations
    @Query("DELETE FROM task_time_entries WHERE jobCode = :jobCode")
    suspend fun deleteTimeEntriesForJob(jobCode: String)
    
    @Query("DELETE FROM task_time_entries WHERE date = :date")
    suspend fun deleteTimeEntriesForDate(date: String)
    
    @Query("DELETE FROM task_time_entries WHERE userId = :userId")
    suspend fun deleteTimeEntriesForUser(userId: String)
    
    @Query("DELETE FROM task_time_entries WHERE todoId = :todoId")
    suspend fun deleteTimeEntriesForTodo(todoId: String)
    
    @Query("DELETE FROM task_time_entries WHERE jobCardId = :jobCardId")
    suspend fun deleteTimeEntriesForJobCard(jobCardId: String)
    
    // Flow queries for real-time updates
    @Query("SELECT * FROM task_time_entries ORDER BY date DESC, startTime DESC")
    fun getAllTimeEntriesFlow(): Flow<List<TaskTimeEntryEntity>>
    
    @Query("SELECT * FROM task_time_entries WHERE userId = :userId AND isActive = 1 LIMIT 1")
    fun getActiveTimeEntryFlow(userId: String): Flow<TaskTimeEntryEntity?>
    
    @Query("SELECT * FROM task_time_entries WHERE userId = :userId AND date = :date ORDER BY startTime ASC")
    fun getTimesheetFlow(userId: String, date: String): Flow<List<TaskTimeEntryEntity>>
    
    @Query("SELECT * FROM task_time_entries WHERE jobCode = :jobCode ORDER BY date DESC, startTime DESC")
    fun getTimeEntriesForJobFlow(jobCode: String): Flow<List<TaskTimeEntryEntity>>
    
    @Query("SELECT * FROM task_time_entries WHERE date = :date ORDER BY startTime DESC")
    fun getTimeEntriesForDateFlow(date: String): Flow<List<TaskTimeEntryEntity>>
    
    @Query("SELECT * FROM task_time_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, startTime DESC")
    fun getTimeEntriesInPeriodFlow(startDate: String, endDate: String): Flow<List<TaskTimeEntryEntity>>
    
    @Query("SELECT * FROM task_time_entries WHERE todoId = :todoId ORDER BY date DESC, startTime DESC")
    fun getTimeEntriesForTodoFlow(todoId: String): Flow<List<TaskTimeEntryEntity>>
    
    @Query("SELECT * FROM task_time_entries WHERE jobCardId = :jobCardId ORDER BY date DESC, startTime DESC")
    fun getTimeEntriesForJobCardFlow(jobCardId: String): Flow<List<TaskTimeEntryEntity>>
}
