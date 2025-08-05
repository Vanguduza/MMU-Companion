package com.aeci.mmucompanion.domain.repository

import com.aeci.mmucompanion.domain.model.SystemStats
import com.aeci.mmucompanion.domain.model.SystemActivity
import com.aeci.mmucompanion.domain.model.SystemHealth

interface SystemRepository {
    suspend fun getSystemStats(): SystemStats
    suspend fun getRecentActivities(): List<SystemActivity>
    suspend fun getSystemHealth(): SystemHealth
}
