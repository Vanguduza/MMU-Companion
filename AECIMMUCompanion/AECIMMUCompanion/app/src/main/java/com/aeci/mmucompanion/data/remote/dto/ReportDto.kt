package com.aeci.mmucompanion.data.remote.dto

import com.aeci.mmucompanion.data.local.entity.ReportEntity
import com.aeci.mmucompanion.domain.model.*
import java.time.LocalDateTime
import java.time.ZoneOffset

// API Response DTOs
data class ReportHistoryResponse(
    val success: Boolean,
    val data: ReportHistoryData,
    val message: String? = null
)

data class ReportHistoryData(
    val reports: List<ReportDto>,
    val pagination: PaginationDto
)

data class ReportDto(
    val id: String,
    val reportType: String,
    val reportTitle: String,
    val generatedBy: UserDto,
    val completionDate: Long,
    val fileName: String,
    val fileSize: Long,
    val format: String,
    val parameters: String? = null,
    val formIds: List<String> = emptyList(),
    val status: String,
    val downloadCount: Int,
    val lastDownloaded: Long? = null,
    val createdAt: Long
) {
    fun toDomainModel(): Report = Report(
        id = id,
        reportType = ReportType.valueOf(reportType),
        reportTitle = reportTitle,
        generatedBy = generatedBy.toDomainModel(),
        completionDate = LocalDateTime.ofEpochSecond(completionDate, 0, ZoneOffset.UTC),
        fileName = fileName,
        fileSize = fileSize,
        format = ExportFormat.valueOf(format),
        parameters = parameters?.let { com.google.gson.Gson().fromJson(it, Map::class.java) as Map<String, Any> },
        formIds = formIds,
        status = ReportStatus.valueOf(status),
        downloadCount = downloadCount,
        lastDownloaded = lastDownloaded?.let { 
            LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) 
        },
        createdAt = LocalDateTime.ofEpochSecond(createdAt, 0, ZoneOffset.UTC)
    )
    
    fun toEntity(): ReportEntity = ReportEntity(
        id = id,
        reportType = reportType,
        reportTitle = reportTitle,
        generatedById = generatedBy.id,
        generatedByName = generatedBy.fullName,
        completionDate = completionDate,
        fileName = fileName,
        fileSize = fileSize,
        format = format,
        parameters = parameters,
        formIds = formIds.joinToString(","),
        status = status,
        downloadCount = downloadCount,
        lastDownloaded = lastDownloaded,
        createdAt = createdAt
    )
}

// API Request DTOs
data class ReportGenerationRequestDto(
    val reportType: String,
    val reportTitle: String,
    val format: String,
    val parameters: String? = null,
    val formIds: String? = null
)

data class ReportGenerationResponseDto(
    val success: Boolean,
    val data: ReportGenerationDataDto,
    val message: String? = null
)

data class ReportGenerationDataDto(
    val reportId: String,
    val fileName: String,
    val filePath: String
)

// Statistics DTOs
data class ReportStatisticsResponse(
    val success: Boolean,
    val data: ReportStatisticsData,
    val message: String? = null
)

data class ReportStatisticsData(
    val totalReports: List<CountDto>,
    val reportsByType: List<ReportTypeCountDto>,
    val reportsByFormat: List<FormatCountDto>,
    val totalDownloads: List<CountDto>,
    val topGenerators: List<TopGeneratorDto>,
    val recentActivity: List<RecentActivityDto>
)

data class CountDto(
    val count: Int
)

data class ReportTypeCountDto(
    val reportType: String,
    val count: Int
)

data class FormatCountDto(
    val format: String,
    val count: Int
)

data class TopGeneratorDto(
    val fullName: String,
    val username: String,
    val reportCount: Int
)

data class RecentActivityDto(
    val reportTitle: String,
    val completionDate: Long,
    val generatedBy: String,
    val downloadCount: Int
)

// My Reports Response
data class MyReportsResponse(
    val success: Boolean,
    val data: ReportHistoryData,
    val message: String? = null
)

// Delete Response
data class DeleteReportResponse(
    val success: Boolean,
    val message: String
)

// Upload Response
data class UploadReportResponse(
    val success: Boolean,
    val data: ReportGenerationDataDto,
    val message: String? = null
) 