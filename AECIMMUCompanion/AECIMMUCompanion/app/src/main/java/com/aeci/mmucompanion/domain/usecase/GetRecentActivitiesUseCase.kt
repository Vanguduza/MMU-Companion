package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.SystemActivity
import com.aeci.mmucompanion.domain.repository.SystemRepository
import javax.inject.Inject

class GetRecentActivitiesUseCase @Inject constructor(
    private val systemRepository: SystemRepository
) {
    suspend operator fun invoke(): List<SystemActivity> = systemRepository.getRecentActivities()
}

