package com.aeci.mmucompanion.domain.model.forms

import java.time.LocalDateTime

data class MmuHandoverCertificate(
    val id: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val siteLocation: String = "",
    val handoverOperator: String = "",
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val status: String = "Draft",
    
    // MMU Handover Certificate specific fields
    val mmuSerialNumber: String = "",
    val receivingOperator: String = "",
    val handoverDate: LocalDateTime? = null,
    val mmuCondition: String = "",
    val fuelLevel: Double = 0.0,
    val oilLevel: String = "",
    val coolantLevel: String = "",
    val hydraulicFluid: String = "",
    val batteryCondition: String = "",
    val tiresCondition: String = "",
    val lightsOperational: Boolean = false,
    val warningDevices: Boolean = false,
    val emergencyStop: Boolean = false,
    val fireExtinguisher: Boolean = false,
    val toolsInventory: List<String> = emptyList(),
    val documentationComplete: Boolean = false,
    val defectsNoted: List<String> = emptyList(),
    val handoverNotes: String = "",
    val receiverSignature: String = "",
    val handoverSignature: String = ""
)
