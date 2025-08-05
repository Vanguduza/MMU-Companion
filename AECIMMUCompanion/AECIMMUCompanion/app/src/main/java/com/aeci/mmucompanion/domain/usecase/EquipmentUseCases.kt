package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.EquipmentStatusIndicator
import com.aeci.mmucompanion.domain.repository.EquipmentRepository
import javax.inject.Inject

class UpdateEquipmentStatusUseCase @Inject constructor(
    private val equipmentRepository: EquipmentRepository
) {
    suspend operator fun invoke(
        equipmentId: String,
        statusIndicator: EquipmentStatusIndicator,
        conditionDescription: String,
        conditionImageUri: String? = null,
        modifiedBy: String
    ): Result<Unit> {
        return equipmentRepository.updateEquipmentStatusAndCondition(
            id = equipmentId,
            statusIndicator = statusIndicator,
            conditionDescription = conditionDescription,
            conditionImageUri = conditionImageUri,
            modifiedBy = modifiedBy
        )
    }
}

class GetEquipmentByIdUseCase @Inject constructor(
    private val equipmentRepository: EquipmentRepository
) {
    suspend operator fun invoke(equipmentId: String) = equipmentRepository.getEquipmentById(equipmentId)
}

class GetAllEquipmentUseCase @Inject constructor(
    private val equipmentRepository: EquipmentRepository
) {
    operator fun invoke() = equipmentRepository.getAllActiveEquipment()
}

