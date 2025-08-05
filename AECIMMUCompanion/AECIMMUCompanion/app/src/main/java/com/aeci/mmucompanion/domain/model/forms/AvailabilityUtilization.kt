package com.aeci.mmucompanion.domain.model.forms

import java.time.LocalDateTime

data class AvailabilityUtilization(
    val id: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val siteLocation: String = "",
    val operatorName: String = "",
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val status: String = "Draft",
    
    // Availability & Utilization specific fields
    val reportingPeriod: String = "",
    val equipmentId: String = "",
    val equipmentType: String = "",
    val plannedOperatingHours: Double = 0.0,
    val actualOperatingHours: Double = 0.0,
    val downTimeHours: Double = 0.0,
    val maintenanceHours: Double = 0.0,
    val breakdownHours: Double = 0.0,
    val availabilityPercentage: Double = 0.0,
    val utilizationPercentage: Double = 0.0,
    val productionTargets: Map<String, Double> = emptyMap(),
    val actualProduction: Map<String, Double> = emptyMap(),
    val efficiencyRating: String = "",
    val downtimeReasons: List<String> = emptyList(),
    val maintenanceActivities: List<String> = emptyList(),
    val operatorComments: String = "",
    val improvementRecommendations: String = "",
    val supervisorReview: String = "",
    val reportComplete: Boolean = false
)
