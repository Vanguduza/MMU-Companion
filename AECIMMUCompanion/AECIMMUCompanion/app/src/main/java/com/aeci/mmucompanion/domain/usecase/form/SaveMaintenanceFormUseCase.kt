package com.aeci.mmucompanion.domain.usecase.form

import com.aeci.mmucompanion.domain.model.MaintenanceReportForm
import com.aeci.mmucompanion.domain.repository.FormRepository
import javax.inject.Inject

class SaveMaintenanceFormUseCase @Inject constructor(
    private val formRepository: FormRepository
) {
    suspend operator fun invoke(form: MaintenanceReportForm): MaintenanceReportForm {
        return formRepository.saveMaintenanceForm(form)
    }
} 
