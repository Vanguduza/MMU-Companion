package com.aeci.mmucompanion.domain.model.forms

import java.time.LocalDateTime

data class PcPumpHighLowPressureTripTest(
    val id: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val siteLocation: String = "",
    val testOperator: String = "",
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val status: String = "Draft",
    
    // PC Pump High/Low Pressure Trip Test specific fields
    val pumpSerialNumber: String = "",
    val testDate: LocalDateTime? = null,
    val testType: String = "",
    val highPressureSetPoint: Double = 0.0,
    val lowPressureSetPoint: Double = 0.0,
    val testPressure: Double = 0.0,
    val tripActivated: Boolean = false,
    val tripPressure: Double = 0.0,
    val responseTime: Double = 0.0,
    val pumpShutdown: Boolean = false,
    val alarmActivated: Boolean = false,
    val resetFunctional: Boolean = false,
    val pressureGaugeAccuracy: String = "",
    val systemLeakage: String = "",
    val safetyValveOperation: String = "",
    val testResults: String = "",
    val calibrationRequired: Boolean = false,
    val maintenanceRequired: String = "",
    val nextTestDate: LocalDateTime? = null,
    val supervisorApproval: String = "",
    val testComplete: Boolean = false
)
