package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.repository.UserRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, currentPassword: String, newPassword: String): Result<Boolean> {
        return userRepository.changePassword(userId, currentPassword, newPassword)
    }
}

