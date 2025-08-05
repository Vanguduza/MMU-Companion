package com.aeci.mmucompanion.domain.model

data class TimeTrackingStatistics(
    val totalTimeWorked: Long = 0L,
    val totalTasks: Int = 0,
    val averageTaskTime: Long = 0L,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val totalBreakTime: Long = 0L,
    val productivityScore: Double = 0.0,
    val mostProductiveHour: Int = 9,
    val dailyAverage: Long = 0L,
    val weeklyTotal: Long = 0L,
    val monthlyTotal: Long = 0L,
    val overtimeHours: Long = 0L
)
