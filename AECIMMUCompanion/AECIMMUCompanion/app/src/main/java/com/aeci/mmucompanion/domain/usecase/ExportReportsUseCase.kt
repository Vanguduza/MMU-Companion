package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.domain.repository.ReportRepository
import com.aeci.mmucompanion.domain.repository.FormRepository
import com.aeci.mmucompanion.domain.repository.EquipmentRepository
import com.aeci.mmucompanion.domain.repository.UserRepository
import java.time.LocalDateTime
import javax.inject.Inject

class ExportReportsUseCase @Inject constructor(
    private val reportRepository: ReportRepository,
    private val formRepository: FormRepository,
    private val equipmentRepository: EquipmentRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        reportType: String,
        format: ExportFormat,
        parameters: Map<String, Any>? = null,
        formIds: List<String> = emptyList()
    ): Result<String> {
        return try {
            val reportTypeEnum = ReportType.valueOf(reportType)
            
            // Create report generation request
            val request = ReportGenerationRequest(
                reportType = reportTypeEnum,
                reportTitle = generateReportTitle(reportTypeEnum),
                format = format,
                parameters = parameters,
                formIds = formIds,
                dateRange = extractDateRange(parameters),
                equipmentIds = extractEquipmentIds(parameters),
                userIds = extractUserIds(parameters)
            )
            
            // Generate the report
            val result = reportRepository.generateReport(request)
            
            result.fold(
                onSuccess = { generationResult ->
                    if (generationResult.success) {
                        Result.success(generationResult.filePath ?: "")
                    } else {
                        Result.failure(Exception(generationResult.error ?: "Report generation failed"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun generateDetailedReport(
        reportType: ReportType,
        format: ExportFormat,
        parameters: Map<String, Any>? = null
    ): Result<ReportGenerationResult> {
        return try {
            // Validate parameters based on report type
            val validatedParameters = validateParameters(reportType, parameters)
            
            // Generate appropriate report title
            val reportTitle = generateDetailedReportTitle(reportType, validatedParameters)
            
            // Extract relevant data based on report type
            val relevantFormIds = extractRelevantFormIds(reportType, validatedParameters)
            
            // Create the generation request
            val request = ReportGenerationRequest(
                reportType = reportType,
                reportTitle = reportTitle,
                format = format,
                parameters = validatedParameters,
                formIds = relevantFormIds,
                dateRange = extractDateRange(validatedParameters),
                equipmentIds = extractEquipmentIds(validatedParameters),
                userIds = extractUserIds(validatedParameters)
            )
            
            reportRepository.generateReport(request)
        } catch (e: Exception) {
            Result.success(
                ReportGenerationResult(
                    success = false,
                    error = e.message
                )
            )
        }
    }
    
    suspend fun generateFormSummaryReport(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        formTypes: List<FormType>? = null,
        format: ExportFormat = ExportFormat.PDF
    ): Result<ReportGenerationResult> {
        val parameters = mutableMapOf<String, Any>(
            "startDate" to startDate.toString(),
            "endDate" to endDate.toString()
        )
        
        formTypes?.let { types ->
            parameters["formTypes"] = types.map { it.name }
        }
        
        return generateDetailedReport(
            reportType = ReportType.FORM_SUMMARY,
            format = format,
            parameters = parameters
        )
    }
    
    suspend fun generateEquipmentReport(
        equipmentIds: List<String>? = null,
        includeMaintenanceHistory: Boolean = true,
        format: ExportFormat = ExportFormat.PDF
    ): Result<ReportGenerationResult> {
        val parameters = mutableMapOf<String, Any>(
            "includeMaintenanceHistory" to includeMaintenanceHistory
        )
        
        equipmentIds?.let { ids ->
            parameters["equipmentIds"] = ids
        }
        
        return generateDetailedReport(
            reportType = ReportType.EQUIPMENT_REPORT,
            format = format,
            parameters = parameters
        )
    }
    
    suspend fun generateComplianceReport(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        includeIncidents: Boolean = true,
        format: ExportFormat = ExportFormat.PDF
    ): Result<ReportGenerationResult> {
        val parameters = mapOf<String, Any>(
            "startDate" to startDate.toString(),
            "endDate" to endDate.toString(),
            "includeIncidents" to includeIncidents
        )
        
        return generateDetailedReport(
            reportType = ReportType.COMPLIANCE_REPORT,
            format = format,
            parameters = parameters
        )
    }
    
    suspend fun generateProductionReport(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        equipmentIds: List<String>? = null,
        format: ExportFormat = ExportFormat.EXCEL
    ): Result<ReportGenerationResult> {
        val parameters = mutableMapOf<String, Any>(
            "startDate" to startDate.toString(),
            "endDate" to endDate.toString()
        )
        
        equipmentIds?.let { ids ->
            parameters["equipmentIds"] = ids
        }
        
        return generateDetailedReport(
            reportType = ReportType.PRODUCTION_REPORT,
            format = format,
            parameters = parameters
        )
    }
    
    private fun generateReportTitle(reportType: ReportType): String {
        val timestamp = LocalDateTime.now().toString().substring(0, 19)
        return "${reportType.displayName} - $timestamp"
    }
    
    private fun generateDetailedReportTitle(
        reportType: ReportType, 
        parameters: Map<String, Any>?
    ): String {
        val baseTitle = reportType.displayName
        val timestamp = LocalDateTime.now().toString().substring(0, 19)
        
        val additionalInfo = when (reportType) {
            ReportType.FORM_SUMMARY -> {
                val startDate = parameters?.get("startDate")?.toString()?.substring(0, 10)
                val endDate = parameters?.get("endDate")?.toString()?.substring(0, 10)
                if (startDate != null && endDate != null) " ($startDate to $endDate)" else ""
            }
            ReportType.EQUIPMENT_REPORT -> {
                val equipmentCount = (parameters?.get("equipmentIds") as? List<*>)?.size
                if (equipmentCount != null) " ($equipmentCount Equipment)" else ""
            }
            else -> ""
        }
        
        return "$baseTitle$additionalInfo - $timestamp"
    }
    
    private suspend fun validateParameters(
        reportType: ReportType,
        parameters: Map<String, Any>?
    ): Map<String, Any> {
        val validatedParams = parameters?.toMutableMap() ?: mutableMapOf()
        
        when (reportType) {
            ReportType.FORM_SUMMARY -> {
                // Ensure date range is provided
                if (!validatedParams.containsKey("startDate")) {
                    validatedParams["startDate"] = LocalDateTime.now().minusDays(30).toString()
                }
                if (!validatedParams.containsKey("endDate")) {
                    validatedParams["endDate"] = LocalDateTime.now().toString()
                }
            }
            ReportType.EQUIPMENT_REPORT -> {
                // Set default include maintenance history if not specified
                if (!validatedParams.containsKey("includeMaintenanceHistory")) {
                    validatedParams["includeMaintenanceHistory"] = true
                }
            }
            else -> {
                // Default validation for other report types
            }
        }
        
        return validatedParams
    }
    
    private suspend fun extractRelevantFormIds(
        reportType: ReportType,
        parameters: Map<String, Any>?
    ): List<String> {
        return when (reportType) {
            ReportType.FORM_SUMMARY -> {
                val dateRange = extractDateRange(parameters)
                if (dateRange != null) {
                    // Get forms within date range
                    formRepository.getFormsByDateRange(
                        dateRange.startDate.toString(),
                        dateRange.endDate.toString()
                    ).map { it.id }
                } else {
                    emptyList()
                }
            }
            else -> parameters?.get("formIds") as? List<String> ?: emptyList()
        }
    }
    
    private fun extractDateRange(parameters: Map<String, Any>?): DateRange? {
        val startDate = parameters?.get("startDate")?.toString()
        val endDate = parameters?.get("endDate")?.toString()
        
        return if (startDate != null && endDate != null) {
            try {
                DateRange(
                    startDate = LocalDateTime.parse(startDate),
                    endDate = LocalDateTime.parse(endDate)
                )
            } catch (e: Exception) {
                null
            }
        } else null
    }
    
    private fun extractEquipmentIds(parameters: Map<String, Any>?): List<String> {
        return parameters?.get("equipmentIds") as? List<String> ?: emptyList()
    }
    
    private fun extractUserIds(parameters: Map<String, Any>?): List<String> {
        return parameters?.get("userIds") as? List<String> ?: emptyList()
    }
}

