package com.aeci.mmucompanion.domain.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.work.*
import com.aeci.mmucompanion.domain.model.SyncStatus
import com.aeci.mmucompanion.domain.repository.FormRepository
import com.aeci.mmucompanion.domain.repository.EquipmentRepository
import com.aeci.mmucompanion.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineSyncService @Inject constructor(
    private val context: Context,
    private val formRepository: FormRepository,
    private val equipmentRepository: EquipmentRepository,
    private val userRepository: UserRepository
) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val workManager = WorkManager.getInstance(context)

    private val _syncStatus = MutableStateFlow(SyncStatus.OFFLINE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _isOnline = MutableStateFlow(isNetworkAvailable())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _pendingSyncItems = MutableStateFlow<List<String>>(emptyList())
    val pendingSyncItems: StateFlow<List<String>> = _pendingSyncItems.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            _isOnline.update { true }
            startAutoSync()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            _isOnline.update { false }
            _syncStatus.update { SyncStatus.OFFLINE }
        }
    }

    init {
        registerNetworkCallback()
        setupPeriodicSync()
    }

    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    private fun setupPeriodicSync() {
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "offline_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

    fun startAutoSync() {
        if (!isNetworkAvailable()) {
            _syncStatus.update { SyncStatus.OFFLINE }
            return
        }

        _syncStatus.update { SyncStatus.SYNCING }

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "manual_sync",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }

    suspend fun syncPendingData(): Result<Unit> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No network connection"))
            }

            _syncStatus.update { SyncStatus.SYNCING }

            // Sync pending forms
            val pendingForms = formRepository.getPendingFormSubmissions()
            pendingForms.forEach { form ->
                val result = formRepository.syncFormSubmission(form)
                if (result.isSuccess) {
                    formRepository.markFormAsSynced(form.id)
                }
            }

            // Sync pending equipment updates
            val pendingEquipmentUpdates = equipmentRepository.getPendingEquipmentUpdates()
            pendingEquipmentUpdates.forEach { equipment ->
                val result = equipmentRepository.syncEquipmentUpdate(equipment)
                if (result.isSuccess) {
                    equipmentRepository.markEquipmentAsSynced(equipment.id)
                }
            }

            // Sync pending user data
            val pendingUserUpdates = userRepository.getPendingUserUpdates()
            pendingUserUpdates.forEach { user ->
                val result = userRepository.syncUserUpdate(user)
                if (result.isSuccess) {
                    userRepository.markUserAsSynced(user.id)
                }
            }

            // Download latest data from server
            downloadLatestData()

            _syncStatus.update { SyncStatus.SYNCED }
            updatePendingSyncItems()

            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.update { SyncStatus.FAILED }
            Result.failure(e)
        }
    }

    private suspend fun downloadLatestData() {
        // Download latest equipment data
        val latestEquipment = equipmentRepository.downloadLatestEquipment()
        if (latestEquipment.isSuccess) {
            equipmentRepository.cacheEquipmentData(latestEquipment.getOrNull() ?: emptyList())
        }

        // Download latest user data
        val latestUsers = userRepository.downloadLatestUsers()
        if (latestUsers.isSuccess) {
            userRepository.cacheUserData(latestUsers.getOrNull() ?: emptyList())
        }

        // Download latest form templates
        val latestForms = formRepository.downloadLatestFormTemplates()
        if (latestForms.isSuccess) {
            formRepository.cacheFormTemplates(latestForms.getOrNull() ?: emptyList())
        }
    }

    private suspend fun updatePendingSyncItems() {
        val pendingItems = mutableListOf<String>()
        
        // Count pending forms
        val pendingForms = formRepository.getPendingFormSubmissions()
        if (pendingForms.isNotEmpty()) {
            pendingItems.add("${pendingForms.size} form submissions")
        }

        // Count pending equipment updates
        val pendingEquipment = equipmentRepository.getPendingEquipmentUpdates()
        if (pendingEquipment.isNotEmpty()) {
            pendingItems.add("${pendingEquipment.size} equipment updates")
        }

        // Count pending user updates
        val pendingUsers = userRepository.getPendingUserUpdates()
        if (pendingUsers.isNotEmpty()) {
            pendingItems.add("${pendingUsers.size} user updates")
        }

        _pendingSyncItems.update { pendingItems }
    }

    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun enableOfflineMode() {
        _syncStatus.update { SyncStatus.OFFLINE }
        // Enable offline-only operations
    }

    fun disableOfflineMode() {
        if (isNetworkAvailable()) {
            startAutoSync()
        }
    }

    fun cancelSync() {
        workManager.cancelUniqueWork("manual_sync")
        _syncStatus.update { 
            if (isNetworkAvailable()) SyncStatus.SYNCED else SyncStatus.OFFLINE 
        }
    }

    fun clearSyncCache() {
        // Clear cached data if needed
        workManager.cancelAllWork()
    }
}

class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val offlineSyncService: OfflineSyncService
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val syncResult = offlineSyncService.syncPendingData()
            if (syncResult.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
} 