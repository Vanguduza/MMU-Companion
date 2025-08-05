package com.aeci.mmucompanion.data.repository

import android.content.Context
import com.aeci.mmucompanion.data.local.dao.ReportDao
import com.aeci.mmucompanion.data.local.entity.ReportEntity
import com.aeci.mmucompanion.data.remote.api.AECIApiService
import com.aeci.mmucompanion.data.remote.dto.*
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val apiService: AECIApiService,
    private val reportDao: ReportDao,
    private val context: Context
) : ReportRepository {

    private val reportsDir = File(context.filesDir, "reports")
    
    init {
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
    }

    override suspend fun getReportHistory(
        page: Int,
        limit: Int,
        filter: ReportFilter?
    ): Result<ReportHistory> {
        return try {
            val response = apiService.getReportHistory(
                page = page,
                limit = limit,
                reportType = filter?.reportType?.name,
                generatedBy = filter?.generatedBy,
                startDate = filter?.startDate?.toEpochSecond(ZoneOffset.UTC),
                endDate = filter?.endDate?.toEpochSecond(ZoneOffset.UTC),
                format = filter?.format?.name
            )
            
            if (response.success) {
                val reportHistory = ReportHistory(
                    reports = response.data.reports.map { it.toDomainModel() },
                    pagination = response.data.pagination.toDomainModel()
                )
                
                // Cache reports locally
                cacheReports(response.data.reports)
                
                Result.success(reportHistory)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get report history"))
            }
        } catch (e: Exception) {
            // Try to get cached data
            getCachedReportHistory(page, limit, filter)
        }
    }

    override suspend fun getMyReports(page: Int, limit: Int): Result<ReportHistory> {
        return try {
            val response = apiService.getMyReports(page, limit)
            
            if (response.success) {
                val reportHistory = ReportHistory(
                    reports = response.data.reports.map { it.toDomainModel() },
                    pagination = response.data.pagination.toDomainModel()
                )
                
                Result.success(reportHistory)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get user reports"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateReport(request: ReportGenerationRequest): Result<ReportGenerationResult> {
        return try {
            val requestBody = ReportGenerationRequestDto(
                reportType = request.reportType.name,
                reportTitle = request.reportTitle,
                format = request.format.name,
                parameters = request.parameters?.let { gson.toJson(it) },
                formIds = if (request.formIds.isNotEmpty()) gson.toJson(request.formIds) else null
            )
            
            val response = apiService.generateReport(requestBody)
            
            if (response.success) {
                Result.success(
                    ReportGenerationResult(
                        success = true,
                        reportId = response.data.reportId,
                        fileName = response.data.fileName,
                        filePath = response.data.filePath,
                        message = response.message
                    )
                )
            } else {
                Result.success(
                    ReportGenerationResult(
                        success = false,
                        error = response.message
                    )
                )
            }
        } catch (e: Exception) {
            Result.success(
                ReportGenerationResult(
                    success = false,
                    error = e.message
                )
            )
        }
    }

    override suspend fun downloadReport(reportId: String, localFilePath: String?): Result<File> {
        return try {
            val response = apiService.downloadReport(reportId)
            
            // Determine local file path
            val fileName = response.headers()["Content-Disposition"]
                ?.substringAfter("filename=\"")
                ?.substringBefore("\"")
                ?: "report_${reportId}.pdf"
                
            val localFile = localFilePath?.let { File(it) } 
                ?: File(reportsDir, fileName)
            
            // Write response body to file
            response.body()?.let { responseBody ->
                FileOutputStream(localFile).use { output ->
                    responseBody.byteStream().use { input ->
                        input.copyTo(output)
                    }
                }
                Result.success(localFile)
            } ?: Result.failure(Exception("Empty response body"))
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteReport(reportId: String): Result<Unit> {
        return try {
            val response = apiService.deleteReport(reportId)
            
            if (response.success) {
                // Remove from local cache
                reportDao.deleteReport(reportId)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to delete report"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReportStatistics(): Result<ReportStatistics> {
        return try {
            val response = apiService.getReportStatistics()
            
            if (response.success) {
                val statistics = ReportStatistics(
                    totalReports = response.data.totalReports.firstOrNull()?.count ?: 0,
                    reportsByType = response.data.reportsByType.associate { 
                        ReportType.valueOf(it.reportType) to it.count 
                    },
                    reportsByFormat = response.data.reportsByFormat.associate { 
                        ExportFormat.valueOf(it.format) to it.count 
                    },
                    totalDownloads = response.data.totalDownloads.firstOrNull()?.count ?: 0,
                    topGenerators = response.data.topGenerators.map { 
                        TopReportGenerator(
                            user = User(
                                id = "",
                                username = it.username,
                                fullName = it.fullName,
                                email = "",
                                role = UserRole.OPERATOR,
                                department = "",
                                shiftPattern = "",
                                permissions = emptyList(),
                                isActive = true,
                                siteId = "site_001"
                            ),
                            reportCount = it.reportCount
                        )
                    },
                    recentActivity = response.data.recentActivity.map {
                        RecentReportActivity(
                            reportTitle = it.reportTitle,
                            completionDate = LocalDateTime.ofEpochSecond(it.completionDate, 0, ZoneOffset.UTC),
                            generatedBy = it.generatedBy,
                            downloadCount = it.downloadCount
                        )
                    }
                )
                
                Result.success(statistics)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get statistics"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReportTemplates(): Result<List<ReportTemplate>> {
        // Implementation for getting report templates
        return Result.success(getDefaultReportTemplates())
    }

    override suspend fun getReportById(reportId: String): Result<Report> {
        return try {
            // First try to get from cache
            val cachedReport = reportDao.getReportById(reportId)
            if (cachedReport != null) {
                return Result.success(cachedReport.toDomainModel())
            }
            
            // If not cached, fetch from server (this would require a new API endpoint)
            Result.failure(Exception("Report not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchReports(query: String, page: Int, limit: Int): Result<ReportHistory> {
        return try {
            // This would require a search API endpoint
            val filter = ReportFilter(searchQuery = query)
            getReportHistory(page, limit, filter)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReportDownloadHistory(reportId: String): Result<List<ReportDownload>> {
        // Implementation for getting download history
        return Result.success(emptyList())
    }

    override suspend fun uploadReport(
        reportType: ReportType,
        reportTitle: String,
        format: ExportFormat,
        file: File,
        parameters: Map<String, Any>?,
        formIds: List<String>
    ): Result<ReportGenerationResult> {
        return try {
            val filePart = MultipartBody.Part.createFormData(
                "reportFile",
                file.name,
                file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            )
            
            val reportTypePart = reportType.name.toRequestBody("text/plain".toMediaTypeOrNull())
            val titlePart = reportTitle.toRequestBody("text/plain".toMediaTypeOrNull())
            val formatPart = format.name.toRequestBody("text/plain".toMediaTypeOrNull())
            val parametersPart = parameters?.let { 
                gson.toJson(it).toRequestBody("application/json".toMediaTypeOrNull()) 
            }
            val formIdsPart = if (formIds.isNotEmpty()) {
                gson.toJson(formIds).toRequestBody("application/json".toMediaTypeOrNull())
            } else null
            
            val response = apiService.uploadReport(
                reportFile = filePart,
                reportType = reportTypePart,
                reportTitle = titlePart,
                format = formatPart,
                parameters = parametersPart,
                formIds = formIdsPart
            )
            
            if (response.success) {
                Result.success(
                    ReportGenerationResult(
                        success = true,
                        reportId = response.data.reportId,
                        fileName = response.data.fileName,
                        filePath = response.data.filePath,
                        message = response.message
                    )
                )
            } else {
                Result.success(
                    ReportGenerationResult(
                        success = false,
                        error = response.message
                    )
                )
            }
        } catch (e: Exception) {
            Result.success(
                ReportGenerationResult(
                    success = false,
                    error = e.message
                )
            )
        }
    }

    override suspend fun getReportsByType(
        reportType: ReportType,
        page: Int,
        limit: Int
    ): Result<ReportHistory> {
        val filter = ReportFilter(reportType = reportType)
        return getReportHistory(page, limit, filter)
    }

    override suspend fun getReportsByDateRange(
        startDate: Long,
        endDate: Long,
        page: Int,
        limit: Int
    ): Result<ReportHistory> {
        val filter = ReportFilter(
            startDate = LocalDateTime.ofEpochSecond(startDate, 0, ZoneOffset.UTC),
            endDate = LocalDateTime.ofEpochSecond(endDate, 0, ZoneOffset.UTC)
        )
        return getReportHistory(page, limit, filter)
    }

    override suspend fun canAccessReport(reportId: String): Result<Boolean> {
        // Implementation for checking access permissions
        return Result.success(true)
    }

    override suspend fun getReportMetadata(reportId: String): Result<ReportMetadata> {
        // Implementation for getting report metadata
        return Result.failure(Exception("Not implemented"))
    }

    override suspend fun exportReportsBundle(
        reportIds: List<String>,
        bundleName: String
    ): Result<File> {
        // Implementation for bundling multiple reports
        return Result.failure(Exception("Not implemented"))
    }

    override suspend fun scheduleReport(schedule: ReportSchedule): Result<String> {
        // Implementation for scheduling reports
        return Result.failure(Exception("Not implemented"))
    }

    override suspend fun getScheduledReports(): Result<List<ReportSchedule>> {
        // Implementation for getting scheduled reports
        return Result.success(emptyList())
    }

    override suspend fun cancelScheduledReport(scheduleId: String): Result<Unit> {
        // Implementation for canceling scheduled reports
        return Result.success(Unit)
    }

    override suspend fun syncReports(): Result<Unit> {
        return try {
            val response = getReportHistory(1, 100)
            response.fold(
                onSuccess = { 
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCachedReports(): Flow<List<Report>> {
        return reportDao.getAllReports().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun cacheReport(reportId: String): Result<Unit> {
        return try {
            val report = getReportById(reportId)
            report.fold(
                onSuccess = { reportData ->
                    reportDao.insertReport(reportData.toEntity())
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearReportCache(): Result<Unit> {
        return try {
            reportDao.clearAllReports()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper methods
    private suspend fun cacheReports(reports: List<ReportDto>) {
        try {
            reports.forEach { reportDto ->
                reportDao.insertReport(reportDto.toEntity())
            }
        } catch (e: Exception) {
            // Silently fail caching
        }
    }

    private suspend fun getCachedReportHistory(
        page: Int,
        limit: Int,
        filter: ReportFilter?
    ): Result<ReportHistory> {
        return try {
            val offset = (page - 1) * limit
            val cachedReports = reportDao.getReportsPaginated(limit, offset)
            val totalCount = reportDao.getReportCount()
            
            val reportHistory = ReportHistory(
                reports = cachedReports.map { it.toDomainModel() },
                pagination = PaginationInfo(
                    page = page,
                    limit = limit,
                    total = totalCount,
                    totalPages = (totalCount + limit - 1) / limit
                )
            )
            
            Result.success(reportHistory)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getDefaultReportTemplates(): List<ReportTemplate> {
        return ReportType.values().map { reportType ->
            ReportTemplate(
                id = reportType.name.lowercase(),
                name = reportType.displayName,
                description = reportType.description,
                reportType = reportType,
                defaultFormat = ExportFormat.PDF,
                parameters = getDefaultParametersForReportType(reportType),
                createdBy = "system",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }
    }

    private fun getDefaultParametersForReportType(reportType: ReportType): List<ReportParameter> {
        return when (reportType) {
            ReportType.FORM_SUMMARY -> listOf(
                ReportParameter("date_range", "Date Range", ParameterType.DATE_RANGE, true),
                ReportParameter("form_types", "Form Types", ParameterType.MULTI_SELECT, false)
            )
            ReportType.EQUIPMENT_REPORT -> listOf(
                ReportParameter("equipment_ids", "Equipment", ParameterType.EQUIPMENT_SELECT, false),
                ReportParameter("include_maintenance", "Include Maintenance", ParameterType.BOOLEAN, false)
            )
            else -> emptyList()
        }
    }

    // Extension function to convert Report to ReportEntity
    private fun Report.toEntity(): com.aeci.mmucompanion.data.local.entity.ReportEntity {
        return com.aeci.mmucompanion.data.local.entity.ReportEntity(
            id = id,
            reportType = reportType.name,
            reportTitle = reportTitle,
            generatedById = generatedBy.id,
            generatedByName = generatedBy.fullName,
            completionDate = completionDate.toEpochSecond(java.time.ZoneOffset.UTC),
            fileName = fileName,
            fileSize = fileSize,
            format = format.name,
            status = status.name,
            downloadCount = downloadCount,
            lastDownloaded = lastDownloaded?.toEpochSecond(java.time.ZoneOffset.UTC),
            createdAt = createdAt.toEpochSecond(java.time.ZoneOffset.UTC)
        )
    }

    companion object {
        private val gson = com.google.gson.Gson()
    }
} 