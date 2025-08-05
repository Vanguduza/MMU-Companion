package com.aeci.mmucompanion.domain.usecase.timetracking

import com.aeci.mmucompanion.domain.model.TimeEntry
import com.aeci.mmucompanion.domain.repository.TodoRepository
import javax.inject.Inject

class StartTodoTimeTrackingUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    suspend operator fun invoke(todoId: String, userId: String): Result<TimeEntry> {
        return try {
            val timeEntry = todoRepository.startTimeTracking(todoId, userId)
            Result.success(timeEntry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
