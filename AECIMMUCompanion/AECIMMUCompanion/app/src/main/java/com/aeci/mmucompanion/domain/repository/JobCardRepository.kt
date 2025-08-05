package com.aeci.mmucompanion.domain.repository

import com.aeci.mmucompanion.domain.model.*
import kotlinx.coroutines.flow.Flow

interface JobCardRepository {
    
    // Basic CRUD Operations
    suspend fun createJobCard(jobCard: JobCard): JobCard
    suspend fun getJobCardById(id: String): JobCard?
    suspend fun updateJobCard(jobCard: JobCard): JobCard
    suspend fun deleteJobCard(id: String): Boolean
    suspend fun getAllJobCards(): List<JobCard>
    
    // Job Card Queries
    suspend fun getJobCardsByUser(userId: String): List<JobCard>
    suspend fun getJobCardsByEquipment(equipmentId: String): List<JobCard>
    suspend fun getJobCardsByStatus(status: JobCardStatus): List<JobCard>
    suspend fun getJobCardsByPriority(priority: JobCardPriority): List<JobCard>
    suspend fun getJobCardsByCategory(category: JobCardCategory): List<JobCard>
    suspend fun getJobCardsByFilters(filters: JobCardFilters): List<JobCard>
    suspend fun getOverdueJobCards(): List<JobCard>
    suspend fun getDueSoonJobCards(days: Int = 3): List<JobCard>
    
    // Real-time Updates
    fun getJobCardsFlow(): Flow<List<JobCard>>
    fun getJobCardsByUserFlow(userId: String): Flow<List<JobCard>>
    fun getJobCardsByStatusFlow(status: JobCardStatus): Flow<List<JobCard>>
    
    // Job Card Progress
    suspend fun addJobCardProgress(progress: JobCardProgress): JobCardProgress
    suspend fun getJobCardProgress(jobCardId: String): List<JobCardProgress>
    suspend fun updateJobCardProgress(progress: JobCardProgress): JobCardProgress
    
    // Time Tracking Integration
    suspend fun startTimeEntry(timeEntry: JobCardTimeEntry): JobCardTimeEntry
    suspend fun endTimeEntry(timeEntryId: String, endTime: java.time.LocalDateTime): JobCardTimeEntry
    suspend fun getTimeEntries(jobCardId: String): List<JobCardTimeEntry>
    suspend fun updateTimeEntry(timeEntry: JobCardTimeEntry): JobCardTimeEntry
    
    // Enhanced Time Tracking with TaskTimeEntry model integration
    suspend fun startTimeTracking(jobCardId: String, userId: String): TaskTimeEntry
    suspend fun stopTimeTracking(jobCardId: String): TaskTimeEntry?
    suspend fun getTimeEntriesForJobCard(jobCardId: String): List<TaskTimeEntry>
    suspend fun getTotalTimeSpentOnJobCard(jobCardId: String): Double // in hours
    suspend fun getActiveTimeEntry(jobCardId: String): TaskTimeEntry?
    suspend fun pauseTimeTracking(jobCardId: String): TaskTimeEntry?
    suspend fun resumeTimeTracking(jobCardId: String): TaskTimeEntry?
    
    // Comments
    suspend fun addComment(comment: JobCardComment): JobCardComment
    suspend fun getComments(jobCardId: String): List<JobCardComment>
    suspend fun updateComment(comment: JobCardComment): JobCardComment
    suspend fun deleteComment(commentId: String): Boolean
    
    // Job Card Templates
    suspend fun createTemplate(template: JobCardTemplate): JobCardTemplate
    suspend fun getTemplateById(id: String): JobCardTemplate?
    suspend fun getTemplatesByCategory(category: JobCardCategory): List<JobCardTemplate>
    suspend fun getAllTemplates(): List<JobCardTemplate>
    suspend fun updateTemplate(template: JobCardTemplate): JobCardTemplate
    suspend fun deleteTemplate(id: String): Boolean
    
    // Job Card from Template
    suspend fun createJobCardFromTemplate(
        templateId: String,
        equipmentId: String,
        assignedTo: String?,
        dueDate: java.time.LocalDate?,
        additionalNotes: String?
    ): JobCard
    
    // Bulk Operations
    suspend fun bulkAssignJobCards(jobCardIds: List<String>, assignedTo: String): Boolean
    suspend fun bulkUpdateStatus(jobCardIds: List<String>, status: JobCardStatus): Boolean
    suspend fun bulkUpdatePriority(jobCardIds: List<String>, priority: JobCardPriority): Boolean
    suspend fun bulkDeleteJobCards(jobCardIds: List<String>): Boolean
    
    // Statistics
    suspend fun getJobCardStatistics(): JobCardStatistics
    suspend fun getJobCardStatisticsByUser(userId: String): JobCardStatistics
    suspend fun getJobCardStatisticsByEquipment(equipmentId: String): JobCardStatistics
    suspend fun getJobCardStatisticsByDateRange(startDate: java.time.LocalDate, endDate: java.time.LocalDate): JobCardStatistics
    
    // Export
    suspend fun exportJobCards(request: JobCardExportRequest): String
    
    // Notifications
    suspend fun createNotification(notification: JobCardNotification): JobCardNotification
    suspend fun getNotifications(userId: String): List<JobCardNotification>
    suspend fun markNotificationAsRead(notificationId: String): Boolean
    suspend fun deleteNotification(notificationId: String): Boolean
    
    // Task Integration
    suspend fun createJobCardFromTask(taskId: String, assignedTo: String?): JobCard
    suspend fun linkJobCardToTask(jobCardId: String, taskId: String): Boolean
    suspend fun getJobCardsByTask(taskId: String): List<JobCard>
    
    // Workflow Management
    suspend fun createWorkflow(workflow: JobCardWorkflow): JobCardWorkflow
    suspend fun getWorkflowById(id: String): JobCardWorkflow?
    suspend fun getAllWorkflows(): List<JobCardWorkflow>
    suspend fun updateWorkflow(workflow: JobCardWorkflow): JobCardWorkflow
    suspend fun deleteWorkflow(id: String): Boolean
    
    // Search and Filter
    suspend fun searchJobCards(query: String): List<JobCard>
    suspend fun getJobCardsByDateRange(startDate: java.time.LocalDate, endDate: java.time.LocalDate): List<JobCard>
    suspend fun getJobCardsByLocation(location: String): List<JobCard>
    
    // Sync Operations
    suspend fun syncJobCards(): Result<Unit>
    suspend fun getUnsyncedJobCards(): List<JobCard>
    suspend fun markJobCardAsSynced(jobCardId: String): Boolean
} 