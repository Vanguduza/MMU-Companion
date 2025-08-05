package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.ExportFormat
import com.aeci.mmucompanion.domain.repository.FormRepository
import javax.inject.Inject

class ExportFormsUseCase @Inject constructor(
    private val formRepository: FormRepository
) {
    suspend operator fun invoke(formIds: List<String>, format: ExportFormat): Result<String> {
        return formRepository.bulkExportForms(formIds, format)
    }
}

