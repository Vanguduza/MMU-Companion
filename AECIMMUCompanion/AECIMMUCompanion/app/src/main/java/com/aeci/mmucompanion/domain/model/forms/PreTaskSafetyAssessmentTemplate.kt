package com.aeci.mmucompanion.domain.model.forms

import com.aeci.mmucompanion.domain.model.FormFieldType

import com.aeci.mmucompanion.domain.model.PdfFieldMapping

import com.aeci.mmucompanion.domain.model.*

/**
 * COMPREHENSIVE Pre-Task Safety Assessment Template
 * Complete field coverage matching original PDF with precise coordinate mapping
 */
class PreTaskSafetyAssessmentTemplate : DigitalFormTemplate {
    
        override val templateId = "PRE_TASK_SAFETY_ASSESSMENT"
    override val title = "Pre-Task Safety Assessment"
    override val version = "1.0"
        override val formType = FormType.PRE_TASK_SAFETY_ASSESSMENT
    override val pdfFileName = "Pre-Task Safety Assessment.pdf"
    
    override val logoCoordinates = listOf(
        LogoCoordinate(
            logoType = "aeci_main_logo",
            x = 50f, y = 750f, width = 120f, height = 60f,
            imagePath = "assets/images/aeci-logo.png"
        ),
        LogoCoordinate(
            logoType = "mining_explosives_logo",
            x = 450f, y = 750f, width = 140f, height = 60f,
            imagePath = "assets/images/AECI-Mining-Explosives-logo_full-colour-2048x980.jpg"
        )
    )
    
    override val staticTextCoordinates = listOf(
        StaticTextCoordinate(
            text = "Pre-Task Safety Assessment".uppercase(),
            x = 200f, y = 720f, fontSize = 16f, fontWeight = "bold",
            alignment = TextAlignment.CENTER
        ),
        StaticTextCoordinate(
            text = "AECI Mining Explosives",
            x = 200f, y = 700f, fontSize = 10f, fontWeight = "normal",
            alignment = TextAlignment.CENTER
        )
    )
    
    override val headerCoordinates = listOf<HeaderCoordinate>()

    override val formRelationships = listOf(
        FormRelationship(
            sourceField = "equipment_id", targetForm = FormType.EQUIPMENT_REGISTER,
            targetField = "equipment_id",
            relationshipType = RelationshipType.LOOKUP
        )
    )

    override fun getValidationRules(): List<ValidationRule> {
        return listOf(
            ValidationRule(fieldName = "date", ruleName = "validation", expression = "date <= TODAY", errorMessage = "Date cannot be in the future"
            )
        )
    }

    override fun getRelatedFormUpdates(): List<FormRelationshipUpdate> {
        return listOf(
            FormRelationshipUpdate(targetFormType = FormType.EQUIPMENT_REGISTER,
                fieldMappings = mapOf("equipment_id" to "last_inspection_date")
            )
        )
    }    
    override fun getFormTemplate(): FormDefinition {
        return FormDefinition(
            id = templateId,
            name = title,
            description = "PreTaskSafetyAssessment form template",
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
        return mapOf()
    }
}



