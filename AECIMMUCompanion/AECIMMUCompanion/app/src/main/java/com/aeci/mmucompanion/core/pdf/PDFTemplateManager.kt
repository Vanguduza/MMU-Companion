package com.aeci.mmucompanion.core.pdf

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * PDF Template Manager
 * Handles storing and retrieving PDF templates programmatically
 */
class PDFTemplateManager(private val context: Context) {
    
    companion object {
        private const val TEMPLATES_DIR = "pdf_templates"
        
        // Template file names
        const val PUMP_INSPECTION_90DAY = "90_day_pump_system_inspection.pdf"
        const val AVAILABILITY_UTILIZATION = "availability_utilization.pdf"
        const val BLAST_HOLE_LOG = "blast_hole_log.pdf"
        const val BOWIE_PUMP_WEEKLY = "bowie_pump_weekly_checklist.pdf"
        const val FIRE_EXTINGUISHER = "fire_extinguisher_inspection.pdf"
        const val JOB_CARD = "job_card.pdf"
        const val MMU_CHASSIS_MAINTENANCE = "mmu_chassis_maintenance_record.pdf"
        const val MMU_HANDOVER_CERTIFICATE = "mmu_handover_certificate.pdf"
        const val MMU_PRODUCTION_DAILY_LOG = "mmu_production_daily_log.pdf"
        const val MMU_QUALITY_REPORT = "mmu_quality_report.pdf"
        const val MONTHLY_PROCESS_MAINTENANCE = "monthly_process_maintenance_record.pdf"
        const val ON_BENCH_MMU_INSPECTION = "on_bench_mmu_inspection.pdf"
        const val PC_PUMP_PRESSURE_TRIP_TEST = "pc_pump_high_low_pressure_trip_test.pdf"
        const val PRETASK_SAFETY_ASSESSMENT = "pretask_safety_assessment.pdf"
        
        // Template metadata
        val TEMPLATE_METADATA = mapOf(
            PUMP_INSPECTION_90DAY to TemplateMetadata(
                "90 Day Pump System Inspection Checklist",
                "Maintenance",
                "Quarterly inspection of pump systems",
                listOf("Millwright", "Technician")
            ),
            AVAILABILITY_UTILIZATION to TemplateMetadata(
                "Availability & Utilization Report",
                "Production",
                "Equipment availability and utilization tracking",
                listOf("Supervisor", "Admin")
            ),
            BLAST_HOLE_LOG to TemplateMetadata(
                "Blast Hole Log",
                "Production",
                "Blast hole drilling and logging",
                listOf("Drill Operator", "Blast Supervisor")
            ),
            BOWIE_PUMP_WEEKLY to TemplateMetadata(
                "Bowie Pump Weekly Checklist",
                "Maintenance",
                "Weekly inspection of Bowie pumps",
                listOf("Technician", "Operator")
            ),
            FIRE_EXTINGUISHER to TemplateMetadata(
                "Fire Extinguisher Inspection Checklist",
                "Safety",
                "Monthly fire extinguisher inspections",
                listOf("Safety Officer", "Technician")
            ),
            JOB_CARD to TemplateMetadata(
                "Job Card",
                "Maintenance",
                "Work order and task assignment",
                listOf("Supervisor", "Technician")
            ),
            MMU_CHASSIS_MAINTENANCE to TemplateMetadata(
                "MMU Chassis Maintenance Record",
                "Maintenance",
                "MMU chassis maintenance and service record",
                listOf("Millwright", "Technician")
            ),
            MMU_HANDOVER_CERTIFICATE to TemplateMetadata(
                "MMU Handover Certificate",
                "Operations",
                "Equipment handover documentation",
                listOf("Supervisor", "Operator")
            ),
            MMU_PRODUCTION_DAILY_LOG to TemplateMetadata(
                "MMU Production Daily Log",
                "Production",
                "Daily production logging and reporting",
                listOf("Operator", "Supervisor")
            ),
            MMU_QUALITY_REPORT to TemplateMetadata(
                "MMU Quality Report",
                "Quality",
                "Quality control and assurance reporting",
                listOf("Quality Controller", "Supervisor")
            ),
            MONTHLY_PROCESS_MAINTENANCE to TemplateMetadata(
                "Monthly Process Maintenance Record",
                "Maintenance",
                "Monthly maintenance of process equipment",
                listOf("Millwright", "Process Technician")
            ),
            ON_BENCH_MMU_INSPECTION to TemplateMetadata(
                "On Bench MMU Inspection",
                "Maintenance",
                "Bench inspection of MMU units",
                listOf("Technician", "Inspector")
            ),
            PC_PUMP_PRESSURE_TRIP_TEST to TemplateMetadata(
                "PC Pump High/Low Pressure Trip Test",
                "Maintenance",
                "Pressure trip testing for PC pumps",
                listOf("Technician", "Millwright")
            ),
            PRETASK_SAFETY_ASSESSMENT to TemplateMetadata(
                "Pre-Task Safety Assessment",
                "Safety",
                "Pre-task safety planning and risk assessment",
                listOf("All Users")
            )
        )
    }
    
    private val templatesDir: File by lazy {
        File(context.filesDir, TEMPLATES_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * Initialize PDF templates from assets
     */
    suspend fun initializeTemplates() {
        try {
            val assetManager = context.assets
            val templateFiles = assetManager.list("pdf_templates") ?: return
            
            templateFiles.forEach { fileName ->
                val templateFile = File(templatesDir, fileName)
                if (!templateFile.exists()) {
                    copyTemplateFromAssets(fileName)
                }
            }
        } catch (e: IOException) {
            throw TemplateException("Failed to initialize PDF templates", e)
        }
    }
    
    /**
     * Copy template from assets to internal storage
     */
    private fun copyTemplateFromAssets(fileName: String) {
        try {
            val inputStream: InputStream = context.assets.open("pdf_templates/$fileName")
            val outputFile = File(templatesDir, fileName)
            val outputStream = FileOutputStream(outputFile)
            
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: IOException) {
            throw TemplateException("Failed to copy template: $fileName", e)
        }
    }
    
    /**
     * Get template file
     */
    fun getTemplate(templateName: String): File {
        val templateFile = File(templatesDir, templateName)
        if (!templateFile.exists()) {
            throw TemplateException("Template not found: $templateName")
        }
        return templateFile
    }
    
    /**
     * Get template metadata
     */
    fun getTemplateMetadata(templateName: String): TemplateMetadata {
        return TEMPLATE_METADATA[templateName] 
            ?: throw TemplateException("Metadata not found for template: $templateName")
    }
    
    /**
     * Get all available templates
     */
    fun getAllTemplates(): List<TemplateInfo> {
        return TEMPLATE_METADATA.map { (fileName, metadata) ->
            TemplateInfo(
                fileName = fileName,
                filePath = File(templatesDir, fileName).absolutePath,
                metadata = metadata,
                exists = File(templatesDir, fileName).exists()
            )
        }
    }
    
    /**
     * Get templates by category
     */
    fun getTemplatesByCategory(category: String): List<TemplateInfo> {
        return getAllTemplates().filter { 
            it.metadata.category.equals(category, ignoreCase = true) 
        }
    }
    
    /**
     * Check if template exists
     */
    fun templateExists(templateName: String): Boolean {
        return File(templatesDir, templateName).exists()
    }
    
    /**
     * Get template file path
     */
    fun getTemplatePath(templateName: String): String {
        return File(templatesDir, templateName).absolutePath
    }
}

/**
 * Template metadata
 */
data class TemplateMetadata(
    val displayName: String,
    val category: String,
    val description: String,
    val allowedRoles: List<String>
)

/**
 * Template information
 */
data class TemplateInfo(
    val fileName: String,
    val filePath: String,
    val metadata: TemplateMetadata,
    val exists: Boolean
)

/**
 * PDF field coordinate mapping
 */
data class PDFFieldCoordinate(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val page: Int = 0
)

/**
 * PDF field definition
 */
data class PDFFieldDefinition(
    val fieldName: String,
    val fieldType: PDFFieldType,
    val coordinates: PDFFieldCoordinate,
    val required: Boolean = false,
    val validation: PdfValidationRule? = null,
    val autoPopulate: String? = null,
    val dependsOn: String? = null,
    val unit: String? = null
)

/**
 * PDF field types
 */
enum class PDFFieldType {
    TEXT,
    NUMBER,
    DATE,
    CHECKBOX,
    RADIO_GROUP,
    DROPDOWN,
    SIGNATURE,
    IMAGE,
    EQUIPMENT_ID
}

/**
 * Validation rules
 */
data class PdfValidationRule(
    val rule: String,
    val value: Any? = null,
    val message: String
)

/**
 * Template exception
 */
class TemplateException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Composition local for PDF Template Manager
 */
val LocalPDFTemplateManager = compositionLocalOf<PDFTemplateManager> {
    error("PDFTemplateManager not provided")
}
