package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.ExportFormat
import com.aeci.mmucompanion.domain.repository.UserRepository
import javax.inject.Inject

class ExportUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userIds: List<String>, format: ExportFormat): Result<String> {
        // Implement actual export logic, e.g., call a repository method
        // return userRepository.bulkExportUsers(userIds, format)
        return Result.success("export_users.pdf")
    }
}

