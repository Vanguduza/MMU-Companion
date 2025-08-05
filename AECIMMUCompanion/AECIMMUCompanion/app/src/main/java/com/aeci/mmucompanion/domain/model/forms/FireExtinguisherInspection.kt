package com.aeci.mmucompanion.domain.model.forms

import java.time.LocalDateTime

data class FireExtinguisherInspection(
    val id: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val siteLocation: String = "",
    val inspectorName: String = "",
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val status: String = "Draft",
    
    // Fire Extinguisher specific fields
    val extinguisherType: String = "",
    val serialNumber: String = "",
    val location: String = "",
    val lastServiceDate: LocalDateTime? = null,
    val expiryDate: LocalDateTime? = null,
    val pressureGauge: String = "",
    val physicalCondition: String = "",
    val accessibilityCheck: Boolean = false,
    val signageVisible: Boolean = false,
    val defectsFound: List<String> = emptyList(),
    val correctiveActions: List<String> = emptyList(),
    val nextInspectionDate: LocalDateTime? = null,
    val inspectorSignature: String = "",
    val inspectionComplete: Boolean = false
)
