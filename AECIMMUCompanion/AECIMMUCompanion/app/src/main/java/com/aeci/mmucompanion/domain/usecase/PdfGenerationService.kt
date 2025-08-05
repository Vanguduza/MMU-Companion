package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.*
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.forms.PdfAcroForm
import com.itextpdf.forms.fields.PdfFormField
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfGenerationService @Inject constructor() {
    
    /**
     * Generates a PDF from a digital form using coordinate mapping
     */
    suspend fun generatePdf(form: Form): Result<ByteArray> {
        return try {
            val pdfBytes = ByteArrayOutputStream()
            val writer = PdfWriter(pdfBytes)
            val pdfDoc = PdfDocument(writer)
            val document = Document(pdfDoc)
            
            // Add header
            addFormHeader(document, form)
            
            // Add form-specific content
            when (form) {
                is BlastHoleLogForm -> addBlastHoleLogContent(document, form)
                is MmuQualityReportForm -> addQualityReportContent(document, form)
                is MmuProductionDailyLogForm -> addProductionLogContent(document, form)
                is PumpInspection90DayForm -> addPumpInspectionContent(document, form)
                is FireExtinguisherInspectionForm -> addFireExtinguisherContent(document, form)
                is BowiePumpWeeklyCheckForm -> addBowiePumpContent(document, form)
                is MmuChassisMaintenanceForm -> addChassisMaintenanceContent(document, form)
                is MmuHandoverCertificateForm -> addHandoverCertificateContent(document, form)
                is OnBenchMmuInspectionForm -> addOnBenchInspectionContent(document, form)
                is PcPumpHighLowPressureTripTestForm -> addPressureTripTestContent(document, form)
                is MonthlyProcessMaintenanceForm -> addMonthlyMaintenanceContent(document, form)
                is PreTaskSafetyForm -> addPreTaskSafetyContent(document, form)
                is JobCardForm -> addJobCardContent(document, form)
                is TimesheetForm -> addTimesheetContent(document, form)
                else -> addGenericFormContent(document, form)
            }
            
            // Add footer
            addFormFooter(document, form)
            
            document.close()
            Result.success(pdfBytes.toByteArray())
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Fills an existing PDF template using coordinate mapping
     */
    suspend fun fillPdfTemplate(
        templateBytes: ByteArray,
        form: Form,
        coordinateMapping: List<PdfFieldMapping>
    ): Result<ByteArray> {
        return try {
            val outputBytes = ByteArrayOutputStream()
            val reader = PdfReader(templateBytes.inputStream())
            val writer = PdfWriter(outputBytes)
            val pdfDoc = PdfDocument(reader, writer)
            
            val acroForm = PdfAcroForm.getAcroForm(pdfDoc, true)
            
            // Fill form fields using coordinate mapping
            coordinateMapping.forEach { mapping ->
                val field = acroForm.getField(mapping.fieldName)
                val value = getFormFieldValue(form, mapping.fieldName)
                
                field?.let {
                    when (mapping.fieldType) {
                        FormFieldType.TEXT -> it.setValue(value.toString())
                        FormFieldType.MULTILINE_TEXT -> it.setValue(value.toString())
                        FormFieldType.TEXTAREA -> it.setValue(value.toString())
                        FormFieldType.NUMBER -> it.setValue(value.toString())
                        FormFieldType.INTEGER -> it.setValue(value.toString())
                        FormFieldType.BOOLEAN -> {
                            val boolValue = value as? Boolean ?: false
                            if (boolValue) it.setValue("Yes") else it.setValue("No")
                        }
                        FormFieldType.DATE -> {
                            val dateValue = value as? java.time.LocalDate
                            it.setValue(dateValue?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "")
                        }
                        FormFieldType.TIME -> it.setValue(value.toString())
                        FormFieldType.DATETIME -> it.setValue(value.toString())
                        FormFieldType.CHECKBOX -> {
                            val boolValue = value as? Boolean ?: false
                            if (boolValue) it.setValue("☑") else it.setValue("☐")
                        }
                        FormFieldType.RADIO -> it.setValue(value.toString())
                        FormFieldType.DROPDOWN -> it.setValue(value.toString())
                        FormFieldType.SIGNATURE -> it.setValue(value.toString())
                        FormFieldType.PHOTO -> it.setValue(value.toString())
                        FormFieldType.BARCODE -> it.setValue(value.toString())
                        FormFieldType.EQUIPMENT_ID -> it.setValue(value.toString())
                        FormFieldType.SITE_CODE -> it.setValue(value.toString())
                        FormFieldType.EMPLOYEE_ID -> it.setValue(value.toString())
                    }
                }
            }
            
            // Flatten the form to prevent further editing
            acroForm.flattenFields()
            
            pdfDoc.close()
            Result.success(outputBytes.toByteArray())
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun addFormHeader(document: Document, form: Form) {
        val title = when (form) {
            is BlastHoleLogForm -> "BLAST HOLE LOG / CHARGE SHEET"
            is MmuQualityReportForm -> "MMU Quality Report"
            is MmuProductionDailyLogForm -> "MMU Production Daily Log"
            is PumpInspection90DayForm -> "90 DAY PUMP SYSTEM INSPECTION CHECKLIST"
            is FireExtinguisherInspectionForm -> "FIRE EXTINGUISHER INSPECTION CHECKLIST"
            is BowiePumpWeeklyCheckForm -> "Bowie Pump Weekly Check List"
            is MmuChassisMaintenanceForm -> "MMU CHASSIS MAINTENANCE RECORD"
            is MmuHandoverCertificateForm -> "MMU HANDOVER CERTIFICATE"
            is OnBenchMmuInspectionForm -> "ON BENCH MMU INSPECTION"
            is PcPumpHighLowPressureTripTestForm -> "PC PUMP HIGH LOW PRESSURE TRIP TEST"
            is MonthlyProcessMaintenanceForm -> "MONTHLY PROCESS MAINTENANCE RECORD"
            is PreTaskSafetyForm -> "Pre-Task Safety Form"
            is JobCardForm -> "Job Card"
            is TimesheetForm -> "Timesheet"
            else -> "Form Report"
        }
        
        document.add(
            Paragraph(title)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(16f)
                .setBold()
        )
        
        document.add(
            Paragraph("AECI Mining Explosives")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12f)
        )
        
        document.add(Paragraph("\n"))
        
        // Add basic form information
        val infoTable = Table(2)
        infoTable.addCell("Form ID:")
        infoTable.addCell(form.id)
        infoTable.addCell("Created:")
        infoTable.addCell(form.createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
        infoTable.addCell("Site:")
        infoTable.addCell(form.siteLocation)
        infoTable.addCell("Status:")
        infoTable.addCell(form.status.name)
        
        document.add(infoTable)
        document.add(Paragraph("\n"))
    }
    
    private fun addBlastHoleLogContent(document: Document, form: BlastHoleLogForm) {
        // Blast Details Section
        document.add(Paragraph("BLAST DETAILS").setBold())
        
        val detailsTable = Table(4)
        detailsTable.addCell("Blast Number:")
        detailsTable.addCell(form.blastNumber)
        detailsTable.addCell("Blast Date:")
        detailsTable.addCell(form.blastDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        
        detailsTable.addCell("Site Name:")
        detailsTable.addCell(form.siteName)
        detailsTable.addCell("Location:")
        detailsTable.addCell(form.siteLocation ?: "")
        
        detailsTable.addCell("Operator:")
        detailsTable.addCell(form.operatorName)
        detailsTable.addCell("Blast Time:")
        detailsTable.addCell(form.blastTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "")
        
        document.add(detailsTable)
        document.add(Paragraph("\n"))
        
        // Hole Details Section
        document.add(Paragraph("HOLE DETAILS").setBold())
        
        val holesTable = Table(6)
        holesTable.addCell("Hole No.")
        holesTable.addCell("Depth (m)")
        holesTable.addCell("Diameter (mm)")
        holesTable.addCell("Emulsion (kg)")
        holesTable.addCell("Primer Type")
        holesTable.addCell("Notes")
        
        form.holes.forEach { hole ->
            holesTable.addCell(hole.holeNumber)
            holesTable.addCell(hole.depth.toString())
            holesTable.addCell(hole.diameter.toString())
            holesTable.addCell(hole.emulsionAmount.toString())
            holesTable.addCell(hole.primerType)
            holesTable.addCell(hole.notes ?: "")
        }
        
        document.add(holesTable)
        document.add(Paragraph("\n"))
        
        // Summary Section
        document.add(Paragraph("BLAST SUMMARY").setBold())
        val summaryTable = Table(2)
        summaryTable.addCell("Total Holes:")
        summaryTable.addCell(form.holes.size.toString())
        summaryTable.addCell("Total Emulsion Used:")
        summaryTable.addCell("${form.totalEmulsionUsed} kg")
        summaryTable.addCell("Average Hole Depth:")
        summaryTable.addCell("${"%.2f".format(form.holes.map { it.depth }.average())} m")
        summaryTable.addCell("Blast Quality Grade:")
        summaryTable.addCell(form.blastQualityGrade ?: "")
        
        document.add(summaryTable)
        
        if (!form.notes.isNullOrBlank()) {
            document.add(Paragraph("\n"))
            document.add(Paragraph("NOTES").setBold())
            document.add(Paragraph(form.notes))
        }
    }
    
    private fun addQualityReportContent(document: Document, form: MmuQualityReportForm) {
        // Report Details
        document.add(Paragraph("QUALITY REPORT DETAILS").setBold())
        
        val detailsTable = Table(4)
        detailsTable.addCell("Report Number:")
        detailsTable.addCell(form.reportNumber)
        detailsTable.addCell("Report Date:")
        detailsTable.addCell(form.reportDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        
        detailsTable.addCell("Shift Pattern:")
        detailsTable.addCell(form.shiftPattern)
        detailsTable.addCell("Shift Supervisor:")
        detailsTable.addCell(form.shiftSupervisor)
        
        detailsTable.addCell("Quality Technician:")
        detailsTable.addCell(form.qualityTechnician)
        detailsTable.addCell("Quality Grade:")
        detailsTable.addCell(form.qualityGrade)
        
        document.add(detailsTable)
        document.add(Paragraph("\n"))
        
        // Production Data
        document.add(Paragraph("PRODUCTION DATA").setBold())
        
        val prodTable = Table(2)
        prodTable.addCell("Target Emulsion Production:")
        prodTable.addCell("${form.targetEmulsionProduction} kg")
        prodTable.addCell("Actual Emulsion Production:")
        prodTable.addCell("${form.actualEmulsionProduction} kg")
        prodTable.addCell("Emulsion Used Today:")
        prodTable.addCell("${form.emulsionUsedToday} kg")
        
        document.add(prodTable)
        document.add(Paragraph("\n"))
        
        // Quality Tests
        document.add(Paragraph("QUALITY TESTS").setBold())
        
        val testsTable = Table(2)
        testsTable.addCell("Viscosity Reading:")
        testsTable.addCell(form.viscosityReading.toString())
        testsTable.addCell("Temperature Reading:")
        testsTable.addCell("${form.temperatureReading}°C")
        testsTable.addCell("pH Level:")
        testsTable.addCell(form.phLevel.toString())
        
        document.add(testsTable)
        
        // Density Tests
        if (form.densityTests.isNotEmpty()) {
            document.add(Paragraph("\n"))
            document.add(Paragraph("DENSITY TESTS").setBold())
            
            val densityTable = Table(3)
            densityTable.addCell("Time")
            densityTable.addCell("Density")
            densityTable.addCell("Within Spec")
            
            form.densityTests.forEach { test ->
                densityTable.addCell(test.testTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                densityTable.addCell(test.density.toString())
                densityTable.addCell(if (test.withinSpec) "Yes" else "No")
            }
            
            document.add(densityTable)
        }
        
        // Quality Issues
        if (form.qualityIssues.isNotEmpty()) {
            document.add(Paragraph("\n"))
            document.add(Paragraph("QUALITY ISSUES").setBold())
            
            form.qualityIssues.forEach { issue ->
                document.add(Paragraph("• ${issue.description}"))
                document.add(Paragraph("  Severity: ${issue.severity}"))
                document.add(Paragraph("  Action Taken: ${issue.actionTaken}"))
            }
        }
        
        // Recommendations
        if (form.recommendations.isNotBlank()) {
            document.add(Paragraph("\n"))
            document.add(Paragraph("RECOMMENDATIONS").setBold())
            document.add(Paragraph(form.recommendations))
        }
    }
    
    private fun addProductionLogContent(document: Document, form: MmuProductionDailyLogForm) {
        // Log Details
        document.add(Paragraph("PRODUCTION LOG DETAILS").setBold())
        
        val detailsTable = Table(4)
        detailsTable.addCell("Log Date:")
        detailsTable.addCell(form.logDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        detailsTable.addCell("Shift Details:")
        detailsTable.addCell(form.shiftDetails)
        
        detailsTable.addCell("Operator:")
        detailsTable.addCell(form.operatorName)
        detailsTable.addCell("Supervisor:")
        detailsTable.addCell(form.supervisorName)
        
        detailsTable.addCell("Start Time:")
        detailsTable.addCell(form.startTime.format(DateTimeFormatter.ofPattern("HH:mm")))
        detailsTable.addCell("End Time:")
        detailsTable.addCell(form.endTime.format(DateTimeFormatter.ofPattern("HH:mm")))
        
        document.add(detailsTable)
        document.add(Paragraph("\n"))
        
        // Production Summary
        document.add(Paragraph("PRODUCTION SUMMARY").setBold())
        
        val prodTable = Table(2)
        prodTable.addCell("Total Operating Hours:")
        prodTable.addCell(form.totalOperatingHours.toString())
        prodTable.addCell("Total Emulsion Consumed:")
        prodTable.addCell("${form.totalEmulsionConsumed} kg")
        prodTable.addCell("Quality Grade Achieved:")
        prodTable.addCell(form.qualityGradeAchieved)
        prodTable.addCell("Production Target:")
        prodTable.addCell("${form.productionTarget} kg")
        prodTable.addCell("Actual Production:")
        prodTable.addCell("${form.actualProduction} kg")
        prodTable.addCell("Operating Temperature:")
        prodTable.addCell("${form.operatingTemperature}°C")
        
        document.add(prodTable)
        document.add(Paragraph("\n"))
        
        // Equipment Status
        document.add(Paragraph("EQUIPMENT STATUS").setBold())
        document.add(Paragraph("Condition: ${form.equipmentCondition}"))
        
        if (form.maintenancePerformed.isNotEmpty()) {
            document.add(Paragraph("\n"))
            document.add(Paragraph("MAINTENANCE PERFORMED").setBold())
            form.maintenancePerformed.forEach { maintenance ->
                document.add(Paragraph("• ${maintenance.description}"))
                document.add(Paragraph("  Time: ${maintenance.timeSpent} minutes"))
                document.add(Paragraph("  Technician: ${maintenance.technicianName}"))
            }
        }
        
        // Safety Observations
        if (form.safetyObservations.isNotEmpty()) {
            document.add(Paragraph("\n"))
            document.add(Paragraph("SAFETY OBSERVATIONS").setBold())
            form.safetyObservations.forEach { observation ->
                document.add(Paragraph("• ${observation.description}"))
                document.add(Paragraph("  Action Required: ${if (observation.actionRequired) "Yes" else "No"}"))
            }
        }
        
        // Comments
        if (form.operatorComments.isNotBlank()) {
            document.add(Paragraph("\n"))
            document.add(Paragraph("OPERATOR COMMENTS").setBold())
            document.add(Paragraph(form.operatorComments))
        }
        
        if (form.supervisorComments.isNotBlank()) {
            document.add(Paragraph("\n"))
            document.add(Paragraph("SUPERVISOR COMMENTS").setBold())
            document.add(Paragraph(form.supervisorComments))
        }
    }
    
    private fun addPumpInspectionContent(document: Document, form: PumpInspection90DayForm) {
        // Inspection Details
        document.add(Paragraph("INSPECTION DETAILS").setBold())
        
        val detailsTable = Table(4)
        detailsTable.addCell("Inspection Date:")
        detailsTable.addCell(form.inspectionDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        detailsTable.addCell("Inspector:")
        detailsTable.addCell(form.inspectorName)
        
        detailsTable.addCell("Pump Serial Number:")
        detailsTable.addCell(form.pumpSerialNumber)
        detailsTable.addCell("Equipment Location:")
        detailsTable.addCell(form.equipmentLocation)
        
        detailsTable.addCell("Last Inspection:")
        detailsTable.addCell(form.lastInspectionDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "")
        detailsTable.addCell("Next Inspection Due:")
        detailsTable.addCell(form.nextInspectionDue.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        
        document.add(detailsTable)
        document.add(Paragraph("\n"))
        
        // Visual Inspection
        document.add(Paragraph("VISUAL INSPECTION ITEMS").setBold())
        
        val inspectionTable = Table(3)
        inspectionTable.addCell("Item")
        inspectionTable.addCell("Pass/Fail")
        inspectionTable.addCell("Notes")
        
        form.visualInspectionItems.forEach { item ->
            inspectionTable.addCell(item.itemName)
            inspectionTable.addCell(if (item.passed) "PASS" else "FAIL")
            inspectionTable.addCell(item.notes)
        }
        
        document.add(inspectionTable)
        document.add(Paragraph("\n"))
        
        // Pressure Tests
        if (form.pressureTests.isNotEmpty()) {
            document.add(Paragraph("PRESSURE TESTS").setBold())
            
            val pressureTable = Table(4)
            pressureTable.addCell("Test Type")
            pressureTable.addCell("Test Pressure")
            pressureTable.addCell("Pass/Fail")
            pressureTable.addCell("Notes")
            
            form.pressureTests.forEach { test ->
                pressureTable.addCell(test.testType)
                pressureTable.addCell("${test.testPressure} bar")
                pressureTable.addCell(if (test.passed) "PASS" else "FAIL")
                pressureTable.addCell(test.notes)
            }
            
            document.add(pressureTable)
        }
        
        // Overall Assessment
        document.add(Paragraph("\n"))
        document.add(Paragraph("OVERALL ASSESSMENT").setBold())
        document.add(Paragraph("Status: ${form.overallStatus}"))
        
        if (form.recommendedActions.isNotEmpty()) {
            document.add(Paragraph("\n"))
            document.add(Paragraph("RECOMMENDED ACTIONS").setBold())
            form.recommendedActions.forEach { action ->
                document.add(Paragraph("• ${action.description}"))
                document.add(Paragraph("  Priority: ${action.priority}"))
                document.add(Paragraph("  Due Date: ${action.dueDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Not specified"}"))
            }
        }
    }
    
    // Additional form content methods would be implemented similarly...
    private fun addFireExtinguisherContent(document: Document, form: FireExtinguisherInspectionForm) {
        // Implementation for fire extinguisher inspection form
    }
    
    private fun addBowiePumpContent(document: Document, form: BowiePumpWeeklyCheckForm) {
        // Implementation for Bowie pump weekly check form
    }
    
    private fun addChassisMaintenanceContent(document: Document, form: MmuChassisMaintenanceForm) {
        // Implementation for chassis maintenance form
    }
    
    private fun addHandoverCertificateContent(document: Document, form: MmuHandoverCertificateForm) {
        // Implementation for handover certificate form
    }
    
    private fun addOnBenchInspectionContent(document: Document, form: OnBenchMmuInspectionForm) {
        // Implementation for on-bench inspection form
    }
    
    private fun addPressureTripTestContent(document: Document, form: PcPumpHighLowPressureTripTestForm) {
        // Implementation for pressure trip test form
    }
    
    private fun addMonthlyMaintenanceContent(document: Document, form: MonthlyProcessMaintenanceForm) {
        // Implementation for monthly maintenance form
    }
    
    private fun addPreTaskSafetyContent(document: Document, form: PreTaskSafetyForm) {
        // Implementation for pre-task safety form
    }
    
    private fun addJobCardContent(document: Document, form: JobCardForm) {
        // Implementation for job card form
    }
    
    private fun addTimesheetContent(document: Document, form: TimesheetForm) {
        // Implementation for timesheet form
    }
    
    private fun addGenericFormContent(document: Document, form: Form) {
        document.add(Paragraph("Generic form content - specific implementation needed"))
    }
    
    private fun addFormFooter(document: Document, form: Form) {
        document.add(Paragraph("\n"))
        document.add(Paragraph("Generated by AECI MMU Companion App").setTextAlignment(TextAlignment.CENTER))
        document.add(Paragraph("${java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}").setTextAlignment(TextAlignment.CENTER))
    }
    
    /**
     * Extracts form field value from Form
     */
    private fun getFormFieldValue(form: Form, fieldName: String): Any? {
        return when (form) {
            is DigitalForm -> form.data[fieldName]
            else -> {
                // Use reflection to get field value from any form implementation
                try {
                    val field = form::class.java.getDeclaredField(fieldName)
                    field.isAccessible = true
                    field.get(form)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    /**
     * Validates PDF template compatibility with form
     */
    suspend fun validatePdfTemplate(templateBytes: ByteArray, form: Form): Result<Boolean> {
        return try {
            val reader = PdfReader(templateBytes.inputStream())
            val pdfDoc = PdfDocument(reader)
            val acroForm = PdfAcroForm.getAcroForm(pdfDoc, false)
            
            val fieldNames = acroForm?.getAllFormFields()?.keys ?: emptySet()
            val formFieldNames = when (form) {
                is DigitalForm -> form.data.keys
                else -> emptySet()
            }
            
            pdfDoc.close()
            
            // Check if template has required fields
            val hasRequiredFields = formFieldNames.all { fieldName ->
                fieldNames.contains(fieldName)
            }
            
            Result.success(hasRequiredFields)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun Double.format(digits: Int) = "%.${digits}f".format(this)
}



