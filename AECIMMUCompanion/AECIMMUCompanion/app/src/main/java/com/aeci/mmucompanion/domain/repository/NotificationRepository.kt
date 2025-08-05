package com.aeci.mmucompanion.domain.repository

import com.aeci.mmucompanion.domain.model.Notification
import com.aeci.mmucompanion.domain.model.NotificationSettings
import com.aeci.mmucompanion.domain.model.NotificationType
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun createNotification(notification: Notification): Result<String>
    
    suspend fun getNotifications(userId: String): Flow<List<Notification>>
    
    suspend fun getUnreadNotifications(userId: String): Flow<List<Notification>>
    
    suspend fun getNotificationsByType(userId: String, type: NotificationType): Flow<List<Notification>>
    
    suspend fun markAsRead(notificationId: String): Result<Unit>
    
    suspend fun markAllAsRead(userId: String): Result<Unit>
    
    suspend fun deleteNotification(notificationId: String): Result<Unit>
    
    suspend fun deleteExpiredNotifications(): Result<Unit>
    
    suspend fun getNotificationSettings(userId: String): NotificationSettings?
    
    suspend fun updateNotificationSettings(settings: NotificationSettings): Result<Unit>
    
    suspend fun scheduleNotification(notification: Notification): Result<Unit>
    
    suspend fun cancelScheduledNotification(notificationId: String): Result<Unit>
    
    suspend fun sendPushNotification(notification: Notification): Result<Unit>
    
    suspend fun getNotificationCount(userId: String): Int
    
    suspend fun getUnreadNotificationCount(userId: String): Int
} 