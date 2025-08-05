package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.Equipment
import com.aeci.mmucompanion.domain.model.EquipmentStatus
import com.aeci.mmucompanion.domain.model.EquipmentStatusIndicator
import com.aeci.mmucompanion.domain.model.EquipmentType
import javax.inject.Inject

class UpdateEquipmentCalibrationStatusUseCase @Inject constructor() {
    
    /**
     * Updates MMU equipment status when calibration warning is flagged
     * @param equipment The equipment to update
     * @param needsCalibration Whether calibration is needed
     * @return Updated equipment with appropriate status changes
     */
    operator fun invoke(equipment: Equipment, needsCalibration: Boolean): Equipment {
        return if (needsCalibration && equipment.type == EquipmentType.MMU) {
            val updatedIssues = equipment.recordedIssues.toMutableList().apply {
                if (!contains("Calibrate pumps")) {
                    add("Calibrate pumps")
                }
            }
            
            // Update status indicator: if GREEN, change to AMBER; otherwise maintain current status
            val newStatusIndicator = if (equipment.statusIndicator == EquipmentStatusIndicator.GREEN) {
                EquipmentStatusIndicator.AMBER
            } else {
                equipment.statusIndicator
            }
            
            equipment.copy(
                statusIndicator = newStatusIndicator,
                recordedIssues = updatedIssues,
                conditionDescription = if (equipment.conditionDescription.isBlank()) {
                    "Pump calibration required - discrepancy > 3%"
                } else {
                    "${equipment.conditionDescription}; Pump calibration required - discrepancy > 3%"
                },
                lastModifiedAt = System.currentTimeMillis()
            )
        } else {
            equipment
        }
    }
    
    /**
     * Removes calibration warning from equipment
     * @param equipment The equipment to update
     * @return Updated equipment with calibration warning removed
     */
    fun removeCalibrationWarning(equipment: Equipment): Equipment {
        val updatedIssues = equipment.recordedIssues.filter { it != "Calibrate pumps" }
        
        // If this was the only issue and status was AMBER, consider changing back to GREEN
        val newStatusIndicator = if (updatedIssues.isEmpty() && 
                                    equipment.statusIndicator == EquipmentStatusIndicator.AMBER &&
                                    equipment.status == EquipmentStatus.OPERATIONAL) {
            EquipmentStatusIndicator.GREEN
        } else {
            equipment.statusIndicator
        }
        
        // Clean up condition description
        val newConditionDescription = equipment.conditionDescription
            .replace("Pump calibration required - discrepancy > 3%", "")
            .replace("; ; ", "; ")
            .replace("^; |; $".toRegex(), "")
            .trim()
        
        return equipment.copy(
            statusIndicator = newStatusIndicator,
            recordedIssues = updatedIssues,
            conditionDescription = newConditionDescription,
            lastModifiedAt = System.currentTimeMillis()
        )
    }
}

