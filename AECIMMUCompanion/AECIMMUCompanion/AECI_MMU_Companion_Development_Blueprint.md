# AECI MMU Companion App: Complete Development Blueprint

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture & Structure](#architecture--structure)
3. [PDF Form Pages Implementation](#pdf-form-pages-implementation)
4. [Dashboard Implementation](#dashboard-implementation)
5. [Authentication & Connectivity](#authentication--connectivity)
6. [Export Engine Strategy](#export-engine-strategy)
7. [Branding & UI Guidelines](#branding--ui-guidelines)
8. [Validation & Error Prevention](#validation--error-prevention)
9. [Pre-Launch QA Strategy](#pre-launch-qa-strategy)
10. [Implementation Roadmap](#implementation-roadmap)

---

## Project Overview

AECI MMU Companion is a secure, **cloud-connected**, **offline-capable** Android application designed to digitize PDF-based mining reports. It enables users to fill and export structured reports, filtered by site and role. The app ensures online-first functionality with cloud servers for credential verification, data syncing, and secure remote access.

### Key Requirements
- Exact PDF layout replication in digital forms
- Offline-first data storage with cloud synchronization
- Role-based access control (Admin, Millwright, Equipment Operator)
- Site-specific filtering and equipment management
- Export to both PDF and Excel formats
- Digital signature capture
- AECI Mining branding (Red #C8102E and White)

---

## Architecture & Structure

### Clean Architecture Implementation

```
app/
├── data/                     # Data layer
│   ├── model/               # Entity classes for all forms
│   │   ├── PumpInspection.kt
│   │   ├── MaintenanceRecord.kt
│   │   ├── ProductionLog.kt
│   │   └── ...
│   ├── database/            # Room database setup
│   │   ├── AppDatabase.kt
│   │   ├── dao/            # Data Access Objects
│   │   └── entities/       # Database entities
│   └── repository/          # Repository pattern implementation
│       ├── FormRepository.kt
│       ├── AuthRepository.kt
│       └── SyncRepository.kt
├── domain/                   # Business logic layer
│   ├── usecase/            # Use cases for each operation
│   │   ├── SaveFormUseCase.kt
│   │   ├── ExportPdfUseCase.kt
│   │   └── SyncDataUseCase.kt
│   └── model/              # Domain models
├── presentation/             # UI layer
│   ├── dashboard/           # Dashboard screens
│   │   ├── AdminDashboard.kt
│   │   ├── MillwrightDashboard.kt
│   │   └── EquipmentDashboard.kt
│   ├── forms/               # All PDF-based forms
│   │   ├── pump/           # Pump-related forms
│   │   ├── maintenance/    # Maintenance forms
│   │   ├── production/     # Production logs
│   │   └── inspection/     # Inspection checklists
│   ├── auth/                # Authentication
│   ├── export/              # Export functionality
│   └── utils/               # Shared UI components
├── core/                     # Core functionality
│   ├── di/                 # Dependency injection
│   ├── network/            # Network configuration
│   ├── constants/          # App constants
│   └── utils/              # Utility classes
└── res/                      # Resources
    ├── drawable/           # Icons and graphics
    ├── layout/             # XML layouts (if needed)
    ├── values/             # Colors, strings, themes
    └── assets/             # PDF templates
        └── pdf_templates/  # Original PDF files
```

---

## PDF Form Pages Implementation

### Coordinate Mapping System

Each PDF form requires precise coordinate mapping for field overlay. The coordinate system uses the following structure:

```kotlin
data class FieldCoordinate(
    val fieldName: String,
    val x: Float,          // X position from left edge
    val y: Float,          // Y position from top edge
    val width: Float,      // Field width
    val height: Float,     // Field height
    val fieldType: FieldType, // TEXT, CHECKBOX, SIGNATURE, DATE, etc.
    val validation: ValidationRule? = null
)

enum class FieldType {
    TEXT, NUMBER, DATE, TIME, CHECKBOX, RADIO, SIGNATURE, DROPDOWN, MULTILINE_TEXT
}
```

### 1. 90 Day Pump System Inspection Checklist

**File**: `90 DAY PUMP SYSTEM INSPECTION CHECKLIST.pdf`

**Description**: Comprehensive pump inspection form with multiple sections for visual checks, pressure tests, and component assessments.

**Coordinate Map**:
```kotlin
val pumpInspectionCoordinates = listOf(
    // Header Information
    FieldCoordinate("date", 450f, 85f, 120f, 25f, FieldType.DATE),
    FieldCoordinate("inspector_name", 150f, 110f, 200f, 25f, FieldType.TEXT),
    FieldCoordinate("equipment_id", 450f, 110f, 150f, 25f, FieldType.TEXT),
    FieldCoordinate("serial_number", 150f, 135f, 200f, 25f, FieldType.TEXT),
    FieldCoordinate("location", 450f, 135f, 150f, 25f, FieldType.TEXT),
    
    // Visual Inspection Section (Y: 180-350)
    FieldCoordinate("pump_housing_check", 50f, 200f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("pump_housing_comments", 350f, 200f, 200f, 25f, FieldType.TEXT),
    FieldCoordinate("coupling_check", 50f, 230f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("coupling_comments", 350f, 230f, 200f, 25f, FieldType.TEXT),
    FieldCoordinate("motor_check", 50f, 260f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("motor_comments", 350f, 260f, 200f, 25f, FieldType.TEXT),
    FieldCoordinate("piping_check", 50f, 290f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("piping_comments", 350f, 290f, 200f, 25f, FieldType.TEXT),
    
    // Pressure Test Section (Y: 380-500)
    FieldCoordinate("pressure_test_date", 150f, 400f, 120f, 25f, FieldType.DATE),
    FieldCoordinate("test_pressure", 350f, 400f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("operating_pressure", 150f, 430f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("pressure_drop", 350f, 430f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("pressure_test_result", 150f, 460f, 20f, 20f, FieldType.CHECKBOX),
    
    // Final Signatures
    FieldCoordinate("inspector_signature", 100f, 650f, 200f, 50f, FieldType.SIGNATURE),
    FieldCoordinate("supervisor_signature", 400f, 650f, 200f, 50f, FieldType.SIGNATURE)
)
```

**UI Implementation Notes**:
- Section-based layout with collapsible headers
- Color-coded status indicators for each check item
- Photo capture capability for equipment issues
- Real-time validation with red borders for required fields

### 2. Bowie Pump Weekly Checklist

**File**: `Bowie Pump Weekly check list.pdf`

**Description**: Weekly maintenance checklist for Bowie pumps with routine inspection items.

**Coordinate Map**:
```kotlin
val bowieWeeklyCoordinates = listOf(
    // Header
    FieldCoordinate("week_ending", 400f, 70f, 120f, 25f, FieldType.DATE),
    FieldCoordinate("pump_number", 150f, 100f, 100f, 25f, FieldType.TEXT),
    FieldCoordinate("technician", 400f, 100f, 150f, 25f, FieldType.TEXT),
    
    // Daily Checks (Monday-Friday grid)
    // Monday Column (X: 120f)
    FieldCoordinate("mon_oil_level", 120f, 180f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("mon_temperature", 120f, 210f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("mon_vibration", 120f, 240f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("mon_pressure", 120f, 270f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("mon_leaks", 120f, 300f, 20f, 20f, FieldType.CHECKBOX),
    
    // Tuesday Column (X: 170f)
    FieldCoordinate("tue_oil_level", 170f, 180f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("tue_temperature", 170f, 210f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("tue_vibration", 170f, 240f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("tue_pressure", 170f, 270f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("tue_leaks", 170f, 300f, 20f, 20f, FieldType.CHECKBOX),
    
    // Continue for Wed, Thu, Fri...
    // Notes Section
    FieldCoordinate("weekly_notes", 50f, 400f, 500f, 100f, FieldType.MULTILINE_TEXT),
    
    // Completion
    FieldCoordinate("completed_by", 150f, 550f, 200f, 25f, FieldType.TEXT),
    FieldCoordinate("completion_date", 400f, 550f, 120f, 25f, FieldType.DATE),
    FieldCoordinate("technician_signature", 150f, 600f, 200f, 50f, FieldType.SIGNATURE)
)
```

**UI Implementation Notes**:
- Grid layout for daily checks
- Swipe gestures for quick navigation between days
- Auto-save functionality for each checkbox
- Weekly summary view with completion percentage

### 3. MMU Production Daily Log

**File**: `mmu production daily log.pdf`

**Description**: Daily production tracking with equipment hours, output metrics, and operational notes.

**Coordinate Map**:
```kotlin
val productionLogCoordinates = listOf(
    // Header Information
    FieldCoordinate("date", 450f, 60f, 120f, 25f, FieldType.DATE),
    FieldCoordinate("shift", 200f, 60f, 100f, 25f, FieldType.DROPDOWN), // Day/Night
    FieldCoordinate("operator", 450f, 90f, 150f, 25f, FieldType.TEXT),
    FieldCoordinate("site_location", 200f, 90f, 150f, 25f, FieldType.TEXT),
    
    // Equipment Hours Section
    FieldCoordinate("mmu_start_hours", 150f, 150f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("mmu_end_hours", 300f, 150f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("mmu_total_hours", 450f, 150f, 100f, 25f, FieldType.NUMBER),
    
    // Production Metrics
    FieldCoordinate("holes_drilled", 150f, 200f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("total_meters", 300f, 200f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("average_depth", 450f, 200f, 100f, 25f, FieldType.NUMBER),
    
    // Downtime Tracking
    FieldCoordinate("maintenance_downtime", 150f, 250f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("weather_downtime", 300f, 250f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("other_downtime", 450f, 250f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("downtime_reason", 50f, 280f, 500f, 25f, FieldType.TEXT),
    
    // Consumables Used
    FieldCoordinate("drill_bits_used", 150f, 330f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("fuel_consumption", 300f, 330f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("water_usage", 450f, 330f, 100f, 25f, FieldType.NUMBER),
    
    // Safety & Environmental
    FieldCoordinate("safety_incidents", 150f, 380f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("environmental_issues", 300f, 380f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("incident_description", 50f, 410f, 500f, 50f, FieldType.MULTILINE_TEXT),
    
    // Weather Conditions
    FieldCoordinate("weather_condition", 150f, 480f, 150f, 25f, FieldType.DROPDOWN),
    FieldCoordinate("temperature", 350f, 480f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("wind_speed", 500f, 480f, 80f, 25f, FieldType.NUMBER),
    
    // Comments and Notes
    FieldCoordinate("daily_notes", 50f, 530f, 500f, 80f, FieldType.MULTILINE_TEXT),
    
    // Signatures
    FieldCoordinate("operator_signature", 100f, 650f, 180f, 50f, FieldType.SIGNATURE),
    FieldCoordinate("supervisor_signature", 320f, 650f, 180f, 50f, FieldType.SIGNATURE),
    FieldCoordinate("date_signed", 520f, 680f, 100f, 25f, FieldType.DATE)
)
```

**UI Implementation Notes**:
- Auto-calculation of total hours and production rates
- Integration with device GPS for location verification
- Photo capture for equipment conditions
- Offline capability with sync indicators

### 4. MMU Chassis Maintenance Record

**File**: `MMU CHASSIS MAINTENANCE RECORD.pdf`

**Description**: Comprehensive maintenance tracking for MMU chassis components and systems.

**Coordinate Map**:
```kotlin
val chassisMaintenanceCoordinates = listOf(
    // Equipment Identification
    FieldCoordinate("equipment_number", 200f, 80f, 150f, 25f, FieldType.TEXT),
    FieldCoordinate("serial_number", 450f, 80f, 150f, 25f, FieldType.TEXT),
    FieldCoordinate("maintenance_date", 200f, 110f, 120f, 25f, FieldType.DATE),
    FieldCoordinate("next_service_date", 450f, 110f, 120f, 25f, FieldType.DATE),
    FieldCoordinate("technician", 200f, 140f, 200f, 25f, FieldType.TEXT),
    FieldCoordinate("service_hours", 450f, 140f, 100f, 25f, FieldType.NUMBER),
    
    // Engine System Checks
    FieldCoordinate("engine_oil_level", 80f, 200f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("engine_oil_condition", 120f, 200f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("engine_oil_changed", 160f, 200f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("engine_notes", 350f, 200f, 200f, 25f, FieldType.TEXT),
    
    FieldCoordinate("coolant_level", 80f, 230f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("coolant_condition", 120f, 230f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("coolant_changed", 160f, 230f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("coolant_notes", 350f, 230f, 200f, 25f, FieldType.TEXT),
    
    FieldCoordinate("air_filter_checked", 80f, 260f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("air_filter_cleaned", 120f, 260f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("air_filter_replaced", 160f, 260f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("air_filter_notes", 350f, 260f, 200f, 25f, FieldType.TEXT),
    
    // Hydraulic System
    FieldCoordinate("hydraulic_fluid_level", 80f, 320f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("hydraulic_fluid_condition", 120f, 320f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("hydraulic_fluid_changed", 160f, 320f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("hydraulic_notes", 350f, 320f, 200f, 25f, FieldType.TEXT),
    
    FieldCoordinate("hydraulic_hoses_checked", 80f, 350f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("hydraulic_fittings_checked", 120f, 350f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("hydraulic_leaks_found", 160f, 350f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("hydraulic_system_notes", 350f, 350f, 200f, 25f, FieldType.TEXT),
    
    // Electrical System
    FieldCoordinate("battery_condition", 80f, 410f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("wiring_inspection", 120f, 410f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("connections_tight", 160f, 410f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("electrical_notes", 350f, 410f, 200f, 25f, FieldType.TEXT),
    
    // Safety Systems
    FieldCoordinate("horn_tested", 80f, 470f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("lights_tested", 120f, 470f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("emergency_stops_tested", 160f, 470f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("safety_notes", 350f, 470f, 200f, 25f, FieldType.TEXT),
    
    // Parts Replaced
    FieldCoordinate("parts_replaced", 50f, 530f, 500f, 60f, FieldType.MULTILINE_TEXT),
    
    // Service Recommendations
    FieldCoordinate("recommendations", 50f, 610f, 500f, 60f, FieldType.MULTILINE_TEXT),
    
    // Completion
    FieldCoordinate("maintenance_complete", 80f, 690f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("next_service_hours", 200f, 690f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("technician_signature", 100f, 730f, 200f, 50f, FieldType.SIGNATURE),
    FieldCoordinate("supervisor_signature", 350f, 730f, 200f, 50f, FieldType.SIGNATURE)
)
```

**UI Implementation Notes**:
- Component-based sections with progress indicators
- Parts inventory integration for replacement tracking
- Service interval calculations and reminders
- Photo documentation for component conditions

### 5. Fire Extinguisher Inspection Checklist

**File**: `FIRE EXTINGUISHER INSPECTION CHECKLIST.pdf`

**Description**: Monthly fire safety equipment inspection form with compliance tracking.

**Coordinate Map**:
```kotlin
val fireExtinguisherCoordinates = listOf(
    // Header Information
    FieldCoordinate("inspection_date", 400f, 70f, 120f, 25f, FieldType.DATE),
    FieldCoordinate("inspector_name", 150f, 100f, 200f, 25f, FieldType.TEXT),
    FieldCoordinate("location", 400f, 100f, 150f, 25f, FieldType.TEXT),
    
    // Extinguisher Details
    FieldCoordinate("extinguisher_id", 150f, 140f, 100f, 25f, FieldType.TEXT),
    FieldCoordinate("extinguisher_type", 300f, 140f, 100f, 25f, FieldType.DROPDOWN),
    FieldCoordinate("manufacture_date", 450f, 140f, 120f, 25f, FieldType.DATE),
    FieldCoordinate("last_service_date", 150f, 170f, 120f, 25f, FieldType.DATE),
    FieldCoordinate("next_service_date", 300f, 170f, 120f, 25f, FieldType.DATE),
    
    // Visual Inspection Items
    FieldCoordinate("accessible_unobstructed", 80f, 220f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("pin_seal_intact", 80f, 250f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("pressure_gauge_normal", 80f, 280f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("hose_nozzle_undamaged", 80f, 310f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("instructions_legible", 80f, 340f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("no_physical_damage", 80f, 370f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("mounting_secure", 80f, 400f, 20f, 20f, FieldType.CHECKBOX),
    
    // Deficiency Notes
    FieldCoordinate("deficiency_found", 80f, 450f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("deficiency_description", 150f, 450f, 400f, 25f, FieldType.TEXT),
    FieldCoordinate("corrective_action", 50f, 490f, 500f, 50f, FieldType.MULTILINE_TEXT),
    
    // Service Required
    FieldCoordinate("recharge_required", 80f, 560f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("hydrostatic_test_due", 250f, 560f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("replacement_required", 420f, 560f, 20f, 20f, FieldType.CHECKBOX),
    
    // Completion
    FieldCoordinate("inspection_satisfactory", 80f, 610f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("follow_up_required", 300f, 610f, 20f, 20f, FieldType.CHECKBOX),
    FieldCoordinate("follow_up_date", 450f, 610f, 120f, 25f, FieldType.DATE),
    
    FieldCoordinate("inspector_signature", 100f, 660f, 200f, 50f, FieldType.SIGNATURE),
    FieldCoordinate("supervisor_signature", 350f, 660f, 200f, 50f, FieldType.SIGNATURE)
)
```

### 6. Availability & Utilization Report

**File**: `Availabilty & Utilization.pdf`

**Description**: Equipment availability and utilization tracking with performance metrics.

**Coordinate Map**:
```kotlin
val availabilityUtilizationCoordinates = listOf(
    // Report Period
    FieldCoordinate("report_period_start", 200f, 70f, 120f, 25f, FieldType.DATE),
    FieldCoordinate("report_period_end", 400f, 70f, 120f, 25f, FieldType.DATE),
    FieldCoordinate("equipment_id", 200f, 100f, 150f, 25f, FieldType.TEXT),
    FieldCoordinate("equipment_description", 400f, 100f, 200f, 25f, FieldType.TEXT),
    
    // Time Tracking (Hours)
    FieldCoordinate("calendar_hours", 200f, 160f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("scheduled_operating_hours", 200f, 190f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("actual_operating_hours", 200f, 220f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("standby_hours", 200f, 250f, 100f, 25f, FieldType.NUMBER),
    
    // Downtime Categories
    FieldCoordinate("planned_maintenance_hours", 200f, 300f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("unplanned_maintenance_hours", 200f, 330f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("breakdown_hours", 200f, 360f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("waiting_for_parts_hours", 200f, 390f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("weather_delay_hours", 200f, 420f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("operator_unavailable_hours", 200f, 450f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("other_downtime_hours", 200f, 480f, 100f, 25f, FieldType.NUMBER),
    
    // Calculated Metrics (Auto-calculated)
    FieldCoordinate("availability_percentage", 450f, 300f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("utilization_percentage", 450f, 330f, 100f, 25f, FieldType.NUMBER),
    FieldCoordinate("efficiency_percentage", 450f, 360f, 100f, 25f, FieldType.NUMBER),
    
    // Comments
    FieldCoordinate("availability_comments", 50f, 530f, 500f, 60f, FieldType.MULTILINE_TEXT),
    
    // Prepared By
    FieldCoordinate("prepared_by", 150f, 620f, 200f, 25f, FieldType.TEXT),
    FieldCoordinate("preparation_date", 400f, 620f, 120f, 25f, FieldType.DATE),
    FieldCoordinate("preparer_signature", 150f, 660f, 200f, 50f, FieldType.SIGNATURE)
)
```

### 7. Additional Forms Implementation

#### Blast Hole Log
- Geological data recording
- Hole diameter and depth tracking
- Explosive charge calculations
- Safety compliance checkpoints

#### Job Card
- Work order management
- Task assignment and completion
- Resource allocation tracking
- Time and material recording

#### MMU Handover Certificate
- Equipment transfer documentation
- Condition assessment
- Outstanding issues tracking
- Responsibility transfer

#### Monthly Process Maintenance Record
- Systematic maintenance scheduling
- Component lifecycle tracking
- Performance trend analysis
- Compliance documentation

#### On Bench MMU Inspection
- Workshop inspection procedures
- Component disassembly records
- Repair and replacement documentation
- Quality assurance checkpoints

#### PC Pump High/Low Pressure Trip Test
- Pressure safety system testing
- Trip point verification
- Calibration records
- Safety compliance documentation

#### Pre-Task Safety Assessment
- Risk identification and mitigation
- Safety equipment verification
- Personnel competency checks
- Work permit authorization

#### MMU Quality Report
- Quality metrics tracking
- Performance benchmarking
- Improvement recommendations
- Customer satisfaction measures

---

## Dashboard Implementation

### Role-Based Dashboard System

#### Admin Dashboard
**Purpose**: System administration and oversight

**Features**:
- User Management (Add/Edit/Delete users, Role assignment)
- Equipment Management (Add/Edit equipment, Site assignment)
- Report Monitoring (View all submitted reports, Status tracking)
- Analytics Dashboard (KPI widgets, Trend analysis)
- Task Assignment (Create/assign tasks, Set due dates)

**Layout Structure**:
```
TopAppBar: AECI Logo | Notifications | User Profile
BottomNavigationBar: Users | Equipment | Reports | Analytics | Tasks

Main Content Area:
- Summary Cards (Total Users, Active Equipment, Pending Reports)
- Quick Actions (Add User, Add Equipment, Generate Report)
- Recent Activity Feed
- System Status Indicators
```

#### Millwright Dashboard
**Purpose**: Site-specific equipment management for maintenance technicians

**Features**:
- Equipment List (Site-filtered, Status indicators, Quick actions)
- Maintenance Calendar (Scheduled tasks, Due dates, Overdue items)
- Work Orders (Assigned tasks, Progress tracking, Completion status)
- Attendance Tracking (Clock in/out, Timesheet generation)
- Reports Access (Create new, View history, Export options)

**Layout Structure**:
```
TopAppBar: Site Name | Sync Status | Profile
FloatingActionButton: Clock In/Out

Main Content:
- Site Equipment Grid/List
- Today's Tasks Section
- Quick Report Access
- Maintenance Alerts
```

#### Equipment Dashboard
**Purpose**: Global equipment overview and status monitoring

**Features**:
- Equipment Overview (All sites, Status filtering, Search functionality)
- Service Schedules (Upcoming maintenance, Overdue items)
- Performance Metrics (Availability, Utilization, Efficiency)
- Maintenance History (Service records, Parts usage, Cost tracking)
- Reporting Tools (Equipment-specific reports, Bulk export)

**Layout Structure**:
```
TopAppBar: Global View | Filter Options | Export
TabLayout: All | Due | Serviced | Critical | Breakdown

Equipment List:
- Card-based layout with status indicators
- Quick action buttons (Service, Report, History)
- Performance charts and metrics
```

---

## Authentication & Connectivity

### Authentication Flow

#### Initial Setup
1. **Network Verification**: Check internet connectivity
2. **Credential Input**: Email/Employee ID + Password + Site Selection
3. **Server Authentication**: Validate against backend API
4. **Local Storage**: Secure credential hashing and storage
5. **Role Assignment**: Download user permissions and site access

#### Offline Authentication
- Stored credential validation (14-day maximum)
- Biometric authentication (if enabled)
- Site data synchronization on reconnect
- Emergency access protocols

#### Security Measures
- EncryptedSharedPreferences for credential storage
- Certificate pinning for API communications
- Session timeout and re-authentication
- Audit logging for access attempts

### Connectivity Management

#### Online Operations
- Real-time data synchronization
- Immediate report submission
- Live equipment status updates
- User management and role changes

#### Offline Capabilities
- Local data storage with Room database
- Queued synchronization with WorkManager
- Offline report creation and editing
- Cached equipment and user data

#### Sync Strategy
- Background synchronization on connectivity restore
- Conflict resolution for concurrent edits
- Priority-based upload queue
- Retry mechanisms with exponential backoff

---

## Export Engine Strategy

### PDF Export System

#### Template Management
- Store original PDF templates in `/assets/pdf_templates/`
- Template versioning and update management
- Template validation and integrity checks

#### Coordinate-Based Rendering
- Use iText7 for PDF manipulation
- Precise field positioning based on coordinate maps
- Font matching and text formatting
- Image and signature overlay support

#### Export Process
1. Load template PDF from assets
2. Parse form data and validate completeness
3. Apply coordinate-based field overlays
4. Add signatures, photos, and dynamic content
5. Generate final PDF with compression
6. Save to local storage and queue for cloud sync

### Excel Export System

#### Data Structure
- Use Apache POI for Excel generation
- Structured worksheets with proper headers
- Data validation and formatting
- Chart and graph generation for metrics

#### Export Features
- Multi-sheet workbooks for complex reports
- Filtered data views and pivot tables
- Conditional formatting for status indicators
- Automatic calculation formulas

---

## Branding & UI Guidelines

### AECI Mining Brand Implementation

#### Color Palette
```xml
<!-- colors.xml -->
<color name="aeci_red_primary">#C8102E</color>
<color name="aeci_white">#FFFFFF</color>
<color name="aeci_grey_light">#D8D8D8</color>
<color name="aeci_grey_dark">#666666</color>
<color name="aeci_black">#000000</color>
<color name="status_green">#4CAF50</color>
<color name="status_yellow">#FF9800</color>
<color name="status_red">#F44336</color>
```

#### Typography
- Primary: Roboto (Google Fonts)
- Secondary: Work Sans (Google Fonts)
- Monospace: Roboto Mono (for technical data)

#### Component Styling
- Buttons: Red background, white text, rounded corners
- Form fields: White background, red accent, black text
- Cards: White background, subtle shadow, red accent strip
- Status indicators: Color-coded with icons

### Material3 Integration

#### Theme Configuration
```xml
<!-- themes.xml -->
<style name="AECITheme" parent="Theme.Material3.DayNight">
    <item name="colorPrimary">@color/aeci_red_primary</item>
    <item name="colorOnPrimary">@color/aeci_white</item>
    <item name="colorSecondary">@color/aeci_grey_light</item>
    <item name="colorTertiary">@color/aeci_grey_dark</item>
</style>
```

#### Dark Mode Support
- Maintain brand colors with adjusted opacity
- Ensure accessibility compliance
- Preserve form readability

---

## Validation & Error Prevention

### Form Validation System

#### Field-Level Validation
- Real-time input validation
- Visual error indicators (red borders, helper text)
- Required field enforcement
- Data type and format validation

#### Business Logic Validation
- Cross-field dependencies
- Range and boundary checks
- Consistency validation across related forms
- Equipment-specific validation rules

#### Validation Rules Engine
```kotlin
data class ValidationRule(
    val ruleType: ValidationType,
    val parameters: Map<String, Any>,
    val errorMessage: String,
    val isRequired: Boolean = false
)

enum class ValidationType {
    REQUIRED, MIN_LENGTH, MAX_LENGTH, NUMERIC_RANGE,
    DATE_RANGE, EMAIL, PHONE, EQUIPMENT_ID, POSITIVE_NUMBER
}
```

### Error Prevention Strategies

#### Development Phase
- Comprehensive resource validation
- Icon and drawable existence checks
- String resource completeness
- Theme consistency verification

#### Build Phase
- Lint check enforcement
- ProGuard/R8 optimization
- Resource optimization
- APK size monitoring

#### Runtime Phase
- Null safety enforcement
- Exception handling and logging
- Network error recovery
- Data corruption detection

---

## Pre-Launch QA Strategy

### Testing Framework

#### Unit Testing
- Repository pattern testing
- UseCase business logic validation
- Data model serialization/deserialization
- Validation rule testing

#### Integration Testing
- Database operations
- Network API interactions
- PDF generation and export
- Authentication flow

#### UI Testing
- Form navigation and input
- Dashboard functionality
- Export operations
- Offline/online transitions

#### Manual Testing Scenarios
- Complete form workflows
- Multi-user role testing
- Offline operation validation
- Export fidelity verification
- Performance under load

### Quality Assurance Checklist

#### Functional Testing
- [ ] All forms render correctly
- [ ] PDF exports match original layouts
- [ ] Excel exports contain all data
- [ ] Authentication works offline/online
- [ ] Role-based access enforcement
- [ ] Data synchronization accuracy

#### Performance Testing
- [ ] App startup time < 3 seconds
- [ ] Form loading time < 1 second
- [ ] Export generation time acceptable
- [ ] Memory usage within limits
- [ ] Battery consumption optimized

#### Security Testing
- [ ] Credential encryption validation
- [ ] API security compliance
- [ ] Data privacy protection
- [ ] Audit trail completeness

#### Usability Testing
- [ ] Intuitive navigation
- [ ] Form completion efficiency
- [ ] Error message clarity
- [ ] Accessibility compliance

---

## Implementation Roadmap

### Phase 1: Foundation (Weeks 1-3)
- Project setup and architecture
- Core data models and database schema
- Authentication system implementation
- Basic UI theme and navigation

### Phase 2: Core Forms (Weeks 4-8)
- Implement priority forms (Production Log, Maintenance Record)
- Coordinate-based rendering engine
- Basic PDF export functionality
- Offline data storage

### Phase 3: Dashboard Development (Weeks 9-11)
- Role-based dashboards
- Equipment management features
- User administration
- Report monitoring

### Phase 4: Advanced Features (Weeks 12-14)
- Excel export capabilities
- Advanced synchronization
- Performance optimization
- Security hardening

### Phase 5: Testing & Deployment (Weeks 15-16)
- Comprehensive testing
- User acceptance testing
- Production deployment
- Documentation completion

### Success Metrics
- Form completion time reduction: 50%
- Data accuracy improvement: 95%+
- User adoption rate: 80%+ within 3 months
- Export fidelity: 99% match to original PDFs
- Offline capability: 100% form functionality

---

This comprehensive blueprint provides the complete foundation for developing the AECI MMU Companion app, ensuring pixel-perfect PDF replication, robust offline capabilities, and efficient mining operation digitization.
