package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.SystemHealth
import com.aeci.mmucompanion.domain.repository.SystemRepository
import javax.inject.Inject

class GetSystemHealthUseCase @Inject constructor(
    private val systemRepository: SystemRepository
) {
    suspend operator fun invoke(): SystemHealth = systemRepository.getSystemHealth()
}

