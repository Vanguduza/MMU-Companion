package com.aeci.mmucompanion.domain.model

import java.time.LocalDateTime
import java.time.LocalDate

// Job Card Models
data class JobCard(
    val id: String,
    val title: String,
    val description: String,
    val status: JobCardStatus,
    val priority: JobCardPriority,
    val category: JobCardCategory,
    val equipmentId: String?,
    val equipmentName: String,
    val siteLocation: String,
    val assignedTo: String?,
    val assignedToName: String?,
    val createdBy: String,
    val createdByName: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val dueDate: LocalDate?,
    val completedDate: LocalDate?,
    val estimatedHours: Double?,
    val actualHours: Double?,
    val partsRequired: List<JobCardPart>,
    val toolsRequired: List<String>,
    val safetyRequirements: List<String>,
    val workInstructions: String?,
    val notes: String?,
    val attachments: List<String> = emptyList(),
    val photos: List<String> = emptyList(),
    val relatedTaskId: String? = null,
    val relatedFormId: String? = null
)

enum class JobCardStatus {
    PENDING, IN_PROGRESS, COMPLETED, CANCELLED, ON_HOLD
}

enum class JobCardPriority {
    LOW, MEDIUM, HIGH, URGENT
}

enum class JobCardCategory {
    PREVENTIVE_MAINTENANCE, CORRECTIVE_MAINTENANCE, INSPECTION, REPAIR, INSTALLATION, CALIBRATION, CLEANING, SAFETY_CHECK
}

data class JobCardPart(
    val partNumber: String,
    val partName: String,
    val quantity: Int,
    val unitCost: Double,
    val supplier: String?,
    val available: Boolean = false,
    val ordered: Boolean = false,
    val received: Boolean = false
)

// Job Card Progress Tracking
data class JobCardProgress(
    val jobCardId: String,
    val step: String,
    val description: String,
    val completed: Boolean,
    val completedBy: String?,
    val completedAt: LocalDateTime?,
    val notes: String?,
    val photos: List<String> = emptyList()
)

// Job Card Time Tracking
data class JobCardTimeEntry(
    val id: String,
    val jobCardId: String,
    val technicianId: String,
    val technicianName: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val hours: Double?,
    val activity: String,
    val notes: String?
)

// Job Card Comments
data class JobCardComment(
    val id: String,
    val jobCardId: String,
    val userId: String,
    val userName: String,
    val comment: String,
    val createdAt: LocalDateTime,
    val isInternal: Boolean = false
)

// Job Card Templates
data class JobCardTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: JobCardCategory,
    val equipmentType: String?,
    val estimatedHours: Double,
    val workInstructions: String,
    val safetyRequirements: List<String>,
    val toolsRequired: List<String>,
    val checklist: List<JobCardChecklistItem>,
    val createdBy: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isActive: Boolean = true
)

data class JobCardChecklistItem(
    val id: String,
    val description: String,
    val isRequired: Boolean = true,
    val order: Int
)

// Job Card Filters and Search
data class JobCardFilters(
    val status: List<JobCardStatus> = emptyList(),
    val priority: List<JobCardPriority> = emptyList(),
    val category: List<JobCardCategory> = emptyList(),
    val assignedTo: String? = null,
    val createdBy: String? = null,
    val equipmentId: String? = null,
    val siteLocation: String? = null,
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null
)

// Job Card Statistics
data class JobCardStatistics(
    val totalJobCards: Int,
    val pendingJobCards: Int,
    val inProgressJobCards: Int,
    val completedJobCards: Int,
    val cancelledJobCards: Int,
    val overdueJobCards: Int,
    val averageCompletionTime: Double?,
    val totalHours: Double,
    val totalCost: Double,
    val completionRate: Double
)

// Job Card Bulk Operations
data class JobCardBulkAction(
    val jobCardIds: List<String>,
    val action: JobCardBulkActionType,
    val parameters: Map<String, Any> = emptyMap()
)

enum class JobCardBulkActionType {
    ASSIGN, CHANGE_STATUS, CHANGE_PRIORITY, DELETE, EXPORT
}

// Job Card Export
data class JobCardExportRequest(
    val jobCardIds: List<String>? = null,
    val filters: JobCardFilters? = null,
    val format: ExportFormat,
    val includeAttachments: Boolean = false
)

// Job Card Notifications
data class JobCardNotification(
    val id: String,
    val jobCardId: String,
    val type: JobCardNotificationType,
    val title: String,
    val message: String,
    val recipientId: String,
    val isRead: Boolean = false,
    val createdAt: LocalDateTime
)

enum class JobCardNotificationType {
    ASSIGNED, DUE_SOON, OVERDUE, COMPLETED, COMMENT_ADDED, STATUS_CHANGED
}

// Job Card Workflow
data class JobCardWorkflow(
    val id: String,
    val name: String,
    val description: String,
    val steps: List<JobCardWorkflowStep>,
    val isActive: Boolean = true
)

data class JobCardWorkflowStep(
    val id: String,
    val name: String,
    val description: String,
    val order: Int,
    val requiredRole: String?,
    val estimatedDuration: Double?,
    val isRequired: Boolean = true
)

// Job Card Integration with Tasks
data class TaskToJobCardMapping(
    val taskId: String,
    val jobCardId: String,
    val mappingType: TaskJobCardMappingType,
    val createdAt: LocalDateTime
)

enum class TaskJobCardMappingType {
    AUTOMATIC, MANUAL, TEMPLATE_BASED
} 