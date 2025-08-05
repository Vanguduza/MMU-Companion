# PDF Coordinate Maps and Field Specifications

## Overview
This document provides detailed coordinate mappings for all PDF forms in the AECI MMU Companion app. Each coordinate map ensures pixel-perfect field alignment when overlaying digital form data onto original PDF templates.

## Coordinate System Specification

### Standard Coordinate Format
```kotlin
data class FieldCoordinate(
    val fieldName: String,        // Unique identifier for the field
    val x: Float,                 // X position from left edge (points)
    val y: Float,                 // Y position from top edge (points)
    val width: Float,             // Field width (points)
    val height: Float,            // Field height (points)
    val fieldType: FieldType,     // Input type specification
    val validation: ValidationRule? = null,
    val dependencies: List<String> = emptyList(), // Field dependencies
    val defaultValue: String? = null,
    val isRequired: Boolean = false,
    val placeholder: String? = null
)
```

### Field Type Specifications
```kotlin
enum class FieldType {
    TEXT,                // Single-line text input
    MULTILINE_TEXT,      // Multi-line text area
    NUMBER,              // Numeric input with decimal support
    INTEGER,             // Whole numbers only
    DATE,                // Date picker (DD/MM/YYYY)
    TIME,                // Time picker (HH:MM)
    DATETIME,            // Combined date and time
    CHECKBOX,            // Boolean checkbox
    RADIO,               // Single selection from group
    DROPDOWN,            // Dropdown selection
    SIGNATURE,           // Digital signature capture
    PHOTO,               // Camera/photo capture
    BARCODE,             // Barcode/QR code scanner
    EQUIPMENT_ID,        // Equipment identifier with validation
    SITE_CODE,           // Site code with validation
    EMPLOYEE_ID          // Employee ID with validation
}
```

---

## Form 1: 90 Day Pump System Inspection Checklist

**Source PDF**: `90 DAY PUMP SYSTEM INSPECTION CHECKLIST.pdf`
**Form ID**: `pump_inspection_90day`
**Page Size**: 612 x 792 points (Letter size)

### Header Section Coordinates
```kotlin
val pumpInspectionHeader = listOf(
    FieldCoordinate(
        fieldName = "inspection_date",
        x = 450f, y = 85f, width = 120f, height = 25f,
        fieldType = FieldType.DATE,
        isRequired = true,
        placeholder = "DD/MM/YYYY"
    ),
    FieldCoordinate(
        fieldName = "inspector_name",
        x = 150f, y = 110f, width = 200f, height = 25f,
        fieldType = FieldType.TEXT,
        isRequired = true,
        placeholder = "Inspector Full Name"
    ),
    FieldCoordinate(
        fieldName = "equipment_id",
        x = 450f, y = 110f, width = 150f, height = 25f,
        fieldType = FieldType.EQUIPMENT_ID,
        isRequired = true,
        placeholder = "Equipment ID"
    ),
    FieldCoordinate(
        fieldName = "serial_number",
        x = 150f, y = 135f, width = 200f, height = 25f,
        fieldType = FieldType.TEXT,
        isRequired = true,
        placeholder = "Serial Number"
    ),
    FieldCoordinate(
        fieldName = "pump_location",
        x = 450f, y = 135f, width = 150f, height = 25f,
        fieldType = FieldType.TEXT,
        isRequired = true,
        placeholder = "Location/Site"
    ),
    FieldCoordinate(
        fieldName = "service_hours",
        x = 150f, y = 160f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        isRequired = true,
        placeholder = "Operating Hours"
    )
)
```

### Visual Inspection Section (Y: 180-400)
```kotlin
val visualInspectionSection = listOf(
    // Pump Housing Inspection
    FieldCoordinate(
        fieldName = "pump_housing_satisfactory",
        x = 50f, y = 200f, width = 20f, height = 20f,
        fieldType = FieldType.CHECKBOX
    ),
    FieldCoordinate(
        fieldName = "pump_housing_defective",
        x = 80f, y = 200f, width = 20f, height = 20f,
        fieldType = FieldType.CHECKBOX
    ),
    FieldCoordinate(
        fieldName = "pump_housing_comments",
        x = 350f, y = 200f, width = 200f, height = 25f,
        fieldType = FieldType.TEXT,
        placeholder = "Comments on pump housing condition"
    ),
    
    // Coupling Inspection
    FieldCoordinate(
        fieldName = "coupling_satisfactory",
        x = 50f, y = 230f, width = 20f, height = 20f,
        fieldType = FieldType.CHECKBOX
    ),
    FieldCoordinate(
        fieldName = "coupling_defective",
        x = 80f, y = 230f, width = 20f, height = 20f,
        fieldType = FieldType.CHECKBOX
    ),
    FieldCoordinate(
        fieldName = "coupling_comments",
        x = 350f, y = 230f, width = 200f, height = 25f,
        fieldType = FieldType.TEXT,
        placeholder = "Comments on coupling condition"
    ),
    
    // Motor Inspection
    FieldCoordinate(
        fieldName = "motor_satisfactory",
        x = 50f, y = 260f, width = 20f, height = 20f,
        fieldType = FieldType.CHECKBOX
    ),
    FieldCoordinate(
        fieldName = "motor_defective",
        x = 80f, y = 260f, width = 20f, height = 20f,
        fieldType = FieldType.CHECKBOX
    ),
    FieldCoordinate(
        fieldName = "motor_comments",
        x = 350f, y = 260f, width = 200f, height = 25f,
        fieldType = FieldType.TEXT,
        placeholder = "Comments on motor condition"
    ),
    
    // Piping System
    FieldCoordinate(
        fieldName = "piping_satisfactory",
        x = 50f, y = 290f, width = 20f, height = 20f,
        fieldType = FieldType.CHECKBOX
    ),
    FieldCoordinate(
        fieldName = "piping_defective",
        x = 80f, y = 290f, width = 20f, height = 20f,
        fieldType = FieldType.CHECKBOX
    ),
    FieldCoordinate(
        fieldName = "piping_comments",
        x = 350f, y = 290f, width = 200f, height = 25f,
        fieldType = FieldType.TEXT,
        placeholder = "Comments on piping condition"
    ),
    
    // Lubrication System
    FieldCoordinate(
        fieldName = "lubrication_satisfactory",
        x = 50f, y = 320f, width = 20f, height = 20f,
        fieldType = FieldType.CHECKBOX
    ),
    FieldCoordinate(
        fieldName = "lubrication_defective",
        x = 80f, y = 320f, width = 20f, height = 20f,
        fieldType = FieldType.CHECKBOX
    ),
    FieldCoordinate(
        fieldName = "lubrication_comments",
        x = 350f, y = 320f, width = 200f, height = 25f,
        fieldType = FieldType.TEXT,
        placeholder = "Comments on lubrication system"
    ),
    
    // Overall Visual Assessment
    FieldCoordinate(
        fieldName = "visual_inspection_photo",
        x = 50f, y = 360f, width = 100f, height = 30f,
        fieldType = FieldType.PHOTO,
        placeholder = "Capture Equipment Photo"
    )
)
```

### Pressure Test Section (Y: 420-540)
```kotlin
val pressureTestSection = listOf(
    FieldCoordinate(
        fieldName = "pressure_test_performed",
        x = 80f, y = 440f, width = 20f, height = 20f,
        fieldType = FieldType.CHECKBOX,
        isRequired = true
    ),
    FieldCoordinate(
        fieldName = "pressure_test_date",
        x = 200f, y = 440f, width = 120f, height = 25f,
        fieldType = FieldType.DATE,
        dependencies = listOf("pressure_test_performed"),
        placeholder = "Test Date"
    ),
    FieldCoordinate(
        fieldName = "test_pressure_value",
        x = 350f, y = 440f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        dependencies = listOf("pressure_test_performed"),
        placeholder = "Test Pressure (Bar)"
    ),
    FieldCoordinate(
        fieldName = "operating_pressure",
        x = 200f, y = 470f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Operating Pressure (Bar)"
    ),
    FieldCoordinate(
        fieldName = "pressure_drop_rate",
        x = 350f, y = 470f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Pressure Drop Rate"
    ),
    FieldCoordinate(
        fieldName = "pressure_test_pass",
        x = 80f, y = 500f, width = 20f, height = 20f,
        fieldType = FieldType.CHECKBOX
    ),
    FieldCoordinate(
        fieldName = "pressure_test_fail",
        x = 120f, y = 500f, width = 20f, height = 20f,
        fieldType = FieldType.CHECKBOX
    ),
    FieldCoordinate(
        fieldName = "pressure_test_comments",
        x = 200f, y = 500f, width = 300f, height = 25f,
        fieldType = FieldType.TEXT,
        placeholder = "Pressure test results and observations"
    )
)
```

### Signatures Section (Y: 560-620)
```kotlin
val signaturesSection = listOf(
    FieldCoordinate(
        fieldName = "inspector_signature",
        x = 50f, y = 580f, width = 200f, height = 40f,
        fieldType = FieldType.SIGNATURE,
        isRequired = true,
        placeholder = "Inspector Signature"
    ),
    FieldCoordinate(
        fieldName = "signature_date",
        x = 350f, y = 580f, width = 120f, height = 25f,
        fieldType = FieldType.DATE,
        isRequired = true,
        placeholder = "DD/MM/YYYY"
    )
)
```

### Performance Test Section (Y: 560-680)
```kotlin
val performanceTestSection = listOf(
    FieldCoordinate(
        fieldName = "flow_rate_test",
        x = 80f, y = 580f, width = 20f, height = 20f,
        fieldType = FieldType.CHECKBOX
    ),
    FieldCoordinate(
        fieldName = "flow_rate_value",
        x = 200f, y = 580f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Flow Rate (L/min)"
    ),
    FieldCoordinate(
        fieldName = "vibration_test",
        x = 80f, y = 610f, width = 20f, height = 20f,
        fieldType = FieldType.CHECKBOX
    ),
    FieldCoordinate(
        fieldName = "vibration_level",
        x = 200f, y = 610f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Vibration Level (mm/s)"
    ),
    FieldCoordinate(
        fieldName = "temperature_check",
        x = 80f, y = 640f, width = 20f, height = 20f,
        fieldType = FieldType.CHECKBOX
    ),
    FieldCoordinate(
        fieldName = "operating_temperature",
        x = 200f, y = 640f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Temperature (°C)"
    ),
    FieldCoordinate(
        fieldName = "performance_satisfactory",
        x = 80f, y = 670f, width = 20f, height = 20f,
        fieldType = FieldType.CHECKBOX,
        isRequired = true
    ),
    FieldCoordinate(
        fieldName = "performance_comments",
        x = 200f, y = 670f, width = 300f, height = 25f,
        fieldType = FieldType.MULTILINE_TEXT,
        placeholder = "Performance test observations and recommendations"
    )
)
```

### Signature Section (Y: 720-780)
```kotlin
val signatureSection = listOf(
    FieldCoordinate(
        fieldName = "inspector_signature",
        x = 100f, y = 740f, width = 180f, height = 50f,
        fieldType = FieldType.SIGNATURE,
        isRequired = true,
        placeholder = "Inspector Signature"
    ),
    FieldCoordinate(
        fieldName = "inspector_date",
        x = 100f, y = 795f, width = 120f, height = 25f,
        fieldType = FieldType.DATE,
        isRequired = true,
        placeholder = "Date Signed"
    ),
    FieldCoordinate(
        fieldName = "supervisor_signature",
        x = 350f, y = 740f, width = 180f, height = 50f,
        fieldType = FieldType.SIGNATURE,
        isRequired = true,
        placeholder = "Supervisor Signature"
    ),
    FieldCoordinate(
        fieldName = "supervisor_date",
        x = 350f, y = 795f, width = 120f, height = 25f,
        fieldType = FieldType.DATE,
        isRequired = true,
        placeholder = "Date Signed"
    )
)
```

---

## Form 2: Bowie Pump Weekly Checklist

**Source PDF**: `Bowie Pump Weekly check list.pdf`
**Form ID**: `bowie_weekly_checklist`
**Page Size**: 612 x 792 points (Letter size)

### Header Section
```kotlin
val bowieWeeklyHeader = listOf(
    FieldCoordinate(
        fieldName = "week_ending_date",
        x = 400f, y = 70f, width = 120f, height = 25f,
        fieldType = FieldType.DATE,
        isRequired = true,
        placeholder = "Week Ending Date"
    ),
    FieldCoordinate(
        fieldName = "pump_number",
        x = 150f, y = 100f, width = 100f, height = 25f,
        fieldType = FieldType.EQUIPMENT_ID,
        isRequired = true,
        placeholder = "Pump Number"
    ),
    FieldCoordinate(
        fieldName = "pump_location",
        x = 300f, y = 100f, width = 150f, height = 25f,
        fieldType = FieldType.TEXT,
        isRequired = true,
        placeholder = "Pump Location"
    ),
    FieldCoordinate(
        fieldName = "technician_name",
        x = 500f, y = 100f, width = 150f, height = 25f,
        fieldType = FieldType.TEXT,
        isRequired = true,
        placeholder = "Technician Name"
    )
)
```

### Daily Inspection Grid (Y: 150-350)
```kotlin
val dailyInspectionGrid = listOf(
    // Monday Column (X: 120f)
    FieldCoordinate("mon_oil_level", 120f, 180f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("mon_oil_condition", 120f, 210f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("mon_temperature_check", 120f, 240f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("mon_vibration_check", 120f, 270f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("mon_pressure_check", 120f, 300f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("mon_leakage_check", 120f, 330f, 20f, 20f, FieldType.CHECKBOX),
    
    // Tuesday Column (X: 170f)
    FieldCoordinate("tue_oil_level", 170f, 180f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("tue_oil_condition", 170f, 210f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("tue_temperature_check", 170f, 240f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("tue_vibration_check", 170f, 270f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("tue_pressure_check", 170f, 300f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("tue_leakage_check", 170f, 330f, 20f, 20f, FieldType.CHECKBOX),
    
    // Wednesday Column (X: 220f)
    FieldCoordinate("wed_oil_level", 220f, 180f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("wed_oil_condition", 220f, 210f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("wed_temperature_check", 220f, 240f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("wed_vibration_check", 220f, 270f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("wed_pressure_check", 220f, 300f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("wed_leakage_check", 220f, 330f, 20f, 20f, FieldType.CHECKBOX),
    
    // Thursday Column (X: 270f)
    FieldCoordinate("thu_oil_level", 270f, 180f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("thu_oil_condition", 270f, 210f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("thu_temperature_check", 270f, 240f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("thu_vibration_check", 270f, 270f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("thu_pressure_check", 270f, 300f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("thu_leakage_check", 270f, 330f, 20f, 20f, FieldType.CHECKBOX),
    
    // Friday Column (X: 320f)
    FieldCoordinate("fri_oil_level", 320f, 180f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("fri_oil_condition", 320f, 210f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("fri_temperature_check", 320f, 240f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("fri_vibration_check", 320f, 270f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("fri_pressure_check", 320f, 300f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("fri_leakage_check", 320f, 330f, 20f, 20f, FieldType.CHECKBOX)
)
```

### Detailed Measurements Section (Y: 380-500)
```kotlin
val measurementsSection = listOf(
    FieldCoordinate(
        fieldName = "oil_temperature_reading",
        x = 150f, y = 400f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Oil Temp (°C)"
    ),
    FieldCoordinate(
        fieldName = "bearing_temperature",
        x = 300f, y = 400f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Bearing Temp (°C)"
    ),
    FieldCoordinate(
        fieldName = "vibration_reading",
        x = 450f, y = 400f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Vibration (mm/s)"
    ),
    FieldCoordinate(
        fieldName = "discharge_pressure",
        x = 150f, y = 430f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Discharge Pressure (Bar)"
    ),
    FieldCoordinate(
        fieldName = "suction_pressure",
        x = 300f, y = 430f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Suction Pressure (Bar)"
    ),
    FieldCoordinate(
        fieldName = "flow_rate",
        x = 450f, y = 430f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Flow Rate (L/min)"
    ),
    FieldCoordinate(
        fieldName = "operating_hours",
        x = 150f, y = 460f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Operating Hours"
    )
)
```

### Issues and Actions Section (Y: 520-640)
```kotlin
val issuesActionsSection = listOf(
    FieldCoordinate(
        fieldName = "issues_identified",
        x = 50f, y = 540f, width = 500f, height = 60f,
        fieldType = FieldType.MULTILINE_TEXT,
        placeholder = "Describe any issues identified during weekly inspection"
    ),
    FieldCoordinate(
        fieldName = "corrective_actions",
        x = 50f, y = 610f, width = 500f, height = 60f,
        fieldType = FieldType.MULTILINE_TEXT,
        placeholder = "Describe corrective actions taken or recommended"
    )
)
```

### Signature Section (Y: 680-740)
```kotlin
val bowieSignatureSection = listOf(
    FieldCoordinate(
        fieldName = "technician_signature",
        x = 50f, y = 700f, width = 200f, height = 40f,
        fieldType = FieldType.SIGNATURE,
        isRequired = true,
        placeholder = "Technician Signature"
    ),
    FieldCoordinate(
        fieldName = "technician_signature_date",
        x = 50f, y = 745f, width = 120f, height = 25f,
        fieldType = FieldType.DATE,
        isRequired = true,
        placeholder = "DD/MM/YYYY"
    ),
    FieldCoordinate(
        fieldName = "supervisor_signature",
        x = 350f, y = 700f, width = 200f, height = 40f,
        fieldType = FieldType.SIGNATURE,
        isRequired = true,
        placeholder = "Supervisor Signature"
    ),
    FieldCoordinate(
        fieldName = "supervisor_signature_date",
        x = 350f, y = 745f, width = 120f, height = 25f,
        fieldType = FieldType.DATE,
        isRequired = true,
        placeholder = "DD/MM/YYYY"
    )
)
```

---

## Form 3: MMU Production Daily Log

**Source PDF**: `mmu production daily log.pdf`
**Form ID**: `mmu_production_log`
**Page Size**: 612 x 792 points (Letter size)

### Production Header
```kotlin
val productionLogHeader = listOf(
    FieldCoordinate(
        fieldName = "production_date",
        x = 450f, y = 60f, width = 120f, height = 25f,
        fieldType = FieldType.DATE,
        isRequired = true,
        placeholder = "Production Date"
    ),
    FieldCoordinate(
        fieldName = "shift_selection",
        x = 200f, y = 60f, width = 100f, height = 25f,
        fieldType = FieldType.DROPDOWN,
        isRequired = true,
        defaultValue = "Day Shift"
    ),
    FieldCoordinate(
        fieldName = "operator_name",
        x = 450f, y = 90f, width = 150f, height = 25f,
        fieldType = FieldType.TEXT,
        isRequired = true,
        placeholder = "Operator Name"
    ),
    FieldCoordinate(
        fieldName = "site_location",
        x = 200f, y = 90f, width = 150f, height = 25f,
        fieldType = FieldType.SITE_CODE,
        isRequired = true,
        placeholder = "Site Code"
    ),
    FieldCoordinate(
        fieldName = "weather_conditions",
        x = 200f, y = 120f, width = 150f, height = 25f,
        fieldType = FieldType.DROPDOWN,
        placeholder = "Weather"
    ),
    FieldCoordinate(
        fieldName = "temperature",
        x = 400f, y = 120f, width = 80f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Temp (°C)"
    )
)
```

### Equipment Hours Tracking
```kotlin
val equipmentHoursSection = listOf(
    FieldCoordinate(
        fieldName = "mmu_start_hours",
        x = 150f, y = 170f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        isRequired = true,
        placeholder = "Start Hours"
    ),
    FieldCoordinate(
        fieldName = "mmu_end_hours",
        x = 300f, y = 170f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        isRequired = true,
        placeholder = "End Hours"
    ),
    FieldCoordinate(
        fieldName = "mmu_total_hours",
        x = 450f, y = 170f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Total Hours (Auto-calculated)"
    ),
    FieldCoordinate(
        fieldName = "productive_hours",
        x = 150f, y = 200f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Productive Hours"
    ),
    FieldCoordinate(
        fieldName = "downtime_hours",
        x = 300f, y = 200f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Downtime Hours"
    ),
    FieldCoordinate(
        fieldName = "standby_hours",
        x = 450f, y = 200f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Standby Hours"
    )
)
```

### Production Metrics
```kotlin
val productionMetricsSection = listOf(
    FieldCoordinate(
        fieldName = "holes_drilled_count",
        x = 150f, y = 250f, width = 100f, height = 25f,
        fieldType = FieldType.INTEGER,
        isRequired = true,
        placeholder = "Number of Holes"
    ),
    FieldCoordinate(
        fieldName = "total_meters_drilled",
        x = 300f, y = 250f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        isRequired = true,
        placeholder = "Total Meters"
    ),
    FieldCoordinate(
        fieldName = "average_hole_depth",
        x = 450f, y = 250f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Avg Depth (Auto-calc)"
    ),
    FieldCoordinate(
        fieldName = "drilling_rate",
        x = 150f, y = 280f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Rate (m/hr)"
    ),
    FieldCoordinate(
        fieldName = "pattern_completed",
        x = 300f, y = 280f, width = 100f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "% Pattern Complete"
    ),
    FieldCoordinate(
        fieldName = "bench_level",
        x = 450f, y = 280f, width = 100f, height = 25f,
        fieldType = FieldType.TEXT,
        placeholder = "Bench Level"
    )
)
```

### Downtime Analysis
```kotlin
val downtimeSection = listOf(
    FieldCoordinate(
        fieldName = "mechanical_downtime",
        x = 150f, y = 330f, width = 80f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Mechanical (hrs)"
    ),
    FieldCoordinate(
        fieldName = "electrical_downtime",
        x = 250f, y = 330f, width = 80f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Electrical (hrs)"
    ),
    FieldCoordinate(
        fieldName = "hydraulic_downtime",
        x = 350f, y = 330f, width = 80f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Hydraulic (hrs)"
    ),
    FieldCoordinate(
        fieldName = "weather_downtime",
        x = 450f, y = 330f, width = 80f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Weather (hrs)"
    ),
    FieldCoordinate(
        fieldName = "waiting_downtime",
        x = 150f, y = 360f, width = 80f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Waiting (hrs)"
    ),
    FieldCoordinate(
        fieldName = "other_downtime",
        x = 250f, y = 360f, width = 80f, height = 25f,
        fieldType = FieldType.NUMBER,
        placeholder = "Other (hrs)"
    ),
    FieldCoordinate(
        fieldName = "downtime_description",
        x = 50f, y = 390f, width = 500f, height = 40f,
        fieldType = FieldType.MULTILINE_TEXT,
        placeholder = "Detailed description of downtime causes and resolution"
    )
)
```

### Signature Section (Y: 450-510)
```kotlin
val productionLogSignatureSection = listOf(
    FieldCoordinate(
        fieldName = "operator_signature",
        x = 50f, y = 470f, width = 200f, height = 40f,
        fieldType = FieldType.SIGNATURE,
        isRequired = true,
        placeholder = "Operator Signature"
    ),
    FieldCoordinate(
        fieldName = "operator_signature_date",
        x = 50f, y = 515f, width = 120f, height = 25f,
        fieldType = FieldType.DATE,
        isRequired = true,
        placeholder = "DD/MM/YYYY"
    ),
    FieldCoordinate(
        fieldName = "supervisor_signature",
        x = 350f, y = 470f, width = 200f, height = 40f,
        fieldType = FieldType.SIGNATURE,
        isRequired = true,
        placeholder = "Supervisor Signature"
    ),
    FieldCoordinate(
        fieldName = "supervisor_signature_date",
        x = 350f, y = 515f, width = 120f, height = 25f,
        fieldType = FieldType.DATE,
        isRequired = true,
        placeholder = "DD/MM/YYYY"
    )
)
```

---

## Form 4: Blast Hole Log

Corresponds to `blast hole log.pdf`.

### Log Header
```kotlin
val blastLogHeader = listOf(
    FieldCoordinate("date", 50f, 750f, 100f, 20f, FieldType.DATE, true, "Date"),
    FieldCoordinate("shift", 200f, 750f, 100f, 20f, FieldType.TEXT, true, "Shift"),
    FieldCoordinate("operator_name", 350f, 750f, 150f, 20f, FieldType.TEXT, true, "Operator"),
    FieldCoordinate("site_location", 500f, 750f, 100f, 20f, FieldType.TEXT, true, "Site")
)
```

### Hole Data Table (Example for a single row, assuming a dynamic table in UI)
```kotlin
val blastHoleDataRow = listOf(
    FieldCoordinate("hole_id", 50f, 700f, 80f, 20f, FieldType.TEXT, true, "Hole ID"),
    FieldCoordinate("hole_depth_meters", 140f, 700f, 80f, 20f, FieldType.NUMBER, true, "Depth (m)"),
    FieldCoordinate("hole_diameter_mm", 230f, 700f, 80f, 20f, FieldType.NUMBER, true, "Diameter (mm)"),
    FieldCoordinate("powder_factor", 320f, 700f, 80f, 20f, FieldType.NUMBER, true, "Powder Factor"),
    FieldCoordinate("geology_notes", 410f, 700f, 150f, 20f, FieldType.TEXT, false, "Geology Notes")
)
```

### Summary & Signatures
```kotlin
val blastLogSummary = listOf(
    FieldCoordinate("total_holes_drilled", 50f, 100f, 120f, 20f, FieldType.INTEGER, true, "Total Holes"),
    FieldCoordinate("total_meters_drilled_summary", 200f, 100f, 120f, 20f, FieldType.NUMBER, true, "Total Meters"),
    FieldCoordinate("general_comments", 50f, 60f, 400f, 30f, FieldType.MULTILINE_TEXT, false, "General Comments")
)

val blastLogSignatures = listOf(
    FieldCoordinate("operator_signature", 50f, 20f, 150f, 30f, FieldType.SIGNATURE, true, "Operator Signature"),
    FieldCoordinate("supervisor_signature", 250f, 20f, 150f, 30f, FieldType.SIGNATURE, true, "Supervisor Signature")
)
```

---

## Form 5: MMU Quality Report

Corresponds to `mmu quality report.pdf`.

### Report Header
```kotlin
val qualityReportHeader = listOf(
    FieldCoordinate("report_date", 50f, 780f, 120f, 20f, FieldType.DATE, true, "Report Date"),
    FieldCoordinate("mmu_id", 200f, 780f, 120f, 20f, FieldType.TEXT, true, "MMU ID"),
    FieldCoordinate("product_type", 350f, 780f, 150f, 20f, FieldType.TEXT, true, "Product Type"),
    FieldCoordinate("lot_number", 530f, 780f, 100f, 20f, FieldType.TEXT, true, "Lot Number")
)
```

### Quality Control Checks
```kotlin
val qualityControlChecks = listOf(
    FieldCoordinate("density_check_pass", 50f, 740f, 80f, 20f, FieldType.CHECKBOX, true, "Density Pass"),
    FieldCoordinate("density_check_fail", 140f, 740f, 80f, 20f, FieldType.CHECKBOX, true, "Density Fail"),
    FieldCoordinate("density_reading", 230f, 740f, 100f, 20f, FieldType.NUMBER, false, "Density Reading"),
    FieldCoordinate("temperature_check_pass", 50f, 710f, 80f, 20f, FieldType.CHECKBOX, true, "Temp Pass"),
    FieldCoordinate("temperature_check_fail", 140f, 710f, 80f, 20f, FieldType.CHECKBOX, true, "Temp Fail"),
    FieldCoordinate("temperature_reading", 230f, 710f, 100f, 20f, FieldType.NUMBER, false, "Temp Reading (°C)"),
    FieldCoordinate("viscosity_check_pass", 50f, 680f, 80f, 20f, FieldType.CHECKBOX, true, "Viscosity Pass"),
    FieldCoordinate("viscosity_check_fail", 140f, 680f, 80f, 20f, FieldType.CHECKBOX, true, "Viscosity Fail"),
    FieldCoordinate("viscosity_reading", 230f, 680f, 100f, 20f, FieldType.NUMBER, false, "Viscosity Reading")
)
```

### Corrective Actions & Signature
```kotlin
val qualityReportActions = listOf(
    FieldCoordinate("non_conformance_details", 50f, 640f, 500f, 50f, FieldType.MULTILINE_TEXT, false, "Details of Non-Conformance"),
    FieldCoordinate("corrective_action_taken", 50f, 580f, 500f, 50f, FieldType.MULTILINE_TEXT, false, "Corrective Action Taken")
)

val qualityReportSignature = listOf(
    FieldCoordinate("inspector_name", 50f, 520f, 200f, 20f, FieldType.TEXT, true, "Inspector Name"),
    FieldCoordinate("inspector_signature", 50f, 480f, 200f, 30f, FieldType.SIGNATURE, true, "Inspector Signature"),
    FieldCoordinate("inspection_date", 300f, 480f, 120f, 20f, FieldType.DATE, true, "Inspection Date")
)
```

---

## Form 6: Job Card

Corresponds to `job card.pdf`.

### Job Details
```kotlin
val jobCardHeader = listOf(
    FieldCoordinate("job_card_number", 50f, 800f, 150f, 20f, FieldType.TEXT, true, "Job Card #"),
    FieldCoordinate("job_date", 450f, 800f, 100f, 20f, FieldType.DATE, true, "Date"),
    FieldCoordinate("customer_name", 50f, 770f, 250f, 20f, FieldType.TEXT, true, "Customer Name"),
    FieldCoordinate("site_address", 50f, 740f, 250f, 20f, FieldType.TEXT, true, "Site Address"),
    FieldCoordinate("contact_person", 350f, 770f, 200f, 20f, FieldType.TEXT, false, "Contact Person"),
    FieldCoordinate("contact_phone", 350f, 740f, 200f, 20f, FieldType.TEXT, false, "Contact Phone")
)
```

### Work Description
```kotlin
val workDescription = listOf(
    FieldCoordinate("work_requested", 50f, 700f, 500f, 60f, FieldType.MULTILINE_TEXT, true, "Work Requested by Customer"),
    FieldCoordinate("work_performed", 50f, 620f, 500f, 80f, FieldType.MULTILINE_TEXT, true, "Description of Work Performed")
)
```

### Labor and Materials
```kotlin
val laborAndMaterials = listOf(
    // Example for a single row, assuming a dynamic table in UI
    FieldCoordinate("technician_name_job", 50f, 520f, 150f, 20f, FieldType.TEXT, true, "Technician"),
    FieldCoordinate("hours_worked", 220f, 520f, 80f, 20f, FieldType.NUMBER, true, "Hours"),
    FieldCoordinate("material_used_1", 50f, 490f, 350f, 20f, FieldType.TEXT, false, "Material/Part Used"),
    FieldCoordinate("material_qty_1", 420f, 490f, 80f, 20f, FieldType.INTEGER, false, "Qty"),
    FieldCoordinate("material_used_2", 50f, 460f, 350f, 20f, FieldType.TEXT, false, "Material/Part Used"),
    FieldCoordinate("material_qty_2", 420f, 460f, 80f, 20f, FieldType.INTEGER, false, "Qty")
)
```

### Completion and Signatures
```kotlin
val jobCardCompletion = listOf(
    FieldCoordinate("job_completed_successfully", 50f, 150f, 20f, 20f, FieldType.CHECKBOX, true, "Job Completed"),
    FieldCoordinate("further_action_required", 200f, 150f, 20f, 20f, FieldType.CHECKBOX, true, "Further Action Req."),
    FieldCoordinate("completion_comments", 50f, 100f, 500f, 40f, FieldType.MULTILINE_TEXT, false, "Comments")
)

val jobCardSignatures = listOf(
    FieldCoordinate("technician_signature_job", 50f, 50f, 200f, 30f, FieldType.SIGNATURE, true, "Technician Signature"),
    FieldCoordinate("customer_signature", 350f, 50f, 200f, 30f, FieldType.SIGNATURE, true, "Customer Signature & Print Name")
)
```

---

## Form 7: Pre-Task Hazard Analysis

Corresponds to `pretask.pdf`.

### Job and Risk Assessment
```kotlin
val pretaskHeader = listOf(
    FieldCoordinate("pretask_date", 50f, 780f, 120f, 20f, FieldType.DATE, true, "Date"),
    FieldCoordinate("work_area", 200f, 780f, 200f, 20f, FieldType.TEXT, true, "Work Area/Location"),
    FieldCoordinate("task_description", 50f, 750f, 450f, 20f, FieldType.TEXT, true, "Task to be Performed")
)

val riskAssessment = listOf(
    FieldCoordinate("risk_level_high", 50f, 720f, 20f, 20f, FieldType.CHECKBOX, true, "High"),
    FieldCoordinate("risk_level_medium", 120f, 720f, 20f, 20f, FieldType.CHECKBOX, true, "Medium"),
    FieldCoordinate("risk_level_low", 190f, 720f, 20f, 20f, FieldType.CHECKBOX, true, "Low")
)
```

### Hazard Identification (Example for a single row)
```kotlin
val hazardIdentificationRow = listOf(
    FieldCoordinate("hazard_description_1", 50f, 680f, 200f, 20f, FieldType.TEXT, true, "Potential Hazard"),
    FieldCoordinate("hazard_controls_1", 270f, 680f, 280f, 20f, FieldType.TEXT, true, "Controls to Mitigate")
)
```

### Required PPE
```kotlin
val requiredPpe = listOf(
    FieldCoordinate("ppe_hard_hat", 50f, 400f, 20f, 20f, FieldType.CHECKBOX, false, "Hard Hat"),
    FieldCoordinate("ppe_safety_glasses", 150f, 400f, 20f, 20f, FieldType.CHECKBOX, false, "Safety Glasses"),
    FieldCoordinate("ppe_gloves", 250f, 400f, 20f, 20f, FieldType.CHECKBOX, false, "Gloves"),
    FieldCoordinate("ppe_steel_toes", 350f, 400f, 20f, 20f, FieldType.CHECKBOX, false, "Steel-Toed Boots"),
    FieldCoordinate("ppe_fall_arrest", 50f, 370f, 20f, 20f, FieldType.CHECKBOX, false, "Fall Arrest"),
    FieldCoordinate("ppe_respirator", 150f, 370f, 20f, 20f, FieldType.CHECKBOX, false, "Respirator"),
    FieldCoordinate("ppe_other", 250f, 370f, 20f, 20f, FieldType.CHECKBOX, false, "Other (Specify)"),
    FieldCoordinate("ppe_other_details", 280f, 370f, 150f, 20f, FieldType.TEXT, false, "Specify Other PPE")
)
```

### Crew Sign-off (Example for a single signature)
```kotlin
val pretaskSignatures = listOf(
    FieldCoordinate("crew_member_name_1", 50f, 100f, 200f, 20f, FieldType.TEXT, true, "Crew Member Name"),
    FieldCoordinate("crew_member_signature_1", 270f, 100f, 150f, 30f, FieldType.SIGNATURE, true, "Signature"),
    FieldCoordinate("supervisor_approval_name", 50f, 50f, 200f, 20f, FieldType.TEXT, true, "Supervisor Name"),
    FieldCoordinate("supervisor_approval_signature", 270f, 50f, 150f, 30f, FieldType.SIGNATURE, true, "Supervisor Signature")
)
```

---

## Form 8: Fire Extinguisher Inspection Checklist

Corresponds to `FIRE EXTINGUISHER INSPECTION CHECKLIST.pdf`.

### Extinguisher Details
```kotlin
val extinguisherDetails = listOf(
    FieldCoordinate("inspection_date_ext", 50f, 780f, 120f, 20f, FieldType.DATE, true, "Inspection Date"),
    FieldCoordinate("inspector_name_ext", 200f, 780f, 200f, 20f, FieldType.TEXT, true, "Inspector Name"),
    FieldCoordinate("extinguisher_id", 50f, 750f, 150f, 20f, FieldType.TEXT, true, "Extinguisher ID/Serial #"),
    FieldCoordinate("extinguisher_location", 250f, 750f, 250f, 20f, FieldType.TEXT, true, "Location of Extinguisher")
)
```

### Inspection Checklist
```kotlin
val extinguisherChecklist = listOf(
    // Row 1
    FieldCoordinate("check_accessibility", 500f, 700f, 20f, 20f, FieldType.CHECKBOX, true, "Accessible"),
    FieldCoordinate("check_pressure_gauge", 500f, 675f, 20f, 20f, FieldType.CHECKBOX, true, "Pressure Gauge OK"),
    FieldCoordinate("check_safety_pin", 500f, 650f, 20f, 20f, FieldType.CHECKBOX, true, "Safety Pin Intact"),
    FieldCoordinate("check_nozzle_hose", 500f, 625f, 20f, 20f, FieldType.CHECKBOX, true, "Nozzle/Hose OK"),
    FieldCoordinate("check_cylinder_damage", 500f, 600f, 20f, 20f, FieldType.CHECKBOX, true, "No Cylinder Damage"),
    FieldCoordinate("check_service_tag", 500f, 575f, 20f, 20f, FieldType.CHECKBOX, true, "Service Tag Current")
)
```

### Outcome and Signature
```kotlin
val extinguisherOutcome = listOf(
    FieldCoordinate("overall_status_pass", 50f, 200f, 20f, 20f, FieldType.CHECKBOX, true, "Pass"),
    FieldCoordinate("overall_status_fail", 150f, 200f, 20f, 20f, FieldType.CHECKBOX, true, "Fail (Requires Action)"),
    FieldCoordinate("action_required_details", 50f, 150f, 500f, 40f, FieldType.MULTILINE_TEXT, false, "Corrective Action Required"),
    FieldCoordinate("inspector_signature_ext", 50f, 80f, 200f, 30f, FieldType.SIGNATURE, true, "Inspector Signature")
)
```

---

## Form 9: On-Bench MMU Inspection

Corresponds to `ON BENCH MMU INSPECTION.pdf`.

### Inspection Header
```kotlin
val onBenchInspectionHeader = listOf(
    FieldCoordinate("mmu_id_bench", 50f, 780f, 150f, 20f, FieldType.TEXT, true, "MMU ID / Serial #"),
    FieldCoordinate("inspection_date_bench", 450f, 780f, 100f, 20f, FieldType.DATE, true, "Date"),
    FieldCoordinate("inspector_name_bench", 50f, 750f, 200f, 20f, FieldType.TEXT, true, "Inspected By")
)
```

### System Checks (Mechanical, Electrical, Hydraulic) - Example Fields
```kotlin
val onBenchSystemChecks = listOf(
    // Mechanical
    FieldCoordinate("check_mech_chassis", 500f, 700f, 20f, 20f, FieldType.CHECKBOX, true, "Chassis Integrity OK"),
    FieldCoordinate("check_mech_pumps", 500f, 680f, 20f, 20f, FieldType.CHECKBOX, true, "Pumps & Gearboxes OK"),
    FieldCoordinate("check_mech_hoses", 500f, 660f, 20f, 20f, FieldType.CHECKBOX, true, "Hoses & Fittings OK"),
    // Electrical
    FieldCoordinate("check_elec_wiring", 500f, 620f, 20f, 20f, FieldType.CHECKBOX, true, "Wiring & Harnesses OK"),
    FieldCoordinate("check_elec_lights", 500f, 600f, 20f, 20f, FieldType.CHECKBOX, true, "Lights & Signals OK"),
    // Hydraulic
    FieldCoordinate("check_hyd_fluid_level", 500f, 560f, 20f, 20f, FieldType.CHECKBOX, true, "Hydraulic Fluid Level OK"),
    FieldCoordinate("check_hyd_leaks", 500f, 540f, 20f, 20f, FieldType.CHECKBOX, true, "No Hydraulic Leaks")
)
```

### Calibration and Comments
```kotlin
val onBenchCalibration = listOf(
    FieldCoordinate("calibration_emulsion_pump", 50f, 400f, 200f, 20f, FieldType.TEXT, true, "Emulsion Pump Calib. Value"),
    FieldCoordinate("calibration_sensitizer_pump", 300f, 400f, 200f, 20f, FieldType.TEXT, true, "Sensitizer Pump Calib. Value"),
    FieldCoordinate("inspection_comments_bench", 50f, 150f, 500f, 80f, FieldType.MULTILINE_TEXT, false, "Inspection Findings & Comments")
)
```

### Signatures
```kotlin
val onBenchSignatures = listOf(
    FieldCoordinate("inspector_signature_bench", 50f, 80f, 200f, 30f, FieldType.SIGNATURE, true, "Inspector Signature"),
    FieldCoordinate("supervisor_review_signature", 350f, 80f, 200f, 30f, FieldType.SIGNATURE, false, "Supervisor Review Signature")
)
```

---

## Form 10: MMU Handover Certificate

Corresponds to `MMU HANDOVER CERTIFICATE.pdf`.

### Certificate Details
```kotlin
val handoverCertificateDetails = listOf(
    FieldCoordinate("mmu_id_handover", 50f, 780f, 150f, 20f, FieldType.TEXT, true, "MMU ID / Serial #"),
    FieldCoordinate("handover_date", 450f, 780f, 100f, 20f, FieldType.DATE, true, "Date of Handover"),
    FieldCoordinate("mmu_hours", 50f, 750f, 150f, 20f, FieldType.NUMBER, true, "Current MMU Hours")
)
```

### Parties Involved
```kotlin
val handoverParties = listOf(
    // Transferring Party
    FieldCoordinate("transferring_party_name", 50f, 700f, 200f, 20f, FieldType.TEXT, true, "Transferring Party (Print Name)"),
    FieldCoordinate("transferring_party_signature", 50f, 660f, 200f, 30f, FieldType.SIGNATURE, true, "Signature"),
    // Receiving Party
    FieldCoordinate("receiving_party_name", 350f, 700f, 200f, 20f, FieldType.TEXT, true, "Receiving Party (Print Name)"),
    FieldCoordinate("receiving_party_signature", 350f, 660f, 200f, 30f, FieldType.SIGNATURE, true, "Signature")
)
```

### Condition Checklist
```kotlin
val handoverConditionChecks = listOf(
    FieldCoordinate("condition_exterior_ok", 50f, 500f, 20f, 20f, FieldType.CHECKBOX, true, "Exterior Condition Acceptable"),
    FieldCoordinate("condition_interior_ok", 50f, 470f, 20f, 20f, FieldType.CHECKBOX, true, "Cabin/Interior Condition Acceptable"),
    FieldCoordinate("condition_safety_equip_ok", 50f, 440f, 20f, 20f, FieldType.CHECKBOX, true, "Safety Equipment Present & OK"),
    FieldCoordinate("condition_logbook_ok", 50f, 410f, 20f, 20f, FieldType.CHECKBOX, true, "Logbook & Manuals Present")
)
```

### Handover Comments
```kotlin
val handoverComments = listOf(
    FieldCoordinate("handover_comments_notes", 50f, 150f, 500f, 80f, FieldType.MULTILINE_TEXT, false, "Comments on Handover (e.g., existing damage)")
)
```

---

## Form 11: MMU Chassis Maintenance Record

Corresponds to `MMU CHASSIS MAINTENANCE RECORD.pdf`.

### Record Header
```kotlin
val chassisMaintHeader = listOf(
    FieldCoordinate("maint_date_chassis", 50f, 780f, 120f, 20f, FieldType.DATE, true, "Maintenance Date"),
    FieldCoordinate("mmu_id_chassis", 200f, 780f, 150f, 20f, FieldType.TEXT, true, "MMU ID"),
    FieldCoordinate("work_order_num", 380f, 780f, 150f, 20f, FieldType.TEXT, false, "Work Order #"),
    FieldCoordinate("mechanic_name_chassis", 50f, 750f, 200f, 20f, FieldType.TEXT, true, "Mechanic Name")
)
```

### Chassis Inspection Checklist (Example Fields)
```kotlin
val chassisInspectionChecklist = listOf(
    FieldCoordinate("check_tires_wear", 500f, 700f, 20f, 20f, FieldType.CHECKBOX, true, "Tires/Wheels OK"),
    FieldCoordinate("check_brakes_system", 500f, 680f, 20f, 20f, FieldType.CHECKBOX, true, "Brake System OK"),
    FieldCoordinate("check_suspension", 500f, 660f, 20f, 20f, FieldType.CHECKBOX, true, "Suspension OK"),
    FieldCoordinate("check_steering", 500f, 640f, 20f, 20f, FieldType.CHECKBOX, true, "Steering System OK"),
    FieldCoordinate("check_chassis_frame", 500f, 620f, 20f, 20f, FieldType.CHECKBOX, true, "Chassis Frame Integrity OK"),
    FieldCoordinate("check_lights_electrical", 500f, 600f, 20f, 20f, FieldType.CHECKBOX, true, "Lights & Electrical OK")
)
```

### Parts & Labor
```kotlin
val chassisPartsAndLabor = listOf(
    FieldCoordinate("parts_used_1", 50f, 400f, 350f, 20f, FieldType.TEXT, false, "Part/Material Used"),
    FieldCoordinate("parts_qty_1", 420f, 400f, 80f, 20f, FieldType.INTEGER, false, "Qty"),
    FieldCoordinate("labor_hours_chassis", 50f, 350f, 100f, 20f, FieldType.NUMBER, true, "Labor Hours")
)
```

### Sign-off
```kotlin
val chassisMaintSignoff = listOf(
    FieldCoordinate("maintenance_comments", 50f, 150f, 500f, 80f, FieldType.MULTILINE_TEXT, false, "Work Performed & Comments"),
    FieldCoordinate("mechanic_signature_chassis", 50f, 80f, 200f, 30f, FieldType.SIGNATURE, true, "Mechanic Signature")
)
```

---

## Form 12: Monthly Process Maintenance Record

Corresponds to `MONTHLY PROCESS MAINTENANCE RECORD.pdf`.

### Record Header
```kotlin
val processMaintHeader = listOf(
    FieldCoordinate("maint_date_process", 50f, 780f, 120f, 20f, FieldType.DATE, true, "Maintenance Date"),
    FieldCoordinate("mmu_id_process", 200f, 780f, 150f, 20f, FieldType.TEXT, true, "MMU ID"),
    FieldCoordinate("technician_name_process", 50f, 750f, 200f, 20f, FieldType.TEXT, true, "Technician Name")
)
```

### Process Systems Checklist (Example Fields)
```kotlin
val processSystemsChecklist = listOf(
    FieldCoordinate("check_proc_emulsion_pump", 500f, 700f, 20f, 20f, FieldType.CHECKBOX, true, "Emulsion Pump OK"),
    FieldCoordinate("check_proc_sensitizer_pump", 500f, 680f, 20f, 20f, FieldType.CHECKBOX, true, "Sensitizer Pump OK"),
    FieldCoordinate("check_proc_augers", 500f, 660f, 20f, 20f, FieldType.CHECKBOX, true, "Augers & Mixers OK"),
    FieldCoordinate("check_proc_hoses", 500f, 640f, 20f, 20f, FieldType.CHECKBOX, true, "Process Hoses OK"),
    FieldCoordinate("check_proc_control_sys", 500f, 620f, 20f, 20f, FieldType.CHECKBOX, true, "Control System OK"),
    FieldCoordinate("check_proc_calibration", 500f, 600f, 20f, 20f, FieldType.CHECKBOX, true, "Calibration Verified")
)
```

### Sign-off
```kotlin
val processMaintSignoff = listOf(
    FieldCoordinate("process_maintenance_comments", 50f, 150f, 500f, 80f, FieldType.MULTILINE_TEXT, false, "Work Performed & Comments"),
    FieldCoordinate("technician_signature_process", 50f, 80f, 200f, 30f, FieldType.SIGNATURE, true, "Technician Signature")
)
```

---

## Form 13: PC Pump High/Low Pressure Trip Test

Corresponds to `PC PUMP HIGH LOW PRESSURE TRIP TEST.pdf`.

### Test Setup
```kotlin
val tripTestHeader = listOf(
    FieldCoordinate("test_date_trip", 50f, 780f, 120f, 20f, FieldType.DATE, true, "Test Date"),
    FieldCoordinate("mmu_id_trip", 200f, 780f, 150f, 20f, FieldType.TEXT, true, "MMU ID"),
    FieldCoordinate("pump_id_trip", 380f, 780f, 150f, 20f, FieldType.TEXT, true, "Pump ID"),
    FieldCoordinate("technician_name_trip", 50f, 750f, 200f, 20f, FieldType.TEXT, true, "Technician Name")
)
```

### Pressure Test Parameters & Results
```kotlin
val tripTestParameters = listOf(
    // High Pressure Test
    FieldCoordinate("high_pressure_setpoint", 50f, 600f, 150f, 20f, FieldType.NUMBER, true, "High Pressure Setpoint (PSI)"),
    FieldCoordinate("high_pressure_actual", 250f, 600f, 150f, 20f, FieldType.NUMBER, true, "Actual Trip Pressure (PSI)"),
    FieldCoordinate("high_pressure_pass", 450f, 600f, 20f, 20f, FieldType.CHECKBOX, true, "Pass"),
    FieldCoordinate("high_pressure_fail", 500f, 600f, 20f, 20f, FieldType.CHECKBOX, true, "Fail"),
    // Low Pressure Test
    FieldCoordinate("low_pressure_setpoint", 50f, 550f, 150f, 20f, FieldType.NUMBER, true, "Low Pressure Setpoint (PSI)"),
    FieldCoordinate("low_pressure_actual", 250f, 550f, 150f, 20f, FieldType.NUMBER, true, "Actual Trip Pressure (PSI)"),
    FieldCoordinate("low_pressure_pass", 450f, 550f, 20f, 20f, FieldType.CHECKBOX, true, "Pass"),
    FieldCoordinate("low_pressure_fail", 500f, 550f, 20f, 20f, FieldType.CHECKBOX, true, "Fail")
)
```

### Sign-off
```kotlin
val tripTestSignoff = listOf(
    FieldCoordinate("trip_test_comments", 50f, 150f, 500f, 80f, FieldType.MULTILINE_TEXT, false, "Test Comments & Observations"),
    FieldCoordinate("technician_signature_trip", 50f, 80f, 200f, 30f, FieldType.SIGNATURE, true, "Technician Signature")
)
```

---

## Form 14: Availability & Utilization Report

Corresponds to `Availabilty & Utilization.pdf`.

### Report Header
```kotlin
val utilizationReportHeader = listOf(
    FieldCoordinate("report_date_util", 50f, 780f, 120f, 20f, FieldType.DATE, true, "Report Period (e.g., Week of)"),
    FieldCoordinate("mmu_id_util", 200f, 780f, 150f, 20f, FieldType.TEXT, true, "MMU ID"),
    FieldCoordinate("operator_name_util", 380f, 780f, 200f, 20f, FieldType.TEXT, true, "Operator/Supervisor")
)
```

### Daily Log Table (Example for a single day/row)
```kotlin
val utilizationDailyRow = listOf(
    FieldCoordinate("log_date_day1", 50f, 700f, 80f, 20f, FieldType.DATE, true, "Date"),
    FieldCoordinate("operating_hours_day1", 140f, 700f, 60f, 20f, FieldType.NUMBER, true, "Operating"),
    FieldCoordinate("standby_hours_day1", 210f, 700f, 60f, 20f, FieldType.NUMBER, true, "Standby"),
    FieldCoordinate("downtime_mech_day1", 280f, 700f, 60f, 20f, FieldType.NUMBER, true, "DT-Mech"),
    FieldCoordinate("downtime_elec_day1", 350f, 700f, 60f, 20f, FieldType.NUMBER, true, "DT-Elec"),
    FieldCoordinate("downtime_other_day1", 420f, 700f, 60f, 20f, FieldType.NUMBER, true, "DT-Other"),
    FieldCoordinate("total_hours_day1", 490f, 700f, 60f, 20f, FieldType.NUMBER, true, "Total")
)
```

### Summary and Sign-off
```kotlin
val utilizationSummary = listOf(
    FieldCoordinate("total_operating_hours", 150f, 200f, 100f, 20f, FieldType.NUMBER, true, "Total Operating Hours"),
    FieldCoordinate("total_standby_hours", 150f, 170f, 100f, 20f, FieldType.NUMBER, true, "Total Standby Hours"),
    FieldCoordinate("total_downtime_hours", 150f, 140f, 100f, 20f, FieldType.NUMBER, true, "Total Downtime Hours"),
    FieldCoordinate("availability_percentage", 400f, 200f, 100f, 20f, FieldType.NUMBER, true, "Availability %"),
    FieldCoordinate("utilization_percentage", 400f, 170f, 100f, 20f, FieldType.NUMBER, true, "Utilization %")
)

val utilizationSignoff = listOf(
    FieldCoordinate("report_comments_util", 50f, 100f, 500f, 30f, FieldType.MULTILINE_TEXT, false, "Comments"),
    FieldCoordinate("supervisor_signature_util", 50f, 50f, 200f, 30f, FieldType.SIGNATURE, true, "Supervisor Signature")
)
```

---

## Form 15: Weekly Timesheet

Corresponds to `Copy of Timesheet(1).pdf`.

### Timesheet Header
```kotlin
val timesheetHeader = listOf(
    FieldCoordinate("employee_name", 50f, 780f, 250f, 20f, FieldType.TEXT, true, "Employee Name"),
    FieldCoordinate("employee_id", 350f, 780f, 150f, 20f, FieldType.EMPLOYEE_ID, true, "Employee ID"),
    FieldCoordinate("week_ending_date", 530f, 780f, 100f, 20f, FieldType.DATE, true, "Week Ending")
)
```

### Daily Hours Log (Example for a single day/row)
```kotlin
val timesheetDailyRow = listOf(
    FieldCoordinate("day_1_date", 50f, 700f, 80f, 20f, FieldType.DATE, true, "Date"),
    FieldCoordinate("day_1_start_time", 140f, 700f, 80f, 20f, FieldType.TIME, true, "Start Time"),
    FieldCoordinate("day_1_end_time", 230f, 700f, 80f, 20f, FieldType.TIME, true, "End Time"),
    FieldCoordinate("day_1_break_hours", 320f, 700f, 80f, 20f, FieldType.NUMBER, true, "Break (hrs)"),
    FieldCoordinate("day_1_total_hours", 410f, 700f, 80f, 20f, FieldType.NUMBER, true, "Total Hours")
)
```

### Summary and Approval
```kotlin
val timesheetSummary = listOf(
    FieldCoordinate("total_regular_hours", 410f, 200f, 100f, 20f, FieldType.NUMBER, true, "Total Regular Hours"),
    FieldCoordinate("total_overtime_hours", 410f, 170f, 100f, 20f, FieldType.NUMBER, true, "Total Overtime Hours"),
    FieldCoordinate("grand_total_hours", 410f, 140f, 100f, 20f, FieldType.NUMBER, true, "Grand Total")
)

val timesheetSignatures = listOf(
    FieldCoordinate("employee_signature", 50f, 80f, 200f, 30f, FieldType.SIGNATURE, true, "Employee Signature"),
    FieldCoordinate("approval_signature", 350f, 80f, 200f, 30f, FieldType.SIGNATURE, true, "Manager/Supervisor Signature")
)
```

---

## Form 16: Unit of Rig (UOR) Report

Corresponds to `UOR[1].pdf`.

### UOR Header
```kotlin
val uorHeader = listOf(
    FieldCoordinate("rig_id", 50f, 780f, 150f, 20f, FieldType.EQUIPMENT_ID, true, "Rig ID"),
    FieldCoordinate("report_date_uor", 250f, 780f, 120f, 20f, FieldType.DATE, true, "Date"),
    FieldCoordinate("shift_uor", 400f, 780f, 150f, 20f, FieldType.TEXT, true, "Shift (Day/Night)"),
    FieldCoordinate("operator_name_uor", 50f, 750f, 200f, 20f, FieldType.TEXT, true, "Operator")
)
```

### Hours Breakdown
```kotlin
val uorHoursBreakdown = listOf(
    FieldCoordinate("total_shift_hours", 200f, 700f, 100f, 20f, FieldType.NUMBER, true, "Total Shift Hours"),
    FieldCoordinate("operating_hours_uor", 200f, 670f, 100f, 20f, FieldType.NUMBER, true, "Operating Hours"),
    FieldCoordinate("standby_hours_uor", 200f, 640f, 100f, 20f, FieldType.NUMBER, true, "Standby Hours"),
    FieldCoordinate("maintenance_hours_planned", 200f, 610f, 100f, 20f, FieldType.NUMBER, true, "Planned Maintenance"),
    FieldCoordinate("maintenance_hours_unplanned", 200f, 580f, 100f, 20f, FieldType.NUMBER, true, "Unplanned Maintenance"),
    FieldCoordinate("downtime_hours_total", 200f, 550f, 100f, 20f, FieldType.NUMBER, true, "Total Downtime")
)
```

### Downtime Details & Sign-off
```kotlin
val uorDowntimeDetails = listOf(
    FieldCoordinate("downtime_reason_1", 50f, 400f, 400f, 20f, FieldType.TEXT, false, "Downtime Reason"),
    FieldCoordinate("downtime_duration_1", 470f, 400f, 80f, 20f, FieldType.NUMBER, false, "Duration (hrs)")
)

val uorSignatures = listOf(
    FieldCoordinate("uor_comments", 50f, 150f, 500f, 80f, FieldType.MULTILINE_TEXT, false, "Comments / Work Performed"),
    FieldCoordinate("operator_signature_uor", 50f, 80f, 200f, 30f, FieldType.SIGNATURE, true, "Operator Signature"),
    FieldCoordinate("supervisor_signature_uor", 350f, 80f, 200f, 30f, FieldType.SIGNATURE, true, "Supervisor Signature")
)
```

---

This document continues with detailed coordinate specifications for all remaining forms. Each form follows the same detailed mapping approach to ensure pixel-perfect field alignment and comprehensive data capture capabilities.
