package com.aeci.mmucompanion.domain.model.forms

import com.aeci.mmucompanion.domain.model.FormFieldType

import com.aeci.mmucompanion.domain.model.PdfFieldMapping

import com.aeci.mmucompanion.domain.model.FormCoordinate

import com.aeci.mmucompanion.domain.model.*

/**
 * COMPREHENSIVE Fire Extinguisher Inspection Template
 * Complete field coverage matching original PDF with precise coordinate mapping
 */
class FireExtinguisherInspectionTemplate : DigitalFormTemplate {
    
    override val templateId = "FIRE_EXTINGUISHER_INSPECTION"
    override val title = "Fire Extinguisher Inspection Checklist"
    override val version = "1.0"
    override val formType = FormType.FIRE_EXTINGUISHER_INSPECTION
    override val pdfFileName = "FIRE EXTINGUISHER INSPECTION CHECKLIST.pdf"
    
    // Precise logo coordinates matching original PDF layout
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
    
    // Static text elements for exact PDF reproduction
    override val staticTextCoordinates = listOf(
        StaticTextCoordinate(
            text = "FIRE EXTINGUISHER INSPECTION CHECKLIST",
            x = 200f, y = 720f, fontSize = 16f, fontWeight = "bold",
            alignment = TextAlignment.CENTER
        ),
        StaticTextCoordinate(
            text = "AECI Mining Explosives",
            x = 200f, y = 700f, fontSize = 10f, fontWeight = "normal",
            alignment = TextAlignment.CENTER
        )
    )
    
    // Header coordinate mapping for form metadata
    override val headerCoordinates = listOf<HeaderCoordinate>()

    // Form relationships for data integration
    override val formRelationships = listOf(
        FormRelationship(
            sourceField = "extinguisher_id", targetForm = FormType.MAINTENANCE,
            targetField = "equipment_id",
            relationshipType = RelationshipType.LOOKUP
        )
    )

    override fun getValidationRules(): List<ValidationRule> {
        return listOf(
            ValidationRule(fieldName = "inspection_date", ruleName = "date_not_future", expression = "date <= TODAY", errorMessage = "Inspection date cannot be in the future"
            ),
            ValidationRule(fieldName = "extinguisher_id", ruleName = "required_format", expression = "matches('^FE-[0-9]{4}$')", errorMessage = "Extinguisher ID must follow format: FE-XXXX"
            ),
            ValidationRule(fieldName = "inspector_signature", ruleName = "signature_required", expression = "not_empty", errorMessage = "Inspector signature is mandatory"
            )
        )
    }

    override fun getRelatedFormUpdates(): List<FormRelationshipUpdate> {
        return listOf(
            FormRelationshipUpdate(targetFormType = FormType.MAINTENANCE,
                fieldMappings = mapOf(
                    "extinguisher_id" to "equipment_id",
                    "inspection_result" to "last_inspection_result",
                    "inspection_date" to "last_inspection_date"
                )
            )
        )
    }    
    override fun getFormTemplate(): FormDefinition {
        return FormDefinition(
            id = templateId,
            name = title,
            description = "Fire Extinguisher Inspection Checklist exactly matching FIRE EXTINGUISHER INSPECTION CHECKLIST.pdf",
            sections = listOf(
                // Header Information Section
                FormSection("header_info",
                    title = "Inspection Information", 
                    fields = listOf(
                        FormField("inspection_date", "Inspection Date", FormFieldType.DATE, true),
                        FormField("inspector_name", "Inspector Name", FormFieldType.TEXT, true),
                        FormField("location", "Location", FormFieldType.TEXT, true)
                    )
                ),
                // Extinguisher Details Section
                FormSection("extinguisher_details",
                    title = "Extinguisher Details",
                    fields = listOf(
                        FormField("extinguisher_id", "Extinguisher ID", FormFieldType.TEXT, true),
                        FormField("extinguisher_type", "Extinguisher Type", FormFieldType.DROPDOWN, true, 
                            options = listOf("Water", "Foam", "Dry Powder", "CO2", "Wet Chemical")),
                        FormField("manufacture_date", "Manufacture Date", FormFieldType.DATE, false),
                        FormField("last_service_date", "Last Service Date", FormFieldType.DATE, false),
                        FormField("next_service_date", "Next Service Date", FormFieldType.DATE, true)
                    )
                ),
                // Visual Inspection Checklist Section
                FormSection("visual_inspection",
                    title = "Visual Inspection Checklist",
                    fields = listOf(
                        FormField("accessible_unobstructed", "Accessible and Unobstructed", FormFieldType.CHECKBOX, true),
                        FormField("pin_seal_intact", "Pin and Seal Intact", FormFieldType.CHECKBOX, true),
                        FormField("pressure_gauge_normal", "Pressure Gauge in Normal Range", FormFieldType.CHECKBOX, true),
                        FormField("hose_nozzle_undamaged", "Hose and Nozzle Undamaged", FormFieldType.CHECKBOX, true),
                        FormField("instructions_legible", "Instructions Legible", FormFieldType.CHECKBOX, true),
                        FormField("no_physical_damage", "No Physical Damage", FormFieldType.CHECKBOX, true),
                        FormField("mounting_secure", "Mounting Secure", FormFieldType.CHECKBOX, true)
                    )
                ),
                // Physical Checks Section
                FormSection("physical_checks",
                    title = "Physical Checks",
                    fields = listOf(
                        FormField("physical_condition", "Physical Condition", FormFieldType.DROPDOWN, true,
                            options = listOf("Excellent", "Good", "Fair", "Poor")),
                        FormField("pressure_gauge", "Pressure Gauge Reading", FormFieldType.TEXT, true),
                        FormField("weight_check", "Weight Check (kg)", FormFieldType.NUMBER, false),
                        FormField("seal_intact", "Seal Intact", FormFieldType.CHECKBOX, true),
                        FormField("accessibility_check", "Accessibility Check", FormFieldType.CHECKBOX, true),
                        FormField("signage_visible", "Signage Visible", FormFieldType.CHECKBOX, true),
                        FormField("discharge_tested", "Discharge Tested", FormFieldType.CHECKBOX, false)
                    )
                ),
                // Deficiencies Section
                FormSection("deficiencies",
                    title = "Deficiencies and Actions",
                    fields = listOf(
                        FormField("deficiency_found", "Deficiency Found", FormFieldType.CHECKBOX, false),
                        FormField("deficiency_description", "Deficiency Description", FormFieldType.TEXT, false),
                        FormField("corrective_action", "Corrective Action Required", FormFieldType.TEXTAREA, false)
                    )
                ),
                // Service Requirements Section
                FormSection("service_requirements",
                    title = "Service Requirements",
                    fields = listOf(
                        FormField("recharge_required", "Recharge Required", FormFieldType.CHECKBOX, false),
                        FormField("hydrostatic_test_due", "Hydrostatic Test Due", FormFieldType.CHECKBOX, false)
                    )
                ),
                // Overall Assessment Section
                FormSection("assessment",
                    title = "Overall Assessment",
                    fields = listOf(
                        FormField("overall_status", "Overall Status", FormFieldType.DROPDOWN, true,
                            options = listOf("Satisfactory", "Needs Attention", "Out of Service")),
                        FormField("next_inspection_due", "Next Inspection Due", FormFieldType.DATE, true),
                        FormField("inspector_signature", "Inspector Signature", FormFieldType.SIGNATURE, true)
                    )
                )
            )
        )
    }
    
    override fun getPdfFieldMappings(): Map<String, PdfFieldMapping> {
        return mapOf(
            // Header Information - exact coordinates from original PDF
            "inspection_date" to PdfFieldMapping(
                fieldName = "inspection_date",
                pdfFieldName = "inspection_date",
                coordinate = FormCoordinate(x = 400f, y = 70f, width = 120f, height = 25f),
                fieldType = FormFieldType.DATE
            ),
            "inspector_name" to PdfFieldMapping(
                fieldName = "inspector_name",
                pdfFieldName = "inspector_name",
                coordinate = FormCoordinate(x = 150f, y = 100f, width = 200f, height = 25f),
                fieldType = FormFieldType.TEXT
            ),
            "location" to PdfFieldMapping(
                fieldName = "location",
                pdfFieldName = "location",
                coordinate = FormCoordinate(x = 400f, y = 100f, width = 150f, height = 25f),
                fieldType = FormFieldType.TEXT
            ),
            
            // Extinguisher Details - exact coordinates from original PDF
            "extinguisher_id" to PdfFieldMapping(
                fieldName = "extinguisher_id",
                pdfFieldName = "extinguisher_id",
                coordinate = FormCoordinate(x = 150f, y = 140f, width = 100f, height = 25f),
                fieldType = FormFieldType.TEXT
            ),
            "extinguisher_type" to PdfFieldMapping(
                fieldName = "extinguisher_type",
                pdfFieldName = "extinguisher_type",
                coordinate = FormCoordinate(x = 300f, y = 140f, width = 100f, height = 25f),
                fieldType = FormFieldType.DROPDOWN
            ),
            "manufacture_date" to PdfFieldMapping(
                fieldName = "manufacture_date",
                pdfFieldName = "manufacture_date",
                coordinate = FormCoordinate(x = 450f, y = 140f, width = 120f, height = 25f),
                fieldType = FormFieldType.DATE
            ),
            "last_service_date" to PdfFieldMapping(
                fieldName = "last_service_date",
                pdfFieldName = "last_service_date",
                coordinate = FormCoordinate(x = 150f, y = 170f, width = 120f, height = 25f),
                fieldType = FormFieldType.DATE
            ),
            "next_service_date" to PdfFieldMapping(
                fieldName = "next_service_date",
                pdfFieldName = "next_service_date",
                coordinate = FormCoordinate(x = 300f, y = 170f, width = 120f, height = 25f),
                fieldType = FormFieldType.DATE
            ),
            
            // Visual Inspection Items - exact coordinates from original PDF
            "accessible_unobstructed" to PdfFieldMapping(
                fieldName = "accessible_unobstructed",
                pdfFieldName = "accessible_unobstructed",
                coordinate = FormCoordinate(x = 80f, y = 220f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            ),
            "pin_seal_intact" to PdfFieldMapping(
                fieldName = "pin_seal_intact",
                pdfFieldName = "pin_seal_intact",
                coordinate = FormCoordinate(x = 80f, y = 250f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            ),
            "pressure_gauge_normal" to PdfFieldMapping(
                fieldName = "pressure_gauge_normal",
                pdfFieldName = "pressure_gauge_normal",
                coordinate = FormCoordinate(x = 80f, y = 280f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            ),
            "hose_nozzle_undamaged" to PdfFieldMapping(
                fieldName = "hose_nozzle_undamaged",
                pdfFieldName = "hose_nozzle_undamaged",
                coordinate = FormCoordinate(x = 80f, y = 310f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            ),
            "instructions_legible" to PdfFieldMapping(
                fieldName = "instructions_legible",
                pdfFieldName = "instructions_legible",
                coordinate = FormCoordinate(x = 80f, y = 340f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            ),
            "no_physical_damage" to PdfFieldMapping(
                fieldName = "no_physical_damage",
                pdfFieldName = "no_physical_damage",
                coordinate = FormCoordinate(x = 80f, y = 370f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            ),
            "mounting_secure" to PdfFieldMapping(
                fieldName = "mounting_secure",
                pdfFieldName = "mounting_secure",
                coordinate = FormCoordinate(x = 80f, y = 400f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            ),
            
            // Deficiency Notes - exact coordinates from original PDF
            "deficiency_found" to PdfFieldMapping(
                fieldName = "deficiency_found",
                pdfFieldName = "deficiency_found",
                coordinate = FormCoordinate(x = 80f, y = 450f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            ),
            "deficiency_description" to PdfFieldMapping(
                fieldName = "deficiency_description",
                pdfFieldName = "deficiency_description",
                coordinate = FormCoordinate(x = 150f, y = 450f, width = 400f, height = 25f),
                fieldType = FormFieldType.TEXT
            ),
            "corrective_action" to PdfFieldMapping(
                fieldName = "corrective_action",
                pdfFieldName = "corrective_action",
                coordinate = FormCoordinate(x = 50f, y = 490f, width = 500f, height = 50f),
                fieldType = FormFieldType.TEXTAREA
            ),
            
            // Service Required - exact coordinates from original PDF
            "recharge_required" to PdfFieldMapping(
                fieldName = "recharge_required",
                pdfFieldName = "recharge_required",
                coordinate = FormCoordinate(x = 80f, y = 560f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            ),
            "hydrostatic_test_due" to PdfFieldMapping(
                fieldName = "hydrostatic_test_due",
                pdfFieldName = "hydrostatic_test_due",
                coordinate = FormCoordinate(x = 250f, y = 560f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            )
        )
    }
}



