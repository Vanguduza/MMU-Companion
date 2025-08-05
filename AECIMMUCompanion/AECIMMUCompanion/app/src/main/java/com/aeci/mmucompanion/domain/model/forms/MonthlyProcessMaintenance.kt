package com.aeci.mmucompanion.domain.model.forms

import java.time.LocalDateTime

data class MonthlyProcessMaintenance(
    val id: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val siteLocation: String = "",
    val maintenanceOperator: String = "",
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val status: String = "Draft",
    
    // Monthly Process Maintenance specific fields
    val maintenanceMonth: String = "",
    val processEquipment: List<String> = emptyList(),
    val pumpMaintenance: String = "",
    val motorInspection: String = "",
    val electricalSystems: String = "",
    val controlSystems: String = "",
    val safetyDevices: String = "",
    val pipingInspection: String = "",
    val valveOperation: String = "",
    val pressureTests: Map<String, Double> = emptyMap(),
    val temperatureReadings: Map<String, Double> = emptyMap(),
    val vibrationAnalysis: String = "",
    val lubricationStatus: String = "",
    val filterCondition: String = "",
    val maintenanceActions: List<String> = emptyList(),
    val partsReplaced: List<String> = emptyList(),
    val calibrationDone: Boolean = false,
    val nextMaintenanceDate: LocalDateTime? = null,
    val supervisorApproval: String = "",
    val maintenanceComplete: Boolean = false
)
