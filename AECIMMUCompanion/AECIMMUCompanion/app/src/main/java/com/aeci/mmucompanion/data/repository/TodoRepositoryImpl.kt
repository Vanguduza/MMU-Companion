package com.aeci.mmucompanion.data.repository

import com.aeci.mmucompanion.data.local.dao.*
import com.aeci.mmucompanion.data.local.entity.*
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.domain.repository.TodoRepository
import com.aeci.mmucompanion.domain.repository.TimeTrackingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Singleton
class TodoRepositoryImpl @Inject constructor(
    private val todoDao: TodoDao,
    private val taskTimeEntryDao: TaskTimeEntryDao,
    private val todoCommentDao: TodoCommentDao,
    private val todoAttachmentDao: TodoAttachmentDao,
    private val timeTrackingRepository: TimeTrackingRepository,
    private val gson: Gson
) : TodoRepository {
    
    // Basic CRUD operations
    override fun getAllTodos(): Flow<List<Todo>> {
        return todoDao.getAllTodos()
    }
    
    override fun getAllTodosWithDetails(): Flow<List<TodoWithDetails>> {
        return todoDao.getAllTodos().map { todos ->
            todos.map { todo ->
                createTodoWithDetails(todo)
            }
        }
    }
    
    override suspend fun getTodoById(id: String): Todo? {
        return todoDao.getTodoById(id)
    }
    
    override fun getTodosByUser(userId: String): Flow<List<TodoWithDetails>> {
        return todoDao.getTodosByUser(userId).map { todos ->
            todos.map { todo ->
                createTodoWithDetails(todo)
            }
        }
    }
    
    override fun getTodosBySite(siteId: String): Flow<List<TodoWithDetails>> {
        return todoDao.getTodosBySite(siteId).map { todos ->
            todos.map { todo ->
                createTodoWithDetails(todo)
            }
        }
    }
    
    override fun getPendingTodos(): Flow<List<TodoWithDetails>> {
        return todoDao.getPendingTodos().map { todos ->
            todos.map { todo ->
                createTodoWithDetails(todo)
            }
        }
    }
    
    override fun getCompletedTodos(): Flow<List<TodoWithDetails>> {
        return todoDao.getCompletedTodos().map { todos ->
            todos.map { todo ->
                createTodoWithDetails(todo)
            }
        }
    }
    
    override fun getOverdueTodos(): Flow<List<TodoWithDetails>> {
        return todoDao.getOverdueTodos().map { todos ->
            todos.map { todo ->
                createTodoWithDetails(todo)
            }
        }
    }
    
    override suspend fun insertTodo(todo: Todo) {
        todoDao.insertTodo(todo)
    }
    
    override suspend fun insertTodos(todos: List<Todo>) {
        todoDao.insertTodos(todos)
    }
    
    override suspend fun updateTodo(todo: Todo) {
        todoDao.updateTodo(todo)
    }
    
    override suspend fun deleteTodo(todoId: String) {
        todoDao.deleteTodo(todoId)
        // Clean up related data
        taskTimeEntryDao.deleteTimeEntriesForTodo(todoId)
        todoCommentDao.deleteCommentsForTodo(todoId)
        todoAttachmentDao.deleteAttachmentsForTodo(todoId)
    }
    
    // Status management
    override suspend fun markTodoAsCompleted(todoId: String) {
        todoDao.markTodoAsCompleted(todoId)
    }
    
    override suspend fun markTodoAsIncomplete(todoId: String) {
        todoDao.markTodoAsIncomplete(todoId)
    }
    
    // Progress and time tracking
    override suspend fun updateTodoProgress(todoId: String, progress: Int) {
        todoDao.updateTodoProgress(todoId, progress)
    }
    
    override suspend fun updateTodoNotes(todoId: String, notes: String) {
        todoDao.updateTodoNotes(todoId, notes)
    }
    
    override suspend fun updateTodoTimeSpent(todoId: String, minutes: Long) {
        val todo = todoDao.getTodoById(todoId)
        todo?.let {
            val updatedTodo = it.copy(
                timeSpentMinutes = minutes,
                lastWorkedOn = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            todoDao.updateTodo(updatedTodo)
        }
    }
    
    override suspend fun updateTodoReminder(todoId: String, reminderDateTime: Long) {
        val todo = todoDao.getTodoById(todoId)
        todo?.let {
            val updatedTodo = it.copy(
                reminderDateTime = reminderDateTime,
                updatedAt = System.currentTimeMillis()
            )
            todoDao.updateTodo(updatedTodo)
        }
    }
    
    // Assignment and scheduling
    override suspend fun reassignTodo(todoId: String, newAssigneeId: String) {
        todoDao.reassignTodo(todoId, newAssigneeId)
    }
    
    override suspend fun updateTodoDueDate(todoId: String, dueDate: Long?) {
        todoDao.updateTodoDueDate(todoId, dueDate)
    }
    
    override suspend fun updateTodoPriority(todoId: String, priority: TodoPriority) {
        todoDao.updateTodoPriority(todoId, priority.name)
    }
    
    // Time entries - Updated for comprehensive time tracking integration
    override suspend fun startTimeTracking(todoId: String, userId: String): TaskTimeEntry {
        return timeTrackingRepository.startTimerForTodo(todoId, userId)
    }
    
    override suspend fun stopTimeTracking(todoId: String): TaskTimeEntry? {
        return timeTrackingRepository.stopTimerForTodo(todoId)
    }
    
    override suspend fun getTimeEntriesForTodo(todoId: String): List<TaskTimeEntry> {
        return timeTrackingRepository.getTimeEntriesForTodo(todoId)
    }
    
    override suspend fun getTotalTimeSpentOnTodo(todoId: String): Double {
        return timeTrackingRepository.getTotalTimeSpentOnTodo(todoId)
    }
    
    override suspend fun getActiveTimeEntry(todoId: String): TaskTimeEntry? {
        return timeTrackingRepository.getTimeEntriesForTodo(todoId)
            .firstOrNull { it.isActive }
    }
    
    override suspend fun pauseTimeTracking(todoId: String): TaskTimeEntry? {
        val activeEntry = getActiveTimeEntry(todoId)
        return activeEntry?.let { timeTrackingRepository.pauseTimeTracking(it.id) }
    }
    
    override suspend fun resumeTimeTracking(todoId: String): TaskTimeEntry? {
        val activeEntry = getActiveTimeEntry(todoId)
        return activeEntry?.let { timeTrackingRepository.resumeTimeTracking(it.id) }
    }
    
    // Comments
    override suspend fun insertTodoComment(comment: TodoComment) {
        todoCommentDao.insertComment(comment.toEntity())
    }
    
    override fun getCommentsForTodo(todoId: String): Flow<List<TodoComment>> {
        return todoCommentDao.getCommentsForTodo(todoId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getPublicCommentsForTodo(todoId: String): Flow<List<TodoComment>> {
        return todoCommentDao.getPublicCommentsForTodo(todoId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    // Attachments
    override suspend fun insertTodoAttachment(attachment: TodoAttachment) {
        todoAttachmentDao.insertAttachment(attachment.toEntity())
    }
    
    override fun getAttachmentsForTodo(todoId: String): Flow<List<TodoAttachment>> {
        return todoAttachmentDao.getAttachmentsForTodo(todoId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    // Statistics and analytics
    override suspend fun getPendingTodoCount(): Int {
        return todoDao.getPendingTodoCount()
    }
    
    override suspend fun getCompletedTodoCount(): Int {
        // This would need to be added to TodoDao
        return 0 // Placeholder
    }
    
    override suspend fun getOverdueTodoCount(): Int {
        return todoDao.getOverdueTodoCount()
    }
    
    override suspend fun getInProgressTodoCount(): Int {
        // This would need to be added to TodoDao
        return 0 // Placeholder
    }
    
    override suspend fun getAverageCompletionTime(): Double {
        // This would need complex calculation in TodoDao
        return 0.0 // Placeholder
    }
    
    override suspend fun getTodoCountByPriority(): Map<TodoPriority, Int> {
        // This would need to be implemented in TodoDao
        return emptyMap() // Placeholder
    }
    
    override suspend fun getTodoCountByCategory(): Map<TodoCategory, Int> {
        // This would need to be implemented in TodoDao
        return emptyMap() // Placeholder
    }
    
    override suspend fun getUserProductivity(userId: String): UserProductivity {
        // This would need complex calculation combining multiple DAOs
        return UserProductivity(
            userId = userId,
            userName = todoDao.getUserName(userId) ?: "Unknown",
            assignedTodos = 0,
            completedTodos = 0,
            averageProgressPercentage = 0.0,
            totalHoursWorked = 0.0,
            onTimeCompletionRate = 0.0,
            productivityScore = 0.0
        ) // Placeholder
    }
    
    // Filtering and searching
    override fun getTodosByCategory(category: TodoCategory): Flow<List<TodoWithDetails>> {
        return todoDao.getTodosByCategory(category.name).map { todos ->
            todos.map { todo ->
                createTodoWithDetails(todo)
            }
        }
    }
    
    override fun getTodosByPriority(priority: TodoPriority): Flow<List<TodoWithDetails>> {
        return todoDao.getTodosByPriority(priority.name).map { todos ->
            todos.map { todo ->
                createTodoWithDetails(todo)
            }
        }
    }
    
    override fun getTodosByJobCard(jobCardId: String): Flow<List<TodoWithDetails>> {
        return todoDao.getTodosByJobCard(jobCardId).map { todos ->
            todos.map { todo ->
                createTodoWithDetails(todo)
            }
        }
    }
    
    override fun getTodosByEquipment(equipmentId: String): Flow<List<TodoWithDetails>> {
        return todoDao.getPendingTodosByEquipment(equipmentId).map { todos ->
            todos.map { todo ->
                createTodoWithDetails(todo)
            }
        }
    }
    
    override fun getTodosByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<TodoWithDetails>> {
        return todoDao.getTodosByUserAndDateRange(userId, startDate, endDate).map { todos ->
            todos.map { todo ->
                createTodoWithDetails(todo)
            }
        }
    }
    
    override fun getInProgressTodos(): Flow<List<TodoWithDetails>> {
        return todoDao.getInProgressTodos().map { todos ->
            todos.map { todo ->
                createTodoWithDetails(todo)
            }
        }
    }
    
    // Helper method to create TodoWithDetails
    private suspend fun createTodoWithDetails(todo: Todo): TodoWithDetails {
        val assignedToUser = todo.assignedToUserId?.let { todoDao.getUserName(it) }
        val createdByUser = todoDao.getUserName(todo.createdByUserId)
        val siteName = todo.siteId?.let { todoDao.getSiteName(it) }
        val equipmentName = todo.equipmentId?.let { todoDao.getEquipmentName(it) }
        val formNumber = todo.formId?.let { todoDao.getFormNumber(it) }
        val jobCardNumber = todo.jobCardId?.let { todoDao.getJobCardNumber(it) }
        
        // Parse checklist from JSON
        val checklist = todo.checklistItems?.let { json ->
            try {
                val type = object : TypeToken<List<ChecklistItem>>() {}.type
                gson.fromJson<List<ChecklistItem>>(json, type)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()
        
        // Get time entries, comments, and attachments
                val timeEntries = try {
            taskTimeEntryDao.getTimeEntriesForTodo(todo.id).map { entity ->
                TaskTimeEntry(
                    id = entity.id,
                    userId = entity.userId,
                    date = java.time.LocalDate.parse(entity.date),
                    startTime = entity.startTime,
                    endTime = entity.endTime,
                    jobCode = entity.jobCode,
                    description = entity.description,
                    regularHours = entity.regularHours,
                    overtimeHours = entity.overtimeHours,
                    isActive = entity.isActive,
                    todoId = entity.todoId,
                    jobCardId = entity.jobCardId,
                    createdAt = java.time.LocalDateTime.parse(entity.createdAt),
                    updatedAt = java.time.LocalDateTime.parse(entity.updatedAt)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
        
        val comments: List<TodoComment> = try {
            todoCommentDao.getCommentsForTodo(todo.id).first().map { entity: TodoCommentEntity ->
                entity.toDomain()
            }
        } catch (e: Exception) {
            emptyList<TodoComment>()
        }

        val attachments: List<TodoAttachment> = try {
            todoAttachmentDao.getAttachmentsForTodo(todo.id).first().map { entity: TodoAttachmentEntity ->
                entity.toDomain()
            }
        } catch (e: Exception) {
            emptyList<TodoAttachment>()
        }
        
        return TodoWithDetails(
            todo = todo,
            assignedToUser = assignedToUser,
            createdByUser = createdByUser,
            siteName = siteName,
            equipmentName = equipmentName,
            formNumber = formNumber,
            jobCardNumber = jobCardNumber,
            checklist = checklist,
            timeEntries = timeEntries,
            comments = comments,
            attachmentFiles = attachments
        )
    }
}
