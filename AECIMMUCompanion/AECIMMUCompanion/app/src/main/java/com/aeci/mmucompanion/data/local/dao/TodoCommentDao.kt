package com.aeci.mmucompanion.data.local.dao

import androidx.room.*
import com.aeci.mmucompanion.data.local.entity.TodoCommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoCommentDao {
    
    @Query("SELECT * FROM todo_comments WHERE todoId = :todoId ORDER BY createdAt ASC")
    fun getCommentsForTodo(todoId: String): Flow<List<TodoCommentEntity>>
    
    @Query("SELECT * FROM todo_comments WHERE todoId = :todoId AND isInternal = 0 ORDER BY createdAt ASC")
    fun getPublicCommentsForTodo(todoId: String): Flow<List<TodoCommentEntity>>
    
    @Query("SELECT * FROM todo_comments WHERE userId = :userId ORDER BY createdAt DESC")
    fun getCommentsByUser(userId: String): Flow<List<TodoCommentEntity>>
    
    @Query("SELECT COUNT(*) FROM todo_comments WHERE todoId = :todoId")
    suspend fun getCommentCountForTodo(todoId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: TodoCommentEntity)
    
    @Update
    suspend fun updateComment(comment: TodoCommentEntity)
    
    @Delete
    suspend fun deleteComment(comment: TodoCommentEntity)
    
    @Query("DELETE FROM todo_comments WHERE todoId = :todoId")
    suspend fun deleteCommentsForTodo(todoId: String)
    
    @Query("SELECT * FROM todo_comments WHERE id = :commentId")
    suspend fun getCommentById(commentId: String): TodoCommentEntity?
}
