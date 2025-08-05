package com.aeci.mmucompanion.presentation.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.aeci.mmucompanion.presentation.model.FormMenuItem

fun getMaintenanceForms(): List<FormMenuItem> {
    return listOf(
        FormMenuItem(
            title = "90 Day Pump System Inspection",
            formType = "PUMP_90_DAY_INSPECTION",
            description = "Comprehensive pump system inspection checklist",
            icon = Icons.Default.Engineering
        ),
        FormMenuItem(
            title = "Fire Extinguisher Inspection",
            formType = "FIRE_EXTINGUISHER_INSPECTION",
            description = "Safety equipment inspection checklist",
            icon = Icons.Default.LocalFireDepartment
        ),
        FormMenuItem(
            title = "On Bench MMU Inspection",
            formType = "ON_BENCH_MMU_INSPECTION",
            description = "MMU bench inspection and maintenance record",
            icon = Icons.Default.Build
        ),
        FormMenuItem(
            title = "PC Pump Pressure Test",
            formType = "PC_PUMP_PRESSURE_TEST",
            description = "High/low pressure trip test procedure",
            icon = Icons.Default.Speed
        ),
        FormMenuItem(
            title = "Monthly Process Maintenance",
            formType = "MONTHLY_PROCESS_MAINTENANCE",
            description = "Monthly maintenance and process record",
            icon = Icons.Default.Schedule
        ),
        FormMenuItem(
            title = "MMU Chassis Maintenance",
            formType = "MMU_CHASSIS_MAINTENANCE",
            description = "Chassis maintenance and service record",
            icon = Icons.Default.DirectionsCar
        ),
        FormMenuItem(
            title = "Bowie Pump Weekly Check",
            formType = "BOWIE_PUMP_WEEKLY_CHECK",
            description = "Weekly pump inspection checklist",
            icon = Icons.Default.PlaylistAddCheck
        )
    )
}

fun getProductionForms(): List<FormMenuItem> {
    return listOf(
        FormMenuItem(
            title = "MMU Daily Production Log",
            formType = "MMU_DAILY_LOG",
            description = "Daily production and operations log",
            icon = Icons.Default.Today
        ),
        FormMenuItem(
            title = "MMU Quality Report",
            formType = "MMU_QUALITY_REPORT",
            description = "Production quality assessment report",
            icon = Icons.Default.Assessment
        ),
        FormMenuItem(
            title = "MMU Handover Certificate",
            formType = "MMU_HANDOVER_CERTIFICATE",
            description = "Equipment handover and certification",
            icon = Icons.Default.Handshake
        ),
        FormMenuItem(
            title = "Blast Hole Log",
            formType = "BLAST_HOLE_LOG",
            description = "Blast hole drilling and preparation log",
            icon = Icons.Default.Construction
        ),
        FormMenuItem(
            title = "Job Card",
            formType = "JOB_CARD",
            description = "Work order and job completion card",
            icon = Icons.Filled.Assignment
        ),
        FormMenuItem(
            title = "Availability & Utilization",
            formType = "AVAILABILITY_UTILIZATION",
            description = "Equipment availability and utilization report",
            icon = Icons.Default.Analytics
        )
    )
}

fun getSafetyForms(): List<FormMenuItem> {
    return listOf(
        FormMenuItem(
            title = "Pre-Task Safety Check",
            formType = "PRETASK_SAFETY_CHECK",
            description = "Pre-work safety assessment and checklist",
            icon = Icons.Default.Security
        ),
        FormMenuItem(
            title = "Fire Extinguisher Inspection",
            formType = "FIRE_EXTINGUISHER_INSPECTION",
            description = "Fire safety equipment inspection",
            icon = Icons.Default.LocalFireDepartment
        ),
        FormMenuItem(
            title = "UOR (Unusual Occurrence Report)",
            formType = "UOR_REPORT",
            description = "Incident and unusual occurrence reporting",
            icon = Icons.Default.Warning
        ),
        FormMenuItem(
            title = "Equipment Safety Check",
            formType = "EQUIPMENT_SAFETY_CHECK",
            description = "Pre-operation equipment safety verification",
            icon = Icons.Default.Verified
        ),
        FormMenuItem(
            title = "Safety Training Record",
            formType = "SAFETY_TRAINING_RECORD",
            description = "Safety training completion and certification",
            icon = Icons.Default.School
        )
    )
}
