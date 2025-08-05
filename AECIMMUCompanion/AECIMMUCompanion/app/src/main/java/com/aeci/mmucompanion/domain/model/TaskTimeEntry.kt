package com.aeci.mmucompanion.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

// Task Time Entry domain model for time tracking functionality
data class TaskTimeEntry(
    val id: String,
    val userId: String,
    val date: LocalDate,
    val startTime: String,
    val endTime: String? = null,
    val jobCode: String,
    val description: String,
    val regularHours: Double,
    val overtimeHours: Double,
    val isActive: Boolean = false,
    val todoId: String? = null,
    val jobCardId: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
