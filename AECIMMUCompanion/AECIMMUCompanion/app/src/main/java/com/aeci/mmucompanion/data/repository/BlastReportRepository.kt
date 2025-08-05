package com.aeci.mmucompanion.data.repository

import com.aeci.mmucompanion.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface BlastReportRepository {
    suspend fun generateBlastReport(input: BlastReportInput): Result<BlastReport>
    suspend fun saveBlastReport(report: BlastReport): Result<Unit>
    suspend fun getBlastReports(siteId: String): Flow<List<BlastReport>>
    suspend fun getBlastReportById(id: String): Result<BlastReport?>
    suspend fun deleteBlastReport(id: String): Result<Unit>
    
    // Data extraction methods
    suspend fun getQualityReportSummary(
        fromDate: LocalDate,
        toDate: LocalDate,
        siteId: String
    ): Result<QualityReportSummary>
    
    suspend fun getBlastHoleLogSummary(
        fromDate: LocalDate,
        toDate: LocalDate,
        siteId: String
    ): Result<BlastHoleLogSummary>
    
    suspend fun getProductionLogSummary(
        fromDate: LocalDate,
        toDate: LocalDate,
        siteId: String
    ): Result<ProductionLogSummary>
    
    suspend fun updateEmulsionQuantityInReports(
        fromDate: LocalDate,
        toDate: LocalDate,
        siteId: String,
        emulsionUsed: Double
    ): Result<Unit>
}
