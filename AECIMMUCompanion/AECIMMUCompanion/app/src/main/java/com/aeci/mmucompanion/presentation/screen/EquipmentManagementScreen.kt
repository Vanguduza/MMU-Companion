@file:OptIn(ExperimentalMaterial3Api::class)

package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aeci.mmucompanion.domain.model.Equipment
import com.aeci.mmucompanion.presentation.viewmodel.EquipmentManagementViewModel
import com.aeci.mmucompanion.domain.model.EquipmentStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentManagementScreen(
    viewModel: EquipmentManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with Add Category Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Equipment Management",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = { viewModel.onShowAddCategoryDialog() },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Category",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Category")
            }
        }
        
        // Category Tabs
        LazyRow(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.categories) { category ->
                FilterChip(
                    onClick = { viewModel.onCategorySelected(category) },
                    label = { Text(category.name) },
                    selected = uiState.selectedCategory?.id == category.id,
                    leadingIcon = {
                        Icon(
                            imageVector = when (category.name) {
                                "PUMPS" -> Icons.Default.WaterDrop
                                "MMUs" -> Icons.Default.Build
                                else -> Icons.Default.Category
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
        
        // Equipment List
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            items(uiState.filteredEquipment) { equipment ->
                ManagementEquipmentCard(
                    equipment = equipment,
                    onEditClick = { viewModel.onEditEquipment(equipment) },
                    onDeleteClick = { viewModel.onDeleteEquipment(equipment) },
                    onStatusClick = { viewModel.onUpdateEquipmentStatus(equipment) },
                    onViewClick = { viewModel.onViewEquipment(equipment) }
                )
            }
        }
        }
    }
    
    // Add Category Dialog
    if (uiState.showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { viewModel.onDismissAddCategoryDialog() },
            onAddCategory = { categoryName ->
                viewModel.addCategory(categoryName)
            }
        )
    }
    
    // Equipment Detail Dialog
    uiState.selectedEquipment?.let { equipment ->
        EquipmentDetailDialog(
            equipment = equipment,
            onDismiss = { viewModel.onDismissEquipmentDetail() },
            onEdit = { viewModel.onEditEquipment(it) },
            onDelete = { viewModel.onDeleteEquipment(it) }
        )
    }
}

@Composable
private fun ManagementEquipmentCard(
    equipment: Equipment,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStatusClick: () -> Unit,
    onViewClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = equipment.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${equipment.id}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Location: ${equipment.location}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Badge(
                        containerColor = when (equipment.status) {
                            EquipmentStatus.OPERATIONAL -> MaterialTheme.colorScheme.primary
                            EquipmentStatus.MAINTENANCE -> MaterialTheme.colorScheme.secondary
                            EquipmentStatus.BREAKDOWN -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.outline
                        }
                    ) {
                        Text(
                            text = equipment.status.name,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "View",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View")
                }
                
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                
                IconButton(
                    onClick = onStatusClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Update Status"
                    )
                }
                
                IconButton(
                    onClick = onDeleteClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAddCategory: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Equipment Category") },
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Category Name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onAddCategory(categoryName)
                    onDismiss()
                },
                enabled = categoryName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EquipmentDetailDialog(
    equipment: Equipment,
    onDismiss: () -> Unit,
    onEdit: (Equipment) -> Unit,
    onDelete: (Equipment) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Equipment Details") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EquipmentDetailRow("Name", equipment.name)
                EquipmentDetailRow("ID", equipment.id)
                EquipmentDetailRow("Location", equipment.location)
                EquipmentDetailRow("Status", equipment.status.name)
                EquipmentDetailRow("Model", equipment.model)
                EquipmentDetailRow("Serial Number", equipment.serialNumber)
                EquipmentDetailRow("Last Service", equipment.lastMaintenanceDate?.let { java.time.Instant.ofEpochMilli(it).toString() } ?: "N/A")
                EquipmentDetailRow("Next Service", equipment.nextMaintenanceDate?.let { java.time.Instant.ofEpochMilli(it).toString() } ?: "N/A")
            }
        },
        confirmButton = {
            Button(
                onClick = { onEdit(equipment) }
            ) {
                Text("Edit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun EquipmentDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
} 