package com.aeci.mmucompanion.data.local.dao

import androidx.room.*
import com.aeci.mmucompanion.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username AND isActive = 1")
    suspend fun getUserByUsername(username: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email AND isActive = 1")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE isActive = 1 ORDER BY fullName ASC")
    fun getAllActiveUsers(): Flow<List<UserEntity>>
    
    @Query("SELECT * FROM users WHERE role = :role AND isActive = 1")
    fun getUsersByRole(role: String): Flow<List<UserEntity>>
    
    @Query("SELECT * FROM users WHERE department = :department AND isActive = 1")
    fun getUsersByDepartment(department: String): Flow<List<UserEntity>>
    
    @Query("SELECT COUNT(*) FROM users WHERE role = :role AND isActive = 1")
    suspend fun getUserCountByRole(role: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("UPDATE users SET lastLoginAt = :lastLoginAt WHERE id = :id")
    suspend fun updateLastLogin(id: String, lastLoginAt: Long)
    
    @Query("UPDATE users SET biometricEnabled = :enabled WHERE id = :id")
    suspend fun updateBiometricEnabled(id: String, enabled: Boolean)
    
    @Query("UPDATE users SET pinHash = :pinHash WHERE id = :id")
    suspend fun updatePinHash(id: String, pinHash: String?)
    
    @Query("SELECT COUNT(*) FROM users WHERE isActive = 1")
    suspend fun getActiveUserCount(): Int
    
    @Query("UPDATE users SET passwordHash = :passwordHash WHERE id = :id")
    suspend fun updatePassword(id: String, passwordHash: String)
    
    @Query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND isActive = 1")
    suspend fun getAdminUserCount(): Int
}
