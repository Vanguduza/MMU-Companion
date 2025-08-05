package com.aeci.mmucompanion.data.templates

import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.domain.model.forms.*
import java.time.LocalDateTime

object FormTemplates {
    private val now = LocalDateTime.now()
    
    // Digital form templates - COMPREHENSIVE system with ALL forms
    private val digitalTemplates = mapOf(
        // FULLY COMPREHENSIVE (16/16) - With complete field mapping including logos
        FormType.UOR_REPORT to UORTemplate(),
        // FormType.TIMESHEET to TimesheetTemplate(), // TODO: Create TimesheetTemplate 
        FormType.BLAST_HOLE_LOG to BlastHoleLogTemplate(),
        FormType.MMU_QUALITY_REPORT to MmuQualityReportTemplate(),
        FormType.MMU_DAILY_LOG to MmuProductionDailyLogTemplate(),
        FormType.PUMP_INSPECTION_90_DAY to PumpInspection90DayTemplate(),
        FormType.BOWIE_PUMP_WEEKLY to BowiePumpWeeklyCheckTemplate(),
        FormType.FIRE_EXTINGUISHER_INSPECTION to FireExtinguisherInspectionTemplate(),
        // FormType.JOB_CARD to JobCardTemplate(), // TODO: Create JobCardTemplate
        FormType.MMU_CHASSIS_MAINTENANCE to MmuChassisMaintenanceTemplate(),
        FormType.MMU_HANDOVER_CERTIFICATE to MmuHandoverCertificateTemplate(),
        FormType.MONTHLY_PROCESS_MAINTENANCE to MonthlyProcessMaintenanceTemplate(),
        FormType.ON_BENCH_MMU_INSPECTION to OnBenchMmuInspectionTemplate(),
        FormType.PC_PUMP_PRESSURE_TRIP_TEST to PcPumpPressureTripTestTemplate(),
        FormType.PRE_TASK_SAFETY_ASSESSMENT to PreTaskSafetyAssessmentTemplate(),
        FormType.AVAILABILITY_UTILIZATION to AvailabilityUtilizationTemplate()
    )
    
    fun getFormTemplate(type: FormType): FormTemplate? {
        // First try to get from new digital template system
        digitalTemplates[type]?.let { digitalTemplate ->
            val templateDef = digitalTemplate.getFormTemplate()
            return FormTemplate(
                id = digitalTemplate.templateId,
                name = digitalTemplate.title,
                description = templateDef.description,
                formType = digitalTemplate.formType,
                templateFile = digitalTemplate.pdfFileName,
                pdfTemplate = digitalTemplate.pdfFileName,
                fieldMappings = emptyList(), // TODO: Convert from coordinates
                version = digitalTemplate.version,
                sections = templateDef.sections,
                fields = templateDef.sections.flatMap { section -> section.fields },
                createdAt = now,
                updatedAt = now
            )
        }
        
        // Fall back to legacy templates for other forms
        return all[getTemplateKey(type)]
    }
    
    fun getAllTemplates(): List<FormTemplate> {
        val digitalForms = digitalTemplates.values.map { digitalTemplate ->
            val templateDef = digitalTemplate.getFormTemplate()
            FormTemplate(
                id = digitalTemplate.templateId,
                name = digitalTemplate.title,
                description = templateDef.description,
                formType = digitalTemplate.formType,
                templateFile = digitalTemplate.pdfFileName,
                pdfTemplate = digitalTemplate.pdfFileName,
                fieldMappings = emptyList(),
                version = digitalTemplate.version,
                sections = templateDef.sections,
                fields = templateDef.sections.flatMap { section -> section.fields },
                createdAt = now,
                updatedAt = now
            )
        }
        
        return digitalForms + all.values.toList()
    }
    
    private fun getTemplateKey(type: FormType): String {
        return when(type) {
            FormType.MAINTENANCE -> "maintenance_form"
            FormType.INSPECTION -> "inspection_form"
            FormType.PUMP_90_DAY_INSPECTION -> "pump_inspection"
            else -> type.name.lowercase()
        }
    }
    
    val all: Map<String, FormTemplate> = mapOf(
        // Basic Form Templates
        "maintenance_form" to FormTemplate(
            id = "maintenance_form",
            name = "Maintenance Report",
            description = "Equipment maintenance reporting form",
            formType = FormType.MAINTENANCE,
            templateFile = "templates/maintenance_form.json",
            pdfTemplate = null, // Generated programmatically
            fieldMappings = emptyList(),
            version = "1.0",
            sections = listOf(
                FormSection(
                    id = "equipment_info",
                    title = "Equipment Information",
                    fields = listOf(
                        FormField(fieldName = "equipment_id", fieldType = FormFieldType.TEXT, label = "Equipment ID", isRequired = true),
                        FormField(fieldName = "equipment_name", fieldType = FormFieldType.TEXT, label = "Equipment Name", isRequired = true),
                        FormField(fieldName = "maintenance_date", fieldType = FormFieldType.DATE, label = "Maintenance Date", isRequired = true),
                        FormField(fieldName = "technician_name", fieldType = FormFieldType.TEXT, label = "Technician Name", isRequired = true),
                        FormField(fieldName = "work_description", fieldType = FormFieldType.MULTILINE_TEXT, label = "Work Description", isRequired = true),
                        FormField(fieldName = "parts_used", fieldType = FormFieldType.MULTILINE_TEXT, label = "Parts Used", isRequired = false),
                        FormField(fieldName = "labor_hours", fieldType = FormFieldType.NUMBER, label = "Labor Hours", isRequired = true),
                        FormField(fieldName = "notes", fieldType = FormFieldType.MULTILINE_TEXT, label = "Notes", isRequired = false)
                    )
                )
            ),
            fields = listOf(),
            createdAt = now,
            updatedAt = now
        ),
        "inspection_form" to FormTemplate(
            id = "inspection_form",
            name = "Inspection Report",
            description = "Equipment inspection reporting form",
            formType = FormType.INSPECTION,
            templateFile = "templates/inspection_form.json",
            pdfTemplate = null, // Generated programmatically
            fieldMappings = emptyList(),
            version = "1.0",
            sections = listOf(
                FormSection(id = "Inspection Details", title = "Inspection Details",
                    fields = listOf(
                        FormField(fieldName = "equipment_id", fieldType = FormFieldType.TEXT, label = "Equipment ID", isRequired = true),
                        FormField(fieldName = "equipment_name", fieldType = FormFieldType.TEXT, label = "Equipment Name", isRequired = true),
                        FormField(fieldName = "inspection_date", fieldType = FormFieldType.DATE, label = "Inspection Date", isRequired = true),
                        FormField(fieldName = "inspector_name", fieldType = FormFieldType.TEXT, label = "Inspector Name", isRequired = true),
                        FormField(fieldName = "inspection_type", fieldType = FormFieldType.DROPDOWN, label = "Inspection Type", isRequired = true),
                        FormField(fieldName = "findings", fieldType = FormFieldType.MULTILINE_TEXT, label = "Findings", isRequired = true),
                        FormField(fieldName = "recommendations", fieldType = FormFieldType.MULTILINE_TEXT, label = "Recommendations", isRequired = false),
                        FormField(fieldName = "next_inspection_date", fieldType = FormFieldType.DATE, label = "Next Inspection Date", isRequired = false)
                    )
                )
            ),
            fields = listOf(),
            createdAt = now,
            updatedAt = now
        ),
        // Pump Inspection Checklist - COMPREHENSIVE from PDF coordinate maps
        "pump_inspection" to FormTemplate(
            id = "pump_inspection",
            name = "90 Day Pump System Inspection Checklist",
            description = "Comprehensive 90 day pump system inspection based on 90 DAY PUMP SYSTEM INSPECTION CHECKLIST.pdf with ALL fields from PDF coordinate maps",
            formType = FormType.PUMP_90_DAY_INSPECTION,
            templateFile = "templates/pump_inspection.json",
            pdfTemplate = "90 DAY PUMP SYSTEM INSPECTION CHECKLIST.pdf",
            fieldMappings = emptyList(),
            version = "2.0",
            sections = listOf(
                FormSection(id = "Inspection Header", title = "Inspection Header",
                    fields = listOf(
                        FormField(fieldName = "inspection_date", fieldType = FormFieldType.DATE, label = "Inspection Date", isRequired = true),
                        FormField(fieldName = "site", fieldType = FormFieldType.TEXT, label = "Site", isRequired = true, isReadOnly = true),
                        FormField(fieldName = "pump_id", fieldType = FormFieldType.TEXT, label = "Pump ID", isRequired = true),
                        FormField(fieldName = "pump_serial_number", fieldType = FormFieldType.TEXT, label = "Pump Serial Number", isRequired = true),
                        FormField(fieldName = "inspector_name", fieldType = FormFieldType.TEXT, label = "Inspector Name", isRequired = true),
                        FormField(fieldName = "pump_type", fieldType = FormFieldType.TEXT, label = "Pump Type", isRequired = true),
                        FormField(fieldName = "location", fieldType = FormFieldType.TEXT, label = "Location", isRequired = true),
                        FormField(fieldName = "operating_hours", fieldType = FormFieldType.NUMBER, label = "Operating Hours", isRequired = true),
                        FormField(fieldName = "last_inspection_date", fieldType = FormFieldType.DATE, label = "Last Inspection Date", isRequired = false)
                    )
                ),
                FormSection(id = "Visual Inspection", title = "Visual Inspection",
                    fields = listOf(
                        FormField(fieldName = "pump_housing_cracks", fieldType = FormFieldType.CHECKBOX, label = "Check for Cracks in Pump Housing", isRequired = false),
                        FormField(fieldName = "pump_housing_condition", fieldType = FormFieldType.DROPDOWN, label = "Pump Housing Condition", isRequired = true, 
                                 options = listOf("Satisfactory", "Defective", "Needs Attention")),
                        FormField(fieldName = "pump_housing_comments", fieldType = FormFieldType.MULTILINE_TEXT, label = "Pump Housing Comments", isRequired = false),
                        FormField(fieldName = "impeller_wear", fieldType = FormFieldType.CHECKBOX, label = "Check Impeller for Wear", isRequired = false),
                        FormField(fieldName = "impeller_condition", fieldType = FormFieldType.DROPDOWN, label = "Impeller Condition", isRequired = true,
                                 options = listOf("Satisfactory", "Defective", "Needs Attention")),
                        FormField(fieldName = "impeller_comments", fieldType = FormFieldType.MULTILINE_TEXT, label = "Impeller Comments", isRequired = false),
                        FormField(fieldName = "discharge_piping_leaks", fieldType = FormFieldType.CHECKBOX, label = "Check Discharge Piping for Leaks", isRequired = false),
                        FormField(fieldName = "discharge_piping_condition", fieldType = FormFieldType.DROPDOWN, label = "Discharge Piping Condition", isRequired = true,
                                 options = listOf("Satisfactory", "Defective", "Needs Attention")),
                        FormField(fieldName = "discharge_piping_comments", fieldType = FormFieldType.MULTILINE_TEXT, label = "Discharge Piping Comments", isRequired = false),
                        FormField(fieldName = "suction_piping_check", fieldType = FormFieldType.CHECKBOX, label = "Check Suction Piping", isRequired = false),
                        FormField(fieldName = "suction_piping_condition", fieldType = FormFieldType.DROPDOWN, label = "Suction Piping Condition", isRequired = true,
                                 options = listOf("Satisfactory", "Defective", "Needs Attention")),
                        FormField(fieldName = "suction_piping_comments", fieldType = FormFieldType.MULTILINE_TEXT, label = "Suction Piping Comments", isRequired = false)
                    )
                ),
                FormSection(id = "Coupling & Motor Assessment", title = "Coupling & Motor Assessment",
                    fields = listOf(
                        FormField(fieldName = "coupling_alignment_check", fieldType = FormFieldType.CHECKBOX, label = "Check Coupling Alignment", isRequired = false),
                        FormField(fieldName = "coupling_condition", fieldType = FormFieldType.DROPDOWN, label = "Coupling Condition", isRequired = true,
                                 options = listOf("Satisfactory", "Defective", "Needs Attention")),
                        FormField(fieldName = "coupling_comments", fieldType = FormFieldType.MULTILINE_TEXT, label = "Coupling Comments", isRequired = false),
                        FormField(fieldName = "motor_vibration_check", fieldType = FormFieldType.CHECKBOX, label = "Check Motor for Vibration", isRequired = false),
                        FormField(fieldName = "motor_condition", fieldType = FormFieldType.DROPDOWN, label = "Motor Condition", isRequired = true,
                                 options = listOf("Satisfactory", "Defective", "Needs Attention")),
                        FormField(fieldName = "motor_comments", fieldType = FormFieldType.MULTILINE_TEXT, label = "Motor Comments", isRequired = false),
                        FormField(fieldName = "motor_current_reading", fieldType = FormFieldType.NUMBER, label = "Motor Current Reading (Amps)", isRequired = false),
                        FormField(fieldName = "motor_temperature", fieldType = FormFieldType.NUMBER, label = "Motor Temperature (°C)", isRequired = false)
                    )
                ),
                FormSection(id = "Performance Tests", title = "Performance Tests",
                    fields = listOf(
                        FormField(fieldName = "pressure_test_suction", fieldType = FormFieldType.NUMBER, label = "Suction Pressure (Bar)", isRequired = true),
                        FormField(fieldName = "pressure_test_discharge", fieldType = FormFieldType.NUMBER, label = "Discharge Pressure (Bar)", isRequired = true),
                        FormField(fieldName = "flow_rate_test", fieldType = FormFieldType.NUMBER, label = "Flow Rate (L/min)", isRequired = true),
                        FormField(fieldName = "vibration_level", fieldType = FormFieldType.NUMBER, label = "Vibration Level (mm/s)", isRequired = false),
                        FormField(fieldName = "temperature_bearing", fieldType = FormFieldType.NUMBER, label = "Bearing Temperature (°C)", isRequired = false),
                        FormField(fieldName = "oil_level_check", fieldType = FormFieldType.CHECKBOX, label = "Oil Level Check", isRequired = false),
                        FormField(fieldName = "oil_condition", fieldType = FormFieldType.DROPDOWN, label = "Oil Condition", isRequired = false,
                                 options = listOf("Good", "Fair", "Poor - Replace"))
                    )
                ),
                FormSection(id = "Electrical & Safety Systems", title = "Electrical & Safety Systems",
                    fields = listOf(
                        FormField(fieldName = "electrical_connections_secure", fieldType = FormFieldType.CHECKBOX, label = "Electrical Connections Secure", isRequired = false),
                        FormField(fieldName = "control_panel_operational", fieldType = FormFieldType.CHECKBOX, label = "Control Panel Operational", isRequired = false),
                        FormField(fieldName = "emergency_stop_functional", fieldType = FormFieldType.CHECKBOX, label = "Emergency Stop Functional", isRequired = false),
                        FormField(fieldName = "pressure_gauges_calibrated", fieldType = FormFieldType.CHECKBOX, label = "Pressure Gauges Calibrated", isRequired = false),
                        FormField(fieldName = "safety_valves_tested", fieldType = FormFieldType.CHECKBOX, label = "Safety Valves Tested", isRequired = false),
                        FormField(fieldName = "guards_protective_devices", fieldType = FormFieldType.CHECKBOX, label = "Guards & Protective Devices in Place", isRequired = false)
                    )
                ),
                FormSection(id = "Overall Assessment & Actions", title = "Overall Assessment & Actions",
                    fields = listOf(
                        FormField(fieldName = "overall_pump_condition", fieldType = FormFieldType.DROPDOWN, label = "Overall Pump Condition", isRequired = true,
                                 options = listOf("Excellent", "Good", "Fair", "Poor", "Critical")),
                        FormField(fieldName = "operational_status", fieldType = FormFieldType.DROPDOWN, label = "Operational Status", isRequired = true,
                                 options = listOf("Operational", "Limited Operation", "Non-Operational", "Under Repair")),
                        FormField(fieldName = "issues_found", fieldType = FormFieldType.MULTILINE_TEXT, label = "Issues/Deficiencies Found", isRequired = false),
                        FormField(fieldName = "corrective_actions_required", fieldType = FormFieldType.MULTILINE_TEXT, label = "Corrective Actions Required", isRequired = false),
                        FormField(fieldName = "parts_needed", fieldType = FormFieldType.MULTILINE_TEXT, label = "Parts/Materials Needed", isRequired = false),
                        FormField(fieldName = "work_order_required", fieldType = FormFieldType.CHECKBOX, label = "Work Order Required", isRequired = false),
                        FormField(fieldName = "work_order_number", fieldType = FormFieldType.TEXT, label = "Work Order Number", isRequired = false),
                        FormField(fieldName = "next_inspection_date", fieldType = FormFieldType.DATE, label = "Next Inspection Date", isRequired = true),
                        FormField(fieldName = "inspection_recommendations", fieldType = FormFieldType.MULTILINE_TEXT, label = "Recommendations", isRequired = false)
                    )
                ),
                FormSection(id = "Sign-off & Documentation", title = "Sign-off & Documentation",
                    fields = listOf(
                        FormField(fieldName = "inspector_signature", fieldType = FormFieldType.SIGNATURE, label = "Inspector Signature", isRequired = true),
                        FormField(fieldName = "supervisor_review", fieldType = FormFieldType.SIGNATURE, label = "Supervisor Review Signature", isRequired = false),
                        FormField(fieldName = "photo_references", fieldType = FormFieldType.MULTILINE_TEXT, label = "Photo References", isRequired = false),
                        FormField(fieldName = "additional_notes", fieldType = FormFieldType.MULTILINE_TEXT, label = "Additional Notes", isRequired = false)
                    )
                )
            ),
            fields = listOf(),
            createdAt = now,
            updatedAt = now
        ),
        // Vehicle Maintenance Record
        "vehicle_maintenance" to FormTemplate(
            id = "vehicle_maintenance",
            name = "Vehicle Maintenance Record",
            description = "Vehicle maintenance tracking based on MMU CHASSIS MAINTENANCE RECORD.pdf",
            formType = FormType.MMU_CHASSIS_MAINTENANCE,
            templateFile = "templates/vehicle_maintenance.json",
            pdfTemplate = "MMU CHASSIS MAINTENANCE RECORD.pdf",
            fieldMappings = emptyList(),
            version = "1.0",
            sections = listOf(
                FormSection(id = "Vehicle Maintenance Information", title = "Vehicle Maintenance Information",
                    fields = listOf(
                        FormField(fieldName = "vehicle_id", fieldType = FormFieldType.TEXT, label = "Vehicle ID", isRequired = true),
                        FormField(fieldName = "site", fieldType = FormFieldType.TEXT, label = "Site", isRequired = true, isReadOnly = true),
                        FormField(fieldName = "maintenance_date", fieldType = FormFieldType.DATE, label = "Maintenance Date", isRequired = true),
                        FormField(fieldName = "mileage", fieldType = FormFieldType.NUMBER, label = "Mileage", isRequired = true),
                        FormField(fieldName = "technician_name", fieldType = FormFieldType.TEXT, label = "Technician Name", isRequired = true),
                        FormField(fieldName = "maintenance_type", fieldType = FormFieldType.DROPDOWN, label = "Maintenance Type", isRequired = true),
                        FormField(fieldName = "engine_oil_check", fieldType = FormFieldType.CHECKBOX, label = "Engine Oil Check", isRequired = false),
                        FormField(fieldName = "brake_fluid_check", fieldType = FormFieldType.CHECKBOX, label = "Brake Fluid Check", isRequired = false),
                        FormField(fieldName = "coolant_check", fieldType = FormFieldType.CHECKBOX, label = "Coolant Check", isRequired = false),
                        FormField(fieldName = "tire_pressure_check", fieldType = FormFieldType.CHECKBOX, label = "Tire Pressure Check", isRequired = false),
                        FormField(fieldName = "battery_check", fieldType = FormFieldType.CHECKBOX, label = "Battery Check", isRequired = false),
                        FormField(fieldName = "lights_check", fieldType = FormFieldType.CHECKBOX, label = "Lights Check", isRequired = false),
                        FormField(fieldName = "brakes_check", fieldType = FormFieldType.CHECKBOX, label = "Brakes Check", isRequired = false),
                        FormField(fieldName = "suspension_check", fieldType = FormFieldType.CHECKBOX, label = "Suspension Check", isRequired = false),
                        FormField(fieldName = "steering_check", fieldType = FormFieldType.CHECKBOX, label = "Steering Check", isRequired = false),
                        FormField(fieldName = "hydraulic_system_check", fieldType = FormFieldType.CHECKBOX, label = "Hydraulic System Check", isRequired = false),
                        FormField(fieldName = "electrical_system_check", fieldType = FormFieldType.CHECKBOX, label = "Electrical System Check", isRequired = false),
                        FormField(fieldName = "work_performed", fieldType = FormFieldType.MULTILINE_TEXT, label = "Work Performed", isRequired = true),
                        FormField(fieldName = "parts_replaced", fieldType = FormFieldType.MULTILINE_TEXT, label = "Parts Replaced", isRequired = false),
                        FormField(fieldName = "next_service_km", fieldType = FormFieldType.NUMBER, label = "Next Service (km)", isRequired = false),
                        FormField(fieldName = "technician_signature", fieldType = FormFieldType.SIGNATURE, label = "Technician Signature", isRequired = true)
                    )
                )
            ),
            fields = listOf(),
            createdAt = now,
            updatedAt = now
        ),
        // Availability & Utilization Report
        "availability_utilization" to FormTemplate(
            id = "availability_utilization",
            name = "Availability & Utilization Report",
            description = "Equipment availability and utilization tracking based on Availabilty & Utilization.pdf",
            formType = FormType.AVAILABILITY_UTILIZATION,
            templateFile = "templates/availability_utilization.json",
            pdfTemplate = "Availabilty & Utilization.pdf",
            fieldMappings = emptyList(),
            version = "1.0",
            sections = listOf(
                FormSection(id = "Equipment Availability & Utilization", title = "Equipment Availability & Utilization",
                    fields = listOf(
                        FormField(fieldName = "equipment_id", fieldType = FormFieldType.TEXT, label = "Equipment ID", isRequired = true),
                        FormField(fieldName = "report_date", fieldType = FormFieldType.DATE, label = "Report Date", isRequired = true),
                        FormField(fieldName = "reporting_period", fieldType = FormFieldType.TEXT, label = "Reporting Period", isRequired = true),
                        FormField(fieldName = "total_available_hours", fieldType = FormFieldType.NUMBER, label = "Total Available Hours", isRequired = true),
                        FormField(fieldName = "operational_hours", fieldType = FormFieldType.NUMBER, label = "Operational Hours", isRequired = true),
                        FormField(fieldName = "maintenance_hours", fieldType = FormFieldType.NUMBER, label = "Maintenance Hours", isRequired = true),
                        FormField(fieldName = "breakdown_hours", fieldType = FormFieldType.NUMBER, label = "Breakdown Hours", isRequired = true),
                        FormField(fieldName = "standby_hours", fieldType = FormFieldType.NUMBER, label = "Standby Hours", isRequired = true),
                        FormField(fieldName = "availability_percentage", fieldType = FormFieldType.NUMBER, label = "Availability %", isRequired = true),
                        FormField(fieldName = "utilization_percentage", fieldType = FormFieldType.NUMBER, label = "Utilization %", isRequired = true),
                        FormField(fieldName = "efficiency_percentage", fieldType = FormFieldType.NUMBER, label = "Efficiency %", isRequired = true),
                        FormField(fieldName = "comments", fieldType = FormFieldType.MULTILINE_TEXT, label = "Comments", isRequired = false),
                        FormField(fieldName = "prepared_by", fieldType = FormFieldType.TEXT, label = "Prepared By", isRequired = true),
                        FormField(fieldName = "approved_by", fieldType = FormFieldType.TEXT, label = "Approved By", isRequired = true)
                    )
                )
            ),
            fields = listOf(),
            createdAt = now,
            updatedAt = now
        ),
        // Bowie Pump Weekly Checklist - COMPREHENSIVE from PDF coordinate maps
        "bowie_pump_checklist" to FormTemplate(
            id = "bowie_pump_checklist",
            name = "Bowie Pump Weekly Checklist",
            description = "Comprehensive weekly pump inspection checklist based on Bowie Pump Weekly check list.pdf with ALL fields from PDF coordinate maps",
            formType = FormType.PUMP_WEEKLY_CHECK,
            templateFile = "templates/bowie_pump_checklist.json",
            pdfTemplate = "Bowie Pump Weekly check list.pdf",
            fieldMappings = emptyList(),
            version = "2.0",
            sections = listOf(
                FormSection(id = "Inspection Header", title = "Inspection Header",
                    fields = listOf(
                        FormField(fieldName = "inspection_date", fieldType = FormFieldType.DATE, label = "Inspection Date", isRequired = true),
                        FormField(fieldName = "week_of", fieldType = FormFieldType.DATE, label = "Week Of", isRequired = true),
                        FormField(fieldName = "pump_id", fieldType = FormFieldType.TEXT, label = "Pump ID", isRequired = true),
                        FormField(fieldName = "pump_location", fieldType = FormFieldType.TEXT, label = "Pump Location", isRequired = true),
                        FormField(fieldName = "inspector_name", fieldType = FormFieldType.TEXT, label = "Inspector Name", isRequired = true),
                        FormField(fieldName = "inspector_id", fieldType = FormFieldType.TEXT, label = "Inspector ID", isRequired = false),
                        FormField(fieldName = "shift", fieldType = FormFieldType.DROPDOWN, label = "Shift", isRequired = true,
                                 options = listOf("Day", "Night", "Swing"))
                    )
                ),
                FormSection(id = "Pump Operation Check", title = "Pump Operation Check",
                    fields = listOf(
                        FormField(fieldName = "pump_running", fieldType = FormFieldType.CHECKBOX, label = "Pump Running Normally", isRequired = false),
                        FormField(fieldName = "pump_start_stop_test", fieldType = FormFieldType.CHECKBOX, label = "Start/Stop Test Performed", isRequired = false),
                        FormField(fieldName = "flow_rate_check", fieldType = FormFieldType.CHECKBOX, label = "Flow Rate Within Spec", isRequired = false),
                        FormField(fieldName = "flow_rate_value", fieldType = FormFieldType.NUMBER, label = "Flow Rate (L/min)", isRequired = false),
                        FormField(fieldName = "pressure_suction", fieldType = FormFieldType.NUMBER, label = "Suction Pressure (Bar)", isRequired = false),
                        FormField(fieldName = "pressure_discharge", fieldType = FormFieldType.NUMBER, label = "Discharge Pressure (Bar)", isRequired = false),
                        FormField(fieldName = "pressure_normal", fieldType = FormFieldType.CHECKBOX, label = "Pressures Within Normal Range", isRequired = false)
                    )
                ),
                FormSection(id = "Visual & Auditory Checks", title = "Visual & Auditory Checks",
                    fields = listOf(
                        FormField(fieldName = "no_unusual_noise", fieldType = FormFieldType.CHECKBOX, label = "No Unusual Noise", isRequired = false),
                        FormField(fieldName = "noise_level_db", fieldType = FormFieldType.NUMBER, label = "Noise Level (dB)", isRequired = false),
                        FormField(fieldName = "no_vibration", fieldType = FormFieldType.CHECKBOX, label = "No Excessive Vibration", isRequired = false),
                        FormField(fieldName = "vibration_level", fieldType = FormFieldType.NUMBER, label = "Vibration Level (mm/s)", isRequired = false),
                        FormField(fieldName = "no_leaks", fieldType = FormFieldType.CHECKBOX, label = "No Visible Leaks", isRequired = false),
                        FormField(fieldName = "leak_locations", fieldType = FormFieldType.MULTILINE_TEXT, label = "Leak Locations (if any)", isRequired = false),
                        FormField(fieldName = "visual_damage_check", fieldType = FormFieldType.CHECKBOX, label = "No Visible Damage", isRequired = false)
                    )
                ),
                FormSection(id = "Temperature Monitoring", title = "Temperature Monitoring",
                    fields = listOf(
                        FormField(fieldName = "bearing_temperature_de", fieldType = FormFieldType.NUMBER, label = "Bearing Temp - Drive End (°C)", isRequired = false),
                        FormField(fieldName = "bearing_temperature_nde", fieldType = FormFieldType.NUMBER, label = "Bearing Temp - Non-Drive End (°C)", isRequired = false),
                        FormField(fieldName = "motor_temperature", fieldType = FormFieldType.NUMBER, label = "Motor Temperature (°C)", isRequired = false),
                        FormField(fieldName = "temperature_normal", fieldType = FormFieldType.CHECKBOX, label = "All Temperatures Normal", isRequired = false),
                        FormField(fieldName = "temperature_alarm_set", fieldType = FormFieldType.CHECKBOX, label = "Temperature Alarms Set Correctly", isRequired = false)
                    )
                ),
                FormSection(id = "Lubrication Inspection", title = "Lubrication Inspection",
                    fields = listOf(
                        FormField(fieldName = "oil_level_ok", fieldType = FormFieldType.CHECKBOX, label = "Oil Level OK", isRequired = false),
                        FormField(fieldName = "oil_level_measurement", fieldType = FormFieldType.TEXT, label = "Oil Level Measurement", isRequired = false),
                        FormField(fieldName = "oil_condition_clear", fieldType = FormFieldType.CHECKBOX, label = "Oil Clean/Clear", isRequired = false),
                        FormField(fieldName = "oil_last_changed", fieldType = FormFieldType.DATE, label = "Oil Last Changed", isRequired = false),
                        FormField(fieldName = "grease_fittings_lubricated", fieldType = FormFieldType.CHECKBOX, label = "Grease Fittings Lubricated", isRequired = false),
                        FormField(fieldName = "grease_type_used", fieldType = FormFieldType.TEXT, label = "Grease Type Used", isRequired = false),
                        FormField(fieldName = "lubrication_schedule_current", fieldType = FormFieldType.CHECKBOX, label = "Lubrication Schedule Current", isRequired = false)
                    )
                ),
                FormSection(id = "Electrical System Check", title = "Electrical System Check",
                    fields = listOf(
                        FormField(fieldName = "motor_current_phase1", fieldType = FormFieldType.NUMBER, label = "Motor Current Phase 1 (A)", isRequired = false),
                        FormField(fieldName = "motor_current_phase2", fieldType = FormFieldType.NUMBER, label = "Motor Current Phase 2 (A)", isRequired = false),
                        FormField(fieldName = "motor_current_phase3", fieldType = FormFieldType.NUMBER, label = "Motor Current Phase 3 (A)", isRequired = false),
                        FormField(fieldName = "motor_current_balanced", fieldType = FormFieldType.CHECKBOX, label = "Motor Currents Balanced", isRequired = false),
                        FormField(fieldName = "voltage_reading", fieldType = FormFieldType.NUMBER, label = "Voltage Reading (V)", isRequired = false),
                        FormField(fieldName = "electrical_connections_secure", fieldType = FormFieldType.CHECKBOX, label = "All Electrical Connections Secure", isRequired = false),
                        FormField(fieldName = "insulation_resistance", fieldType = FormFieldType.NUMBER, label = "Insulation Resistance (MΩ)", isRequired = false)
                    )
                ),
                FormSection(id = "Control & Safety Systems", title = "Control & Safety Systems",
                    fields = listOf(
                        FormField(fieldName = "control_panel_operational", fieldType = FormFieldType.CHECKBOX, label = "Control Panel Operational", isRequired = false),
                        FormField(fieldName = "start_stop_buttons_tested", fieldType = FormFieldType.CHECKBOX, label = "Start/Stop Buttons Tested", isRequired = false),
                        FormField(fieldName = "emergency_stop_tested", fieldType = FormFieldType.CHECKBOX, label = "Emergency Stop Tested", isRequired = false),
                        FormField(fieldName = "pressure_switches_tested", fieldType = FormFieldType.CHECKBOX, label = "Pressure Switches Tested", isRequired = false),
                        FormField(fieldName = "level_switches_tested", fieldType = FormFieldType.CHECKBOX, label = "Level Switches Tested", isRequired = false),
                        FormField(fieldName = "safety_devices_functional", fieldType = FormFieldType.CHECKBOX, label = "All Safety Devices Functional", isRequired = false),
                        FormField(fieldName = "alarms_tested", fieldType = FormFieldType.CHECKBOX, label = "Alarm Systems Tested", isRequired = false)
                    )
                ),
                FormSection(id = "Mechanical Components", title = "Mechanical Components",
                    fields = listOf(
                        FormField(fieldName = "coupling_alignment_checked", fieldType = FormFieldType.CHECKBOX, label = "Coupling Alignment Checked", isRequired = false),
                        FormField(fieldName = "coupling_condition_good", fieldType = FormFieldType.CHECKBOX, label = "Coupling Condition Good", isRequired = false),
                        FormField(fieldName = "shaft_seal_condition", fieldType = FormFieldType.DROPDOWN, label = "Shaft Seal Condition", isRequired = false,
                                 options = listOf("Good", "Fair", "Needs Replacement", "Leaking")),
                        FormField(fieldName = "impeller_clearance_checked", fieldType = FormFieldType.CHECKBOX, label = "Impeller Clearance Checked", isRequired = false),
                        FormField(fieldName = "wear_ring_condition", fieldType = FormFieldType.DROPDOWN, label = "Wear Ring Condition", isRequired = false,
                                 options = listOf("Good", "Fair", "Worn - Monitor", "Replace Soon")),
                        FormField(fieldName = "foundation_bolts_tight", fieldType = FormFieldType.CHECKBOX, label = "Foundation Bolts Tight", isRequired = false)
                    )
                ),
                FormSection(id = "Housekeeping & Environment", title = "Housekeeping & Environment",
                    fields = listOf(
                        FormField(fieldName = "area_clean_organized", fieldType = FormFieldType.CHECKBOX, label = "Area Clean & Organized", isRequired = false),
                        FormField(fieldName = "drainage_adequate", fieldType = FormFieldType.CHECKBOX, label = "Drainage Adequate", isRequired = false),
                        FormField(fieldName = "lighting_adequate", fieldType = FormFieldType.CHECKBOX, label = "Lighting Adequate", isRequired = false),
                        FormField(fieldName = "access_clear", fieldType = FormFieldType.CHECKBOX, label = "Access Routes Clear", isRequired = false),
                        FormField(fieldName = "tools_equipment_secure", fieldType = FormFieldType.CHECKBOX, label = "Tools & Equipment Secure", isRequired = false),
                        FormField(fieldName = "housekeeping_satisfactory", fieldType = FormFieldType.CHECKBOX, label = "Overall Housekeeping Satisfactory", isRequired = false)
                    )
                ),
                FormSection(id = "Issues & Actions", title = "Issues & Actions",
                    fields = listOf(
                        FormField(fieldName = "issues_identified", fieldType = FormFieldType.MULTILINE_TEXT, label = "Issues/Deficiencies Identified", isRequired = false),
                        FormField(fieldName = "immediate_actions_taken", fieldType = FormFieldType.MULTILINE_TEXT, label = "Immediate Actions Taken", isRequired = false),
                        FormField(fieldName = "corrective_actions_required", fieldType = FormFieldType.MULTILINE_TEXT, label = "Corrective Actions Required", isRequired = false),
                        FormField(fieldName = "work_order_raised", fieldType = FormFieldType.CHECKBOX, label = "Work Order Raised", isRequired = false),
                        FormField(fieldName = "work_order_number", fieldType = FormFieldType.TEXT, label = "Work Order Number", isRequired = false),
                        FormField(fieldName = "priority_level", fieldType = FormFieldType.DROPDOWN, label = "Priority Level", isRequired = false,
                                 options = listOf("Low", "Medium", "High", "Critical")),
                        FormField(fieldName = "estimated_completion_date", fieldType = FormFieldType.DATE, label = "Estimated Completion Date", isRequired = false)
                    )
                ),
                FormSection(id = "Recommendations & Sign-off", title = "Recommendations & Sign-off",
                    fields = listOf(
                        FormField(fieldName = "overall_pump_condition", fieldType = FormFieldType.DROPDOWN, label = "Overall Pump Condition", isRequired = true,
                                 options = listOf("Excellent", "Good", "Fair", "Poor", "Critical")),
                        FormField(fieldName = "recommended_actions", fieldType = FormFieldType.MULTILINE_TEXT, label = "Recommended Actions", isRequired = false),
                        FormField(fieldName = "next_inspection_date", fieldType = FormFieldType.DATE, label = "Next Inspection Date", isRequired = true),
                        FormField(fieldName = "inspection_duration_minutes", fieldType = FormFieldType.NUMBER, label = "Inspection Duration (minutes)", isRequired = false),
                        FormField(fieldName = "inspector_signature", fieldType = FormFieldType.SIGNATURE, label = "Inspector Signature", isRequired = true),
                        FormField(fieldName = "supervisor_review_signature", fieldType = FormFieldType.SIGNATURE, label = "Supervisor Review Signature", isRequired = false),
                        FormField(fieldName = "additional_comments", fieldType = FormFieldType.MULTILINE_TEXT, label = "Additional Comments", isRequired = false)
                    )
                )
            ),
            fields = listOf(),
            createdAt = now,
            updatedAt = now
        ),
        // Quality Report
        "mmu_quality_report" to FormTemplate(
            id = "mmu_quality_report",
            name = "MMU Quality Report",
            description = "Quality control reporting for MMU operations based on mmu quality report.pdf",
            formType = FormType.MMU_QUALITY_REPORT,
            templateFile = "templates/mmu_quality_report.json",
            pdfTemplate = "mmu quality report.pdf",
            fieldMappings = emptyList(),
            version = "1.0",
            sections = listOf(
                FormSection(id = "Quality Control Information", title = "Quality Control Information",
                    fields = listOf(
                        FormField(fieldName = "report_date", fieldType = FormFieldType.DATE, label = "Report Date", isRequired = true),
                        FormField(fieldName = "batch_number", fieldType = FormFieldType.TEXT, label = "Batch Number", isRequired = true),
                        FormField(fieldName = "product_type", fieldType = FormFieldType.DROPDOWN, label = "Product Type", isRequired = true),
                        FormField(fieldName = "quality_inspector", fieldType = FormFieldType.TEXT, label = "Quality Inspector", isRequired = true),
                        FormField(fieldName = "sample_size", fieldType = FormFieldType.NUMBER, label = "Sample Size", isRequired = true),
                        FormField(fieldName = "moisture_content", fieldType = FormFieldType.NUMBER, label = "Moisture Content %", isRequired = true),
                        FormField(fieldName = "particle_size", fieldType = FormFieldType.NUMBER, label = "Particle Size (μm)", isRequired = true),
                        FormField(fieldName = "density", fieldType = FormFieldType.NUMBER, label = "Density (g/cm³)", isRequired = true),
                        FormField(fieldName = "ph_level", fieldType = FormFieldType.NUMBER, label = "pH Level", isRequired = true),
                        FormField(fieldName = "temperature", fieldType = FormFieldType.NUMBER, label = "Temperature (°C)", isRequired = true),
                        FormField(fieldName = "quality_grade", fieldType = FormFieldType.DROPDOWN, label = "Quality Grade", isRequired = true),
                        FormField(fieldName = "test_results", fieldType = FormFieldType.MULTILINE_TEXT, label = "Test Results", isRequired = true),
                        FormField(fieldName = "compliance_status", fieldType = FormFieldType.DROPDOWN, label = "Compliance Status", isRequired = true),
                        FormField(fieldName = "non_conformances", fieldType = FormFieldType.MULTILINE_TEXT, label = "Non-Conformances", isRequired = false),
                        FormField(fieldName = "corrective_actions", fieldType = FormFieldType.MULTILINE_TEXT, label = "Corrective Actions", isRequired = false),
                        FormField(fieldName = "inspector_signature", fieldType = FormFieldType.SIGNATURE, label = "Inspector Signature", isRequired = true)
                    )
                )
            ),
            fields = listOf(),
            createdAt = now,
            updatedAt = now
        ),
        // Production Daily Log - COMPREHENSIVE from PDF coordinate maps
        "mmu_production_log" to FormTemplate(
            id = "mmu_production_log",
            name = "MMU Production Daily Log",
            description = "Comprehensive daily production logging based on mmu production daily log.pdf with ALL fields from PDF coordinate maps",
            formType = FormType.MMU_DAILY_LOG,
            templateFile = "templates/mmu_production_log.json",
            pdfTemplate = "mmu production daily log.pdf",
            fieldMappings = emptyList(),
            version = "2.0",
            sections = listOf(
                FormSection(id = "Header Information", title = "Header Information",
                    fields = listOf(
                        FormField(fieldName = "production_date", fieldType = FormFieldType.DATE, label = "Production Date", isRequired = true),
                        FormField(fieldName = "mmu_id", fieldType = FormFieldType.TEXT, label = "MMU ID", isRequired = true),
                        FormField(fieldName = "shift", fieldType = FormFieldType.DROPDOWN, label = "Shift", isRequired = true,
                                 options = listOf("Day", "Night", "Swing")),
                        FormField(fieldName = "operator_name", fieldType = FormFieldType.TEXT, label = "Operator Name", isRequired = true),
                        FormField(fieldName = "supervisor_name", fieldType = FormFieldType.TEXT, label = "Supervisor Name", isRequired = true),
                        FormField(fieldName = "location", fieldType = FormFieldType.TEXT, label = "Location/Zone", isRequired = true)
                    )
                ),
                FormSection(id = "Production Details", title = "Production Details",
                    fields = listOf(
                        FormField(fieldName = "product_type", fieldType = FormFieldType.DROPDOWN, label = "Product Type", isRequired = true,
                                 options = listOf("Emulsion", "ANFO", "Heavy ANFO", "Packaged", "Other")),
                        FormField(fieldName = "batch_number", fieldType = FormFieldType.TEXT, label = "Batch Number", isRequired = true),
                        FormField(fieldName = "recipe_number", fieldType = FormFieldType.TEXT, label = "Recipe Number", isRequired = false),
                        FormField(fieldName = "start_time", fieldType = FormFieldType.TIME, label = "Start Time", isRequired = true),
                        FormField(fieldName = "end_time", fieldType = FormFieldType.TIME, label = "End Time", isRequired = true),
                        FormField(fieldName = "total_production_time", fieldType = FormFieldType.NUMBER, label = "Total Production Time (hrs)", isRequired = true),
                        FormField(fieldName = "total_production", fieldType = FormFieldType.NUMBER, label = "Total Production (kg)", isRequired = true),
                        FormField(fieldName = "target_production", fieldType = FormFieldType.NUMBER, label = "Target Production (kg)", isRequired = true),
                        FormField(fieldName = "efficiency_percentage", fieldType = FormFieldType.NUMBER, label = "Efficiency %", isRequired = true),
                        FormField(fieldName = "production_rate", fieldType = FormFieldType.NUMBER, label = "Production Rate (kg/hr)", isRequired = false)
                    )
                ),
                FormSection(id = "Raw Materials Consumption", title = "Raw Materials Consumption",
                    fields = listOf(
                        FormField(fieldName = "ammonium_nitrate_kg", fieldType = FormFieldType.NUMBER, label = "Ammonium Nitrate (kg)", isRequired = true),
                        FormField(fieldName = "diesel_fuel_liters", fieldType = FormFieldType.NUMBER, label = "Diesel Fuel (L)", isRequired = true),
                        FormField(fieldName = "emulsifier_kg", fieldType = FormFieldType.NUMBER, label = "Emulsifier (kg)", isRequired = false),
                        FormField(fieldName = "water_liters", fieldType = FormFieldType.NUMBER, label = "Water (L)", isRequired = false),
                        FormField(fieldName = "sensitizer_kg", fieldType = FormFieldType.NUMBER, label = "Sensitizer (kg)", isRequired = false),
                        FormField(fieldName = "other_additives", fieldType = FormFieldType.MULTILINE_TEXT, label = "Other Additives", isRequired = false),
                        FormField(fieldName = "waste_material_kg", fieldType = FormFieldType.NUMBER, label = "Waste Material (kg)", isRequired = false)
                    )
                ),
                FormSection(id = "Quality Control", title = "Quality Control",
                    fields = listOf(
                        FormField(fieldName = "density_gcm3", fieldType = FormFieldType.NUMBER, label = "Density (g/cm³)", isRequired = true),
                        FormField(fieldName = "temperature_celsius", fieldType = FormFieldType.NUMBER, label = "Temperature (°C)", isRequired = true),
                        FormField(fieldName = "viscosity_cps", fieldType = FormFieldType.NUMBER, label = "Viscosity (cPs)", isRequired = false),
                        FormField(fieldName = "quality_test_results", fieldType = FormFieldType.MULTILINE_TEXT, label = "Quality Test Results", isRequired = false),
                        FormField(fieldName = "quality_issues", fieldType = FormFieldType.MULTILINE_TEXT, label = "Quality Issues/Non-Conformances", isRequired = false),
                        FormField(fieldName = "corrective_actions_quality", fieldType = FormFieldType.MULTILINE_TEXT, label = "Corrective Actions Taken", isRequired = false)
                    )
                ),
                FormSection(id = "Equipment Performance", title = "Equipment Performance",
                    fields = listOf(
                        FormField(fieldName = "mmu_operating_hours", fieldType = FormFieldType.NUMBER, label = "MMU Operating Hours", isRequired = true),
                        FormField(fieldName = "emulsion_pump_pressure", fieldType = FormFieldType.NUMBER, label = "Emulsion Pump Pressure (Bar)", isRequired = false),
                        FormField(fieldName = "sensitizer_pump_pressure", fieldType = FormFieldType.NUMBER, label = "Sensitizer Pump Pressure (Bar)", isRequired = false),
                        FormField(fieldName = "mixer_speed_rpm", fieldType = FormFieldType.NUMBER, label = "Mixer Speed (RPM)", isRequired = false),
                        FormField(fieldName = "engine_hours", fieldType = FormFieldType.NUMBER, label = "Engine Hours", isRequired = true),
                        FormField(fieldName = "fuel_consumption_liters", fieldType = FormFieldType.NUMBER, label = "Fuel Consumption (L)", isRequired = false),
                        FormField(fieldName = "equipment_issues", fieldType = FormFieldType.MULTILINE_TEXT, label = "Equipment Issues/Maintenance Needs", isRequired = false)
                    )
                ),
                FormSection(id = "Downtime & Delays", title = "Downtime & Delays",
                    fields = listOf(
                        FormField(fieldName = "planned_downtime_minutes", fieldType = FormFieldType.NUMBER, label = "Planned Downtime (minutes)", isRequired = false),
                        FormField(fieldName = "unplanned_downtime_minutes", fieldType = FormFieldType.NUMBER, label = "Unplanned Downtime (minutes)", isRequired = false),
                        FormField(fieldName = "total_downtime_minutes", fieldType = FormFieldType.NUMBER, label = "Total Downtime (minutes)", isRequired = false),
                        FormField(fieldName = "downtime_reason_mechanical", fieldType = FormFieldType.CHECKBOX, label = "Mechanical Breakdown", isRequired = false),
                        FormField(fieldName = "downtime_reason_electrical", fieldType = FormFieldType.CHECKBOX, label = "Electrical Fault", isRequired = false),
                        FormField(fieldName = "downtime_reason_material", fieldType = FormFieldType.CHECKBOX, label = "Material Shortage", isRequired = false),
                        FormField(fieldName = "downtime_reason_weather", fieldType = FormFieldType.CHECKBOX, label = "Weather", isRequired = false),
                        FormField(fieldName = "downtime_reason_other", fieldType = FormFieldType.CHECKBOX, label = "Other", isRequired = false),
                        FormField(fieldName = "downtime_reason_details", fieldType = FormFieldType.MULTILINE_TEXT, label = "Downtime Reason Details", isRequired = false),
                        FormField(fieldName = "corrective_actions_downtime", fieldType = FormFieldType.MULTILINE_TEXT, label = "Corrective Actions for Downtime", isRequired = false)
                    )
                ),
                FormSection(id = "Safety & Environmental", title = "Safety & Environmental",
                    fields = listOf(
                        FormField(fieldName = "safety_incidents", fieldType = FormFieldType.MULTILINE_TEXT, label = "Safety Incidents/Near Misses", isRequired = false),
                        FormField(fieldName = "ppe_compliance", fieldType = FormFieldType.CHECKBOX, label = "PPE Compliance Verified", isRequired = false),
                        FormField(fieldName = "environmental_incidents", fieldType = FormFieldType.MULTILINE_TEXT, label = "Environmental Incidents/Spills", isRequired = false),
                        FormField(fieldName = "waste_disposal_method", fieldType = FormFieldType.TEXT, label = "Waste Disposal Method", isRequired = false),
                        FormField(fieldName = "safety_checks_completed", fieldType = FormFieldType.CHECKBOX, label = "Pre-shift Safety Checks Completed", isRequired = false),
                        FormField(fieldName = "emergency_equipment_checked", fieldType = FormFieldType.CHECKBOX, label = "Emergency Equipment Checked", isRequired = false)
                    )
                ),
                FormSection(id = "Weather & Conditions", title = "Weather & Conditions",
                    fields = listOf(
                        FormField(fieldName = "weather_conditions", fieldType = FormFieldType.DROPDOWN, label = "Weather Conditions", isRequired = false,
                                 options = listOf("Clear", "Cloudy", "Light Rain", "Heavy Rain", "Snow", "Windy", "Extreme Conditions")),
                        FormField(fieldName = "ambient_temperature", fieldType = FormFieldType.NUMBER, label = "Ambient Temperature (°C)", isRequired = false),
                        FormField(fieldName = "wind_speed", fieldType = FormFieldType.NUMBER, label = "Wind Speed (km/h)", isRequired = false),
                        FormField(fieldName = "visibility", fieldType = FormFieldType.DROPDOWN, label = "Visibility", isRequired = false,
                                 options = listOf("Excellent", "Good", "Fair", "Poor")),
                        FormField(fieldName = "weather_impact", fieldType = FormFieldType.MULTILINE_TEXT, label = "Weather Impact on Operations", isRequired = false)
                    )
                ),
                FormSection(id = "Comments & Actions", title = "Comments & Actions",
                    fields = listOf(
                        FormField(fieldName = "shift_comments", fieldType = FormFieldType.MULTILINE_TEXT, label = "Shift Comments", isRequired = false),
                        FormField(fieldName = "handover_notes", fieldType = FormFieldType.MULTILINE_TEXT, label = "Handover Notes for Next Shift", isRequired = false),
                        FormField(fieldName = "maintenance_requests", fieldType = FormFieldType.MULTILINE_TEXT, label = "Maintenance Requests", isRequired = false),
                        FormField(fieldName = "improvements_suggestions", fieldType = FormFieldType.MULTILINE_TEXT, label = "Process Improvements/Suggestions", isRequired = false),
                        FormField(fieldName = "follow_up_actions", fieldType = FormFieldType.MULTILINE_TEXT, label = "Follow-up Actions Required", isRequired = false)
                    )
                ),
                FormSection(id = "Sign-off & Approval", title = "Sign-off & Approval",
                    fields = listOf(
                        FormField(fieldName = "operator_signature", fieldType = FormFieldType.SIGNATURE, label = "Operator Signature", isRequired = true),
                        FormField(fieldName = "supervisor_signature", fieldType = FormFieldType.SIGNATURE, label = "Supervisor Signature", isRequired = true),
                        FormField(fieldName = "quality_inspector_signature", fieldType = FormFieldType.SIGNATURE, label = "Quality Inspector Signature", isRequired = false),
                        FormField(fieldName = "report_completion_time", fieldType = FormFieldType.TIME, label = "Report Completion Time", isRequired = true)
                    )
                )
            ),
            fields = listOf(),
            createdAt = now,
            updatedAt = now
        ),
        // Timesheet
        "timesheet" to FormTemplate(
            id = "timesheet",
            name = "Employee Timesheet",
            description = "Employee time tracking based on Copy of Timesheet(1).pdf",
            formType = FormType.TIMESHEET,
            templateFile = "templates/timesheet.json",
            pdfTemplate = "Copy of Timesheet(1).pdf",
            fieldMappings = emptyList(),
            version = "1.0",
            sections = listOf(
                FormSection(id = "Timesheet Information", title = "Timesheet Information",
                    fields = listOf(
                        FormField(fieldName = "employee_name", fieldType = FormFieldType.TEXT, label = "Employee Name", isRequired = true),
                        FormField(fieldName = "employee_id", fieldType = FormFieldType.TEXT, label = "Employee ID", isRequired = true),
                        FormField(fieldName = "week_ending", fieldType = FormFieldType.DATE, label = "Week Ending", isRequired = true),
                        FormField(fieldName = "department", fieldType = FormFieldType.TEXT, label = "Department", isRequired = true),
                        FormField(fieldName = "monday_hours", fieldType = FormFieldType.NUMBER, label = "Monday Hours", isRequired = false),
                        FormField(fieldName = "tuesday_hours", fieldType = FormFieldType.NUMBER, label = "Tuesday Hours", isRequired = false),
                        FormField(fieldName = "wednesday_hours", fieldType = FormFieldType.NUMBER, label = "Wednesday Hours", isRequired = false),
                        FormField(fieldName = "thursday_hours", fieldType = FormFieldType.NUMBER, label = "Thursday Hours", isRequired = false),
                        FormField(fieldName = "friday_hours", fieldType = FormFieldType.NUMBER, label = "Friday Hours", isRequired = false),
                        FormField(fieldName = "saturday_hours", fieldType = FormFieldType.NUMBER, label = "Saturday Hours", isRequired = false),
                        FormField(fieldName = "sunday_hours", fieldType = FormFieldType.NUMBER, label = "Sunday Hours", isRequired = false),
                        FormField(fieldName = "total_regular_hours", fieldType = FormFieldType.NUMBER, label = "Total Regular Hours", isRequired = false),
                        FormField(fieldName = "overtime_hours", fieldType = FormFieldType.NUMBER, label = "Overtime Hours", isRequired = false),
                        FormField(fieldName = "employee_signature", fieldType = FormFieldType.SIGNATURE, label = "Employee Signature", isRequired = true),
                        FormField(fieldName = "supervisor_signature", fieldType = FormFieldType.SIGNATURE, label = "Supervisor Signature", isRequired = true)
                    )
                )
            ),
            fields = listOf(),
            createdAt = now,
            updatedAt = now
        ),
        // UOR Report
        "uor_report" to FormTemplate(
            id = "uor_report",
            name = "UOR Report",
            description = "Unusual Occurrence Report based on UOR[1].pdf",
            formType = FormType.UOR_REPORT,
            templateFile = "templates/uor_report.json",
            pdfTemplate = "UOR[1].pdf",
            fieldMappings = emptyList(),
            version = "1.0",
            sections = listOf(
                FormSection(id = "Unusual Occurrence Report", title = "Unusual Occurrence Report",
                    fields = listOf(
                        FormField(fieldName = "incident_date", fieldType = FormFieldType.DATE, label = "Incident Date", isRequired = true),
                        FormField(fieldName = "site", fieldType = FormFieldType.TEXT, label = "Site", isRequired = true, isReadOnly = true),
                        FormField(fieldName = "incident_time", fieldType = FormFieldType.TIME, label = "Incident Time", isRequired = true),
                        FormField(fieldName = "location", fieldType = FormFieldType.TEXT, label = "Location", isRequired = true),
                        FormField(fieldName = "reported_by", fieldType = FormFieldType.TEXT, label = "Reported By", isRequired = true),
                        FormField(fieldName = "incident_type", fieldType = FormFieldType.DROPDOWN, label = "Incident Type", isRequired = true),
                        FormField(fieldName = "severity_level", fieldType = FormFieldType.DROPDOWN, label = "Severity Level", isRequired = true),
                        FormField(fieldName = "persons_involved", fieldType = FormFieldType.MULTILINE_TEXT, label = "Persons Involved", isRequired = false),
                        FormField(fieldName = "equipment_involved", fieldType = FormFieldType.MULTILINE_TEXT, label = "Equipment Involved", isRequired = false),
                        FormField(fieldName = "incident_description", fieldType = FormFieldType.MULTILINE_TEXT, label = "Incident Description", isRequired = true),
                        FormField(fieldName = "immediate_actions", fieldType = FormFieldType.MULTILINE_TEXT, label = "Immediate Actions Taken", isRequired = true),
                        FormField(fieldName = "root_cause", fieldType = FormFieldType.MULTILINE_TEXT, label = "Root Cause", isRequired = false),
                        FormField(fieldName = "corrective_actions", fieldType = FormFieldType.MULTILINE_TEXT, label = "Corrective Actions", isRequired = false),
                        FormField(fieldName = "preventive_measures", fieldType = FormFieldType.MULTILINE_TEXT, label = "Preventive Measures", isRequired = false),
                        FormField(fieldName = "follow_up_required", fieldType = FormFieldType.CHECKBOX, label = "Follow-up Required", isRequired = false),
                        FormField(fieldName = "follow_up_date", fieldType = FormFieldType.DATE, label = "Follow-up Date", isRequired = false),
                        FormField(fieldName = "reporter_signature", fieldType = FormFieldType.SIGNATURE, label = "Reporter Signature", isRequired = true),
                        FormField(fieldName = "supervisor_signature", fieldType = FormFieldType.SIGNATURE, label = "Supervisor Signature", isRequired = true)
                    )
                )
            ),
            fields = listOf(),
            createdAt = now,
            updatedAt = now
        ),
        
        // Fire Extinguisher Inspection Template - COMPREHENSIVE from PDF coordinate maps
        "fire_extinguisher_inspection" to FormTemplate(
            id = "fire_extinguisher_inspection",
            name = "Fire Extinguisher Inspection Checklist",
            description = "Comprehensive monthly fire extinguisher inspection checklist based on FIRE EXTINGUISHER INSPECTION CHECKLIST.pdf with ALL fields from PDF coordinate maps",
            formType = FormType.FIRE_EXTINGUISHER_INSPECTION,
            templateFile = "templates/fire_extinguisher_inspection.json",
            pdfTemplate = "FIRE EXTINGUISHER INSPECTION CHECKLIST.pdf",
            fieldMappings = emptyList(),
            version = "2.0",
            sections = listOf(
                FormSection(id = "Header Information", title = "Header Information",
                    fields = listOf(
                        FormField(fieldName = "inspection_date", fieldType = FormFieldType.DATE, label = "Inspection Date", isRequired = true),
                        FormField(fieldName = "inspection_month", fieldType = FormFieldType.DROPDOWN, label = "Inspection Month", isRequired = true,
                                 options = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")),
                        FormField(fieldName = "inspection_year", fieldType = FormFieldType.INTEGER, label = "Inspection Year", isRequired = true),
                        FormField(fieldName = "site_location", fieldType = FormFieldType.TEXT, label = "Site/Location", isRequired = true),
                        FormField(fieldName = "building_area", fieldType = FormFieldType.TEXT, label = "Building/Area", isRequired = true),
                        FormField(fieldName = "inspector_name", fieldType = FormFieldType.TEXT, label = "Inspector Name", isRequired = true),
                        FormField(fieldName = "inspector_employee_id", fieldType = FormFieldType.TEXT, label = "Inspector Employee ID", isRequired = false),
                        FormField(fieldName = "inspection_due_date", fieldType = FormFieldType.DATE, label = "Inspection Due Date", isRequired = false)
                    )
                ),
                FormSection(id = "Extinguisher Identification", title = "Extinguisher Identification",
                    fields = listOf(
                        FormField(fieldName = "extinguisher_number", fieldType = FormFieldType.TEXT, label = "Extinguisher Serial Number", isRequired = true),
                        FormField(fieldName = "extinguisher_tag_number", fieldType = FormFieldType.TEXT, label = "Extinguisher Tag Number", isRequired = true),
                        FormField(fieldName = "extinguisher_location", fieldType = FormFieldType.TEXT, label = "Exact Location Description", isRequired = true),
                        FormField(fieldName = "extinguisher_type", fieldType = FormFieldType.DROPDOWN, label = "Extinguisher Type", isRequired = true,
                                 options = listOf("Water", "Foam", "Dry Chemical", "CO2", "Wet Chemical", "Halon", "Clean Agent")),
                        FormField(fieldName = "extinguisher_size", fieldType = FormFieldType.DROPDOWN, label = "Extinguisher Size/Capacity", isRequired = true,
                                 options = listOf("2.5 lbs", "5 lbs", "10 lbs", "20 lbs", "30 lbs", "Other")),
                        FormField(fieldName = "manufacture_date", fieldType = FormFieldType.DATE, label = "Manufacture Date", isRequired = false),
                        FormField(fieldName = "last_service_date", fieldType = FormFieldType.DATE, label = "Last Service Date", isRequired = false),
                        FormField(fieldName = "next_service_due", fieldType = FormFieldType.DATE, label = "Next Service Due", isRequired = false)
                    )
                ),
                FormSection(id = "Physical Condition Assessment", title = "Physical Condition Assessment",
                    fields = listOf(
                        FormField(fieldName = "extinguisher_present", fieldType = FormFieldType.BOOLEAN, label = "Extinguisher Present at Location", isRequired = true),
                        FormField(fieldName = "mounted_properly", fieldType = FormFieldType.BOOLEAN, label = "Properly Mounted/Positioned", isRequired = true),
                        FormField(fieldName = "mounting_height_correct", fieldType = FormFieldType.BOOLEAN, label = "Mounting Height Correct", isRequired = true),
                        FormField(fieldName = "accessibility_clear", fieldType = FormFieldType.BOOLEAN, label = "Access Path Clear (36\" clearance)", isRequired = true),
                        FormField(fieldName = "visibility_unobstructed", fieldType = FormFieldType.BOOLEAN, label = "Visibility Unobstructed", isRequired = true),
                        FormField(fieldName = "physical_damage", fieldType = FormFieldType.DROPDOWN, label = "Physical Damage Assessment", isRequired = true,
                                 options = listOf("None", "Minor Dents", "Major Dents", "Corrosion", "Cracks", "Severe Damage")),
                        FormField(fieldName = "cylinder_condition", fieldType = FormFieldType.DROPDOWN, label = "Cylinder Condition", isRequired = true,
                                 options = listOf("Excellent", "Good", "Fair", "Poor", "Unacceptable")),
                        FormField(fieldName = "paint_condition", fieldType = FormFieldType.DROPDOWN, label = "Paint/Finish Condition", isRequired = true,
                                 options = listOf("Like New", "Good", "Faded", "Scratched", "Peeling", "Bare Metal"))
                    )
                ),
                FormSection(id = "Pressure & Operating Components", title = "Pressure & Operating Components",
                    fields = listOf(
                        FormField(fieldName = "pressure_gauge_present", fieldType = FormFieldType.BOOLEAN, label = "Pressure Gauge Present", isRequired = true),
                        FormField(fieldName = "pressure_gauge_reading", fieldType = FormFieldType.DROPDOWN, label = "Pressure Gauge Reading", isRequired = true,
                                 options = listOf("Green Zone - Full", "Yellow Zone - Recharge", "Red Zone - Empty", "No Reading", "Gauge Damaged")),
                        FormField(fieldName = "needle_position", fieldType = FormFieldType.TEXT, label = "Needle Position (PSI if visible)", isRequired = false),
                        FormField(fieldName = "gauge_readable", fieldType = FormFieldType.BOOLEAN, label = "Gauge Face Readable", isRequired = true),
                        FormField(fieldName = "operating_pin_present", fieldType = FormFieldType.BOOLEAN, label = "Operating Pin Present", isRequired = true),
                        FormField(fieldName = "pin_securely_inserted", fieldType = FormFieldType.BOOLEAN, label = "Pin Securely Inserted", isRequired = true),
                        FormField(fieldName = "pin_tamper_seal_intact", fieldType = FormFieldType.BOOLEAN, label = "Pin Tamper Seal Intact", isRequired = true),
                        FormField(fieldName = "handle_condition", fieldType = FormFieldType.DROPDOWN, label = "Handle/Lever Condition", isRequired = true,
                                 options = listOf("Excellent", "Good", "Loose", "Bent", "Broken", "Missing")),
                        FormField(fieldName = "trigger_mechanism", fieldType = FormFieldType.DROPDOWN, label = "Trigger Mechanism", isRequired = true,
                                 options = listOf("Functions Properly", "Stiff", "Loose", "Jammed", "Broken"))
                    )
                ),
                FormSection(id = "Hose & Nozzle Inspection", title = "Hose & Nozzle Inspection",
                    fields = listOf(
                        FormField(fieldName = "hose_present", fieldType = FormFieldType.BOOLEAN, label = "Hose Present (if applicable)", isRequired = false),
                        FormField(fieldName = "hose_condition", fieldType = FormFieldType.DROPDOWN, label = "Hose Condition", isRequired = false,
                                 options = listOf("Excellent", "Good", "Minor Cracks", "Major Cracks", "Brittle", "Damaged", "N/A")),
                        FormField(fieldName = "hose_connection_secure", fieldType = FormFieldType.BOOLEAN, label = "Hose Connection Secure", isRequired = false),
                        FormField(fieldName = "nozzle_present", fieldType = FormFieldType.BOOLEAN, label = "Nozzle Present", isRequired = true),
                        FormField(fieldName = "nozzle_clear", fieldType = FormFieldType.BOOLEAN, label = "Nozzle Clear of Obstruction", isRequired = true),
                        FormField(fieldName = "nozzle_condition", fieldType = FormFieldType.DROPDOWN, label = "Nozzle Condition", isRequired = true,
                                 options = listOf("Excellent", "Good", "Clogged", "Corroded", "Damaged", "Missing")),
                        FormField(fieldName = "discharge_test_performed", fieldType = FormFieldType.BOOLEAN, label = "Discharge Test Performed", isRequired = false),
                        FormField(fieldName = "spray_pattern_normal", fieldType = FormFieldType.BOOLEAN, label = "Spray Pattern Normal", isRequired = false)
                    )
                ),
                FormSection(id = "Labels & Documentation", title = "Labels & Documentation",
                    fields = listOf(
                        FormField(fieldName = "instruction_label_present", fieldType = FormFieldType.BOOLEAN, label = "Operating Instructions Label Present", isRequired = true),
                        FormField(fieldName = "instruction_label_legible", fieldType = FormFieldType.BOOLEAN, label = "Instructions Legible", isRequired = true),
                        FormField(fieldName = "inspection_tag_present", fieldType = FormFieldType.BOOLEAN, label = "Inspection Tag Present", isRequired = true),
                        FormField(fieldName = "inspection_tag_current", fieldType = FormFieldType.BOOLEAN, label = "Inspection Tag Current", isRequired = true),
                        FormField(fieldName = "service_tag_present", fieldType = FormFieldType.BOOLEAN, label = "Service Tag Present", isRequired = false),
                        FormField(fieldName = "ul_label_present", fieldType = FormFieldType.BOOLEAN, label = "UL/Approval Label Present", isRequired = true),
                        FormField(fieldName = "ul_label_legible", fieldType = FormFieldType.BOOLEAN, label = "UL/Approval Label Legible", isRequired = true),
                        FormField(fieldName = "class_rating_visible", fieldType = FormFieldType.TEXT, label = "Fire Class Rating (A,B,C,D,K)", isRequired = true),
                        FormField(fieldName = "capacity_marking_visible", fieldType = FormFieldType.BOOLEAN, label = "Capacity Marking Visible", isRequired = true)
                    )
                ),
                FormSection(id = "Safety & Signage", title = "Safety & Signage",
                    fields = listOf(
                        FormField(fieldName = "fire_extinguisher_sign_present", fieldType = FormFieldType.BOOLEAN, label = "Fire Extinguisher Sign Present", isRequired = true),
                        FormField(fieldName = "sign_visible_from_distance", fieldType = FormFieldType.BOOLEAN, label = "Sign Visible from 50 feet", isRequired = true),
                        FormField(fieldName = "emergency_lighting_adequate", fieldType = FormFieldType.BOOLEAN, label = "Emergency Lighting Adequate", isRequired = true),
                        FormField(fieldName = "location_marking_clear", fieldType = FormFieldType.BOOLEAN, label = "Location Marking Clear", isRequired = true),
                        FormField(fieldName = "floor_marking_visible", fieldType = FormFieldType.BOOLEAN, label = "Floor Marking Visible", isRequired = false),
                        FormField(fieldName = "area_around_clean", fieldType = FormFieldType.BOOLEAN, label = "Area Around Extinguisher Clean", isRequired = true),
                        FormField(fieldName = "safety_hazards_present", fieldType = FormFieldType.BOOLEAN, label = "Safety Hazards Present in Area", isRequired = false),
                        FormField(fieldName = "hazard_description", fieldType = FormFieldType.MULTILINE_TEXT, label = "Safety Hazard Description", isRequired = false)
                    )
                ),
                FormSection(id = "Weight & Agent Verification", title = "Weight & Agent Verification",
                    fields = listOf(
                        FormField(fieldName = "weight_check_performed", fieldType = FormFieldType.BOOLEAN, label = "Weight Check Performed", isRequired = false),
                        FormField(fieldName = "current_weight", fieldType = FormFieldType.NUMBER, label = "Current Weight (lbs)", isRequired = false),
                        FormField(fieldName = "target_weight", fieldType = FormFieldType.NUMBER, label = "Target Weight (lbs)", isRequired = false),
                        FormField(fieldName = "weight_variance_acceptable", fieldType = FormFieldType.BOOLEAN, label = "Weight Variance Acceptable", isRequired = false),
                        FormField(fieldName = "agent_settlement_check", fieldType = FormFieldType.BOOLEAN, label = "Agent Settlement Check (shake test)", isRequired = false),
                        FormField(fieldName = "agent_flows_freely", fieldType = FormFieldType.BOOLEAN, label = "Agent Flows Freely", isRequired = false),
                        FormField(fieldName = "inversion_test_performed", fieldType = FormFieldType.BOOLEAN, label = "Inversion Test Performed", isRequired = false),
                        FormField(fieldName = "agent_discharge_normal", fieldType = FormFieldType.BOOLEAN, label = "Agent Discharge Normal", isRequired = false)
                    )
                ),
                FormSection(id = "Functional Testing", title = "Functional Testing",
                    fields = listOf(
                        FormField(fieldName = "trigger_pull_test", fieldType = FormFieldType.DROPDOWN, label = "Trigger Pull Test", isRequired = false,
                                 options = listOf("Not Performed", "Normal Resistance", "Too Easy", "Too Hard", "Jammed")),
                        FormField(fieldName = "pin_removal_test", fieldType = FormFieldType.DROPDOWN, label = "Pin Removal Test", isRequired = false,
                                 options = listOf("Not Performed", "Removes Easily", "Difficult", "Cannot Remove")),
                        FormField(fieldName = "seal_replacement_needed", fieldType = FormFieldType.BOOLEAN, label = "New Seal Required", isRequired = false),
                        FormField(fieldName = "lock_mechanism_test", fieldType = FormFieldType.DROPDOWN, label = "Lock Mechanism Test", isRequired = false,
                                 options = listOf("Not Applicable", "Functions Properly", "Loose", "Tight", "Broken")),
                        FormField(fieldName = "gauge_tap_test", fieldType = FormFieldType.BOOLEAN, label = "Pressure Gauge Tap Test Performed", isRequired = false),
                        FormField(fieldName = "gauge_response_normal", fieldType = FormFieldType.BOOLEAN, label = "Gauge Response Normal", isRequired = false)
                    )
                ),
                FormSection(id = "Deficiencies & Corrective Actions", title = "Deficiencies & Corrective Actions",
                    fields = listOf(
                        FormField(fieldName = "deficiencies_found", fieldType = FormFieldType.BOOLEAN, label = "Deficiencies Found", isRequired = true),
                        FormField(fieldName = "deficiency_description", fieldType = FormFieldType.MULTILINE_TEXT, label = "Deficiency Description", isRequired = false),
                        FormField(fieldName = "immediate_action_required", fieldType = FormFieldType.BOOLEAN, label = "Immediate Action Required", isRequired = false),
                        FormField(fieldName = "extinguisher_removed_service", fieldType = FormFieldType.BOOLEAN, label = "Extinguisher Removed from Service", isRequired = false),
                        FormField(fieldName = "corrective_action_taken", fieldType = FormFieldType.MULTILINE_TEXT, label = "Corrective Action Taken", isRequired = false),
                        FormField(fieldName = "followup_required", fieldType = FormFieldType.BOOLEAN, label = "Follow-up Required", isRequired = false),
                        FormField(fieldName = "followup_date", fieldType = FormFieldType.DATE, label = "Follow-up Date", isRequired = false),
                        FormField(fieldName = "work_order_number", fieldType = FormFieldType.TEXT, label = "Work Order Number", isRequired = false),
                        FormField(fieldName = "replacement_extinguisher_installed", fieldType = FormFieldType.BOOLEAN, label = "Replacement Extinguisher Installed", isRequired = false)
                    )
                ),
                FormSection(id = "Overall Assessment", title = "Overall Assessment",
                    fields = listOf(
                        FormField(fieldName = "overall_condition", fieldType = FormFieldType.DROPDOWN, label = "Overall Condition Assessment", isRequired = true,
                                 options = listOf("Excellent - No Issues", "Good - Minor Issues", "Fair - Moderate Issues", "Poor - Major Issues", "Unacceptable - Replace Immediately")),
                        FormField(fieldName = "ready_for_emergency_use", fieldType = FormFieldType.BOOLEAN, label = "Ready for Emergency Use", isRequired = true),
                        FormField(fieldName = "compliance_status", fieldType = FormFieldType.DROPDOWN, label = "Compliance Status", isRequired = true,
                                 options = listOf("Fully Compliant", "Minor Non-Compliance", "Major Non-Compliance", "Critical Non-Compliance")),
                        FormField(fieldName = "recommendation", fieldType = FormFieldType.DROPDOWN, label = "Recommendation", isRequired = true,
                                 options = listOf("Continue Service", "Schedule Maintenance", "Replace Soon", "Replace Immediately", "Remove from Service")),
                        FormField(fieldName = "inspector_notes", fieldType = FormFieldType.MULTILINE_TEXT, label = "Inspector Notes", isRequired = false),
                        FormField(fieldName = "special_conditions", fieldType = FormFieldType.MULTILINE_TEXT, label = "Special Environmental Conditions", isRequired = false)
                    )
                ),
                FormSection(id = "Completion & Sign-off", title = "Completion & Sign-off",
                    fields = listOf(
                        FormField(fieldName = "inspection_completed_time", fieldType = FormFieldType.TIME, label = "Inspection Completed Time", isRequired = false),
                        FormField(fieldName = "next_inspection_date", fieldType = FormFieldType.DATE, label = "Next Monthly Inspection Due", isRequired = true),
                        FormField(fieldName = "next_service_date", fieldType = FormFieldType.DATE, label = "Next Service Date", isRequired = false),
                        FormField(fieldName = "inspector_signature", fieldType = FormFieldType.SIGNATURE, label = "Inspector Signature", isRequired = true),
                        FormField(fieldName = "supervisor_review_required", fieldType = FormFieldType.BOOLEAN, label = "Supervisor Review Required", isRequired = false),
                        FormField(fieldName = "supervisor_signature", fieldType = FormFieldType.SIGNATURE, label = "Supervisor Signature", isRequired = false),
                        FormField(fieldName = "date_entered_system", fieldType = FormFieldType.DATE, label = "Date Entered in System", isRequired = false),
                        FormField(fieldName = "record_retention_date", fieldType = FormFieldType.DATE, label = "Record Retention Until", isRequired = false)
                    )
                )
            ),
            fields = listOf(),
            createdAt = now,
            updatedAt = now
        ),
        
        // MMU Handover Certificate Template
        "mmu_handover_certificate" to FormTemplate(
            id = "mmu_handover_certificate",
            name = "MMU Handover Certificate",
            description = "Mobile Manufacturing Unit handover certificate",
            formType = FormType.MMU_HANDOVER_CERTIFICATE,
            templateFile = "templates/mmu_handover_certificate.json",
            pdfTemplate = "MMU HANDOVER CERTIFICATE.pdf",
            fieldMappings = emptyList(),
            version = "1.0",
            sections = listOf(
                FormSection(id = "Handover Details", title = "Handover Details",
                    fields = listOf(
                        FormField(fieldName = "handover_date", fieldType = FormFieldType.DATE, label = "Handover Date", isRequired = true),
                        FormField(fieldName = "shift_from", fieldType = FormFieldType.TEXT, label = "Shift From", isRequired = true),
                        FormField(fieldName = "shift_to", fieldType = FormFieldType.TEXT, label = "Shift To", isRequired = true),
                        FormField(fieldName = "mmu_number", fieldType = FormFieldType.TEXT, label = "MMU Number", isRequired = true),
                        FormField(fieldName = "location", fieldType = FormFieldType.TEXT, label = "Location", isRequired = true),
                        FormField(fieldName = "operator_from", fieldType = FormFieldType.TEXT, label = "Operator Handing Over", isRequired = true),
                        FormField(fieldName = "operator_to", fieldType = FormFieldType.TEXT, label = "Operator Taking Over", isRequired = true),
                        FormField(fieldName = "fuel_level", fieldType = FormFieldType.NUMBER, label = "Fuel Level (%)", isRequired = true),
                        FormField(fieldName = "engine_hours", fieldType = FormFieldType.NUMBER, label = "Engine Hours", isRequired = true),
                        FormField(fieldName = "hydraulic_oil_level", fieldType = FormFieldType.DROPDOWN, label = "Hydraulic Oil Level", isRequired = true,
                                 options = listOf("Full", "3/4", "1/2", "1/4", "Low")),
                        FormField(fieldName = "engine_oil_level", fieldType = FormFieldType.DROPDOWN, label = "Engine Oil Level", isRequired = true,
                                 options = listOf("Full", "3/4", "1/2", "1/4", "Low")),
                        FormField(fieldName = "coolant_level", fieldType = FormFieldType.DROPDOWN, label = "Coolant Level", isRequired = true,
                                 options = listOf("Full", "3/4", "1/2", "1/4", "Low")),
                        FormField(fieldName = "grease_level", fieldType = FormFieldType.DROPDOWN, label = "Grease Level", isRequired = true,
                                 options = listOf("Full", "3/4", "1/2", "1/4", "Low")),
                        FormField(fieldName = "equipment_condition", fieldType = FormFieldType.DROPDOWN, label = "Overall Equipment Condition", isRequired = true,
                                 options = listOf("Excellent", "Good", "Fair", "Poor")),
                        FormField(fieldName = "defects_reported", fieldType = FormFieldType.MULTILINE_TEXT, label = "Defects Reported", isRequired = false),
                        FormField(fieldName = "work_completed", fieldType = FormFieldType.MULTILINE_TEXT, label = "Work Completed This Shift", isRequired = false),
                        FormField(fieldName = "handover_notes", fieldType = FormFieldType.MULTILINE_TEXT, label = "Handover Notes", isRequired = false)
                    )
                )
            ),
            fields = listOf(),
            createdAt = now,
            updatedAt = now
        ),
        
        // Pre-task Safety Check Template - COMPREHENSIVE from PDF coordinate maps
        "pretask_safety_check" to FormTemplate(
            id = "pretask_safety_check",
            name = "Pre-task Safety Check",
            description = "Comprehensive pre-task safety hazard identification and mitigation planning based on pretask.pdf with ALL fields from PDF coordinate maps",
            formType = FormType.PRETASK_SAFETY,
            templateFile = "templates/pretask_safety_check.json",
            pdfTemplate = "pretask.pdf",
            fieldMappings = emptyList(),
            version = "2.0",
            sections = listOf(
                FormSection(id = "Task Identification", title = "Task Identification",
                    fields = listOf(
                        FormField(fieldName = "analysis_date", fieldType = FormFieldType.DATE, label = "Analysis Date", isRequired = true),
                        FormField(fieldName = "task_description", fieldType = FormFieldType.MULTILINE_TEXT, label = "Task Description", isRequired = true),
                        FormField(fieldName = "work_location", fieldType = FormFieldType.TEXT, label = "Work Location", isRequired = true),
                        FormField(fieldName = "department", fieldType = FormFieldType.TEXT, label = "Department", isRequired = true),
                        FormField(fieldName = "job_number", fieldType = FormFieldType.TEXT, label = "Job/Work Order Number", isRequired = false),
                        FormField(fieldName = "estimated_duration", fieldType = FormFieldType.TEXT, label = "Estimated Task Duration", isRequired = true),
                        FormField(fieldName = "task_priority", fieldType = FormFieldType.DROPDOWN, label = "Task Priority", isRequired = true,
                                 options = listOf("Critical", "High", "Medium", "Low", "Routine")),
                        FormField(fieldName = "scheduled_start_time", fieldType = FormFieldType.TIME, label = "Scheduled Start Time", isRequired = true),
                        FormField(fieldName = "scheduled_completion_time", fieldType = FormFieldType.TIME, label = "Scheduled Completion Time", isRequired = true)
                    )
                ),
                FormSection(id = "Personnel Information", title = "Personnel Information",
                    fields = listOf(
                        FormField(fieldName = "team_leader_name", fieldType = FormFieldType.TEXT, label = "Team Leader Name", isRequired = true),
                        FormField(fieldName = "team_leader_employee_id", fieldType = FormFieldType.TEXT, label = "Team Leader Employee ID", isRequired = false),
                        FormField(fieldName = "supervisor_name", fieldType = FormFieldType.TEXT, label = "Supervisor Name", isRequired = true),
                        FormField(fieldName = "safety_officer_name", fieldType = FormFieldType.TEXT, label = "Safety Officer Name", isRequired = false),
                        FormField(fieldName = "total_personnel_count", fieldType = FormFieldType.INTEGER, label = "Total Personnel Involved", isRequired = true),
                        FormField(fieldName = "experienced_personnel_count", fieldType = FormFieldType.INTEGER, label = "Experienced Personnel", isRequired = false),
                        FormField(fieldName = "trainee_personnel_count", fieldType = FormFieldType.INTEGER, label = "Trainee Personnel", isRequired = false),
                        FormField(fieldName = "contractor_personnel_involved", fieldType = FormFieldType.CHECKBOX, label = "Contractor Personnel Involved", isRequired = false),
                        FormField(fieldName = "contractor_company_name", fieldType = FormFieldType.TEXT, label = "Contractor Company Name", isRequired = false)
                    )
                ),
                FormSection(id = "Equipment & Tools Assessment", title = "Equipment & Tools Assessment",
                    fields = listOf(
                        FormField(fieldName = "primary_equipment_list", fieldType = FormFieldType.MULTILINE_TEXT, label = "Primary Equipment Required", isRequired = true),
                        FormField(fieldName = "hand_tools_required", fieldType = FormFieldType.MULTILINE_TEXT, label = "Hand Tools Required", isRequired = false),
                        FormField(fieldName = "specialized_tools", fieldType = FormFieldType.MULTILINE_TEXT, label = "Specialized Tools/Equipment", isRequired = false),
                        FormField(fieldName = "equipment_inspection_required", fieldType = FormFieldType.CHECKBOX, label = "Equipment Pre-Use Inspection Required", isRequired = false),
                        FormField(fieldName = "equipment_certification_current", fieldType = FormFieldType.CHECKBOX, label = "Equipment Certifications Current", isRequired = false),
                        FormField(fieldName = "lifting_equipment_involved", fieldType = FormFieldType.CHECKBOX, label = "Lifting Equipment Involved", isRequired = false),
                        FormField(fieldName = "electrical_equipment_involved", fieldType = FormFieldType.CHECKBOX, label = "Electrical Equipment Involved", isRequired = false),
                        FormField(fieldName = "hot_work_equipment", fieldType = FormFieldType.CHECKBOX, label = "Hot Work Equipment", isRequired = false)
                    )
                ),
                FormSection(id = "Environmental Conditions Assessment", title = "Environmental Conditions Assessment",
                    fields = listOf(
                        FormField(fieldName = "weather_forecast", fieldType = FormFieldType.DROPDOWN, label = "Weather Forecast", isRequired = true,
                                 options = listOf("Clear", "Cloudy", "Rain Expected", "Storm Warning", "High Winds", "Extreme Heat", "Cold Weather")),
                        FormField(fieldName = "wind_conditions", fieldType = FormFieldType.DROPDOWN, label = "Wind Conditions", isRequired = true,
                                 options = listOf("Calm", "Light", "Moderate", "Strong", "Extreme")),
                        FormField(fieldName = "visibility_conditions", fieldType = FormFieldType.DROPDOWN, label = "Visibility Conditions", isRequired = true,
                                 options = listOf("Excellent", "Good", "Fair", "Poor", "Very Poor")),
                        FormField(fieldName = "ground_conditions", fieldType = FormFieldType.DROPDOWN, label = "Ground Conditions", isRequired = true,
                                 options = listOf("Dry & Stable", "Wet", "Muddy", "Icy", "Uneven", "Soft", "Rocky")),
                        FormField(fieldName = "lighting_conditions", fieldType = FormFieldType.DROPDOWN, label = "Lighting Conditions", isRequired = true,
                                 options = listOf("Natural Daylight", "Artificial Lighting", "Low Light", "Night Work")),
                        FormField(fieldName = "noise_levels", fieldType = FormFieldType.DROPDOWN, label = "Expected Noise Levels", isRequired = true,
                                 options = listOf("Low", "Moderate", "High", "Extreme")),
                        FormField(fieldName = "dust_generation_expected", fieldType = FormFieldType.CHECKBOX, label = "Dust Generation Expected", isRequired = false),
                        FormField(fieldName = "vibration_expected", fieldType = FormFieldType.CHECKBOX, label = "Vibration Expected", isRequired = false)
                    )
                ),
                FormSection(id = "Hazard Identification - Physical Hazards", title = "Hazard Identification - Physical Hazards",
                    fields = listOf(
                        FormField(fieldName = "struck_by_hazard", fieldType = FormFieldType.CHECKBOX, label = "Struck By Moving Objects", isRequired = false),
                        FormField(fieldName = "struck_against_hazard", fieldType = FormFieldType.CHECKBOX, label = "Struck Against Fixed Objects", isRequired = false),
                        FormField(fieldName = "caught_in_between_hazard", fieldType = FormFieldType.CHECKBOX, label = "Caught In/Between", isRequired = false),
                        FormField(fieldName = "fall_from_height_hazard", fieldType = FormFieldType.CHECKBOX, label = "Fall from Height", isRequired = false),
                        FormField(fieldName = "fall_same_level_hazard", fieldType = FormFieldType.CHECKBOX, label = "Fall on Same Level", isRequired = false),
                        FormField(fieldName = "lifting_manual_hazard", fieldType = FormFieldType.CHECKBOX, label = "Manual Lifting/Handling", isRequired = false),
                        FormField(fieldName = "lifting_mechanical_hazard", fieldType = FormFieldType.CHECKBOX, label = "Mechanical Lifting", isRequired = false),
                        FormField(fieldName = "crushing_hazard", fieldType = FormFieldType.CHECKBOX, label = "Crushing/Compression", isRequired = false),
                        FormField(fieldName = "pinch_point_hazard", fieldType = FormFieldType.CHECKBOX, label = "Pinch Points", isRequired = false),
                        FormField(fieldName = "rotating_equipment_hazard", fieldType = FormFieldType.CHECKBOX, label = "Rotating Equipment", isRequired = false)
                    )
                ),
                FormSection(id = "Hazard Identification - Chemical/Environmental", title = "Hazard Identification - Chemical/Environmental",
                    fields = listOf(
                        FormField(fieldName = "chemical_exposure_hazard", fieldType = FormFieldType.CHECKBOX, label = "Chemical Exposure", isRequired = false),
                        FormField(fieldName = "toxic_gas_hazard", fieldType = FormFieldType.CHECKBOX, label = "Toxic Gas Exposure", isRequired = false),
                        FormField(fieldName = "oxygen_deficiency_hazard", fieldType = FormFieldType.CHECKBOX, label = "Oxygen Deficiency", isRequired = false),
                        FormField(fieldName = "dust_inhalation_hazard", fieldType = FormFieldType.CHECKBOX, label = "Dust Inhalation", isRequired = false),
                        FormField(fieldName = "skin_contact_hazard", fieldType = FormFieldType.CHECKBOX, label = "Skin Contact with Chemicals", isRequired = false),
                        FormField(fieldName = "eye_contact_hazard", fieldType = FormFieldType.CHECKBOX, label = "Eye Contact with Chemicals", isRequired = false),
                        FormField(fieldName = "fire_hazard", fieldType = FormFieldType.CHECKBOX, label = "Fire Risk", isRequired = false),
                        FormField(fieldName = "explosion_hazard", fieldType = FormFieldType.CHECKBOX, label = "Explosion Risk", isRequired = false),
                        FormField(fieldName = "confined_space_hazard", fieldType = FormFieldType.CHECKBOX, label = "Confined Space Entry", isRequired = false),
                        FormField(fieldName = "heat_stress_hazard", fieldType = FormFieldType.CHECKBOX, label = "Heat Stress", isRequired = false)
                    )
                ),
                FormSection(id = "Hazard Identification - Electrical/Energy", title = "Hazard Identification - Electrical/Energy",
                    fields = listOf(
                        FormField(fieldName = "electrical_shock_hazard", fieldType = FormFieldType.CHECKBOX, label = "Electrical Shock", isRequired = false),
                        FormField(fieldName = "arc_flash_hazard", fieldType = FormFieldType.CHECKBOX, label = "Arc Flash", isRequired = false),
                        FormField(fieldName = "stored_energy_hazard", fieldType = FormFieldType.CHECKBOX, label = "Stored Energy Release", isRequired = false),
                        FormField(fieldName = "pressure_release_hazard", fieldType = FormFieldType.CHECKBOX, label = "Pressure Release", isRequired = false),
                        FormField(fieldName = "hot_surfaces_hazard", fieldType = FormFieldType.CHECKBOX, label = "Hot Surfaces/Burns", isRequired = false),
                        FormField(fieldName = "cold_surfaces_hazard", fieldType = FormFieldType.CHECKBOX, label = "Cold Surfaces/Frostbite", isRequired = false),
                        FormField(fieldName = "lockout_tagout_required", fieldType = FormFieldType.CHECKBOX, label = "Lockout/Tagout Required", isRequired = false),
                        FormField(fieldName = "high_voltage_present", fieldType = FormFieldType.CHECKBOX, label = "High Voltage Present", isRequired = false)
                    )
                ),
                FormSection(id = "Risk Assessment Matrix", title = "Risk Assessment Matrix",
                    fields = listOf(
                        FormField(fieldName = "highest_risk_hazard", fieldType = FormFieldType.TEXT, label = "Highest Risk Hazard Identified", isRequired = true),
                        FormField(fieldName = "probability_rating", fieldType = FormFieldType.DROPDOWN, label = "Probability Rating", isRequired = true,
                                 options = listOf("1 - Rare", "2 - Unlikely", "3 - Possible", "4 - Likely", "5 - Almost Certain")),
                        FormField(fieldName = "severity_rating", fieldType = FormFieldType.DROPDOWN, label = "Severity Rating", isRequired = true,
                                 options = listOf("1 - Insignificant", "2 - Minor", "3 - Moderate", "4 - Major", "5 - Catastrophic")),
                        FormField(fieldName = "initial_risk_level", fieldType = FormFieldType.DROPDOWN, label = "Initial Risk Level", isRequired = true,
                                 options = listOf("Low (1-4)", "Medium (5-9)", "High (10-15)", "Extreme (16-25)")),
                        FormField(fieldName = "risk_tolerance", fieldType = FormFieldType.DROPDOWN, label = "Risk Tolerance", isRequired = true,
                                 options = listOf("Acceptable", "Tolerable with Controls", "Unacceptable - Stop Work"))
                    )
                ),
                FormSection(id = "Control Measures - Engineering Controls", title = "Control Measures - Engineering Controls",
                    fields = listOf(
                        FormField(fieldName = "ventilation_required", fieldType = FormFieldType.CHECKBOX, label = "Ventilation System Required", isRequired = false),
                        FormField(fieldName = "barriers_guards_required", fieldType = FormFieldType.CHECKBOX, label = "Barriers/Guards Required", isRequired = false),
                        FormField(fieldName = "isolation_systems", fieldType = FormFieldType.CHECKBOX, label = "Isolation Systems Required", isRequired = false),
                        FormField(fieldName = "fall_protection_systems", fieldType = FormFieldType.CHECKBOX, label = "Fall Protection Systems", isRequired = false),
                        FormField(fieldName = "emergency_shutdown_systems", fieldType = FormFieldType.CHECKBOX, label = "Emergency Shutdown Systems", isRequired = false),
                        FormField(fieldName = "noise_control_measures", fieldType = FormFieldType.CHECKBOX, label = "Noise Control Measures", isRequired = false),
                        FormField(fieldName = "engineering_controls_details", fieldType = FormFieldType.MULTILINE_TEXT, label = "Engineering Controls Details", isRequired = false)
                    )
                ),
                FormSection(id = "Control Measures - Administrative Controls", title = "Control Measures - Administrative Controls",
                    fields = listOf(
                        FormField(fieldName = "work_permits_required", fieldType = FormFieldType.MULTILINE_TEXT, label = "Work Permits Required", isRequired = false),
                        FormField(fieldName = "training_requirements", fieldType = FormFieldType.MULTILINE_TEXT, label = "Training Requirements", isRequired = false),
                        FormField(fieldName = "supervision_level", fieldType = FormFieldType.DROPDOWN, label = "Supervision Level Required", isRequired = true,
                                 options = listOf("Continuous", "Frequent", "Periodic", "Initial Only")),
                        FormField(fieldName = "communication_procedures", fieldType = FormFieldType.MULTILINE_TEXT, label = "Communication Procedures", isRequired = false),
                        FormField(fieldName = "emergency_procedures", fieldType = FormFieldType.MULTILINE_TEXT, label = "Emergency Procedures", isRequired = true),
                        FormField(fieldName = "work_rotation_required", fieldType = FormFieldType.CHECKBOX, label = "Work Rotation Required", isRequired = false),
                        FormField(fieldName = "buddy_system_required", fieldType = FormFieldType.CHECKBOX, label = "Buddy System Required", isRequired = false)
                    )
                ),
                FormSection(id = "Personal Protective Equipment (PPE)", title = "Personal Protective Equipment (PPE)",
                    fields = listOf(
                        FormField(fieldName = "hard_hat_required", fieldType = FormFieldType.CHECKBOX, label = "Hard Hat Required", isRequired = false),
                        FormField(fieldName = "safety_glasses_required", fieldType = FormFieldType.CHECKBOX, label = "Safety Glasses Required", isRequired = false),
                        FormField(fieldName = "hearing_protection_required", fieldType = FormFieldType.CHECKBOX, label = "Hearing Protection Required", isRequired = false),
                        FormField(fieldName = "respiratory_protection_required", fieldType = FormFieldType.CHECKBOX, label = "Respiratory Protection Required", isRequired = false),
                        FormField(fieldName = "gloves_required", fieldType = FormFieldType.CHECKBOX, label = "Protective Gloves Required", isRequired = false),
                        FormField(fieldName = "safety_footwear_required", fieldType = FormFieldType.CHECKBOX, label = "Safety Footwear Required", isRequired = false),
                        FormField(fieldName = "fall_arrest_harness_required", fieldType = FormFieldType.CHECKBOX, label = "Fall Arrest Harness Required", isRequired = false),
                        FormField(fieldName = "chemical_suit_required", fieldType = FormFieldType.CHECKBOX, label = "Chemical Protective Suit Required", isRequired = false),
                        FormField(fieldName = "high_visibility_clothing_required", fieldType = FormFieldType.CHECKBOX, label = "High Visibility Clothing Required", isRequired = false),
                        FormField(fieldName = "ppe_inspection_required", fieldType = FormFieldType.CHECKBOX, label = "PPE Pre-Use Inspection Required", isRequired = false)
                    )
                ),
                FormSection(id = "Emergency Preparedness", title = "Emergency Preparedness",
                    fields = listOf(
                        FormField(fieldName = "emergency_contact_numbers", fieldType = FormFieldType.MULTILINE_TEXT, label = "Emergency Contact Numbers", isRequired = true),
                        FormField(fieldName = "nearest_medical_facility", fieldType = FormFieldType.TEXT, label = "Nearest Medical Facility", isRequired = true),
                        FormField(fieldName = "first_aid_personnel_available", fieldType = FormFieldType.CHECKBOX, label = "First Aid Personnel Available", isRequired = false),
                        FormField(fieldName = "fire_extinguisher_locations", fieldType = FormFieldType.TEXT, label = "Fire Extinguisher Locations", isRequired = false),
                        FormField(fieldName = "emergency_assembly_point", fieldType = FormFieldType.TEXT, label = "Emergency Assembly Point", isRequired = true),
                        FormField(fieldName = "evacuation_routes", fieldType = FormFieldType.MULTILINE_TEXT, label = "Evacuation Routes", isRequired = true),
                        FormField(fieldName = "emergency_equipment_available", fieldType = FormFieldType.MULTILINE_TEXT, label = "Emergency Equipment Available", isRequired = false),
                        FormField(fieldName = "spill_response_materials", fieldType = FormFieldType.CHECKBOX, label = "Spill Response Materials Available", isRequired = false)
                    )
                ),
                FormSection(id = "Final Risk Assessment", title = "Final Risk Assessment",
                    fields = listOf(
                        FormField(fieldName = "residual_risk_level", fieldType = FormFieldType.DROPDOWN, label = "Residual Risk Level (After Controls)", isRequired = true,
                                 options = listOf("Low", "Medium", "High", "Extreme")),
                        FormField(fieldName = "risk_acceptable", fieldType = FormFieldType.DROPDOWN, label = "Is Residual Risk Acceptable?", isRequired = true,
                                 options = listOf("Yes - Proceed", "No - Additional Controls Needed", "No - Stop Work")),
                        FormField(fieldName = "additional_controls_needed", fieldType = FormFieldType.MULTILINE_TEXT, label = "Additional Controls Needed", isRequired = false),
                        FormField(fieldName = "work_authorization", fieldType = FormFieldType.DROPDOWN, label = "Work Authorization", isRequired = true,
                                 options = listOf("Authorized to Proceed", "Conditional Authorization", "Not Authorized - Stop")),
                        FormField(fieldName = "special_conditions", fieldType = FormFieldType.MULTILINE_TEXT, label = "Special Conditions/Restrictions", isRequired = false)
                    )
                ),
                FormSection(id = "Approvals & Sign-offs", title = "Approvals & Sign-offs",
                    fields = listOf(
                        FormField(fieldName = "team_leader_signature", fieldType = FormFieldType.SIGNATURE, label = "Team Leader Signature", isRequired = true),
                        FormField(fieldName = "supervisor_signature", fieldType = FormFieldType.SIGNATURE, label = "Supervisor Signature", isRequired = true),
                        FormField(fieldName = "safety_officer_signature", fieldType = FormFieldType.SIGNATURE, label = "Safety Officer Signature", isRequired = false),
                        FormField(fieldName = "analysis_review_date", fieldType = FormFieldType.DATE, label = "Analysis Review Date", isRequired = false),
                        FormField(fieldName = "next_review_required", fieldType = FormFieldType.DATE, label = "Next Review Required", isRequired = false),
                        FormField(fieldName = "analysis_valid_until", fieldType = FormFieldType.DATE, label = "Analysis Valid Until", isRequired = true),
                        FormField(fieldName = "post_task_review_required", fieldType = FormFieldType.CHECKBOX, label = "Post-Task Review Required", isRequired = false)
                    )
                )
            ),
            fields = listOf(),
            createdAt = now,
            updatedAt = now
        ),

        // Blast Hole Log Template - COMPREHENSIVE from PDF coordinate maps
        "blast_hole_log" to FormTemplate(
            id = "blast_hole_log",
            name = "Blast Hole Log",
            description = "Comprehensive blast hole drilling and charging log based on blast hole log.pdf with ALL fields from PDF coordinate maps",
            formType = FormType.BLAST_HOLE_LOG,
            templateFile = "templates/blast_hole_log.json",
            pdfTemplate = "blast hole log.pdf",
            fieldMappings = emptyList(),
            version = "2.0",
            sections = listOf(
                FormSection(id = "Header Information", title = "Header Information",
                    fields = listOf(
                        FormField(fieldName = "log_date", fieldType = FormFieldType.DATE, label = "Log Date", isRequired = true),
                        FormField(fieldName = "blast_date", fieldType = FormFieldType.DATE, label = "Planned Blast Date", isRequired = true),
                        FormField(fieldName = "blast_number", fieldType = FormFieldType.TEXT, label = "Blast Number", isRequired = true),
                        FormField(fieldName = "bench_number", fieldType = FormFieldType.TEXT, label = "Bench Number", isRequired = true),
                        FormField(fieldName = "pit_name", fieldType = FormFieldType.TEXT, label = "Pit/Mine Name", isRequired = true),
                        FormField(fieldName = "location_coordinates", fieldType = FormFieldType.TEXT, label = "Location Coordinates", isRequired = false),
                        FormField(fieldName = "elevation", fieldType = FormFieldType.NUMBER, label = "Elevation (m)", isRequired = false)
                    )
                ),
                FormSection(id = "Personnel & Authorization", title = "Personnel & Authorization",
                    fields = listOf(
                        FormField(fieldName = "shift", fieldType = FormFieldType.DROPDOWN, label = "Shift", isRequired = true,
                                 options = listOf("Day", "Night", "Swing")),
                        FormField(fieldName = "blaster_name", fieldType = FormFieldType.TEXT, label = "Licensed Blaster Name", isRequired = true),
                        FormField(fieldName = "blaster_license_number", fieldType = FormFieldType.TEXT, label = "Blaster License Number", isRequired = true),
                        FormField(fieldName = "supervisor_name", fieldType = FormFieldType.TEXT, label = "Supervisor Name", isRequired = true),
                        FormField(fieldName = "driller_name", fieldType = FormFieldType.TEXT, label = "Driller Name", isRequired = false),
                        FormField(fieldName = "safety_officer", fieldType = FormFieldType.TEXT, label = "Safety Officer", isRequired = false),
                        FormField(fieldName = "blast_permit_number", fieldType = FormFieldType.TEXT, label = "Blast Permit Number", isRequired = true)
                    )
                ),
                FormSection(id = "Blast Design Parameters", title = "Blast Design Parameters",
                    fields = listOf(
                        FormField(fieldName = "hole_diameter", fieldType = FormFieldType.NUMBER, label = "Hole Diameter (mm)", isRequired = true),
                        FormField(fieldName = "hole_depth_planned", fieldType = FormFieldType.NUMBER, label = "Planned Hole Depth (m)", isRequired = true),
                        FormField(fieldName = "hole_spacing", fieldType = FormFieldType.NUMBER, label = "Hole Spacing (m)", isRequired = true),
                        FormField(fieldName = "burden_distance", fieldType = FormFieldType.NUMBER, label = "Burden Distance (m)", isRequired = true),
                        FormField(fieldName = "bench_height", fieldType = FormFieldType.NUMBER, label = "Bench Height (m)", isRequired = true),
                        FormField(fieldName = "sub_drill_depth", fieldType = FormFieldType.NUMBER, label = "Sub-drill Depth (m)", isRequired = true),
                        FormField(fieldName = "stemming_length", fieldType = FormFieldType.NUMBER, label = "Stemming Length (m)", isRequired = true),
                        FormField(fieldName = "total_holes_planned", fieldType = FormFieldType.INTEGER, label = "Total Holes Planned", isRequired = true)
                    )
                ),
                FormSection(id = "Drilling Information", title = "Drilling Information",
                    fields = listOf(
                        FormField(fieldName = "drill_rig_id", fieldType = FormFieldType.TEXT, label = "Drill Rig ID", isRequired = true),
                        FormField(fieldName = "drilling_start_time", fieldType = FormFieldType.TIME, label = "Drilling Start Time", isRequired = false),
                        FormField(fieldName = "drilling_end_time", fieldType = FormFieldType.TIME, label = "Drilling End Time", isRequired = false),
                        FormField(fieldName = "drill_bit_size", fieldType = FormFieldType.NUMBER, label = "Drill Bit Size (mm)", isRequired = false),
                        FormField(fieldName = "drilling_rate", fieldType = FormFieldType.NUMBER, label = "Average Drilling Rate (m/min)", isRequired = false),
                        FormField(fieldName = "drilling_issues", fieldType = FormFieldType.MULTILINE_TEXT, label = "Drilling Issues Encountered", isRequired = false),
                        FormField(fieldName = "ground_conditions", fieldType = FormFieldType.MULTILINE_TEXT, label = "Ground Conditions Observed", isRequired = false)
                    )
                ),
                FormSection(id = "Hole Log Details (Individual Holes)", title = "Hole Log Details (Individual Holes)",
                    fields = listOf(
                        FormField(fieldName = "hole_number_range", fieldType = FormFieldType.TEXT, label = "Hole Number Range (e.g., 1-25)", isRequired = true),
                        FormField(fieldName = "actual_depth_variance", fieldType = FormFieldType.MULTILINE_TEXT, label = "Actual Depth Variances from Plan", isRequired = false),
                        FormField(fieldName = "hole_deviation_notes", fieldType = FormFieldType.MULTILINE_TEXT, label = "Hole Deviation Notes", isRequired = false),
                        FormField(fieldName = "water_encountered", fieldType = FormFieldType.CHECKBOX, label = "Water Encountered in Holes", isRequired = false),
                        FormField(fieldName = "water_level_depth", fieldType = FormFieldType.NUMBER, label = "Water Level Depth (m)", isRequired = false),
                        FormField(fieldName = "geological_changes", fieldType = FormFieldType.MULTILINE_TEXT, label = "Geological Changes Noted", isRequired = false),
                        FormField(fieldName = "hole_condition_issues", fieldType = FormFieldType.MULTILINE_TEXT, label = "Hole Condition Issues", isRequired = false)
                    )
                ),
                FormSection(id = "Explosive Materials", title = "Explosive Materials",
                    fields = listOf(
                        FormField(fieldName = "primary_explosive_type", fieldType = FormFieldType.TEXT, label = "Primary Explosive Type", isRequired = true),
                        FormField(fieldName = "explosive_manufacturer", fieldType = FormFieldType.TEXT, label = "Explosive Manufacturer", isRequired = false),
                        FormField(fieldName = "explosive_batch_number", fieldType = FormFieldType.TEXT, label = "Explosive Batch Number", isRequired = false),
                        FormField(fieldName = "explosive_per_hole_kg", fieldType = FormFieldType.NUMBER, label = "Explosive per Hole (kg)", isRequired = true),
                        FormField(fieldName = "total_explosive_used_kg", fieldType = FormFieldType.NUMBER, label = "Total Explosive Used (kg)", isRequired = true),
                        FormField(fieldName = "powder_factor", fieldType = FormFieldType.NUMBER, label = "Powder Factor (kg/m³)", isRequired = false),
                        FormField(fieldName = "booster_type", fieldType = FormFieldType.TEXT, label = "Booster Type", isRequired = false),
                        FormField(fieldName = "booster_weight_g", fieldType = FormFieldType.NUMBER, label = "Booster Weight (g)", isRequired = false)
                    )
                ),
                FormSection(id = "Initiation System", title = "Initiation System",
                    fields = listOf(
                        FormField(fieldName = "detonator_type", fieldType = FormFieldType.TEXT, label = "Detonator Type", isRequired = true),
                        FormField(fieldName = "detonator_manufacturer", fieldType = FormFieldType.TEXT, label = "Detonator Manufacturer", isRequired = false),
                        FormField(fieldName = "delay_sequence", fieldType = FormFieldType.MULTILINE_TEXT, label = "Delay Sequence Pattern", isRequired = true),
                        FormField(fieldName = "total_detonators_used", fieldType = FormFieldType.INTEGER, label = "Total Detonators Used", isRequired = true),
                        FormField(fieldName = "initiation_method", fieldType = FormFieldType.DROPDOWN, label = "Initiation Method", isRequired = true,
                                 options = listOf("Electronic", "Shock Tube", "Safety Fuse", "Detonating Cord")),
                        FormField(fieldName = "surface_connectors", fieldType = FormFieldType.TEXT, label = "Surface Connectors Used", isRequired = false),
                        FormField(fieldName = "tie_in_details", fieldType = FormFieldType.MULTILINE_TEXT, label = "Tie-in Details", isRequired = false)
                    )
                ),
                FormSection(id = "Stemming & Sealing", title = "Stemming & Sealing",
                    fields = listOf(
                        FormField(fieldName = "stemming_material", fieldType = FormFieldType.TEXT, label = "Stemming Material Type", isRequired = true),
                        FormField(fieldName = "stemming_source", fieldType = FormFieldType.TEXT, label = "Stemming Material Source", isRequired = false),
                        FormField(fieldName = "stemming_compaction_method", fieldType = FormFieldType.TEXT, label = "Stemming Compaction Method", isRequired = false),
                        FormField(fieldName = "collar_protection", fieldType = FormFieldType.CHECKBOX, label = "Collar Protection Installed", isRequired = false),
                        FormField(fieldName = "deck_charges_used", fieldType = FormFieldType.CHECKBOX, label = "Deck Charges Used", isRequired = false),
                        FormField(fieldName = "deck_details", fieldType = FormFieldType.MULTILINE_TEXT, label = "Deck Configuration Details", isRequired = false)
                    )
                ),
                FormSection(id = "Quality Control & Verification", title = "Quality Control & Verification",
                    fields = listOf(
                        FormField(fieldName = "loading_sequence_verified", fieldType = FormFieldType.CHECKBOX, label = "Loading Sequence Verified", isRequired = false),
                        FormField(fieldName = "tie_in_inspected", fieldType = FormFieldType.CHECKBOX, label = "Tie-in System Inspected", isRequired = false),
                        FormField(fieldName = "continuity_test_performed", fieldType = FormFieldType.CHECKBOX, label = "Continuity Test Performed", isRequired = false),
                        FormField(fieldName = "explosive_calculation_verified", fieldType = FormFieldType.CHECKBOX, label = "Explosive Calculations Verified", isRequired = false),
                        FormField(fieldName = "safety_distances_checked", fieldType = FormFieldType.CHECKBOX, label = "Safety Distances Checked", isRequired = false),
                        FormField(fieldName = "qc_inspector_name", fieldType = FormFieldType.TEXT, label = "QC Inspector Name", isRequired = false)
                    )
                ),
                FormSection(id = "Environmental Conditions", title = "Environmental Conditions",
                    fields = listOf(
                        FormField(fieldName = "weather_conditions", fieldType = FormFieldType.DROPDOWN, label = "Weather Conditions", isRequired = true,
                                 options = listOf("Clear", "Cloudy", "Light Rain", "Heavy Rain", "Snow", "Fog", "Windy")),
                        FormField(fieldName = "wind_speed_kmh", fieldType = FormFieldType.NUMBER, label = "Wind Speed (km/h)", isRequired = false),
                        FormField(fieldName = "wind_direction", fieldType = FormFieldType.TEXT, label = "Wind Direction", isRequired = false),
                        FormField(fieldName = "temperature_celsius", fieldType = FormFieldType.NUMBER, label = "Temperature (°C)", isRequired = false),
                        FormField(fieldName = "visibility_rating", fieldType = FormFieldType.DROPDOWN, label = "Visibility", isRequired = true,
                                 options = listOf("Excellent", "Good", "Fair", "Poor")),
                        FormField(fieldName = "humidity_percentage", fieldType = FormFieldType.NUMBER, label = "Humidity (%)", isRequired = false),
                        FormField(fieldName = "weather_impact_notes", fieldType = FormFieldType.MULTILINE_TEXT, label = "Weather Impact on Operations", isRequired = false)
                    )
                ),
                FormSection(id = "Safety Precautions", title = "Safety Precautions",
                    fields = listOf(
                        FormField(fieldName = "exclusion_zone_established", fieldType = FormFieldType.CHECKBOX, label = "Exclusion Zone Established", isRequired = false),
                        FormField(fieldName = "exclusion_radius_m", fieldType = FormFieldType.NUMBER, label = "Exclusion Radius (m)", isRequired = false),
                        FormField(fieldName = "warning_signals_tested", fieldType = FormFieldType.CHECKBOX, label = "Warning Signals Tested", isRequired = false),
                        FormField(fieldName = "communication_systems_checked", fieldType = FormFieldType.CHECKBOX, label = "Communication Systems Checked", isRequired = false),
                        FormField(fieldName = "emergency_procedures_briefed", fieldType = FormFieldType.CHECKBOX, label = "Emergency Procedures Briefed", isRequired = false),
                        FormField(fieldName = "first_aid_available", fieldType = FormFieldType.CHECKBOX, label = "First Aid Station Available", isRequired = false),
                        FormField(fieldName = "safety_equipment_checked", fieldType = FormFieldType.CHECKBOX, label = "Safety Equipment Checked", isRequired = false)
                    )
                ),
                FormSection(id = "Issues & Deviations", title = "Issues & Deviations",
                    fields = listOf(
                        FormField(fieldName = "design_deviations", fieldType = FormFieldType.MULTILINE_TEXT, label = "Deviations from Original Design", isRequired = false),
                        FormField(fieldName = "loading_problems", fieldType = FormFieldType.MULTILINE_TEXT, label = "Loading Problems Encountered", isRequired = false),
                        FormField(fieldName = "equipment_issues", fieldType = FormFieldType.MULTILINE_TEXT, label = "Equipment Issues", isRequired = false),
                        FormField(fieldName = "material_issues", fieldType = FormFieldType.MULTILINE_TEXT, label = "Material Quality Issues", isRequired = false),
                        FormField(fieldName = "corrective_actions_taken", fieldType = FormFieldType.MULTILINE_TEXT, label = "Corrective Actions Taken", isRequired = false),
                        FormField(fieldName = "delay_reasons", fieldType = FormFieldType.MULTILINE_TEXT, label = "Reasons for Delays", isRequired = false)
                    )
                ),
                FormSection(id = "Final Verification & Sign-off", title = "Final Verification & Sign-off",
                    fields = listOf(
                        FormField(fieldName = "all_holes_loaded", fieldType = FormFieldType.CHECKBOX, label = "All Planned Holes Loaded", isRequired = false),
                        FormField(fieldName = "ready_for_blast", fieldType = FormFieldType.CHECKBOX, label = "Ready for Blast", isRequired = false),
                        FormField(fieldName = "blast_scheduled_time", fieldType = FormFieldType.TIME, label = "Scheduled Blast Time", isRequired = false),
                        FormField(fieldName = "total_loading_time_hours", fieldType = FormFieldType.NUMBER, label = "Total Loading Time (hours)", isRequired = false),
                        FormField(fieldName = "blast_notes", fieldType = FormFieldType.MULTILINE_TEXT, label = "Additional Blast Notes", isRequired = false),
                        FormField(fieldName = "post_blast_assessment_required", fieldType = FormFieldType.CHECKBOX, label = "Post-Blast Assessment Required", isRequired = false),
                        FormField(fieldName = "blaster_signature", fieldType = FormFieldType.SIGNATURE, label = "Licensed Blaster Signature", isRequired = true),
                        FormField(fieldName = "supervisor_signature", fieldType = FormFieldType.SIGNATURE, label = "Supervisor Signature", isRequired = true),
                        FormField(fieldName = "final_inspection_time", fieldType = FormFieldType.TIME, label = "Final Inspection Time", isRequired = false)
                    )
                )
            ),
            fields = listOf(),
            createdAt = now,
            updatedAt = now
        ),

        // Job Card Template - COMPREHENSIVE from PDF coordinate maps
        "job_card" to FormTemplate(
            id = "job_card",
            name = "Job Card",
            description = "Comprehensive maintenance and repair job card based on job card.pdf with ALL fields from PDF coordinate maps",
            formType = FormType.JOB_CARD,
            templateFile = "templates/job_card.json",
            pdfTemplate = "job card.pdf",
            fieldMappings = emptyList(),
            version = "2.0",
            sections = listOf(
                FormSection(id = "Job Header Information", title = "Job Header Information",
                    fields = listOf(
                        FormField(fieldName = "job_number", fieldType = FormFieldType.TEXT, label = "Job Number", isRequired = true),
                        FormField(fieldName = "work_order_number", fieldType = FormFieldType.TEXT, label = "Work Order Number", isRequired = false),
                        FormField(fieldName = "site", fieldType = FormFieldType.TEXT, label = "Site", isRequired = true, isReadOnly = true),
                        FormField(fieldName = "date_issued", fieldType = FormFieldType.DATE, label = "Date Issued", isRequired = true),
                        FormField(fieldName = "time_issued", fieldType = FormFieldType.TIME, label = "Time Issued", isRequired = false),
                        FormField(fieldName = "issued_by", fieldType = FormFieldType.TEXT, label = "Issued By", isRequired = true),
                        FormField(fieldName = "department", fieldType = FormFieldType.TEXT, label = "Department", isRequired = false)
                    )
                ),
                FormSection(id = "Equipment Information", title = "Equipment Information",
                    fields = listOf(
                        FormField(fieldName = "equipment_id", fieldType = FormFieldType.DROPDOWN, label = "Equipment ID", isRequired = true),
                        FormField(fieldName = "equipment_description", fieldType = FormFieldType.TEXT, label = "Equipment Description", isRequired = true),
                        FormField(fieldName = "equipment_location", fieldType = FormFieldType.TEXT, label = "Equipment Location", isRequired = true),
                        FormField(fieldName = "equipment_make", fieldType = FormFieldType.TEXT, label = "Equipment Make", isRequired = false),
                        FormField(fieldName = "equipment_model", fieldType = FormFieldType.TEXT, label = "Equipment Model", isRequired = false),
                        FormField(fieldName = "equipment_serial_number", fieldType = FormFieldType.TEXT, label = "Serial Number", isRequired = false),
                        FormField(fieldName = "equipment_hours", fieldType = FormFieldType.NUMBER, label = "Equipment Hours", isRequired = false),
                        FormField(fieldName = "last_service_date", fieldType = FormFieldType.DATE, label = "Last Service Date", isRequired = false)
                    )
                ),
                FormSection(id = "Work Request Details", title = "Work Request Details",
                    fields = listOf(
                        FormField(fieldName = "work_description", fieldType = FormFieldType.MULTILINE_TEXT, label = "Work Description", isRequired = true),
                        FormField(fieldName = "work_requested", fieldType = FormFieldType.MULTILINE_TEXT, label = "Work Requested", isRequired = true),
                        FormField(fieldName = "failure_description", fieldType = FormFieldType.MULTILINE_TEXT, label = "Failure/Problem Description", isRequired = false),
                        FormField(fieldName = "symptoms_observed", fieldType = FormFieldType.MULTILINE_TEXT, label = "Symptoms Observed", isRequired = false),
                        FormField(fieldName = "priority", fieldType = FormFieldType.DROPDOWN, label = "Priority", isRequired = true,
                                 options = listOf("Emergency", "Urgent", "High", "Medium", "Low")),
                        FormField(fieldName = "work_type", fieldType = FormFieldType.DROPDOWN, label = "Work Type", isRequired = true,
                                 options = listOf("Preventive", "Corrective", "Emergency", "Inspection", "Modification", "Installation")),
                        FormField(fieldName = "requested_by", fieldType = FormFieldType.TEXT, label = "Requested By", isRequired = true),
                        FormField(fieldName = "requested_by_id", fieldType = FormFieldType.TEXT, label = "Requestor ID", isRequired = false),
                        FormField(fieldName = "contact_details", fieldType = FormFieldType.TEXT, label = "Contact Details", isRequired = false)
                    )
                ),
                FormSection(id = "Authorization & Planning", title = "Authorization & Planning",
                    fields = listOf(
                        FormField(fieldName = "approved_by", fieldType = FormFieldType.TEXT, label = "Approved By", isRequired = false),
                        FormField(fieldName = "approval_date", fieldType = FormFieldType.DATE, label = "Approval Date", isRequired = false),
                        FormField(fieldName = "budget_approved", fieldType = FormFieldType.NUMBER, label = "Budget Approved ($)", isRequired = false),
                        FormField(fieldName = "estimated_hours", fieldType = FormFieldType.NUMBER, label = "Estimated Labor Hours", isRequired = true),
                        FormField(fieldName = "estimated_cost", fieldType = FormFieldType.NUMBER, label = "Estimated Total Cost ($)", isRequired = false),
                        FormField(fieldName = "target_completion_date", fieldType = FormFieldType.DATE, label = "Target Completion Date", isRequired = false),
                        FormField(fieldName = "required_skills", fieldType = FormFieldType.MULTILINE_TEXT, label = "Required Skills/Qualifications", isRequired = false),
                        FormField(fieldName = "crew_size_required", fieldType = FormFieldType.INTEGER, label = "Crew Size Required", isRequired = false)
                    )
                ),
                FormSection(id = "Safety & Permits", title = "Safety & Permits",
                    fields = listOf(
                        FormField(fieldName = "safety_precautions", fieldType = FormFieldType.MULTILINE_TEXT, label = "Safety Precautions Required", isRequired = true),
                        FormField(fieldName = "ppe_required", fieldType = FormFieldType.MULTILINE_TEXT, label = "PPE Required", isRequired = true),
                        FormField(fieldName = "permit_required", fieldType = FormFieldType.CHECKBOX, label = "Work Permit Required", isRequired = false),
                        FormField(fieldName = "permit_number", fieldType = FormFieldType.TEXT, label = "Permit Number", isRequired = false),
                        FormField(fieldName = "lockout_tagout_required", fieldType = FormFieldType.CHECKBOX, label = "Lockout/Tagout Required", isRequired = false),
                        FormField(fieldName = "hot_work_permit", fieldType = FormFieldType.CHECKBOX, label = "Hot Work Permit Required", isRequired = false),
                        FormField(fieldName = "confined_space_permit", fieldType = FormFieldType.CHECKBOX, label = "Confined Space Permit Required", isRequired = false),
                        FormField(fieldName = "shutdown_required", fieldType = FormFieldType.BOOLEAN, label = "Equipment Shutdown Required", isRequired = true),
                        FormField(fieldName = "isolation_points", fieldType = FormFieldType.MULTILINE_TEXT, label = "Isolation Points", isRequired = false)
                    )
                ),
                FormSection(id = "Tools & Materials", title = "Tools & Materials",
                    fields = listOf(
                        FormField(fieldName = "tools_required", fieldType = FormFieldType.MULTILINE_TEXT, label = "Tools Required", isRequired = false),
                        FormField(fieldName = "special_equipment_needed", fieldType = FormFieldType.MULTILINE_TEXT, label = "Special Equipment Needed", isRequired = false),
                        FormField(fieldName = "materials_required", fieldType = FormFieldType.MULTILINE_TEXT, label = "Materials/Parts Required", isRequired = false),
                        FormField(fieldName = "spare_parts_ordered", fieldType = FormFieldType.CHECKBOX, label = "Spare Parts Ordered", isRequired = false),
                        FormField(fieldName = "parts_availability_confirmed", fieldType = FormFieldType.CHECKBOX, label = "Parts Availability Confirmed", isRequired = false),
                        FormField(fieldName = "estimated_parts_cost", fieldType = FormFieldType.NUMBER, label = "Estimated Parts Cost ($)", isRequired = false)
                    )
                ),
                FormSection(id = "Work Execution", title = "Work Execution", 
                    fields = listOf(
                        FormField(fieldName = "assigned_technician", fieldType = FormFieldType.TEXT, label = "Assigned Technician", isRequired = true),
                        FormField(fieldName = "technician_id", fieldType = FormFieldType.TEXT, label = "Technician ID", isRequired = false),
                        FormField(fieldName = "crew_members", fieldType = FormFieldType.MULTILINE_TEXT, label = "Crew Members", isRequired = false),
                        FormField(fieldName = "actual_start_date", fieldType = FormFieldType.DATE, label = "Actual Start Date", isRequired = true),
                        FormField(fieldName = "actual_start_time", fieldType = FormFieldType.TIME, label = "Actual Start Time", isRequired = true),
                        FormField(fieldName = "completion_date", fieldType = FormFieldType.DATE, label = "Completion Date", isRequired = false),
                        FormField(fieldName = "completion_time", fieldType = FormFieldType.TIME, label = "Completion Time", isRequired = false),
                        FormField(fieldName = "work_interruptions", fieldType = FormFieldType.MULTILINE_TEXT, label = "Work Interruptions/Delays", isRequired = false),
                        FormField(fieldName = "delay_reasons", fieldType = FormFieldType.MULTILINE_TEXT, label = "Reasons for Delays", isRequired = false)
                    )
                ),
                FormSection(id = "Work Performed", title = "Work Performed",
                    fields = listOf(
                        FormField(fieldName = "work_performed_detailed", fieldType = FormFieldType.MULTILINE_TEXT, label = "Detailed Work Performed", isRequired = true),
                        FormField(fieldName = "procedures_followed", fieldType = FormFieldType.MULTILINE_TEXT, label = "Procedures Followed", isRequired = false),
                        FormField(fieldName = "root_cause_identified", fieldType = FormFieldType.MULTILINE_TEXT, label = "Root Cause Identified", isRequired = false),
                        FormField(fieldName = "corrective_actions_taken", fieldType = FormFieldType.MULTILINE_TEXT, label = "Corrective Actions Taken", isRequired = false),
                        FormField(fieldName = "preventive_measures", fieldType = FormFieldType.MULTILINE_TEXT, label = "Preventive Measures Implemented", isRequired = false),
                        FormField(fieldName = "modifications_made", fieldType = FormFieldType.MULTILINE_TEXT, label = "Modifications Made", isRequired = false),
                        FormField(fieldName = "calibration_performed", fieldType = FormFieldType.CHECKBOX, label = "Calibration Performed", isRequired = false),
                        FormField(fieldName = "calibration_details", fieldType = FormFieldType.MULTILINE_TEXT, label = "Calibration Details", isRequired = false)
                    )
                ),
                FormSection(id = "Materials & Parts Used", title = "Materials & Parts Used",
                    fields = listOf(
                        FormField(fieldName = "parts_used_detailed", fieldType = FormFieldType.MULTILINE_TEXT, label = "Parts Used (with quantities)", isRequired = false),
                        FormField(fieldName = "part_numbers", fieldType = FormFieldType.MULTILINE_TEXT, label = "Part Numbers", isRequired = false),
                        FormField(fieldName = "materials_consumed", fieldType = FormFieldType.MULTILINE_TEXT, label = "Materials Consumed", isRequired = false),
                        FormField(fieldName = "total_parts_cost", fieldType = FormFieldType.NUMBER, label = "Total Parts Cost ($)", isRequired = false),
                        FormField(fieldName = "warranty_information", fieldType = FormFieldType.MULTILINE_TEXT, label = "Warranty Information", isRequired = false),
                        FormField(fieldName = "returned_parts", fieldType = FormFieldType.MULTILINE_TEXT, label = "Parts Returned to Store", isRequired = false)
                    )
                ),
                FormSection(id = "Labor & Time", title = "Labor & Time",
                    fields = listOf(
                        FormField(fieldName = "regular_hours", fieldType = FormFieldType.NUMBER, label = "Regular Hours", isRequired = true),
                        FormField(fieldName = "overtime_hours", fieldType = FormFieldType.NUMBER, label = "Overtime Hours", isRequired = false),
                        FormField(fieldName = "total_labor_hours", fieldType = FormFieldType.NUMBER, label = "Total Labor Hours", isRequired = true),
                        FormField(fieldName = "labor_rate", fieldType = FormFieldType.NUMBER, label = "Labor Rate ($/hr)", isRequired = false),
                        FormField(fieldName = "total_labor_cost", fieldType = FormFieldType.NUMBER, label = "Total Labor Cost ($)", isRequired = false),
                        FormField(fieldName = "travel_time", fieldType = FormFieldType.NUMBER, label = "Travel Time (hrs)", isRequired = false),
                        FormField(fieldName = "waiting_time", fieldType = FormFieldType.NUMBER, label = "Waiting Time (hrs)", isRequired = false)
                    )
                ),
                FormSection(id = "Testing & Verification", title = "Testing & Verification",
                    fields = listOf(
                        FormField(fieldName = "testing_performed", fieldType = FormFieldType.CHECKBOX, label = "Post-Work Testing Performed", isRequired = false),
                        FormField(fieldName = "test_results", fieldType = FormFieldType.MULTILINE_TEXT, label = "Test Results", isRequired = false),
                        FormField(fieldName = "performance_verified", fieldType = FormFieldType.CHECKBOX, label = "Performance Verified", isRequired = false),
                        FormField(fieldName = "safety_checks_completed", fieldType = FormFieldType.CHECKBOX, label = "Safety Checks Completed", isRequired = false),
                        FormField(fieldName = "equipment_returned_to_service", fieldType = FormFieldType.CHECKBOX, label = "Equipment Returned to Service", isRequired = false),
                        FormField(fieldName = "tested_by", fieldType = FormFieldType.TEXT, label = "Tested By", isRequired = false),
                        FormField(fieldName = "test_date", fieldType = FormFieldType.DATE, label = "Test Date", isRequired = false)
                    )
                ),
                FormSection(id = "Job Completion & Status", title = "Job Completion & Status",
                    fields = listOf(
                        FormField(fieldName = "job_status", fieldType = FormFieldType.DROPDOWN, label = "Job Status", isRequired = true,
                                 options = listOf("Completed", "Partially Completed", "On Hold", "Cancelled", "Requires Follow-up")),
                        FormField(fieldName = "job_completed_successfully", fieldType = FormFieldType.CHECKBOX, label = "Job Completed Successfully", isRequired = false),
                        FormField(fieldName = "further_action_required", fieldType = FormFieldType.CHECKBOX, label = "Further Action Required", isRequired = false),
                        FormField(fieldName = "follow_up_work_needed", fieldType = FormFieldType.MULTILINE_TEXT, label = "Follow-up Work Needed", isRequired = false),
                        FormField(fieldName = "completion_notes", fieldType = FormFieldType.MULTILINE_TEXT, label = "Completion Notes", isRequired = false),
                        FormField(fieldName = "customer_notification", fieldType = FormFieldType.CHECKBOX, label = "Customer Notified of Completion", isRequired = false),
                        FormField(fieldName = "warranty_period", fieldType = FormFieldType.TEXT, label = "Warranty Period", isRequired = false)
                    )
                ),
                FormSection(id = "Recommendations & Future Actions", title = "Recommendations & Future Actions",
                    fields = listOf(
                        FormField(fieldName = "recommendations", fieldType = FormFieldType.MULTILINE_TEXT, label = "Recommendations", isRequired = false),
                        FormField(fieldName = "future_maintenance_needs", fieldType = FormFieldType.MULTILINE_TEXT, label = "Future Maintenance Needs", isRequired = false),
                        FormField(fieldName = "spare_parts_recommendations", fieldType = FormFieldType.MULTILINE_TEXT, label = "Spare Parts Recommendations", isRequired = false),
                        FormField(fieldName = "training_recommendations", fieldType = FormFieldType.MULTILINE_TEXT, label = "Training Recommendations", isRequired = false),
                        FormField(fieldName = "process_improvements", fieldType = FormFieldType.MULTILINE_TEXT, label = "Process Improvements", isRequired = false)
                    )
                ),
                FormSection(id = "Sign-off & Approval", title = "Sign-off & Approval",
                    fields = listOf(
                        FormField(fieldName = "technician_signature", fieldType = FormFieldType.SIGNATURE, label = "Technician Signature", isRequired = true),
                        FormField(fieldName = "supervisor_signature", fieldType = FormFieldType.SIGNATURE, label = "Supervisor Signature", isRequired = false),
                        FormField(fieldName = "customer_signature", fieldType = FormFieldType.SIGNATURE, label = "Customer Signature & Print Name", isRequired = false),
                        FormField(fieldName = "quality_inspector_signature", fieldType = FormFieldType.SIGNATURE, label = "Quality Inspector Signature", isRequired = false),
                        FormField(fieldName = "final_approval_signature", fieldType = FormFieldType.SIGNATURE, label = "Final Approval Signature", isRequired = false),
                        FormField(fieldName = "completion_comments", fieldType = FormFieldType.MULTILINE_TEXT, label = "Final Comments", isRequired = false)
                    )
                )
            ),
            fields = listOf(),
            createdAt = now,
            updatedAt = now
        ),

        // On Bench MMU Inspection Template - Comprehensive
        "on_bench_mmu_inspection" to FormTemplate(
            id = "on_bench_mmu_inspection",
            name = "On Bench MMU Inspection",
            description = "Comprehensive on bench Mobile Manufacturing Unit inspection checklist",
            formType = FormType.ON_BENCH_MMU_INSPECTION,
            templateFile = "templates/on_bench_mmu_inspection.json",
            pdfTemplate = "ON BENCH MMU INSPECTION.pdf",
            fieldMappings = emptyList(),
            version = "1.0",
            sections = listOf(
                FormSection(id = "Inspection Details", title = "Inspection Details",
                    fields = listOf(
                        FormField(fieldName = "inspection_date", fieldType = FormFieldType.DATE, label = "Inspection Date", isRequired = true),
                        FormField(fieldName = "mmu_number", fieldType = FormFieldType.DROPDOWN, label = "MMU Number", isRequired = true),
                        FormField(fieldName = "serial_number", fieldType = FormFieldType.TEXT, label = "Serial Number", isRequired = true),
                        FormField(fieldName = "inspector_name", fieldType = FormFieldType.TEXT, label = "Inspector Name", isRequired = true),
                        FormField(fieldName = "supervisor_name", fieldType = FormFieldType.TEXT, label = "Supervisor Name", isRequired = true),
                        FormField(fieldName = "inspection_type", fieldType = FormFieldType.DROPDOWN, label = "Inspection Type", isRequired = true,
                                 options = listOf("Pre-shift", "Post-shift", "Scheduled", "Unscheduled")),
                        FormField(fieldName = "operating_hours", fieldType = FormFieldType.NUMBER, label = "Operating Hours", isRequired = true)
                    )
                ),
                FormSection(id = "Engine Inspection", title = "Engine Inspection",
                    fields = listOf(
                        FormField(fieldName = "engine_oil_level", fieldType = FormFieldType.DROPDOWN, label = "Engine Oil Level", isRequired = true,
                                 options = listOf("Full", "Above Min", "At Min", "Below Min")),
                        FormField(fieldName = "engine_oil_condition", fieldType = FormFieldType.DROPDOWN, label = "Engine Oil Condition", isRequired = true,
                                 options = listOf("Good", "Fair", "Poor", "Replace")),
                        FormField(fieldName = "coolant_level", fieldType = FormFieldType.DROPDOWN, label = "Coolant Level", isRequired = true,
                                 options = listOf("Full", "Above Min", "At Min", "Below Min")),
                        FormField(fieldName = "air_filter_condition", fieldType = FormFieldType.DROPDOWN, label = "Air Filter Condition", isRequired = true,
                                 options = listOf("Clean", "Dirty", "Replace")),
                        FormField(fieldName = "belt_condition", fieldType = FormFieldType.DROPDOWN, label = "Belt Condition", isRequired = true,
                                 options = listOf("Good", "Worn", "Cracked", "Replace")),
                        FormField(fieldName = "engine_condition", fieldType = FormFieldType.DROPDOWN, label = "Overall Engine Condition", isRequired = true,
                                 options = listOf("Excellent", "Good", "Fair", "Poor"))
                    )
                ),
                FormSection(id = "Hydraulic System", title = "Hydraulic System",
                    fields = listOf(
                        FormField(fieldName = "hydraulic_oil_level", fieldType = FormFieldType.DROPDOWN, label = "Hydraulic Oil Level", isRequired = true,
                                 options = listOf("Full", "Above Min", "At Min", "Below Min")),
                        FormField(fieldName = "hydraulic_oil_condition", fieldType = FormFieldType.DROPDOWN, label = "Hydraulic Oil Condition", isRequired = true,
                                 options = listOf("Good", "Fair", "Poor", "Replace")),
                        FormField(fieldName = "hydraulic_hoses", fieldType = FormFieldType.DROPDOWN, label = "Hydraulic Hoses", isRequired = true,
                                 options = listOf("Good", "Minor Wear", "Damaged", "Replace")),
                        FormField(fieldName = "hydraulic_leaks", fieldType = FormFieldType.BOOLEAN, label = "Hydraulic Leaks Present", isRequired = true),
                        FormField(fieldName = "hydraulic_system", fieldType = FormFieldType.DROPDOWN, label = "Overall Hydraulic System", isRequired = true,
                                 options = listOf("Excellent", "Good", "Fair", "Poor"))
                    )
                ),
                FormSection(id = "Electrical System", title = "Electrical System",
                    fields = listOf(
                        FormField(fieldName = "battery_condition", fieldType = FormFieldType.DROPDOWN, label = "Battery Condition", isRequired = true,
                                 options = listOf("Good", "Fair", "Poor", "Replace")),
                        FormField(fieldName = "wiring_condition", fieldType = FormFieldType.DROPDOWN, label = "Wiring Condition", isRequired = true,
                                 options = listOf("Good", "Minor Damage", "Damaged", "Replace")),
                        FormField(fieldName = "lights_functional", fieldType = FormFieldType.BOOLEAN, label = "All Lights Functional", isRequired = true),
                        FormField(fieldName = "gauges_functional", fieldType = FormFieldType.BOOLEAN, label = "All Gauges Functional", isRequired = true),
                        FormField(fieldName = "electrical_system", fieldType = FormFieldType.DROPDOWN, label = "Overall Electrical System", isRequired = true,
                                 options = listOf("Excellent", "Good", "Fair", "Poor"))
                    )
                ),
                FormSection(id = "Safety Systems", title = "Safety Systems",
                    fields = listOf(
                        FormField(fieldName = "emergency_stop", fieldType = FormFieldType.BOOLEAN, label = "Emergency Stop Functional", isRequired = true),
                        FormField(fieldName = "warning_devices", fieldType = FormFieldType.BOOLEAN, label = "Warning Devices Functional", isRequired = true),
                        FormField(fieldName = "fire_extinguisher", fieldType = FormFieldType.BOOLEAN, label = "Fire Extinguisher Present & Charged", isRequired = true),
                        FormField(fieldName = "first_aid_kit", fieldType = FormFieldType.BOOLEAN, label = "First Aid Kit Present", isRequired = true),
                        FormField(fieldName = "safety_systems", fieldType = FormFieldType.DROPDOWN, label = "Overall Safety Systems", isRequired = true,
                                 options = listOf("Excellent", "Good", "Fair", "Poor"))
                    )
                ),
                FormSection(id = "Structural Integrity", title = "Structural Integrity",
                    fields = listOf(
                        FormField(fieldName = "frame_condition", fieldType = FormFieldType.DROPDOWN, label = "Frame Condition", isRequired = true,
                                 options = listOf("Good", "Minor Cracks", "Major Cracks", "Repair Required")),
                        FormField(fieldName = "mounting_bolts", fieldType = FormFieldType.DROPDOWN, label = "Mounting Bolts", isRequired = true,
                                 options = listOf("Tight", "Loose", "Missing")),
                        FormField(fieldName = "guards_covers", fieldType = FormFieldType.DROPDOWN, label = "Guards & Covers", isRequired = true,
                                 options = listOf("All Present", "Some Missing", "Many Missing")),
                        FormField(fieldName = "structural_integrity", fieldType = FormFieldType.DROPDOWN, label = "Overall Structural Integrity", isRequired = true,
                                 options = listOf("Excellent", "Good", "Fair", "Poor"))
                    )
                ),
                FormSection(id = "Inspection Summary", title = "Inspection Summary",
                    fields = listOf(
                        FormField(fieldName = "defects_found", fieldType = FormFieldType.MULTILINE_TEXT, label = "Defects Found", isRequired = false),
                        FormField(fieldName = "corrective_actions", fieldType = FormFieldType.MULTILINE_TEXT, label = "Corrective Actions Required", isRequired = false),
                        FormField(fieldName = "parts_required", fieldType = FormFieldType.MULTILINE_TEXT, label = "Parts Required", isRequired = false),
                        FormField(fieldName = "overall_rating", fieldType = FormFieldType.DROPDOWN, label = "Overall Rating", isRequired = true,
                                 options = listOf("Pass", "Conditional Pass", "Fail")),
                        FormField(fieldName = "next_inspection_date", fieldType = FormFieldType.DATE, label = "Next Inspection Date", isRequired = true),
                        FormField(fieldName = "inspection_notes", fieldType = FormFieldType.MULTILINE_TEXT, label = "Inspection Notes", isRequired = false)
                    )
                )
            ),
            fields = listOf(),
            createdAt = now,
            updatedAt = now
        ),

        // PC Pump Pressure Test Template - Comprehensive
        "pc_pump_pressure_test" to FormTemplate(
            id = "pc_pump_pressure_test",
            name = "PC Pump High Low Pressure Trip Test",
            description = "Comprehensive PC Pump high and low pressure trip test procedure",
            formType = FormType.PC_PUMP_PRESSURE_TRIP_TEST,
            templateFile = "templates/pc_pump_pressure_test.json",
            pdfTemplate = "PC PUMP HIGH LOW PRESSURE TRIP TEST.pdf",
            fieldMappings = emptyList(),
            version = "1.0",
            sections = listOf(
                FormSection(id = "Test Information", title = "Test Information",
                    fields = listOf(
                        FormField(fieldName = "test_date", fieldType = FormFieldType.DATE, label = "Test Date", isRequired = true),
                        FormField(fieldName = "pump_id", fieldType = FormFieldType.DROPDOWN, label = "Pump ID", isRequired = true),
                        FormField(fieldName = "pump_model", fieldType = FormFieldType.TEXT, label = "Pump Model", isRequired = true),
                        FormField(fieldName = "pump_serial_number", fieldType = FormFieldType.TEXT, label = "Pump Serial Number", isRequired = true),
                        FormField(fieldName = "technician_name", fieldType = FormFieldType.TEXT, label = "Technician Name", isRequired = true),
                        FormField(fieldName = "supervisor_name", fieldType = FormFieldType.TEXT, label = "Supervisor Name", isRequired = true),
                        FormField(fieldName = "test_equipment", fieldType = FormFieldType.TEXT, label = "Test Equipment Used", isRequired = true),
                        FormField(fieldName = "calibration_date", fieldType = FormFieldType.DATE, label = "Test Equipment Calibration Date", isRequired = true)
                    )
                ),
                FormSection(id = "Pre-Test Conditions", title = "Pre-Test Conditions",
                    fields = listOf(
                        FormField(fieldName = "ambient_temperature", fieldType = FormFieldType.NUMBER, label = "Ambient Temperature (°C)", isRequired = true),
                        FormField(fieldName = "pump_temperature", fieldType = FormFieldType.NUMBER, label = "Pump Temperature (°C)", isRequired = true),
                        FormField(fieldName = "operating_hours", fieldType = FormFieldType.NUMBER, label = "Pump Operating Hours", isRequired = true),
                        FormField(fieldName = "system_pressure", fieldType = FormFieldType.NUMBER, label = "System Pressure (Bar)", isRequired = true),
                        FormField(fieldName = "pre_test_notes", fieldType = FormFieldType.MULTILINE_TEXT, label = "Pre-Test Notes", isRequired = false)
                    )
                ),
                FormSection(id = "High Pressure Trip Test", title = "High Pressure Trip Test",
                    fields = listOf(
                        FormField(fieldName = "high_pressure_setpoint", fieldType = FormFieldType.NUMBER, label = "High Pressure Setpoint (Bar)", isRequired = true),
                        FormField(fieldName = "high_pressure_trip_point", fieldType = FormFieldType.NUMBER, label = "Actual High Pressure Trip Point (Bar)", isRequired = true),
                        FormField(fieldName = "high_pressure_tolerance", fieldType = FormFieldType.NUMBER, label = "Tolerance (+/- Bar)", isRequired = true),
                        FormField(fieldName = "high_pressure_within_tolerance", fieldType = FormFieldType.BOOLEAN, label = "Within Tolerance", isRequired = true),
                        FormField(fieldName = "high_pressure_response_time", fieldType = FormFieldType.NUMBER, label = "Response Time (seconds)", isRequired = true),
                        FormField(fieldName = "high_pressure_test_result", fieldType = FormFieldType.DROPDOWN, label = "High Pressure Test Result", isRequired = true,
                                 options = listOf("Pass", "Fail")),
                        FormField(fieldName = "high_pressure_notes", fieldType = FormFieldType.MULTILINE_TEXT, label = "High Pressure Test Notes", isRequired = false)
                    )
                ),
                FormSection(id = "Low Pressure Trip Test", title = "Low Pressure Trip Test",
                    fields = listOf(
                        FormField(fieldName = "low_pressure_setpoint", fieldType = FormFieldType.NUMBER, label = "Low Pressure Setpoint (Bar)", isRequired = true),
                        FormField(fieldName = "low_pressure_trip_point", fieldType = FormFieldType.NUMBER, label = "Actual Low Pressure Trip Point (Bar)", isRequired = true),
                        FormField(fieldName = "low_pressure_tolerance", fieldType = FormFieldType.NUMBER, label = "Tolerance (+/- Bar)", isRequired = true),
                        FormField(fieldName = "low_pressure_within_tolerance", fieldType = FormFieldType.BOOLEAN, label = "Within Tolerance", isRequired = true),
                        FormField(fieldName = "low_pressure_response_time", fieldType = FormFieldType.NUMBER, label = "Response Time (seconds)", isRequired = true),
                        FormField(fieldName = "low_pressure_test_result", fieldType = FormFieldType.DROPDOWN, label = "Low Pressure Test Result", isRequired = true,
                                 options = listOf("Pass", "Fail")),
                        FormField(fieldName = "low_pressure_notes", fieldType = FormFieldType.MULTILINE_TEXT, label = "Low Pressure Test Notes", isRequired = false)
                    )
                ),
                FormSection(id = "Test Summary & Recommendations", title = "Test Summary & Recommendations",
                    fields = listOf(
                        FormField(fieldName = "overall_test_result", fieldType = FormFieldType.DROPDOWN, label = "Overall Test Result", isRequired = true,
                                 options = listOf("Pass", "Fail")),
                        FormField(fieldName = "corrective_actions", fieldType = FormFieldType.MULTILINE_TEXT, label = "Corrective Actions Required", isRequired = false),
                        FormField(fieldName = "parts_required", fieldType = FormFieldType.MULTILINE_TEXT, label = "Parts Required", isRequired = false),
                        FormField(fieldName = "next_test_date", fieldType = FormFieldType.DATE, label = "Next Test Date", isRequired = true),
                        FormField(fieldName = "test_frequency", fieldType = FormFieldType.DROPDOWN, label = "Test Frequency", isRequired = true,
                                 options = listOf("Weekly", "Monthly", "Quarterly", "Semi-Annual", "Annual")),
                        FormField(fieldName = "recommendations", fieldType = FormFieldType.MULTILINE_TEXT, label = "Recommendations", isRequired = false),
                        FormField(fieldName = "test_summary", fieldType = FormFieldType.MULTILINE_TEXT, label = "Test Summary", isRequired = false)
                    )
                )
            ),
            fields = listOf(),
            createdAt = now,
            updatedAt = now
        ),

        // Monthly Process Maintenance Record Template - Comprehensive
        "monthly_process_maintenance" to FormTemplate(
            id = "monthly_process_maintenance",
            name = "Monthly Process Maintenance Record",
            description = "Comprehensive monthly process equipment maintenance record",
            formType = FormType.MONTHLY_PROCESS_MAINTENANCE,
            templateFile = "templates/monthly_process_maintenance.json",
            pdfTemplate = "MONTHLY PROCESS MAINTENANCE RECORD.pdf",
            fieldMappings = emptyList(),
            version = "1.0",
            sections = listOf(
                FormSection(id = "Maintenance Period", title = "Maintenance Period",
                    fields = listOf(
                        FormField(fieldName = "maintenance_month", fieldType = FormFieldType.DROPDOWN, label = "Maintenance Month", isRequired = true,
                                 options = listOf("January", "February", "March", "April", "May", "June", 
                                               "July", "August", "September", "October", "November", "December")),
                        FormField(fieldName = "maintenance_year", fieldType = FormFieldType.NUMBER, label = "Maintenance Year", isRequired = true),
                        FormField(fieldName = "plant_area", fieldType = FormFieldType.TEXT, label = "Plant Area", isRequired = true),
                        FormField(fieldName = "supervisor_name", fieldType = FormFieldType.TEXT, label = "Supervisor Name", isRequired = true),
                        FormField(fieldName = "maintenance_team", fieldType = FormFieldType.MULTILINE_TEXT, label = "Maintenance Team", isRequired = true),
                        FormField(fieldName = "report_prepared_by", fieldType = FormFieldType.TEXT, label = "Report Prepared By", isRequired = true),
                        FormField(fieldName = "report_date", fieldType = FormFieldType.DATE, label = "Report Date", isRequired = true)
                    )
                ),
                FormSection(id = "Equipment Maintenance Summary", title = "Equipment Maintenance Summary",
                    fields = listOf(
                        FormField(fieldName = "total_equipment_count", fieldType = FormFieldType.NUMBER, label = "Total Equipment Count", isRequired = true),
                        FormField(fieldName = "equipment_maintained", fieldType = FormFieldType.NUMBER, label = "Equipment Maintained", isRequired = true),
                        FormField(fieldName = "equipment_list", fieldType = FormFieldType.MULTILINE_TEXT, label = "Equipment Maintained (List)", isRequired = true),
                        FormField(fieldName = "scheduled_maintenance", fieldType = FormFieldType.MULTILINE_TEXT, label = "Scheduled Maintenance Completed", isRequired = true),
                        FormField(fieldName = "unscheduled_maintenance", fieldType = FormFieldType.MULTILINE_TEXT, label = "Unscheduled Maintenance", isRequired = false),
                        FormField(fieldName = "emergency_repairs", fieldType = FormFieldType.MULTILINE_TEXT, label = "Emergency Repairs", isRequired = false),
                        FormField(fieldName = "preventive_actions", fieldType = FormFieldType.MULTILINE_TEXT, label = "Preventive Actions Taken", isRequired = false)
                    )
                ),
                FormSection(id = "Parts & Materials", title = "Parts & Materials",
                    fields = listOf(
                        FormField(fieldName = "parts_replaced", fieldType = FormFieldType.MULTILINE_TEXT, label = "Parts Replaced", isRequired = false),
                        FormField(fieldName = "parts_cost", fieldType = FormFieldType.NUMBER, label = "Parts Cost", isRequired = false),
                        FormField(fieldName = "consumables_used", fieldType = FormFieldType.MULTILINE_TEXT, label = "Consumables Used", isRequired = false),
                        FormField(fieldName = "consumables_cost", fieldType = FormFieldType.NUMBER, label = "Consumables Cost", isRequired = false),
                        FormField(fieldName = "total_material_cost", fieldType = FormFieldType.NUMBER, label = "Total Material Cost", isRequired = false)
                    )
                ),
                FormSection(id = "Labor & Downtime", title = "Labor & Downtime",
                    fields = listOf(
                        FormField(fieldName = "total_labor_hours", fieldType = FormFieldType.NUMBER, label = "Total Labor Hours", isRequired = true),
                        FormField(fieldName = "planned_downtime", fieldType = FormFieldType.NUMBER, label = "Planned Downtime (Hours)", isRequired = true),
                        FormField(fieldName = "unplanned_downtime", fieldType = FormFieldType.NUMBER, label = "Unplanned Downtime (Hours)", isRequired = true),
                        FormField(fieldName = "total_downtime", fieldType = FormFieldType.NUMBER, label = "Total Downtime (Hours)", isRequired = true),
                        FormField(fieldName = "downtime_cost", fieldType = FormFieldType.NUMBER, label = "Estimated Downtime Cost", isRequired = false),
                        FormField(fieldName = "labor_cost", fieldType = FormFieldType.NUMBER, label = "Labor Cost", isRequired = false),
                        FormField(fieldName = "maintenance_cost", fieldType = FormFieldType.NUMBER, label = "Total Maintenance Cost", isRequired = false)
                    )
                ),
                FormSection(id = "Performance Metrics", title = "Performance Metrics",
                    fields = listOf(
                        FormField(fieldName = "equipment_availability", fieldType = FormFieldType.NUMBER, label = "Equipment Availability (%)", isRequired = true),
                        FormField(fieldName = "mtbf", fieldType = FormFieldType.NUMBER, label = "Mean Time Between Failures (Hours)", isRequired = false),
                        FormField(fieldName = "mttr", fieldType = FormFieldType.NUMBER, label = "Mean Time To Repair (Hours)", isRequired = false),
                        FormField(fieldName = "reliability_rating", fieldType = FormFieldType.DROPDOWN, label = "Overall Reliability Rating", isRequired = true,
                                 options = listOf("Excellent", "Good", "Fair", "Poor"))
                    )
                ),
                FormSection(id = "Issues & Planning", title = "Issues & Planning",
                    fields = listOf(
                        FormField(fieldName = "outstanding_issues", fieldType = FormFieldType.MULTILINE_TEXT, label = "Outstanding Issues", isRequired = false),
                        FormField(fieldName = "recurring_problems", fieldType = FormFieldType.MULTILINE_TEXT, label = "Recurring Problems", isRequired = false),
                        FormField(fieldName = "improvement_recommendations", fieldType = FormFieldType.MULTILINE_TEXT, label = "Improvement Recommendations", isRequired = false),
                        FormField(fieldName = "next_month_planning", fieldType = FormFieldType.MULTILINE_TEXT, label = "Next Month Planning", isRequired = false),
                        FormField(fieldName = "budget_considerations", fieldType = FormFieldType.MULTILINE_TEXT, label = "Budget Considerations", isRequired = false),
                        FormField(fieldName = "training_needs", fieldType = FormFieldType.MULTILINE_TEXT, label = "Training Needs", isRequired = false),
                        FormField(fieldName = "maintenance_summary", fieldType = FormFieldType.MULTILINE_TEXT, label = "Monthly Maintenance Summary", isRequired = false)
                    )
                )
            ),
            fields = listOf(),
            createdAt = now,
            updatedAt = now
        )
    )
}






