package com.aeci.mmucompanion.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class FileShareManager(private val context: Context) {
    
    companion object {
        private const val FILE_PROVIDER_AUTHORITY = "com.aeci.mmucompanion.fileprovider"
        
        // File types and MIME types
        private val MIME_TYPES = mapOf(
            "pdf" to "application/pdf",
            "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "csv" to "text/csv",
            "json" to "application/json",
            "txt" to "text/plain"
        )
    }
    
    /**
     * Represents a generated report file
     */
    data class ReportFile(
        val name: String,
        val type: String, // pdf, xlsx, csv, json
        val content: ByteArray,
        val metadata: Map<String, String> = emptyMap()
    ) {
        fun getDisplayName(): String {
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            return "${name}_${timestamp}.${type}"
        }
        
        fun getMimeType(): String = MIME_TYPES[type.lowercase()] ?: "application/octet-stream"
    }
    
    /**
     * Generates a mock report file based on type and content
     */
    fun generateReportFile(
        reportType: String,
        format: String,
        dateRange: String,
        includeCharts: Boolean = true,
        includeRawData: Boolean = false
    ): ReportFile {
        val content = when (format.lowercase()) {
            "pdf" -> generatePdfContent(reportType, dateRange, includeCharts)
            "xlsx" -> generateExcelContent(reportType, dateRange, includeRawData)
            "csv" -> generateCsvContent(reportType, dateRange)
            "json" -> generateJsonContent(reportType, dateRange, includeRawData)
            else -> generateTextContent(reportType, dateRange)
        }
        
        return ReportFile(
            name = reportType.replace(" ", "_"),
            type = format.lowercase(),
            content = content,
            metadata = mapOf(
                "reportType" to reportType,
                "dateRange" to dateRange,
                "generatedAt" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                "includeCharts" to includeCharts.toString(),
                "includeRawData" to includeRawData.toString()
            )
        )
    }
    
    /**
     * Saves the report file to internal storage and shows share/open dialog
     */
    suspend fun saveAndShareReport(reportFile: ReportFile): Boolean {
        return try {
            // Create reports directory if it doesn't exist
            val reportsDir = File(context.filesDir, "reports")
            if (!reportsDir.exists()) {
                reportsDir.mkdirs()
            }
            
            // Save file
            val file = File(reportsDir, reportFile.getDisplayName())
            FileOutputStream(file).use { output ->
                output.write(reportFile.content)
            }
            
            // Show share/open dialog
            showShareDialog(file, reportFile)
            
            true
        } catch (e: Exception) {
            android.util.Log.e("FileShareManager", "Error saving/sharing report", e)
            Toast.makeText(context, "Error generating report: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }
    
    /**
     * Shows the share/open dialog for the generated report
     */
    private fun showShareDialog(file: File, reportFile: ReportFile) {
        try {
            val uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
            
            // Create chooser intent that shows both "Open with" and "Share" options
            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, reportFile.getMimeType())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = reportFile.getMimeType()
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "AECI MMU Report - ${reportFile.name}")
                putExtra(Intent.EXTRA_TEXT, buildReportDescription(reportFile))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            // Create chooser that includes both open and share options
            val chooserIntent = Intent.createChooser(shareIntent, "Share Report")
            
            // Add "Open with" option to chooser
            if (openIntent.resolveActivity(context.packageManager) != null) {
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(openIntent))
            }
            
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
            
            // Show success message
            Toast.makeText(
                context, 
                "Report generated successfully!\nFile: ${reportFile.getDisplayName()}", 
                Toast.LENGTH_LONG
            ).show()
            
        } catch (e: Exception) {
            android.util.Log.e("FileShareManager", "Error showing share dialog", e)
            
            // Fallback: show simple success message with file location
            Toast.makeText(
                context,
                "Report saved to: ${file.absolutePath}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    /**
     * Builds a description for the report
     */
    private fun buildReportDescription(reportFile: ReportFile): String {
        return buildString {
            appendLine("AECI MMU Companion - ${reportFile.metadata["reportType"]}")
            appendLine("Generated: ${reportFile.metadata["generatedAt"]}")
            appendLine("Period: ${reportFile.metadata["dateRange"]}")
            
            if (reportFile.metadata["includeCharts"] == "true") {
                appendLine("• Charts and graphs included")
            }
            if (reportFile.metadata["includeRawData"] == "true") {
                appendLine("• Raw data tables included")
            }
            
            appendLine()
            appendLine("This report was generated by the AECI MMU Companion app for maintenance management and operational reporting.")
        }
    }
    
    // Mock content generators
    private fun generatePdfContent(reportType: String, dateRange: String, includeCharts: Boolean): ByteArray {
        // In a real implementation, this would use a PDF library like iText
        val content = """
            AECI MMU COMPANION REPORT
            ========================
            
            Report Type: $reportType
            Date Range: $dateRange
            Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}
            
            SUMMARY
            -------
            This is a mock PDF report generated by the AECI MMU Companion app.
            In a production environment, this would contain actual data from your operations.
            
            ${if (includeCharts) "Charts and visualizations would be embedded here.\n\n" else ""}
            
            For demonstration purposes, this report contains sample data:
            - Total forms processed: 245
            - Equipment inspections: 89
            - Maintenance tasks completed: 67
            - Safety incidents: 2 (resolved)
            - Overall efficiency: 94.2%
            
            END OF REPORT
        """.trimIndent()
        
        return content.toByteArray()
    }
    
    private fun generateExcelContent(reportType: String, dateRange: String, includeRawData: Boolean): ByteArray {
        // Mock Excel content (would use Apache POI in real implementation)
        val content = """
            AECI MMU Report,$reportType
            Date Range,$dateRange
            Generated,${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}
            
            Metric,Value,Unit
            Total Forms,245,count
            Completion Rate,94.2,%
            Equipment Uptime,98.7,%
            Safety Score,100,%
            
            ${if (includeRawData) "Raw Data Section:\nTimestamp,Equipment,Status,Operator\n2024-01-15 08:30,MMU-001,Operational,John Smith\n2024-01-15 09:45,MMU-002,Maintenance,Jane Doe\n" else ""}
        """.trimIndent()
        
        return content.toByteArray()
    }
    
    private fun generateCsvContent(reportType: String, dateRange: String): ByteArray {
        val content = """
            Report Type,Date Range,Generated
            $reportType,$dateRange,${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}
            
            Metric,Value,Unit
            Total Forms,245,count
            Completion Rate,94.2,%
            Equipment Uptime,98.7,%
            Safety Score,100,%
        """.trimIndent()
        
        return content.toByteArray()
    }
    
    private fun generateJsonContent(reportType: String, dateRange: String, includeRawData: Boolean): ByteArray {
        val content = """
            {
                "report": {
                    "type": "$reportType",
                    "dateRange": "$dateRange",
                    "generated": "${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}",
                    "summary": {
                        "totalForms": 245,
                        "completionRate": 94.2,
                        "equipmentUptime": 98.7,
                        "safetyScore": 100
                    }${if (includeRawData) """,
                    "rawData": [
                        {"timestamp": "2024-01-15 08:30", "equipment": "MMU-001", "status": "Operational", "operator": "John Smith"},
                        {"timestamp": "2024-01-15 09:45", "equipment": "MMU-002", "status": "Maintenance", "operator": "Jane Doe"}
                    ]""" else ""}
                }
            }
        """.trimIndent()
        
        return content.toByteArray()
    }
    
    private fun generateTextContent(reportType: String, dateRange: String): ByteArray {
        val content = """
            AECI MMU COMPANION REPORT
            ========================
            
            Report Type: $reportType
            Date Range: $dateRange
            Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}
            
            SUMMARY
            -------
            Total Forms: 245
            Completion Rate: 94.2%
            Equipment Uptime: 98.7%
            Safety Score: 100%
            
            This is a text-based report generated by the AECI MMU Companion app.
        """.trimIndent()
        
        return content.toByteArray()
    }
} 