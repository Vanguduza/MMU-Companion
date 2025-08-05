package com.aeci.mmucompanion.domain.model.forms

import java.time.LocalDateTime

data class PreTaskSafety(
    val id: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val siteLocation: String = "",
    val safetyOfficer: String = "",
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val status: String = "Draft",
    
    // Pre-Task Safety specific fields
    val taskDescription: String = "",
    val taskDate: LocalDateTime? = null,
    val teamMembers: List<String> = emptyList(),
    val hazardIdentification: List<String> = emptyList(),
    val riskAssessment: String = "",
    val controlMeasures: List<String> = emptyList(),
    val ppeRequired: List<String> = emptyList(),
    val equipmentChecks: List<String> = emptyList(),
    val environmentalConditions: String = "",
    val emergencyProcedures: String = "",
    val communicationPlan: String = "",
    val permitRequired: Boolean = false,
    val permitNumber: String = "",
    val trainingRequired: List<String> = emptyList(),
    val competencyVerified: Boolean = false,
    val toolboxTalkConducted: Boolean = false,
    val safetyBriefingComplete: Boolean = false,
    val authorizedToCommence: Boolean = false,
    val supervisorSignature: String = "",
    val teamSignatures: List<String> = emptyList()
)
