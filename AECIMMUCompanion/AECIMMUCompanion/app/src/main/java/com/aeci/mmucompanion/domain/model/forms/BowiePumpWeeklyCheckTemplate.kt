package com.aeci.mmucompanion.domain.model.forms

import com.aeci.mmucompanion.domain.model.FormFieldType

import com.aeci.mmucompanion.domain.model.PdfFieldMapping

import com.aeci.mmucompanion.domain.model.FormCoordinate

import com.aeci.mmucompanion.domain.model.*

/**
 * Template for Bowie Pump Weekly Check
 * Weekly maintenance check for Bowie pumps
 */
class BowiePumpWeeklyCheckTemplate : DigitalFormTemplate {
    
        override val templateId = "BOWIE_PUMP_WEEKLY_CHECK"
    override val title = "Bowie Pump Weekly Check"
    override val version = "1.0"
        override val formType = FormType.PUMP_WEEKLY_CHECK
    override val pdfFileName = "Bowie Pump Weekly check list.pdf"
    
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
            text = "Bowie Pump Weekly Check".uppercase(),
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

    override fun getFormTemplate(): FormDefinition {
        return FormDefinition(
            id = templateId,
            name = title,
            description = "Weekly maintenance check for Bowie pumps",
            sections = listOf(
                FormSection("pump_details",
                    title = "Pump Details",
                    fields = listOf(
                        FormField("pump_id", "Pump ID", FormFieldType.TEXT, true),
                        FormField("date", "Date", FormFieldType.DATE, true),
                        FormField("technician", "Technician", FormFieldType.TEXT, true)
                    )
                ),
                FormSection("checklist",
                    title = "Weekly Check Items",
                    fields = listOf(
                        FormField("pressure_check", "Pressure Check", FormFieldType.CHECKBOX, true),
                        FormField("visual_inspection", "Visual Inspection", FormFieldType.CHECKBOX, true),
                        FormField("lubrication", "Lubrication", FormFieldType.CHECKBOX, false)
                    )
                )
            )
        )
    }

    override fun getRelatedFormUpdates(): List<FormRelationshipUpdate> {
        return listOf()
    }    
    override fun getPdfFieldMappings(): Map<String, PdfFieldMapping> {
        return mapOf(
            "form_type" to PdfFieldMapping(
                fieldName = "form_type",
                pdfFieldName = "form_type",
                coordinate = FormCoordinate(50f, 50f, 200f, 30f),
                fieldType = FormFieldType.TEXT
            ),
            "created_date" to PdfFieldMapping(
                fieldName = "created_date", 
                pdfFieldName = "created_date",
                coordinate = FormCoordinate(300f, 50f, 150f, 30f),
                fieldType = FormFieldType.DATE
            ),
            "site_location" to PdfFieldMapping(
                fieldName = "site_location",
                pdfFieldName = "site_location", 
                coordinate = FormCoordinate(500f, 50f, 200f, 30f),
                fieldType = FormFieldType.TEXT
            )
        )
    }
}


