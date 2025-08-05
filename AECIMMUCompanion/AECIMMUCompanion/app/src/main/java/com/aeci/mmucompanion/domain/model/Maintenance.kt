package com.aeci.mmucompanion.domain.model

import java.util.Date

data class MaintenanceTask(
    val id: String,
    val equipmentId: String,
    val description: String,
    val priority: Priority,
    val scheduledDate: Date,
    val completedAt: Date? = null,
    val assignedTo: String? = null
) 