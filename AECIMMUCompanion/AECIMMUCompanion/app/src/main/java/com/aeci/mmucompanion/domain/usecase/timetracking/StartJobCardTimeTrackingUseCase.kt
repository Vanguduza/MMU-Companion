package com.aeci.mmucompanion.domain.usecase.timetracking

import com.aeci.mmucompanion.domain.model.TimeEntry
import com.aeci.mmucompanion.domain.repository.JobCardRepository
import javax.inject.Inject

class StartJobCardTimeTrackingUseCase @Inject constructor(
    private val jobCardRepository: JobCardRepository
) {
    suspend operator fun invoke(jobCardId: String, userId: String): Result<TimeEntry> {
        return try {
            val timeEntry = jobCardRepository.startTimeTracking(jobCardId, userId)
            Result.success(timeEntry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
