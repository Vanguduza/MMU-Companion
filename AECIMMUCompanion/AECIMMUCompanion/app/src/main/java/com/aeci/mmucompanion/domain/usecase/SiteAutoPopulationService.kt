[nqs#package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.domain.model.User
import com.aeci.mmucompanion.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SiteAutoPopulationService @Inject constructor(
    private val userRepository: UserRepository) {
    
    /**
     * Auto-populates site-related fields in forms based on user's current site assignment
     */
    suspend fun autoPopulateForm(form: Form, userId: String): Form {
        val user = userRepository.getUserById(userId) ?: return form
        
        return when (form) {
            is BlastHoleLogForm -> {
                form.copy(
                    siteId = user.siteId,
                    siteName = form.siteName, // Keep original site name
                    siteLocation = form.siteLocation, // Keep original location
                    operatorName = if (form.operatorName.isBlank()) user.fullName else form.operatorName
                )
            }
            
            is MmuQualityReportForm -> {
                form.copy(
                    siteId = user.siteId,
                    siteLocation = form.siteLocation,
                    qualityTechnician = if (form.qualityTechnician.isBlank()) user.fullName else form.qualityTechnician
                )
            }
            
            is MmuProductionDailyLogForm -> {
                form.copy(
                    siteId = user.siteId,
                    siteLocation = form.siteLocation,
                    operatorName = if (form.operatorName.isBlank()) user.fullName else form.operatorName
                )
            }
            
            is PumpInspection90DayForm -> {
                form.copy(
                    siteId = user.siteId,
                    siteLocation = form.siteLocation,
                    siteName = form.siteName,
                    inspectorName = if (form.inspectorName.isBlank()) user.fullName else form.inspectorName
                )
            }
            
            is FireExtinguisherInspectionForm -> {
                form.copy(
                    siteId = user.currentSiteId ?: form.siteId,
                    siteName = user.currentSiteName ?: form.siteName,
                    inspectorName = if (form.inspectorName.isBlank()) user.fullName else form.inspectorName
                )
            }
            
            is BowiePumpWeeklyCheckForm -> {
                form.copy(
                    siteId = user.currentSiteId ?: form.siteId,
                    siteName = user.currentSiteName ?: form.siteName,
                    technicianName = if (form.technicianName.isBlank()) user.fullName else form.technicianName
                )
            }
            
            is MmuChassisMaintenance -> {
                form.copy(
                    siteId = user.currentSiteId ?: form.siteId,
                    siteName = user.currentSiteName ?: form.siteName,
                    technicianName = if (form.technicianName.isBlank()) user.fullName else form.technicianName
                )
            }
            
            is MmuHandoverCertificate -> {
                form.copy(
                    siteId = user.currentSiteId ?: form.siteId,
                    siteName = user.currentSiteName ?: form.siteName,
                    handoverTechnician = if (form.handoverTechnician.isBlank()) user.fullName else form.handoverTechnician
                )
            }
            
            is OnBenchMmuInspection -> {
                form.copy(
                    siteId = user.currentSiteId ?: form.siteId,
                    siteName = user.currentSiteName ?: form.siteName,
                    inspectorName = if (form.inspectorName.isBlank()) user.fullName else form.inspectorName
                )
            }
            
            is PcPumpHighLowPressureTripTest -> {
                form.copy(
                    siteId = user.currentSiteId ?: form.siteId,
                    siteName = user.currentSiteName ?: form.siteName,
                    testTechnician = if (form.testTechnician.isBlank()) user.fullName else form.testTechnician
                )
            }
            
            is MonthlyProcessMaintenance -> {
                form.copy(
                    siteId = user.currentSiteId ?: form.siteId,
                    siteName = user.currentSiteName ?: form.siteName,
                    maintenanceTechnician = if (form.maintenanceTechnician.isBlank()) user.fullName else form.maintenanceTechnician
                )
            }
            
            is PreTaskSafety -> {
                form.copy(
                    siteId = user.currentSiteId ?: form.siteId,
                    siteName = user.currentSiteName ?: form.siteName,
                    teamLeader = if (form.teamLeader.isBlank()) user.fullName else form.teamLeader
                )
            }
            
            is JobCard -> {
                form.copy(
                    siteId = user.currentSiteId ?: form.siteId,
                    siteName = user.currentSiteName ?: form.siteName,
                    assignedTechnician = if (form.assignedTechnician.isBlank()) user.fullName else form.assignedTechnician
                )
            }
            
            is Timesheet -> {
                form.copy(
                    siteId = user.currentSiteId ?: form.siteId,
                    siteName = user.currentSiteName ?: form.siteName,
                    employeeName = if (form.employeeName.isBlank()) user.fullName else form.employeeName
                )
            }
            
            else -> form
        }
    }
    
    /**
     * Gets default Form values based on user's current site and role
     */
    suspend fun getDefaultFormValues(userId: String, formType: FormType): Map<String, Any> {
        val user = userRepository.getUserById(userId) ?: return emptyMap()
        
        val baseDefaults = mutableMapOf<String, Any>()
        
        // Common site-related defaults
        baseDefaults["siteId"] = user.siteId
        baseDefaults["siteName"] = "" // Will need to be populated from a site service
        baseDefaults["siteLocation"] = "" // Will need to be populated from a site service
        
        // Role-specific defaults
        when (user.role) {
            UserRole.MAINTENANCE -> {
                baseDefaults["technicianName"] = user.fullName
                baseDefaults["operatorName"] = user.fullName
                baseDefaults["inspectorName"] = user.fullName
            }
            UserRole.SUPERVISOR -> {
                baseDefaults["supervisorName"] = user.fullName
                baseDefaults["shiftSupervisor"] = user.fullName
                baseDefaults["approvedBy"] = user.fullName
            }
            UserRole.OPERATOR -> {
                baseDefaults["operatorName"] = user.fullName
                baseDefaults["employeeName"] = user.fullName
            }
            UserRole.ADMIN -> {
                baseDefaults["approvedBy"] = user.fullName
            }
        }
        
        // Form-specific defaults
        when (formType) {
            FormType.BLAST_HOLE_LOG -> {
                baseDefaults["blastDate"] = java.time.LocalDate.now()
                baseDefaults["recordedBy"] = user.fullName
            }
            
            FormType.MMU_QUALITY_REPORT -> {
                baseDefaults["reportDate"] = java.time.LocalDate.now()
                baseDefaults["reportNumber"] = generateReportNumber("QR")
                baseDefaults["qualityTechnician"] = user.fullName
            }
            
            FormType.MMU_PRODUCTION_DAILY_LOG -> {
                baseDefaults["logDate"] = java.time.LocalDate.now()
                baseDefaults["startTime"] = java.time.LocalDateTime.now().withHour(6).withMinute(0)
                baseDefaults["endTime"] = java.time.LocalDateTime.now().withHour(18).withMinute(0)
            }
            
            FormType.PUMP_INSPECTION_90_DAY -> {
                baseDefaults["inspectionDate"] = java.time.LocalDate.now()
                baseDefaults["nextInspectionDue"] = java.time.LocalDate.now().plusDays(90)
            }
            
            FormType.FIRE_EXTINGUISHER_INSPECTION -> {
                baseDefaults["inspectionDate"] = java.time.LocalDate.now()
                baseDefaults["nextInspectionDue"] = java.time.LocalDate.now().plusDays(30)
            }
            
            FormType.BOWIE_PUMP_WEEKLY_CHECK_CHECK -> {
                baseDefaults["checkDate"] = java.time.LocalDate.now()
                baseDefaults["nextCheckDue"] = java.time.LocalDate.now().plusDays(7)
            }
            
            FormType.MMU_CHASSIS_MAINTENANCE -> {
                baseDefaults["maintenanceDate"] = java.time.LocalDate.now()
            }
            
            FormType.MMU_HANDOVER_CERTIFICATE -> {
                baseDefaults["handoverDate"] = java.time.LocalDate.now()
                baseDefaults["handoverTime"] = java.time.LocalTime.now()
            }
            
            FormType.ON_BENCH_MMU_INSPECTION -> {
                baseDefaults["inspectionDate"] = java.time.LocalDate.now()
            }
            
            FormType.PC_PUMP_HIGH_LOW_PRESSURE_TRIP_TEST -> {
                baseDefaults["testDate"] = java.time.LocalDate.now()
            }
            
            FormType.MONTHLY_PROCESS_MAINTENANCE -> {
                baseDefaults["maintenanceDate"] = java.time.LocalDate.now()
                baseDefaults["maintenanceMonth"] = java.time.LocalDate.now().month.name
            }
            
            FormType.PRE_TASK_SAFETY_ASSESSMENT -> {
                baseDefaults["taskDate"] = java.time.LocalDate.now()
                baseDefaults["taskStartTime"] = java.time.LocalTime.now()
            }
            
            FormType.JOB_CARD -> {
                baseDefaults["jobDate"] = java.time.LocalDate.now()
                baseDefaults["jobNumber"] = generateJobNumber()
            }
            
            FormType.TIMESHEET -> {
                baseDefaults["weekStarting"] = getWeekStartDate()
                baseDefaults["employeeId"] = user.id
                baseDefaults["employeeName"] = user.fullName
            }
            
            else -> {
                // No specific defaults for other Form types
            }
        }
        
        return baseDefaults
    }
    
    /**
     * Updates user's current site information
     */
    suspend fun updateUserCurrentSite(userId: String, siteId: String, siteName: String, siteLocation: String? = null) {
        val user = userRepository.getUserById(userId) ?: return
        
        val updatedUser = user.copy(
            siteId = siteId
            // TODO: Add siteName and siteLocation to User model if needed
        )
        
        userRepository.updateUser(updatedUser)
    }
    
    /**
     * Gets user's site history for quick selection
     */
    suspend fun getUserSiteHistory(userId: String): List<SiteInfo> {
        val user = userRepository.getUserById(userId) ?: return emptyList()
        return user.siteHistory ?: emptyList()
    }
    
    private fun generateReportNumber(prefix: String): String {
        val date = java.time.LocalDate.now()
        val timestamp = System.currentTimeMillis()
        return "$prefix-${date.year}${date.monthValue.toString().padStart(2, '0')}${date.dayOfMonth.toString().padStart(2, '0')}-${timestamp % 10000}"
    }
    
    private fun generateJobNumber(): String {
        val date = java.time.LocalDate.now()
        val timestamp = System.currentTimeMillis()
        return "JOB-${date.year}${date.monthValue.toString().padStart(2, '0')}${date.dayOfMonth.toString().padStart(2, '0')}-${timestamp % 10000}"
    }
    
    private fun getWeekStartDate(): java.time.LocalDate {
        val today = java.time.LocalDate.now()
        val dayOfWeek = today.dayOfWeek.value
        return today.minusDays((dayOfWeek - 1).toLong()) // Monday as week start
    }
}

data class SiteInfo(
    val siteId: String,
    val siteName: String,
    val siteLocation: String?,
    val lastVisited: java.time.LocalDateTime)

// Extension properties for User model
val User.currentSiteId: String?
    get() = this.siteId

val User.currentSiteName: String?
    get() = "Default Site" // TODO: Add siteName to User model

val User.currentSiteLocation: String?
    get() = "Default Location" // TODO: Add siteLocation to User model

val User.lastSiteUpdate: java.time.LocalDateTime?
    get() = java.time.LocalDateTime.now() // TODO: Add lastSiteUpdate to User model

val User.siteHistory: List<SiteInfo>?
    get() = listOf(SiteInfo(
        siteId = this.siteId,
        siteName = "Default Site",
        siteLocation = "Default Location",
        lastVisited = java.time.LocalDateTime.now()
    ))

data class SiteAssignment(
    val siteId: String,
    val siteName: String,
    val siteLocation: String?,
    val assignedAt: java.time.LocalDateTime,
    val isActive: Boolean = true)



