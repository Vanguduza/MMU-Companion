package com.aeci.mmucompanion.domain.model

import java.util.Date

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val priority: NotificationPriority,
    val userId: String,
    val relatedEntityId: String? = null, // Task ID, Equipment ID, etc.
    val relatedEntityType: String? = null, // "task", "equipment", "form", etc.
    val isRead: Boolean = false,
    val createdAt: Date,
    val scheduledAt: Date? = null,
    val expiresAt: Date? = null,
    val actionUrl: String? = null,
    val metadata: Map<String, Any> = emptyMap()
)

enum class NotificationType {
    TASK_ASSIGNED,
    TASK_COMPLETED,
    TASK_OVERDUE,
    TASK_UPDATED,
    EQUIPMENT_ALERT,
    EQUIPMENT_MAINTENANCE_DUE,
    FORM_SUBMITTED,
    FORM_APPROVED,
    FORM_REJECTED,
    SYSTEM_ALERT,
    SYSTEM_MAINTENANCE,
    USER_MESSAGE,
    REMINDER
}

enum class NotificationPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

data class NotificationSettings(
    val userId: String,
    val enablePushNotifications: Boolean = true,
    val enableEmailNotifications: Boolean = true,
    val enableInAppNotifications: Boolean = true,
    val taskNotifications: Boolean = true,
    val equipmentNotifications: Boolean = true,
    val formNotifications: Boolean = true,
    val systemNotifications: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "06:00"
)

data class NotificationChannel(
    val id: String,
    val name: String,
    val description: String,
    val importance: Int,
    val enableVibration: Boolean = true,
    val enableSound: Boolean = true,
    val soundUri: String? = null
) 