package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.User
import com.aeci.mmucompanion.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<User> {
        return userRepository.updateUser(user).map { user }
    }
}

