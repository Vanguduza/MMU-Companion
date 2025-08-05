package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.SystemStats
import com.aeci.mmucompanion.domain.repository.SystemRepository
import javax.inject.Inject

class GetSystemStatsUseCase @Inject constructor(
    private val systemRepository: SystemRepository
) {
    suspend operator fun invoke(): SystemStats = systemRepository.getSystemStats()
}

