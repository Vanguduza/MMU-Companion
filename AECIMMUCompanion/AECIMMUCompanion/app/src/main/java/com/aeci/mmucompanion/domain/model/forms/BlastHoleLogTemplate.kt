package com.aeci.mmucompanion.domain.model.forms

import com.aeci.mmucompanion.domain.model.FormFieldType

import com.aeci.mmucompanion.domain.model.PdfFieldMapping

import com.aeci.mmucompanion.domain.model.FormCoordinate

import com.aeci.mmucompanion.domain.model.*

/**
 * Template for Blast Hole Log
 * Blast hole drilling and logging form
 */
class BlastHoleLogTemplate : DigitalFormTemplate {
    
        override val templateId = "BLAST_HOLE_LOG"
    override val title = "Blast Hole Log"
    override val version = "1.0"
        override val formType = FormType.BLAST_HOLE_LOG
    override val pdfFileName = "blast hole log.pdf"
    
    override val logoCoordinates = listOf(
        LogoCoordinate(
            logoType = "aeci_main_logo",
            x = 50f, y = 750f, width = 120f, height = 60f
        ),
        LogoCoordinate(
            logoType = "mining_explosives_logo", 
            x = 450f, y = 750f, width = 140f, height = 60f
        )
    )
    
    override val staticTextCoordinates = listOf(
        StaticTextCoordinate(
            text = "Blast Hole Log".uppercase(),
            x = 200f, y = 720f, fontSize = 16f, fontWeight = "bold"
        )
    )
    
    override val headerCoordinates = listOf<HeaderCoordinate>()

    override val formRelationships = listOf<FormRelationship>()

    override fun getValidationRules(): List<ValidationRule> {
        return listOf(
            ValidationRule(fieldName = "date", ruleName = "validation", expression = "date <= TODAY", errorMessage = "Date cannot be in the future"
            )
        )
    }

    override fun getRelatedFormUpdates(): List<FormRelationshipUpdate> {
        return listOf()
    }    
    override fun getFormTemplate(): FormDefinition {
        return FormDefinition(
            id = templateId,
            name = title,
            description = "Blast Hole Log exactly matching blast hole log.pdf",
            sections = listOf(
                // Header Section - matches original PDF layout
                FormSection("log_header",
                    title = "Log Header", 
                    fields = listOf(
                        FormField("date", "Date", FormFieldType.DATE, true),
                        FormField("shift", "Shift", FormFieldType.TEXT, true),
                        FormField("operator_name", "Operator Name", FormFieldType.TEXT, true),
                        FormField("site_location", "Site Location", FormFieldType.TEXT, true)
                    )
                ),
                // Blast Information Section
                FormSection("blast_info",
                    title = "Blast Information",
                    fields = listOf(
                        FormField("blast_number", "Blast Number", FormFieldType.TEXT, true),
                        FormField("blast_date", "Blast Date", FormFieldType.DATE, true),
                        FormField("blast_time", "Blast Time", FormFieldType.TIME, false),
                        FormField("site_name", "Site Name", FormFieldType.TEXT, true),
                        FormField("total_emulsion_used", "Total Emulsion Used (kg)", FormFieldType.NUMBER, false),
                        FormField("blast_quality_grade", "Blast Quality Grade", FormFieldType.TEXT, false)
                    )
                ),
                // Hole Data Section - individual hole entries
                FormSection("hole_data",
                    title = "Hole Data",
                    fields = listOf(
                        FormField("hole_number", "Hole Number", FormFieldType.TEXT, false),
                        FormField("hole_depth_meters", "Hole Depth (m)", FormFieldType.NUMBER, false),
                        FormField("hole_diameter_mm", "Hole Diameter (mm)", FormFieldType.NUMBER, false),
                        FormField("emulsion_amount", "Emulsion Amount (kg)", FormFieldType.NUMBER, false),
                        FormField("primer_type", "Primer Type", FormFieldType.TEXT, false),
                        FormField("powder_factor", "Powder Factor", FormFieldType.NUMBER, false),
                        FormField("geology_notes", "Geology Notes", FormFieldType.TEXT, false)
                    )
                ),
                // Summary Section
                FormSection("summary",
                    title = "Summary",
                    fields = listOf(
                        FormField("total_holes_drilled", "Total Holes Drilled", FormFieldType.NUMBER, true),
                        FormField("total_meters_drilled_summary", "Total Meters Drilled", FormFieldType.NUMBER, true),
                        FormField("general_comments", "General Comments", FormFieldType.TEXTAREA, false)
                    )
                ),
                // Signatures Section
                FormSection("signatures",
                    title = "Signatures",
                    fields = listOf(
                        FormField("operator_signature", "Operator Signature", FormFieldType.SIGNATURE, true),
                        FormField("supervisor_signature", "Supervisor Signature", FormFieldType.SIGNATURE, true)
                    )
                )
            )
        )
    }    
    override fun getPdfFieldMappings(): Map<String, PdfFieldMapping> {
        return mapOf(
            // Header fields - exact coordinates from original PDF
            "date" to PdfFieldMapping(
                fieldName = "date",
                pdfFieldName = "date",
                coordinate = FormCoordinate(50f, 750f, 100f, 20f),
                fieldType = FormFieldType.DATE
            ),
            "shift" to PdfFieldMapping(
                fieldName = "shift",
                pdfFieldName = "shift",
                coordinate = FormCoordinate(200f, 750f, 100f, 20f),
                fieldType = FormFieldType.TEXT
            ),
            "operator_name" to PdfFieldMapping(
                fieldName = "operator_name",
                pdfFieldName = "operator_name",
                coordinate = FormCoordinate(350f, 750f, 150f, 20f),
                fieldType = FormFieldType.TEXT
            ),
            "site_location" to PdfFieldMapping(
                fieldName = "site_location",
                pdfFieldName = "site_location",
                coordinate = FormCoordinate(500f, 750f, 100f, 20f),
                fieldType = FormFieldType.TEXT
            ),
            
            // Hole data fields - example coordinates for first row
            "hole_depth_meters" to PdfFieldMapping(
                fieldName = "hole_depth_meters",
                pdfFieldName = "hole_depth_meters",
                coordinate = FormCoordinate(140f, 700f, 80f, 20f),
                fieldType = FormFieldType.NUMBER
            ),
            "hole_diameter_mm" to PdfFieldMapping(
                fieldName = "hole_diameter_mm",
                pdfFieldName = "hole_diameter_mm",
                coordinate = FormCoordinate(230f, 700f, 80f, 20f),
                fieldType = FormFieldType.NUMBER
            ),
            "powder_factor" to PdfFieldMapping(
                fieldName = "powder_factor",
                pdfFieldName = "powder_factor",
                coordinate = FormCoordinate(320f, 700f, 80f, 20f),
                fieldType = FormFieldType.NUMBER
            ),
            "geology_notes" to PdfFieldMapping(
                fieldName = "geology_notes",
                pdfFieldName = "geology_notes",
                coordinate = FormCoordinate(410f, 700f, 150f, 20f),
                fieldType = FormFieldType.TEXT
            ),
            
            // Summary fields - exact coordinates from original PDF
            "total_holes_drilled" to PdfFieldMapping(
                fieldName = "total_holes_drilled",
                pdfFieldName = "total_holes_drilled",
                coordinate = FormCoordinate(50f, 100f, 120f, 20f),
                fieldType = FormFieldType.NUMBER
            ),
            "total_meters_drilled_summary" to PdfFieldMapping(
                fieldName = "total_meters_drilled_summary",
                pdfFieldName = "total_meters_drilled_summary",
                coordinate = FormCoordinate(200f, 100f, 120f, 20f),
                fieldType = FormFieldType.NUMBER
            ),
            "general_comments" to PdfFieldMapping(
                fieldName = "general_comments",
                pdfFieldName = "general_comments",
                coordinate = FormCoordinate(50f, 60f, 400f, 30f),
                fieldType = FormFieldType.TEXTAREA
            ),
            
            // Signature fields - exact coordinates from original PDF
            "operator_signature" to PdfFieldMapping(
                fieldName = "operator_signature",
                pdfFieldName = "operator_signature",
                coordinate = FormCoordinate(50f, 20f, 150f, 30f),
                fieldType = FormFieldType.SIGNATURE
            ),
            "supervisor_signature" to PdfFieldMapping(
                fieldName = "supervisor_signature",
                pdfFieldName = "supervisor_signature",
                coordinate = FormCoordinate(250f, 20f, 150f, 30f),
                fieldType = FormFieldType.SIGNATURE
            )
        )
    }
}


