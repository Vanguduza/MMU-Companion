package com.aeci.mmucompanion

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.aeci.mmucompanion.domain.repository.UserRepository
import com.aeci.mmucompanion.core.util.ServerConnectionService
import com.aeci.mmucompanion.domain.model.User
import com.aeci.mmucompanion.domain.model.UserRole
import com.aeci.mmucompanion.domain.model.Permission

@HiltAndroidApp
class AECIMMUCompanionApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var userRepository: UserRepository
    
    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("AECIApp", "Application onCreate() started")
        
        // WorkManager is automatically initialized when using Configuration.Provider
        
        // Start critical initialization tasks with priority (move to background immediately)
        CoroutineScope(Dispatchers.IO).launch {
            initializeCriticalServices()
        }
        
        // Defer all non-critical tasks to avoid blocking app startup
        CoroutineScope(Dispatchers.IO).launch {
            initializeNonCriticalServices()
        }
        
        android.util.Log.d("AECIApp", "Application onCreate() completed")
    }
    
    private suspend fun initializeCriticalServices() {
        try {
            android.util.Log.d("AECIApp", "Starting critical services initialization")
            
            // Check for existing admin users first (fastest operation)
            val adminCount = userRepository.getAdminUserCount()
            if (adminCount == 0) {
                android.util.Log.d("AECIApp", "No admin users found, creating default admin")
                createDefaultAdminUser()
            } else {
                android.util.Log.d("AECIApp", "Admin users already exist: $adminCount")
            }
            
            android.util.Log.d("AECIApp", "Critical services initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("AECIApp", "Error during critical initialization", e)
        }
    }
    
    private suspend fun initializeNonCriticalServices() {
        try {
            android.util.Log.d("AECIApp", "Starting non-critical services initialization")
            
            // Delay non-critical services to improve perceived startup time
            kotlinx.coroutines.delay(3000) // Wait 3 seconds after app launch
            
            // Start automatic server connection service
            android.util.Log.d("AECIApp", "Starting server connection service")
            ServerConnectionService.start(this@AECIMMUCompanionApplication)
            
            android.util.Log.i("AECIApp", "Non-critical services initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("AECIApp", "Error during non-critical initialization", e)
        }
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.WARN) // Reduce logging overhead
            .setMaxSchedulerLimit(10) // Reduce thread count for faster startup
            .setTaskExecutor(java.util.concurrent.Executors.newFixedThreadPool(1)) // Minimal thread pool
            .build()
    
    private suspend fun createDefaultAdminUser() {
        try {
            // Check if any admin users exist
            val adminCount = userRepository.getAdminUserCount()
            if (adminCount == 0) {
                // Create default admin user
            val defaultAdmin = User(
                id = "admin_001",
                username = "admin",
                fullName = "System Administrator",
                email = "admin@aeci.com",
                role = UserRole.ADMIN,
                department = "IT",
                shiftPattern = "Day",
                permissions = listOf(
                    Permission.VIEW_FORMS,
                    Permission.CREATE_FORMS,
                    Permission.EDIT_FORMS,
                    Permission.DELETE_FORMS,
                    Permission.SUBMIT_FORMS,
                    Permission.APPROVE_FORMS,
                    Permission.VIEW_EQUIPMENT,
                    Permission.MANAGE_EQUIPMENT,
                    Permission.VIEW_USERS,
                    Permission.MANAGE_USERS,
                    Permission.VIEW_REPORTS,
                    Permission.EXPORT_DATA,
                    Permission.SYSTEM_ADMIN,
                    Permission.SYNC_DATA
                ),
                isActive = true,
                siteId = "site_001" // Default site assignment
            )                // Default password: AECIAdmin2025! (should be changed on first login)
                userRepository.createUserWithPassword(defaultAdmin, "AECIAdmin2025!")
                android.util.Log.i("AECIApp", "Default admin user created with username: admin, password: AECIAdmin2025!")
            }
        } catch (e: Exception) {
            android.util.Log.e("AECIApp", "Error creating default admin user", e)
        }
    }
}
