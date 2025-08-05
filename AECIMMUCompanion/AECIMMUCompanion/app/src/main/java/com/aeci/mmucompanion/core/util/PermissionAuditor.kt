package com.aeci.mmucompanion.core.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionAuditor {
    
    data class PermissionInfo(
        val permission: String,
        val description: String,
        val required: Boolean,
        val granted: Boolean,
        val apiLevel: Int?,
        val purpose: String
    )
    
    data class PermissionAuditReport(
        val totalPermissions: Int,
        val grantedPermissions: Int,
        val deniedPermissions: Int,
        val criticalMissing: List<PermissionInfo>,
        val allPermissions: List<PermissionInfo>
    )
    
    /**
     * All permissions used by the AECI MMU Companion app
     */
    private val ALL_PERMISSIONS = listOf(
        PermissionInfo(
            permission = android.Manifest.permission.INTERNET,
            description = "Network Access",
            required = true,
            granted = false,
            apiLevel = null,
            purpose = "Connect to mobile server, sync data, download updates"
        ),
        PermissionInfo(
            permission = android.Manifest.permission.ACCESS_NETWORK_STATE,
            description = "Network State Information", 
            required = true,
            granted = false,
            apiLevel = null,
            purpose = "Check internet connectivity before data operations"
        ),
        PermissionInfo(
            permission = android.Manifest.permission.ACCESS_WIFI_STATE,
            description = "WiFi State Information",
            required = true,
            granted = false,
            apiLevel = null,
            purpose = "Monitor WiFi connection for server connectivity"
        ),
        PermissionInfo(
            permission = android.Manifest.permission.CAMERA,
            description = "Camera Access",
            required = true,
            granted = false,
            apiLevel = null,
            purpose = "Capture photos for maintenance records, equipment documentation"
        ),
        PermissionInfo(
            permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            description = "Write External Storage",
            required = false,
            granted = false,
            apiLevel = 28, // Only for API 28 and below
            purpose = "Save reports and photos to external storage (legacy devices)"
        ),
        PermissionInfo(
            permission = android.Manifest.permission.READ_EXTERNAL_STORAGE,
            description = "Read External Storage",
            required = false,
            granted = false,
            apiLevel = 32, // Only for API 32 and below
            purpose = "Access existing files and photos (legacy devices)"
        ),
        PermissionInfo(
            permission = android.Manifest.permission.READ_MEDIA_IMAGES,
            description = "Read Media Images",
            required = true,
            granted = false,
            apiLevel = 33, // API 33+
            purpose = "Access photos for attachment to maintenance forms"
        ),
        PermissionInfo(
            permission = android.Manifest.permission.ACCESS_FINE_LOCATION,
            description = "Fine Location Access",
            required = true,
            granted = false,
            apiLevel = null,
            purpose = "GPS tracking for equipment locations, geo-tagged maintenance records"
        ),
        PermissionInfo(
            permission = android.Manifest.permission.ACCESS_COARSE_LOCATION,
            description = "Coarse Location Access",
            required = true,
            granted = false,
            apiLevel = null,
            purpose = "General location for equipment management and site identification"
        ),
        PermissionInfo(
            permission = android.Manifest.permission.USE_BIOMETRIC,
            description = "Biometric Authentication",
            required = false,
            granted = false,
            apiLevel = null,
            purpose = "Secure login using fingerprint or face recognition"
        ),
        PermissionInfo(
            permission = android.Manifest.permission.USE_FINGERPRINT,
            description = "Fingerprint Authentication",
            required = false,
            granted = false,
            apiLevel = null,
            purpose = "Secure login using fingerprint (legacy devices)"
        ),
        PermissionInfo(
            permission = android.Manifest.permission.FOREGROUND_SERVICE,
            description = "Foreground Service",
            required = true,
            granted = false,
            apiLevel = 34, // Required for API 34+
            purpose = "Background synchronization and server connectivity monitoring"
        ),
        PermissionInfo(
            permission = "android.permission.FOREGROUND_SERVICE_DATA_SYNC",
            description = "Foreground Service - Data Sync",
            required = true,
            granted = false,
            apiLevel = 34, // Required for API 34+
            purpose = "Background data synchronization with mobile server"
        ),
        PermissionInfo(
            permission = android.Manifest.permission.POST_NOTIFICATIONS,
            description = "Post Notifications",
            required = true,
            granted = false,
            apiLevel = 33, // Required for API 33+
            purpose = "Display maintenance alerts, sync status, and important notifications"
        ),
        PermissionInfo(
            permission = android.Manifest.permission.VIBRATE,
            description = "Vibration Control",
            required = false,
            granted = false,
            apiLevel = null,
            purpose = "Haptic feedback for critical alerts and notifications"
        ),
        PermissionInfo(
            permission = android.Manifest.permission.WAKE_LOCK,
            description = "Wake Lock",
            required = false,
            granted = false,
            apiLevel = null,
            purpose = "Keep device awake during critical data sync operations"
        ),
        PermissionInfo(
            permission = android.Manifest.permission.NFC,
            description = "Near Field Communication",
            required = false,
            granted = false,
            apiLevel = null,
            purpose = "Future feature: NFC tags for equipment identification"
        ),
        PermissionInfo(
            permission = android.Manifest.permission.READ_PHONE_STATE,
            description = "Phone State Information",
            required = false,
            granted = false,
            apiLevel = null,
            purpose = "Device identification for licensing and security"
        ),
        PermissionInfo(
            permission = android.Manifest.permission.SYSTEM_ALERT_WINDOW,
            description = "System Alert Window",
            required = false,
            granted = false,
            apiLevel = null,
            purpose = "Display critical safety alerts over other apps"
        ),
        PermissionInfo(
            permission = android.Manifest.permission.REQUEST_INSTALL_PACKAGES,
            description = "Install Packages",
            required = false,
            granted = false,
            apiLevel = null,
            purpose = "Install app updates and supplementary modules"
        ),
        PermissionInfo(
            permission = android.Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            description = "Manage External Storage",
            required = false,
            granted = false,
            apiLevel = 30, // API 30+
            purpose = "Full access to external storage for comprehensive file management"
        )
    )
    
    /**
     * Performs a comprehensive audit of all app permissions
     */
    fun auditPermissions(context: Context): PermissionAuditReport {
        val currentApiLevel = Build.VERSION.SDK_INT
        val relevantPermissions = ALL_PERMISSIONS.filter { permission ->
            permission.apiLevel == null || 
            (permission.apiLevel!! <= currentApiLevel && !isLegacyPermission(permission, currentApiLevel))
        }
        
        val updatedPermissions = relevantPermissions.map { permission ->
            permission.copy(
                granted = ContextCompat.checkSelfPermission(context, permission.permission) == PackageManager.PERMISSION_GRANTED
            )
        }
        
        val grantedCount = updatedPermissions.count { it.granted }
        val deniedCount = updatedPermissions.count { !it.granted }
        val criticalMissing = updatedPermissions.filter { it.required && !it.granted }
        
        return PermissionAuditReport(
            totalPermissions = updatedPermissions.size,
            grantedPermissions = grantedCount,
            deniedPermissions = deniedCount,
            criticalMissing = criticalMissing,
            allPermissions = updatedPermissions
        )
    }
    
    /**
     * Checks if a permission is legacy and shouldn't be requested on current API
     */
    private fun isLegacyPermission(permission: PermissionInfo, currentApiLevel: Int): Boolean {
        return when (permission.permission) {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE -> currentApiLevel > 28
            android.Manifest.permission.READ_EXTERNAL_STORAGE -> currentApiLevel > 32
            else -> false
        }
    }
    
    /**
     * Gets permissions that should be requested for current API level
     */
    fun getPermissionsToRequest(context: Context): List<String> {
        val auditReport = auditPermissions(context)
        return auditReport.allPermissions
            .filter { !it.granted }
            .map { it.permission }
    }
    
    /**
     * Generates a detailed permission report
     */
    fun generatePermissionReport(context: Context): String {
        val report = auditPermissions(context)
        
        return buildString {
            appendLine("=== AECI MMU Companion - Permission Audit Report ===")
            appendLine("Generated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}")
            appendLine("Android API Level: ${Build.VERSION.SDK_INT}")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine()
            
            appendLine("SUMMARY:")
            appendLine("- Total Permissions: ${report.totalPermissions}")
            appendLine("- Granted: ${report.grantedPermissions}")
            appendLine("- Denied: ${report.deniedPermissions}")
            appendLine("- Critical Missing: ${report.criticalMissing.size}")
            appendLine()
            
            if (report.criticalMissing.isNotEmpty()) {
                appendLine("⚠️  CRITICAL MISSING PERMISSIONS:")
                report.criticalMissing.forEach { permission ->
                    appendLine("   • ${permission.description}")
                    appendLine("     Purpose: ${permission.purpose}")
                }
                appendLine()
            }
            
            appendLine("DETAILED PERMISSION STATUS:")
            appendLine("=".repeat(50))
            
            report.allPermissions.sortedBy { it.permission }.forEach { permission ->
                val status = if (permission.granted) "✅ GRANTED" else "❌ DENIED"
                val required = if (permission.required) "[REQUIRED]" else "[OPTIONAL]"
                
                appendLine("$status $required ${permission.description}")
                appendLine("   Permission: ${permission.permission}")
                appendLine("   Purpose: ${permission.purpose}")
                if (permission.apiLevel != null) {
                    appendLine("   API Level: ${permission.apiLevel}+")
                }
                appendLine()
            }
            
            appendLine("=== END OF REPORT ===")
        }
    }
} 