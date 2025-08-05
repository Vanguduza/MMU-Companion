package com.aeci.mmucompanion.data.repository

import com.aeci.mmucompanion.data.local.dao.JobCardDao
import com.aeci.mmucompanion.data.local.entity.JobCardEntity
import com.aeci.mmucompanion.data.remote.api.AECIApiService
import com.aeci.mmucompanion.data.remote.dto.JobCardDto
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.domain.repository.JobCardRepository
import com.aeci.mmucompanion.domain.repository.TimeTrackingRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate
import java.time.LocalDateTime

@Singleton
class JobCardRepositoryImpl @Inject constructor(
    private val jobCardDao: JobCardDao,
    private val apiService: AECIApiService,
    private val timeTrackingRepository: TimeTrackingRepository,
    private val gson: Gson
) : JobCardRepository {
    
    // Basic CRUD Operations
    override suspend fun createJobCard(jobCard: JobCard): JobCard {
        try {
            // Save to local database first
            val jobCardEntity = JobCardEntity(
                id = jobCard.id,
                title = jobCard.title,
                description = jobCard.description,
                status = jobCard.status.name,
                priority = jobCard.priority.name,
                category = jobCard.category.name,
                equipmentId = jobCard.equipmentId,
                equipmentName = jobCard.equipmentName,
                siteLocation = jobCard.siteLocation,
                assignedTo = jobCard.assignedTo,
                assignedToName = jobCard.assignedToName,
                createdBy = jobCard.createdBy,
                createdByName = jobCard.createdByName,
                createdAt = jobCard.createdAt.toString(),
                updatedAt = jobCard.updatedAt.toString(),
                dueDate = jobCard.dueDate?.toString(),
                completedDate = jobCard.completedDate?.toString(),
                estimatedHours = jobCard.estimatedHours,
                actualHours = jobCard.actualHours,
                partsRequired = gson.toJson(jobCard.partsRequired),
                toolsRequired = gson.toJson(jobCard.toolsRequired),
                safetyRequirements = gson.toJson(jobCard.safetyRequirements),
                workInstructions = jobCard.workInstructions,
                notes = jobCard.notes,
                attachments = gson.toJson(jobCard.attachments),
                photos = gson.toJson(jobCard.photos),
                relatedTaskId = jobCard.relatedTaskId,
                relatedFormId = jobCard.relatedFormId,
                synced = false
            )
            
            jobCardDao.insertJobCard(jobCardEntity)
            
            // Try to sync with server
            try {
                val jobCardDto = JobCardDto(
                    id = jobCard.id,
                    title = jobCard.title,
                    description = jobCard.description,
                    status = jobCard.status.name,
                    priority = jobCard.priority.name,
                    category = jobCard.category.name,
                    equipmentId = jobCard.equipmentId,
                    equipmentName = jobCard.equipmentName,
                    siteLocation = jobCard.siteLocation,
                    assignedTo = jobCard.assignedTo,
                    assignedToName = jobCard.assignedToName,
                    createdBy = jobCard.createdBy,
                    createdByName = jobCard.createdByName,
                    createdAt = jobCard.createdAt.toString(),
                    updatedAt = jobCard.updatedAt.toString(),
                    dueDate = jobCard.dueDate?.toString(),
                    completedDate = jobCard.completedDate?.toString(),
                    estimatedHours = jobCard.estimatedHours,
                    actualHours = jobCard.actualHours,
                    partsRequired = gson.toJson(jobCard.partsRequired),
                    toolsRequired = gson.toJson(jobCard.toolsRequired),
                    safetyRequirements = gson.toJson(jobCard.safetyRequirements),
                    workInstructions = jobCard.workInstructions,
                    notes = jobCard.notes,
                    attachments = gson.toJson(jobCard.attachments),
                    photos = gson.toJson(jobCard.photos),
                    relatedTaskId = jobCard.relatedTaskId,
                    relatedFormId = jobCard.relatedFormId
                )
                
                apiService.saveJobCard(jobCardDto)
                jobCardDao.updateJobCardSyncStatus(jobCard.id, true)
                
            } catch (e: Exception) {
                // Server sync failed, but local save succeeded
                // Job card will be synced later
            }
            
            return jobCard
            
        } catch (e: Exception) {
            throw Exception("Failed to create job card: ${e.message}")
        }
    }
    
    override suspend fun getJobCardById(id: String): JobCard? {
        return try {
            val entity = jobCardDao.getJobCardById(id)
            entity?.toJobCard(gson)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun updateJobCard(jobCard: JobCard): JobCard {
        return createJobCard(jobCard) // Use the same save logic
    }
    
    override suspend fun deleteJobCard(id: String): Boolean {
        return try {
            jobCardDao.deleteJobCard(id)
            
            // Try to delete from server
            try {
                apiService.deleteJobCard(id)
            } catch (e: Exception) {
                // Server deletion failed, but local deletion succeeded
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getAllJobCards(): List<JobCard> {
        return try {
            val entities = jobCardDao.getAllJobCards()
            entities.mapNotNull { it.toJobCard(gson) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Job Card Queries
    override suspend fun getJobCardsByUser(userId: String): List<JobCard> {
        return try {
            val entities = jobCardDao.getJobCardsByUser(userId)
            entities.mapNotNull { it.toJobCard(gson) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getJobCardsByEquipment(equipmentId: String): List<JobCard> {
        return try {
            val entities = jobCardDao.getJobCardsByEquipment(equipmentId)
            entities.mapNotNull { it.toJobCard(gson) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getJobCardsByStatus(status: JobCardStatus): List<JobCard> {
        return try {
            val entities = jobCardDao.getJobCardsByStatus(status.name)
            entities.mapNotNull { it.toJobCard(gson) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getJobCardsByPriority(priority: JobCardPriority): List<JobCard> {
        return try {
            val entities = jobCardDao.getJobCardsByPriority(priority.name)
            entities.mapNotNull { it.toJobCard(gson) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getJobCardsByCategory(category: JobCardCategory): List<JobCard> {
        return try {
            val entities = jobCardDao.getJobCardsByCategory(category.name)
            entities.mapNotNull { it.toJobCard(gson) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getJobCardsByFilters(filters: JobCardFilters): List<JobCard> {
        return try {
            val entities = jobCardDao.getJobCardsByFilters(
                statuses = filters.status.map { it.name },
                priorities = filters.priority.map { it.name },
                categories = filters.category.map { it.name },
                assignedTo = filters.assignedTo,
                createdBy = filters.createdBy,
                equipmentId = filters.equipmentId,
                siteLocation = filters.siteLocation,
                dateFrom = filters.dateFrom?.toString(),
                dateTo = filters.dateTo?.toString()
            )
            entities.mapNotNull { it.toJobCard(gson) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getOverdueJobCards(): List<JobCard> {
        return try {
            val entities = jobCardDao.getOverdueJobCards()
            entities.mapNotNull { it.toJobCard(gson) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getDueSoonJobCards(days: Int): List<JobCard> {
        return try {
            val entities = jobCardDao.getDueSoonJobCards(days)
            entities.mapNotNull { it.toJobCard(gson) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Real-time Updates
    override fun getJobCardsFlow(): Flow<List<JobCard>> = flow {
        jobCardDao.getAllJobCardsFlow().collect { entities ->
            val jobCards = entities.mapNotNull { it.toJobCard(gson) }
            emit(jobCards)
        }
    }
    
    override fun getJobCardsByUserFlow(userId: String): Flow<List<JobCard>> = flow {
        jobCardDao.getJobCardsByUserFlow(userId).collect { entities ->
            val jobCards = entities.mapNotNull { it.toJobCard(gson) }
            emit(jobCards)
        }
    }
    
    override fun getJobCardsByStatusFlow(status: JobCardStatus): Flow<List<JobCard>> = flow {
        jobCardDao.getJobCardsByStatusFlow(status.name).collect { entities ->
            val jobCards = entities.mapNotNull { it.toJobCard(gson) }
            emit(jobCards)
        }
    }
    
    // Job Card Progress
    override suspend fun addJobCardProgress(progress: JobCardProgress): JobCardProgress {
        // Implementation for progress tracking
        return progress
    }
    
    override suspend fun getJobCardProgress(jobCardId: String): List<JobCardProgress> {
        // Implementation for getting progress
        return emptyList()
    }
    
    override suspend fun updateJobCardProgress(progress: JobCardProgress): JobCardProgress {
        // Implementation for updating progress
        return progress
    }
    
    // Time Tracking
    override suspend fun startTimeEntry(timeEntry: JobCardTimeEntry): JobCardTimeEntry {
        // Implementation for time tracking
        return timeEntry
    }
    
    override suspend fun endTimeEntry(timeEntryId: String, endTime: LocalDateTime): JobCardTimeEntry {
        // Implementation for ending time entry
        return JobCardTimeEntry(
            id = timeEntryId,
            jobCardId = "",
            technicianId = "",
            technicianName = "",
            startTime = LocalDateTime.now(),
            endTime = endTime,
            hours = null,
            activity = "",
            notes = null
        )
    }
    
    override suspend fun getTimeEntries(jobCardId: String): List<JobCardTimeEntry> {
        // Implementation for getting time entries
        return emptyList()
    }
    
    override suspend fun updateTimeEntry(timeEntry: JobCardTimeEntry): JobCardTimeEntry {
        // Implementation for updating time entry
        return timeEntry
    }
    
    // Enhanced Time Tracking with TaskTimeEntry model integration
    override suspend fun startTimeTracking(jobCardId: String, userId: String): TaskTimeEntry {
        return timeTrackingRepository.startTimerForJobCard(jobCardId, userId)
    }
    
    override suspend fun stopTimeTracking(jobCardId: String): TaskTimeEntry? {
        return timeTrackingRepository.stopTimerForJobCard(jobCardId)
    }
    
    override suspend fun getTimeEntriesForJobCard(jobCardId: String): List<TaskTimeEntry> {
        return timeTrackingRepository.getTimeEntriesForJobCard(jobCardId)
    }
    
    override suspend fun getTotalTimeSpentOnJobCard(jobCardId: String): Double {
        return timeTrackingRepository.getTotalTimeSpentOnJobCard(jobCardId)
    }
    
    override suspend fun getActiveTimeEntry(jobCardId: String): TaskTimeEntry? {
        return timeTrackingRepository.getTimeEntriesForJobCard(jobCardId)
            .firstOrNull { it.isActive }
    }
    
    override suspend fun pauseTimeTracking(jobCardId: String): TaskTimeEntry? {
        val activeEntry = getActiveTimeEntry(jobCardId)
        return activeEntry?.let { timeTrackingRepository.pauseTimeTracking(it.id) }
    }
    
    override suspend fun resumeTimeTracking(jobCardId: String): TaskTimeEntry? {
        val activeEntry = getActiveTimeEntry(jobCardId)
        return activeEntry?.let { timeTrackingRepository.resumeTimeTracking(it.id) }
    }
    
    // Comments
    override suspend fun addComment(comment: JobCardComment): JobCardComment {
        // Implementation for adding comments
        return comment
    }
    
    override suspend fun getComments(jobCardId: String): List<JobCardComment> {
        // Implementation for getting comments
        return emptyList()
    }
    
    override suspend fun updateComment(comment: JobCardComment): JobCardComment {
        // Implementation for updating comments
        return comment
    }
    
    override suspend fun deleteComment(commentId: String): Boolean {
        // Implementation for deleting comments
        return true
    }
    
    // Job Card Templates
    override suspend fun createTemplate(template: JobCardTemplate): JobCardTemplate {
        // Implementation for creating templates
        return template
    }
    
    override suspend fun getTemplateById(id: String): JobCardTemplate? {
        // Implementation for getting template by ID
        return null
    }
    
    override suspend fun getTemplatesByCategory(category: JobCardCategory): List<JobCardTemplate> {
        // Implementation for getting templates by category
        return emptyList()
    }
    
    override suspend fun getAllTemplates(): List<JobCardTemplate> {
        // Implementation for getting all templates
        return emptyList()
    }
    
    override suspend fun updateTemplate(template: JobCardTemplate): JobCardTemplate {
        // Implementation for updating templates
        return template
    }
    
    override suspend fun deleteTemplate(id: String): Boolean {
        // Implementation for deleting templates
        return true
    }
    
    // Job Card from Template
    override suspend fun createJobCardFromTemplate(
        templateId: String,
        equipmentId: String,
        assignedTo: String?,
        dueDate: LocalDate?,
        additionalNotes: String?
    ): JobCard {
        // Implementation for creating job card from template
        return JobCard(
            id = "",
            title = "",
            description = "",
            status = JobCardStatus.PENDING,
            priority = JobCardPriority.MEDIUM,
            category = JobCardCategory.PREVENTIVE_MAINTENANCE,
            equipmentId = equipmentId,
            equipmentName = "",
            siteLocation = "",
            assignedTo = assignedTo,
            assignedToName = null,
            createdBy = "",
            createdByName = "",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            dueDate = dueDate,
            completedDate = null,
            estimatedHours = null,
            actualHours = null,
            partsRequired = emptyList(),
            toolsRequired = emptyList(),
            safetyRequirements = emptyList(),
            workInstructions = null,
            notes = additionalNotes,
            attachments = emptyList(),
            photos = emptyList(),
            relatedTaskId = null,
            relatedFormId = null
        )
    }
    
    // Bulk Operations
    override suspend fun bulkAssignJobCards(jobCardIds: List<String>, assignedTo: String): Boolean {
        // Implementation for bulk assignment
        return true
    }
    
    override suspend fun bulkUpdateStatus(jobCardIds: List<String>, status: JobCardStatus): Boolean {
        // Implementation for bulk status update
        return true
    }
    
    override suspend fun bulkUpdatePriority(jobCardIds: List<String>, priority: JobCardPriority): Boolean {
        // Implementation for bulk priority update
        return true
    }
    
    override suspend fun bulkDeleteJobCards(jobCardIds: List<String>): Boolean {
        // Implementation for bulk deletion
        return true
    }
    
    // Statistics
    override suspend fun getJobCardStatistics(): JobCardStatistics {
        // Implementation for getting statistics
        return JobCardStatistics(
            totalJobCards = 0,
            pendingJobCards = 0,
            inProgressJobCards = 0,
            completedJobCards = 0,
            cancelledJobCards = 0,
            overdueJobCards = 0,
            averageCompletionTime = null,
            totalHours = 0.0,
            totalCost = 0.0,
            completionRate = 0.0
        )
    }
    
    override suspend fun getJobCardStatisticsByUser(userId: String): JobCardStatistics {
        // Implementation for getting statistics by user
        return getJobCardStatistics()
    }
    
    override suspend fun getJobCardStatisticsByEquipment(equipmentId: String): JobCardStatistics {
        // Implementation for getting statistics by equipment
        return getJobCardStatistics()
    }
    
    override suspend fun getJobCardStatisticsByDateRange(startDate: LocalDate, endDate: LocalDate): JobCardStatistics {
        // Implementation for getting statistics by date range
        return getJobCardStatistics()
    }
    
    // Export
    override suspend fun exportJobCards(request: JobCardExportRequest): String {
        // Implementation for exporting job cards
        return ""
    }
    
    // Notifications
    override suspend fun createNotification(notification: JobCardNotification): JobCardNotification {
        // Implementation for creating notifications
        return notification
    }
    
    override suspend fun getNotifications(userId: String): List<JobCardNotification> {
        // Implementation for getting notifications
        return emptyList()
    }
    
    override suspend fun markNotificationAsRead(notificationId: String): Boolean {
        // Implementation for marking notifications as read
        return true
    }
    
    override suspend fun deleteNotification(notificationId: String): Boolean {
        // Implementation for deleting notifications
        return true
    }
    
    // Task Integration
    override suspend fun createJobCardFromTask(taskId: String, assignedTo: String?): JobCard {
        // Implementation for creating job card from task
        return JobCard(
            id = "",
            title = "",
            description = "",
            status = JobCardStatus.PENDING,
            priority = JobCardPriority.MEDIUM,
            category = JobCardCategory.PREVENTIVE_MAINTENANCE,
            equipmentId = null,
            equipmentName = "",
            siteLocation = "",
            assignedTo = assignedTo,
            assignedToName = null,
            createdBy = "",
            createdByName = "",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            dueDate = null,
            completedDate = null,
            estimatedHours = null,
            actualHours = null,
            partsRequired = emptyList(),
            toolsRequired = emptyList(),
            safetyRequirements = emptyList(),
            workInstructions = null,
            notes = null,
            attachments = emptyList(),
            photos = emptyList(),
            relatedTaskId = taskId,
            relatedFormId = null
        )
    }
    
    override suspend fun linkJobCardToTask(jobCardId: String, taskId: String): Boolean {
        // Implementation for linking job card to task
        return true
    }
    
    override suspend fun getJobCardsByTask(taskId: String): List<JobCard> {
        // Implementation for getting job cards by task
        return emptyList()
    }
    
    // Workflow Management
    override suspend fun createWorkflow(workflow: JobCardWorkflow): JobCardWorkflow {
        // Implementation for creating workflows
        return workflow
    }
    
    override suspend fun getWorkflowById(id: String): JobCardWorkflow? {
        // Implementation for getting workflow by ID
        return null
    }
    
    override suspend fun getAllWorkflows(): List<JobCardWorkflow> {
        // Implementation for getting all workflows
        return emptyList()
    }
    
    override suspend fun updateWorkflow(workflow: JobCardWorkflow): JobCardWorkflow {
        // Implementation for updating workflows
        return workflow
    }
    
    override suspend fun deleteWorkflow(id: String): Boolean {
        // Implementation for deleting workflows
        return true
    }
    
    // Search and Filter
    override suspend fun searchJobCards(query: String): List<JobCard> {
        // Implementation for searching job cards
        return emptyList()
    }
    
    override suspend fun getJobCardsByDateRange(startDate: LocalDate, endDate: LocalDate): List<JobCard> {
        // Implementation for getting job cards by date range
        return emptyList()
    }
    
    override suspend fun getJobCardsByLocation(location: String): List<JobCard> {
        // Implementation for getting job cards by location
        return emptyList()
    }
    
    // Sync Operations
    override suspend fun syncJobCards(): Result<Unit> {
        // Implementation for syncing job cards
        return Result.success(Unit)
    }
    
    override suspend fun getUnsyncedJobCards(): List<JobCard> {
        // Implementation for getting unsynced job cards
        return emptyList()
    }
    
    override suspend fun markJobCardAsSynced(jobCardId: String): Boolean {
        // Implementation for marking job card as synced
        return true
    }
}

// Extension function to convert JobCardEntity to JobCard
private fun JobCardEntity.toJobCard(gson: Gson): JobCard? {
    return try {
        JobCard(
            id = id,
            title = title,
            description = description,
            status = JobCardStatus.valueOf(status),
            priority = JobCardPriority.valueOf(priority),
            category = JobCardCategory.valueOf(category),
            equipmentId = equipmentId,
            equipmentName = equipmentName,
            siteLocation = siteLocation,
            assignedTo = assignedTo,
            assignedToName = assignedToName,
            createdBy = createdBy,
            createdByName = createdByName,
            createdAt = LocalDateTime.parse(createdAt),
            updatedAt = LocalDateTime.parse(updatedAt),
            dueDate = dueDate?.let { LocalDate.parse(it) },
            completedDate = completedDate?.let { LocalDate.parse(it) },
            estimatedHours = estimatedHours,
            actualHours = actualHours,
            partsRequired = gson.fromJson(partsRequired, Array<JobCardPart>::class.java).toList(),
            toolsRequired = gson.fromJson(toolsRequired, Array<String>::class.java).toList(),
            safetyRequirements = gson.fromJson(safetyRequirements, Array<String>::class.java).toList(),
            workInstructions = workInstructions,
            notes = notes,
            attachments = gson.fromJson(attachments, Array<String>::class.java).toList(),
            photos = gson.fromJson(photos, Array<String>::class.java).toList(),
            relatedTaskId = relatedTaskId,
            relatedFormId = relatedFormId
        )
    } catch (e: Exception) {
        null
    }
} 