package com.aeci.mmucompanion.domain.usecase.jobcard

import com.aeci.mmucompanion.domain.model.JobCard
import com.aeci.mmucompanion.domain.repository.JobCardRepository
import javax.inject.Inject

class GetJobCardsByUserUseCase @Inject constructor(
    private val jobCardRepository: JobCardRepository
) {
    suspend operator fun invoke(userId: String): List<JobCard> {
        return jobCardRepository.getJobCardsByUser(userId)
    }
} 
