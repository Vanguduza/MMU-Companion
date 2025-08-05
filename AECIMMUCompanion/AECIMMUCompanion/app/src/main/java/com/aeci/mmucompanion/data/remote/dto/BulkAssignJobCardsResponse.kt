package com.aeci.mmucompanion.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BulkAssignJobCardsResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("assigned_count")
    val assignedCount: Int,
    
    @SerializedName("failed_assignments")
    val failedAssignments: List<String> = emptyList()
) 