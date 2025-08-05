package com.aeci.mmucompanion.core.util

import android.content.Context
import com.aeci.mmucompanion.data.remote.ApiConfig
import com.aeci.mmucompanion.data.remote.api.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerConnectionManager @Inject constructor(
    private val context: Context
) {
    private var currentApiService: ApiService? = null
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    
    /**
     * Test connection to server and find the best working URL
     */
    suspend fun findAndConnectToServer(): Result<ApiService> = withContext(Dispatchers.IO) {
        try {
            val urls = ApiConfig.getAllPossibleUrls()
            
            for (url in urls) {
                try {
                    val testService = createApiService(url)
                    val response = testService.getHealth()
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        // Update global config
                        ApiConfig.updateBaseUrl(url)
                        currentApiService = testService
                        
                        // Try to get network info for better IP detection
                        try {
                            val networkInfo = testService.getNetworkInfo()
                            if (networkInfo.isSuccessful) {
                                val addresses = networkInfo.body()?.addresses
                                if (!addresses.isNullOrEmpty()) {
                                    // Use the first available address as the preferred URL
                                    val preferredUrl = addresses.first().url.removeSuffix("/")
                                    if (preferredUrl != url) {
                                        ApiConfig.updateBaseUrl(preferredUrl)
                                        currentApiService = createApiService(preferredUrl)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Network info failed, but health check passed, so continue with current URL
                        }
                        
                        return@withContext Result.success(currentApiService!!)
                    }
                } catch (e: Exception) {
                    // This URL failed, try the next one
                    continue
                }
            }
            
            Result.failure(Exception("No server found at any of the configured URLs"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current API service or attempt to reconnect
     */
    suspend fun getApiService(): Result<ApiService> {
        return currentApiService?.let { 
            Result.success(it) 
        } ?: findAndConnectToServer()
    }
    
    /**
     * Test connection to a specific URL
     */
    suspend fun testConnection(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val testService = createApiService(url)
            val response = testService.getHealth()
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Manually set server URL (for user configuration)
     */
    suspend fun setServerUrl(url: String): Result<ApiService> = withContext(Dispatchers.IO) {
        try {
            val normalizedUrl = if (url.endsWith("/")) url.dropLast(1) else url
            val testService = createApiService(normalizedUrl)
            val response = testService.getHealth()
            
            if (response.isSuccessful && response.body()?.success == true) {
                ApiConfig.updateBaseUrl(normalizedUrl)
                currentApiService = testService
                Result.success(testService)
            } else {
                Result.failure(Exception("Server at $normalizedUrl is not responding correctly"))
            }
        } catch (e: ConnectException) {
            Result.failure(Exception("Cannot connect to server at $url. Check if the server is running and the URL is correct."))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Connection timeout to $url. Server may be slow or unreachable."))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to connect to $url: ${e.message}"))
        }
    }
    
    /**
     * Get server network information
     */
    suspend fun getServerNetworkInfo(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val service = getApiService().getOrNull() ?: return@withContext Result.failure(Exception("No API service available"))
            val response = service.getNetworkInfo()
            
            if (response.isSuccessful) {
                val addresses = response.body()?.addresses?.map { it.url } ?: emptyList()
                Result.success(addresses)
            } else {
                Result.failure(Exception("Failed to get network info"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun createApiService(baseUrl: String): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        return retrofit.create(ApiService::class.java)
    }
    
    /**
     * Clear current connection (for troubleshooting)
     */
    fun clearConnection() {
        currentApiService = null
    }
    
    /**
     * Get current server URL
     */
    fun getCurrentServerUrl(): String = ApiConfig.baseUrl
}
