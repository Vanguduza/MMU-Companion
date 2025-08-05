package com.aeci.mmucompanion.data.remote.dto

import com.google.gson.annotations.SerializedName

data class JobCardStatisticsResponse(
    @SerializedName("total_job_cards")
    val totalJobCards: Int,
    
    @SerializedName("pending_count")
    val pendingCount: Int,
    
    @SerializedName("in_progress_count")
    val inProgressCount: Int,
    
    @SerializedName("completed_count")
    val completedCount: Int,
    
    @SerializedName("overdue_count")
    val overdueCount: Int,
    
    @SerializedName("high_priority_count")
    val highPriorityCount: Int,
    
    @SerializedName("average_completion_time_hours")
    val averageCompletionTimeHours: Double? = null
) 