package com.aeci.mmucompanion.domain.usecase

import javax.inject.Inject

class MarkTaskCompleteUseCase @Inject constructor() {
    suspend operator fun invoke(taskId: String): Result<Boolean> {
        // TODO: Implement mark task complete
        return Result.success(true)
    }
}

