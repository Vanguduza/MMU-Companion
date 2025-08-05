package com.aeci.mmucompanion.data.model

import java.time.LocalDate

data class BlastReport(
    val id: String,
    val dateRange: String,
    val fromDate: LocalDate,
    val toDate: LocalDate,
    val bcmBlasted: Double,
    val totalHolesCharged: Int,
    val averageHoleDepth: Double,
    val averageBurden: Double,
    val averageSpacing: Double,
    val averageStemmingLength: Double,
    val averageFlowRate: Double,
    val averageDeliveryRate: Double,
    val averageFinalCupDensity: Double,
    val averagePumpingPressure: Double,
    val totalEmulsionUsed: Double,
    val powderFactor: Double,
    val emulsionSource: String, // "Weighbridge" or "Blast Hole Log"
    val weighbridgeTotal: Double?,
    val blastHoleTotal: Double?,
    val discrepancyPercent: Double?,
    val needsCalibration: Boolean,
    val machineHours: Double,
    val breakdownNotes: List<String>,
    val generatedBy: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val siteId: String
)

data class WeighbridgeTicket(
    val ticketNumber: String,
    val weight: Double,
    val date: LocalDate,
    val vehicle: String = ""
)

data class BlastReportInput(
    val fromDate: LocalDate,
    val toDate: LocalDate,
    val bcmBlasted: Double,
    val weighbridgeTickets: List<WeighbridgeTicket> = emptyList(),
    val fallbackToBlastHoleLog: Boolean = false,
    val siteId: String,
    val generatedBy: String
)

// Summary data extracted from various reports
data class QualityReportSummary(
    val averageHoleDepth: Double,
    val averageBurden: Double,
    val averageSpacing: Double,
    val averageStemmingLength: Double,
    val averageFlowRate: Double,
    val averageDeliveryRate: Double,
    val averageFinalCupDensity: Double,
    val averagePumpingPressure: Double,
    val reportCount: Int
)

data class BlastHoleLogSummary(
    val totalHolesCharged: Int,
    val totalEmulsionUsed: Double,
    val logCount: Int
)

data class ProductionLogSummary(
    val totalMachineHours: Double,
    val breakdownNotes: List<String>,
    val logCount: Int
)
