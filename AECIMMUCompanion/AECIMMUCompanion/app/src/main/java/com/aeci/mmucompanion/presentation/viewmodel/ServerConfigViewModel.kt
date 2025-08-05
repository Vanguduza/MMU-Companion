package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.core.util.MobileServerConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServerConfigViewModel @Inject constructor(
    private val mobileServerConfig: MobileServerConfig
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServerConfigUiState())
    val uiState: StateFlow<ServerConfigUiState> = _uiState.asStateFlow()

    init {
        loadCurrentConfig()
    }

    private fun loadCurrentConfig() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true
            )
            try {
                val currentUrl = mobileServerConfig.getActiveServerUrl()
                val networkInfo = mobileServerConfig.getNetworkInfo()
                _uiState.value = _uiState.value.copy(
                    serverUrl = currentUrl,
                    manualServerUrl = currentUrl,
                    currentServerUrl = currentUrl,
                    networkInfo = networkInfo,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load configuration",
                    errorMessage = e.message ?: "Failed to load configuration",
                    isLoading = false
                )
            }
        }
    }

    fun updateServerUrl(url: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                val success = mobileServerConfig.setManualServerUrl(url)
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        serverUrl = url,
                        manualServerUrl = url,
                        currentServerUrl = url,
                        isLoading = false,
                        successMessage = "Server URL updated successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Server is not reachable",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update server URL",
                    isLoading = false
                )
            }
        }
    }

    fun setManualServerUrl(url: String) {
        _uiState.value = _uiState.value.copy(
            manualServerUrl = url
        )
    }

    fun toggleAutoDiscovery() {
        if (_uiState.value.autoDiscoveryEnabled) {
            mobileServerConfig.enableAutoDiscovery()
        }
        _uiState.value = _uiState.value.copy(
            autoDiscoveryEnabled = !_uiState.value.autoDiscoveryEnabled
        )
    }

    fun scanForServers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isScanning = true
            )
            
            try {
                // Simulate scanning
                kotlinx.coroutines.delay(2000)
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    discoveredServers = listOf("http://192.168.1.100:3000", "http://192.168.1.101:3000")
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    error = e.message ?: "Scan failed"
                )
            }
        }
    }

    fun testConnection(url: String = "") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                isTesting = true,
                error = null
            )
            
            try {
                val testUrl = if (url.isNotEmpty()) url else mobileServerConfig.getActiveServerUrl()
                val result = mobileServerConfig.testConnection(testUrl)
                
                when (result) {
                    is MobileServerConfig.ConnectionResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isTesting = false,
                            connectionStatus = ConnectionStatus.Connected(testUrl, result.responseTime.toInt()),
                            successMessage = "Connection successful"
                        )
                    }
                    is MobileServerConfig.ConnectionResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isTesting = false,
                            connectionStatus = ConnectionStatus.Disconnected,
                            error = result.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Connection test failed",
                    isLoading = false,
                    isTesting = false,
                    connectionStatus = ConnectionStatus.Disconnected
                )
            }
        }
    }

    fun refreshConnection() {
        testConnection()
    }

    fun resetConfiguration() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true
            )
            
            try {
                mobileServerConfig.enableAutoDiscovery()
                loadCurrentConfig()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to reset configuration",
                    isLoading = false
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            errorMessage = null,
            successMessage = null
        )
    }

    sealed class ConnectionStatus {
        data class Connected(val serverUrl: String, val responseTime: Int) : ConnectionStatus()
        object Disconnected : ConnectionStatus()
        object Connecting : ConnectionStatus()
        object Unknown : ConnectionStatus()
    }
}

data class ServerConfigUiState(
    val serverUrl: String = "",
    val manualServerUrl: String = "",
    val currentServerUrl: String = "",
    val connectionStatus: ServerConfigViewModel.ConnectionStatus = ServerConfigViewModel.ConnectionStatus.Unknown,
    val autoDiscoveryEnabled: Boolean = true,
    val isScanning: Boolean = false,
    val isTesting: Boolean = false,
    val discoveredServers: List<String> = emptyList(),
    val networkInfo: MobileServerConfig.NetworkInfo = MobileServerConfig.NetworkInfo(
        isConnected = false,
        isWiFi = false,
        ssid = "",
        ipAddress = "",
        gateway = ""
    ),
    val isLoading: Boolean = false,
    val error: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
) 
