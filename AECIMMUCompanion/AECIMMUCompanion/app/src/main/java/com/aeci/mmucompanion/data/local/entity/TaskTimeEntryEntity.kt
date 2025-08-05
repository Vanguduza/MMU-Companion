package com.aeci.mmucompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_time_entries")
data class TaskTimeEntryEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val date: String, // Store as ISO string
    val startTime: String,
    val endTime: String? = null,
    val jobCode: String,
    val description: String,
    val regularHours: Double,
    val overtimeHours: Double,
    val isActive: Boolean = false,
    val todoId: String? = null,
    val jobCardId: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val synced: Boolean = false
)

