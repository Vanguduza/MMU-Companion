package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicianListScreen(
    navController: NavController,
    mode: String // "edit", "remove", or "reset_password"
) {
    val title = when (mode) {
        "edit" -> "Edit Technician Profile"
        "remove" -> "Remove Technician"
        "reset_password" -> "Reset Technician Password"
        else -> "Manage Technicians"
    }
    
    // Mock data for demonstration
    val technicians = remember {
        listOf(
            TechnicianInfo("1", "John Smith", "john.smith@aeci.com", "Maintenance"),
            TechnicianInfo("2", "Sarah Johnson", "sarah.johnson@aeci.com", "Inspection"),
            TechnicianInfo("3", "Mike Wilson", "mike.wilson@aeci.com", "Safety"),
            TechnicianInfo("4", "Emma Davis", "emma.davis@aeci.com", "Maintenance")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = when (mode) {
                    "edit" -> "Select a technician to edit their profile information"
                    "remove" -> "Select a technician to remove from the system"
                    "reset_password" -> "Select a technician to reset their password"
                    else -> "Select an action for the technician"
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Technicians List
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(technicians) { technician ->
                    TechnicianCard(
                        technician = technician,
                        mode = mode,
                        onAction = { techId ->
                            // Handle the action based on mode
                            when (mode) {
                                "edit" -> {
                                    // Navigate to edit screen or show edit dialog
                                    // For now, just show a placeholder
                                }
                                "remove" -> {
                                    // Show confirmation dialog and remove
                                }
                                "reset_password" -> {
                                    // Show password reset confirmation
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TechnicianCard(
    technician: TechnicianInfo,
    mode: String,
    onAction: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = technician.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = technician.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Department: ${technician.department}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Button(
                onClick = { onAction(technician.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (mode) {
                        "remove" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                Icon(
                    imageVector = when (mode) {
                        "edit" -> Icons.Filled.Edit
                        "remove" -> Icons.Default.Delete
                        "reset_password" -> Icons.Default.Lock
                        else -> Icons.Filled.Edit
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (mode) {
                        "edit" -> "Edit"
                        "remove" -> "Remove"
                        "reset_password" -> "Reset"
                        else -> "Action"
                    }
                )
            }
        }
    }
}

data class TechnicianInfo(
    val id: String,
    val name: String,
    val email: String,
    val department: String
)
