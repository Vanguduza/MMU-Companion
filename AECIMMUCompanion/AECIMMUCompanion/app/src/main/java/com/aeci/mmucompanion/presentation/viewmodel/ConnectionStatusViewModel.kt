package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.core.util.ServerConnectionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionStatusViewModel @Inject constructor() : ViewModel() {
    
    private val _connectionState = MutableStateFlow<ServerConnectionService.ConnectionState>(
        ServerConnectionService.ConnectionState.SEARCHING
    )
    val connectionState: StateFlow<ServerConnectionService.ConnectionState> = _connectionState.asStateFlow()
    
    init {
        // In a real implementation, this would observe the actual service
        // For now, we'll simulate the connection states
        simulateConnectionStates()
    }
    
    private fun simulateConnectionStates() {
        viewModelScope.launch {
            // This is a simulation - in reality, this would observe the actual service
            _connectionState.value = ServerConnectionService.ConnectionState.SEARCHING
            
            kotlinx.coroutines.delay(2000)
            
            // Simulate finding mobile server
            _connectionState.value = ServerConnectionService.ConnectionState.CONNECTED("http://192.168.1.100:3000")
        }
    }
    
    fun refreshConnection() {
        viewModelScope.launch {
            _connectionState.value = ServerConnectionService.ConnectionState.SEARCHING
            // In reality, this would trigger the service to re-scan
        }
    }
} 
