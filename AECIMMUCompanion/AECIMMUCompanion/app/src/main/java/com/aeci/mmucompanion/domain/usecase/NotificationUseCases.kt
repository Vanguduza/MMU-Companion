package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.Notification
import com.aeci.mmucompanion.domain.model.NotificationType
import com.aeci.mmucompanion.domain.model.NotificationPriority
import com.aeci.mmucompanion.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class CreateNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(
        title: String,
        message: String,
        type: NotificationType,
        priority: NotificationPriority,
        userId: String,
        relatedEntityId: String? = null,
        relatedEntityType: String? = null,
        actionUrl: String? = null,
        metadata: Map<String, Any> = emptyMap()
    ): Result<String> {
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            type = type,
            priority = priority,
            userId = userId,
            relatedEntityId = relatedEntityId,
            relatedEntityType = relatedEntityType,
            createdAt = Date(),
            actionUrl = actionUrl,
            metadata = metadata
        )
        
        return notificationRepository.createNotification(notification)
    }
}

class CreateTaskNotificationUseCase @Inject constructor(
    private val createNotificationUseCase: CreateNotificationUseCase
) {
    suspend fun notifyTaskAssigned(
        assignedUserId: String,
        taskId: String,
        taskDescription: String,
        equipmentName: String
    ): Result<String> {
        return createNotificationUseCase(
            title = "New Task Assigned",
            message = "You have been assigned a new task for $equipmentName: $taskDescription",
            type = NotificationType.TASK_ASSIGNED,
            priority = NotificationPriority.MEDIUM,
            userId = assignedUserId,
            relatedEntityId = taskId,
            relatedEntityType = "task",
            actionUrl = "tasks/$taskId",
            metadata = mapOf(
                "equipmentName" to equipmentName,
                "taskDescription" to taskDescription
            )
        )
    }
    
    suspend fun notifyTaskCompleted(
        supervisorUserId: String,
        taskId: String,
        taskDescription: String,
        completedByUser: String
    ): Result<String> {
        return createNotificationUseCase(
            title = "Task Completed",
            message = "$completedByUser has completed the task: $taskDescription",
            type = NotificationType.TASK_COMPLETED,
            priority = NotificationPriority.LOW,
            userId = supervisorUserId,
            relatedEntityId = taskId,
            relatedEntityType = "task",
            actionUrl = "tasks/$taskId",
            metadata = mapOf(
                "completedByUser" to completedByUser,
                "taskDescription" to taskDescription
            )
        )
    }
    
    suspend fun notifyTaskOverdue(
        assignedUserId: String,
        taskId: String,
        taskDescription: String,
        daysOverdue: Int
    ): Result<String> {
        return createNotificationUseCase(
            title = "Task Overdue",
            message = "Task is $daysOverdue days overdue: $taskDescription",
            type = NotificationType.TASK_OVERDUE,
            priority = NotificationPriority.HIGH,
            userId = assignedUserId,
            relatedEntityId = taskId,
            relatedEntityType = "task",
            actionUrl = "tasks/$taskId",
            metadata = mapOf(
                "daysOverdue" to daysOverdue,
                "taskDescription" to taskDescription
            )
        )
    }
}

class GetNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(userId: String): Flow<List<Notification>> {
        return notificationRepository.getNotifications(userId)
    }
}

class GetUnreadNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(userId: String): Flow<List<Notification>> {
        return notificationRepository.getUnreadNotifications(userId)
    }
}

class MarkNotificationAsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: String): Result<Unit> {
        return notificationRepository.markAsRead(notificationId)
    }
}

class MarkAllNotificationsAsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        return notificationRepository.markAllAsRead(userId)
    }
}

class GetUnreadNotificationCountUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(userId: String): Int {
        return notificationRepository.getUnreadNotificationCount(userId)
    }
}

class DeleteNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: String): Result<Unit> {
        return notificationRepository.deleteNotification(notificationId)
    }
} 
