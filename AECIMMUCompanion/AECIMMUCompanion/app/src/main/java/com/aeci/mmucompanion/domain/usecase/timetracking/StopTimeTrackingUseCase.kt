package com.aeci.mmucompanion.domain.usecase.timetracking

import com.aeci.mmucompanion.domain.model.TaskTimeEntry
import com.aeci.mmucompanion.domain.repository.TimeTrackingRepository
import javax.inject.Inject

class StopTimeTrackingUseCase @Inject constructor(
    private val timeTrackingRepository: TimeTrackingRepository
) {
    suspend operator fun invoke(timeEntryId: String): Result<TaskTimeEntry?> {
        return try {
            val timeEntry = timeTrackingRepository.stopTimeTracking(timeEntryId)
            Result.success(timeEntry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
