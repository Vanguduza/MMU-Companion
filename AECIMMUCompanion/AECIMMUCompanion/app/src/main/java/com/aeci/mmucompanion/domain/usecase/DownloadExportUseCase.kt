package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.repository.SystemRepository
import javax.inject.Inject

class DownloadExportUseCase @Inject constructor(
    private val systemRepository: SystemRepository
) {
    suspend operator fun invoke(fileName: String): Result<String> {
        // Implement actual download logic
        // return systemRepository.downloadExport(fileName)
        return Result.success("/downloads/$fileName")
    }
}

