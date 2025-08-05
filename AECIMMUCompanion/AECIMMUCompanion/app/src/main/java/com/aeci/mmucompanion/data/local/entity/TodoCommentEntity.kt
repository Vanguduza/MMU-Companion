package com.aeci.mmucompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aeci.mmucompanion.domain.model.TodoComment

@Entity(tableName = "todo_comments")
data class TodoCommentEntity(
    @PrimaryKey
    val id: String,
    val todoId: String,
    val userId: String,
    val comment: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isInternal: Boolean = false
)

fun TodoCommentEntity.toDomain(): TodoComment {
    return TodoComment(
        id = id,
        todoId = todoId,
        userId = userId,
        comment = comment,
        createdAt = createdAt,
        isInternal = isInternal
    )
}

fun TodoComment.toEntity(): TodoCommentEntity {
    return TodoCommentEntity(
        id = id,
        todoId = todoId,
        userId = userId,
        comment = comment,
        createdAt = createdAt,
        isInternal = isInternal
    )
}
