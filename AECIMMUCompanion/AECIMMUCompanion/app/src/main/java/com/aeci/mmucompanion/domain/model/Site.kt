package com.aeci.mmucompanion.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sites")
data class Site(
    @PrimaryKey
    val id: String,
    val name: String,
    val code: String,
    val address: String,
    val city: String,
    val province: String,
    val country: String,
    val postalCode: String,
    val contactPerson: String,
    val contactEmail: String,
    val contactPhone: String,
    val isActive: Boolean = true,
    val isHeadOffice: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// For site selection and management
data class SiteWithStats(
    val site: Site,
    val equipmentCount: Int = 0,
    val technicianCount: Int = 0,
    val activeJobCards: Int = 0
)
