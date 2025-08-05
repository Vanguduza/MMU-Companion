package com.aeci.mmucompanion.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.sp
import com.aeci.mmucompanion.domain.model.SyncStatus

@Composable
fun OfflineIndicator(
    isOnline: Boolean,
    syncStatus: SyncStatus,
    pendingSyncItems: List<String>,
    onSyncClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        !isOnline -> Color(0xFFFF5722) // Red for offline
        syncStatus == SyncStatus.SYNCING -> Color(0xFFFF9800) // Orange for syncing
        syncStatus == SyncStatus.FAILED -> Color(0xFFFF5722) // Red for failed
        syncStatus == SyncStatus.SYNCED -> Color(0xFF4CAF50) // Green for synced
        else -> Color(0xFF757575) // Gray for unknown
    }

    val textColor = Color.White
    val iconColor = Color.White

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        !isOnline -> Icons.Default.CloudOff
                        syncStatus == SyncStatus.SYNCING -> Icons.Default.Sync
                        syncStatus == SyncStatus.FAILED -> Icons.Default.ErrorOutline
                        syncStatus == SyncStatus.SYNCED -> Icons.Default.CloudDone
                        else -> Icons.Default.Cloud
                    },
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column {
                    Text(
                        text = when {
                            !isOnline -> "Offline Mode"
                            syncStatus == SyncStatus.SYNCING -> "Syncing..."
                            syncStatus == SyncStatus.FAILED -> "Sync Failed"
                            syncStatus == SyncStatus.SYNCED -> "Online - Synced"
                            else -> "Online"
                        },
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (pendingSyncItems.isNotEmpty()) {
                        Text(
                            text = "${pendingSyncItems.size} items pending sync",
                            color = textColor.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            if (isOnline && syncStatus != SyncStatus.SYNCING && pendingSyncItems.isNotEmpty()) {
                IconButton(
                    onClick = onSyncClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync Now",
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SyncStatusBanner(
    syncStatus: SyncStatus,
    pendingSyncItems: List<String>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (syncStatus == SyncStatus.SYNCING || syncStatus == SyncStatus.FAILED) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = when (syncStatus) {
                SyncStatus.SYNCING -> MaterialTheme.colorScheme.secondaryContainer
                SyncStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (syncStatus == SyncStatus.SYNCING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = when (syncStatus) {
                            SyncStatus.SYNCING -> "Syncing data..."
                            SyncStatus.FAILED -> "Sync failed. Some data may not be up to date."
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PendingSyncDialog(
    isVisible: Boolean,
    pendingSyncItems: List<String>,
    onDismiss: () -> Unit,
    onSyncNow: () -> Unit,
    onClearCache: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Pending Sync Items") },
            text = {
                Column {
                    Text("The following items are waiting to be synced:")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    pendingSyncItems.forEach { item ->
                        Text(
                            text = "• $item",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = onSyncNow) {
                    Text("Sync Now")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
} 