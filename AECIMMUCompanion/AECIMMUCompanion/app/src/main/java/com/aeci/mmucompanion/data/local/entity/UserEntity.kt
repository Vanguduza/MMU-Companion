package com.aeci.mmucompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val fullName: String,
    val email: String,
    val role: String, // "OPERATOR", "SUPERVISOR", "MAINTENANCE", "ADMIN"
    val department: String,
    val shiftPattern: String,
    val permissions: String, // JSON string of permissions
    val isActive: Boolean,
    val lastLoginAt: Long? = null,
    val createdAt: Long,
    val biometricEnabled: Boolean = false,
    val pinHash: String? = null,
    val passwordHash: String? = null,
    val profileImageUri: String? = null
)
