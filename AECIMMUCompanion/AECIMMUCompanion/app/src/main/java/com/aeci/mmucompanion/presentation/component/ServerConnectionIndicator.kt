package com.aeci.mmucompanion.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aeci.mmucompanion.core.util.ServerConnectionService
import com.aeci.mmucompanion.presentation.viewmodel.ConnectionStatusViewModel

@Composable
fun ServerConnectionIndicator(
    modifier: Modifier = Modifier,
    viewModel: ConnectionStatusViewModel = hiltViewModel()
) {
    val connectionState by viewModel.connectionState.collectAsState()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (connectionState) {
                is ServerConnectionService.ConnectionState.CONNECTED -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                ServerConnectionService.ConnectionState.SEARCHING -> Color(0xFFFF9800).copy(alpha = 0.1f)
                ServerConnectionService.ConnectionState.NOT_FOUND -> Color(0xFF2196F3).copy(alpha = 0.1f)
                ServerConnectionService.ConnectionState.NO_NETWORK -> Color(0xFF9E9E9E).copy(alpha = 0.1f)
                is ServerConnectionService.ConnectionState.ERROR -> Color(0xFFF44336).copy(alpha = 0.1f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        when (connectionState) {
                            is ServerConnectionService.ConnectionState.CONNECTED -> Color(0xFF4CAF50)
                            ServerConnectionService.ConnectionState.SEARCHING -> Color(0xFFFF9800)
                            ServerConnectionService.ConnectionState.NOT_FOUND -> Color(0xFF2196F3)
                            ServerConnectionService.ConnectionState.NO_NETWORK -> Color(0xFF9E9E9E)
                            is ServerConnectionService.ConnectionState.ERROR -> Color(0xFFF44336)
                        }
                    )
            )
            
            // Status icon
            Icon(
                imageVector = when (connectionState) {
                    is ServerConnectionService.ConnectionState.CONNECTED -> Icons.Default.CloudDone
                    ServerConnectionService.ConnectionState.SEARCHING -> Icons.Default.Search
                    ServerConnectionService.ConnectionState.NOT_FOUND -> Icons.Default.CloudOff
                    ServerConnectionService.ConnectionState.NO_NETWORK -> Icons.Default.WifiOff
                    is ServerConnectionService.ConnectionState.ERROR -> Icons.Default.Error
                },
                contentDescription = null,
                tint = when (connectionState) {
                    is ServerConnectionService.ConnectionState.CONNECTED -> Color(0xFF4CAF50)
                    ServerConnectionService.ConnectionState.SEARCHING -> Color(0xFFFF9800)
                    ServerConnectionService.ConnectionState.NOT_FOUND -> Color(0xFF2196F3)
                    ServerConnectionService.ConnectionState.NO_NETWORK -> Color(0xFF9E9E9E)
                    is ServerConnectionService.ConnectionState.ERROR -> Color(0xFFF44336)
                },
                modifier = Modifier.size(16.dp)
            )
            
            // Status text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (connectionState) {
                        is ServerConnectionService.ConnectionState.CONNECTED -> "Mobile Server Connected"
                        ServerConnectionService.ConnectionState.SEARCHING -> "Searching for Mobile Server"
                        ServerConnectionService.ConnectionState.NOT_FOUND -> "Using Cloud Server"
                        ServerConnectionService.ConnectionState.NO_NETWORK -> "Offline Mode"
                        is ServerConnectionService.ConnectionState.ERROR -> "Connection Error"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                )
                
                if (connectionState is ServerConnectionService.ConnectionState.CONNECTED) {
                    val connectedState = connectionState as ServerConnectionService.ConnectionState.CONNECTED
                    Text(
                        text = connectedState.serverUrl,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (connectionState is ServerConnectionService.ConnectionState.ERROR) {
                    val errorState = connectionState as ServerConnectionService.ConnectionState.ERROR
                    Text(
                        text = errorState.message,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// Compact version for the top bar
@Composable
fun CompactServerConnectionIndicator(
    modifier: Modifier = Modifier,
    viewModel: ConnectionStatusViewModel = hiltViewModel()
) {
    val connectionState by viewModel.connectionState.collectAsState()
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    when (connectionState) {
                        is ServerConnectionService.ConnectionState.CONNECTED -> Color(0xFF4CAF50)
                        ServerConnectionService.ConnectionState.SEARCHING -> Color(0xFFFF9800)
                        ServerConnectionService.ConnectionState.NOT_FOUND -> Color(0xFF2196F3)
                        ServerConnectionService.ConnectionState.NO_NETWORK -> Color(0xFF9E9E9E)
                        is ServerConnectionService.ConnectionState.ERROR -> Color(0xFFF44336)
                    }
                )
        )
        
        Text(
            text = when (connectionState) {
                is ServerConnectionService.ConnectionState.CONNECTED -> "Mobile"
                ServerConnectionService.ConnectionState.SEARCHING -> "Searching"
                ServerConnectionService.ConnectionState.NOT_FOUND -> "Cloud"
                ServerConnectionService.ConnectionState.NO_NETWORK -> "Offline"
                is ServerConnectionService.ConnectionState.ERROR -> "Error"
            },
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 10.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 