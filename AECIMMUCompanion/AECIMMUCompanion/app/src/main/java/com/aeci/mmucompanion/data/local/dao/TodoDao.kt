package com.aeci.mmucompanion.data.local.dao

import androidx.room.*
import com.aeci.mmucompanion.domain.model.Todo
import com.aeci.mmucompanion.domain.model.TodoWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    
    @Query("SELECT * FROM todos ORDER BY priority DESC, dueDate ASC, createdAt DESC")
    fun getAllTodos(): Flow<List<Todo>>
    
    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: String): Todo?
    
    @Query("SELECT * FROM todos WHERE assignedToUserId = :userId ORDER BY priority DESC, dueDate ASC, createdAt DESC")
    fun getTodosByUser(userId: String): Flow<List<Todo>>
    
    @Query("SELECT * FROM todos WHERE siteId = :siteId ORDER BY priority DESC, dueDate ASC, createdAt DESC")
    fun getTodosBySite(siteId: String): Flow<List<Todo>>
    
    @Query("SELECT * FROM todos WHERE isCompleted = 0 ORDER BY priority DESC, dueDate ASC, createdAt DESC")
    fun getPendingTodos(): Flow<List<Todo>>
    
    @Query("SELECT * FROM todos WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTodos(): Flow<List<Todo>>
    
    @Query("SELECT * FROM todos WHERE isCompleted = 0 AND dueDate IS NOT NULL AND dueDate < :currentTime ORDER BY dueDate ASC, priority DESC")
    fun getOverdueTodos(currentTime: Long = System.currentTimeMillis()): Flow<List<Todo>>
    
    @Query("SELECT * FROM todos WHERE category = :category ORDER BY priority DESC, dueDate ASC, createdAt DESC")
    fun getTodosByCategory(category: String): Flow<List<Todo>>
    
    @Query("SELECT * FROM todos WHERE priority = :priority ORDER BY dueDate ASC, createdAt DESC")
    fun getTodosByPriority(priority: String): Flow<List<Todo>>
    
    @Query("SELECT * FROM todos WHERE jobCardId = :jobCardId ORDER BY createdAt DESC")
    fun getTodosByJobCard(jobCardId: String): Flow<List<Todo>>
    
    @Query("SELECT * FROM todos WHERE equipmentId = :equipmentId AND isCompleted = 0 ORDER BY priority DESC, dueDate ASC")
    fun getPendingTodosByEquipment(equipmentId: String): Flow<List<Todo>>
    
    @Query("SELECT * FROM todos WHERE assignedToUserId = :userId AND isCompleted = 0 AND dueDate IS NOT NULL AND dueDate BETWEEN :startDate AND :endDate ORDER BY dueDate ASC")
    fun getTodosByUserAndDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<Todo>>
    
    @Query("SELECT * FROM todos WHERE isCompleted = 0 AND progressPercentage > 0 AND progressPercentage < 100 ORDER BY priority DESC, dueDate ASC")
    fun getInProgressTodos(): Flow<List<Todo>>
    
    @Query("UPDATE todos SET progressPercentage = :progress, updatedAt = :updatedAt WHERE id = :todoId")
    suspend fun updateTodoProgress(todoId: String, progress: Int, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE todos SET progressNotes = :notes, updatedAt = :updatedAt WHERE id = :todoId")
    suspend fun updateTodoNotes(todoId: String, notes: String, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE todos SET estimatedHours = :hours, updatedAt = :updatedAt WHERE id = :todoId")
    suspend fun updateEstimatedHours(todoId: String, hours: Double, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE todos SET actualHours = :hours, updatedAt = :updatedAt WHERE id = :todoId")
    suspend fun updateActualHours(todoId: String, hours: Double, updatedAt: Long = System.currentTimeMillis())
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: Todo)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodos(todos: List<Todo>)
    
    @Update
    suspend fun updateTodo(todo: Todo)
    
    @Query("DELETE FROM todos WHERE id = :todoId")
    suspend fun deleteTodo(todoId: String)
    
    @Query("UPDATE todos SET isCompleted = 1, completedAt = :completedAt, progressPercentage = 100, updatedAt = :updatedAt WHERE id = :todoId")
    suspend fun markTodoAsCompleted(todoId: String, completedAt: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE todos SET isCompleted = 0, completedAt = null, progressPercentage = :progress, updatedAt = :updatedAt WHERE id = :todoId")
    suspend fun markTodoAsIncomplete(todoId: String, progress: Int = 0, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE todos SET dueDate = :dueDate, updatedAt = :updatedAt WHERE id = :todoId")
    suspend fun updateTodoDueDate(todoId: String, dueDate: Long?, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE todos SET priority = :priority, updatedAt = :updatedAt WHERE id = :todoId")
    suspend fun updateTodoPriority(todoId: String, priority: String, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE todos SET assignedToUserId = :userId, updatedAt = :updatedAt WHERE id = :todoId")
    suspend fun reassignTodo(todoId: String, userId: String, updatedAt: Long = System.currentTimeMillis())
    
    // Statistics and Analytics
    @Query("SELECT COUNT(*) FROM todos WHERE isCompleted = 0")
    suspend fun getPendingTodoCount(): Int
    
    @Query("SELECT COUNT(*) FROM todos WHERE isCompleted = 0 AND dueDate IS NOT NULL AND dueDate < :currentTime")
    suspend fun getOverdueTodoCount(currentTime: Long = System.currentTimeMillis()): Int
    
    @Query("SELECT COUNT(*) FROM todos WHERE isCompleted = 1 AND completedAt >= :startDate AND completedAt <= :endDate")
    suspend fun getCompletedTodoCountInPeriod(startDate: Long, endDate: Long): Int
    
    @Query("SELECT COUNT(*) FROM todos WHERE assignedToUserId = :userId AND isCompleted = 0")
    suspend fun getPendingTodoCountForUser(userId: String): Int
    
    @Query("SELECT AVG(progressPercentage) FROM todos WHERE assignedToUserId = :userId AND isCompleted = 0")
    suspend fun getAverageProgressForUser(userId: String): Double?
    
    @Query("SELECT AVG(actualHours) FROM todos WHERE isCompleted = 1 AND actualHours > 0 AND category = :category")
    suspend fun getAverageHoursForCategory(category: String): Double?
    
    @Query("SELECT COUNT(*) FROM todos WHERE category = :category AND isCompleted = 0")
    suspend fun getPendingTodoCountByCategory(category: String): Int
    
    @Query("SELECT COUNT(*) FROM todos WHERE priority = :priority AND isCompleted = 0")
    suspend fun getPendingTodoCountByPriority(priority: String): Int
    
    // Helper queries for getting related entity names
    @Query("SELECT fullName FROM users WHERE id = :userId")
    suspend fun getUserName(userId: String): String?
    
    @Query("SELECT name FROM sites WHERE id = :siteId")
    suspend fun getSiteName(siteId: String): String?
    
    @Query("SELECT name FROM equipment WHERE id = :equipmentId")
    suspend fun getEquipmentName(equipmentId: String): String?
    
    @Query("SELECT reportNumber FROM forms WHERE id = :formId")
    suspend fun getFormNumber(formId: String): String?
    
    @Query("SELECT title FROM job_cards WHERE id = :jobCardId")
    suspend fun getJobCardNumber(jobCardId: String): String?
}
