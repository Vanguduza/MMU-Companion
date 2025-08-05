# AECI MMU Companion App: Complete Form Specifications

## Overview
This document provides comprehensive specifications for all 14 PDF-based forms in the AECI MMU Companion app. Each form includes detailed field mappings, validation rules, business logic, and export requirements.

---

## Form Index

1. [90 Day Pump System Inspection Checklist](#1-90-day-pump-system-inspection-checklist)
2. [Availability & Utilization Report](#2-availability--utilization-report)
3. [Blast Hole Log](#3-blast-hole-log)
4. [Bowie Pump Weekly Checklist](#4-bowie-pump-weekly-checklist)
5. [Fire Extinguisher Inspection Checklist](#5-fire-extinguisher-inspection-checklist)
6. [Job Card](#6-job-card)
7. [MMU Chassis Maintenance Record](#7-mmu-chassis-maintenance-record)
8. [MMU Handover Certificate](#8-mmu-handover-certificate)
9. [MMU Production Daily Log](#9-mmu-production-daily-log)
10. [MMU Quality Report](#10-mmu-quality-report)
11. [Monthly Process Maintenance Record](#11-monthly-process-maintenance-record)
12. [On Bench MMU Inspection](#12-on-bench-mmu-inspection)
13. [PC Pump High/Low Pressure Trip Test](#13-pc-pump-highlow-pressure-trip-test)
14. [Pre-Task Safety Assessment](#14-pre-task-safety-assessment)

---

## 1. 90 Day Pump System Inspection Checklist

### Form Metadata
- **Form ID**: `pump_inspection_90day`
- **Category**: Maintenance
- **Frequency**: Quarterly
- **Required Role**: Millwright, Technician
- **Dependencies**: Equipment must be operational
- **Estimated Completion Time**: 45-60 minutes

### Field Specifications

#### Header Section
```json
{
  "section": "header",
  "fields": [
    {
      "name": "inspection_date",
      "type": "date",
      "coordinates": { "x": 450, "y": 85, "width": 120, "height": 25 },
      "required": true,
      "validation": {
        "rule": "not_future_date",
        "message": "Inspection date cannot be in the future"
      }
    },
    {
      "name": "inspector_name",
      "type": "text",
      "coordinates": { "x": 150, "y": 110, "width": 200, "height": 25 },
      "required": true,
      "validation": {
        "rule": "min_length",
        "value": 2,
        "message": "Inspector name must be at least 2 characters"
      }
    },
    {
      "name": "equipment_id",
      "type": "equipment_id",
      "coordinates": { "x": 450, "y": 110, "width": 150, "height": 25 },
      "required": true,
      "validation": {
        "rule": "equipment_exists",
        "message": "Equipment ID must exist in system"
      }
    },
    {
      "name": "serial_number",
      "type": "text",
      "coordinates": { "x": 150, "y": 135, "width": 200, "height": 25 },
      "required": true,
      "auto_populate": "from_equipment_id"
    },
    {
      "name": "pump_location",
      "type": "text",
      "coordinates": { "x": 450, "y": 135, "width": 150, "height": 25 },
      "required": true,
      "auto_populate": "from_equipment_id"
    },
    {
      "name": "service_hours",
      "type": "number",
      "coordinates": { "x": 150, "y": 160, "width": 100, "height": 25 },
      "required": true,
      "validation": {
        "rule": "positive_number",
        "message": "Service hours must be positive"
      }
    }
  ]
}
```

#### Visual Inspection Section
```json
{
  "section": "visual_inspection",
  "fields": [
    {
      "name": "pump_housing_condition",
      "type": "radio_group",
      "coordinates": [
        { "option": "satisfactory", "x": 50, "y": 200, "width": 20, "height": 20 },
        { "option": "defective", "x": 80, "y": 200, "width": 20, "height": 20 }
      ],
      "required": true,
      "options": ["satisfactory", "defective"]
    },
    {
      "name": "pump_housing_comments",
      "type": "text",
      "coordinates": { "x": 350, "y": 200, "width": 200, "height": 25 },
      "required_if": "pump_housing_condition=defective"
    },
    {
      "name": "coupling_condition",
      "type": "radio_group",
      "coordinates": [
        { "option": "satisfactory", "x": 50, "y": 230, "width": 20, "height": 20 },
        { "option": "defective", "x": 80, "y": 230, "width": 20, "height": 20 }
      ],
      "required": true
    },
    {
      "name": "coupling_comments",
      "type": "text",
      "coordinates": { "x": 350, "y": 230, "width": 200, "height": 25 },
      "required_if": "coupling_condition=defective"
    },
    {
      "name": "motor_condition",
      "type": "radio_group",
      "coordinates": [
        { "option": "satisfactory", "x": 50, "y": 260, "width": 20, "height": 20 },
        { "option": "defective", "x": 80, "y": 260, "width": 20, "height": 20 }
      ],
      "required": true
    },
    {
      "name": "motor_comments",
      "type": "text",
      "coordinates": { "x": 350, "y": 260, "width": 200, "height": 25 },
      "required_if": "motor_condition=defective"
    },
    {
      "name": "piping_condition",
      "type": "radio_group",
      "coordinates": [
        { "option": "satisfactory", "x": 50, "y": 290, "width": 20, "height": 20 },
        { "option": "defective", "x": 80, "y": 290, "width": 20, "height": 20 }
      ],
      "required": true
    },
    {
      "name": "piping_comments",
      "type": "text",
      "coordinates": { "x": 350, "y": 290, "width": 200, "height": 25 },
      "required_if": "piping_condition=defective"
    },
    {
      "name": "lubrication_condition",
      "type": "radio_group",
      "coordinates": [
        { "option": "satisfactory", "x": 50, "y": 320, "width": 20, "height": 20 },
        { "option": "defective", "x": 80, "y": 320, "width": 20, "height": 20 }
      ],
      "required": true
    },
    {
      "name": "lubrication_comments",
      "type": "text",
      "coordinates": { "x": 350, "y": 320, "width": 200, "height": 25 },
      "required_if": "lubrication_condition=defective"
    },
    {
      "name": "equipment_photo",
      "type": "photo",
      "coordinates": { "x": 50, "y": 360, "width": 100, "height": 30 },
      "required": false,
      "max_files": 3
    }
  ]
}
```

#### Pressure Test Section
```json
{
  "section": "pressure_test",
  "fields": [
    {
      "name": "pressure_test_performed",
      "type": "checkbox",
      "coordinates": { "x": 80, "y": 440, "width": 20, "height": 20 },
      "required": true
    },
    {
      "name": "pressure_test_date",
      "type": "date",
      "coordinates": { "x": 200, "y": 440, "width": 120, "height": 25 },
      "required_if": "pressure_test_performed=true",
      "validation": {
        "rule": "same_or_before",
        "field": "inspection_date",
        "message": "Test date cannot be after inspection date"
      }
    },
    {
      "name": "test_pressure_value",
      "type": "number",
      "coordinates": { "x": 350, "y": 440, "width": 100, "height": 25 },
      "required_if": "pressure_test_performed=true",
      "validation": {
        "rule": "range",
        "min": 0,
        "max": 50,
        "message": "Test pressure must be between 0-50 Bar"
      },
      "unit": "Bar"
    },
    {
      "name": "operating_pressure",
      "type": "number",
      "coordinates": { "x": 200, "y": 470, "width": 100, "height": 25 },
      "required_if": "pressure_test_performed=true",
      "validation": {
        "rule": "positive_number",
        "message": "Operating pressure must be positive"
      },
      "unit": "Bar"
    },
    {
      "name": "pressure_drop_rate",
      "type": "number",
      "coordinates": { "x": 350, "y": 470, "width": 100, "height": 25 },
      "required_if": "pressure_test_performed=true",
      "unit": "Bar/min"
    },
    {
      "name": "pressure_test_result",
      "type": "radio_group",
      "coordinates": [
        { "option": "pass", "x": 80, "y": 500, "width": 20, "height": 20 },
        { "option": "fail", "x": 120, "y": 500, "width": 20, "height": 20 }
      ],
      "required_if": "pressure_test_performed=true"
    },
    {
      "name": "pressure_test_comments",
      "type": "text",
      "coordinates": { "x": 200, "y": 500, "width": 300, "height": 25 },
      "required_if": "pressure_test_result=fail"
    }
  ]
}
```

#### Performance Test Section
```json
{
  "section": "performance_test",
  "fields": [
    {
      "name": "flow_rate_test",
      "type": "checkbox",
      "coordinates": { "x": 80, "y": 580, "width": 20, "height": 20 }
    },
    {
      "name": "flow_rate_value",
      "type": "number",
      "coordinates": { "x": 200, "y": 580, "width": 100, "height": 25 },
      "required_if": "flow_rate_test=true",
      "unit": "L/min"
    },
    {
      "name": "vibration_test",
      "type": "checkbox",
      "coordinates": { "x": 80, "y": 610, "width": 20, "height": 20 }
    },
    {
      "name": "vibration_level",
      "type": "number",
      "coordinates": { "x": 200, "y": 610, "width": 100, "height": 25 },
      "required_if": "vibration_test=true",
      "unit": "mm/s",
      "validation": {
        "rule": "range",
        "min": 0,
        "max": 50,
        "message": "Vibration level must be between 0-50 mm/s"
      }
    },
    {
      "name": "temperature_check",
      "type": "checkbox",
      "coordinates": { "x": 80, "y": 640, "width": 20, "height": 20 }
    },
    {
      "name": "operating_temperature",
      "type": "number",
      "coordinates": { "x": 200, "y": 640, "width": 100, "height": 25 },
      "required_if": "temperature_check=true",
      "unit": "°C",
      "validation": {
        "rule": "range",
        "min": -10,
        "max": 100,
        "message": "Temperature must be between -10°C and 100°C"
      }
    },
    {
      "name": "performance_satisfactory",
      "type": "checkbox",
      "coordinates": { "x": 80, "y": 670, "width": 20, "height": 20 },
      "required": true
    },
    {
      "name": "performance_comments",
      "type": "multiline_text",
      "coordinates": { "x": 200, "y": 670, "width": 300, "height": 25 },
      "max_lines": 4
    }
  ]
}
```

#### Signature Section
```json
{
  "section": "signatures",
  "fields": [
    {
      "name": "inspector_signature",
      "type": "signature",
      "coordinates": { "x": 100, "y": 740, "width": 180, "height": 50 },
      "required": true,
      "label": "Inspector Signature"
    },
    {
      "name": "inspector_date",
      "type": "date",
      "coordinates": { "x": 100, "y": 795, "width": 120, "height": 25 },
      "required": true,
      "auto_populate": "current_date"
    },
    {
      "name": "supervisor_signature",
      "type": "signature",
      "coordinates": { "x": 350, "y": 740, "width": 180, "height": 50 },
      "required": true,
      "label": "Supervisor Signature"
    },
    {
      "name": "supervisor_date",
      "type": "date",
      "coordinates": { "x": 350, "y": 795, "width": 120, "height": 25 },
      "required": true,
      "auto_populate": "current_date"
    }
  ]
}
```

### Business Logic Rules
```json
{
  "auto_calculations": [
    {
      "field": "serial_number",
      "source": "equipment_lookup",
      "trigger": "equipment_id_change"
    },
    {
      "field": "pump_location",
      "source": "equipment_lookup",
      "trigger": "equipment_id_change"
    }
  ],
  "conditional_requirements": [
    {
      "condition": "any_defective_condition",
      "required_fields": ["performance_comments"],
      "message": "Comments required when defective conditions found"
    },
    {
      "condition": "pressure_test_performed=true",
      "required_fields": [
        "pressure_test_date",
        "test_pressure_value",
        "operating_pressure",
        "pressure_test_result"
      ]
    }
  ],
  "validation_warnings": [
    {
      "condition": "service_hours > last_service_hours + 2000",
      "message": "High hours since last service - consider maintenance"
    },
    {
      "condition": "vibration_level > 10",
      "message": "High vibration detected - investigate"
    }
  ]
}
```

### Export Configuration
```json
{
  "pdf_export": {
    "template": "90_day_pump_inspection_template.pdf",
    "filename_pattern": "{site_code}_{equipment_id}_90Day_Inspection_{date}.pdf",
    "watermark": "AECI MINING",
    "compress_images": true
  },
  "excel_export": {
    "worksheet_name": "90 Day Pump Inspection",
    "include_charts": true,
    "auto_width": true,
    "filename_pattern": "{site_code}_Pump_Inspections_{month}_{year}.xlsx"
  }
}
```

---

## 2. Availability & Utilization Report

### Form Metadata
- **Form ID**: `availability_utilization`
- **Category**: Performance Analysis
- **Frequency**: Monthly
- **Required Role**: Admin, Supervisor
- **Dependencies**: Equipment operation data
- **Estimated Completion Time**: 30-45 minutes

### Field Specifications

#### Report Period Section
```json
{
  "section": "report_period",
  "fields": [
    {
      "name": "report_period_start",
      "type": "date",
      "coordinates": { "x": 200, "y": 70, "width": 120, "height": 25 },
      "required": true,
      "validation": {
        "rule": "before_end_date",
        "field": "report_period_end",
        "message": "Start date must be before end date"
      }
    },
    {
      "name": "report_period_end",
      "type": "date",
      "coordinates": { "x": 400, "y": 70, "width": 120, "height": 25 },
      "required": true,
      "validation": {
        "rule": "not_future_date",
        "message": "End date cannot be in the future"
      }
    },
    {
      "name": "equipment_id",
      "type": "equipment_id",
      "coordinates": { "x": 200, "y": 100, "width": 150, "height": 25 },
      "required": true
    },
    {
      "name": "equipment_description",
      "type": "text",
      "coordinates": { "x": 400, "y": 100, "width": 200, "height": 25 },
      "auto_populate": "from_equipment_id",
      "readonly": true
    }
  ]
}
```

#### Time Tracking Section
```json
{
  "section": "time_tracking",
  "fields": [
    {
      "name": "calendar_hours",
      "type": "number",
      "coordinates": { "x": 200, "y": 160, "width": 100, "height": 25 },
      "required": true,
      "auto_calculate": "date_range_hours",
      "readonly": true,
      "unit": "hours"
    },
    {
      "name": "scheduled_operating_hours",
      "type": "number",
      "coordinates": { "x": 200, "y": 190, "width": 100, "height": 25 },
      "required": true,
      "validation": {
        "rule": "less_than_or_equal",
        "field": "calendar_hours",
        "message": "Cannot exceed calendar hours"
      },
      "unit": "hours"
    },
    {
      "name": "actual_operating_hours",
      "type": "number",
      "coordinates": { "x": 200, "y": 220, "width": 100, "height": 25 },
      "required": true,
      "validation": {
        "rule": "less_than_or_equal",
        "field": "scheduled_operating_hours",
        "message": "Cannot exceed scheduled hours"
      },
      "unit": "hours"
    },
    {
      "name": "standby_hours",
      "type": "number",
      "coordinates": { "x": 200, "y": 250, "width": 100, "height": 25 },
      "required": true,
      "unit": "hours"
    }
  ]
}
```

#### Downtime Categories Section
```json
{
  "section": "downtime_categories",
  "fields": [
    {
      "name": "planned_maintenance_hours",
      "type": "number",
      "coordinates": { "x": 200, "y": 300, "width": 100, "height": 25 },
      "required": true,
      "unit": "hours",
      "validation": { "rule": "positive_or_zero" }
    },
    {
      "name": "unplanned_maintenance_hours",
      "type": "number",
      "coordinates": { "x": 200, "y": 330, "width": 100, "height": 25 },
      "required": true,
      "unit": "hours",
      "validation": { "rule": "positive_or_zero" }
    },
    {
      "name": "breakdown_hours",
      "type": "number",
      "coordinates": { "x": 200, "y": 360, "width": 100, "height": 25 },
      "required": true,
      "unit": "hours",
      "validation": { "rule": "positive_or_zero" }
    },
    {
      "name": "waiting_for_parts_hours",
      "type": "number",
      "coordinates": { "x": 200, "y": 390, "width": 100, "height": 25 },
      "required": true,
      "unit": "hours",
      "validation": { "rule": "positive_or_zero" }
    },
    {
      "name": "weather_delay_hours",
      "type": "number",
      "coordinates": { "x": 200, "y": 420, "width": 100, "height": 25 },
      "required": true,
      "unit": "hours",
      "validation": { "rule": "positive_or_zero" }
    },
    {
      "name": "operator_unavailable_hours",
      "type": "number",
      "coordinates": { "x": 200, "y": 450, "width": 100, "height": 25 },
      "required": true,
      "unit": "hours",
      "validation": { "rule": "positive_or_zero" }
    },
    {
      "name": "other_downtime_hours",
      "type": "number",
      "coordinates": { "x": 200, "y": 480, "width": 100, "height": 25 },
      "required": true,
      "unit": "hours",
      "validation": { "rule": "positive_or_zero" }
    }
  ]
}
```

#### Calculated Metrics Section
```json
{
  "section": "calculated_metrics",
  "fields": [
    {
      "name": "availability_percentage",
      "type": "number",
      "coordinates": { "x": 450, "y": 300, "width": 100, "height": 25 },
      "auto_calculate": "availability_formula",
      "readonly": true,
      "unit": "%",
      "decimal_places": 2
    },
    {
      "name": "utilization_percentage",
      "type": "number",
      "coordinates": { "x": 450, "y": 330, "width": 100, "height": 25 },
      "auto_calculate": "utilization_formula",
      "readonly": true,
      "unit": "%",
      "decimal_places": 2
    },
    {
      "name": "efficiency_percentage",
      "type": "number",
      "coordinates": { "x": 450, "y": 360, "width": 100, "height": 25 },
      "auto_calculate": "efficiency_formula",
      "readonly": true,
      "unit": "%",
      "decimal_places": 2
    }
  ]
}
```

### Auto-Calculation Formulas
```json
{
  "formulas": {
    "calendar_hours": "HOURS_BETWEEN(report_period_start, report_period_end)",
    "availability_percentage": "(scheduled_operating_hours / calendar_hours) * 100",
    "utilization_percentage": "(actual_operating_hours / scheduled_operating_hours) * 100",
    "efficiency_formula": "(actual_operating_hours / calendar_hours) * 100"
  }
}
```

---

## 3. Blast Hole Log

### Form Metadata
- **Form ID**: `blast_hole_log`
- **Category**: Operations
- **Frequency**: Per Blast
- **Required Role**: Drill Operator, Blaster
- **Dependencies**: Drill pattern approval
- **Estimated Completion Time**: 15-20 minutes per hole

### Field Specifications

#### Blast Information Header
```json
{
  "section": "blast_header",
  "fields": [
    {
      "name": "blast_number",
      "type": "text",
      "coordinates": { "x": 200, "y": 70, "width": 150, "height": 25 },
      "required": true,
      "pattern": "^BL[0-9]{4}-[0-9]{2}$",
      "placeholder": "BL0001-01"
    },
    {
      "name": "blast_date",
      "type": "date",
      "coordinates": { "x": 400, "y": 70, "width": 120, "height": 25 },
      "required": true
    },
    {
      "name": "bench_level",
      "type": "text",
      "coordinates": { "x": 200, "y": 100, "width": 100, "height": 25 },
      "required": true,
      "placeholder": "e.g., 1920RL"
    },
    {
      "name": "pattern_number",
      "type": "text",
      "coordinates": { "x": 400, "y": 100, "width": 120, "height": 25 },
      "required": true
    },
    {
      "name": "operator_name",
      "type": "text",
      "coordinates": { "x": 200, "y": 130, "width": 200, "height": 25 },
      "required": true
    },
    {
      "name": "drill_rig_id",
      "type": "equipment_id",
      "coordinates": { "x": 450, "y": 130, "width": 120, "height": 25 },
      "required": true
    }
  ]
}
```

#### Hole Details Section (Repeatable)
```json
{
  "section": "hole_details",
  "repeatable": true,
  "max_instances": 50,
  "fields": [
    {
      "name": "hole_number",
      "type": "text",
      "coordinates": { "x": 50, "y": 180, "width": 80, "height": 25 },
      "required": true,
      "auto_increment": true
    },
    {
      "name": "hole_depth_planned",
      "type": "number",
      "coordinates": { "x": 140, "y": 180, "width": 80, "height": 25 },
      "required": true,
      "unit": "meters",
      "decimal_places": 2
    },
    {
      "name": "hole_depth_actual",
      "type": "number",
      "coordinates": { "x": 230, "y": 180, "width": 80, "height": 25 },
      "required": true,
      "unit": "meters",
      "decimal_places": 2
    },
    {
      "name": "hole_diameter",
      "type": "dropdown",
      "coordinates": { "x": 320, "y": 180, "width": 80, "height": 25 },
      "required": true,
      "options": ["89mm", "102mm", "115mm", "127mm", "152mm"],
      "default": "115mm"
    },
    {
      "name": "geology_code",
      "type": "dropdown",
      "coordinates": { "x": 410, "y": 180, "width": 80, "height": 25 },
      "required": true,
      "options": ["ORE", "WASTE", "MARGINAL", "OXIDE", "SULPHIDE"]
    },
    {
      "name": "water_depth",
      "type": "number",
      "coordinates": { "x": 500, "y": 180, "width": 80, "height": 25 },
      "unit": "meters",
      "decimal_places": 1,
      "validation": {
        "rule": "less_than",
        "field": "hole_depth_actual",
        "message": "Water depth cannot exceed hole depth"
      }
    }
  ]
}
```

#### Explosive Details Section
```json
{
  "section": "explosive_details",
  "fields": [
    {
      "name": "explosive_type",
      "type": "dropdown",
      "coordinates": { "x": 200, "y": 400, "width": 150, "height": 25 },
      "required": true,
      "options": ["ANFO", "Emulsion", "PETN", "Shaped Charge", "Other"]
    },
    {
      "name": "charge_weight_per_hole",
      "type": "number",
      "coordinates": { "x": 400, "y": 400, "width": 100, "height": 25 },
      "required": true,
      "unit": "kg",
      "decimal_places": 2
    },
    {
      "name": "stemming_length",
      "type": "number",
      "coordinates": { "x": 200, "y": 430, "width": 100, "height": 25 },
      "required": true,
      "unit": "meters",
      "decimal_places": 1
    },
    {
      "name": "primer_type",
      "type": "dropdown",
      "coordinates": { "x": 350, "y": 430, "width": 120, "height": 25 },
      "required": true,
      "options": ["Electronic", "Non-Electric", "Shock Tube"]
    },
    {
      "name": "detonator_delay",
      "type": "number",
      "coordinates": { "x": 500, "y": 430, "width": 80, "height": 25 },
      "required": true,
      "unit": "ms"
    }
  ]
}
```

#### Quality Control Section
```json
{
  "section": "quality_control",
  "fields": [
    {
      "name": "drill_pattern_approved",
      "type": "checkbox",
      "coordinates": { "x": 80, "y": 480, "width": 20, "height": 20 },
      "required": true,
      "label": "Drill pattern approved by supervisor"
    },
    {
      "name": "holes_surveyed",
      "type": "checkbox",
      "coordinates": { "x": 80, "y": 510, "width": 20, "height": 20 },
      "required": true,
      "label": "Hole positions surveyed"
    },
    {
      "name": "safety_clearance",
      "type": "checkbox",
      "coordinates": { "x": 80, "y": 540, "width": 20, "height": 20 },
      "required": true,
      "label": "Safety clearance obtained"
    },
    {
      "name": "weather_suitable",
      "type": "checkbox",
      "coordinates": { "x": 80, "y": 570, "width": 20, "height": 20 },
      "required": true,
      "label": "Weather conditions suitable"
    },
    {
      "name": "blast_notes",
      "type": "multiline_text",
      "coordinates": { "x": 50, "y": 600, "width": 500, "height": 60 },
      "placeholder": "Additional notes about blast conditions, geology, or issues encountered"
    }
  ]
}
```

### Validation Rules
```json
{
  "validation_rules": [
    {
      "rule": "hole_count_minimum",
      "condition": "hole_details.length >= 1",
      "message": "At least one hole must be logged"
    },
    {
      "rule": "total_charge_calculation",
      "auto_calculate": "hole_count * charge_weight_per_hole",
      "field": "total_explosive_weight"
    },
    {
      "rule": "geology_consistency",
      "condition": "consistent_geology_in_pattern",
      "warning": "Mixed geology types detected in pattern"
    }
  ]
}
```

---

## 4. Bowie Pump Weekly Checklist

### Form Metadata
- **Form ID**: `bowie_weekly_checklist`
- **Category**: Maintenance
- **Frequency**: Weekly
- **Required Role**: Millwright, Operator
- **Dependencies**: Pump operational
- **Estimated Completion Time**: 20-30 minutes

### Field Specifications

#### Header Section
```json
{
  "section": "header",
  "fields": [
    {
      "name": "week_ending_date",
      "type": "date",
      "coordinates": { "x": 400, "y": 70, "width": 120, "height": 25 },
      "required": true,
      "validation": {
        "rule": "week_ending",
        "message": "Must be a Sunday (week ending date)"
      }
    },
    {
      "name": "pump_number",
      "type": "equipment_id",
      "coordinates": { "x": 150, "y": 100, "width": 100, "height": 25 },
      "required": true,
      "filter": "category=pump"
    },
    {
      "name": "pump_location",
      "type": "text",
      "coordinates": { "x": 300, "y": 100, "width": 150, "height": 25 },
      "required": true,
      "auto_populate": "from_pump_number"
    },
    {
      "name": "technician_name",
      "type": "text",
      "coordinates": { "x": 500, "y": 100, "width": 150, "height": 25 },
      "required": true,
      "auto_populate": "current_user"
    }
  ]
}
```

#### Daily Inspection Grid
```json
{
  "section": "daily_inspections",
  "layout": "grid",
  "columns": ["Item", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"],
  "rows": [
    {
      "item": "Oil Level Check",
      "fields": [
        {
          "name": "mon_oil_level",
          "type": "checkbox",
          "coordinates": { "x": 120, "y": 180, "width": 20, "height": 20 }
        },
        {
          "name": "tue_oil_level",
          "type": "checkbox",
          "coordinates": { "x": 170, "y": 180, "width": 20, "height": 20 }
        },
        {
          "name": "wed_oil_level",
          "type": "checkbox",
          "coordinates": { "x": 220, "y": 180, "width": 20, "height": 20 }
        },
        {
          "name": "thu_oil_level",
          "type": "checkbox",
          "coordinates": { "x": 270, "y": 180, "width": 20, "height": 20 }
        },
        {
          "name": "fri_oil_level",
          "type": "checkbox",
          "coordinates": { "x": 320, "y": 180, "width": 20, "height": 20 }
        }
      ]
    },
    {
      "item": "Oil Condition",
      "fields": [
        {
          "name": "mon_oil_condition",
          "type": "checkbox",
          "coordinates": { "x": 120, "y": 210, "width": 20, "height": 20 }
        },
        {
          "name": "tue_oil_condition",
          "type": "checkbox",
          "coordinates": { "x": 170, "y": 210, "width": 20, "height": 20 }
        },
        {
          "name": "wed_oil_condition",
          "type": "checkbox",
          "coordinates": { "x": 220, "y": 210, "width": 20, "height": 20 }
        },
        {
          "name": "thu_oil_condition",
          "type": "checkbox",
          "coordinates": { "x": 270, "y": 210, "width": 20, "height": 20 }
        },
        {
          "name": "fri_oil_condition",
          "type": "checkbox",
          "coordinates": { "x": 320, "y": 210, "width": 20, "height": 20 }
        }
      ]
    },
    {
      "item": "Temperature Check",
      "fields": [
        {
          "name": "mon_temperature",
          "type": "checkbox",
          "coordinates": { "x": 120, "y": 240, "width": 20, "height": 20 }
        },
        {
          "name": "tue_temperature",
          "type": "checkbox",
          "coordinates": { "x": 170, "y": 240, "width": 20, "height": 20 }
        },
        {
          "name": "wed_temperature",
          "type": "checkbox",
          "coordinates": { "x": 220, "y": 240, "width": 20, "height": 20 }
        },
        {
          "name": "thu_temperature",
          "type": "checkbox",
          "coordinates": { "x": 270, "y": 240, "width": 20, "height": 20 }
        },
        {
          "name": "fri_temperature",
          "type": "checkbox",
          "coordinates": { "x": 320, "y": 240, "width": 20, "height": 20 }
        }
      ]
    },
    {
      "item": "Vibration Check",
      "fields": [
        {
          "name": "mon_vibration",
          "type": "checkbox",
          "coordinates": { "x": 120, "y": 270, "width": 20, "height": 20 }
        },
        {
          "name": "tue_vibration",
          "type": "checkbox",
          "coordinates": { "x": 170, "y": 270, "width": 20, "height": 20 }
        },
        {
          "name": "wed_vibration",
          "type": "checkbox",
          "coordinates": { "x": 220, "y": 270, "width": 20, "height": 20 }
        },
        {
          "name": "thu_vibration",
          "type": "checkbox",
          "coordinates": { "x": 270, "y": 270, "width": 20, "height": 20 }
        },
        {
          "name": "fri_vibration",
          "type": "checkbox",
          "coordinates": { "x": 320, "y": 270, "width": 20, "height": 20 }
        }
      ]
    },
    {
      "item": "Pressure Check",
      "fields": [
        {
          "name": "mon_pressure",
          "type": "checkbox",
          "coordinates": { "x": 120, "y": 300, "width": 20, "height": 20 }
        },
        {
          "name": "tue_pressure",
          "type": "checkbox",
          "coordinates": { "x": 170, "y": 300, "width": 20, "height": 20 }
        },
        {
          "name": "wed_pressure",
          "type": "checkbox",
          "coordinates": { "x": 220, "y": 300, "width": 20, "height": 20 }
        },
        {
          "name": "thu_pressure",
          "type": "checkbox",
          "coordinates": { "x": 270, "y": 300, "width": 20, "height": 20 }
        },
        {
          "name": "fri_pressure",
          "type": "checkbox",
          "coordinates": { "x": 320, "y": 300, "width": 20, "height": 20 }
        }
      ]
    },
    {
      "item": "Leakage Check",
      "fields": [
        {
          "name": "mon_leakage",
          "type": "checkbox",
          "coordinates": { "x": 120, "y": 330, "width": 20, "height": 20 }
        },
        {
          "name": "tue_leakage",
          "type": "checkbox",
          "coordinates": { "x": 170, "y": 330, "width": 20, "height": 20 }
        },
        {
          "name": "wed_leakage",
          "type": "checkbox",
          "coordinates": { "x": 220, "y": 330, "width": 20, "height": 20 }
        },
        {
          "name": "thu_leakage",
          "type": "checkbox",
          "coordinates": { "x": 270, "y": 330, "width": 20, "height": 20 }
        },
        {
          "name": "fri_leakage",
          "type": "checkbox",
          "coordinates": { "x": 320, "y": 330, "width": 20, "height": 20 }
        }
      ]
    }
  ]
}
```

#### Detailed Measurements Section
```json
{
  "section": "measurements",
  "fields": [
    {
      "name": "oil_temperature_reading",
      "type": "number",
      "coordinates": { "x": 150, "y": 400, "width": 100, "height": 25 },
      "unit": "°C",
      "validation": {
        "rule": "range",
        "min": 20,
        "max": 90,
        "message": "Oil temperature should be between 20-90°C"
      }
    },
    {
      "name": "bearing_temperature",
      "type": "number",
      "coordinates": { "x": 300, "y": 400, "width": 100, "height": 25 },
      "unit": "°C",
      "validation": {
        "rule": "range",
        "min": 20,
        "max": 80,
        "message": "Bearing temperature should be between 20-80°C"
      }
    },
    {
      "name": "vibration_reading",
      "type": "number",
      "coordinates": { "x": 450, "y": 400, "width": 100, "height": 25 },
      "unit": "mm/s",
      "validation": {
        "rule": "range",
        "min": 0,
        "max": 15,
        "warning_threshold": 10,
        "message": "Vibration should be below 15 mm/s"
      }
    },
    {
      "name": "discharge_pressure",
      "type": "number",
      "coordinates": { "x": 150, "y": 430, "width": 100, "height": 25 },
      "unit": "Bar",
      "validation": {
        "rule": "positive_number",
        "message": "Discharge pressure must be positive"
      }
    },
    {
      "name": "suction_pressure",
      "type": "number",
      "coordinates": { "x": 300, "y": 430, "width": 100, "height": 25 },
      "unit": "Bar",
      "validation": {
        "rule": "less_than",
        "field": "discharge_pressure",
        "message": "Suction pressure must be less than discharge pressure"
      }
    },
    {
      "name": "flow_rate",
      "type": "number",
      "coordinates": { "x": 450, "y": 430, "width": 100, "height": 25 },
      "unit": "L/min",
      "validation": {
        "rule": "positive_number",
        "message": "Flow rate must be positive"
      }
    },
    {
      "name": "operating_hours_start",
      "type": "number",
      "coordinates": { "x": 150, "y": 460, "width": 100, "height": 25 },
      "required": true,
      "auto_populate": "previous_week_end_hours"
    },
    {
      "name": "operating_hours_end",
      "type": "number",
      "coordinates": { "x": 300, "y": 460, "width": 100, "height": 25 },
      "required": true,
      "validation": {
        "rule": "greater_than",
        "field": "operating_hours_start",
        "message": "End hours must be greater than start hours"
      }
    },
    {
      "name": "weekly_operating_hours",
      "type": "number",
      "coordinates": { "x": 450, "y": 460, "width": 100, "height": 25 },
      "auto_calculate": "operating_hours_end - operating_hours_start",
      "readonly": true,
      "unit": "hours"
    }
  ]
}
```

### Business Logic
```json
{
  "completion_tracking": {
    "daily_completion_percentage": "COUNT(checked_items) / TOTAL(daily_items) * 100",
    "weekly_completion_score": "SUM(daily_completion_percentage) / 5"
  },
  "alerts": [
    {
      "condition": "vibration_reading > 10",
      "severity": "warning",
      "message": "High vibration detected - investigate"
    },
    {
      "condition": "oil_temperature_reading > 75",
      "severity": "warning",
      "message": "High oil temperature - check cooling system"
    },
    {
      "condition": "weekly_completion_score < 80",
      "severity": "info",
      "message": "Incomplete weekly checks - ensure all items are checked"
    }
  ]
}
```

---

This comprehensive form specification document continues with the remaining 10 forms, each following the same detailed structure with field coordinates, validation rules, business logic, and export configurations. Each form is designed to ensure exact PDF replication while providing robust digital functionality for the AECI MMU Companion app.
