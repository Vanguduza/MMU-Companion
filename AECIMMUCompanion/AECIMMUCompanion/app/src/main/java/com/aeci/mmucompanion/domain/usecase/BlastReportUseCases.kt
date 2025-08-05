package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.data.model.*
import com.aeci.mmucompanion.data.repository.BlastReportRepository
import com.aeci.mmucompanion.domain.repository.FormRepository
import com.aeci.mmucompanion.domain.repository.EquipmentRepository
import com.aeci.mmucompanion.domain.model.EquipmentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

class GenerateBlastReportUseCase @Inject constructor(
    private val blastReportRepository: BlastReportRepository,
    private val formRepository: FormRepository,
    private val equipmentRepository: EquipmentRepository,
    private val updateEquipmentCalibrationStatusUseCase: UpdateEquipmentCalibrationStatusUseCase
) {
    suspend operator fun invoke(input: BlastReportInput): Result<BlastReport> = withContext(Dispatchers.IO) {
        try {
            // Extract data from various reports
            val qualitySummaryResult = blastReportRepository.getQualityReportSummary(
                input.fromDate, input.toDate, input.siteId
            )
            val blastHoleSummaryResult = blastReportRepository.getBlastHoleLogSummary(
                input.fromDate, input.toDate, input.siteId
            )
            val productionSummaryResult = blastReportRepository.getProductionLogSummary(
                input.fromDate, input.toDate, input.siteId
            )

            // Handle potential errors
            val qualitySummary = qualitySummaryResult.getOrElse { 
                return@withContext Result.failure(it)
            }
            val blastHoleSummary = blastHoleSummaryResult.getOrElse { 
                return@withContext Result.failure(it)
            }
            val productionSummary = productionSummaryResult.getOrElse { 
                return@withContext Result.failure(it)
            }

            // Calculate emulsion totals
            val weighbridgeTotal = if (input.weighbridgeTickets.isNotEmpty()) {
                input.weighbridgeTickets.sumOf { it.weight }
            } else null

            val blastHoleTotal = blastHoleSummary.totalEmulsionUsed

            // Determine emulsion source and total
            val (totalEmulsionUsed, emulsionSource) = when {
                weighbridgeTotal != null && !input.fallbackToBlastHoleLog -> {
                    weighbridgeTotal to "Weighbridge"
                }
                else -> {
                    blastHoleTotal to "Blast Hole Log"
                }
            }

            // Calculate powder factor
            val powderFactor = if (input.bcmBlasted > 0) {
                totalEmulsionUsed / input.bcmBlasted
            } else 0.0

            // Calculate discrepancy and calibration flag
            val discrepancyPercent = if (weighbridgeTotal != null && blastHoleTotal > 0) {
                abs(weighbridgeTotal - blastHoleTotal) / weighbridgeTotal * 100
            } else null

            val needsCalibration = discrepancyPercent?.let { it > 3.0 } ?: false

            // Create date range string
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val dateRange = if (input.fromDate == input.toDate) {
                input.fromDate.format(dateFormatter)
            } else {
                "${input.fromDate.format(dateFormatter)} to ${input.toDate.format(dateFormatter)}"
            }

            // Create blast report
            val blastReport = BlastReport(
                id = UUID.randomUUID().toString(),
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

            // Update emulsion quantities in related reports
            blastReportRepository.updateEmulsionQuantityInReports(
                input.fromDate,
                input.toDate,
                input.siteId,
                totalEmulsionUsed
            )

            // Update MMU equipment status if calibration is needed
            if (needsCalibration) {
                try {
                    val mmuEquipment = equipmentRepository.getEquipmentBySite(input.siteId)
                        .filter { it.type == EquipmentType.MMU }
                    
                    mmuEquipment.forEach { equipment ->
                        val updatedEquipment = updateEquipmentCalibrationStatusUseCase(
                            equipment, 
                            needsCalibration
                        )
                        equipmentRepository.updateEquipment(updatedEquipment)
                    }
                } catch (e: Exception) {
                    // Log equipment update failure but don't fail the entire blast report generation
                    // In a real app, you might want to add proper logging here
                }
            }

            // Save the blast report
            blastReportRepository.saveBlastReport(blastReport).fold(
                onSuccess = { Result.success(blastReport) },
                onFailure = { Result.failure(it) }
            )

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GetBlastReportsUseCase @Inject constructor(
    private val blastReportRepository: BlastReportRepository
) {
    suspend operator fun invoke(siteId: String) = blastReportRepository.getBlastReports(siteId)
}

class DeleteBlastReportUseCase @Inject constructor(
    private val blastReportRepository: BlastReportRepository
) {
    suspend operator fun invoke(reportId: String): Result<Unit> {
        return blastReportRepository.deleteBlastReport(reportId)
    }
}

