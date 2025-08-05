package com.aeci.mmucompanion.domain.model

import java.time.LocalDateTime

data class Report(
    val id: String,
    val reportType: ReportType,
    val reportTitle: String,
    val generatedBy: User,
    val completionDate: LocalDateTime,
    val fileName: String,
    val fileSize: Long,
    val format: ExportFormat,
    val parameters: Map<String, Any>? = null,
    val formIds: List<String> = emptyList(),
    val status: ReportStatus = ReportStatus.COMPLETED,
    val downloadCount: Int = 0,
    val lastDownloaded: LocalDateTime? = null,
    val createdAt: LocalDateTime
)

data class ReportHistory(
    val reports: List<Report>,
    val pagination: PaginationInfo
)

data class PaginationInfo(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)

data class ReportDownload(
    val id: String,
    val reportId: String,
    val downloadedBy: User,
    val downloadDate: LocalDateTime,
    val ipAddress: String? = null,
    val userAgent: String? = null
)

data class ReportStatistics(
    val totalReports: Int,
    val reportsByType: Map<ReportType, Int>,
    val reportsByFormat: Map<ExportFormat, Int>,
    val totalDownloads: Int,
    val topGenerators: List<TopReportGenerator>,
    val recentActivity: List<RecentReportActivity>
)

data class TopReportGenerator(
    val user: User,
    val reportCount: Int
)

data class RecentReportActivity(
    val reportTitle: String,
    val completionDate: LocalDateTime,
    val generatedBy: String,
    val downloadCount: Int
)

data class ReportGenerationRequest(
    val reportType: ReportType,
    val reportTitle: String,
    val format: ExportFormat,
    val parameters: Map<String, Any>? = null,
    val formIds: List<String> = emptyList(),
    val dateRange: DateRange? = null,
    val equipmentIds: List<String> = emptyList(),
    val userIds: List<String> = emptyList()
)

data class DateRange(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
)

data class ReportGenerationResult(
    val success: Boolean,
    val reportId: String? = null,
    val fileName: String? = null,
    val filePath: String? = null,
    val message: String? = null,
    val error: String? = null
)

enum class ReportType(val displayName: String, val description: String) {
    FORM_SUMMARY("Form Summary", "Complete overview of all forms"),
    EQUIPMENT_REPORT("Equipment Report", "Equipment status and maintenance"),
    COMPLIANCE_REPORT("Compliance Report", "Safety and compliance metrics"),
    PRODUCTION_REPORT("Production Report", "Daily production statistics"),
    MAINTENANCE_REPORT("Maintenance Report", "Maintenance schedules and history"),
    USER_ACTIVITY("User Activity", "User activity and performance"),
    AUDIT_LOG("Audit Log", "System audit trail"),
    AVAILABILITY_UTILIZATION("Availability & Utilization", "Equipment availability and utilization"),
    QUALITY_CONTROL("Quality Control", "Quality control metrics"),
    SAFETY_REPORT("Safety Report", "Safety incidents and metrics"),
    TIMESHEET_REPORT("Timesheet Report", "User timesheet data"),
    BLAST_REPORT("Blast Report", "Comprehensive blast operation report"),
    CUSTOM_REPORT("Custom Report", "Custom generated report")
}

enum class ReportStatus {
    PENDING,
    GENERATING,
    COMPLETED,
    FAILED,
    EXPIRED
}

data class ReportFilter(
    val reportType: ReportType? = null,
    val generatedBy: String? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val format: ExportFormat? = null,
    val status: ReportStatus? = null,
    val searchQuery: String? = null
)

data class ReportTemplate(
    val id: String,
    val name: String,
    val description: String,
    val reportType: ReportType,
    val defaultFormat: ExportFormat,
    val parameters: List<ReportParameter>,
    val isActive: Boolean = true,
    val createdBy: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class ReportParameter(
    val key: String,
    val displayName: String,
    val parameterType: ParameterType,
    val required: Boolean = false,
    val defaultValue: Any? = null,
    val options: List<String> = emptyList(),
    val validation: ParameterValidation? = null
)

enum class ParameterType {
    STRING,
    INTEGER,
    DOUBLE,
    BOOLEAN,
    DATE,
    DATE_RANGE,
    SINGLE_SELECT,
    MULTI_SELECT,
    USER_SELECT,
    EQUIPMENT_SELECT,
    FORM_SELECT
}

data class ParameterValidation(
    val minValue: Number? = null,
    val maxValue: Number? = null,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val pattern: String? = null,
    val customValidation: String? = null
)

data class ReportMetadata(
    val generatedDate: LocalDateTime,
    val generatedBy: User,
    val parameters: Map<String, Any>,
    val dataSourceCount: Int,
    val processingTimeMs: Long,
    val fileSize: Long,
    val checksum: String? = null
)

data class ReportSchedule(
    val id: String,
    val name: String,
    val reportTemplate: ReportTemplate,
    val schedule: SchedulePattern,
    val recipients: List<String>,
    val isActive: Boolean = true,
    val lastRun: LocalDateTime? = null,
    val nextRun: LocalDateTime,
    val createdBy: String,
    val createdAt: LocalDateTime
)

data class SchedulePattern(
    val frequency: ScheduleFrequency,
    val interval: Int = 1,
    val daysOfWeek: List<DayOfWeek> = emptyList(),
    val dayOfMonth: Int? = null,
    val time: String, // HH:mm format
    val timezone: String = "UTC"
)

enum class ScheduleFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

enum class DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
} 