package com.aeci.mmucompanion.domain.usecase.timetracking

import com.aeci.mmucompanion.domain.model.TaskTimeEntry
import com.aeci.mmucompanion.domain.repository.TimeTrackingRepository
import java.time.LocalDate
import javax.inject.Inject

class GetTimesheetUseCase @Inject constructor(
    private val timeTrackingRepository: TimeTrackingRepository
) {
    suspend operator fun invoke(userId: String, date: LocalDate): Result<List<TaskTimeEntry>> {
        return try {
            val timesheet = timeTrackingRepository.getTimesheetForDate(userId, date)
            Result.success(timesheet)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getWeekly(userId: String, weekStartDate: LocalDate): Result<List<TaskTimeEntry>> {
        return try {
            val timesheet = timeTrackingRepository.getTimesheetForWeek(userId, weekStartDate)
            Result.success(timesheet)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMonthly(userId: String, year: Int, month: Int): Result<List<TaskTimeEntry>> {
        return try {
            val timesheet = timeTrackingRepository.getTimesheetForMonth(userId, year, month)
            Result.success(timesheet)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
