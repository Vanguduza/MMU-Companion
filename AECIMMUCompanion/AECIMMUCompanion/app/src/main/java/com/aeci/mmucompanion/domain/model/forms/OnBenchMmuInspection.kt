package com.aeci.mmucompanion.domain.model.forms

import java.time.LocalDateTime

data class OnBenchMmuInspection(
    val id: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val siteLocation: String = "",
    val inspectorName: String = "",
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val status: String = "Draft",
    
    // On Bench MMU Inspection specific fields
    val mmuSerialNumber: String = "",
    val inspectionDate: LocalDateTime? = null,
    val benchLocation: String = "",
    val visualInspection: String = "",
    val structuralComponents: String = "",
    val mechanicalSystems: String = "",
    val electricalSystems: String = "",
    val hydraulicSystems: String = "",
    val pneumaticSystems: String = "",
    val controlSystems: String = "",
    val safetyDevices: String = "",
    val emergencySystems: String = "",
    val testProcedures: List<String> = emptyList(),
    val testResults: Map<String, String> = emptyMap(),
    val calibrationChecks: List<String> = emptyList(),
    val defectsFound: List<String> = emptyList(),
    val correctiveActions: List<String> = emptyList(),
    val partsRequired: List<String> = emptyList(),
    val inspectionComplete: Boolean = false,
    val readyForDeployment: Boolean = false,
    val supervisorApproval: String = ""
)
