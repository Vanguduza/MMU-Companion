package com.aeci.mmucompanion.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BulkAssignJobCardsRequest(
    @SerializedName("job_card_ids")
    val jobCardIds: List<String>,
    
    @SerializedName("assigned_to_id")
    val assignedToId: String,
    
    @SerializedName("assigned_by_id")
    val assignedById: String,
    
    @SerializedName("notes")
    val notes: String? = null
) 