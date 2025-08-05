package com.aeci.mmucompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forms")
data class FormEntity(
    @PrimaryKey
    val id: String,
    val formType: String,
    val createdAt: String,
    val updatedAt: String,
    val createdBy: String,
    val status: String,
    val equipmentId: String? = null,
    val shiftId: String? = null,
    val locationId: String? = null,
    val siteLocation: String,
    val reportNumber: String,
    val formData: String, // JSON string of the complete form
    val synced: Boolean = false,
    val lastSyncAttempt: Long? = null
)
