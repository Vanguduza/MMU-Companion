package com.aeci.mmucompanion.domain.repository

import com.aeci.mmucompanion.domain.model.*
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    // Basic CRUD operations
    fun getAllTodos(): Flow<List<Todo>>
    fun getAllTodosWithDetails(): Flow<List<TodoWithDetails>>
    suspend fun getTodoById(id: String): Todo?
    fun getTodosByUser(userId: String): Flow<List<TodoWithDetails>>
    fun getTodosBySite(siteId: String): Flow<List<TodoWithDetails>>
    fun getPendingTodos(): Flow<List<TodoWithDetails>>
    fun getCompletedTodos(): Flow<List<TodoWithDetails>>
    fun getOverdueTodos(): Flow<List<TodoWithDetails>>
    suspend fun insertTodo(todo: Todo)
    suspend fun insertTodos(todos: List<Todo>)
    suspend fun updateTodo(todo: Todo)
    suspend fun deleteTodo(todoId: String)
    
    // Status management
    suspend fun markTodoAsCompleted(todoId: String)
    suspend fun markTodoAsIncomplete(todoId: String)
    
    // Progress and time tracking
    suspend fun updateTodoProgress(todoId: String, progress: Int)
    suspend fun updateTodoNotes(todoId: String, notes: String)
    suspend fun updateTodoTimeSpent(todoId: String, minutes: Long)
    suspend fun updateTodoReminder(todoId: String, reminderDateTime: Long)
    
    // Assignment and scheduling
    suspend fun reassignTodo(todoId: String, newAssigneeId: String)
    suspend fun updateTodoDueDate(todoId: String, dueDate: Long?)
    suspend fun updateTodoPriority(todoId: String, priority: TodoPriority)
    
    // Time entries - Updated for comprehensive time tracking integration
    suspend fun startTimeTracking(todoId: String, userId: String): TaskTimeEntry
    suspend fun stopTimeTracking(todoId: String): TaskTimeEntry?
    suspend fun getTimeEntriesForTodo(todoId: String): List<TaskTimeEntry>
    suspend fun getTotalTimeSpentOnTodo(todoId: String): Double // in hours
    suspend fun getActiveTimeEntry(todoId: String): TaskTimeEntry?
    suspend fun pauseTimeTracking(todoId: String): TaskTimeEntry?
    suspend fun resumeTimeTracking(todoId: String): TaskTimeEntry?
    
    // Comments
    suspend fun insertTodoComment(comment: TodoComment)
    fun getCommentsForTodo(todoId: String): Flow<List<TodoComment>>
    fun getPublicCommentsForTodo(todoId: String): Flow<List<TodoComment>>
    
    // Attachments
    suspend fun insertTodoAttachment(attachment: TodoAttachment)
    fun getAttachmentsForTodo(todoId: String): Flow<List<TodoAttachment>>
    
    // Statistics and analytics
    suspend fun getPendingTodoCount(): Int
    suspend fun getCompletedTodoCount(): Int
    suspend fun getOverdueTodoCount(): Int
    suspend fun getInProgressTodoCount(): Int
    suspend fun getAverageCompletionTime(): Double
    suspend fun getTodoCountByPriority(): Map<TodoPriority, Int>
    suspend fun getTodoCountByCategory(): Map<TodoCategory, Int>
    suspend fun getUserProductivity(userId: String): UserProductivity
    
    // Filtering and searching
    fun getTodosByCategory(category: TodoCategory): Flow<List<TodoWithDetails>>
    fun getTodosByPriority(priority: TodoPriority): Flow<List<TodoWithDetails>>
    fun getTodosByJobCard(jobCardId: String): Flow<List<TodoWithDetails>>
    fun getTodosByEquipment(equipmentId: String): Flow<List<TodoWithDetails>>
    fun getTodosByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<TodoWithDetails>>
    fun getInProgressTodos(): Flow<List<TodoWithDetails>>
}
