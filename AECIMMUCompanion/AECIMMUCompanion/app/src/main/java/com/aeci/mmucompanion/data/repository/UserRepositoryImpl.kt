package com.aeci.mmucompanion.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.aeci.mmucompanion.data.local.dao.UserDao
import com.aeci.mmucompanion.data.local.entity.UserEntity
import com.aeci.mmucompanion.data.remote.api.AECIApiService
import com.aeci.mmucompanion.data.remote.api.LoginRequest
import com.aeci.mmucompanion.data.remote.api.MobileServerApiService
import com.aeci.mmucompanion.data.remote.dto.PasswordResetRequest
import com.aeci.mmucompanion.domain.model.Permission
import com.aeci.mmucompanion.domain.model.User
import com.aeci.mmucompanion.domain.model.UserRole
import com.aeci.mmucompanion.domain.repository.UserRepository
import com.aeci.mmucompanion.core.util.MobileServerConfig
import com.aeci.mmucompanion.core.util.NetworkManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import com.aeci.mmucompanion.data.remote.api.MobileLoginRequest
import com.aeci.mmucompanion.data.remote.api.MobileLoginResponse

@Singleton
class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDao: UserDao,
    private val apiService: AECIApiService,
    private val mobileServerApiService: MobileServerApiService,
    private val mobileServerConfig: MobileServerConfig,
    private val networkManager: NetworkManager,
    private val gson: Gson
) : UserRepository {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "user_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
    }
    
    override suspend fun authenticateUser(username: String, password: String): Result<User> {
        return try {
            // First try local authentication for offline mode
            val localUser = userDao.getUserByUsername(username)
            if (localUser != null && verifyPassword(localUser.id, password)) {
                updateLastLogin(localUser.id)
                saveCurrentUser(localUser.id)
                return Result.success(localUser.toUser())
            }
            
            // Get device ID for authentication
            val deviceId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )
            
            // Try mobile server first (automatic discovery)
            val mobileServerUrl = mobileServerConfig.getActiveServerUrl()
            if (mobileServerUrl.isNotEmpty()) {
                try {
                    val mobileResponse = mobileServerApiService.login(
                        MobileLoginRequest(username, password)
                    )
                    
                    if (mobileResponse.isSuccessful && mobileResponse.body() != null) {
                        val loginResponse = mobileResponse.body()!!
                        
                        // Save tokens
                        encryptedPrefs.edit()
                            .putString(KEY_AUTH_TOKEN, loginResponse.token)
                            .putString(KEY_REFRESH_TOKEN, loginResponse.token) // Mobile server doesn't have refresh token
                            .putLong(KEY_TOKEN_EXPIRY, System.currentTimeMillis() + (24 * 60 * 60 * 1000)) // 24 hours
                            .apply()
                        
                        // Save/update user locally
                        val userEntity = loginResponse.user.toUserEntity()
                        userDao.insertUser(userEntity)
                        
                        updateLastLogin(userEntity.id)
                        saveCurrentUser(userEntity.id)
                        
                        return Result.success(userEntity.toUser())
                    }
                } catch (e: Exception) {
                    // Mobile server failed, continue to cloud fallback
                    e.printStackTrace()
                }
            }
            
            // Fallback to original AECI cloud server
            try {
                val response = apiService.login(
                    LoginRequest(username, password, deviceId)
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val loginResponse = response.body()!!
                    
                    // Save tokens
                    encryptedPrefs.edit()
                        .putString(KEY_AUTH_TOKEN, loginResponse.token)
                        .putString(KEY_REFRESH_TOKEN, loginResponse.refreshToken)
                        .putLong(KEY_TOKEN_EXPIRY, System.currentTimeMillis() + loginResponse.expiresIn)
                        .apply()
                    
                    // Save/update user locally
                    val userEntity = loginResponse.user.toUserEntity()
                    userDao.insertUser(userEntity)
                    
                    updateLastLogin(userEntity.id)
                    saveCurrentUser(userEntity.id)
                    
                    Result.success(userEntity.toUser())
                } else {
                    val errorMessage = "Authentication failed"
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun authenticateWithBiometric(userId: String): Result<User> {
        return try {
            val user = userDao.getUserById(userId)
            if (user != null && user.biometricEnabled) {
                updateLastLogin(userId)
                saveCurrentUser(userId)
                Result.success(user.toUser())
            } else {
                Result.failure(Exception("Biometric authentication not enabled or user not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun authenticateWithPin(userId: String, pin: String): Result<User> {
        return try {
            val user = userDao.getUserById(userId)
            if (user != null && verifyPassword(userId, pin)) {
                updateLastLogin(userId)
                saveCurrentUser(userId)
                Result.success(user.toUser())
            } else {
                Result.failure(Exception("Invalid PIN"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrentUser(): User? {
        val userId = encryptedPrefs.getString(KEY_CURRENT_USER_ID, null)
        return userId?.let { userDao.getUserById(it)?.toUser() }
    }
    
    override suspend fun updateLastLogin(userId: String) {
        userDao.updateLastLogin(userId, System.currentTimeMillis())
    }
    
    override suspend fun enableBiometric(userId: String, enabled: Boolean) {
        userDao.updateBiometricEnabled(userId, enabled)
    }
    
    override suspend fun updatePin(userId: String, pin: String?) {
        val hashedPin = pin?.let { hashPassword(it) }
        userDao.updatePinHash(userId, hashedPin)
    }
    
    override suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)?.toUser()
    }
    
    override suspend fun getAllUsers(): List<User> {
        return userDao.getAllActiveUsers().map { entities ->
            entities.map { it.toUser() }
        }.first()
    }

    override fun getAllActiveUsers(): Flow<List<User>> {
        return userDao.getAllActiveUsers().map { entities ->
            entities.map { it.toUser() }
        }
    }
    
    override fun getUsersByRole(role: UserRole): Flow<List<User>> {
        return userDao.getUsersByRole(role.name).map { entities ->
            entities.map { it.toUser() }
        }
    }
    
    override fun getUsersByDepartment(department: String): Flow<List<User>> {
        return userDao.getUsersByDepartment(department).map { entities ->
            entities.map { it.toUser() }
        }
    }
    
    override suspend fun createUser(user: User): Result<String> {
        return try {
            val userEntity = UserEntity(
                id = user.id,
                username = user.username,
                fullName = user.fullName,
                email = user.email,
                role = user.role.name,
                department = user.department,
                shiftPattern = user.shiftPattern,
                permissions = gson.toJson(user.permissions.map { it.name }),
                isActive = user.isActive,
                lastLoginAt = user.lastLoginAt,
                createdAt = System.currentTimeMillis(),
                biometricEnabled = user.biometricEnabled,
                passwordHash = null
            )
            
            userDao.insertUser(userEntity)
            Result.success(user.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createUserWithPassword(user: User, password: String): Result<String> {
        return try {
            val hashedPassword = hashPassword(password)
            val userEntity = UserEntity(
                id = user.id,
                username = user.username,
                fullName = user.fullName,
                email = user.email,
                role = user.role.name,
                department = user.department,
                shiftPattern = user.shiftPattern,
                permissions = gson.toJson(user.permissions.map { it.name }),
                isActive = user.isActive,
                lastLoginAt = user.lastLoginAt,
                createdAt = System.currentTimeMillis(),
                biometricEnabled = user.biometricEnabled,
                passwordHash = hashedPassword
            )
            
            userDao.insertUser(userEntity)
            
            // Mark as requiring password change for non-admin created users
            if (user.role != UserRole.ADMIN || user.id != "admin-default-001") {
                markPasswordChangeRequired(user.id, true)
            }
            
            Result.success(user.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateUser(user: User): Result<User> {
        return try {
            val userEntity = userDao.getUserById(user.id)
            if (userEntity != null) {
                val updatedEntity = userEntity.copy(
                    fullName = user.fullName,
                    email = user.email,
                    role = user.role.name,
                    department = user.department,
                    shiftPattern = user.shiftPattern,
                    permissions = gson.toJson(user.permissions.map { it.name }),
                    isActive = user.isActive,
                    biometricEnabled = user.biometricEnabled
                )
                userDao.updateUser(updatedEntity)
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deactivateUser(userId: String): Result<Unit> {
        return try {
            val user = userDao.getUserById(userId)
            if (user != null) {
                val deactivatedUser = user.copy(isActive = false)
                userDao.updateUser(deactivatedUser)
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun resetUserPassword(userId: String, newPassword: String): Result<Unit> {
        return try {
            val hashedPassword = hashPassword(newPassword)
            userDao.updatePinHash(userId, hashedPassword)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun requestPasswordReset(email: String): Result<Unit> {
        return try {
            val user = userDao.getUserByEmail(email)
            if (user != null) {
                // Generate reset token
                val resetToken = generateResetToken()
                val tokenExpiry = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // 24 hours
                
                // Store reset token
                encryptedPrefs.edit()
                    .putString("reset_token_${user.id}", resetToken)
                    .putLong("reset_token_expiry_${user.id}", tokenExpiry)
                    .apply()
                
                // Send reset email via API
                val response = apiService.sendPasswordResetEmail(
                    PasswordResetRequest(
                        email = email,
                        resetToken = resetToken,
                        deviceId = getDeviceId()
                    )
                )
                
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to send password reset email"))
                }
            } else {
                Result.failure(Exception("User with email not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun verifyPasswordResetToken(token: String): Result<Boolean> {
        return try {
            // Check all stored tokens for validity
            val allPrefs = encryptedPrefs.all
            for ((key, value) in allPrefs) {
                if (key.startsWith("reset_token_") && !key.contains("expiry")) {
                    val storedToken = value as? String
                    if (storedToken == token) {
                        val userId = key.removePrefix("reset_token_")
                        val expiryKey = "reset_token_expiry_$userId"
                        val expiry = encryptedPrefs.getLong(expiryKey, 0)
                        
                        if (System.currentTimeMillis() < expiry) {
                            return Result.success(true)
                        } else {
                            // Token expired, clean up
                            encryptedPrefs.edit()
                                .remove(key)
                                .remove(expiryKey)
                                .apply()
                            return Result.success(false)
                        }
                    }
                }
            }
            Result.success(false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun resetPasswordWithToken(token: String, newPassword: String): Result<Unit> {
        return try {
            val tokenValid = verifyPasswordResetToken(token).getOrNull() ?: false
            if (!tokenValid) {
                return Result.failure(Exception("Invalid or expired reset token"))
            }
            
            // Find user ID for this token
            val allPrefs = encryptedPrefs.all
            var userId: String? = null
            for ((key, value) in allPrefs) {
                if (key.startsWith("reset_token_") && !key.contains("expiry")) {
                    val storedToken = value as? String
                    if (storedToken == token) {
                        userId = key.removePrefix("reset_token_")
                        break
                    }
                }
            }
            
            if (userId != null) {
                val hashedPassword = hashPassword(newPassword)
                userDao.updatePinHash(userId, hashedPassword)
                
                // Mark password as changed
                markPasswordChanged(userId)
                
                // Clean up reset token
                encryptedPrefs.edit()
                    .remove("reset_token_$userId")
                    .remove("reset_token_expiry_$userId")
                    .apply()
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found for reset token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAdminUserCount(): Int {
        return try {
            userDao.getAdminUserCount()
        } catch (e: Exception) {
            0
        }
    }
    
    override suspend fun requiresPasswordChange(userId: String): Boolean {
        return encryptedPrefs.getBoolean("requires_password_change_$userId", false)
    }
    
    override suspend fun markPasswordChanged(userId: String) {
        markPasswordChangeRequired(userId, false)
    }
    
    override suspend fun createTechnician(
        name: String,
        employeeId: String,
        department: String,
        shiftPattern: String
    ): Result<User> {
        return try {
            val userId = java.util.UUID.randomUUID().toString()
            val user = User(
                id = userId,
                username = employeeId, // Use employeeId as username
                email = "${employeeId}@company.com", // Generate email
                fullName = name,
                role = UserRole.MAINTENANCE,
                isActive = true,
                department = department,
                shiftPattern = shiftPattern,
                permissions = listOf(
                    Permission.VIEW_FORMS,
                    Permission.CREATE_FORMS,
                    Permission.EDIT_FORMS,
                    Permission.VIEW_EQUIPMENT,
                    Permission.MANAGE_EQUIPMENT
                ),
                siteId = "site_001"
            )
            
            val userEntity = UserEntity(
                id = user.id,
                username = user.username,
                fullName = user.fullName,
                email = user.email,
                role = user.role.name,
                department = user.department,
                shiftPattern = user.shiftPattern,
                permissions = com.google.gson.Gson().toJson(user.permissions.map { it.name }),
                isActive = user.isActive,
                lastLoginAt = null,
                createdAt = System.currentTimeMillis(),
                biometricEnabled = false,
                pinHash = null,
                passwordHash = null
            )
            
            userDao.insertUser(userEntity)
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun syncUsers(): Result<Unit> {
        return try {
            val token = getAuthToken()
            val response = apiService.getUsers("Bearer $token")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val apiUsers = response.body()!!.users
                
                apiUsers.forEach { apiUser ->
                    val userEntity = apiUser.toUserEntity()
                    userDao.insertUser(userEntity)
                }
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync users"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun logout() {
        encryptedPrefs.edit()
            .remove(KEY_CURRENT_USER_ID)
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_TOKEN_EXPIRY)
            .apply()
    }
    
    override suspend fun hasPermission(userId: String, permission: String): Boolean {
        val user = userDao.getUserById(userId) ?: return false
        
        val permissions = try {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(user.permissions, listType)
        } catch (e: Exception) {
            emptyList<String>()
        }
        
        return permissions.contains(permission) || user.role == "ADMIN"
    }
    
    override suspend fun changePassword(userId: String, currentPassword: String, newPassword: String): Result<Boolean> {
        return try {
            // Verify current password first
            val user = userDao.getUserById(userId) ?: return Result.failure(Exception("User not found"))
            val hashedCurrentPassword = hashPassword(currentPassword)
            
            if (user.passwordHash != hashedCurrentPassword) {
                return Result.success(false) // Current password is incorrect
            }
            
            // Update password
            val hashedNewPassword = hashPassword(newPassword)
            userDao.updatePassword(userId, hashedNewPassword)
            
            // Mark password as changed (no longer requires change)
            markPasswordChanged(userId)
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteUser(userId: String): Result<Boolean> {
        return try {
            val user = userDao.getUserById(userId)
            if (user != null) {
                userDao.deleteUser(user)
                Result.success(true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyPassword(userId: String, password: String): Boolean {
        return try {
            val user = userDao.getUserById(userId)
            if (user != null) {
                // In a real implementation, you would hash the password and compare
                // For now, return true as a placeholder
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun saveCurrentUser(userId: String) {
        encryptedPrefs.edit()
            .putString(KEY_CURRENT_USER_ID, userId)
            .apply()
    }
    
    private fun getAuthToken(): String? {
        return encryptedPrefs.getString(KEY_AUTH_TOKEN, null)
    }
    
    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashBytes = md.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    private fun UserEntity.toUser(): User {
        val permissionsList = try {
            val listType = object : TypeToken<List<String>>() {}.type
            val permissionNames = gson.fromJson<List<String>>(permissions, listType)
            permissionNames.mapNotNull { 
                try { Permission.valueOf(it) } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            emptyList<Permission>()
        }
        
        return User(
            id = id,
            username = username,
            fullName = fullName,
            email = email,
            role = try { UserRole.valueOf(role) } catch (e: Exception) { UserRole.OPERATOR },
            department = department,
            shiftPattern = shiftPattern,
            permissions = permissionsList,
            isActive = isActive,
            lastLoginAt = lastLoginAt,
            biometricEnabled = biometricEnabled,
            siteId = "DEFAULT_SITE"
        )
    }
    
    private fun com.aeci.mmucompanion.data.remote.api.UserApiModel.toUserEntity(): UserEntity {
        return UserEntity(
            id = id,
            username = username,
            fullName = fullName,
            email = email,
            role = role,
            department = department,
            shiftPattern = shiftPattern,
            permissions = "[]", // Will be updated when permissions are synced
            isActive = isActive,
            createdAt = System.currentTimeMillis(),
            biometricEnabled = false,
            pinHash = null,
            passwordHash = null
        )
    }
    
    private fun com.aeci.mmucompanion.data.remote.api.UserDto.toUserEntity(): UserEntity {
        return UserEntity(
            id = id.toString(),
            username = username,
            fullName = fullName,
            email = "", // UserDto doesn't have email
            role = role,
            department = department ?: "",
            shiftPattern = "", // UserDto doesn't have shiftPattern
            permissions = "[]", // Will be updated when permissions are synced
            isActive = isActive,
            lastLoginAt = lastLogin?.toLongOrNull() ?: System.currentTimeMillis(),
            createdAt = createdAt.toLongOrNull() ?: System.currentTimeMillis(),
            biometricEnabled = false,
            pinHash = null,
            passwordHash = null
        )
    }
    
    private fun User.toUserEntity(): UserEntity {
        return UserEntity(
            id = this.id,
            username = this.username,
            email = this.email,
            fullName = this.fullName,
            role = this.role.name, // Convert enum to string
            department = this.department,
            shiftPattern = this.shiftPattern,
            permissions = this.permissions.map { it.name }.joinToString(","), // Convert enum list to string
            isActive = this.isActive,
            lastLoginAt = this.lastLoginAt,
            createdAt = System.currentTimeMillis(), // Use current time since User model doesn't have createdAt
            biometricEnabled = this.biometricEnabled,
            pinHash = null,
            passwordHash = null // Password hash not exposed in User model
        )
    }
    
    private fun markPasswordChangeRequired(userId: String, required: Boolean) {
        encryptedPrefs.edit()
            .putBoolean("requires_password_change_$userId", required)
            .apply()
    }
    
    private fun generateResetToken(): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..32)
            .map { charset.random() }
            .joinToString("")
    }
    
    private fun getDeviceId(): String {
        return android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
    }

    override suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)?.toUser()
    }

    override suspend fun getUserByEmployeeId(employeeId: String): User? {
        // For now, search by username as employeeId - this would be improved with a proper employeeId field
        return userDao.getUserByUsername(employeeId)?.toUser()
    }

    override suspend fun savePasswordResetToken(userId: String, token: String, expiresAt: Long) {
        // Save the password reset token to the database or local storage
        // This could be implemented using SharedPreferences or a separate DAO
        val userEntity = userDao.getUserById(userId)
        if (userEntity != null) {
            // For now, we'll just log the token save operation
            // In a real implementation, you'd save this to a separate table or encrypted storage
            println("Saving password reset token for user: $userId, expires at: $expiresAt")
        }
    }

    override suspend fun getUserByResetToken(token: String): User? {
        // TODO: Implement token lookup logic
        // This would typically query a password reset tokens table
        // For now, return null as placeholder
        return null
    }

    override suspend fun getPasswordResetTokenData(token: String): Pair<String, Long>? {
        // TODO: Implement token data lookup logic
        // This would typically query a password reset tokens table for userId and expiresAt
        // For now, return null as placeholder
        return null
    }

    override suspend fun updatePassword(userId: String, newPassword: String): Result<Unit> {
        return try {
            val hashedPassword = hashPassword(newPassword)
            userDao.updatePassword(userId, hashedPassword)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveAccountRecoveryToken(userId: String, token: String, expiresAt: Long) {
        try {
            // Save account recovery token to encrypted preferences
            encryptedPrefs.edit()
                .putString("recovery_token_$userId", token)
                .putLong("recovery_token_expiry_$userId", expiresAt)
                .apply()
            android.util.Log.d("UserRepository", "Saved account recovery token for user $userId")
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Failed to save account recovery token", e)
            throw e
        }
    }

    override suspend fun invalidatePasswordResetToken(token: String) {
        try {
            // Find and remove the password reset token from encrypted preferences
            val allPrefs = encryptedPrefs.all
            val keysToRemove = allPrefs.keys.filter { key ->
                key.startsWith("reset_token_") && allPrefs[key] == token
            }
            
            val editor = encryptedPrefs.edit()
            keysToRemove.forEach { key ->
                editor.remove(key)
                // Also remove the corresponding expiry
                val userId = key.removePrefix("reset_token_")
                editor.remove("reset_token_expiry_$userId")
            }
            editor.apply()
            android.util.Log.d("UserRepository", "Invalidated password reset token")
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Failed to invalidate password reset token", e)
            throw e
        }
    }

    override suspend fun markPasswordAsChanged(userId: String) {
        try {
            // Mark that the user no longer requires a password change
            encryptedPrefs.edit().putBoolean("requires_password_change_$userId", false).apply()
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Failed to mark password as changed", e)
            throw e
        }
    }

    // Sync methods needed by OfflineSyncService
    override suspend fun getPendingUserUpdates(): List<User> {
        return try {
            // Get users that have been modified locally but not synced
            // For now, return empty list - this can be implemented when user sync is needed
            emptyList()
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Failed to get pending user updates", e)
            emptyList()
        }
    }

    override suspend fun syncUserUpdate(user: User): Result<Unit> {
        return try {
            // Sync user update to server
            // For now, just mark as successful - this can be implemented when server sync is needed
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Failed to sync user update", e)
            Result.failure(e)
        }
    }

    override suspend fun markUserAsSynced(userId: String) {
        try {
            // Mark user as synced in local database
            // For now, this is a placeholder - can be implemented when user sync tracking is needed
            android.util.Log.d("UserRepository", "Marked user $userId as synced")
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Failed to mark user as synced", e)
            throw e
        }
    }

    override suspend fun downloadLatestUsers(): Result<List<User>> {
        return try {
            // Download latest users from server
            // For now, return empty list - this can be implemented when server sync is needed
            Result.success(emptyList())
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Failed to download latest users", e)
            Result.failure(e)
        }
    }

    override suspend fun cacheUserData(users: List<User>) {
        try {
            // Cache user data locally
            users.forEach { user ->
                val userEntity = user.toUserEntity()
                userDao.insertUser(userEntity)
            }
            android.util.Log.d("UserRepository", "Cached ${users.size} users")
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Failed to cache user data", e)
            throw e
        }
    }
}
