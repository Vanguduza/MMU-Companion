package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.repository.SystemRepository
import javax.inject.Inject

class GetExportHistoryUseCase @Inject constructor(
    private val systemRepository: SystemRepository
) {
    suspend operator fun invoke(): List<String> {
        // Implement actual export history retrieval
        // return systemRepository.getExportHistory()
        return emptyList()
    }
}

