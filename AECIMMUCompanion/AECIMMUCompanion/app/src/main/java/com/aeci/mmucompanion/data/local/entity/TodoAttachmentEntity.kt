package com.aeci.mmucompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aeci.mmucompanion.domain.model.TodoAttachment

@Entity(tableName = "todo_attachments")
data class TodoAttachmentEntity(
    @PrimaryKey
    val id: String,
    val todoId: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val mimeType: String,
    val uploadedBy: String,
    val uploadedAt: Long = System.currentTimeMillis()
)

fun TodoAttachmentEntity.toDomain(): TodoAttachment {
    return TodoAttachment(
        id = id,
        todoId = todoId,
        fileName = fileName,
        filePath = filePath,
        fileSize = fileSize,
        mimeType = mimeType,
        uploadedBy = uploadedBy,
        uploadedAt = uploadedAt
    )
}

fun TodoAttachment.toEntity(): TodoAttachmentEntity {
    return TodoAttachmentEntity(
        id = id,
        todoId = todoId,
        fileName = fileName,
        filePath = filePath,
        fileSize = fileSize,
        mimeType = mimeType,
        uploadedBy = uploadedBy,
        uploadedAt = uploadedAt
    )
}
