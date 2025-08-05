package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.repository.UserRepository
import javax.inject.Inject

class UpdateBiometricSettingUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, enabled: Boolean): Result<Boolean> {
        userRepository.enableBiometric(userId, enabled)
        return Result.success(enabled)
    }
}

