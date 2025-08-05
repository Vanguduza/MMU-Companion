package com.aeci.mmucompanion.domain.model

data class SystemHealth(
    val databaseHealth: HealthStatus,
    val syncServiceHealth: HealthStatus,
    val storageHealth: HealthStatus,
    val networkHealth: HealthStatus
) 