package com.aeci.mmucompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aeci.mmucompanion.data.remote.dto.ReportDto
import com.aeci.mmucompanion.domain.model.*
import java.time.LocalDateTime
import java.time.ZoneOffset

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey
    val id: String,
    val reportType: String,
    val reportTitle: String,
    val generatedById: String,
    val generatedByName: String,
    val completionDate: Long,
    val fileName: String,
    val fileSize: Long,
    val format: String,
    val parameters: String? = null,
    val formIds: String = "", // Comma-separated IDs
    val status: String,
    val downloadCount: Int,
    val lastDownloaded: Long? = null,
    val createdAt: Long,
    val localFilePath: String? = null, // Path to locally cached file
    val isDownloaded: Boolean = false,
    val lastSynced: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): Report = Report(
        id = id,
        reportType = ReportType.valueOf(reportType),
        reportTitle = reportTitle,
        generatedBy = User(
            id = generatedById,
            username = "",
            fullName = generatedByName,
            email = "",
            role = UserRole.OPERATOR,
            department = "",
            shiftPattern = "",
            permissions = emptyList(),
            isActive = true,
            siteId = "site_001"
        ),
        completionDate = LocalDateTime.ofEpochSecond(completionDate, 0, ZoneOffset.UTC),
        fileName = fileName,
        fileSize = fileSize,
        format = ExportFormat.valueOf(format),
        parameters = parameters?.let { 
            com.google.gson.Gson().fromJson(it, Map::class.java) as Map<String, Any> 
        },
        formIds = if (formIds.isBlank()) emptyList() else formIds.split(","),
        status = ReportStatus.valueOf(status),
        downloadCount = downloadCount,
        lastDownloaded = lastDownloaded?.let { 
            LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) 
        },
        createdAt = LocalDateTime.ofEpochSecond(createdAt, 0, ZoneOffset.UTC)
    )
}

fun Report.toEntity(): ReportEntity = ReportEntity(
    id = id,
    reportType = reportType.name,
    reportTitle = reportTitle,
    generatedById = generatedBy.id,
    generatedByName = generatedBy.fullName,
    completionDate = completionDate.toEpochSecond(ZoneOffset.UTC),
    fileName = fileName,
    fileSize = fileSize,
    format = format.name,
    parameters = parameters?.let { com.google.gson.Gson().toJson(it) },
    formIds = formIds.joinToString(","),
    status = status.name,
    downloadCount = downloadCount,
    lastDownloaded = lastDownloaded?.toEpochSecond(ZoneOffset.UTC),
    createdAt = createdAt.toEpochSecond(ZoneOffset.UTC)
) 

fun ReportDto.toEntity(): ReportEntity {
    return ReportEntity(
        id = id,
        reportType = reportType,
        reportTitle = reportTitle,
        generatedById = generatedBy.id,
        generatedByName = generatedBy.fullName, // Use fullName instead of name
        completionDate = completionDate,
        fileName = fileName,
        fileSize = fileSize,
        format = format,
        status = status,
        downloadCount = downloadCount,
        lastDownloaded = lastDownloaded,
        createdAt = createdAt
    )
} 