package com.aeci.mmucompanion.domain.model

import java.util.Date

data class SystemActivity(
    val id: String,
    val user: String,
    val action: String,
    val type: ActivityType,
    val timestamp: Date
)
