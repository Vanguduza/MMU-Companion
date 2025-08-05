package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.User
import com.aeci.mmucompanion.domain.repository.UserRepository
import javax.inject.Inject

class AuthenticateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        return userRepository.authenticateUser(username, password)
    }
}

class AuthenticateWithBiometricUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<User> {
        return userRepository.authenticateWithBiometric(userId)
    }
}

class AuthenticateWithPinUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, pin: String): Result<User> {
        return userRepository.authenticateWithPin(userId, pin)
    }
}

class GetCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): User? {
        return userRepository.getCurrentUser()
    }
}

class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke() {
        userRepository.logout()
    }
}

class HasPermissionUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, permission: String): Boolean {
        return userRepository.hasPermission(userId, permission)
    }
}

