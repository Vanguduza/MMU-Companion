package com.aeci.mmucompanion.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FormDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("form_type")
    val formType: String,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    @SerializedName("created_by")
    val createdBy: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("equipment_id")
    val equipmentId: String? = null,
    
    @SerializedName("site_location")
    val siteLocation: String,
    
    @SerializedName("report_number")
    val reportNumber: String,
    
    @SerializedName("form_data")
    val formData: String // JSON string of the complete form
) 