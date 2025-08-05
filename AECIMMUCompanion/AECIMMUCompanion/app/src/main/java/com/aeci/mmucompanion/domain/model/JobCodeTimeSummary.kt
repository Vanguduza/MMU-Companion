package com.aeci.mmucompanion.domain.model

data class JobCodeTimeSummary(
    val jobCode: String = "",
    val jobTitle: String = "",
    val totalTime: Long = 0L,
    val taskCount: Int = 0,
    val averageTime: Long = 0L,
    val minTime: Long = 0L,
    val maxTime: Long = 0L,
    val completionRate: Double = 0.0,
    val efficiencyScore: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val category: String = "",
    val priority: String = "MEDIUM",
    val estimatedTime: Long = 0L,
    val actualTime: Long = 0L,
    val variance: Double = 0.0
)
