package com.aeci.mmucompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "job_cards")
data class JobCardEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
    val category: String,
    val equipmentId: String?,
    val equipmentName: String,
    val siteLocation: String,
    val assignedTo: String?,
    val assignedToName: String?,
    val createdBy: String,
    val createdByName: String,
    val createdAt: String,
    val updatedAt: String,
    val dueDate: String?,
    val completedDate: String?,
    val estimatedHours: Double?,
    val actualHours: Double?,
    val partsRequired: String, // JSON string
    val toolsRequired: String, // JSON string
    val safetyRequirements: String, // JSON string
    val workInstructions: String?,
    val notes: String?,
    val attachments: String, // JSON string
    val photos: String, // JSON string
    val relatedTaskId: String?,
    val relatedFormId: String?,
    val synced: Boolean = false,
    val lastSyncAttempt: Long? = null
) 