package com.aeci.mmucompanion.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aeci.mmucompanion.domain.model.Equipment
import com.aeci.mmucompanion.domain.model.EquipmentStatusIndicator
import com.aeci.mmucompanion.presentation.component.ImagePickerComponent

@Composable
fun EquipmentStatusCard(
    equipment: Equipment,
    onStatusChange: (EquipmentStatusIndicator, String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showEditDialog = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = equipment.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                EquipmentStatusIndicatorIcon(
                    statusIndicator = equipment.statusIndicator,
                    size = 24.dp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Location: ${equipment.location}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (equipment.conditionDescription.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Condition: ${equipment.conditionDescription}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            equipment.lastModifiedBy?.let { modifiedBy ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Last updated by: $modifiedBy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
    
    if (showEditDialog) {
        EquipmentStatusEditDialog(
            equipment = equipment,
            onDismiss = { showEditDialog = false },
            onSave = { indicator, description, imageUri ->
                onStatusChange(indicator, description, imageUri)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun EquipmentStatusIndicatorIcon(
    statusIndicator: EquipmentStatusIndicator,
    size: androidx.compose.ui.unit.Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    val color = when (statusIndicator) {
        EquipmentStatusIndicator.GREEN -> Color(0xFF4CAF50)  // Green
        EquipmentStatusIndicator.AMBER -> Color(0xFFFF9800)  // Amber/Orange
        EquipmentStatusIndicator.RED -> Color(0xFFF44336)    // Red
    }
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.White, CircleShape)
    )
}

@Composable
fun EquipmentStatusEditDialog(
    equipment: Equipment,
    onDismiss: () -> Unit,
    onSave: (EquipmentStatusIndicator, String, String?) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(equipment.statusIndicator) }
    var conditionText by remember { mutableStateOf(equipment.conditionDescription) }
    var conditionImageUri by remember { mutableStateOf(equipment.conditionImageUri) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Update Equipment Status",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = equipment.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Status Indicator:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EquipmentStatusIndicator.values().forEach { status ->
                        val isSelected = selectedStatus == status
                        val statusText = when (status) {
                            EquipmentStatusIndicator.GREEN -> "Good"
                            EquipmentStatusIndicator.AMBER -> "Moderate"
                            EquipmentStatusIndicator.RED -> "Critical"
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { selectedStatus = status }
                                .padding(8.dp)
                        ) {
                            EquipmentStatusIndicatorIcon(
                                statusIndicator = status,
                                size = 32.dp,
                                modifier = Modifier.then(
                                    if (isSelected) {
                                        Modifier.border(
                                            2.dp,
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        )
                                    } else Modifier
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Condition Description:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = conditionText,
                    onValueChange = { conditionText = it },
                    placeholder = { Text("Describe the current condition...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Condition Evidence Photo (Optional):",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ImagePickerComponent(
                    imageUris = if (conditionImageUri != null) listOf(conditionImageUri!!) else emptyList(),
                    onImagesSelected = { uris -> conditionImageUri = uris.firstOrNull() },
                    onImageRemoved = { _ -> conditionImageUri = null },
                    maxImages = 1,
                    allowMultiple = false
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { onSave(selectedStatus, conditionText.trim(), conditionImageUri) }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
