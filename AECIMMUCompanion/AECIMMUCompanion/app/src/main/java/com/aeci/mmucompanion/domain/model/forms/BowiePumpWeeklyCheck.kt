package com.aeci.mmucompanion.domain.model.forms

import java.time.LocalDateTime

data class BowiePumpWeeklyCheck(
    val id: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val siteLocation: String = "",
    val inspectorName: String = "",
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val status: String = "Draft",
    
    // Bowie Pump specific fields
    val pumpSerialNumber: String = "",
    val pumpLocation: String = "",
    val weekEndingDate: LocalDateTime? = null,
    val motorCondition: String = "",
    val pumpCondition: String = "",
    val beltTension: String = "",
    val oilLevel: String = "",
    val pressureReading: Double = 0.0,
    val temperatureReading: Double = 0.0,
    val vibrationCheck: String = "",
    val noiseLevel: String = "",
    val leakageCheck: String = "",
    val safetyDevices: String = "",
    val emergencyStop: String = "",
    val maintenanceRequired: String = "",
    val correctiveActions: String = "",
    val nextScheduledMaintenance: LocalDateTime? = null,
    val supervisorSignature: String = "",
    val inspectionComplete: Boolean = false
)
