package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.Priority
import com.aeci.mmucompanion.domain.repository.EquipmentRepository
import com.aeci.mmucompanion.domain.repository.UserRepository
import javax.inject.Inject

class CreateWorkOrderUseCase @Inject constructor(
    private val equipmentRepository: EquipmentRepository,
    private val userRepository: UserRepository,
    private val createTaskNotificationUseCase: CreateTaskNotificationUseCase
) {
    suspend operator fun invoke(
        equipmentId: String,
        assignedTo: String,
        description: String,
        priority: Priority = Priority.MEDIUM,
        site: String? = null
    ): Result<String> {
        return try {
            // Validate inputs
            if (equipmentId.isBlank()) {
                return Result.failure(IllegalArgumentException("Equipment ID cannot be empty"))
            }
            if (assignedTo.isBlank()) {
                return Result.failure(IllegalArgumentException("Assigned user cannot be empty"))
            }
            if (description.isBlank()) {
                return Result.failure(IllegalArgumentException("Description cannot be empty"))
            }
            
            // Create work order
            val result = equipmentRepository.createWorkOrder(
                equipmentId = equipmentId,
                assignedTo = assignedTo,
                description = description,
                priority = priority,
                site = site
            )
            
            // Send notification if work order was created successfully
            if (result.isSuccess) {
                val workOrderId = result.getOrNull()!!
                val equipment = equipmentRepository.getEquipmentById(equipmentId)
                val equipmentName = equipment?.name ?: "Unknown Equipment"
                
                // Notify the assigned user
                createTaskNotificationUseCase.notifyTaskAssigned(
                    assignedUserId = assignedTo,
                    taskId = workOrderId,
                    taskDescription = description,
                    equipmentName = equipmentName
                )
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 
