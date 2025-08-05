package com.aeci.mmucompanion.domain.repository

import com.aeci.mmucompanion.domain.model.TaskTimeEntry
import com.aeci.mmucompanion.domain.model.Todo
import com.aeci.mmucompanion.domain.model.JobCard
import com.aeci.mmucompanion.domain.model.TimeTrackingStatistics
import com.aeci.mmucompanion.domain.model.ProductivityMetrics
import com.aeci.mmucompanion.domain.model.JobCodeTimeSummary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface TimeTrackingRepository {
    
    // TaskTimeEntry CRUD Operations
    suspend fun createTimeEntry(timeEntry: TaskTimeEntry): TaskTimeEntry
    suspend fun getTimeEntryById(id: String): TaskTimeEntry?
    suspend fun updateTimeEntry(timeEntry: TaskTimeEntry): TaskTimeEntry
    suspend fun deleteTimeEntry(id: String): Boolean
    suspend fun getAllTimeEntries(): List<TaskTimeEntry>
    
    // TaskTimeEntry Queries
    suspend fun getTimeEntriesByDate(date: LocalDate): List<TaskTimeEntry>
    suspend fun getTimeEntriesByDateRange(startDate: LocalDate, endDate: LocalDate): List<TaskTimeEntry>
    suspend fun getTimeEntriesByJobCode(jobCode: String): List<TaskTimeEntry>
    suspend fun getTimeEntriesByUser(userId: String): List<TaskTimeEntry>
    
    // Active Time Tracking
    suspend fun startTimeTracking(
        userId: String,
        jobCode: String,
        description: String,
        todoId: String? = null,
        jobCardId: String? = null
    ): TaskTimeEntry
    
    suspend fun stopTimeTracking(timeEntryId: String): TaskTimeEntry?
    suspend fun getActiveTimeEntry(userId: String): TaskTimeEntry?
    suspend fun pauseTimeTracking(timeEntryId: String): TaskTimeEntry?
    suspend fun resumeTimeTracking(timeEntryId: String): TaskTimeEntry?
    
    // Todo Integration
    suspend fun getTimeEntriesForTodo(todoId: String): List<TaskTimeEntry>
    suspend fun getTotalTimeSpentOnTodo(todoId: String): Double // in hours
    suspend fun updateTodoTimeSpent(todoId: String, additionalHours: Double)
    suspend fun startTimerForTodo(todoId: String, userId: String): TaskTimeEntry
    suspend fun stopTimerForTodo(todoId: String): TaskTimeEntry?
    
    // Job Card Integration  
    suspend fun getTimeEntriesForJobCard(jobCardId: String): List<TaskTimeEntry>
    suspend fun getTotalTimeSpentOnJobCard(jobCardId: String): Double // in hours
    suspend fun updateJobCardTimeSpent(jobCardId: String, additionalHours: Double)
    suspend fun startTimerForJobCard(jobCardId: String, userId: String): TaskTimeEntry
    suspend fun stopTimerForJobCard(jobCardId: String): TaskTimeEntry?
    
    // Timesheet Operations
    suspend fun getTimesheetForDate(userId: String, date: LocalDate): List<TaskTimeEntry>
    suspend fun getTimesheetForWeek(userId: String, weekStartDate: LocalDate): List<TaskTimeEntry>
    suspend fun getTimesheetForMonth(userId: String, year: Int, month: Int): List<TaskTimeEntry>
    suspend fun calculateRegularHours(timeEntries: List<TaskTimeEntry>): Double
    suspend fun calculateOvertimeHours(timeEntries: List<TaskTimeEntry>): Double
    suspend fun submitTimesheet(userId: String, date: LocalDate): Boolean
    suspend fun approveTimesheet(userId: String, date: LocalDate, approvedBy: String): Boolean
    
    // Statistics and Reports
    suspend fun getTimeTrackingStatistics(userId: String, startDate: LocalDate, endDate: LocalDate): TimeTrackingStatistics
    suspend fun getProductivityMetrics(userId: String, startDate: LocalDate, endDate: LocalDate): ProductivityMetrics
    suspend fun getJobCodeSummary(jobCode: String, startDate: LocalDate, endDate: LocalDate): JobCodeTimeSummary
    
    // Real-time Updates
    fun getTimeEntriesFlow(): Flow<List<TaskTimeEntry>>
    fun getActiveTimeEntryFlow(userId: String): Flow<TaskTimeEntry?>
    fun getTimesheetFlow(userId: String, date: LocalDate): Flow<List<TaskTimeEntry>>
    
    // Sync Operations
    suspend fun syncTimeEntries(): Result<Unit>
    suspend fun getUnsyncedTimeEntries(): List<TaskTimeEntry>
    suspend fun markTimeEntryAsSynced(timeEntryId: String): Boolean
}

// Data classes for time tracking statistics
data class TimeTrackingStatistics(
    val totalHours: Double,
    val regularHours: Double,
    val overtimeHours: Double,
    val averageHoursPerDay: Double,
    val mostProductiveDay: LocalDate?,
    val topJobCodes: List<JobCodeSummary>,
    val todoCompletionRate: Double,
    val jobCardCompletionRate: Double
)

data class ProductivityMetrics(
    val tasksCompleted: Int,
    val averageTaskDuration: Double,
    val focusTimePercentage: Double,
    val breakTimePercentage: Double,
    val efficiencyScore: Double
)

data class JobCodeTimeSummary(
    val jobCode: String,
    val totalHours: Double,
    val numberOfEntries: Int,
    val averageEntryDuration: Double,
    val associatedTodos: Int,
    val associatedJobCards: Int
)

data class JobCodeSummary(
    val jobCode: String,
    val hours: Double,
    val percentage: Double
)
