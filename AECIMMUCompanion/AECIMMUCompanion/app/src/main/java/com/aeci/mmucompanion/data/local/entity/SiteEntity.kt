package com.aeci.mmucompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "sites",
    indices = [
        Index(value = ["code"], unique = true),
        Index(value = ["isHeadOffice"]),
        Index(value = ["isActive"])
    ]
)
data class SiteEntity(
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
    val isActive: Boolean,
    val isHeadOffice: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
