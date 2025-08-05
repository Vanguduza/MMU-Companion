package com.aeci.mmucompanion.domain.model.forms

import java.time.LocalDateTime

data class BlastHoleLog(
    val id: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val siteLocation: String = "",
    val operatorName: String = "",
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val status: String = "Draft",
    
    // Blast Hole specific fields
    val holeNumber: String = "",
    val depth: Double = 0.0,
    val diameter: Double = 0.0,
    val explosive: String = "",
    val chargeWeight: Double = 0.0,
    val stemming: String = "",
    val blastPattern: String = "",
    val weatherConditions: String = "",
    val geologicalNotes: String = "",
    val safetyChecks: List<String> = emptyList(),
    val supervisorApproval: String = "",
    val completionTime: LocalDateTime? = null,
    val notes: String = ""
)
