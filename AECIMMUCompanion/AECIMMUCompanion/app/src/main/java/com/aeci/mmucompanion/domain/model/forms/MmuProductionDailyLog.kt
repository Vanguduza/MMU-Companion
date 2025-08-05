package com.aeci.mmucompanion.domain.model.forms

import java.time.LocalDateTime

data class MmuProductionDailyLog(
    val id: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val siteLocation: String = "",
    val operatorName: String = "",
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val status: String = "Draft",
    
    // MMU Production Daily Log specific fields
    val productionDate: LocalDateTime? = null,
    val shiftType: String = "",
    val mmuSerialNumber: String = "",
    val startTime: LocalDateTime? = null,
    val endTime: LocalDateTime? = null,
    val productionTarget: Double = 0.0,
    val actualProduction: Double = 0.0,
    val productionRate: Double = 0.0,
    val efficiency: Double = 0.0,
    val downtime: Double = 0.0,
    val downtimeReasons: List<String> = emptyList(),
    val rawMaterialsUsed: Map<String, Double> = emptyMap(),
    val consumablesUsed: Map<String, Double> = emptyMap(),
    val qualityChecks: List<String> = emptyList(),
    val processParameters: Map<String, Double> = emptyMap(),
    val environmentalConditions: String = "",
    val safetyIncidents: List<String> = emptyList(),
    val maintenancePerformed: List<String> = emptyList(),
    val operatorNotes: String = "",
    val supervisorReview: String = "",
    val logComplete: Boolean = false
)
