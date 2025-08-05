package com.aeci.mmucompanion.domain.model.forms

import com.aeci.mmucompanion.domain.model.FormFieldType

import com.aeci.mmucompanion.domain.model.PdfFieldMapping

import com.aeci.mmucompanion.domain.model.FormCoordinate

import com.aeci.mmucompanion.domain.model.*

/**
 * Template for Pre-Task Safety Check
 * Pre-task safety and risk assessment
 */
class PreTaskTemplate : DigitalFormTemplate {
    
        override val templateId = "PRE_TASK"
    override val title = "Pre-Task Safety Check"
    override val version = "1.0"
        override val formType = FormType.PRE_TASK
    override val pdfFileName = "pre-task.pdf"
    
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
            text = "Pre-Task Safety Check".uppercase(),
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
            description = "PreTask form template",
            sections = listOf(
                FormSection("general_info",
                    title = "General Information", 
                    fields = listOf(
                        FormField("date", "Date", FormFieldType.DATE, true),
                        FormField("time", "Time", FormFieldType.TIME, true),
                        FormField("site_id", "Site ID", FormFieldType.TEXT, true),
                        FormField("operator", "Operator", FormFieldType.TEXT, true)
                    )
                ),
                FormSection("form_data",
                    title = "Form Data",
                    fields = listOf(
                        FormField("status", "Status", FormFieldType.TEXT, false),
                        FormField("notes", "Notes", FormFieldType.TEXTAREA, false)
                    )
                )
            )
        )
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


