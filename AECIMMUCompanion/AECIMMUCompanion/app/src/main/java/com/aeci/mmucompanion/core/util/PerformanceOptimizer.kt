package com.aeci.mmucompanion.core.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PerformanceOptimizer {
    
    /**
     * Configuration for performance optimization
     */
    data class PerformanceConfig(
        val lazyColumnItemsPerPage: Int = 20,
        val imageLoadingDelay: Long = 100L,
        val backgroundTaskDelay: Long = 2000L,
        val enableMemoryOptimization: Boolean = true,
        val enableLazyLoading: Boolean = true
    )
    
    private val defaultConfig = PerformanceConfig()
    
    /**
     * Gets optimized configuration based on device capabilities
     */
    fun getOptimizedConfig(context: Context): PerformanceConfig {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val memInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        val availableMemoryMB = memInfo.availMem / (1024 * 1024)
        
        return when {
            availableMemoryMB < 1024 -> PerformanceConfig(
                lazyColumnItemsPerPage = 10,
                imageLoadingDelay = 200L,
                backgroundTaskDelay = 3000L,
                enableMemoryOptimization = true,
                enableLazyLoading = true
            )
            availableMemoryMB < 2048 -> PerformanceConfig(
                lazyColumnItemsPerPage = 15,
                imageLoadingDelay = 150L,
                backgroundTaskDelay = 2500L,
                enableMemoryOptimization = true,
                enableLazyLoading = true
            )
            else -> defaultConfig
        }
    }
    
    /**
     * Optimized remember for expensive computations
     */
    @Composable
    fun <T> rememberOptimized(
        vararg keys: Any?,
        calculation: () -> T
    ): T {
        return remember(*keys) {
            try {
                calculation()
            } catch (e: Exception) {
                android.util.Log.e("PerformanceOptimizer", "Error in optimized calculation", e)
                throw e
            }
        }
    }
    
    /**
     * Launches background task with performance optimization
     */
    @Composable
    fun LaunchedEffectOptimized(
        vararg keys: Any?,
        delay: Long = defaultConfig.backgroundTaskDelay,
        block: suspend () -> Unit
    ) {
        LaunchedEffect(*keys) {
            try {
                // Add delay to avoid blocking UI during startup
                kotlinx.coroutines.delay(delay)
                
                // Execute on IO thread to avoid blocking main thread
                withContext(Dispatchers.IO) {
                    block()
                }
            } catch (e: Exception) {
                android.util.Log.e("PerformanceOptimizer", "Error in optimized LaunchedEffect", e)
            }
        }
    }
    
    /**
     * Memory optimization utilities
     */
    object Memory {
        
        /**
         * Forces garbage collection if memory optimization is enabled
         */
        fun optimizeMemory(context: Context) {
            val config = getOptimizedConfig(context)
            if (config.enableMemoryOptimization) {
                try {
                    System.gc()
                    Runtime.getRuntime().gc()
                } catch (e: Exception) {
                    android.util.Log.w("PerformanceOptimizer", "Could not trigger garbage collection", e)
                }
            }
        }
        
        /**
         * Gets current memory usage information
         */
        fun getMemoryInfo(): String {
            val runtime = Runtime.getRuntime()
            val totalMemory = runtime.totalMemory() / (1024 * 1024)
            val freeMemory = runtime.freeMemory() / (1024 * 1024)
            val usedMemory = totalMemory - freeMemory
            val maxMemory = runtime.maxMemory() / (1024 * 1024)
            
            return "Memory: ${usedMemory}MB used / ${totalMemory}MB total / ${maxMemory}MB max"
        }
    }
    
    /**
     * UI performance optimization utilities
     */
    object UI {
        
        /**
         * Calculates optimal LazyColumn page size based on device performance
         */
        fun getOptimalPageSize(context: Context): Int {
            return getOptimizedConfig(context).lazyColumnItemsPerPage
        }
        
        /**
         * Gets optimal image loading delay
         */
        fun getImageLoadingDelay(context: Context): Long {
            return getOptimizedConfig(context).imageLoadingDelay
        }
        
        /**
         * Checks if lazy loading should be enabled
         */
        fun shouldUseLazyLoading(context: Context): Boolean {
            return getOptimizedConfig(context).enableLazyLoading
        }
    }
} 