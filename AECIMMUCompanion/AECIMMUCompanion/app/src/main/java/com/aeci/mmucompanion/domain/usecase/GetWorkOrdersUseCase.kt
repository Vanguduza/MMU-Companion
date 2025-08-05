package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.WorkOrder
import com.aeci.mmucompanion.domain.repository.EquipmentRepository
import javax.inject.Inject

class GetWorkOrdersUseCase @Inject constructor(
    private val equipmentRepository: EquipmentRepository
) {
    suspend operator fun invoke(): List<WorkOrder> {
        return equipmentRepository.getWorkOrders()
    }
}

