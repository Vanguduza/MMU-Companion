package com.aeci.mmucompanion.domain.model.forms

import java.time.LocalDateTime

data class MmuQualityReport(
    val id: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val siteLocation: String = "",
    val qualityOfficer: String = "",
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val status: String = "Draft",
    
    // MMU Quality Report specific fields
    val reportDate: LocalDateTime? = null,
    val mmuSerialNumber: String = "",
    val productBatch: String = "",
    val qualityStandards: List<String> = emptyList(),
    val testResults: Map<String, String> = emptyMap(),
    val chemicalComposition: Map<String, Double> = emptyMap(),
    val physicalProperties: Map<String, String> = emptyMap(),
    val performanceMetrics: Map<String, Double> = emptyMap(),
    val complianceStatus: String = "",
    val deviations: List<String> = emptyList(),
    val correctiveActions: List<String> = emptyList(),
    val qualityAssurance: String = "",
    val certificationStatus: String = "",
    val customerFeedback: String = "",
    val improvementRecommendations: String = "",
    val nextReviewDate: LocalDateTime? = null,
    val approvedBy: String = "",
    val qualityApproval: String = "",
    val reportComplete: Boolean = false
)
