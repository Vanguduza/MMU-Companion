package com.aeci.mmucompanion.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.aeci.mmucompanion.core.util.NetworkManager
import com.aeci.mmucompanion.domain.usecase.SyncFormsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncFormsUseCase: SyncFormsUseCase,
    private val networkManager: NetworkManager
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        const val WORK_NAME = "sync_work"
        const val TAG_SYNC = "sync"
        
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
                flexTimeInterval = 5,
                flexTimeIntervalUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag(TAG_SYNC)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest
            )
        }
        
        fun scheduleOneTimeSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag(TAG_SYNC)
                .build()
            
            WorkManager.getInstance(context).enqueue(syncWorkRequest)
        }
        
        fun cancelSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
    
    override suspend fun doWork(): Result {
        return try {
            // Check if network is available
            if (!networkManager.isNetworkAvailable()) {
                return Result.retry()
            }
            
            // Check if on metered connection and handle accordingly
            if (networkManager.isOnMeteredConnection()) {
                // Only sync critical data on metered connections
                // This is a placeholder for selective sync logic
            }
            
            // Perform sync operations
            val syncResult = syncFormsUseCase()
            
            if (syncResult.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (exception: Exception) {
            // Log the error
            android.util.Log.e("SyncWorker", "Sync failed", exception)
            Result.retry()
        }
    }
}

@HiltWorker
class DataCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        const val WORK_NAME = "cleanup_work"
        
        fun scheduleCleanup(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresDeviceIdle(true)
                .build()
            
            val cleanupWorkRequest = PeriodicWorkRequestBuilder<DataCleanupWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .setInitialDelay(4, TimeUnit.HOURS) // Run during night hours
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                cleanupWorkRequest
            )
        }
    }
    
    override suspend fun doWork(): Result {
        return try {
            // Clean up old temporary files
            cleanupTempFiles()
            
            // Clean up old cached data
            cleanupOldCacheData()
            
            // Clean up old logs
            cleanupOldLogs()
            
            Result.success()
        } catch (exception: Exception) {
            android.util.Log.e("DataCleanupWorker", "Cleanup failed", exception)
            Result.failure()
        }
    }
    
    private suspend fun cleanupTempFiles() {
        // Implementation to clean up temporary files
        val tempDir = applicationContext.cacheDir
        val cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        
        tempDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoffTime) {
                file.delete()
            }
        }
    }
    
    private suspend fun cleanupOldCacheData() {
        // Implementation to clean up old cached data
        // This could include removing old form drafts, cached images, etc.
    }
    
    private suspend fun cleanupOldLogs() {
        // Implementation to clean up old log files
    }
}
