package com.aeci.mmucompanion.domain.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.aeci.mmucompanion.R
import com.aeci.mmucompanion.domain.model.FormField
import com.aeci.mmucompanion.domain.model.FormFieldType
import com.aeci.mmucompanion.domain.model.FormSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PDFExportService @Inject constructor(
    private val context: Context
) {

    suspend fun exportFormToPDF(
        formId: String,
        formTitle: String,
        formSections: List<FormSection>,
        formData: Map<String, String>,
        outputPath: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(612, 792, 1).create() // Letter size
            val page = pdfDocument.startPage(pageInfo)
            
            val canvas = page.canvas
            val paint = Paint()
            paint.textSize = 12f
            paint.isAntiAlias = true
            
            // Draw AECI logo at top-right corner
            drawAECILogo(canvas, paint)
            
            // Draw form header with title and basic info
            drawFormHeader(canvas, paint, formTitle)
            
            // Draw form data based on field coordinates and form-specific layout
            drawFormDataWithCoordinates(canvas, paint, formId, formSections, formData)
            
            pdfDocument.finishPage(page)
            
            // Save to file
            val fileOutputStream = FileOutputStream(outputPath)
            pdfDocument.writeTo(fileOutputStream)
            fileOutputStream.close()
            pdfDocument.close()
            
            Result.success(outputPath)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generatePDF(
        formId: String,
        formSections: List<FormSection>,
        formData: Map<String, Any>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(612, 792, 1).create() // Letter size
            val page = pdfDocument.startPage(pageInfo)
            
            val canvas = page.canvas
            val paint = Paint()
            paint.textSize = 12f
            paint.isAntiAlias = true
            
            // Convert formData to Map<String, String>
            val stringFormData = formData.mapValues { it.value.toString() }
            
            // Draw form data based on field coordinates and form-specific layout
            drawFormDataWithCoordinates(canvas, paint, formId, formSections, stringFormData)
            
            pdfDocument.finishPage(page)
            
            // Save to file in app's external files directory
            val fileName = "${formId}_${System.currentTimeMillis()}.pdf"
            val outputFile = java.io.File(context.getExternalFilesDir(null), fileName)
            val fileOutputStream = java.io.FileOutputStream(outputFile)
            pdfDocument.writeTo(fileOutputStream)
            fileOutputStream.close()
            pdfDocument.close()
            
            Result.success(outputFile.absolutePath)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun drawFormDataWithCoordinates(
        canvas: Canvas,
        paint: Paint,
        formType: String,
        sections: List<FormSection>,
        formData: Map<String, String>
    ) {
        // Draw AECI logo first
        drawAECILogo(canvas)
        
        // Draw form header
        drawFormHeader(canvas, paint, formType)
        
        // Use form-specific layout if available
        when (formType.lowercase()) {
            "pump_inspection" -> drawPumpInspectionLayout(canvas, paint, formData)
            "job_card" -> drawJobCardLayout(canvas, paint, formData)
            "mmu_daily_log" -> drawMMUDailyLogLayout(canvas, paint, formData)
            "handover_certificate" -> drawHandoverCertificateLayout(canvas, paint, formData)
            "chassis_maintenance" -> drawChassisMaintenanceLayout(canvas, paint, formData)
            "quality_report" -> drawQualityReportLayout(canvas, paint, formData)
            "production_log" -> drawProductionLogLayout(canvas, paint, formData)
            "monthly_maintenance" -> drawMonthlyMaintenanceLayout(canvas, paint, formData)
            "bench_inspection" -> drawBenchInspectionLayout(canvas, paint, formData)
            "pressure_trip_test" -> drawPressureTripTestLayout(canvas, paint, formData)
            "pretask_assessment" -> drawPretaskAssessmentLayout(canvas, paint, formData)
            "fire_extinguisher" -> drawFireExtinguisherLayout(canvas, paint, formData)
            "90_day_pump" -> draw90DayPumpLayout(canvas, paint, formData)
            "timesheet" -> drawTimesheetLayout(canvas, paint, formData)
            "uor" -> drawUORLayout(canvas, paint, formData)
            "blast_hole_log" -> drawBlastHoleLogLayout(canvas, paint, formData)
            else -> {
                // Fallback to coordinate-based rendering
                drawWithCoordinates(canvas, paint, sections, formData)
            }
        }
    }

    private fun drawWithCoordinates(
        canvas: Canvas,
        paint: Paint,
        sections: List<FormSection>,
        formData: Map<String, String>
    ) {
        sections.forEach { section ->
            section.fields.forEach { field ->
                val value = formData[field.fieldName] ?: ""
                
                // Use precise coordinates if available
                if (field.x > 0 && field.y > 0) {
                    drawFieldAtCoordinates(canvas, paint, field, value)
                }
            }
        }
    }

    private fun drawFieldAtCoordinates(
        canvas: Canvas,
        paint: Paint,
        field: FormField,
        value: String
    ) {
        when (field.fieldType) {
            FormFieldType.TEXT,
            FormFieldType.MULTILINE_TEXT,
            FormFieldType.NUMBER,
            FormFieldType.INTEGER,
            FormFieldType.DATE,
            FormFieldType.TIME,
            FormFieldType.EQUIPMENT_ID,
            FormFieldType.SITE_CODE,
            FormFieldType.EMPLOYEE_ID -> {
                drawTextValue(canvas, paint, field.label, value, field.x, field.y)
            }
            
            FormFieldType.CHECKBOX -> {
                drawCheckboxValue(canvas, paint, field.label, value, field.x, field.y)
            }
            
            FormFieldType.DROPDOWN -> {
                drawDropdownValue(canvas, paint, field.label, value, field.x, field.y)
            }
            
            FormFieldType.SIGNATURE -> {
                drawSignaturePlaceholder(canvas, paint, field.label, field.x, field.y)
            }
            
            FormFieldType.PHOTO -> {
                drawPhotoPlaceholder(canvas, paint, field.label, field.x, field.y)
            }
            
            else -> {
                drawTextValue(canvas, paint, field.label, value, field.x, field.y)
            }
        }
    }

    private fun drawTextValue(
        canvas: Canvas,
        paint: Paint,
        label: String,
        value: String,
        x: Float,
        y: Float
    ) {
        // Draw label
        paint.isFakeBoldText = true
        canvas.drawText("$label:", x, y, paint)
        
        // Draw value
        paint.isFakeBoldText = false
        val labelWidth = paint.measureText("$label: ")
        canvas.drawText(value, x + labelWidth, y, paint)
    }

    private fun drawCheckboxValue(
        canvas: Canvas,
        paint: Paint,
        label: String,
        value: String,
        x: Float,
        y: Float
    ) {
        // Draw checkbox
        val checkboxSize = 15f
        paint.style = Paint.Style.STROKE
        canvas.drawRect(x, y - checkboxSize, x + checkboxSize, y, paint)
        
        // Draw check mark if checked
        if (value.toBoolean()) {
            paint.style = Paint.Style.FILL
            canvas.drawLine(x + 3f, y - 8f, x + 7f, y - 4f, paint)
            canvas.drawLine(x + 7f, y - 4f, x + 12f, y - 12f, paint)
        }
        
        // Draw label
        paint.style = Paint.Style.FILL
        canvas.drawText(label, x + checkboxSize + 5f, y - 3f, paint)
    }

    private fun drawDropdownValue(
        canvas: Canvas,
        paint: Paint,
        label: String,
        value: String,
        x: Float,
        y: Float
    ) {
        drawTextValue(canvas, paint, label, value, x, y)
    }

    private fun drawSignaturePlaceholder(
        canvas: Canvas,
        paint: Paint,
        label: String,
        x: Float,
        y: Float
    ) {
        // Draw signature line
        paint.style = Paint.Style.STROKE
        canvas.drawLine(x, y + 5f, x + 150f, y + 5f, paint)
        
        // Draw label
        paint.style = Paint.Style.FILL
        canvas.drawText(label, x, y - 5f, paint)
    }

    private fun drawPhotoPlaceholder(
        canvas: Canvas,
        paint: Paint,
        label: String,
        x: Float,
        y: Float
    ) {
        // Draw photo placeholder box
        paint.style = Paint.Style.STROKE
        canvas.drawRect(x, y - 30f, x + 100f, y + 30f, paint)
        
        // Draw label
        paint.style = Paint.Style.FILL
        canvas.drawText("$label: [Photo]", x, y - 35f, paint)
    }

    suspend fun getFormCoordinates(formId: String): Map<String, FormField> = withContext(Dispatchers.IO) {
        // Return coordinate mappings for the specific form
        // This would ideally come from a database or configuration file
        when (formId) {
            "pump_inspection_90day" -> getPumpInspectionCoordinates()
            "availability_utilization" -> getAvailabilityUtilizationCoordinates()
            "blast_hole_log" -> getBlastHoleLogCoordinates()
            "bowie_weekly" -> getBowieWeeklyCoordinates()
            "fire_extinguisher" -> getFireExtinguisherCoordinates()
            "job_card" -> getJobCardCoordinates()
            "chassis_maintenance" -> getChassisMaintenanceCoordinates()
            "handover_certificate" -> getHandoverCertificateCoordinates()
            "production_log" -> getProductionLogCoordinates()
            "quality_report" -> getQualityReportCoordinates()
            "monthly_maintenance" -> getMonthlyMaintenanceCoordinates()
            "bench_inspection" -> getBenchInspectionCoordinates()
            "pressure_trip_test" -> getPressureTripTestCoordinates()
            "pretask_assessment" -> getPretaskAssessmentCoordinates()
            else -> emptyMap()
        }
    }

    private fun getPumpInspectionCoordinates(): Map<String, FormField> {
        // Based on the PDF coordinate maps from the markdown file
        return mapOf(
            "inspection_date" to FormField(
                fieldName = "inspection_date",
                label = "Inspection Date",
                fieldType = FormFieldType.DATE,
                x = 450f, y = 85f, width = 120f, height = 25f
            ),
            "inspector_name" to FormField(
                fieldName = "inspector_name",
                label = "Inspector Name",
                fieldType = FormFieldType.TEXT,
                x = 150f, y = 110f, width = 200f, height = 25f
            ),
            "equipment_id" to FormField(
                fieldName = "equipment_id",
                label = "Equipment ID",
                fieldType = FormFieldType.EQUIPMENT_ID,
                x = 450f, y = 110f, width = 150f, height = 25f
            ),
            "serial_number" to FormField(
                fieldName = "serial_number",
                label = "Serial Number",
                fieldType = FormFieldType.TEXT,
                x = 150f, y = 135f, width = 200f, height = 25f
            ),
            "pump_location" to FormField(
                fieldName = "pump_location",
                label = "Pump Location",
                fieldType = FormFieldType.TEXT,
                x = 450f, y = 135f, width = 150f, height = 25f
            ),
            "service_hours" to FormField(
                fieldName = "service_hours",
                label = "Service Hours",
                fieldType = FormFieldType.NUMBER,
                x = 150f, y = 160f, width = 100f, height = 25f
            )
        )
    }

    // Add other coordinate mapping functions as needed
    private fun getAvailabilityUtilizationCoordinates(): Map<String, FormField> = emptyMap()
    private fun getBlastHoleLogCoordinates(): Map<String, FormField> = emptyMap()
    private fun getBowieWeeklyCoordinates(): Map<String, FormField> = emptyMap()
    private fun getFireExtinguisherCoordinates(): Map<String, FormField> = emptyMap()
    private fun getMMUDailyLogCoordinates(): Map<String, FormField> = mapOf(
        "log_date" to FormField(
            fieldName = "log_date",
            label = "Date",
            fieldType = FormFieldType.DATE,
            x = 150f, y = 120f, width = 150f, height = 25f
        ),
        "shift" to FormField(
            fieldName = "shift",
            label = "Shift",
            fieldType = FormFieldType.DROPDOWN,
            x = 350f, y = 120f, width = 100f, height = 25f
        ),
        "operator_name" to FormField(
            fieldName = "operator_name",
            label = "Operator",
            fieldType = FormFieldType.TEXT,
            x = 150f, y = 150f, width = 200f, height = 25f
        )
    )
    private fun getJobCardCoordinates(): Map<String, FormField> = mapOf(
        "job_number" to FormField(
            fieldName = "job_number",
            label = "Job Number",
            fieldType = FormFieldType.TEXT,
            x = 150f, y = 120f, width = 200f, height = 25f
        ),
        "description" to FormField(
            fieldName = "description",
            label = "Job Description",
            fieldType = FormFieldType.MULTILINE_TEXT,
            x = 150f, y = 150f, width = 400f, height = 60f
        ),
        "assigned_to" to FormField(
            fieldName = "assigned_to",
            label = "Assigned To",
            fieldType = FormFieldType.TEXT,
            x = 150f, y = 230f, width = 200f, height = 25f
        )
    )
    private fun getChassisMaintenanceCoordinates(): Map<String, FormField> = mapOf(
        "maintenance_date" to FormField(
            fieldName = "maintenance_date",
            label = "Maintenance Date",
            fieldType = FormFieldType.DATE,
            x = 150f, y = 120f, width = 150f, height = 25f
        ),
        "chassis_id" to FormField(
            fieldName = "chassis_id",
            label = "Chassis ID",
            fieldType = FormFieldType.TEXT,
            x = 350f, y = 120f, width = 150f, height = 25f
        ),
        "maintenance_type" to FormField(
            fieldName = "maintenance_type",
            label = "Maintenance Type",
            fieldType = FormFieldType.DROPDOWN,
            x = 150f, y = 150f, width = 200f, height = 25f
        )
    )

    private fun getHandoverCertificateCoordinates(): Map<String, FormField> = emptyMap()
    private fun getProductionLogCoordinates(): Map<String, FormField> = emptyMap()
    private fun getQualityReportCoordinates(): Map<String, FormField> = emptyMap()
    private fun getMonthlyMaintenanceCoordinates(): Map<String, FormField> = emptyMap()
    private fun getBenchInspectionCoordinates(): Map<String, FormField> = emptyMap()
    private fun getPressureTripTestCoordinates(): Map<String, FormField> = emptyMap()
    private fun getPretaskAssessmentCoordinates(): Map<String, FormField> = emptyMap()

    // Form-specific layout methods for pixel-perfect PDF generation
    
    private fun drawPumpInspectionLayout(canvas: Canvas, paint: Paint, formData: Map<String, String>) {
        val coordinates = getPumpInspectionCoordinates()
        coordinates.forEach { (fieldName, field) ->
            val value = formData[fieldName] ?: ""
            drawFieldAtCoordinates(canvas, paint, field, value)
        }
    }

    private fun drawJobCardLayout(canvas: Canvas, paint: Paint, formData: Map<String, String>) {
        val coordinates = getJobCardCoordinates()
        coordinates.forEach { (fieldName, field) ->
            val value = formData[fieldName] ?: ""
            drawFieldAtCoordinates(canvas, paint, field, value)
        }
    }

    private fun drawMMUDailyLogLayout(canvas: Canvas, paint: Paint, formData: Map<String, String>) {
        val coordinates = getMMUDailyLogCoordinates()
        coordinates.forEach { (fieldName, field) ->
            val value = formData[fieldName] ?: ""
            drawFieldAtCoordinates(canvas, paint, field, value)
        }
    }

    private fun drawHandoverCertificateLayout(canvas: Canvas, paint: Paint, formData: Map<String, String>) {
        val coordinates = getHandoverCertificateCoordinates()
        coordinates.forEach { (fieldName, field) ->
            val value = formData[fieldName] ?: ""
            drawFieldAtCoordinates(canvas, paint, field, value)
        }
    }

    private fun drawChassisMaintenanceLayout(canvas: Canvas, paint: Paint, formData: Map<String, String>) {
        val coordinates = getChassisMaintenanceCoordinates()
        coordinates.forEach { (fieldName, field) ->
            val value = formData[fieldName] ?: ""
            drawFieldAtCoordinates(canvas, paint, field, value)
        }
    }

    private fun drawQualityReportLayout(canvas: Canvas, paint: Paint, formData: Map<String, String>) {
        val coordinates = getQualityReportCoordinates()
        coordinates.forEach { (fieldName, field) ->
            val value = formData[fieldName] ?: ""
            drawFieldAtCoordinates(canvas, paint, field, value)
        }
    }

    private fun drawProductionLogLayout(canvas: Canvas, paint: Paint, formData: Map<String, String>) {
        val coordinates = getProductionLogCoordinates()
        coordinates.forEach { (fieldName, field) ->
            val value = formData[fieldName] ?: ""
            drawFieldAtCoordinates(canvas, paint, field, value)
        }
    }

    private fun drawMonthlyMaintenanceLayout(canvas: Canvas, paint: Paint, formData: Map<String, String>) {
        val coordinates = getMonthlyMaintenanceCoordinates()
        coordinates.forEach { (fieldName, field) ->
            val value = formData[fieldName] ?: ""
            drawFieldAtCoordinates(canvas, paint, field, value)
        }
    }

    private fun drawBenchInspectionLayout(canvas: Canvas, paint: Paint, formData: Map<String, String>) {
        val coordinates = getBenchInspectionCoordinates()
        coordinates.forEach { (fieldName, field) ->
            val value = formData[fieldName] ?: ""
            drawFieldAtCoordinates(canvas, paint, field, value)
        }
    }

    private fun drawPressureTripTestLayout(canvas: Canvas, paint: Paint, formData: Map<String, String>) {
        val coordinates = getPressureTripTestCoordinates()
        coordinates.forEach { (fieldName, field) ->
            val value = formData[fieldName] ?: ""
            drawFieldAtCoordinates(canvas, paint, field, value)
        }
    }

    private fun drawPretaskAssessmentLayout(canvas: Canvas, paint: Paint, formData: Map<String, String>) {
        val coordinates = getPretaskAssessmentCoordinates()
        coordinates.forEach { (fieldName, field) ->
            val value = formData[fieldName] ?: ""
            drawFieldAtCoordinates(canvas, paint, field, value)
        }
    }

    private fun drawFireExtinguisherLayout(canvas: Canvas, paint: Paint, formData: Map<String, String>) {
        // Fire extinguisher inspection specific layout
        drawFormTitle(canvas, paint, "FIRE EXTINGUISHER INSPECTION CHECKLIST", 100f, 80f)
        
        // Draw fire extinguisher specific fields
        val fields = mapOf(
            "date" to Pair(150f, 120f),
            "inspector_name" to Pair(150f, 150f),
            "extinguisher_id" to Pair(150f, 180f),
            "location" to Pair(150f, 210f),
            "condition" to Pair(150f, 240f)
        )
        
        fields.forEach { (fieldName, position) ->
            val value = formData[fieldName] ?: ""
            canvas.drawText("$fieldName: $value", position.first, position.second, paint)
        }
    }

    private fun draw90DayPumpLayout(canvas: Canvas, paint: Paint, formData: Map<String, String>) {
        // 90 Day pump system inspection specific layout
        drawFormTitle(canvas, paint, "90 DAY PUMP SYSTEM INSPECTION CHECKLIST", 100f, 80f)
        
        val fields = mapOf(
            "inspection_date" to Pair(150f, 120f),
            "pump_id" to Pair(150f, 150f),
            "inspector" to Pair(150f, 180f),
            "system_pressure" to Pair(150f, 210f),
            "condition_status" to Pair(150f, 240f)
        )
        
        fields.forEach { (fieldName, position) ->
            val value = formData[fieldName] ?: ""
            canvas.drawText("$fieldName: $value", position.first, position.second, paint)
        }
    }

    private fun drawTimesheetLayout(canvas: Canvas, paint: Paint, formData: Map<String, String>) {
        // Timesheet specific layout
        drawFormTitle(canvas, paint, "TIMESHEET", 100f, 80f)
        
        val fields = mapOf(
            "employee_name" to Pair(150f, 120f),
            "employee_id" to Pair(150f, 150f),
            "period_start" to Pair(150f, 180f),
            "period_end" to Pair(150f, 210f),
            "total_hours" to Pair(150f, 240f)
        )
        
        fields.forEach { (fieldName, position) ->
            val value = formData[fieldName] ?: ""
            canvas.drawText("$fieldName: $value", position.first, position.second, paint)
        }
    }

    private fun drawUORLayout(canvas: Canvas, paint: Paint, formData: Map<String, String>) {
        // UOR (Unit Operation Report) specific layout
        drawFormTitle(canvas, paint, "UNIT OPERATION REPORT", 100f, 80f)
        
        val fields = mapOf(
            "report_date" to Pair(150f, 120f),
            "unit_id" to Pair(150f, 150f),
            "operator" to Pair(150f, 180f),
            "operation_type" to Pair(150f, 210f),
            "status" to Pair(150f, 240f)
        )
        
        fields.forEach { (fieldName, position) ->
            val value = formData[fieldName] ?: ""
            canvas.drawText("$fieldName: $value", position.first, position.second, paint)
        }
    }

    private fun drawBlastHoleLogLayout(canvas: Canvas, paint: Paint, formData: Map<String, String>) {
        // Blast hole log specific layout
        drawFormTitle(canvas, paint, "BLAST HOLE LOG", 100f, 80f)
        
        val fields = mapOf(
            "hole_number" to Pair(150f, 120f),
            "depth" to Pair(150f, 150f),
            "diameter" to Pair(150f, 180f),
            "explosive_type" to Pair(150f, 210f),
            "charge_weight" to Pair(150f, 240f)
        )
        
        fields.forEach { (fieldName, position) ->
            val value = formData[fieldName] ?: ""
            canvas.drawText("$fieldName: $value", position.first, position.second, paint)
        }
    }

    private fun drawFormTitle(canvas: Canvas, paint: Paint, title: String, x: Float, y: Float) {
        val originalSize = paint.textSize
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText(title, x, y, paint)
        paint.textSize = originalSize
        paint.isFakeBoldText = false
    }
    
    private fun drawAECILogo(canvas: Canvas, paint: Paint? = null) {
        // Draw AECI logo at top of page
        val logoPaint = paint ?: Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 24f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        canvas.drawText("AECI", 50f, 50f, logoPaint)
    }
    
    private fun drawAECILogo(canvas: Canvas) {
        drawAECILogo(canvas, null)
    }
    
    private fun drawFormHeader(canvas: Canvas, paint: Paint, formTitle: String) {
        val headerPaint = Paint(paint).apply {
            textSize = 18f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        canvas.drawText(formTitle, 200f, 50f, headerPaint)
    }
} 