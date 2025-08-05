package com.aeci.mmucompanion.data.remote

object ApiConfig {
    // Default server URL - UPDATE THIS WITH YOUR PHONE'S IP ADDRESS
    private const val DEFAULT_BASE_URL = "http://192.168.1.100:3000"
    
    // Fallback URLs for different network scenarios
    private val FALLBACK_URLS = listOf(
        "http://192.168.1.100:3000",    // Default WiFi range
        "http://192.168.0.100:3000",    // Alternative WiFi range
        "http://10.0.0.100:3000",       // Mobile hotspot range
        "http://localhost:3000"         // Local development
    )
    
    // Current base URL (can be changed at runtime)
    var baseUrl: String = DEFAULT_BASE_URL
        private set
    
    /**
     * Update the base URL for API calls
     * This allows dynamic server URL changes based on network conditions
     */
    fun updateBaseUrl(newUrl: String) {
        baseUrl = if (newUrl.endsWith("/")) {
            newUrl.dropLast(1)
        } else {
            newUrl
        }
    }
    
    /**
     * Get all possible server URLs for connection testing
     */
    fun getAllPossibleUrls(): List<String> {
        return listOf(baseUrl) + FALLBACK_URLS.filter { it != baseUrl }
    }
    
    /**
     * API Endpoints
     */
    object Endpoints {
        const val HEALTH = "/api/health"
        const val NETWORK_INFO = "/api/network-info"
        
        // Authentication
        const val LOGIN = "/api/auth/login"
        const val CHANGE_PASSWORD = "/api/auth/change-password"
        const val ME = "/api/auth/me"
        
        // Users
        const val USERS = "/api/users"
        
        // Todos
        const val TODOS = "/api/todos"
        const val TODO_COMMENTS = "/api/todos/{id}/comments"
        const val TODO_TIME_START = "/api/todos/{id}/time/start"
        const val TODO_TIME_STOP = "/api/todos/{id}/time/{entryId}/stop"
        const val TODO_ANALYTICS = "/api/todos/analytics"
        const val TODO_BULK = "/api/todos/bulk"
        
        // Forms
        const val FORMS = "/api/forms"
        
        // Equipment
        const val EQUIPMENT = "/api/equipment"
        
        // Reports
        const val REPORTS = "/api/reports"
    }
}
