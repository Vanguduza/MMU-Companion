package com.aeci.mmucompanion.domain.model

data class SystemStats(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val totalForms: Int = 0,
    val formsToday: Int = 0,
    val totalEquipment: Int = 0,
    val systemLoad: Float = 0f,
    val storageUsed: Float = 0f,
    val lastBackupTime: String = "N/A"
)
