package com.aeci.mmucompanion.domain.repository

import com.aeci.mmucompanion.domain.model.User
import com.aeci.mmucompanion.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun authenticateUser(username: String, password: String): Result<User>
    
    suspend fun getAllUsers(): List<User>
    
    suspend fun authenticateWithBiometric(userId: String): Result<User>
    
    suspend fun authenticateWithPin(userId: String, pin: String): Result<User>
    
    suspend fun getCurrentUser(): User?
    
    suspend fun updateLastLogin(userId: String)
    
    suspend fun enableBiometric(userId: String, enabled: Boolean)
    
    suspend fun updatePin(userId: String, pin: String?)
    
    suspend fun getUserById(userId: String): User?
    
    fun getAllActiveUsers(): Flow<List<User>>
    
    fun getUsersByRole(role: UserRole): Flow<List<User>>
    
    fun getUsersByDepartment(department: String): Flow<List<User>>
    
    // User profile management
    suspend fun updateUser(user: User): Result<User>
    
    suspend fun deleteUser(userId: String): Result<Boolean>
    
    suspend fun createTechnician(
        name: String, 
        employeeId: String, 
        department: String, 
        shiftPattern: String
    ): Result<User>
    
    // Password recovery methods
    suspend fun getUserByEmail(email: String): User?
    
    suspend fun getUserByEmployeeId(employeeId: String): User?
    
    suspend fun savePasswordResetToken(userId: String, token: String, expiresAt: Long)
    
    suspend fun getUserByResetToken(token: String): User?
    
    suspend fun getPasswordResetTokenData(token: String): Pair<String, Long>? // userId, expiresAt
    
    suspend fun updatePassword(userId: String, newPassword: String): Result<Unit>
    
    suspend fun invalidatePasswordResetToken(token: String)
    
    suspend fun verifyPassword(userId: String, password: String): Boolean
    
    suspend fun markPasswordAsChanged(userId: String)

    // Sync operations for offline support
    suspend fun getPendingUserUpdates(): List<User>
    suspend fun syncUserUpdate(user: User): Result<Unit>
    suspend fun markUserAsSynced(userId: String)
    suspend fun downloadLatestUsers(): Result<List<User>>
    suspend fun cacheUserData(users: List<User>)
    
    suspend fun saveAccountRecoveryToken(userId: String, token: String, expiresAt: Long)
    
    suspend fun createUser(user: User): Result<String>
    
    suspend fun createUserWithPassword(user: User, password: String): Result<String>
    
    suspend fun deactivateUser(userId: String): Result<Unit>
    
    suspend fun resetUserPassword(userId: String, newPassword: String): Result<Unit>
    
    suspend fun requestPasswordReset(email: String): Result<Unit>
    
    suspend fun verifyPasswordResetToken(token: String): Result<Boolean>
    
    suspend fun resetPasswordWithToken(token: String, newPassword: String): Result<Unit>
    
    suspend fun getAdminUserCount(): Int
    
    suspend fun requiresPasswordChange(userId: String): Boolean
    
    suspend fun markPasswordChanged(userId: String)
    
    suspend fun syncUsers(): Result<Unit>
    
    suspend fun logout()
    
    suspend fun hasPermission(userId: String, permission: String): Boolean
    
    suspend fun changePassword(userId: String, currentPassword: String, newPassword: String): Result<Boolean>
}
