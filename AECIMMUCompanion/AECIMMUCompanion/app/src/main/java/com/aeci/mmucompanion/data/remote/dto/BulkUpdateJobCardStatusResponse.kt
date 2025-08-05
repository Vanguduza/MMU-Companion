package com.aeci.mmucompanion.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BulkUpdateJobCardStatusResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("updated_count")
    val updatedCount: Int,
    
    @SerializedName("failed_updates")
    val failedUpdates: List<String> = emptyList()
) 