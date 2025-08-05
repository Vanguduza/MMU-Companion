package com.aeci.mmucompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
    val assignedTo: String?,
    val dueDate: Long?,
    val completedDate: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val siteId: String
)
