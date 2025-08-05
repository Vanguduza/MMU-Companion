package com.aeci.mmucompanion.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val priority: TodoPriority = TodoPriority.MEDIUM,
    val category: TodoCategory = TodoCategory.GENERAL,
    val dueDate: Long? = null,
    val startDate: Long? = null,
    val assignedToUserId: String? = null,
    val createdByUserId: String,
    val siteId: String? = null,
    val equipmentId: String? = null,
    val formId: String? = null,
    val jobCardId: String? = null, // Link to job card for technician integration
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    
    // Progress Tracking
    val progressPercentage: Int = 0, // 0-100
    val progressNotes: String? = null,
    val estimatedHours: Double? = null,
    val actualHours: Double? = null,
    val checklistItems: String? = null, // JSON array of checklist items
    val attachments: String? = null, // JSON array of attachment file paths
    
    // Time Tracking
    val timeSpentMinutes: Long = 0, // Total time spent in minutes
    val lastWorkedOn: Long? = null, // Last time work was done on this task
    
    // Dependencies and Blocking
    val dependsOnTodoIds: String? = null, // JSON array of todo IDs this depends on
    val blockingTodoIds: String? = null, // JSON array of todo IDs this blocks
    val isBlocked: Boolean = false,
    val blockedReason: String? = null,
    
    // Recurrence
    val isRecurring: Boolean = false,
    val recurrencePattern: RecurrencePattern? = null,
    val nextDueDate: Long? = null,
    val parentRecurringTodoId: String? = null,
    
    // Notifications and Reminders
    val reminderDateTime: Long? = null,
    val isReminderSent: Boolean = false,
    val notificationSettings: String? = null, // JSON for notification preferences
    
    // Work Context
    val workLocation: String? = null, // Where work needs to be done
    val requiredTools: String? = null, // JSON array of required tools/equipment
    val safetyRequirements: String? = null, // Safety protocols needed
    val skillLevel: SkillLevel = SkillLevel.INTERMEDIATE,
    
    // Quality and Review
    val requiresApproval: Boolean = false,
    val approvedBy: String? = null,
    val approvedAt: Long? = null,
    val qualityCheckRequired: Boolean = false,
    val qualityCheckCompletedBy: String? = null,
    val qualityCheckCompletedAt: Long? = null,
    
    // Cost and Resources
    val estimatedCost: Double? = null,
    val actualCost: Double? = null,
    val budgetCode: String? = null,
    val resourcesAllocated: String? = null, // JSON for allocated resources
    
    // Integration and Sync
    val externalId: String? = null, // For integration with external systems
    val syncStatus: SyncStatus = SyncStatus.LOCAL,
    val lastSyncAt: Long? = null,
    
    // Tags and Labels
    val tags: String? = null, // JSON array of tags for filtering/searching
    val customFields: String? = null // JSON for custom field values
)

enum class TodoPriority(val displayName: String, val value: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    URGENT("Urgent", 4),
    CRITICAL("Critical", 5)
}

enum class TodoCategory(val displayName: String) {
    GENERAL("General"),
    MAINTENANCE("Maintenance"),
    INSPECTION("Inspection"),
    SAFETY("Safety"),
    DOCUMENTATION("Documentation"),
    EQUIPMENT("Equipment"),
    FORM_SUBMISSION("Form Submission"),
    CALIBRATION("Calibration"),
    REPAIR("Repair"),
    PREVENTIVE_MAINTENANCE("Preventive Maintenance"),
    CORRECTIVE_MAINTENANCE("Corrective Maintenance"),
    TRAINING("Training"),
    AUDIT("Audit"),
    EMERGENCY("Emergency"),
    PROJECT("Project"),
    RESEARCH("Research")
}

enum class RecurrencePattern(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    BIWEEKLY("Bi-weekly"),
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly"),
    YEARLY("Yearly"),
    CUSTOM("Custom")
}

enum class SkillLevel(val displayName: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
    EXPERT("Expert"),
    SPECIALIZED("Specialized")
}

enum class SyncStatus(val displayName: String) {
    OFFLINE("Offline"),
    LOCAL("Local Only"),
    PENDING_SYNC("Pending Sync"),
    SYNCING("Syncing"),
    SYNCED("Synced"),
    SYNC_ERROR("Sync Error"),
    FAILED("Failed"),
    CONFLICT("Conflict")
}

// Sync-related data classes and interfaces
data class SyncableItem(
    val id: String,
    val type: SyncItemType,
    val data: Map<String, Any>,
    val lastModified: Long,
    val syncStatus: SyncStatus = SyncStatus.OFFLINE
)

enum class SyncItemType {
    FORM,
    EQUIPMENT,
    USER,
    TODO,
    JOB_CARD,
    MAINTENANCE_RECORD
}

interface Syncable {
    val id: String
    val syncStatus: SyncStatus
    val lastModified: Long
}

data class TodoWithDetails(
    val todo: Todo,
    val assignedToUser: String? = null,
    val createdByUser: String? = null,
    val siteName: String? = null,
    val equipmentName: String? = null,
    val formNumber: String? = null,
    val jobCardNumber: String? = null,
    val dependentTodos: List<Todo> = emptyList(),
    val blockingTodos: List<Todo> = emptyList(),
    val checklist: List<ChecklistItem> = emptyList(),
    val timeEntries: List<TaskTimeEntry> = emptyList(),
    val comments: List<TodoComment> = emptyList(),
    val attachmentFiles: List<TodoAttachment> = emptyList()
)

data class ChecklistItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val completedBy: String? = null
)

// TimeEntry is defined in MmuFormDataClasses.kt

data class TodoComment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val todoId: String,
    val userId: String,
    val comment: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isInternal: Boolean = false // Internal comments not visible to assignee
)

data class TodoAttachment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val todoId: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val mimeType: String,
    val uploadedBy: String,
    val uploadedAt: Long = System.currentTimeMillis()
)

// Data classes for analytics and reporting
data class TodoStatistics(
    val totalTodos: Int,
    val pendingTodos: Int,
    val completedTodos: Int,
    val overdueTodos: Int,
    val inProgressTodos: Int,
    val averageCompletionTime: Double,
    val completionRate: Double,
    val priorityDistribution: Map<TodoPriority, Int>,
    val categoryDistribution: Map<TodoCategory, Int>
)

data class UserProductivity(
    val userId: String,
    val userName: String,
    val assignedTodos: Int,
    val completedTodos: Int,
    val averageProgressPercentage: Double,
    val totalHoursWorked: Double,
    val onTimeCompletionRate: Double,
    val productivityScore: Double
)
