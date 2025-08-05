package com.aeci.mmucompanion.data.repository.impl

import com.aeci.mmucompanion.data.model.*
import com.aeci.mmucompanion.data.repository.BlastReportRepository
import com.aeci.mmucompanion.domain.repository.FormRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlastReportRepositoryImpl @Inject constructor(
    private val formRepository: FormRepository
) : BlastReportRepository {

    private val _blastReports = MutableStateFlow<List<BlastReport>>(emptyList())

    override suspend fun generateBlastReport(input: BlastReportInput): Result<BlastReport> {
        return try {
            // This would typically fetch from database
            val qualitySummary = getQualityReportSummary(input.fromDate, input.toDate, input.siteId).getOrThrow()
            val blastHoleSummary = getBlastHoleLogSummary(input.fromDate, input.toDate, input.siteId).getOrThrow()
            val productionSummary = getProductionLogSummary(input.fromDate, input.toDate, input.siteId).getOrThrow()

            // Generate report logic would be here
            Result.success(createMockBlastReport(input, qualitySummary, blastHoleSummary, productionSummary))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveBlastReport(report: BlastReport): Result<Unit> {
        return try {
            val currentReports = _blastReports.value.toMutableList()
            currentReports.add(report)
            _blastReports.value = currentReports
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBlastReports(siteId: String): Flow<List<BlastReport>> {
        return _blastReports.asStateFlow()
    }

    override suspend fun getBlastReportById(id: String): Result<BlastReport?> {
        return try {
            val report = _blastReports.value.find { it.id == id }
            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBlastReport(id: String): Result<Unit> {
        return try {
            val currentReports = _blastReports.value.toMutableList()
            currentReports.removeAll { it.id == id }
            _blastReports.value = currentReports
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getQualityReportSummary(
        fromDate: LocalDate,
        toDate: LocalDate,
        siteId: String
    ): Result<QualityReportSummary> {
        return try {
            // Mock data - in real implementation, this would query the database
            val summary = QualityReportSummary(
                averageHoleDepth = 12.5,
                averageBurden = 4.0,
                averageSpacing = 4.5,
                averageStemmingLength = 3.5,
                averageFlowRate = 150.0,
                averageDeliveryRate = 75.0,
                averageFinalCupDensity = 1.15,
                averagePumpingPressure = 180.0,
                reportCount = 5
            )
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBlastHoleLogSummary(
        fromDate: LocalDate,
        toDate: LocalDate,
        siteId: String
    ): Result<BlastHoleLogSummary> {
        return try {
            // Mock data - in real implementation, this would query the database
            val summary = BlastHoleLogSummary(
                totalHolesCharged = 156,
                totalEmulsionUsed = 2450.0,
                logCount = 8
            )
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProductionLogSummary(
        fromDate: LocalDate,
        toDate: LocalDate,
        siteId: String
    ): Result<ProductionLogSummary> {
        return try {
            // Mock data - in real implementation, this would query the database
            val summary = ProductionLogSummary(
                totalMachineHours = 18.5,
                breakdownNotes = listOf(
                    "Hydraulic pump maintenance - 2 hours downtime",
                    "Electrical fault resolved - 1 hour downtime"
                ),
                logCount = 3
            )
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEmulsionQuantityInReports(
        fromDate: LocalDate,
        toDate: LocalDate,
        siteId: String,
        emulsionUsed: Double
    ): Result<Unit> {
        return try {
            // In real implementation, this would update MMU Quality and Production reports
            // with the calculated emulsion quantity
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createMockBlastReport(
        input: BlastReportInput,
        qualitySummary: QualityReportSummary,
        blastHoleSummary: BlastHoleLogSummary,
        productionSummary: ProductionLogSummary
    ): BlastReport {
        val weighbridgeTotal = if (input.weighbridgeTickets.isNotEmpty()) {
            input.weighbridgeTickets.sumOf { it.weight }
        } else null

        val blastHoleTotal = blastHoleSummary.totalEmulsionUsed

        val (totalEmulsionUsed, emulsionSource) = when {
            weighbridgeTotal != null && !input.fallbackToBlastHoleLog -> {
                weighbridgeTotal to "Weighbridge"
            }
            else -> {
                blastHoleTotal to "Blast Hole Log"
            }
        }

        val powderFactor = if (input.bcmBlasted > 0) {
            totalEmulsionUsed / input.bcmBlasted
        } else 0.0

        val discrepancyPercent = if (weighbridgeTotal != null && blastHoleTotal > 0) {
            kotlin.math.abs(weighbridgeTotal - blastHoleTotal) / weighbridgeTotal * 100
        } else null

        val needsCalibration = discrepancyPercent?.let { it > 3.0 } ?: false

        val dateRange = if (input.fromDate == input.toDate) {
            input.fromDate.toString()
        } else {
            "${input.fromDate} to ${input.toDate}"
        }

        return BlastReport(
            id = java.util.UUID.randomUUID().toString(),
            dateRange = dateRange,
            fromDate = input.fromDate,
            toDate = input.toDate,
            bcmBlasted = input.bcmBlasted,
            totalHolesCharged = blastHoleSummary.totalHolesCharged,
            averageHoleDepth = qualitySummary.averageHoleDepth,
            averageBurden = qualitySummary.averageBurden,
            averageSpacing = qualitySummary.averageSpacing,
            averageStemmingLength = qualitySummary.averageStemmingLength,
            averageFlowRate = qualitySummary.averageFlowRate,
            averageDeliveryRate = qualitySummary.averageDeliveryRate,
            averageFinalCupDensity = qualitySummary.averageFinalCupDensity,
            averagePumpingPressure = qualitySummary.averagePumpingPressure,
            totalEmulsionUsed = totalEmulsionUsed,
            powderFactor = powderFactor,
            emulsionSource = emulsionSource,
            weighbridgeTotal = weighbridgeTotal,
            blastHoleTotal = blastHoleTotal,
            discrepancyPercent = discrepancyPercent,
            needsCalibration = needsCalibration,
            machineHours = productionSummary.totalMachineHours,
            breakdownNotes = productionSummary.breakdownNotes,
            generatedBy = input.generatedBy,
            siteId = input.siteId
        )
    }
}
