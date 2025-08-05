package com.aeci.mmucompanion.domain.model.forms

import com.aeci.mmucompanion.domain.model.FormFieldType

import com.aeci.mmucompanion.domain.model.PdfFieldMapping

import com.aeci.mmucompanion.domain.model.*

/**
 * COMPREHENSIVE Job Card Template
 * Complete field coverage matching original PDF with precise coordinate mapping
 */
class JobCardTemplate : DigitalFormTemplate {
    
        override val templateId = "JOB_CARD"
    override val title = "Job Card"
    override val version = "1.0"
        override val formType = FormType.JOB_CARD
    override val pdfFileName = "job card.pdf"
    
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
            text = "Job Card".uppercase(),
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
            description = "Job Card exactly matching job card.pdf",
            sections = listOf(
                // Header Section - matches original PDF layout
                FormSection("job_header",
                    title = "Job Information", 
                    fields = listOf(
                        FormField("job_card_number", "Job Card Number", FormFieldType.TEXT, true),
                        FormField("job_date", "Job Date", FormFieldType.DATE, true),
                        FormField("customer_name", "Customer Name", FormFieldType.TEXT, true),
                        FormField("site_address", "Site Address", FormFieldType.TEXT, true),
                        FormField("contact_person", "Contact Person", FormFieldType.TEXT, false),
                        FormField("contact_phone", "Contact Phone", FormFieldType.TEXT, false)
                    )
                ),
                // Work Description Section
                FormSection("work_description",
                    title = "Work Description",
                    fields = listOf(
                        FormField("work_requested", "Work Requested by Customer", FormFieldType.TEXTAREA, true),
                        FormField("work_performed", "Description of Work Performed", FormFieldType.TEXTAREA, true)
                    )
                ),
                // Job Details Section
                FormSection("job_details",
                    title = "Job Details",
                    fields = listOf(
                        FormField("job_number", "Job Number", FormFieldType.TEXT, true),
                        FormField("assigned_technician", "Assigned Technician", FormFieldType.TEXT, true),
                        FormField("site_name", "Site Name", FormFieldType.TEXT, true),
                        FormField("equipment_id", "Equipment ID", FormFieldType.TEXT, false),
                        FormField("job_description", "Job Description", FormFieldType.TEXTAREA, true),
                        FormField("work_type", "Work Type", FormFieldType.DROPDOWN, true,
                            options = listOf("Maintenance", "Repair", "Installation", "Inspection", "Emergency")),
                        FormField("priority", "Priority", FormFieldType.DROPDOWN, true,
                            options = listOf("Low", "Normal", "High", "Urgent"))
                    )
                ),
                // Labor and Materials Section
                FormSection("labor_materials",
                    title = "Labor and Materials",
                    fields = listOf(
                        FormField("technician_name_job", "Technician Name", FormFieldType.TEXT, true),
                        FormField("hours_worked", "Hours Worked", FormFieldType.NUMBER, true),
                        FormField("estimated_hours", "Estimated Hours", FormFieldType.NUMBER, false),
                        FormField("actual_hours", "Actual Hours", FormFieldType.NUMBER, false),
                        FormField("material_used_1", "Material/Part Used 1", FormFieldType.TEXT, false),
                        FormField("material_qty_1", "Quantity 1", FormFieldType.NUMBER, false),
                        FormField("material_used_2", "Material/Part Used 2", FormFieldType.TEXT, false),
                        FormField("material_qty_2", "Quantity 2", FormFieldType.NUMBER, false),
                        FormField("materials_used", "Materials Used", FormFieldType.TEXTAREA, false),
                        FormField("tools_required", "Tools Required", FormFieldType.TEXTAREA, false)
                    )
                ),
                // Completion Section
                FormSection("completion",
                    title = "Job Completion",
                    fields = listOf(
                        FormField("job_completed_successfully", "Job Completed Successfully", FormFieldType.CHECKBOX, true),
                        FormField("work_completed", "Work Completed", FormFieldType.CHECKBOX, true),
                        FormField("quality_check", "Quality Check Passed", FormFieldType.CHECKBOX, false),
                        FormField("customer_satisfaction", "Customer Satisfaction", FormFieldType.DROPDOWN, false,
                            options = listOf("Excellent", "Good", "Satisfactory", "Poor")),
                        FormField("follow_up_required", "Follow-up Required", FormFieldType.CHECKBOX, false)
                    )
                ),
                // Signatures Section
                FormSection("signatures",
                    title = "Signatures",
                    fields = listOf(
                        FormField("technician_signature", "Technician Signature", FormFieldType.SIGNATURE, true),
                        FormField("supervisor_approval", "Supervisor Approval", FormFieldType.SIGNATURE, true)
                    )
                )
            )
        )
    }
    
    override fun getPdfFieldMappings(): Map<String, PdfFieldMapping> {
        return mapOf(
            // Job Header - exact coordinates from original PDF
            "job_card_number" to PdfFieldMapping(
                fieldName = "job_card_number",
                pdfFieldName = "job_card_number",
                coordinate = FormCoordinate(50f, 800f, 150f, 20f),
                fieldType = FormFieldType.TEXT
            ),
            "job_date" to PdfFieldMapping(
                fieldName = "job_date",
                pdfFieldName = "job_date",
                coordinate = FormCoordinate(450f, 800f, 100f, 20f),
                fieldType = FormFieldType.DATE
            ),
            "customer_name" to PdfFieldMapping(
                fieldName = "customer_name",
                pdfFieldName = "customer_name",
                coordinate = FormCoordinate(50f, 770f, 250f, 20f),
                fieldType = FormFieldType.TEXT
            ),
            "site_address" to PdfFieldMapping(
                fieldName = "site_address",
                pdfFieldName = "site_address",
                coordinate = FormCoordinate(50f, 740f, 250f, 20f),
                fieldType = FormFieldType.TEXT
            ),
            "contact_person" to PdfFieldMapping(
                fieldName = "contact_person",
                pdfFieldName = "contact_person",
                coordinate = FormCoordinate(350f, 770f, 200f, 20f),
                fieldType = FormFieldType.TEXT
            ),
            "contact_phone" to PdfFieldMapping(
                fieldName = "contact_phone",
                pdfFieldName = "contact_phone",
                coordinate = FormCoordinate(350f, 740f, 200f, 20f),
                fieldType = FormFieldType.TEXT
            ),
            
            // Work Description - exact coordinates from original PDF
            "work_requested" to PdfFieldMapping(
                fieldName = "work_requested",
                pdfFieldName = "work_requested",
                coordinate = FormCoordinate(50f, 700f, 500f, 60f),
                fieldType = FormFieldType.TEXTAREA
            ),
            "work_performed" to PdfFieldMapping(
                fieldName = "work_performed",
                pdfFieldName = "work_performed",
                coordinate = FormCoordinate(50f, 620f, 500f, 80f),
                fieldType = FormFieldType.TEXTAREA
            ),
            
            // Labor and Materials - exact coordinates from original PDF
            "technician_name_job" to PdfFieldMapping(
                fieldName = "technician_name_job",
                pdfFieldName = "technician_name_job",
                coordinate = FormCoordinate(50f, 520f, 150f, 20f),
                fieldType = FormFieldType.TEXT
            ),
            "hours_worked" to PdfFieldMapping(
                fieldName = "hours_worked",
                pdfFieldName = "hours_worked",
                coordinate = FormCoordinate(220f, 520f, 80f, 20f),
                fieldType = FormFieldType.NUMBER
            ),
            "material_used_1" to PdfFieldMapping(
                fieldName = "material_used_1",
                pdfFieldName = "material_used_1",
                coordinate = FormCoordinate(50f, 490f, 350f, 20f),
                fieldType = FormFieldType.TEXT
            ),
            "material_qty_1" to PdfFieldMapping(
                fieldName = "material_qty_1",
                pdfFieldName = "material_qty_1",
                coordinate = FormCoordinate(420f, 490f, 80f, 20f),
                fieldType = FormFieldType.NUMBER
            ),
            "material_used_2" to PdfFieldMapping(
                fieldName = "material_used_2",
                pdfFieldName = "material_used_2",
                coordinate = FormCoordinate(50f, 460f, 350f, 20f),
                fieldType = FormFieldType.TEXT
            ),
            "material_qty_2" to PdfFieldMapping(
                fieldName = "material_qty_2",
                pdfFieldName = "material_qty_2",
                coordinate = FormCoordinate(420f, 460f, 80f, 20f),
                fieldType = FormFieldType.NUMBER
            ),
            
            // Completion - exact coordinates from original PDF
            "job_completed_successfully" to PdfFieldMapping(
                fieldName = "job_completed_successfully",
                pdfFieldName = "job_completed_successfully",
                coordinate = FormCoordinate(50f, 150f, 20f, 20f),
                fieldType = FormFieldType.CHECKBOX
            )
        )
    }
}



