package com.aeci.mmucompanion.core.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.aeci.mmucompanion.MainActivity
import com.aeci.mmucompanion.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ServerConnectionService : Service(), LifecycleOwner {

    @Inject
    lateinit var mobileServerConfig: MobileServerConfig
    
    @Inject
    lateinit var networkManager: NetworkManager

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val _connectionState: MutableStateFlow<ConnectionState> = MutableStateFlow(ConnectionState.SEARCHING)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    override val lifecycle: Lifecycle = lifecycleRegistry

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "server_connection"
        private const val CHANNEL_NAME = "Mobile Server Connection"
        private const val CONNECTION_CHECK_INTERVAL = 30000L // 30 seconds
        
        fun start(context: Context) {
            val intent = Intent(context, ServerConnectionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, ServerConnectionService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        createNotificationChannel()
        startConnectionMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        
        // Start as foreground service with initial notification
        startForeground(NOTIFICATION_ID, createNotification(ConnectionState.SEARCHING))
        
        return START_STICKY // Restart if killed
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows mobile server connection status"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startConnectionMonitoring() {
        lifecycleScope.launch {
            // Initial connection attempt
            attemptConnection()
            
            // Periodic connection monitoring
            while (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                delay(CONNECTION_CHECK_INTERVAL)
                
                if (networkManager.isNetworkAvailable()) {
                    attemptConnection()
                } else {
                    updateConnectionState(ConnectionState.NO_NETWORK)
                }
            }
        }
    }

    private suspend fun attemptConnection() {
        updateConnectionState(ConnectionState.SEARCHING)
        
        try {
            val serverUrl = mobileServerConfig.getActiveServerUrl()
            
            if (serverUrl.isNotEmpty()) {
                // Test the connection to make sure it's working
                val result = mobileServerConfig.testConnection(serverUrl)
                when (result) {
                    is MobileServerConfig.ConnectionResult.Success -> {
                        updateConnectionState(ConnectionState.CONNECTED(serverUrl))
                    }
                    is MobileServerConfig.ConnectionResult.Error -> {
                        updateConnectionState(ConnectionState.ERROR(result.message))
                    }
                }
            } else {
                updateConnectionState(ConnectionState.NOT_FOUND)
            }
        } catch (e: Exception) {
            updateConnectionState(ConnectionState.ERROR(e.message ?: "Connection failed"))
        }
    }

    private fun updateConnectionState(newState: ConnectionState) {
        _connectionState.value = newState
        
        // Update notification
        val notification = createNotification(newState)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotification(state: ConnectionState): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, text, icon) = when (state) {
            ConnectionState.SEARCHING -> Triple(
                "AECI MMU - Searching for mobile server...",
                "Looking for server on local network",
                R.drawable.ic_launcher_foreground
            )
            is ConnectionState.CONNECTED -> Triple(
                "AECI MMU - Connected to mobile server",
                "Server: ${state.serverUrl}",
                R.drawable.ic_launcher_foreground
            )
            ConnectionState.NOT_FOUND -> Triple(
                "AECI MMU - Connecting to mobile server",
                "Establishing connection to mobile server via internet",
                R.drawable.ic_launcher_foreground
            )
            ConnectionState.NO_NETWORK -> Triple(
                "AECI MMU - No network connection",
                "Offline mode active",
                R.drawable.ic_launcher_foreground
            )
            is ConnectionState.ERROR -> Triple(
                "AECI MMU - Connection error",
                state.message,
                R.drawable.ic_launcher_foreground
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(icon)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Can't be dismissed
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    sealed class ConnectionState {
        object SEARCHING : ConnectionState()
        data class CONNECTED(val serverUrl: String) : ConnectionState()
        object NOT_FOUND : ConnectionState()
        object NO_NETWORK : ConnectionState()
        data class ERROR(val message: String) : ConnectionState()
    }
} 