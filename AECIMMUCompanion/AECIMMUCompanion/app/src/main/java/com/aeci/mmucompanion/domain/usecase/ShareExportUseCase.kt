package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.repository.SystemRepository
import javax.inject.Inject

class ShareExportUseCase @Inject constructor(
    private val systemRepository: SystemRepository
) {
    suspend operator fun invoke(fileName: String): Result<Boolean> {
        // Implement actual share logic
        // return systemRepository.shareExport(fileName)
        return Result.success(true)
    }
}

