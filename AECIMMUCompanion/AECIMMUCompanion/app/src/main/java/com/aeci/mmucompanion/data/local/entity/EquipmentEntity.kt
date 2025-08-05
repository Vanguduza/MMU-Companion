package com.aeci.mmucompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipment")
data class EquipmentEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: String, // "MMU", "PUMP", "CRUSHER", etc.
    val model: String,
    val serialNumber: String,
    val location: String,
    val siteId: String, // Site where equipment is located
    val status: String, // "OPERATIONAL", "MAINTENANCE", "BREAKDOWN", "OFFLINE"
    val statusIndicator: String = "GREEN", // "GREEN", "AMBER", "RED"
    val conditionDescription: String = "",
    val manufacturer: String,
    val installationDate: Long,
    val lastMaintenanceDate: Long? = null,
    val nextMaintenanceDate: Long? = null,
    val specifications: String, // JSON string of technical specs
    val operatingParameters: String, // JSON string of operating parameters
    val isActive: Boolean = true,
    val lastModifiedBy: String? = null,
    val lastModifiedAt: Long? = null,
    val imageUri: String? = null, // Equipment image
    val conditionImageUri: String? = null, // Current condition evidence photo
    val recordedIssues: String = "", // JSON string of recorded issues
    val createdAt: Long,
    val updatedAt: Long
)
