package com.aeci.mmucompanion.domain.usecase.timetracking

import com.aeci.mmucompanion.domain.model.TaskTimeEntry
import com.aeci.mmucompanion.domain.repository.TimeTrackingRepository
import javax.inject.Inject

class StartTimeTrackingUseCase @Inject constructor(
    private val timeTrackingRepository: TimeTrackingRepository
) {
    suspend operator fun invoke(
        userId: String,
        jobCode: String,
        description: String,
        todoId: String? = null,
        jobCardId: String? = null
    ): Result<TaskTimeEntry> {
        return try {
            val timeEntry = timeTrackingRepository.startTimeTracking(
                userId = userId,
                jobCode = jobCode,
                description = description,
                todoId = todoId,
                jobCardId = jobCardId
            )
            Result.success(timeEntry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
