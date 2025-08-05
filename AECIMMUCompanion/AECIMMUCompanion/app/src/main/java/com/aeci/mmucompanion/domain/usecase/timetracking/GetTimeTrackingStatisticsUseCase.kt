package com.aeci.mmucompanion.domain.usecase.timetracking

import com.aeci.mmucompanion.domain.model.TimeTrackingStatistics
import com.aeci.mmucompanion.domain.repository.TimeTrackingRepository
import java.time.LocalDate
import javax.inject.Inject

class GetTimeTrackingStatisticsUseCase @Inject constructor(
    private val timeTrackingRepository: TimeTrackingRepository
) {
    suspend operator fun invoke(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<TimeTrackingStatistics> {
        return try {
            val statistics = timeTrackingRepository.getTimeTrackingStatistics(userId, startDate, endDate)
            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
