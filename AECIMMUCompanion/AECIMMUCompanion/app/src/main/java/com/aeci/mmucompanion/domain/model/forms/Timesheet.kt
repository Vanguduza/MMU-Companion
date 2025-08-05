package com.aeci.mmucompanion.domain.model.forms

import java.time.LocalDateTime

data class Timesheet(
    val id: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val siteLocation: String = "",
    val employeeName: String = "",
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val status: String = "Draft",
    
    // Timesheet specific fields
    val employeeId: String = "",
    val weekEndingDate: LocalDateTime? = null,
    val mondayHours: Double = 0.0,
    val tuesdayHours: Double = 0.0,
    val wednesdayHours: Double = 0.0,
    val thursdayHours: Double = 0.0,
    val fridayHours: Double = 0.0,
    val saturdayHours: Double = 0.0,
    val sundayHours: Double = 0.0,
    val totalRegularHours: Double = 0.0,
    val overtimeHours: Double = 0.0,
    val totalHours: Double = 0.0,
    val projectCodes: Map<String, Double> = emptyMap(),
    val leaveHours: Double = 0.0,
    val sickLeaveHours: Double = 0.0,
    val supervisorApproval: String = "",
    val employeeSignature: String = "",
    val approvalDate: LocalDateTime? = null
)
