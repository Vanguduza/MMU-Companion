package com.aeci.mmucompanion.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

// Form - Base interface for all digital forms
interface Form {
    val id: String
    val formType: FormType
    val createdAt: LocalDateTime
    val updatedAt: LocalDateTime
    val createdBy: String
    val status: FormStatus
    val siteId: String
    val siteLocation: String
    val equipmentId: String?
        get() = null
    val reportNumber: String?
        get() = null
    val title: String?
        get() = null
    val assignedTo: String?
        get() = null
    val description: String?
        get() = null
    val location: String?
        get() = null
    val instructions: String?
        get() = null
    val submittedBy: String?
        get() = null
    val completedBy: String?
        get() = null
    val reviewedBy: String?
        get() = null
    val notes: String?
        get() = null
}

// Generic Digital Form implementation
data class DigitalForm(
    override val id: String,
    override val formType: FormType,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val createdBy: String,
    override val status: FormStatus,
    override val siteId: String,
    override val siteLocation: String,
    override val equipmentId: String? = null,
    override val reportNumber: String? = null,
    override val title: String? = null,
    override val assignedTo: String? = null,
    override val description: String? = null,
    override val location: String? = null,
    override val instructions: String? = null,
    override val submittedBy: String? = null,
    override val completedBy: String? = null,
    override val reviewedBy: String? = null,
    override val notes: String? = null,
    val data: Map<String, Any> = emptyMap()
) : Form

// Blast Hole Log Form
data class BlastHoleLogForm(
    override val id: String,
    override val formType: FormType = FormType.BLAST_HOLE_LOG,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val createdBy: String,
    override val status: FormStatus,
    override val siteId: String,
    override val siteLocation: String,
    
    // Header Information
    val blastNumber: String,
    val blastDate: LocalDate,
    val blastTime: LocalDateTime?,
    val siteName: String,
    val operatorName: String,
    
    // Blast Details
    val holes: List<BlastHole>,
    val totalEmulsionUsed: Double,
    val blastQualityGrade: String,
    override val notes: String?
) : Form

data class BlastHole(
    val holeNumber: String,
    val depth: Double,
    val diameter: Double,
    val emulsionAmount: Double,
    val primerType: String,
    val notes: String
)

// MMU Quality Report Form
data class MmuQualityReportForm(
    override val id: String,
    override val formType: FormType = FormType.MMU_QUALITY_REPORT,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val createdBy: String,
    override val status: FormStatus,
    override val siteId: String,
    override val siteLocation: String,
    
    // Report Information
    override val reportNumber: String,
    val reportDate: LocalDate,
    val shiftPattern: String,
    val shiftSupervisor: String,
    val qualityTechnician: String,
    val qualityGrade: String,
    
    // Production Data
    val targetEmulsionProduction: Double,
    val actualEmulsionProduction: Double,
    val emulsionUsedToday: Double,
    
    // Quality Testing
    val viscosityReading: Double,
    val temperatureReading: Double,
    val phLevel: Double,
    val densityTests: List<DensityTest>,
    
    // Issues and Actions
    val qualityIssues: List<QualityIssue>,
    val recommendations: String,
    val approvedBy: String
) : Form

data class DensityTest(
    val testTime: String,
    val density: Double,
    val withinSpec: Boolean
)

data class QualityIssue(
    val description: String,
    val severity: String,
    val actionTaken: String
)

// MMU Production Daily Log Form
data class MmuProductionDailyLogForm(
    override val id: String,
    override val formType: FormType = FormType.MMU_DAILY_LOG,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val createdBy: String,
    override val status: FormStatus,
    override val siteId: String,
    override val siteLocation: String,
    
    // Daily Log Information
    val logDate: LocalDate,
    val shiftDetails: String,
    val operatorName: String,
    val supervisorName: String,
    val startTime: String,
    val endTime: String,
    
    // Production Metrics
    val totalOperatingHours: Double,
    val totalEmulsionConsumed: Double,
    val qualityGradeAchieved: String,
    val productionTarget: Double,
    val actualProduction: Double,
    val operatingTemperature: Double,
    
    // Equipment and Maintenance
    val equipmentCondition: String,
    val maintenancePerformed: List<MaintenanceActivity>,
    
    // Safety and Observations
    val safetyObservations: List<SafetyObservation>,
    val operatorComments: String,
    val supervisorComments: String
) : Form

data class MaintenanceActivity(
    val description: String,
    val timeSpent: Double,
    val technicianName: String
)

data class SafetyObservation(
    val description: String,
    val actionRequired: Boolean
)

// Pump Inspection 90 Day Form
data class PumpInspection90DayForm(
    override val id: String,
    override val formType: FormType = FormType.PUMP_90_DAY_INSPECTION,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val createdBy: String,
    override val status: FormStatus,
    override val siteId: String,
    override val siteLocation: String,
    
    // Inspection Information
    val inspectionDate: LocalDate,
    val inspectorName: String,
    val siteName: String,
    val pumpSerialNumber: String,
    val equipmentLocation: String,
    val lastInspectionDate: LocalDate?,
    val nextInspectionDue: LocalDate,
    
    // Visual Inspection
    val visualInspectionItems: List<VisualInspectionItem>,
    
    // Pressure Testing
    val pressureTests: List<PressureTest>,
    
    // Overall Assessment
    val overallStatus: String,
    val recommendedActions: List<RecommendedAction>,
    val inspectorSignature: String,
    val supervisorApproval: String
) : Form

data class VisualInspectionItem(
    val itemName: String,
    val passed: Boolean,
    val notes: String
)

data class PressureTest(
    val testType: String,
    val testPressure: Double,
    val passed: Boolean,
    val notes: String
)

data class RecommendedAction(
    val description: String,
    val priority: String,
    val dueDate: LocalDate?
)

// Fire Extinguisher Inspection Form
data class FireExtinguisherInspectionForm(
    override val id: String,
    override val formType: FormType = FormType.FIRE_EXTINGUISHER_INSPECTION,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val createdBy: String,
    override val status: FormStatus,
    override val siteId: String,
    override val siteLocation: String,
    
    // Inspection Information
    val inspectionDate: LocalDate,
    val inspectorName: String,
    val siteName: String,
    
    // Extinguisher Details
    val extinguisherId: String,
    val extinguisherType: String,
    override val location: String?,
    val lastInspectionDate: LocalDate?,
    val nextInspectionDue: LocalDate,
    
    // Inspection Results
    val physicalCondition: String,
    val pressureGauge: String,
    val sealIntact: Boolean,
    val accessibilityCheck: Boolean,
    val signageVisible: Boolean,
    val weightCheck: Double,
    val dischargeTested: Boolean,
    
    // Overall Assessment
    val overallStatus: String,
    val deficienciesFound: List<String>,
    val correctiveActions: List<String>,
    val inspectorSignature: String,
    val nextServiceDate: LocalDate?
) : Form

// Bowie Pump Weekly Check Form
data class BowiePumpWeeklyCheckForm(
    override val id: String,
    override val formType: FormType = FormType.PUMP_WEEKLY_CHECK,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val createdBy: String,
    override val status: FormStatus,
    override val siteId: String,
    override val siteLocation: String,
    
    // Check Information
    val checkDate: LocalDate,
    val technicianName: String,
    val siteName: String,
    val pumpModel: String,
    val serialNumber: String,
    
    // Weekly Checks
    val oilLevel: String,
    val waterLevel: String,
    val beltCondition: String,
    val noiseLevel: String,
    val vibration: String,
    val temperature: Double,
    val pressure: Double,
    val leaks: Boolean,
    val generalCleanliness: String,
    
    // Maintenance Actions
    val maintenanceRequired: Boolean,
    val actionsPerformed: List<String>,
    val nextCheckDate: LocalDate,
    val technicianSignature: String
) : Form

// MMU Chassis Maintenance Form
data class MmuChassisMaintenanceForm(
    override val id: String,
    override val formType: FormType = FormType.MMU_CHASSIS_MAINTENANCE,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val createdBy: String,
    override val status: FormStatus,
    override val siteId: String,
    override val siteLocation: String,
    
    // Maintenance Information
    val maintenanceDate: LocalDate,
    val technicianName: String,
    val siteName: String,
    val chassisNumber: String,
    val mmuModel: String,
    
    // Maintenance Tasks
    val hydraulicSystem: String,
    val brakingSystem: String,
    val steeringSystem: String,
    val suspensionSystem: String,
    val electricalSystem: String,
    val fuelSystem: String,
    val coolingSystem: String,
    
    // Completed Work
    val workCompleted: List<String>,
    val partsReplaced: List<String>,
    val hoursSpent: Double,
    val nextMaintenanceDate: LocalDate,
    val technicianSignature: String,
    val supervisorApproval: String
) : Form

// MMU Handover Certificate Form
data class MmuHandoverCertificateForm(
    override val id: String,
    override val formType: FormType = FormType.MMU_HANDOVER_CERTIFICATE,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val createdBy: String,
    override val status: FormStatus,
    override val siteId: String,
    override val siteLocation: String,
    
    // Handover Information
    val handoverDate: LocalDate,
    val handoverTechnician: String,
    val receivingTechnician: String,
    val siteName: String,
    val mmuId: String,
    
    // Equipment Condition
    val engineCondition: String,
    val hydraulicsCondition: String,
    val electricalCondition: String,
    val structuralCondition: String,
    val safetyEquipment: String,
    
    // Documentation
    val manuals: Boolean,
    val certificates: Boolean,
    val sparePartsInventory: List<String>,
    val knownIssues: List<String>,
    val handoverNotes: String,
    
    // Signatures
    val handoverSignature: String,
    val receivingSignature: String,
    val supervisorApproval: String
) : Form

// On Bench MMU Inspection Form
data class OnBenchMmuInspectionForm(
    override val id: String,
    override val formType: FormType = FormType.ON_BENCH_MMU_INSPECTION,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val createdBy: String,
    override val status: FormStatus,
    override val siteId: String,
    override val siteLocation: String,
    
    // Inspection Information
    val inspectionDate: LocalDate,
    val inspectorName: String,
    val siteName: String,
    val mmuId: String,
    val benchLocation: String,
    
    // Inspection Areas
    val mixingChamber: String,
    val pumpAssembly: String,
    val controlSystems: String,
    val safetyDevices: String,
    val calibrationCheck: String,
    val performanceTest: String,
    
    // Test Results
    val pressureTest: Double,
    val flowRateTest: Double,
    val mixingRatioTest: String,
    val leakageTest: Boolean,
    
    // Overall Assessment
    val overallCondition: String,
    val deficienciesFound: List<String>,
    val correctiveActions: List<String>,
    val nextInspectionDate: LocalDate,
    val inspectorSignature: String,
    val qualityApproval: String
) : Form

// PC Pump High Low Pressure Trip Test Form
data class PcPumpHighLowPressureTripTestForm(
    override val id: String,
    override val formType: FormType = FormType.PC_PUMP_PRESSURE_TEST,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val createdBy: String,
    override val status: FormStatus,
    override val siteId: String,
    override val siteLocation: String,
    
    // Test Information
    val testDate: LocalDate,
    val testTechnician: String,
    val siteName: String,
    val pumpId: String,
    val pumpModel: String,
    
    // Pressure Tests
    val highPressureSetPoint: Double,
    val lowPressureSetPoint: Double,
    val highPressureTrip: Boolean,
    val lowPressureTrip: Boolean,
    val tripResponseTime: Double,
    
    // Test Results
    val testPassed: Boolean,
    val calibrationRequired: Boolean,
    val settingsAdjusted: Boolean,
    val testNotes: String,
    
    // Certification
    val nextTestDate: LocalDate,
    val technicianSignature: String,
    val supervisorApproval: String
) : Form

// Monthly Process Maintenance Form
data class MonthlyProcessMaintenanceForm(
    override val id: String,
    override val formType: FormType = FormType.MONTHLY_PROCESS_MAINTENANCE,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val createdBy: String,
    override val status: FormStatus,
    override val siteId: String,
    override val siteLocation: String,
    
    // Maintenance Information
    val maintenanceMonth: String,
    val maintenanceTechnician: String,
    val siteName: String,
    val processUnit: String,
    
    // Monthly Tasks
    val equipmentInspection: List<String>,
    val systemCalibration: List<String>,
    val preventiveMaintenance: List<String>,
    val safetySystemCheck: List<String>,
    val performanceVerification: List<String>,
    
    // Maintenance Summary
    val totalHours: Double,
    val issuesIdentified: List<String>,
    val correctiveActions: List<String>,
    val sparePartsUsed: List<String>,
    val nextMaintenanceDate: LocalDate,
    val technicianSignature: String,
    val supervisorApproval: String
) : Form

// Pre-Task Safety Form
data class PreTaskSafetyForm(
    override val id: String,
    override val formType: FormType = FormType.PRETASK_SAFETY,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val createdBy: String,
    override val status: FormStatus,
    override val siteId: String,
    override val siteLocation: String,
    
    // Task Information
    val taskDate: LocalDate,
    val taskDescription: String,
    val teamLeader: String,
    val siteName: String,
    val workLocation: String,
    
    // Safety Assessment
    val hazardIdentification: List<String>,
    val riskAssessment: List<String>,
    val controlMeasures: List<String>,
    val ppeRequired: List<String>,
    val emergencyProcedures: String,
    
    // Team Safety
    val teamMembers: List<String>,
    val safetyBriefing: Boolean,
    val equipmentCheck: Boolean,
    val communicationPlan: String,
    
    // Authorization
    val taskApproved: Boolean,
    val leaderSignature: String,
    val supervisorApproval: String
) : Form

// Job Card Form
data class JobCardForm(
    override val id: String,
    override val formType: FormType = FormType.JOB_CARD,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val createdBy: String,
    override val status: FormStatus,
    override val siteId: String,
    override val siteLocation: String,
    
    // Job Information
    val jobNumber: String,
    val jobDate: LocalDate,
    val assignedTechnician: String,
    val siteName: String,
    override val equipmentId: String,
    val jobDescription: String,
    
    // Work Details
    val workType: String,
    val priority: String,
    val estimatedHours: Double,
    val actualHours: Double,
    val materialsUsed: List<String>,
    val toolsRequired: List<String>,
    
    // Completion
    val workCompleted: Boolean,
    val qualityCheck: Boolean,
    val customerSatisfaction: String,
    val followUpRequired: Boolean,
    val technicianSignature: String,
    val supervisorApproval: String
) : Form

// Timesheet Form
data class TimesheetForm(
    override val id: String,
    override val formType: FormType = FormType.TIMESHEET,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val createdBy: String,
    override val status: FormStatus,
    override val siteId: String,
    override val siteLocation: String,
    
    // Employee Information
    val employeeId: String,
    val employeeName: String,
    val siteName: String,
    val weekEnding: LocalDate,
    val department: String,
    
    // Time Entries
    val timeEntries: List<TimeEntry>,
    val totalRegularHours: Double,
    val totalOvertimeHours: Double,
    val totalHours: Double,
    
    // Approval
    val employeeSignature: String,
    val supervisorApproval: String,
    val approvalDate: LocalDate?
) : Form

// Time Entry data class for timesheets
data class TimeEntry(
    val date: LocalDate,
    val startTime: String,
    val endTime: String,
    val jobCode: String,
    val description: String,
    val regularHours: Double,
    val overtimeHours: Double
)

// Enum for PDF Field Types
enum class PdfFieldType {
    TEXT, NUMBER, DATE, BOOLEAN, SIGNATURE, DROPDOWN, CHECKBOX
}

