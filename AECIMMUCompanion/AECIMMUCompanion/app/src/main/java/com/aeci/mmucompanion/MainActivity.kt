package com.aeci.mmucompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aeci.mmucompanion.presentation.screen.AECIMMUCompanionApp
import com.aeci.mmucompanion.ui.theme.AECIMMUCompanionTheme
import com.aeci.mmucompanion.worker.SyncWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Set content immediately for faster UI display
        setContent {
            AECIMMUCompanionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AECIMMUCompanionApp()
                }
            }
        }
        
        // Defer non-critical operations to avoid blocking UI
        scheduleBackgroundTasks()
    }
    
    private fun scheduleBackgroundTasks() {
        // Use a separate thread for permission requests and sync scheduling
        Thread {
            try {
                // Request necessary permissions based on Android version
                val permissions = mutableListOf<String>()
                
                // Core permissions for all versions
                val corePermissions = listOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.VIBRATE,
                    android.Manifest.permission.WAKE_LOCK,
                    android.Manifest.permission.ACCESS_WIFI_STATE
                )
                
                corePermissions.forEach { permission ->
                    if (androidx.core.content.ContextCompat.checkSelfPermission(this, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        permissions.add(permission)
                    }
                }
                
                // Version-specific permissions
                if (android.os.Build.VERSION.SDK_INT >= 34) {
                    val api34Permissions = listOf(
                        android.Manifest.permission.FOREGROUND_SERVICE,
                        "android.permission.FOREGROUND_SERVICE_DATA_SYNC"
                    )
                    api34Permissions.forEach { permission ->
                        if (androidx.core.content.ContextCompat.checkSelfPermission(this, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            permissions.add(permission)
                        }
                    }
                }
                
                if (android.os.Build.VERSION.SDK_INT >= 33) {
                    val api33Permissions = listOf(
                        android.Manifest.permission.POST_NOTIFICATIONS,
                        android.Manifest.permission.READ_MEDIA_IMAGES
                    )
                    api33Permissions.forEach { permission ->
                        if (androidx.core.content.ContextCompat.checkSelfPermission(this, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            permissions.add(permission)
                        }
                    }
                }
                
                // External storage permissions (version-dependent)
                if (android.os.Build.VERSION.SDK_INT <= 28) {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
                if (android.os.Build.VERSION.SDK_INT <= 32) {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
                
                // Request all missing permissions
                if (permissions.isNotEmpty()) {
                    runOnUiThread {
                        androidx.core.app.ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 1001)
                        android.util.Log.i("MainActivity", "Requesting ${permissions.size} permissions: $permissions")
                    }
                }

                // Schedule background sync
                SyncWorker.schedulePeriodicSync(this)
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error in background tasks", e)
            }
        }.start()
    }
}