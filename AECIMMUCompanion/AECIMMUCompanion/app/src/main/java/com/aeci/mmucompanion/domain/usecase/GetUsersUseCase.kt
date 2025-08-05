package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.User
import com.aeci.mmucompanion.domain.repository.UserRepository
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): List<User> = userRepository.getAllUsers()
}

