package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.MaintenanceTask
import com.aeci.mmucompanion.domain.repository.EquipmentRepository
import javax.inject.Inject

class GetMaintenanceTasksUseCase @Inject constructor(
    private val equipmentRepository: EquipmentRepository
) {
    suspend operator fun invoke(): List<MaintenanceTask> {
        return equipmentRepository.getMaintenanceTasks()
    }
}

