package com.aeci.mmucompanion.presentation.component

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.aeci.mmucompanion.domain.model.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.time.format.DateTimeFormatter
import kotlin.math.max

class PDFTemplateManager(private val context: Context) {
    
    private val templates = mutableMapOf<FormType, PDFTemplate>()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    init {
        loadTemplates()
    }
    
    private fun loadTemplates() {
        templates[FormType.MAINTENANCE] = createMaintenanceTemplate()
        templates[FormType.INSPECTION] = createInspectionTemplate()
        templates[FormType.SAFETY] = createSafetyTemplate()
        templates[FormType.INCIDENT] = createIncidentTemplate()
        templates[FormType.EQUIPMENT_CHECK] = createEquipmentCheckTemplate()
        templates[FormType.WORK_ORDER] = createWorkOrderTemplate()
    }
    
    fun generatePDF(formData: FormData): ByteArray {
        val template = templates[formData.formType] 
            ?: throw IllegalArgumentException("No template found for ${formData.formType}")
            
        val document = PdfDocument()
        val pages = mutableListOf<PdfDocument.Page>()
        
        try {
            // Create pages based on template
            template.pages.forEachIndexed { index, pageTemplate ->
                val pageInfo = PdfDocument.PageInfo.Builder(
                    pageTemplate.width, 
                    pageTemplate.height, 
                    index + 1
                ).create()
                
                val page = document.startPage(pageInfo)
                val canvas = page.canvas
                
                // Draw template background
                drawTemplateBackground(canvas, pageTemplate)
                
                // Fill in form data
                fillFormData(canvas, pageTemplate, formData)
                
                document.finishPage(page)
                pages.add(page)
            }
            
            // Write to byte array
            val outputStream = ByteArrayOutputStream()
            document.writeTo(outputStream)
            return outputStream.toByteArray()
            
        } catch (e: Exception) {
            throw IOException("Failed to generate PDF: ${e.message}", e)
        } finally {
            document.close()
        }
    }
    
    private fun drawTemplateBackground(canvas: Canvas, pageTemplate: PDFPageTemplate) {
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.DEFAULT
        }
        
        // Draw header
        pageTemplate.header?.let { header ->
            drawHeader(canvas, header, paint)
        }
        
        // Draw form fields
        pageTemplate.fields.forEach { field ->
            drawField(canvas, field, paint)
        }
        
        // Draw footer
        pageTemplate.footer?.let { footer ->
            drawFooter(canvas, footer, paint)
        }
    }
    
    private fun drawHeader(canvas: Canvas, header: PDFHeader, paint: Paint) {
        // Draw AECI logo placeholder
        val logoPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.FILL
        }
        canvas.drawRect(
            header.logoPosition.x,
            header.logoPosition.y,
            header.logoPosition.x + header.logoSize.width,
            header.logoPosition.y + header.logoSize.height,
            logoPaint
        )
        
        // Draw title
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(
            header.title,
            header.titlePosition.x,
            header.titlePosition.y,
            titlePaint
        )
        
        // Draw subtitle
        val subtitlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            typeface = Typeface.DEFAULT
        }
        canvas.drawText(
            header.subtitle,
            header.subtitlePosition.x,
            header.subtitlePosition.y,
            subtitlePaint
        )
    }
    
    private fun drawField(canvas: Canvas, field: PDFField, paint: Paint) {
        // Draw field label
        val labelPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.DEFAULT
        }
        canvas.drawText(
            field.label,
            field.labelPosition.x,
            field.labelPosition.y,
            labelPaint
        )
        
        // Draw field border
        val borderPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(
            field.valuePosition.x,
            field.valuePosition.y,
            field.valuePosition.x + field.width,
            field.valuePosition.y + field.height,
            borderPaint
        )
    }
    
    private fun drawFooter(canvas: Canvas, footer: PDFFooter, paint: Paint) {
        // Draw signature fields
        footer.signatureFields.forEach { signatureField ->
            drawSignatureField(canvas, signatureField, paint)
        }
        
        // Draw footer text
        val footerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 8f
            typeface = Typeface.DEFAULT
        }
        canvas.drawText(
            footer.footerText,
            footer.footerPosition.x,
            footer.footerPosition.y,
            footerPaint
        )
    }
    
    private fun drawSignatureField(canvas: Canvas, signatureField: PDFSignatureField, paint: Paint) {
        // Draw signature line
        val linePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1f
        }
        canvas.drawLine(
            signatureField.position.x,
            signatureField.position.y + signatureField.height,
            signatureField.position.x + signatureField.width,
            signatureField.position.y + signatureField.height,
            linePaint
        )
        
        // Draw signature label
        val labelPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
        }
        canvas.drawText(
            signatureField.label,
            signatureField.position.x,
            signatureField.position.y + signatureField.height + 20,
            labelPaint
        )
    }
    
    private fun fillFormData(canvas: Canvas, pageTemplate: PDFPageTemplate, formData: FormData) {
        val valuePaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.DEFAULT
        }
        
        pageTemplate.fields.forEach { field ->
            val value = getFieldValue(field.fieldName, formData)
            if (value.isNotEmpty()) {
                when (field.fieldType) {
                    PDFFieldType.TEXT -> {
                        canvas.drawText(
                            value,
                            field.valuePosition.x + 5,
                            field.valuePosition.y + 15,
                            valuePaint
                        )
                    }
                    PDFFieldType.NUMBER -> {
                        canvas.drawText(
                            value,
                            field.valuePosition.x + 5,
                            field.valuePosition.y + 15,
                            valuePaint
                        )
                    }
                    PDFFieldType.DATE -> {
                        canvas.drawText(
                            value,
                            field.valuePosition.x + 5,
                            field.valuePosition.y + 15,
                            valuePaint
                        )
                    }
                    PDFFieldType.BOOLEAN -> {
                        if (value.toBoolean()) {
                            drawCheckmark(canvas, field.valuePosition, field.height)
                        }
                    }
                    PDFFieldType.TABLE -> {
                        drawTable(canvas, field, value, valuePaint)
                    }
                    else -> {
                        canvas.drawText(
                            value,
                            field.valuePosition.x + 5,
                            field.valuePosition.y + 15,
                            valuePaint
                        )
                    }
                }
            }
        }
    }
    
    private fun drawCheckmark(canvas: Canvas, position: PDFPoint, height: Float) {
        val checkPaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        
        val checkSize = height * 0.6f
        val centerX = position.x + checkSize / 2
        val centerY = position.y + height / 2
        
        // Draw checkmark
        canvas.drawLine(
            centerX - checkSize / 3,
            centerY,
            centerX - checkSize / 6,
            centerY + checkSize / 4,
            checkPaint
        )
        canvas.drawLine(
            centerX - checkSize / 6,
            centerY + checkSize / 4,
            centerX + checkSize / 3,
            centerY - checkSize / 4,
            checkPaint
        )
    }
    
    private fun drawTable(canvas: Canvas, field: PDFField, data: String, paint: Paint) {
        // Parse table data (assuming JSON format)
        // This is a simplified implementation
        val lines = data.split("\n")
        val lineHeight = 20f
        
        lines.forEachIndexed { index, line ->
            canvas.drawText(
                line,
                field.valuePosition.x + 5,
                field.valuePosition.y + 15 + (index * lineHeight),
                paint
            )
        }
    }
    
    private fun getFieldValue(fieldName: String, formData: FormData): String {
        return when (formData) {
            is MaintenanceReportForm -> getMaintenanceFieldValue(fieldName, formData)
            is InspectionReportForm -> getInspectionFieldValue(fieldName, formData)
            is SafetyReportForm -> getSafetyFieldValue(fieldName, formData)
            else -> ""
        }
    }
    
    private fun getMaintenanceFieldValue(fieldName: String, form: MaintenanceReportForm): String {
        return when (fieldName) {
            "reportNumber" -> form.reportNumber
            "equipmentName" -> form.equipmentName
            "equipmentModel" -> form.equipmentModel
            "equipmentSerial" -> form.equipmentSerial
            "equipmentLocation" -> form.equipmentLocation
            "equipmentHours" -> form.equipmentHours?.toString() ?: ""
            "maintenanceType" -> form.maintenanceType.name
            "workDescription" -> form.workDescription
            "laborHours" -> form.laborHours.toString()
            "maintenanceDate" -> form.maintenanceDate.format(dateFormatter)
            "completionDate" -> form.completionDate?.format(dateFormatter) ?: ""
            "nextMaintenanceDate" -> form.nextMaintenanceDate?.format(dateFormatter) ?: ""
            "technicianName" -> form.technicianName
            "technicianId" -> form.technicianId
            "supervisorName" -> form.supervisorName ?: ""
            "supervisorApproval" -> form.supervisorApproval.toString()
            "preMaintenanceCondition" -> form.preMaintenanceCondition.name
            "postMaintenanceCondition" -> form.postMaintenanceCondition.name
            "partsUsed" -> form.partsUsed.joinToString("\n") { 
                "${it.partName} (${it.partNumber}) - Qty: ${it.quantity}"
            }
            "issuesFound" -> form.issuesFound.joinToString("\n")
            "recommendations" -> form.recommendations ?: ""
            "notes" -> form.notes ?: ""
            "siteLocation" -> form.siteLocation
            "createdBy" -> form.createdBy
            "createdAt" -> form.createdAt.format(dateFormatter)
            else -> ""
        }
    }
    
    private fun getInspectionFieldValue(fieldName: String, form: InspectionReportForm): String {
        return when (fieldName) {
            "reportNumber" -> form.reportNumber
            "equipmentName" -> form.equipmentName
            "equipmentModel" -> form.equipmentModel
            "equipmentSerial" -> form.equipmentSerial
            "equipmentLocation" -> form.equipmentLocation
            "inspectionType" -> form.inspectionType.name
            "inspectionDate" -> form.inspectionDate.format(dateFormatter)
            "inspectorName" -> form.inspectorName
            "inspectorId" -> form.inspectorId
            "inspectionFrequency" -> form.inspectionFrequency
            "lastInspectionDate" -> form.lastInspectionDate?.format(dateFormatter) ?: ""
            "nextInspectionDate" -> form.nextInspectionDate.format(dateFormatter)
            "overallCondition" -> form.overallCondition.name
            "operationalStatus" -> form.operationalStatus.name
            "complianceStatus" -> form.complianceStatus.name
            "certificationRequired" -> form.certificationRequired.toString()
            "inspectionItems" -> form.inspectionItems.joinToString("\n") { 
                "${it.itemName}: ${it.condition.name}"
            }
            "deficienciesFound" -> form.deficienciesFound.joinToString("\n") { 
                "${it.description} (${it.severity.name})"
            }
            "correctiveActions" -> form.correctiveActions.joinToString("\n") { 
                "${it.description} - Due: ${it.dueDate.format(dateFormatter)}"
            }
            "recommendations" -> form.recommendations ?: ""
            "notes" -> form.notes ?: ""
            "siteLocation" -> form.siteLocation
            "createdBy" -> form.createdBy
            "createdAt" -> form.createdAt.format(dateFormatter)
            else -> ""
        }
    }
    
    private fun getSafetyFieldValue(fieldName: String, form: SafetyReportForm): String {
        return when (fieldName) {
            "reportNumber" -> form.reportNumber
            "incidentDate" -> form.incidentDate.format(dateFormatter)
            "incidentTime" -> form.incidentTime
            "incidentLocation" -> form.incidentLocation
            "incidentType" -> form.incidentType.name
            "severityLevel" -> form.severityLevel.name
            "reportedBy" -> form.reportedBy
            "reporterId" -> form.reporterId
            "witnessesPresent" -> form.witnessesPresent.toString()
            "witnesses" -> form.witnesses.joinToString("\n") { 
                "${it.name} (${it.id})"
            }
            "injuredPersons" -> form.injuredPersons.joinToString("\n") { 
                "${it.name}: ${it.injuryType}"
            }
            "incidentDescription" -> form.incidentDescription
            "immediateActions" -> form.immediateActions
            "rootCause" -> form.rootCause ?: ""
            "contributingFactors" -> form.contributingFactors.joinToString("\n")
            "equipmentInvolved" -> form.equipmentInvolved.joinToString(", ")
            "environmentalConditions" -> form.environmentalConditions
            "ppeUsed" -> form.ppeUsed.joinToString(", ")
            "safetyProceduresFollowed" -> form.safetyProceduresFollowed.toString()
            "investigationRequired" -> form.investigationRequired.toString()
            "investigatorAssigned" -> form.investigatorAssigned ?: ""
            "investigationFindings" -> form.investigationFindings ?: ""
            "correctiveActions" -> form.correctiveActions.joinToString("\n") { 
                "${it.description} - Due: ${it.dueDate.format(dateFormatter)}"
            }
            "preventiveMeasures" -> form.preventiveMeasures.joinToString("\n")
            "regulatoryNotification" -> form.regulatoryNotification.toString()
            "regulatoryBody" -> form.regulatoryBody ?: ""
            "notes" -> form.notes ?: ""
            "siteLocation" -> form.siteLocation
            "createdBy" -> form.createdBy
            "createdAt" -> form.createdAt.format(dateFormatter)
            else -> ""
        }
    }
    
    // Template creation methods
    private fun createMaintenanceTemplate(): PDFTemplate {
        val page1 = PDFPageTemplate(
            width = 595, // A4 width in points
            height = 842, // A4 height in points
            header = PDFHeader(
                title = "MAINTENANCE REPORT",
                subtitle = "AECI Mobile Manufacturing Unit",
                logoPosition = PDFPoint(50f, 50f),
                logoSize = PDFSize(80f, 60f),
                titlePosition = PDFPoint(200f, 70f),
                subtitlePosition = PDFPoint(200f, 90f)
            ),
            fields = listOf(
                PDFField("reportNumber", "Report Number:", PDFPoint(50f, 120f), PDFPoint(150f, 120f), 200f, 25f, PDFFieldType.TEXT),
                PDFField("equipmentName", "Equipment Name:", PDFPoint(50f, 150f), PDFPoint(150f, 150f), 200f, 25f, PDFFieldType.TEXT),
                PDFField("equipmentModel", "Model:", PDFPoint(350f, 150f), PDFPoint(400f, 150f), 150f, 25f, PDFFieldType.TEXT),
                PDFField("equipmentSerial", "Serial Number:", PDFPoint(50f, 180f), PDFPoint(150f, 180f), 200f, 25f, PDFFieldType.TEXT),
                PDFField("equipmentLocation", "Location:", PDFPoint(350f, 180f), PDFPoint(400f, 180f), 150f, 25f, PDFFieldType.TEXT),
                PDFField("equipmentHours", "Operating Hours:", PDFPoint(50f, 210f), PDFPoint(150f, 210f), 100f, 25f, PDFFieldType.NUMBER),
                PDFField("maintenanceType", "Maintenance Type:", PDFPoint(350f, 210f), PDFPoint(450f, 210f), 100f, 25f, PDFFieldType.TEXT),
                PDFField("maintenanceDate", "Maintenance Date:", PDFPoint(50f, 240f), PDFPoint(150f, 240f), 100f, 25f, PDFFieldType.DATE),
                PDFField("completionDate", "Completion Date:", PDFPoint(350f, 240f), PDFPoint(450f, 240f), 100f, 25f, PDFFieldType.DATE),
                PDFField("technicianName", "Technician:", PDFPoint(50f, 270f), PDFPoint(150f, 270f), 200f, 25f, PDFFieldType.TEXT),
                PDFField("technicianId", "Technician ID:", PDFPoint(350f, 270f), PDFPoint(450f, 270f), 100f, 25f, PDFFieldType.TEXT),
                PDFField("workDescription", "Work Description:", PDFPoint(50f, 300f), PDFPoint(150f, 300f), 450f, 60f, PDFFieldType.TEXT),
                PDFField("partsUsed", "Parts Used:", PDFPoint(50f, 370f), PDFPoint(150f, 370f), 450f, 80f, PDFFieldType.TABLE),
                PDFField("laborHours", "Labor Hours:", PDFPoint(50f, 460f), PDFPoint(150f, 460f), 100f, 25f, PDFFieldType.NUMBER),
                PDFField("preMaintenanceCondition", "Pre-Maintenance Condition:", PDFPoint(50f, 490f), PDFPoint(200f, 490f), 150f, 25f, PDFFieldType.TEXT),
                PDFField("postMaintenanceCondition", "Post-Maintenance Condition:", PDFPoint(350f, 490f), PDFPoint(480f, 490f), 100f, 25f, PDFFieldType.TEXT),
                PDFField("issuesFound", "Issues Found:", PDFPoint(50f, 520f), PDFPoint(150f, 520f), 450f, 60f, PDFFieldType.TEXT),
                PDFField("recommendations", "Recommendations:", PDFPoint(50f, 590f), PDFPoint(150f, 590f), 450f, 60f, PDFFieldType.TEXT),
                PDFField("nextMaintenanceDate", "Next Maintenance Date:", PDFPoint(50f, 660f), PDFPoint(200f, 660f), 100f, 25f, PDFFieldType.DATE),
                PDFField("notes", "Additional Notes:", PDFPoint(50f, 690f), PDFPoint(150f, 690f), 450f, 60f, PDFFieldType.TEXT)
            ),
            footer = PDFFooter(
                signatureFields = listOf(
                    PDFSignatureField("Technician Signature", PDFPoint(50f, 760f), 150f, 30f),
                    PDFSignatureField("Supervisor Signature", PDFPoint(350f, 760f), 150f, 30f)
                ),
                footerText = "AECI MMU Companion - Maintenance Report - Generated on ${java.time.LocalDate.now().format(dateFormatter)}",
                footerPosition = PDFPoint(50f, 820f)
            )
        )
        
        return PDFTemplate(
            formType = FormType.MAINTENANCE,
            pages = listOf(page1)
        )
    }
    
    private fun createInspectionTemplate(): PDFTemplate {
        val page1 = PDFPageTemplate(
            width = 595,
            height = 842,
            header = PDFHeader(
                title = "INSPECTION REPORT",
                subtitle = "AECI Mobile Manufacturing Unit",
                logoPosition = PDFPoint(50f, 50f),
                logoSize = PDFSize(80f, 60f),
                titlePosition = PDFPoint(200f, 70f),
                subtitlePosition = PDFPoint(200f, 90f)
            ),
            fields = listOf(
                PDFField("reportNumber", "Report Number:", PDFPoint(50f, 120f), PDFPoint(150f, 120f), 200f, 25f, PDFFieldType.TEXT),
                PDFField("equipmentName", "Equipment Name:", PDFPoint(50f, 150f), PDFPoint(150f, 150f), 200f, 25f, PDFFieldType.TEXT),
                PDFField("equipmentModel", "Model:", PDFPoint(350f, 150f), PDFPoint(400f, 150f), 150f, 25f, PDFFieldType.TEXT),
                PDFField("equipmentSerial", "Serial Number:", PDFPoint(50f, 180f), PDFPoint(150f, 180f), 200f, 25f, PDFFieldType.TEXT),
                PDFField("equipmentLocation", "Location:", PDFPoint(350f, 180f), PDFPoint(400f, 180f), 150f, 25f, PDFFieldType.TEXT),
                PDFField("inspectionType", "Inspection Type:", PDFPoint(50f, 210f), PDFPoint(150f, 210f), 150f, 25f, PDFFieldType.TEXT),
                PDFField("inspectionDate", "Inspection Date:", PDFPoint(350f, 210f), PDFPoint(450f, 210f), 100f, 25f, PDFFieldType.DATE),
                PDFField("inspectorName", "Inspector:", PDFPoint(50f, 240f), PDFPoint(150f, 240f), 200f, 25f, PDFFieldType.TEXT),
                PDFField("inspectorId", "Inspector ID:", PDFPoint(350f, 240f), PDFPoint(450f, 240f), 100f, 25f, PDFFieldType.TEXT),
                PDFField("lastInspectionDate", "Last Inspection:", PDFPoint(50f, 270f), PDFPoint(150f, 270f), 100f, 25f, PDFFieldType.DATE),
                PDFField("nextInspectionDate", "Next Inspection:", PDFPoint(350f, 270f), PDFPoint(450f, 270f), 100f, 25f, PDFFieldType.DATE),
                PDFField("inspectionItems", "Inspection Items:", PDFPoint(50f, 300f), PDFPoint(150f, 300f), 450f, 120f, PDFFieldType.TABLE),
                PDFField("overallCondition", "Overall Condition:", PDFPoint(50f, 430f), PDFPoint(180f, 430f), 150f, 25f, PDFFieldType.TEXT),
                PDFField("operationalStatus", "Operational Status:", PDFPoint(350f, 430f), PDFPoint(480f, 430f), 100f, 25f, PDFFieldType.TEXT),
                PDFField("complianceStatus", "Compliance Status:", PDFPoint(50f, 460f), PDFPoint(180f, 460f), 150f, 25f, PDFFieldType.TEXT),
                PDFField("certificationRequired", "Certification Required:", PDFPoint(350f, 460f), PDFPoint(480f, 460f), 50f, 25f, PDFFieldType.BOOLEAN),
                PDFField("deficienciesFound", "Deficiencies Found:", PDFPoint(50f, 490f), PDFPoint(180f, 490f), 450f, 80f, PDFFieldType.TEXT),
                PDFField("correctiveActions", "Corrective Actions:", PDFPoint(50f, 580f), PDFPoint(180f, 580f), 450f, 80f, PDFFieldType.TEXT),
                PDFField("recommendations", "Recommendations:", PDFPoint(50f, 670f), PDFPoint(150f, 670f), 450f, 60f, PDFFieldType.TEXT)
            ),
            footer = PDFFooter(
                signatureFields = listOf(
                    PDFSignatureField("Inspector Signature", PDFPoint(50f, 760f), 150f, 30f),
                    PDFSignatureField("Supervisor Signature", PDFPoint(350f, 760f), 150f, 30f)
                ),
                footerText = "AECI MMU Companion - Inspection Report - Generated on ${java.time.LocalDate.now().format(dateFormatter)}",
                footerPosition = PDFPoint(50f, 820f)
            )
        )
        
        return PDFTemplate(
            formType = FormType.INSPECTION,
            pages = listOf(page1)
        )
    }
    
    private fun createSafetyTemplate(): PDFTemplate {
        val page1 = PDFPageTemplate(
            width = 595,
            height = 842,
            header = PDFHeader(
                title = "SAFETY INCIDENT REPORT",
                subtitle = "AECI Mobile Manufacturing Unit",
                logoPosition = PDFPoint(50f, 50f),
                logoSize = PDFSize(80f, 60f),
                titlePosition = PDFPoint(200f, 70f),
                subtitlePosition = PDFPoint(200f, 90f)
            ),
            fields = listOf(
                PDFField("reportNumber", "Report Number:", PDFPoint(50f, 120f), PDFPoint(150f, 120f), 200f, 25f, PDFFieldType.TEXT),
                PDFField("incidentDate", "Incident Date:", PDFPoint(50f, 150f), PDFPoint(150f, 150f), 100f, 25f, PDFFieldType.DATE),
                PDFField("incidentTime", "Incident Time:", PDFPoint(350f, 150f), PDFPoint(450f, 150f), 100f, 25f, PDFFieldType.TEXT),
                PDFField("incidentLocation", "Location:", PDFPoint(50f, 180f), PDFPoint(150f, 180f), 350f, 25f, PDFFieldType.TEXT),
                PDFField("incidentType", "Incident Type:", PDFPoint(50f, 210f), PDFPoint(150f, 210f), 150f, 25f, PDFFieldType.TEXT),
                PDFField("severityLevel", "Severity Level:", PDFPoint(350f, 210f), PDFPoint(450f, 210f), 100f, 25f, PDFFieldType.TEXT),
                PDFField("reportedBy", "Reported By:", PDFPoint(50f, 240f), PDFPoint(150f, 240f), 200f, 25f, PDFFieldType.TEXT),
                PDFField("reporterId", "Reporter ID:", PDFPoint(350f, 240f), PDFPoint(450f, 240f), 100f, 25f, PDFFieldType.TEXT),
                PDFField("witnessesPresent", "Witnesses Present:", PDFPoint(50f, 270f), PDFPoint(180f, 270f), 50f, 25f, PDFFieldType.BOOLEAN),
                PDFField("witnesses", "Witness Details:", PDFPoint(50f, 300f), PDFPoint(150f, 300f), 450f, 60f, PDFFieldType.TEXT),
                PDFField("injuredPersons", "Injured Persons:", PDFPoint(50f, 370f), PDFPoint(150f, 370f), 450f, 60f, PDFFieldType.TEXT),
                PDFField("incidentDescription", "Incident Description:", PDFPoint(50f, 440f), PDFPoint(180f, 440f), 450f, 80f, PDFFieldType.TEXT),
                PDFField("immediateActions", "Immediate Actions Taken:", PDFPoint(50f, 530f), PDFPoint(180f, 530f), 450f, 60f, PDFFieldType.TEXT),
                PDFField("rootCause", "Root Cause:", PDFPoint(50f, 600f), PDFPoint(150f, 600f), 450f, 40f, PDFFieldType.TEXT),
                PDFField("correctiveActions", "Corrective Actions:", PDFPoint(50f, 650f), PDFPoint(180f, 650f), 450f, 60f, PDFFieldType.TEXT),
                PDFField("regulatoryNotification", "Regulatory Notification Required:", PDFPoint(50f, 720f), PDFPoint(250f, 720f), 50f, 25f, PDFFieldType.BOOLEAN)
            ),
            footer = PDFFooter(
                signatureFields = listOf(
                    PDFSignatureField("Reporter Signature", PDFPoint(50f, 760f), 150f, 30f),
                    PDFSignatureField("Safety Officer Signature", PDFPoint(350f, 760f), 150f, 30f)
                ),
                footerText = "AECI MMU Companion - Safety Report - Generated on ${java.time.LocalDate.now().format(dateFormatter)}",
                footerPosition = PDFPoint(50f, 820f)
            )
        )
        
        return PDFTemplate(
            formType = FormType.SAFETY,
            pages = listOf(page1)
        )
    }
    
    private fun createIncidentTemplate(): PDFTemplate {
        return createSafetyTemplate().copy(
            formType = FormType.INCIDENT
        )
    }
    
    private fun createEquipmentCheckTemplate(): PDFTemplate {
        return createInspectionTemplate().copy(
            formType = FormType.EQUIPMENT_CHECK
        )
    }
    
    private fun createWorkOrderTemplate(): PDFTemplate {
        return createMaintenanceTemplate().copy(
            formType = FormType.WORK_ORDER
        )
    }
}

// Template Data Classes
data class PDFTemplate(
    val formType: FormType,
    val pages: List<PDFPageTemplate>
)

data class PDFPageTemplate(
    val width: Int,
    val height: Int,
    val header: PDFHeader?,
    val fields: List<PDFField>,
    val footer: PDFFooter?
)

data class PDFHeader(
    val title: String,
    val subtitle: String,
    val logoPosition: PDFPoint,
    val logoSize: PDFSize,
    val titlePosition: PDFPoint,
    val subtitlePosition: PDFPoint
)

data class PDFField(
    val fieldName: String,
    val label: String,
    val labelPosition: PDFPoint,
    val valuePosition: PDFPoint,
    val width: Float,
    val height: Float,
    val fieldType: PDFFieldType
)

data class PDFFooter(
    val signatureFields: List<PDFSignatureField>,
    val footerText: String,
    val footerPosition: PDFPoint
)

data class PDFSignatureField(
    val label: String,
    val position: PDFPoint,
    val width: Float,
    val height: Float
)

data class PDFPoint(
    val x: Float,
    val y: Float
)

data class PDFSize(
    val width: Float,
    val height: Float
) 