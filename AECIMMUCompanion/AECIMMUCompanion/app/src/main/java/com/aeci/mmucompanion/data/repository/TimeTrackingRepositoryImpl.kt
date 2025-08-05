package com.aeci.mmucompanion.data.repository

import com.aeci.mmucompanion.data.local.dao.TaskTimeEntryDao
import com.aeci.mmucompanion.data.local.dao.TodoDao
import com.aeci.mmucompanion.data.local.dao.JobCardDao
import com.aeci.mmucompanion.data.local.entity.TaskTimeEntryEntity
import com.aeci.mmucompanion.data.remote.api.AECIApiService
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.domain.repository.TimeTrackingRepository
import com.aeci.mmucompanion.domain.repository.JobCodeSummary
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeTrackingRepositoryImpl @Inject constructor(
    private val taskTimeEntryDao: TaskTimeEntryDao,
    private val todoDao: TodoDao,
    private val jobCardDao: JobCardDao,
    private val apiService: AECIApiService,
    private val gson: Gson
) : TimeTrackingRepository {
    
    // TaskTimeEntry CRUD Operations
    override suspend fun createTimeEntry(timeEntry: TaskTimeEntry): TaskTimeEntry {
        return try {
            val entity = timeEntry.toEntity()
            taskTimeEntryDao.insertTimeEntry(entity)
            
            // Try to sync with server
            try {
                // Sync implementation would go here
            } catch (e: Exception) {
                // Offline mode - will sync later
            }
            
            timeEntry
        } catch (e: Exception) {
            throw Exception("Failed to create time entry: ${e.message}")
        }
    }
    
    override suspend fun getTimeEntryById(id: String): TaskTimeEntry? {
        return try {
            val entity = taskTimeEntryDao.getTimeEntryById(id)
            entity?.toTaskTimeEntry()
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun updateTimeEntry(timeEntry: TaskTimeEntry): TaskTimeEntry {
        return try {
            val entity = timeEntry.toEntity()
            taskTimeEntryDao.updateTimeEntry(entity)
            timeEntry
        } catch (e: Exception) {
            throw Exception("Failed to update time entry: ${e.message}")
        }
    }
    
    override suspend fun deleteTimeEntry(id: String): Boolean {
        return try {
            taskTimeEntryDao.deleteTimeEntry(id)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getAllTimeEntries(): List<TaskTimeEntry> {
        return try {
            val entities = taskTimeEntryDao.getAllTimeEntries()
            entities.mapNotNull { it.toTaskTimeEntry() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // TaskTimeEntry Queries
    override suspend fun getTimeEntriesByDate(date: LocalDate): List<TaskTimeEntry> {
        return try {
            val entities = taskTimeEntryDao.getTimeEntriesByDate(date.toString())
            entities.mapNotNull { it.toTaskTimeEntry() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getTimeEntriesByDateRange(startDate: LocalDate, endDate: LocalDate): List<TaskTimeEntry> {
        return try {
            val entities = taskTimeEntryDao.getTimeEntriesByDateRange(startDate.toString(), endDate.toString())
            entities.mapNotNull { it.toTaskTimeEntry() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getTimeEntriesByJobCode(jobCode: String): List<TaskTimeEntry> {
        return try {
            val entities = taskTimeEntryDao.getTimeEntriesByJobCode(jobCode)
            entities.mapNotNull { it.toTaskTimeEntry() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getTimeEntriesByUser(userId: String): List<TaskTimeEntry> {
        return try {
            val entities = taskTimeEntryDao.getTimeEntriesByUser(userId)
            entities.mapNotNull { it.toTaskTimeEntry() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Active Time Tracking
    override suspend fun startTimeTracking(
        userId: String,
        jobCode: String,
        description: String,
        todoId: String?,
        jobCardId: String?
    ): TaskTimeEntry {
        // Stop any existing active time tracking for this user
        getActiveTimeEntry(userId)?.let { activeEntry ->
            stopTimeTracking(activeEntry.id)
        }
        
        val timeEntry = TaskTimeEntry(
            id = java.util.UUID.randomUUID().toString(),
            userId = userId,
            date = LocalDate.now(),
            startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
            endTime = null,
            jobCode = jobCode,
            description = description,
            regularHours = 0.0,
            overtimeHours = 0.0,
            isActive = true,
            todoId = todoId,
            jobCardId = jobCardId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        return createTimeEntry(timeEntry)
    }
    
    override suspend fun stopTimeTracking(timeEntryId: String): TaskTimeEntry? {
        return try {
            val entity = taskTimeEntryDao.getTimeEntryById(timeEntryId) ?: return null
            val timeEntry = entity.toTaskTimeEntry() ?: return null
            
            if (!timeEntry.isActive) return timeEntry
            
            val now = LocalDateTime.now()
            val endTime = now.format(DateTimeFormatter.ofPattern("HH:mm"))
            
            // Calculate hours worked
            val startDateTime = LocalDateTime.of(
                timeEntry.date,
                java.time.LocalTime.parse(timeEntry.startTime, DateTimeFormatter.ofPattern("HH:mm"))
            )
            val endDateTime = now
            
            val totalMinutes = ChronoUnit.MINUTES.between(startDateTime, endDateTime)
            val totalHours = totalMinutes / 60.0
            
            // Calculate regular vs overtime (assuming 8 hours regular workday)
            val regularHours = minOf(totalHours, 8.0)
            val overtimeHours = maxOf(0.0, totalHours - 8.0)
            
            val updatedTimeEntry = timeEntry.copy(
                endTime = endTime,
                regularHours = regularHours,
                overtimeHours = overtimeHours,
                isActive = false,
                updatedAt = now
            )
            
            updateTimeEntry(updatedTimeEntry)
            
            // Update associated todo or job card
            timeEntry.todoId?.let { todoId ->
                updateTodoTimeSpent(todoId, totalHours)
            }
            
            timeEntry.jobCardId?.let { jobCardId ->
                updateJobCardTimeSpent(jobCardId, totalHours)
            }
            
            updatedTimeEntry
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getActiveTimeEntry(userId: String): TaskTimeEntry? {
        return try {
            val entity = taskTimeEntryDao.getActiveTimeEntry(userId)
            entity?.toTaskTimeEntry()
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun pauseTimeTracking(timeEntryId: String): TaskTimeEntry? {
        // Implementation for pausing time tracking
        return stopTimeTracking(timeEntryId)
    }
    
    override suspend fun resumeTimeTracking(timeEntryId: String): TaskTimeEntry? {
        return try {
            val timeEntry = getTimeEntryById(timeEntryId) ?: return null
            
            // Create a new time entry for the resumed session
            startTimeTracking(
                userId = timeEntry.userId,
                jobCode = timeEntry.jobCode,
                description = timeEntry.description,
                todoId = timeEntry.todoId,
                jobCardId = timeEntry.jobCardId
            )
        } catch (e: Exception) {
            null
        }
    }
    
    // Todo Integration
    override suspend fun getTimeEntriesForTodo(todoId: String): List<TaskTimeEntry> {
        return try {
            val entities = taskTimeEntryDao.getTimeEntriesForTodo(todoId)
            entities.mapNotNull { it.toTaskTimeEntry() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getTotalTimeSpentOnTodo(todoId: String): Double {
        return try {
            val timeEntries = getTimeEntriesForTodo(todoId)
            timeEntries.sumOf { it.regularHours + it.overtimeHours }
        } catch (e: Exception) {
            0.0
        }
    }
    
    override suspend fun updateTodoTimeSpent(todoId: String, additionalHours: Double) {
        try {
            val todo = todoDao.getTodoById(todoId) ?: return
            val totalTimeMinutes = (getTotalTimeSpentOnTodo(todoId) * 60).toLong()
            
            val updatedTodo = todo.copy(
                timeSpentMinutes = totalTimeMinutes,
                actualHours = getTotalTimeSpentOnTodo(todoId),
                lastWorkedOn = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            todoDao.updateTodo(updatedTodo)
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    override suspend fun startTimerForTodo(todoId: String, userId: String): TaskTimeEntry {
        val todo = todoDao.getTodoById(todoId)
        return startTimeTracking(
            userId = userId,
            jobCode = todo?.category?.name ?: "GENERAL",
            description = todo?.title ?: "Todo task",
            todoId = todoId,
            jobCardId = null
        )
    }
    
    override suspend fun stopTimerForTodo(todoId: String): TaskTimeEntry? {
        val activeEntry = taskTimeEntryDao.getActiveTimeEntryForTodo(todoId)
        return activeEntry?.let { stopTimeTracking(it.id) }
    }
    
    // Job Card Integration
    override suspend fun getTimeEntriesForJobCard(jobCardId: String): List<TaskTimeEntry> {
        return try {
            val entities = taskTimeEntryDao.getTimeEntriesForJobCard(jobCardId)
            entities.mapNotNull { it.toTaskTimeEntry() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getTotalTimeSpentOnJobCard(jobCardId: String): Double {
        return try {
            val timeEntries = getTimeEntriesForJobCard(jobCardId)
            timeEntries.sumOf { it.regularHours + it.overtimeHours }
        } catch (e: Exception) {
            0.0
        }
    }
    
    override suspend fun updateJobCardTimeSpent(jobCardId: String, additionalHours: Double) {
        try {
            val jobCard = jobCardDao.getJobCardById(jobCardId) ?: return
            val totalHours = getTotalTimeSpentOnJobCard(jobCardId)
            
            val updatedJobCard = jobCard.copy(
                actualHours = totalHours,
                updatedAt = LocalDateTime.now().toString()
            )
            
            jobCardDao.updateJobCard(updatedJobCard)
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    override suspend fun startTimerForJobCard(jobCardId: String, userId: String): TaskTimeEntry {
        val jobCard = jobCardDao.getJobCardById(jobCardId)
        return startTimeTracking(
            userId = userId,
            jobCode = jobCard?.category ?: "MAINTENANCE",
            description = jobCard?.title ?: "Job card task",
            todoId = null,
            jobCardId = jobCardId
        )
    }
    
    override suspend fun stopTimerForJobCard(jobCardId: String): TaskTimeEntry? {
        val activeEntry = taskTimeEntryDao.getActiveTimeEntryForJobCard(jobCardId)
        return activeEntry?.let { stopTimeTracking(it.id) }
    }
    
    // Timesheet Operations
    override suspend fun getTimesheetForDate(userId: String, date: LocalDate): List<TaskTimeEntry> {
        return try {
            val entities = taskTimeEntryDao.getTimesheetForDate(userId, date.toString())
            entities.mapNotNull { it.toTaskTimeEntry() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getTimesheetForWeek(userId: String, weekStartDate: LocalDate): List<TaskTimeEntry> {
        val weekEndDate = weekStartDate.plusDays(6)
        return getTimeEntriesByDateRange(weekStartDate, weekEndDate)
            .filter { it.userId == userId }
    }
    
    override suspend fun getTimesheetForMonth(userId: String, year: Int, month: Int): List<TaskTimeEntry> {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.withDayOfMonth(startDate.lengthOfMonth())
        return getTimeEntriesByDateRange(startDate, endDate)
            .filter { it.userId == userId }
    }
    
    override suspend fun calculateRegularHours(timeEntries: List<TaskTimeEntry>): Double {
        return timeEntries.sumOf { it.regularHours }
    }
    
    override suspend fun calculateOvertimeHours(timeEntries: List<TaskTimeEntry>): Double {
        return timeEntries.sumOf { it.overtimeHours }
    }
    
    override suspend fun submitTimesheet(userId: String, date: LocalDate): Boolean {
        // Implementation for submitting timesheet
        return true
    }
    
    override suspend fun approveTimesheet(userId: String, date: LocalDate, approvedBy: String): Boolean {
        // Implementation for approving timesheet
        return true
    }
    
    // Statistics and Reports
    override suspend fun getTimeTrackingStatistics(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): TimeTrackingStatistics {
        val timeEntries = getTimeEntriesByDateRange(startDate, endDate)
            .filter { it.userId == userId }
        
        val totalHours = timeEntries.sumOf { it.regularHours + it.overtimeHours }
        val regularHours = timeEntries.sumOf { it.regularHours }
        val overtimeHours = timeEntries.sumOf { it.overtimeHours }
        
        val daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1
        val averageHoursPerDay = if (daysBetween > 0) totalHours / daysBetween else 0.0
        
        val jobCodeSummaries = timeEntries
            .groupBy { it.jobCode }
            .map { (jobCode, entries) ->
                val hours = entries.sumOf { it.regularHours + it.overtimeHours }
                JobCodeSummary(
                    jobCode = jobCode,
                    hours = hours,
                    percentage = if (totalHours > 0) (hours / totalHours) * 100 else 0.0
                )
            }
            .sortedByDescending { it.hours }
        
        val mostProductiveDay = timeEntries
            .groupBy { it.date }
            .maxByOrNull { (_, entries) -> 
                entries.sumOf { entry -> entry.regularHours + entry.overtimeHours } 
            }
            ?.key
        
        return TimeTrackingStatistics(
            totalTimeWorked = totalHours.toLong(),
            totalTasks = 0,
            averageTaskTime = if (totalHours > 0) (totalHours * 60).toLong() else 0L,
            completedTasks = 0,
            pendingTasks = 0,
            totalBreakTime = 0L,
            productivityScore = 0.0,
            mostProductiveHour = 9,
            dailyAverage = if (averageHoursPerDay > 0) (averageHoursPerDay * 60).toLong() else 0L,
            weeklyTotal = (totalHours * 60).toLong(),
            monthlyTotal = (totalHours * 60).toLong(),
            overtimeHours = overtimeHours.toLong()
        )
    }
    
    override suspend fun getProductivityMetrics(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): ProductivityMetrics {
        // Implementation for productivity metrics
        return ProductivityMetrics(
            efficiency = 0.0,
            tasksPerHour = 0.0,
            averageTaskDuration = 0L,
            completionRate = 0.0,
            qualityScore = 0.0,
            timeUtilization = 0.0,
            breakEfficiency = 0.0,
            performanceScore = 0.0,
            consistencyRating = 0.0,
            improvementTrend = 0.0,
            benchmarkComparison = 0.0,
            recommendedBreakTime = 0L
        )
    }
    
    override suspend fun getJobCodeSummary(
        jobCode: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): JobCodeTimeSummary {
        val timeEntries = getTimeEntriesByDateRange(startDate, endDate)
            .filter { it.jobCode == jobCode }
        
        val totalHours = timeEntries.sumOf { it.regularHours + it.overtimeHours }
        val averageEntryDuration = if (timeEntries.isNotEmpty()) {
            totalHours / timeEntries.size
        } else 0.0
        
        return JobCodeTimeSummary(
            jobCode = jobCode,
            jobTitle = "",
            totalTime = totalHours.toLong(),
            taskCount = timeEntries.size,
            averageTime = averageEntryDuration.toLong(),
            minTime = 0L,
            maxTime = 0L,
            completionRate = 0.0,
            efficiencyScore = 0.0,
            lastUpdated = System.currentTimeMillis(),
            category = "",
            priority = "MEDIUM",
            estimatedTime = 0L,
            actualTime = totalHours.toLong(),
            variance = 0.0
        )
    }
    
    // Real-time Updates
    override fun getTimeEntriesFlow(): Flow<List<TaskTimeEntry>> = flow {
        taskTimeEntryDao.getAllTimeEntriesFlow().collect { entities ->
            val timeEntries = entities.mapNotNull { it.toTaskTimeEntry() }
            emit(timeEntries)
        }
    }
    
    override fun getActiveTimeEntryFlow(userId: String): Flow<TaskTimeEntry?> = flow {
        taskTimeEntryDao.getActiveTimeEntryFlow(userId).collect { entity ->
            emit(entity?.toTaskTimeEntry())
        }
    }
    
    override fun getTimesheetFlow(userId: String, date: LocalDate): Flow<List<TaskTimeEntry>> = flow {
        taskTimeEntryDao.getTimesheetFlow(userId, date.toString()).collect { entities ->
            val timeEntries = entities.mapNotNull { it.toTaskTimeEntry() }
            emit(timeEntries)
        }
    }
    
    // Sync Operations
    override suspend fun syncTimeEntries(): Result<Unit> {
        return try {
            // Implementation for syncing time entries with server
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUnsyncedTimeEntries(): List<TaskTimeEntry> {
        return try {
            val entities = taskTimeEntryDao.getUnsyncedTimeEntries()
            entities.mapNotNull { it.toTaskTimeEntry() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun markTimeEntryAsSynced(timeEntryId: String): Boolean {
        return try {
            taskTimeEntryDao.markTimeEntryAsSynced(timeEntryId)
            true
        } catch (e: Exception) {
            false
        }
    }
}

// Extension functions for entity conversion
private fun TaskTimeEntry.toEntity(): TaskTimeEntryEntity {
    return TaskTimeEntryEntity(
        id = id,
        userId = userId,
        date = date.toString(),
        startTime = startTime,
        endTime = endTime,
        jobCode = jobCode,
        description = description,
        regularHours = regularHours,
        overtimeHours = overtimeHours,
        isActive = isActive,
        todoId = todoId,
        jobCardId = jobCardId,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
        synced = false
    )
}

private fun TaskTimeEntryEntity.toTaskTimeEntry(): TaskTimeEntry? {
    return try {
        TaskTimeEntry(
            id = id,
            userId = userId,
            date = LocalDate.parse(date),
            startTime = startTime,
            endTime = endTime,
            jobCode = jobCode,
            description = description,
            regularHours = regularHours,
            overtimeHours = overtimeHours,
            isActive = isActive,
            todoId = todoId,
            jobCardId = jobCardId,
            createdAt = LocalDateTime.parse(createdAt),
            updatedAt = LocalDateTime.parse(updatedAt)
        )
    } catch (e: Exception) {
        null
    }
}
