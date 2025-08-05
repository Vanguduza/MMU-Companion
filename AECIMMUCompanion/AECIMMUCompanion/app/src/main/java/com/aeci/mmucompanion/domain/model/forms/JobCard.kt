package com.aeci.mmucompanion.domain.model.forms

import java.time.LocalDateTime

data class JobCard(
    val id: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val siteLocation: String = "",
    val assignedTechnician: String = "",
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val status: String = "Draft",
    
    // Job Card specific fields
    val jobNumber: String = "",
    val workDescription: String = "",
    val priority: String = "",
    val estimatedHours: Double = 0.0,
    val actualHours: Double = 0.0,
    val materialsRequired: List<String> = emptyList(),
    val toolsRequired: List<String> = emptyList(),
    val safetyRequirements: List<String> = emptyList(),
    val workStartTime: LocalDateTime? = null,
    val workEndTime: LocalDateTime? = null,
    val completionStatus: String = "",
    val qualityCheck: String = "",
    val supervisorReview: String = "",
    val customerSignature: String = "",
    val technicianSignature: String = "",
    val additionalNotes: String = ""
)
