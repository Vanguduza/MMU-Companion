package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.Equipment
import com.aeci.mmucompanion.domain.repository.EquipmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEquipmentUseCase @Inject constructor(
    private val equipmentRepository: EquipmentRepository
) {
    operator fun invoke(): Flow<List<Equipment>> {
        return equipmentRepository.getAllActiveEquipment()
    }
}

