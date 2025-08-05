package com.aeci.mmucompanion.domain.model.forms

import java.time.LocalDateTime

data class PumpInspection90Day(
    val id: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val siteLocation: String = "",
    val inspectorName: String = "",
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val status: String = "Draft",
    
    // 90 Day Pump Inspection specific fields
    val pumpSerialNumber: String = "",
    val pumpType: String = "",
    val inspectionDate: LocalDateTime? = null,
    val lastInspectionDate: LocalDateTime? = null,
    val operatingHours: Double = 0.0,
    val mechanicalInspection: String = "",
    val electricalInspection: String = "",
    val hydraulicInspection: String = "",
    val performanceTest: String = "",
    val pressureTest: Map<String, Double> = emptyMap(),
    val flowRateTest: Double = 0.0,
    val vibrationAnalysis: String = "",
    val temperatureReadings: Map<String, Double> = emptyMap(),
    val alignmentChecks: String = "",
    val bearingCondition: String = "",
    val sealCondition: String = "",
    val lubricationSystem: String = "",
    val coolingSystem: String = "",
    val controlSystems: String = "",
    val safetyDevices: String = "",
    val defectsFound: List<String> = emptyList(),
    val correctiveActions: List<String> = emptyList(),
    val partsReplaced: List<String> = emptyList(),
    val nextInspectionDate: LocalDateTime? = null,
    val inspectionComplete: Boolean = false,
    val supervisorApproval: String = ""
)
