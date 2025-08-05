package com.aeci.mmucompanion.data.local.dao

import androidx.room.*
import com.aeci.mmucompanion.data.local.entity.TodoAttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoAttachmentDao {
    
    @Query("SELECT * FROM todo_attachments WHERE todoId = :todoId ORDER BY uploadedAt DESC")
    fun getAttachmentsForTodo(todoId: String): Flow<List<TodoAttachmentEntity>>
    
    @Query("SELECT * FROM todo_attachments WHERE uploadedBy = :userId ORDER BY uploadedAt DESC")
    fun getAttachmentsByUser(userId: String): Flow<List<TodoAttachmentEntity>>
    
    @Query("SELECT COUNT(*) FROM todo_attachments WHERE todoId = :todoId")
    suspend fun getAttachmentCountForTodo(todoId: String): Int
    
    @Query("SELECT SUM(fileSize) FROM todo_attachments WHERE todoId = :todoId")
    suspend fun getTotalFileSizeForTodo(todoId: String): Long?
    
    @Query("SELECT * FROM todo_attachments WHERE mimeType LIKE 'image/%' AND todoId = :todoId ORDER BY uploadedAt DESC")
    fun getImageAttachmentsForTodo(todoId: String): Flow<List<TodoAttachmentEntity>>
    
    @Query("SELECT * FROM todo_attachments WHERE mimeType LIKE 'application/pdf%' AND todoId = :todoId ORDER BY uploadedAt DESC")
    fun getPdfAttachmentsForTodo(todoId: String): Flow<List<TodoAttachmentEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: TodoAttachmentEntity)
    
    @Update
    suspend fun updateAttachment(attachment: TodoAttachmentEntity)
    
    @Delete
    suspend fun deleteAttachment(attachment: TodoAttachmentEntity)
    
    @Query("DELETE FROM todo_attachments WHERE todoId = :todoId")
    suspend fun deleteAttachmentsForTodo(todoId: String)
    
    @Query("SELECT * FROM todo_attachments WHERE id = :attachmentId")
    suspend fun getAttachmentById(attachmentId: String): TodoAttachmentEntity?
    
    @Query("SELECT * FROM todo_attachments WHERE filePath = :filePath")
    suspend fun getAttachmentByPath(filePath: String): TodoAttachmentEntity?
}
