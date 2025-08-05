package com.aeci.mmucompanion.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// Timesheet Models
data class Timesheet(
    val id: String,
    val userId: String,
    val userName: String,
    val period: TimesheetPeriod,
    val status: TimesheetStatus,
    val entries: List<TimesheetEntry>,
    val totalHours: Double,
    val regularHours: Double,
    val overtimeHours: Double,
    val submittedAt: LocalDateTime?,
    val approvedAt: LocalDateTime?,
    val approvedBy: String?,
    val comments: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class TimesheetPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val weekNumber: Int,
    val year: Int
)

enum class TimesheetStatus {
    DRAFT, SUBMITTED, APPROVED, REJECTED, PENDING_APPROVAL
}

data class TimesheetEntry(
    val id: String,
    val timesheetId: String,
    val date: LocalDate,
    val shiftType: ShiftType,
    val clockInTime: LocalTime?,
    val clockOutTime: LocalTime?,
    val breakDuration: Int = 30, // minutes
    val totalHours: Double,
    val regularHours: Double,
    val overtimeHours: Double,
    val jobCardId: String?,
    val equipmentId: String?,
    val activities: List<String>,
    val notes: String?,
    val location: String,
    val isAbsent: Boolean = false,
    val absenceReason: String?
)

data class TimesheetTemplate(
    val id: String,
    val name: String,
    val description: String,
    val shiftPattern: String,
    val regularHoursPerDay: Double,
    val workDaysPerWeek: Int,
    val isActive: Boolean = true
)

// Timesheet Summary for dashboard
data class TimesheetSummary(
    val currentWeekHours: Double,
    val currentMonthHours: Double,
    val pendingTimesheets: Int,
    val lastSubmittedDate: LocalDate?
)

// Time tracking for real-time clock in/out
data class TimeClockEntry(
    val id: String,
    val userId: String,
    val timestamp: LocalDateTime,
    val type: TimeClockType,
    val location: String?,
    val equipmentId: String?,
    val notes: String?
)

enum class TimeClockType {
    CLOCK_IN, CLOCK_OUT, BREAK_START, BREAK_END
}
