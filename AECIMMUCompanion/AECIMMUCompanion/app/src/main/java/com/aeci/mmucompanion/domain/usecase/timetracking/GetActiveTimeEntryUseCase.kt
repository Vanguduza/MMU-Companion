package com.aeci.mmucompanion.domain.usecase.timetracking

import com.aeci.mmucompanion.domain.model.TaskTimeEntry
import com.aeci.mmucompanion.domain.repository.TimeTrackingRepository
import javax.inject.Inject

class GetActiveTimeEntryUseCase @Inject constructor(
    private val timeTrackingRepository: TimeTrackingRepository
) {
    suspend operator fun invoke(userId: String): Result<TaskTimeEntry?> {
        return try {
            val activeEntry = timeTrackingRepository.getActiveTimeEntry(userId)
            Result.success(activeEntry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
