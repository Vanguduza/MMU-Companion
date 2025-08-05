package com.aeci.mmucompanion.domain.model

import java.util.Date

data class WorkOrder(
    val id: String,
    val equipmentId: String,
    val description: String,
    val createdDate: Date,
    val status: WorkOrderStatus,
    val priority: Priority,
    val assignedTo: String? = null,
    val completedAt: Date? = null
)

enum class WorkOrderStatus {
    OPEN,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    ON_HOLD
} 