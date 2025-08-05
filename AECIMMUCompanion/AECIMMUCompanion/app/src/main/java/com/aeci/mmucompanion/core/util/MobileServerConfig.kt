package com.aeci.mmucompanion.core.util

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import javax.inject.Singleton

@Singleton
class MobileServerConfig constructor(
    private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "mobile_server_config", 
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_AUTO_DISCOVERY = "auto_discovery"
        private const val KEY_LAST_KNOWN_IP = "last_known_ip"
        private const val DEFAULT_PORT = 3000
        private const val HEALTH_ENDPOINT = "/api/health"
        private const val CONNECTION_TIMEOUT = 5000
        
        // Embedded server link for automatic connection
        private const val EMBEDDED_SERVER_LINK = "aeci-mmu://server"
        private const val S24_SERVER_HOSTNAME = "aeci-s24-server"
    }

    // Primary server URLs to try (in order of preference)
    private val fallbackServers = listOf(
        "http://$S24_SERVER_HOSTNAME:$DEFAULT_PORT", // Samsung S24 server via hostname
        "http://samsung-s24.local:$DEFAULT_PORT", // mDNS hostname
        "http://aeci-mobile-server.local:$DEFAULT_PORT", // Alternative mDNS
        "http://192.168.1.100:$DEFAULT_PORT", // Common home network
        "http://192.168.0.100:$DEFAULT_PORT",  // Alternative home network
        "http://10.0.0.100:$DEFAULT_PORT", // Common router IP range
        "http://172.16.0.100:$DEFAULT_PORT", // Corporate network range
        "http://10.0.2.2:$DEFAULT_PORT" // Android emulator (last resort)
    )

    var serverUrl: String
        get() = prefs.getString(KEY_SERVER_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SERVER_URL, value).apply()

    var autoDiscoveryEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_DISCOVERY, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_DISCOVERY, value).apply()

    private var lastKnownIp: String
        get() = prefs.getString(KEY_LAST_KNOWN_IP, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_KNOWN_IP, value).apply()

    /**
     * Get the current server URL, attempting auto-discovery if enabled
     */
    suspend fun getActiveServerUrl(): String = withContext(Dispatchers.IO) {
        // If manual URL is set and working, use it
        if (serverUrl.isNotEmpty() && isServerReachable(serverUrl)) {
            return@withContext serverUrl
        }

        // Try auto-discovery if enabled
        if (autoDiscoveryEnabled) {
            val discoveredUrl = discoverMobileServer()
            if (discoveredUrl != null) {
                serverUrl = discoveredUrl
                return@withContext discoveredUrl
            }
        }

        // Try last known IP
        if (lastKnownIp.isNotEmpty()) {
            val lastKnownUrl = "http://$lastKnownIp:$DEFAULT_PORT"
            if (isServerReachable(lastKnownUrl)) {
                serverUrl = lastKnownUrl
                return@withContext lastKnownUrl
            }
        }

        // Try fallback servers
        for (fallbackUrl in fallbackServers) {
            if (isServerReachable(fallbackUrl)) {
                serverUrl = fallbackUrl
                return@withContext fallbackUrl
            }
        }

        // Return empty if no server found
        ""
    }

    /**
     * Discover mobile server on local network
     */
    private suspend fun discoverMobileServer(): String? = withContext(Dispatchers.IO) {
        try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val dhcp = wifiManager.dhcpInfo
            
            if (dhcp != null) {
                val gateway = dhcp.gateway
                val subnet = gateway and dhcp.netmask
                
                // Scan common IP ranges in the subnet
                for (i in 1..254) {
                    val ip = (subnet and 0xFFFFFF00.toInt()) or i
                    val ipString = String.format(
                        "%d.%d.%d.%d",
                        (ip and 0xff),
                        (ip shr 8 and 0xff),
                        (ip shr 16 and 0xff),
                        (ip shr 24 and 0xff)
                    )
                    
                    val candidateUrl = "http://$ipString:$DEFAULT_PORT"
                    if (isServerReachable(candidateUrl)) {
                        lastKnownIp = ipString
                        return@withContext candidateUrl
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        null
    }

    /**
     * Check if server is reachable
     */
    private suspend fun isServerReachable(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val connection = URL("$url$HEALTH_ENDPOINT").openConnection() as HttpURLConnection
            connection.connectTimeout = CONNECTION_TIMEOUT
            connection.readTimeout = CONNECTION_TIMEOUT
            connection.requestMethod = "GET"
            
            val responseCode = connection.responseCode
            connection.disconnect()
            
            responseCode == 200
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Test connection to a specific URL
     */
    suspend fun testConnection(url: String): ConnectionResult = withContext(Dispatchers.IO) {
        try {
            val connection = URL("$url$HEALTH_ENDPOINT").openConnection() as HttpURLConnection
            connection.connectTimeout = CONNECTION_TIMEOUT
            connection.readTimeout = CONNECTION_TIMEOUT
            connection.requestMethod = "GET"
            
            val responseCode = connection.responseCode
            val responseTime = System.currentTimeMillis()
            
            connection.disconnect()
            
            when (responseCode) {
                200 -> ConnectionResult.Success(url, responseTime)
                else -> ConnectionResult.Error("HTTP $responseCode")
            }
        } catch (e: Exception) {
            ConnectionResult.Error(e.message ?: "Connection failed")
        }
    }

    /**
     * Get network information for debugging
     */
    fun getNetworkInfo(): NetworkInfo {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val dhcp = wifiManager.dhcpInfo
        
        return NetworkInfo(
            isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true,
            isWiFi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true,
            ssid = wifiInfo?.ssid?.replace("\"", "") ?: "",
            ipAddress = dhcp?.let { 
                String.format(
                    "%d.%d.%d.%d",
                    (it.ipAddress and 0xff),
                    (it.ipAddress shr 8 and 0xff),
                    (it.ipAddress shr 16 and 0xff),
                    (it.ipAddress shr 24 and 0xff)
                )
            } ?: "",
            gateway = dhcp?.let {
                String.format(
                    "%d.%d.%d.%d",
                    (it.gateway and 0xff),
                    (it.gateway shr 8 and 0xff),
                    (it.gateway shr 16 and 0xff),
                    (it.gateway shr 24 and 0xff)
                )
            } ?: ""
        )
    }

    /**
     * Set manual server URL
     */
    suspend fun setManualServerUrl(url: String): Boolean {
        val normalizedUrl = if (url.startsWith("http")) url else "http://$url"
        return if (isServerReachable(normalizedUrl)) {
            serverUrl = normalizedUrl
            autoDiscoveryEnabled = false
            true
        } else {
            false
        }
    }

    /**
     * Reset to auto-discovery mode
     */
    fun enableAutoDiscovery() {
        autoDiscoveryEnabled = true
        serverUrl = ""
    }

    sealed class ConnectionResult {
        data class Success(val url: String, val responseTime: Long) : ConnectionResult()
        data class Error(val message: String) : ConnectionResult()
    }

    data class NetworkInfo(
        val isConnected: Boolean,
        val isWiFi: Boolean,
        val ssid: String,
        val ipAddress: String,
        val gateway: String
    )
} 