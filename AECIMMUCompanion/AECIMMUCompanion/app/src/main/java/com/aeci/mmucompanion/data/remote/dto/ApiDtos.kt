package com.aeci.mmucompanion.data.remote.dto

import com.aeci.mmucompanion.domain.model.PaginationInfo
import com.aeci.mmucompanion.domain.model.Permission
import com.aeci.mmucompanion.domain.model.User
import com.aeci.mmucompanion.domain.model.UserRole
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

// Request DTOs
data class LoginRequest(
    val username: String,
    val password: String
)

data class ChangePasswordRequest(
    @SerializedName("currentPassword")
    val currentPassword: String,
    @SerializedName("newPassword")
    val newPassword: String
)

data class CreateTodoRequest(
    val title: String,
    val description: String = "",
    val priority: String = "MEDIUM",
    val category: String = "GENERAL",
    val status: String = "PENDING",
    @SerializedName("progress_percentage")
    val progressPercentage: Int = 0,
    @SerializedName("estimated_hours")
    val estimatedHours: Double = 0.0,
    @SerializedName("due_date")
    val dueDate: Long? = null,
    @SerializedName("reminder_date")
    val reminderDate: Long? = null,
    @SerializedName("start_date")
    val startDate: Long? = null,
    @SerializedName("assigned_to_user_id")
    val assignedToUserId: String? = null,
    @SerializedName("site_id")
    val siteId: String? = null,
    @SerializedName("equipment_id")
    val equipmentId: String? = null,
    @SerializedName("form_id")
    val formId: String? = null,
    @SerializedName("job_card_number")
    val jobCardNumber: String? = null,
    val tags: List<String> = emptyList(),
    val notes: String = "",
    @SerializedName("checklist_items")
    val checklistItems: List<ChecklistItemDto> = emptyList(),
    @SerializedName("depends_on_todo_ids")
    val dependsOnTodoIds: List<String> = emptyList(),
    @SerializedName("blocked_by")
    val blockedBy: String = "",
    @SerializedName("recurrence_pattern")
    val recurrencePattern: String? = null,
    @SerializedName("skill_level")
    val skillLevel: String = "BEGINNER",
    @SerializedName("estimated_cost")
    val estimatedCost: Double = 0.0,
    @SerializedName("custom_fields")
    val customFields: JsonObject = JsonObject()
)

data class UpdateTodoRequest(
    val title: String? = null,
    val description: String? = null,
    val priority: String? = null,
    val category: String? = null,
    val status: String? = null,
    @SerializedName("progress_percentage")
    val progressPercentage: Int? = null,
    @SerializedName("estimated_hours")
    val estimatedHours: Double? = null,
    @SerializedName("actual_hours")
    val actualHours: Double? = null,
    @SerializedName("time_spent_minutes")
    val timeSpentMinutes: Int? = null,
    @SerializedName("due_date")
    val dueDate: Long? = null,
    @SerializedName("reminder_date")
    val reminderDate: Long? = null,
    @SerializedName("start_date")
    val startDate: Long? = null,
    @SerializedName("assigned_to_user_id")
    val assignedToUserId: String? = null,
    @SerializedName("site_id")
    val siteId: String? = null,
    @SerializedName("equipment_id")
    val equipmentId: String? = null,
    @SerializedName("form_id")
    val formId: String? = null,
    @SerializedName("job_card_number")
    val jobCardNumber: String? = null,
    val tags: List<String>? = null,
    val notes: String? = null,
    @SerializedName("checklist_items")
    val checklistItems: List<ChecklistItemDto>? = null,
    @SerializedName("depends_on_todo_ids")
    val dependsOnTodoIds: List<String>? = null,
    @SerializedName("blocked_by")
    val blockedBy: String? = null,
    @SerializedName("recurrence_pattern")
    val recurrencePattern: String? = null,
    @SerializedName("skill_level")
    val skillLevel: String? = null,
    @SerializedName("estimated_cost")
    val estimatedCost: Double? = null,
    @SerializedName("actual_cost")
    val actualCost: Double? = null,
    @SerializedName("custom_fields")
    val customFields: JsonObject? = null,
    @SerializedName("sync_status")
    val syncStatus: String? = null,
    @SerializedName("is_completed")
    val isCompleted: Boolean? = null,
    @SerializedName("is_archived")
    val isArchived: Boolean? = null
)

data class AddCommentRequest(
    val comment: String,
    @SerializedName("comment_type")
    val commentType: String = "PUBLIC"
)

data class StartTimeTrackingRequest(
    val description: String = ""
)

data class BulkUpdateTodosRequest(
    @SerializedName("todoIds")
    val todoIds: List<String>,
    val updates: UpdateTodoRequest
)

data class CreateUserRequest(
    val username: String,
    val email: String,
    val fullName: String,
    val role: String? = null,
    val department: String? = null,
    val shiftPattern: String? = null,
    val permissions: List<String>? = null,
    val tempPassword: String
)

// Response DTOs
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val user: UserDto? = null
)

data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    @SerializedName("full_name")
    val fullName: String,
    val role: String,
    val department: String?,
    @SerializedName("shift_pattern")
    val shiftPattern: String?,
    val permissions: List<String>,
    @SerializedName("requires_password_change")
    val requiresPasswordChange: Boolean,
    @SerializedName("biometric_enabled")
    val biometricEnabled: Boolean
) {
    fun toDomainModel(): User = User(
        id = id,
        username = username,
        fullName = fullName,
        email = email,
        role = try { UserRole.valueOf(role) } catch (e: Exception) { UserRole.OPERATOR },
        department = department ?: "",
        shiftPattern = shiftPattern ?: "",
        permissions = permissions.mapNotNull { permString ->
            try { Permission.valueOf(permString) } catch (e: Exception) { null }
        },
        isActive = true,
        siteId = "site_001"
    )
}

data class TodoDto(
    val id: String,
    val title: String,
    val description: String,
    val priority: String,
    val category: String,
    val status: String,
    @SerializedName("progress_percentage")
    val progressPercentage: Int,
    @SerializedName("estimated_hours")
    val estimatedHours: Double,
    @SerializedName("actual_hours")
    val actualHours: Double,
    @SerializedName("time_spent_minutes")
    val timeSpentMinutes: Int,
    @SerializedName("due_date")
    val dueDate: Long?,
    @SerializedName("reminder_date")
    val reminderDate: Long?,
    @SerializedName("start_date")
    val startDate: Long?,
    @SerializedName("completed_at")
    val completedAt: Long?,
    @SerializedName("assigned_to_user_id")
    val assignedToUserId: String?,
    @SerializedName("created_by_user_id")
    val createdByUserId: String,
    @SerializedName("site_id")
    val siteId: String?,
    @SerializedName("equipment_id")
    val equipmentId: String?,
    @SerializedName("form_id")
    val formId: String?,
    @SerializedName("job_card_number")
    val jobCardNumber: String?,
    val tags: List<String>,
    val notes: String,
    @SerializedName("checklist_items")
    val checklistItems: List<ChecklistItemDto>,
    @SerializedName("depends_on_todo_ids")
    val dependsOnTodoIds: List<String>,
    @SerializedName("blocked_by")
    val blockedBy: String,
    @SerializedName("recurrence_pattern")
    val recurrencePattern: String?,
    @SerializedName("skill_level")
    val skillLevel: String,
    @SerializedName("estimated_cost")
    val estimatedCost: Double,
    @SerializedName("actual_cost")
    val actualCost: Double,
    @SerializedName("custom_fields")
    val customFields: JsonObject,
    @SerializedName("sync_status")
    val syncStatus: String,
    @SerializedName("is_completed")
    val isCompleted: Boolean,
    @SerializedName("is_archived")
    val isArchived: Boolean,
    @SerializedName("created_at")
    val createdAt: Long,
    @SerializedName("updated_at")
    val updatedAt: Long,
    
    // Additional fields from JOIN queries
    @SerializedName("assigned_to_name")
    val assignedToName: String? = null,
    @SerializedName("created_by_name")
    val createdByName: String? = null,
    @SerializedName("equipment_name")
    val equipmentName: String? = null,
    @SerializedName("form_type")
    val formType: String? = null,
    
    // Related data (when fetching detailed todo)
    val comments: List<TodoCommentDto>? = null,
    val attachments: List<TodoAttachmentDto>? = null,
    @SerializedName("time_entries")
    val timeEntries: List<TimeEntryDto>? = null
)

data class ChecklistItemDto(
    val id: String,
    val text: String,
    @SerializedName("is_completed")
    val isCompleted: Boolean,
    @SerializedName("completed_at")
    val completedAt: Long? = null,
    @SerializedName("completed_by")
    val completedBy: String? = null
)

data class TodoCommentDto(
    val id: String,
    @SerializedName("todo_id")
    val todoId: String,
    val comment: String,
    @SerializedName("comment_type")
    val commentType: String,
    @SerializedName("created_by_user_id")
    val createdByUserId: String,
    @SerializedName("created_at")
    val createdAt: Long,
    @SerializedName("user_name")
    val userName: String? = null
)

data class TodoAttachmentDto(
    val id: String,
    @SerializedName("todo_id")
    val todoId: String,
    @SerializedName("file_name")
    val fileName: String,
    @SerializedName("file_path")
    val filePath: String,
    @SerializedName("file_size")
    val fileSize: Long,
    @SerializedName("mime_type")
    val mimeType: String?,
    @SerializedName("uploaded_by_user_id")
    val uploadedByUserId: String,
    @SerializedName("uploaded_at")
    val uploadedAt: Long,
    @SerializedName("uploaded_by_name")
    val uploadedByName: String? = null
)

data class TimeEntryDto(
    val id: String,
    @SerializedName("todo_id")
    val todoId: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("start_time")
    val startTime: Long,
    @SerializedName("end_time")
    val endTime: Long?,
    @SerializedName("duration_minutes")
    val durationMinutes: Int,
    val description: String,
    @SerializedName("is_break")
    val isBreak: Boolean,
    @SerializedName("created_at")
    val createdAt: Long,
    @SerializedName("user_name")
    val userName: String? = null
)

data class TodosResponse(
    val success: Boolean,
    val todos: List<TodoDto>,
    val pagination: PaginationDto? = null
)

data class PaginationDto(
    val page: Int,
    val limit: Int,
    val total: Int,
    @SerializedName("total_pages")
    val totalPages: Int = (total + limit - 1) / limit
) {
    fun toDomainModel(): PaginationInfo = PaginationInfo(
        page = page,
        limit = limit,
        total = total,
        totalPages = totalPages
    )
}

data class TodoAnalyticsDto(
    @SerializedName("total_todos")
    val totalTodos: Int,
    @SerializedName("completed_todos")
    val completedTodos: Int,
    @SerializedName("overdue_todos")
    val overdueTodos: Int,
    @SerializedName("completion_rate")
    val completionRate: Double,
    @SerializedName("average_progress")
    val averageProgress: Double,
    @SerializedName("total_time_hours")
    val totalTimeHours: Double,
    @SerializedName("priority_distribution")
    val priorityDistribution: Map<String, Int>,
    @SerializedName("category_distribution")
    val categoryDistribution: Map<String, Int>
)

data class NetworkInfoResponse(
    val success: Boolean,
    val port: Int,
    val addresses: List<NetworkAddressDto>,
    val hostname: String
)

data class NetworkAddressDto(
    @SerializedName("interface")
    val networkInterface: String,
    val address: String,
    val url: String
)
