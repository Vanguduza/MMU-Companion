package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aeci.mmucompanion.core.util.MobileServerConfig
import com.aeci.mmucompanion.presentation.viewmodel.ServerConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerConfigurationScreen(
    viewModel: ServerConfigViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Server Configuration",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
        
        item {
            // Connection Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (uiState.connectionStatus) {
                        is ServerConfigViewModel.ConnectionStatus.Connected -> Color(0xFF4CAF50)
                        is ServerConfigViewModel.ConnectionStatus.Disconnected -> Color(0xFFF44336)
                        is ServerConfigViewModel.ConnectionStatus.Connecting -> Color(0xFFFF9800)
                        is ServerConfigViewModel.ConnectionStatus.Unknown -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (uiState.connectionStatus) {
                                is ServerConfigViewModel.ConnectionStatus.Connected -> Icons.Default.CheckCircle
                                is ServerConfigViewModel.ConnectionStatus.Disconnected -> Icons.Default.Error
                                is ServerConfigViewModel.ConnectionStatus.Connecting -> Icons.Default.Refresh
                                is ServerConfigViewModel.ConnectionStatus.Unknown -> Icons.Default.Help
                            },
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (uiState.connectionStatus) {
                                is ServerConfigViewModel.ConnectionStatus.Connected -> "Connected to Mobile Server"
                                is ServerConfigViewModel.ConnectionStatus.Disconnected -> "Server Disconnected"
                                is ServerConfigViewModel.ConnectionStatus.Connecting -> "Connecting..."
                                is ServerConfigViewModel.ConnectionStatus.Unknown -> "Connection Status Unknown"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    val connected = uiState.connectionStatus as? ServerConfigViewModel.ConnectionStatus.Connected
                    if (connected != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "URL: ${connected.serverUrl}",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (connected.responseTime > 0) {
                            Text(
                                text = "Response Time: ${connected.responseTime}ms",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
        
        item {
            // Auto Discovery Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Auto Discovery",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Automatically find mobile server on local network",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.autoDiscoveryEnabled,
                            onCheckedChange = { viewModel.toggleAutoDiscovery() }
                        )
                    }
                    
                    if (uiState.autoDiscoveryEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.scanForServers() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isScanning
                        ) {
                            if (uiState.isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scanning...")
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan for Servers")
                            }
                        }
                    }
                }
            }
        }
        
        item {
            // Manual Configuration Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Manual Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var manualUrl by remember { mutableStateOf(uiState.manualServerUrl) }
                    
                    OutlinedTextField(
                        value = manualUrl,
                        onValueChange = { manualUrl = it },
                        label = { Text("Server URL") },
                        placeholder = { Text("http://192.168.1.100:3000") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.testConnection(manualUrl) },
                            modifier = Modifier.weight(1f),
                            enabled = manualUrl.isNotBlank() && !uiState.isTesting
                        ) {
                            if (uiState.isTesting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test")
                        }
                        
                        Button(
                            onClick = { viewModel.setManualServerUrl(manualUrl) },
                            modifier = Modifier.weight(1f),
                            enabled = manualUrl.isNotBlank()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save")
                        }
                    }
                }
            }
        }
        
        item {
            // Network Information
            val networkInfo = uiState.networkInfo
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Network Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                NetworkInfoItem("Connection", if (networkInfo.isConnected) "Connected" else "Disconnected")
                NetworkInfoItem("Type", if (networkInfo.isWiFi) "WiFi" else "Mobile Data")
                if (networkInfo.ssid.isNotEmpty()) {
                    NetworkInfoItem("WiFi Network", networkInfo.ssid)
                }
                if (networkInfo.ipAddress.isNotEmpty()) {
                    NetworkInfoItem("Device IP", networkInfo.ipAddress)
                }
                if (networkInfo.gateway.isNotEmpty()) {
                    NetworkInfoItem("Gateway", networkInfo.gateway)
                }
            }
        }
        
        item {
            // Server Management
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Server Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.refreshConnection() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Refresh")
                        }
                        
                        OutlinedButton(
                            onClick = { viewModel.resetConfiguration() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset")
                        }
                    }
                }
            }
        }
        
        item {
            // Error Message
            val errorMessage = uiState.errorMessage
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkInfoItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
} 