package com.aeci.mmucompanion.data.templates

import com.aeci.mmucompanion.domain.model.*

object FormCoordinateMaps {
    
    fun getCoordinateMap(formType: FormType): List<FieldCoordinate> {
        return when (formType) {
            FormType.PUMP_90_DAY_INSPECTION -> pump90DayInspectionCoordinates
            FormType.PUMP_WEEKLY_CHECK -> pumpWeeklyCheckCoordinates
            FormType.MMU_DAILY_LOG -> mmuDailyLogCoordinates
            FormType.MMU_QUALITY_REPORT -> mmuQualityReportCoordinates
            FormType.MMU_HANDOVER_CERTIFICATE -> mmuHandoverCertificateCoordinates
            FormType.MMU_CHASSIS_MAINTENANCE -> mmuChassisMaintenanceCoordinates
            FormType.ON_BENCH_MMU_INSPECTION -> onBenchMmuInspectionCoordinates
            FormType.PC_PUMP_PRESSURE_TEST -> pcPumpPressureTestCoordinates
            FormType.AVAILABILITY_UTILIZATION -> availabilityUtilizationCoordinates
            FormType.BLAST_HOLE_LOG -> blastHoleLogCoordinates
            FormType.PRETASK_SAFETY -> pretaskSafetyCoordinates
            FormType.FIRE_EXTINGUISHER_INSPECTION -> fireExtinguisherInspectionCoordinates
            FormType.MONTHLY_PROCESS_MAINTENANCE -> monthlyProcessMaintenanceCoordinates
            FormType.JOB_CARD -> jobCardCoordinates
            FormType.TIMESHEET -> timesheetCoordinates
            FormType.UOR_REPORT -> uorReportCoordinates
            FormType.PRETASK -> pretaskCoordinates
            else -> emptyList()
        }
    }

    // 1. 90 Day Pump System Inspection Checklist
    private val pump90DayInspectionCoordinates = listOf(
        // Header Section
        FieldCoordinate("inspection_date", 450, 85, 120, 25, "DATE", true, "DD/MM/YYYY"),
        FieldCoordinate("inspector_name", 150, 110, 200, 25, "TEXT", true, "Inspector Full Name"),
        FieldCoordinate("equipment_id", 450, 110, 150, 25, "TEXT", true, "Equipment ID"),
        FieldCoordinate("serial_number", 150, 135, 200, 25, "TEXT", true, "Serial Number"),
        FieldCoordinate("pump_location", 450, 135, 150, 25, "TEXT", true, "Location/Site"),
        FieldCoordinate("service_hours", 150, 160, 100, 25, "NUMBER", true, "Operating Hours"),
        
        // Visual Inspection Section
        FieldCoordinate("pump_housing_satisfactory", 50, 200, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("pump_housing_defective", 80, 200, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("pump_housing_comments", 350, 200, 200, 25, "TEXT", false, "Comments on pump housing"),
        
        FieldCoordinate("coupling_satisfactory", 50, 230, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("coupling_defective", 80, 230, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("coupling_comments", 350, 230, 200, 25, "TEXT", false, "Comments on coupling"),
        
        FieldCoordinate("motor_satisfactory", 50, 260, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("motor_defective", 80, 260, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("motor_comments", 350, 260, 200, 25, "TEXT", false, "Comments on motor"),
        
        FieldCoordinate("piping_satisfactory", 50, 290, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("piping_defective", 80, 290, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("piping_comments", 350, 290, 200, 25, "TEXT", false, "Comments on piping"),
        
        FieldCoordinate("lubrication_satisfactory", 50, 320, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("lubrication_defective", 80, 320, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("lubrication_comments", 350, 320, 200, 25, "TEXT", false, "Comments on lubrication"),
        
        // Pressure Test Section
        FieldCoordinate("pressure_test_performed", 80, 440, 20, 20, "CHECKBOX", true, ""),
        FieldCoordinate("pressure_test_date", 200, 440, 120, 25, "DATE", false, "Test Date"),
        FieldCoordinate("test_pressure_value", 350, 440, 100, 25, "NUMBER", false, "Test Pressure (Bar)"),
        FieldCoordinate("operating_pressure", 200, 470, 100, 25, "NUMBER", false, "Operating Pressure (Bar)"),
        FieldCoordinate("pressure_drop_rate", 350, 470, 100, 25, "NUMBER", false, "Pressure Drop Rate"),
        FieldCoordinate("pressure_test_pass", 80, 500, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("pressure_test_fail", 120, 500, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("pressure_test_comments", 200, 500, 300, 25, "TEXT", false, "Pressure test results"),
        
        // Signatures
        FieldCoordinate("inspector_signature", 100, 740, 180, 50, "SIGNATURE", true, "Inspector Signature"),
        FieldCoordinate("inspector_date", 100, 795, 120, 25, "DATE", true, "Date Signed"),
        FieldCoordinate("supervisor_signature", 350, 740, 180, 50, "SIGNATURE", true, "Supervisor Signature"),
        FieldCoordinate("supervisor_date", 350, 795, 120, 25, "DATE", true, "Date Signed")
    )

    // 2. Bowie Pump Weekly Checklist
    private val pumpWeeklyCheckCoordinates = listOf(
        // Header
        FieldCoordinate("weekly_date", 450, 60, 120, 25, "DATE", true, "Week of"),
        FieldCoordinate("pump_id", 200, 60, 150, 25, "TEXT", true, "Pump ID"),
        FieldCoordinate("operator_name", 450, 90, 150, 25, "TEXT", true, "Operator Name"),
        FieldCoordinate("shift", 200, 90, 100, 25, "DROPDOWN", true, "Shift"),
        
        // Daily Check Grid - Monday
        FieldCoordinate("mon_oil_level", 120, 180, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("mon_oil_condition", 120, 210, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("mon_temperature_check", 120, 240, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("mon_vibration_check", 120, 270, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("mon_pressure_check", 120, 300, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("mon_leakage_check", 120, 330, 20, 20, "CHECKBOX", false, ""),
        
        // Tuesday
        FieldCoordinate("tue_oil_level", 170, 180, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("tue_oil_condition", 170, 210, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("tue_temperature_check", 170, 240, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("tue_vibration_check", 170, 270, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("tue_pressure_check", 170, 300, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("tue_leakage_check", 170, 330, 20, 20, "CHECKBOX", false, ""),
        
        // Wednesday
        FieldCoordinate("wed_oil_level", 220, 180, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("wed_oil_condition", 220, 210, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("wed_temperature_check", 220, 240, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("wed_vibration_check", 220, 270, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("wed_pressure_check", 220, 300, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("wed_leakage_check", 220, 330, 20, 20, "CHECKBOX", false, ""),
        
        // Thursday
        FieldCoordinate("thu_oil_level", 270, 180, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("thu_oil_condition", 270, 210, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("thu_temperature_check", 270, 240, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("thu_vibration_check", 270, 270, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("thu_pressure_check", 270, 300, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("thu_leakage_check", 270, 330, 20, 20, "CHECKBOX", false, ""),
        
        // Friday
        FieldCoordinate("fri_oil_level", 320, 180, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("fri_oil_condition", 320, 210, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("fri_temperature_check", 320, 240, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("fri_vibration_check", 320, 270, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("fri_pressure_check", 320, 300, 20, 20, "CHECKBOX", false, ""),
        FieldCoordinate("fri_leakage_check", 320, 330, 20, 20, "CHECKBOX", false, ""),
        
        // Measurements
        FieldCoordinate("oil_temperature_reading", 150, 400, 100, 25, "NUMBER", false, "Oil Temp (°C)"),
        FieldCoordinate("bearing_temperature", 300, 400, 100, 25, "NUMBER", false, "Bearing Temp (°C)"),
        FieldCoordinate("vibration_reading", 450, 400, 100, 25, "NUMBER", false, "Vibration (mm/s)"),
        FieldCoordinate("discharge_pressure", 150, 430, 100, 25, "NUMBER", false, "Discharge Pressure (Bar)"),
        FieldCoordinate("suction_pressure", 300, 430, 100, 25, "NUMBER", false, "Suction Pressure (Bar)"),
        FieldCoordinate("flow_rate", 450, 430, 100, 25, "NUMBER", false, "Flow Rate (L/min)"),
        FieldCoordinate("operating_hours", 150, 460, 100, 25, "NUMBER", false, "Operating Hours"),
        
        // Issues and Actions
        FieldCoordinate("issues_found", 50, 520, 500, 60, "MULTILINE_TEXT", false, "Issues Found"),
        FieldCoordinate("corrective_actions", 50, 590, 500, 60, "MULTILINE_TEXT", false, "Corrective Actions Taken"),
        
        // Signatures
        FieldCoordinate("operator_signature", 100, 680, 180, 50, "SIGNATURE", true, "Operator Signature"),
        FieldCoordinate("supervisor_signature", 350, 680, 180, 50, "SIGNATURE", false, "Supervisor Signature")
    )

    // 3. MMU Daily Log
    private val mmuDailyLogCoordinates = listOf(
        // Header
        FieldCoordinate("production_date", 450, 60, 120, 25, "DATE", true, "Production Date"),
        FieldCoordinate("shift_selection", 200, 60, 100, 25, "DROPDOWN", true, "Day/Night Shift"),
        FieldCoordinate("operator_name", 450, 90, 150, 25, "TEXT", true, "Operator Name"),
        FieldCoordinate("site_location", 200, 90, 150, 25, "TEXT", true, "Site Code"),
        FieldCoordinate("weather_conditions", 200, 120, 150, 25, "DROPDOWN", false, "Weather"),
        FieldCoordinate("temperature", 400, 120, 80, 25, "NUMBER", false, "Temp (°C)"),
        
        // Equipment Hours
        FieldCoordinate("mmu_start_hours", 150, 180, 100, 25, "NUMBER", true, "Start Hours"),
        FieldCoordinate("mmu_end_hours", 300, 180, 100, 25, "NUMBER", true, "End Hours"),
        FieldCoordinate("mmu_total_hours", 450, 180, 100, 25, "NUMBER", true, "Total Hours"),
        
        // Production Metrics
        FieldCoordinate("holes_drilled_count", 150, 250, 100, 25, "INTEGER", true, "Number of Holes"),
        FieldCoordinate("total_meters_drilled", 300, 250, 100, 25, "NUMBER", true, "Total Meters"),
        FieldCoordinate("average_hole_depth", 450, 250, 100, 25, "NUMBER", false, "Avg Depth"),
        FieldCoordinate("drilling_rate", 150, 280, 100, 25, "NUMBER", false, "Rate (m/hr)"),
        FieldCoordinate("pattern_completed", 300, 280, 100, 25, "NUMBER", false, "% Pattern Complete"),
        FieldCoordinate("bench_level", 450, 280, 100, 25, "TEXT", false, "Bench Level"),
        
        // Downtime
        FieldCoordinate("maintenance_downtime", 150, 340, 100, 25, "NUMBER", false, "Maintenance (hrs)"),
        FieldCoordinate("weather_downtime", 300, 340, 100, 25, "NUMBER", false, "Weather (hrs)"),
        FieldCoordinate("other_downtime", 450, 340, 100, 25, "NUMBER", false, "Other (hrs)"),
        FieldCoordinate("downtime_reason", 50, 370, 500, 25, "TEXT", false, "Downtime Reason"),
        
        // Consumables
        FieldCoordinate("drill_bits_used", 150, 430, 100, 25, "NUMBER", false, "Drill Bits Used"),
        FieldCoordinate("fuel_consumption", 300, 430, 100, 25, "NUMBER", false, "Fuel (L)"),
        FieldCoordinate("water_usage", 450, 430, 100, 25, "NUMBER", false, "Water (L)"),
        
        // Safety
        FieldCoordinate("safety_incidents", 50, 490, 500, 40, "MULTILINE_TEXT", false, "Safety Incidents/Near Misses"),
        FieldCoordinate("environmental_observations", 50, 540, 500, 40, "MULTILINE_TEXT", false, "Environmental Observations"),
        
        // Signatures
        FieldCoordinate("operator_signature", 100, 620, 180, 50, "SIGNATURE", true, "Operator Signature"),
        FieldCoordinate("supervisor_signature", 350, 620, 180, 50, "SIGNATURE", false, "Supervisor Signature")
    )

    // 4. MMU Quality Report
    private val mmuQualityReportCoordinates = listOf(
        // Header
        FieldCoordinate("quality_report_date", 450, 60, 120, 25, "DATE", true, "Report Date"),
        FieldCoordinate("batch_number", 200, 60, 150, 25, "TEXT", true, "Batch Number"),
        FieldCoordinate("product_type", 450, 90, 150, 25, "DROPDOWN", true, "Product Type"),
        FieldCoordinate("quality_inspector", 200, 90, 150, 25, "TEXT", true, "Inspector"),
        
        // Quality Control Checks
        FieldCoordinate("density_check_pass", 50, 200, 80, 20, "CHECKBOX", false, ""),
        FieldCoordinate("density_check_fail", 140, 200, 80, 20, "CHECKBOX", false, ""),
        FieldCoordinate("density_reading", 230, 200, 100, 20, "NUMBER", false, "Density Reading"),
        
        FieldCoordinate("temperature_check_pass", 50, 230, 80, 20, "CHECKBOX", false, ""),
        FieldCoordinate("temperature_check_fail", 140, 230, 80, 20, "CHECKBOX", false, ""),
        FieldCoordinate("temperature_reading", 230, 230, 100, 20, "NUMBER", false, "Temp Reading (°C)"),
        
        FieldCoordinate("viscosity_check_pass", 50, 260, 80, 20, "CHECKBOX", false, ""),
        FieldCoordinate("viscosity_check_fail", 140, 260, 80, 20, "CHECKBOX", false, ""),
        FieldCoordinate("viscosity_reading", 230, 260, 100, 20, "NUMBER", false, "Viscosity Reading"),
        
        // Corrective Actions
        FieldCoordinate("non_conformance_details", 50, 320, 500, 50, "MULTILINE_TEXT", false, "Details of Non-Conformance"),
        FieldCoordinate("corrective_action_taken", 50, 380, 500, 50, "MULTILINE_TEXT", false, "Corrective Action Taken"),
        
        // Signature
        FieldCoordinate("inspector_signature", 100, 460, 180, 50, "SIGNATURE", true, "Inspector Signature"),
        FieldCoordinate("inspection_date", 350, 480, 120, 25, "DATE", true, "Inspection Date")
    )

    // 5. MMU Handover Certificate
    private val mmuHandoverCertificateCoordinates = listOf(
        // Header
        FieldCoordinate("mmu_id_handover", 50, 780, 150, 20, "TEXT", true, "MMU ID / Serial #"),
        FieldCoordinate("handover_date", 450, 780, 100, 20, "DATE", true, "Date of Handover"),
        FieldCoordinate("mmu_hours", 50, 750, 150, 20, "NUMBER", true, "Current MMU Hours"),
        
        // Parties
        FieldCoordinate("transferring_party_name", 50, 700, 200, 20, "TEXT", true, "Transferring Party"),
        FieldCoordinate("transferring_party_signature", 50, 660, 200, 30, "SIGNATURE", true, "Signature"),
        FieldCoordinate("receiving_party_name", 350, 700, 200, 20, "TEXT", true, "Receiving Party"),
        FieldCoordinate("receiving_party_signature", 350, 660, 200, 30, "SIGNATURE", true, "Signature"),
        
        // Condition Checks
        FieldCoordinate("condition_exterior_ok", 50, 500, 20, 20, "CHECKBOX", true, ""),
        FieldCoordinate("condition_interior_ok", 50, 470, 20, 20, "CHECKBOX", true, ""),
        FieldCoordinate("condition_safety_equip_ok", 50, 440, 20, 20, "CHECKBOX", true, ""),
        FieldCoordinate("condition_logbook_ok", 50, 410, 20, 20, "CHECKBOX", true, ""),
        
        // Comments
        FieldCoordinate("handover_comments", 50, 300, 500, 80, "MULTILINE_TEXT", false, "Handover Comments")
    )

    // 6. Remaining form coordinate maps would continue here...
    private val mmuChassisMaintenanceCoordinates = listOf(
        // Placeholder for chassis maintenance coordinates
        FieldCoordinate("maintenance_date", 450, 60, 120, 25, "DATE", true, "Maintenance Date"),
        FieldCoordinate("technician_name", 200, 60, 150, 25, "TEXT", true, "Technician Name"),
        FieldCoordinate("chassis_id", 450, 90, 150, 25, "TEXT", true, "Chassis ID")
    )

    private val onBenchMmuInspectionCoordinates = listOf(
        // Placeholder for on-bench inspection coordinates
        FieldCoordinate("mmu_id_bench", 50, 780, 150, 20, "TEXT", true, "MMU ID / Serial #"),
        FieldCoordinate("inspection_date_bench", 450, 780, 100, 20, "DATE", true, "Date"),
        FieldCoordinate("inspector_name_bench", 50, 750, 200, 20, "TEXT", true, "Inspected By")
    )

    private val pcPumpPressureTestCoordinates = listOf(
        // Placeholder for pressure test coordinates
        FieldCoordinate("test_date_trip", 50, 780, 120, 20, "DATE", true, "Test Date"),
        FieldCoordinate("mmu_id_trip", 200, 780, 150, 20, "TEXT", true, "MMU ID"),
        FieldCoordinate("pump_id_trip", 380, 780, 150, 20, "TEXT", true, "Pump ID")
    )

    private val availabilityUtilizationCoordinates = listOf(
        // Placeholder for availability report coordinates
        FieldCoordinate("report_period_start", 200, 70, 120, 25, "DATE", true, "Start Date"),
        FieldCoordinate("report_period_end", 400, 70, 120, 25, "DATE", true, "End Date"),
        FieldCoordinate("equipment_id", 200, 100, 150, 25, "TEXT", true, "Equipment ID")
    )

    private val blastHoleLogCoordinates = listOf(
        // Placeholder for blast hole log coordinates
        FieldCoordinate("blast_date", 450, 60, 120, 25, "DATE", true, "Blast Date"),
        FieldCoordinate("hole_number", 200, 60, 100, 25, "TEXT", true, "Hole Number"),
        FieldCoordinate("depth", 350, 60, 100, 25, "NUMBER", true, "Depth (m)")
    )

    private val pretaskSafetyCoordinates = listOf(
        // Placeholder for pretask safety coordinates
        FieldCoordinate("pretask_date", 50, 780, 120, 20, "DATE", true, "Date"),
        FieldCoordinate("work_area", 200, 780, 200, 20, "TEXT", true, "Work Area/Location"),
        FieldCoordinate("task_description", 50, 750, 450, 20, "TEXT", true, "Task to be Performed")
    )

    private val fireExtinguisherInspectionCoordinates = listOf(
        // Placeholder for fire extinguisher coordinates
        FieldCoordinate("inspection_date_ext", 50, 780, 120, 20, "DATE", true, "Inspection Date"),
        FieldCoordinate("inspector_name_ext", 200, 780, 200, 20, "TEXT", true, "Inspector Name"),
        FieldCoordinate("extinguisher_id", 50, 750, 150, 20, "TEXT", true, "Extinguisher ID")
    )

    private val monthlyProcessMaintenanceCoordinates = listOf(
        // Placeholder for monthly process coordinates
        FieldCoordinate("process_date", 450, 60, 120, 25, "DATE", true, "Process Date"),
        FieldCoordinate("process_type", 200, 60, 150, 25, "TEXT", true, "Process Type")
    )

    private val jobCardCoordinates = listOf(
        // Placeholder for job card coordinates
        FieldCoordinate("job_card_number", 50, 800, 150, 20, "TEXT", true, "Job Card #"),
        FieldCoordinate("job_date", 450, 800, 100, 20, "DATE", true, "Date"),
        FieldCoordinate("customer_name", 50, 770, 250, 20, "TEXT", true, "Customer Name")
    )

    private val timesheetCoordinates = listOf(
        // Placeholder for timesheet coordinates
        FieldCoordinate("timesheet_date", 450, 60, 120, 25, "DATE", true, "Date"),
        FieldCoordinate("employee_name", 200, 60, 150, 25, "TEXT", true, "Employee Name")
    )

    private val uorReportCoordinates = listOf(
        // Placeholder for UOR report coordinates
        FieldCoordinate("uor_date", 450, 60, 120, 25, "DATE", true, "UOR Date"),
        FieldCoordinate("uor_number", 200, 60, 150, 25, "TEXT", true, "UOR Number")
    )

    private val pretaskCoordinates = listOf(
        // Placeholder for pretask coordinates
        FieldCoordinate("pretask_date", 50, 780, 120, 20, "DATE", true, "Date"),
        FieldCoordinate("work_area", 200, 780, 200, 20, "TEXT", true, "Work Area")
    )
} 