package com.aeci.mmucompanion.domain.model

data class ProductivityMetrics(
    val efficiency: Double = 0.0,
    val tasksPerHour: Double = 0.0,
    val averageTaskDuration: Long = 0L,
    val completionRate: Double = 0.0,
    val qualityScore: Double = 0.0,
    val timeUtilization: Double = 0.0,
    val breakEfficiency: Double = 0.0,
    val performanceScore: Double = 0.0,
    val consistencyRating: Double = 0.0,
    val improvementTrend: Double = 0.0,
    val benchmarkComparison: Double = 0.0,
    val recommendedBreakTime: Long = 0L
)
