package com.aeci.mmucompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shifts")
data class ShiftEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val startTime: String, // "06:00"
    val endTime: String, // "18:00"
    val duration: Int, // hours
    val type: String, // "DAY", "NIGHT", "BACK_SHIFT"
    val isActive: Boolean = true,
    val supervisorId: String? = null,
    val location: String,
    val createdAt: Long
)
