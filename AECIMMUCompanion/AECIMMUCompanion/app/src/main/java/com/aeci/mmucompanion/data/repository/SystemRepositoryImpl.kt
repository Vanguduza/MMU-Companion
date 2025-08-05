package com.aeci.mmucompanion.data.repository

import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.domain.repository.SystemRepository
import java.util.*
import javax.inject.Inject

class SystemRepositoryImpl @Inject constructor() : SystemRepository {
    override suspend fun getSystemStats(): SystemStats {
        return SystemStats(
            totalUsers = 150,
            activeUsers = 25,
            totalForms = 5000,
            formsToday = 120,
            totalEquipment = 50,
            systemLoad = 0.75f,
            storageUsed = 0.60f,
            lastBackupTime = "2023-10-27 02:00"
        )
    }

    override suspend fun getRecentActivities(): List<SystemActivity> {
        return listOf(
            SystemActivity("1", "John Doe", "Logged in", ActivityType.USER_LOGIN, Date()),
            SystemActivity("2", "Jane Smith", "Submitted form 'Daily Inspection'", ActivityType.FORM_SUBMITTED, Date(System.currentTimeMillis() - 60000 * 5)),
            SystemActivity("3", "Admin", "System backup completed", ActivityType.SYSTEM_BACKUP, Date(System.currentTimeMillis() - 3600000)),
            SystemActivity("4", "System", "High CPU usage detected", ActivityType.SYSTEM_ERROR, Date(System.currentTimeMillis() - 3600000 * 2))
        )
    }

    override suspend fun getSystemHealth(): SystemHealth {
        return SystemHealth(
            databaseHealth = HealthStatus.HEALTHY,
            syncServiceHealth = HealthStatus.HEALTHY,
            storageHealth = HealthStatus.WARNING,
            networkHealth = HealthStatus.HEALTHY
        )
    }
}
