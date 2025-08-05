package com.aeci.mmucompanion.domain.model.forms

import java.time.LocalDateTime

data class MmuChassisMaintenance(
    val id: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val siteLocation: String = "",
    val technicianName: String = "",
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val status: String = "Draft",
    
    // MMU Chassis Maintenance specific fields
    val mmuSerialNumber: String = "",
    val chassisCondition: String = "",
    val structuralIntegrity: String = "",
    val weldingInspection: String = "",
    val paintCondition: String = "",
    val boltTorque: Map<String, Double> = emptyMap(),
    val alignmentCheck: String = "",
    val groundingSystem: String = "",
    val supportStructure: String = "",
    val accessPlatforms: String = "",
    val safetyRailings: String = "",
    val emergencyExits: String = "",
    val maintenanceActions: List<String> = emptyList(),
    val partsReplaced: List<String> = emptyList(),
    val nextMaintenanceDate: LocalDateTime? = null,
    val supervisorApproval: String = "",
    val maintenanceComplete: Boolean = false
)
