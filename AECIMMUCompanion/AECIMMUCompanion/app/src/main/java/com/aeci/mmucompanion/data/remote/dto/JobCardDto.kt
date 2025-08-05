package com.aeci.mmucompanion.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class JobCardDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("priority")
    val priority: String,
    
    @SerializedName("category")
    val category: String,
    
    @SerializedName("equipment_id")
    val equipmentId: String? = null,
    
    @SerializedName("equipment_name")
    val equipmentName: String,
    
    @SerializedName("site_location")
    val siteLocation: String,
    
    @SerializedName("assigned_to")
    val assignedTo: String? = null,
    
    @SerializedName("assigned_to_name")
    val assignedToName: String? = null,
    
    @SerializedName("created_by")
    val createdBy: String,
    
    @SerializedName("created_by_name")
    val createdByName: String,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    @SerializedName("due_date")
    val dueDate: String? = null,
    
    @SerializedName("completed_date")
    val completedDate: String? = null,
    
    @SerializedName("estimated_hours")
    val estimatedHours: Double? = null,
    
    @SerializedName("actual_hours")
    val actualHours: Double? = null,
    
    @SerializedName("parts_required")
    val partsRequired: String, // JSON string
    
    @SerializedName("tools_required")
    val toolsRequired: String, // JSON string
    
    @SerializedName("safety_requirements")
    val safetyRequirements: String, // JSON string
    
    @SerializedName("work_instructions")
    val workInstructions: String? = null,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("attachments")
    val attachments: String, // JSON string
    
    @SerializedName("photos")
    val photos: String, // JSON string
    
    @SerializedName("related_task_id")
    val relatedTaskId: String? = null,
    
    @SerializedName("related_form_id")
    val relatedFormId: String? = null
) 

data class BulkUpdateJobCardStatusRequest(
    val jobCardIds: List<String>,
    val newStatus: String,
    val updatedBy: String,
    val notes: String? = null
)

data class JobCardExportRequestDto(
    val jobCardIds: List<String>,
    val format: String = "pdf", // pdf, excel, csv
    val includeAttachments: Boolean = false,
    val dateRange: DateRangeDto? = null
)

data class JobCardExportResponseDto(
    val success: Boolean,
    val downloadUrl: String?,
    val fileName: String?,
    val errorMessage: String? = null
)

data class DateRangeDto(
    val startDate: String,
    val endDate: String
) 