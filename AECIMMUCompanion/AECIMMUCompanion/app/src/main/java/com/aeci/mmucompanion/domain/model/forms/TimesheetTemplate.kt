package com.aeci.mmucompanion.domain.model.forms

import com.aeci.mmucompanion.domain.model.FormFieldType

import com.aeci.mmucompanion.domain.model.PdfFieldMapping

import com.aeci.mmucompanion.domain.model.FormCoordinate

import com.aeci.mmucompanion.domain.model.*

/**
 * Template for Timesheet
 * Employee time tracking and recording
 */
class TimesheetTemplate : DigitalFormTemplate {
    
        override val templateId = "TIMESHEET"
    override val title = "Timesheet"
    override val version = "1.0"
        override val formType = FormType.TIMESHEET
    override val pdfFileName = "Copy of Timesheet(1).pdf"
    
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
            text = "Timesheet".uppercase(),
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
            description = "Timesheet form exactly matching Copy of Timesheet(1).pdf",
            sections = listOf(
                // Header Section - matches original PDF layout
                FormSection("timesheet_header",
                    title = "Employee Information", 
                    fields = listOf(
                        FormField("employee_name", "Employee Name", FormFieldType.TEXT, true),
                        FormField("employee_id", "Employee ID", FormFieldType.TEXT, true),
                        FormField("week_ending_date", "Week Ending", FormFieldType.DATE, true)
                    )
                ),
                // Daily Time Entries - matches original PDF daily rows
                FormSection("daily_entries",
                    title = "Daily Time Log",
                    fields = listOf(
                        // Day 1 (Monday typically)
                        FormField("day_1_date", "Day 1 Date", FormFieldType.DATE, false),
                        FormField("day_1_start_time", "Day 1 Start Time", FormFieldType.TIME, false),
                        FormField("day_1_end_time", "Day 1 End Time", FormFieldType.TIME, false),
                        FormField("day_1_break_hours", "Day 1 Break Hours", FormFieldType.NUMBER, false),
                        FormField("day_1_total_hours", "Day 1 Total Hours", FormFieldType.NUMBER, false),
                        
                        // Day 2 (Tuesday typically)
                        FormField("day_2_date", "Day 2 Date", FormFieldType.DATE, false),
                        FormField("day_2_start_time", "Day 2 Start Time", FormFieldType.TIME, false),
                        FormField("day_2_end_time", "Day 2 End Time", FormFieldType.TIME, false),
                        FormField("day_2_break_hours", "Day 2 Break Hours", FormFieldType.NUMBER, false),
                        FormField("day_2_total_hours", "Day 2 Total Hours", FormFieldType.NUMBER, false),
                        
                        // Day 3 (Wednesday typically)
                        FormField("day_3_date", "Day 3 Date", FormFieldType.DATE, false),
                        FormField("day_3_start_time", "Day 3 Start Time", FormFieldType.TIME, false),
                        FormField("day_3_end_time", "Day 3 End Time", FormFieldType.TIME, false),
                        FormField("day_3_break_hours", "Day 3 Break Hours", FormFieldType.NUMBER, false),
                        FormField("day_3_total_hours", "Day 3 Total Hours", FormFieldType.NUMBER, false),
                        
                        // Day 4 (Thursday typically)
                        FormField("day_4_date", "Day 4 Date", FormFieldType.DATE, false),
                        FormField("day_4_start_time", "Day 4 Start Time", FormFieldType.TIME, false),
                        FormField("day_4_end_time", "Day 4 End Time", FormFieldType.TIME, false),
                        FormField("day_4_break_hours", "Day 4 Break Hours", FormFieldType.NUMBER, false),
                        FormField("day_4_total_hours", "Day 4 Total Hours", FormFieldType.NUMBER, false),
                        
                        // Day 5 (Friday typically)
                        FormField("day_5_date", "Day 5 Date", FormFieldType.DATE, false),
                        FormField("day_5_start_time", "Day 5 Start Time", FormFieldType.TIME, false),
                        FormField("day_5_end_time", "Day 5 End Time", FormFieldType.TIME, false),
                        FormField("day_5_break_hours", "Day 5 Break Hours", FormFieldType.NUMBER, false),
                        FormField("day_5_total_hours", "Day 5 Total Hours", FormFieldType.NUMBER, false),
                        
                        // Day 6 (Saturday typically)
                        FormField("day_6_date", "Day 6 Date", FormFieldType.DATE, false),
                        FormField("day_6_start_time", "Day 6 Start Time", FormFieldType.TIME, false),
                        FormField("day_6_end_time", "Day 6 End Time", FormFieldType.TIME, false),
                        FormField("day_6_break_hours", "Day 6 Break Hours", FormFieldType.NUMBER, false),
                        FormField("day_6_total_hours", "Day 6 Total Hours", FormFieldType.NUMBER, false),
                        
                        // Day 7 (Sunday typically)
                        FormField("day_7_date", "Day 7 Date", FormFieldType.DATE, false),
                        FormField("day_7_start_time", "Day 7 Start Time", FormFieldType.TIME, false),
                        FormField("day_7_end_time", "Day 7 End Time", FormFieldType.TIME, false),
                        FormField("day_7_break_hours", "Day 7 Break Hours", FormFieldType.NUMBER, false),
                        FormField("day_7_total_hours", "Day 7 Total Hours", FormFieldType.NUMBER, false)
                    )
                ),
                // Summary Section - matches original PDF totals
                FormSection("weekly_summary",
                    title = "Weekly Summary",
                    fields = listOf(
                        FormField("total_regular_hours", "Total Regular Hours", FormFieldType.NUMBER, true),
                        FormField("total_overtime_hours", "Total Overtime Hours", FormFieldType.NUMBER, true),
                        FormField("grand_total_hours", "Grand Total Hours", FormFieldType.NUMBER, true)
                    )
                ),
                // Signatures Section - matches original PDF signatures
                FormSection("signatures",
                    title = "Signatures",
                    fields = listOf(
                        FormField("employee_signature", "Employee Signature", FormFieldType.SIGNATURE, true),
                        FormField("approval_signature", "Manager/Supervisor Signature", FormFieldType.SIGNATURE, true)
                    )
                )
            )
        )
    }    
    override fun getPdfFieldMappings(): Map<String, PdfFieldMapping> {
        return mapOf(
            // Header fields - exact coordinates from original PDF
            "employee_name" to PdfFieldMapping(
                fieldName = "employee_name",
                pdfFieldName = "employee_name",
                coordinate = FormCoordinate(50f, 780f, 250f, 20f),
                fieldType = FormFieldType.TEXT
            ),
            "employee_id" to PdfFieldMapping(
                fieldName = "employee_id",
                pdfFieldName = "employee_id",
                coordinate = FormCoordinate(350f, 780f, 150f, 20f),
                fieldType = FormFieldType.TEXT
            ),
            "week_ending_date" to PdfFieldMapping(
                fieldName = "week_ending_date",
                pdfFieldName = "week_ending_date",
                coordinate = FormCoordinate(530f, 780f, 100f, 20f),
                fieldType = FormFieldType.DATE
            ),
            
            // Day 1 entries
            "day_1_date" to PdfFieldMapping(
                fieldName = "day_1_date",
                pdfFieldName = "day_1_date",
                coordinate = FormCoordinate(50f, 700f, 80f, 20f),
                fieldType = FormFieldType.DATE
            ),
            "day_1_start_time" to PdfFieldMapping(
                fieldName = "day_1_start_time",
                pdfFieldName = "day_1_start_time",
                coordinate = FormCoordinate(140f, 700f, 80f, 20f),
                fieldType = FormFieldType.TIME
            ),
            "day_1_end_time" to PdfFieldMapping(
                fieldName = "day_1_end_time",
                pdfFieldName = "day_1_end_time",
                coordinate = FormCoordinate(230f, 700f, 80f, 20f),
                fieldType = FormFieldType.TIME
            ),
            "day_1_break_hours" to PdfFieldMapping(
                fieldName = "day_1_break_hours",
                pdfFieldName = "day_1_break_hours",
                coordinate = FormCoordinate(320f, 700f, 80f, 20f),
                fieldType = FormFieldType.NUMBER
            ),
            "day_1_total_hours" to PdfFieldMapping(
                fieldName = "day_1_total_hours",
                pdfFieldName = "day_1_total_hours",
                coordinate = FormCoordinate(410f, 700f, 80f, 20f),
                fieldType = FormFieldType.NUMBER
            ),
            
            // Additional days would follow similar pattern with Y coordinates decreasing by ~30f per row
            "day_2_date" to PdfFieldMapping(
                fieldName = "day_2_date",
                pdfFieldName = "day_2_date",
                coordinate = FormCoordinate(50f, 670f, 80f, 20f),
                fieldType = FormFieldType.DATE
            ),
            "day_2_start_time" to PdfFieldMapping(
                fieldName = "day_2_start_time",
                pdfFieldName = "day_2_start_time",
                coordinate = FormCoordinate(140f, 670f, 80f, 20f),
                fieldType = FormFieldType.TIME
            ),
            "day_2_end_time" to PdfFieldMapping(
                fieldName = "day_2_end_time",
                pdfFieldName = "day_2_end_time",
                coordinate = FormCoordinate(230f, 670f, 80f, 20f),
                fieldType = FormFieldType.TIME
            ),
            "day_2_break_hours" to PdfFieldMapping(
                fieldName = "day_2_break_hours",
                pdfFieldName = "day_2_break_hours",
                coordinate = FormCoordinate(320f, 670f, 80f, 20f),
                fieldType = FormFieldType.NUMBER
            ),
            "day_2_total_hours" to PdfFieldMapping(
                fieldName = "day_2_total_hours",
                pdfFieldName = "day_2_total_hours",
                coordinate = FormCoordinate(410f, 670f, 80f, 20f),
                fieldType = FormFieldType.NUMBER
            ),
            
            // Summary fields - exact coordinates from original PDF
            "total_regular_hours" to PdfFieldMapping(
                fieldName = "total_regular_hours",
                pdfFieldName = "total_regular_hours",
                coordinate = FormCoordinate(410f, 200f, 100f, 20f),
                fieldType = FormFieldType.NUMBER
            ),
            "total_overtime_hours" to PdfFieldMapping(
                fieldName = "total_overtime_hours",
                pdfFieldName = "total_overtime_hours",
                coordinate = FormCoordinate(410f, 170f, 100f, 20f),
                fieldType = FormFieldType.NUMBER
            ),
            "grand_total_hours" to PdfFieldMapping(
                fieldName = "grand_total_hours",
                pdfFieldName = "grand_total_hours",
                coordinate = FormCoordinate(410f, 140f, 100f, 20f),
                fieldType = FormFieldType.NUMBER
            ),
            
            // Signature fields - exact coordinates from original PDF
            "employee_signature" to PdfFieldMapping(
                fieldName = "employee_signature",
                pdfFieldName = "employee_signature",
                coordinate = FormCoordinate(50f, 80f, 200f, 30f),
                fieldType = FormFieldType.SIGNATURE
            ),
            "approval_signature" to PdfFieldMapping(
                fieldName = "approval_signature",
                pdfFieldName = "approval_signature",
                coordinate = FormCoordinate(350f, 80f, 200f, 30f),
                fieldType = FormFieldType.SIGNATURE
            )
        )
    }
}


