package com.aeci.mmucompanion.domain.model.forms

import com.aeci.mmucompanion.domain.model.FormFieldType

import com.aeci.mmucompanion.domain.model.PdfFieldMapping

import com.aeci.mmucompanion.domain.model.FormCoordinate

import com.aeci.mmucompanion.domain.model.*

/**
 * COMPREHENSIVE 90 Day Pump System Inspection Template
 * Complete field coverage matching original PDF with precise coordinate mapping
 */
class PumpInspection90DayTemplate : DigitalFormTemplate {
    
    override val templateId = "PUMP_INSPECTION_90_DAY" 
    override val title = "90 Day Pump System Inspection"
    override val version = "1.0"
    override val formType = FormType.PUMP_INSPECTION_90_DAY
    override val pdfFileName = "90 DAY PUMP SYSTEM INSPECTION CHECKLIST.pdf"
    
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
            text = "90 DAY PUMP SYSTEM INSPECTION CHECKLIST",
            x = 200f, y = 720f, fontSize = 16f, fontWeight = "bold",
            alignment = TextAlignment.CENTER
        )
    )
    
    override val headerCoordinates = listOf<HeaderCoordinate>()

    override val formRelationships = listOf(
        FormRelationship(
            sourceField = "pump_serial_number", targetForm = FormType.MAINTENANCE,
            targetField = "equipment_id",
            relationshipType = RelationshipType.LOOKUP
        )
    )

    override fun getValidationRules(): List<ValidationRule> {
        return listOf(
            ValidationRule(fieldName = "inspection_date", ruleName = "date_validation", expression = "date <= TODAY", errorMessage = "Inspection date cannot be in the future"
            ),
            ValidationRule(fieldName = "pump_serial_number", ruleName = "required_field", expression = "not_empty", errorMessage = "Pump serial number is required"
            )
        )
    }

    override fun getRelatedFormUpdates(): List<FormRelationshipUpdate> {
        return listOf(
            FormRelationshipUpdate(targetFormType = FormType.MAINTENANCE,
                fieldMappings = mapOf(
                    "pump_serial_number" to "equipment_id",
                    "inspection_date" to "last_inspection_date"
                )
            )
        )
    }    
    override fun getFormTemplate(): FormDefinition {
        return FormDefinition(
            id = templateId,
            name = title,
            description = "90 Day Pump System Inspection exactly matching 90 DAY PUMP SYSTEM INSPECTION CHECKLIST.pdf",
            sections = listOf(
                // Header Section - matches original PDF layout
                FormSection("inspection_header",
                    title = "Inspection Information", 
                    fields = listOf(
                        FormField("inspection_date", "Inspection Date", FormFieldType.DATE, true),
                        FormField("inspector_name", "Inspector Name", FormFieldType.TEXT, true),
                        FormField("equipment_id", "Equipment ID", FormFieldType.TEXT, true),
                        FormField("serial_number", "Serial Number", FormFieldType.TEXT, true),
                        FormField("pump_location", "Pump Location", FormFieldType.TEXT, true),
                        FormField("service_hours", "Service Hours", FormFieldType.NUMBER, true)
                    )
                ),
                // Visual Inspection Section - comprehensive checklist
                FormSection("visual_inspection",
                    title = "Visual Inspection Checklist",
                    fields = listOf(
                        // Pump Housing
                        FormField("pump_housing_satisfactory", "Pump Housing - Satisfactory", FormFieldType.CHECKBOX, false),
                        FormField("pump_housing_defective", "Pump Housing - Defective", FormFieldType.CHECKBOX, false),
                        FormField("pump_housing_comments", "Pump Housing Comments", FormFieldType.TEXT, false),
                        
                        // Coupling
                        FormField("coupling_satisfactory", "Coupling - Satisfactory", FormFieldType.CHECKBOX, false),
                        FormField("coupling_defective", "Coupling - Defective", FormFieldType.CHECKBOX, false),
                        FormField("coupling_comments", "Coupling Comments", FormFieldType.TEXT, false),
                        
                        // Motor
                        FormField("motor_satisfactory", "Motor - Satisfactory", FormFieldType.CHECKBOX, false),
                        FormField("motor_defective", "Motor - Defective", FormFieldType.CHECKBOX, false),
                        FormField("motor_comments", "Motor Comments", FormFieldType.TEXT, false),
                        
                        // Piping
                        FormField("piping_satisfactory", "Piping - Satisfactory", FormFieldType.CHECKBOX, false),
                        FormField("piping_defective", "Piping - Defective", FormFieldType.CHECKBOX, false),
                        FormField("piping_comments", "Piping Comments", FormFieldType.TEXT, false),
                        
                        // Lubrication
                        FormField("lubrication_satisfactory", "Lubrication - Satisfactory", FormFieldType.CHECKBOX, false),
                        FormField("lubrication_defective", "Lubrication - Defective", FormFieldType.CHECKBOX, false),
                        FormField("lubrication_comments", "Lubrication Comments", FormFieldType.TEXT, false)
                    )
                ),
                // Inspection Details Section  
                FormSection("inspection_details",
                    title = "Inspection Details",
                    fields = listOf(
                        FormField("site_name", "Site Name", FormFieldType.TEXT, true),
                        FormField("pump_serial_number", "Pump Serial Number", FormFieldType.TEXT, true),
                        FormField("equipment_location", "Equipment Location", FormFieldType.TEXT, true),
                        FormField("last_inspection_date", "Last Inspection Date", FormFieldType.DATE, false),
                        FormField("next_inspection_due", "Next Inspection Due", FormFieldType.DATE, true)
                    )
                ),
                // Pressure Testing Section
                FormSection("pressure_testing",
                    title = "Pressure Testing",
                    fields = listOf(
                        FormField("pressure_test_type_1", "Pressure Test Type 1", FormFieldType.TEXT, false),
                        FormField("test_pressure_1", "Test Pressure 1", FormFieldType.NUMBER, false),
                        FormField("pressure_test_passed_1", "Pressure Test 1 Passed", FormFieldType.CHECKBOX, false),
                        FormField("pressure_test_notes_1", "Pressure Test 1 Notes", FormFieldType.TEXT, false),
                        
                        FormField("pressure_test_type_2", "Pressure Test Type 2", FormFieldType.TEXT, false),
                        FormField("test_pressure_2", "Test Pressure 2", FormFieldType.NUMBER, false),
                        FormField("pressure_test_passed_2", "Pressure Test 2 Passed", FormFieldType.CHECKBOX, false),
                        FormField("pressure_test_notes_2", "Pressure Test 2 Notes", FormFieldType.TEXT, false)
                    )
                ),
                // Overall Assessment Section
                FormSection("assessment",
                    title = "Overall Assessment",
                    fields = listOf(
                        FormField("overall_status", "Overall Status", FormFieldType.DROPDOWN, true,
                            options = listOf("Satisfactory", "Needs Attention", "Requires Immediate Action")),
                        FormField("recommended_action_1", "Recommended Action 1", FormFieldType.TEXT, false),
                        FormField("recommended_priority_1", "Priority 1", FormFieldType.DROPDOWN, false,
                            options = listOf("Low", "Medium", "High", "Critical")),
                        FormField("recommended_due_date_1", "Due Date 1", FormFieldType.DATE, false),
                        
                        FormField("recommended_action_2", "Recommended Action 2", FormFieldType.TEXT, false),
                        FormField("recommended_priority_2", "Priority 2", FormFieldType.DROPDOWN, false,
                            options = listOf("Low", "Medium", "High", "Critical")),
                        FormField("recommended_due_date_2", "Due Date 2", FormFieldType.DATE, false)
                    )
                ),
                // Signatures Section
                FormSection("signatures",
                    title = "Signatures",
                    fields = listOf(
                        FormField("inspector_signature", "Inspector Signature", FormFieldType.SIGNATURE, true),
                        FormField("supervisor_approval", "Supervisor Approval", FormFieldType.SIGNATURE, true)
                    )
                )
            )
        )
    }
    
    override fun getPdfFieldMappings(): Map<String, PdfFieldMapping> {
        return mapOf(
            // Header Section - exact coordinates from original PDF
            "inspection_date" to PdfFieldMapping(
                fieldName = "inspection_date",
                pdfFieldName = "inspection_date",
                coordinate = FormCoordinate(x = 450f, y = 85f, width = 120f, height = 25f),
                fieldType = FormFieldType.DATE
            ),
            "inspector_name" to PdfFieldMapping(
                fieldName = "inspector_name",
                pdfFieldName = "inspector_name",
                coordinate = FormCoordinate(x = 150f, y = 110f, width = 200f, height = 25f),
                fieldType = FormFieldType.TEXT
            ),
            "equipment_id" to PdfFieldMapping(
                fieldName = "equipment_id",
                pdfFieldName = "equipment_id",
                coordinate = FormCoordinate(x = 450f, y = 110f, width = 150f, height = 25f),
                fieldType = FormFieldType.TEXT
            ),
            "serial_number" to PdfFieldMapping(
                fieldName = "serial_number",
                pdfFieldName = "serial_number",
                coordinate = FormCoordinate(x = 150f, y = 135f, width = 200f, height = 25f),
                fieldType = FormFieldType.TEXT
            ),
            "pump_location" to PdfFieldMapping(
                fieldName = "pump_location",
                pdfFieldName = "pump_location",
                coordinate = FormCoordinate(x = 450f, y = 135f, width = 150f, height = 25f),
                fieldType = FormFieldType.TEXT
            ),
            "service_hours" to PdfFieldMapping(
                fieldName = "service_hours",
                pdfFieldName = "service_hours",
                coordinate = FormCoordinate(x = 150f, y = 160f, width = 100f, height = 25f),
                fieldType = FormFieldType.NUMBER
            ),
            
            // Visual Inspection Section - exact coordinates from original PDF
            "pump_housing_satisfactory" to PdfFieldMapping(
                fieldName = "pump_housing_satisfactory",
                pdfFieldName = "pump_housing_satisfactory",
                coordinate = FormCoordinate(x = 50f, y = 200f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            ),
            "pump_housing_defective" to PdfFieldMapping(
                fieldName = "pump_housing_defective",
                pdfFieldName = "pump_housing_defective",
                coordinate = FormCoordinate(x = 80f, y = 200f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            ),
            "pump_housing_comments" to PdfFieldMapping(
                fieldName = "pump_housing_comments",
                pdfFieldName = "pump_housing_comments",
                coordinate = FormCoordinate(x = 350f, y = 200f, width = 200f, height = 25f),
                fieldType = FormFieldType.TEXT
            ),
            
            "coupling_satisfactory" to PdfFieldMapping(
                fieldName = "coupling_satisfactory",
                pdfFieldName = "coupling_satisfactory",
                coordinate = FormCoordinate(x = 50f, y = 230f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            ),
            "coupling_defective" to PdfFieldMapping(
                fieldName = "coupling_defective",
                pdfFieldName = "coupling_defective",
                coordinate = FormCoordinate(x = 80f, y = 230f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            ),
            "coupling_comments" to PdfFieldMapping(
                fieldName = "coupling_comments",
                pdfFieldName = "coupling_comments",
                coordinate = FormCoordinate(x = 350f, y = 230f, width = 200f, height = 25f),
                fieldType = FormFieldType.TEXT
            ),
            
            "motor_satisfactory" to PdfFieldMapping(
                fieldName = "motor_satisfactory",
                pdfFieldName = "motor_satisfactory",
                coordinate = FormCoordinate(x = 50f, y = 260f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            ),
            "motor_defective" to PdfFieldMapping(
                fieldName = "motor_defective",
                pdfFieldName = "motor_defective",
                coordinate = FormCoordinate(x = 80f, y = 260f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            ),
            "motor_comments" to PdfFieldMapping(
                fieldName = "motor_comments",
                pdfFieldName = "motor_comments",
                coordinate = FormCoordinate(x = 350f, y = 260f, width = 200f, height = 25f),
                fieldType = FormFieldType.TEXT
            ),
            
            "piping_satisfactory" to PdfFieldMapping(
                fieldName = "piping_satisfactory",
                pdfFieldName = "piping_satisfactory",
                coordinate = FormCoordinate(x = 50f, y = 290f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            ),
            "piping_defective" to PdfFieldMapping(
                fieldName = "piping_defective",
                pdfFieldName = "piping_defective",
                coordinate = FormCoordinate(x = 80f, y = 290f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            ),
            "piping_comments" to PdfFieldMapping(
                fieldName = "piping_comments",
                pdfFieldName = "piping_comments",
                coordinate = FormCoordinate(x = 350f, y = 290f, width = 200f, height = 25f),
                fieldType = FormFieldType.TEXT
            ),
            
            "lubrication_satisfactory" to PdfFieldMapping(
                fieldName = "lubrication_satisfactory",
                pdfFieldName = "lubrication_satisfactory",
                coordinate = FormCoordinate(x = 50f, y = 320f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            ),
            "lubrication_defective" to PdfFieldMapping(
                fieldName = "lubrication_defective",
                pdfFieldName = "lubrication_defective",
                coordinate = FormCoordinate(x = 80f, y = 320f, width = 20f, height = 20f),
                fieldType = FormFieldType.CHECKBOX
            ),
            "lubrication_comments" to PdfFieldMapping(
                fieldName = "lubrication_comments",
                pdfFieldName = "lubrication_comments",
                coordinate = FormCoordinate(x = 350f, y = 320f, width = 200f, height = 25f),
                fieldType = FormFieldType.TEXT
            )
        )
    }
}



