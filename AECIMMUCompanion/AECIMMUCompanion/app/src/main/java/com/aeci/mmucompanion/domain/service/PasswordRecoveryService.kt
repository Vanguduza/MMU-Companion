package com.aeci.mmucompanion.domain.service

import com.aeci.mmucompanion.domain.model.User
import com.aeci.mmucompanion.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class PasswordRecoveryService @Inject constructor(
    private val userRepository: UserRepository,
    private val emailService: EmailService
) {

    suspend fun requestPasswordReset(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Validate email format
            if (!isValidEmail(email)) {
                return@withContext Result.failure(Exception("Invalid email format"))
            }

            // Check if user exists
            val user = userRepository.getUserByEmail(email)
            if (user == null) {
                // For security, don't reveal if email exists or not
                return@withContext Result.success("If this email is registered, you will receive a password reset link")
            }

            // Generate reset token
            val token = generateResetToken()
            val expirationTime = System.currentTimeMillis() + (30 * 60 * 1000) // 30 minutes

            // Save reset token to database
            try {
                userRepository.savePasswordResetToken(
                    userId = user.id,
                    token = token,
                    expiresAt = expirationTime
                )
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }

            // Send reset email
            val emailResult = emailService.sendPasswordResetEmail(
                email = email,
                userFullName = user.fullName,
                resetToken = token
            )

            emailResult.fold(
                onSuccess = { /* email sent successfully */ },
                onFailure = { return@withContext Result.failure(it) }
            )

            Result.success("If this email is registered, you will receive a password reset link")

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun validateResetToken(token: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val user = userRepository.getUserByResetToken(token)
            if (user == null) {
                return@withContext Result.failure(Exception("Invalid or expired reset token"))
            }

            // Check if token is expired
            val tokenData = userRepository.getPasswordResetTokenData(token)
            if (tokenData == null || tokenData.second < System.currentTimeMillis()) {
                return@withContext Result.failure(Exception("Reset token has expired"))
            }

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(token: String, newPassword: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Validate token
            val userResult = validateResetToken(token)
            if (userResult.isFailure) {
                return@withContext Result.failure(userResult.exceptionOrNull() ?: Exception("Invalid token"))
            }

            val user = userResult.getOrNull()!!

            // Validate new password
            val passwordValidation = validatePassword(newPassword)
            if (passwordValidation.isFailure) {
                return@withContext Result.failure(passwordValidation.exceptionOrNull() ?: Exception("Invalid password"))
            }

            // Update password
            val updateResult = userRepository.updatePassword(user.id, newPassword)
            if (updateResult.isFailure) {
                return@withContext Result.failure(updateResult.exceptionOrNull() ?: Exception("Failed to update password"))
            }

            // Invalidate reset token
            userRepository.invalidatePasswordResetToken(token)

            // Send confirmation email
            emailService.sendPasswordChangeConfirmationEmail(
                email = user.email,
                userFullName = user.fullName
            )

            Result.success("Password has been successfully reset")

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(
        userId: String,
        currentPassword: String,
        newPassword: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Verify current password
            val user = userRepository.getUserById(userId)
            if (user == null) {
                return@withContext Result.failure(Exception("User not found"))
            }

            val isCurrentPasswordValid = userRepository.verifyPassword(userId, currentPassword)
            if (!isCurrentPasswordValid) {
                return@withContext Result.failure(Exception("Current password is incorrect"))
            }

            // Validate new password
            val passwordValidation = validatePassword(newPassword)
            if (passwordValidation.isFailure) {
                return@withContext Result.failure(passwordValidation.exceptionOrNull() ?: Exception("Invalid password"))
            }

            // Check if new password is different from current
            if (currentPassword == newPassword) {
                return@withContext Result.failure(Exception("New password must be different from current password"))
            }

            // Update password
            val updateResult = userRepository.updatePassword(userId, newPassword)
            if (updateResult.isFailure) {
                return@withContext Result.failure(updateResult.exceptionOrNull() ?: Exception("Failed to update password"))
            }

            // Mark password as changed (no longer requires change)
            userRepository.markPasswordAsChanged(userId)

            // Send confirmation email
            emailService.sendPasswordChangeConfirmationEmail(
                email = user.email,
                userFullName = user.fullName
            )

            Result.success("Password has been successfully changed")

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun requestAccountRecovery(emailOrEmployeeId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Try to find user by email or employee ID
            val user = userRepository.getUserByEmail(emailOrEmployeeId) 
                ?: userRepository.getUserByEmployeeId(emailOrEmployeeId)

            if (user == null) {
                // For security, don't reveal if account exists or not
                return@withContext Result.success("If this account exists, you will receive recovery instructions")
            }

            // Generate recovery token
            val recoveryToken = generateResetToken()
            val expirationTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // 24 hours

            // Save recovery token to database
            try {
                userRepository.saveAccountRecoveryToken(
                    userId = user.id,
                    token = recoveryToken,
                    expiresAt = expirationTime
                )
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }

            // Send recovery email
            val emailResult = emailService.sendAccountRecoveryEmail(
                email = user.email,
                userFullName = user.fullName,
                username = user.username,
                employeeId = user.username, // Use username as employeeId
                recoveryToken = recoveryToken
            )

            if (emailResult.isFailure) {
                return@withContext Result.failure(emailResult.exceptionOrNull() ?: Exception("Failed to send recovery email"))
            }

            Result.success("If this account exists, you will receive recovery instructions")

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateResetToken(): String {
        val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..32)
            .map { characters[Random.nextInt(characters.length)] }
            .joinToString("")
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(Regex(emailPattern))
    }

    private fun validatePassword(password: String): Result<Unit> {
        when {
            password.length < 8 -> return Result.failure(Exception("Password must be at least 8 characters long"))
            !password.any { it.isUpperCase() } -> return Result.failure(Exception("Password must contain at least one uppercase letter"))
            !password.any { it.isLowerCase() } -> return Result.failure(Exception("Password must contain at least one lowercase letter"))
            !password.any { it.isDigit() } -> return Result.failure(Exception("Password must contain at least one digit"))
            !password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) } -> return Result.failure(Exception("Password must contain at least one special character"))
        }
        return Result.success(Unit)
    }
}

data class PasswordResetTokenData(
    val userId: String,
    val token: String,
    val expirationTime: Long,
    val isUsed: Boolean = false
)

@Singleton
class EmailService @Inject constructor() {

    suspend fun sendPasswordResetEmail(
        email: String,
        userFullName: String,
        resetToken: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // In a real implementation, this would use a proper email service
            // like SendGrid, AWS SES, or SMTP
            
            val resetLink = "https://your-app-domain.com/reset-password?token=$resetToken"
            
            val emailContent = """
                Dear $userFullName,
                
                You have requested to reset your password for your AECI MMU Companion account.
                
                Please click the following link to reset your password:
                $resetLink
                
                This link will expire in 30 minutes.
                
                If you did not request this password reset, please ignore this email.
                
                Best regards,
                AECI MMU Companion Team
            """.trimIndent()

            // TODO: Implement actual email sending
            // For now, just log the email content
            println("Email would be sent to: $email")
            println("Content: $emailContent")

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordChangeConfirmationEmail(
        email: String,
        userFullName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val emailContent = """
                Dear $userFullName,
                
                Your password for AECI MMU Companion has been successfully changed.
                
                If you did not make this change, please contact your administrator immediately.
                
                Best regards,
                AECI MMU Companion Team
            """.trimIndent()

            // TODO: Implement actual email sending
            println("Email would be sent to: $email")
            println("Content: $emailContent")

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendAccountRecoveryEmail(
        email: String,
        userFullName: String,
        username: String,
        employeeId: String,
        recoveryToken: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val recoveryLink = "https://your-app-domain.com/recover-account?token=$recoveryToken"
            
            val emailContent = """
                Dear $userFullName,
                
                You have requested account recovery for your AECI MMU Companion account.
                
                Your account details:
                - Username: $username
                - Employee ID: $employeeId
                - Email: $email
                
                Please click the following link to recover your account:
                $recoveryLink
                
                This link will expire in 24 hours.
                
                If you did not request this account recovery, please ignore this email.
                
                Best regards,
                AECI MMU Companion Team
            """.trimIndent()

            // TODO: Implement actual email sending
            println("Email would be sent to: $email")
            println("Content: $emailContent")

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 