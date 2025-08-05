package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aeci.mmucompanion.presentation.model.FormMenuItem
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.aeci.mmucompanion.presentation.util.getMaintenanceForms
import com.aeci.mmucompanion.presentation.util.getProductionForms
import com.aeci.mmucompanion.presentation.util.getSafetyForms
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.presentation.viewmodel.TechnicianDashboardViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicianDashboardScreen(
    navController: NavHostController,
    viewModel: TechnicianDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showMaintenanceMenu by remember { mutableStateOf(false) }
    var showProductionMenu by remember { mutableStateOf(false) }
    var showSafetyMenu by remember { mutableStateOf(false) }
    var selectedEquipmentStatus by remember { mutableStateOf("") }
    var showEquipmentStatusDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Welcome Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Technician Dashboard",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Quick access to maintenance, production, and safety forms",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
        
        // Quick Action Buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Maintenance Button
            QuickActionButton(
                title = "Maintenance",
                subtitle = "Equipment maintenance and inspection forms",
                icon = Icons.Default.Build,
                color = MaterialTheme.colorScheme.errorContainer,
                onClick = { showMaintenanceMenu = true }
            )
            
            // Production Button  
            QuickActionButton(
                title = "Production",
                subtitle = "Production logs and quality reports",
                icon = Icons.Default.Factory,
                color = MaterialTheme.colorScheme.primaryContainer,
                onClick = { showProductionMenu = true }
            )
            
            // Safety Button
            QuickActionButton(
                title = "Safety",
                subtitle = "Safety checks and incident reports",
                icon = Icons.Default.HealthAndSafety,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                onClick = { showSafetyMenu = true }
            )
            
            // Blast Report Button
            QuickActionButton(
                title = "Blast Reports",
                subtitle = "Generate comprehensive blast reports",
                icon = Icons.Default.Assessment,
                color = MaterialTheme.colorScheme.secondaryContainer,
                onClick = { navController.navigate("blast_reports") }
            )
            
            // TODO/Tasks Button
            QuickActionButton(
                title = "Tasks & To-Do",
                subtitle = "Manage assigned tasks and personal todos",
                icon = Icons.Default.Task,
                color = MaterialTheme.colorScheme.primaryContainer,
                onClick = { navController.navigate("todo_management") }
            )
        }
        
        // Recent Activity Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Recent Activity",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                if (uiState.recentForms.isNotEmpty()) {
                    uiState.recentForms.take(5).forEach { form ->
                        RecentFormItem(form = form) {
                            navController.navigate("forms/view/${form.id}")
                        }
                    }
                } else {
                    Text(
                        text = "No recent activity",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    // Maintenance Forms Menu
    if (showMaintenanceMenu) {
        FormsPopupMenu(
            title = "Maintenance Forms",
            forms = getMaintenanceForms(),
            onDismiss = { showMaintenanceMenu = false },
            onFormSelected = { formType ->
                navController.navigate("forms/create/$formType")
                showMaintenanceMenu = false
            }
        )
    }
    
    // Production Forms Menu
    if (showProductionMenu) {
        FormsPopupMenu(
            title = "Production Forms",
            forms = getProductionForms(),
            onDismiss = { showProductionMenu = false },
            onFormSelected = { formType ->
                navController.navigate("forms/create/$formType")
                showProductionMenu = false
            }
        )
    }
    
    // Safety Forms Menu
    if (showSafetyMenu) {
        FormsPopupMenu(
            title = "Safety Forms",
            forms = getSafetyForms(),
            onDismiss = { showSafetyMenu = false },
            onFormSelected = { formType ->
                navController.navigate("forms/create/$formType")
                showSafetyMenu = false
            }
        )
    }
    
    // Main Dashboard Content
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Image Section
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { navController.navigate("user_profile") },
                        contentAlignment = Alignment.Center
                    ) {
                        uiState.currentUser?.profileImageUri?.let { imageUri ->
                            // TODO: Load actual profile image here
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile Image",
                                modifier = Modifier.size(30.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } ?: Icon(
                            Icons.Default.Person,
                            contentDescription = "Default Profile",
                            modifier = Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Technician Dashboard",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Welcome back, ${uiState.currentUser?.fullName ?: "Technician"}",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "${uiState.currentUser?.department ?: "Department"} • ${uiState.currentUser?.role?.name ?: "Role"}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Profile Settings Button
                    IconButton(
                        onClick = { navController.navigate("user_profile") }
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Profile Settings",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
        
        // Quick Actions
        item {
            Text(
                text = "Quick Actions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(getTechnicianQuickActions()) { action ->
                    TechnicianActionCard(
                        title = action.title,
                        icon = action.icon,
                        onClick = { 
                            when (action.route) {
                                "maintenance_form" -> navController.navigate("maintenance_form")
                                "inspection_form" -> navController.navigate("inspection_form")
                                "safety_form" -> navController.navigate("safety_form")
                                "incident_form" -> navController.navigate("incident_form")
                                "equipment_list" -> navController.navigate("equipment_list")
                                "my_reports" -> navController.navigate("my_reports")
                                "job_cards" -> navController.navigate("job_cards")
                                else -> navController.navigate(action.route)
                            }
                        }
                    )
                }
            }
        }
        
        // Equipment Status Overview
        item {
            Text(
                text = "Equipment Status Overview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TechnicianEquipmentStatusColumn(
                    title = "OPERATIONAL",
                    count = uiState.equipmentStatus.operational,
                    color = Color(0xFF4CAF50),
                    statusText = "Green",
                    onClick = { 
                        selectedEquipmentStatus = "OPERATIONAL"
                        showEquipmentStatusDialog = true
                    }
                )
                
                TechnicianEquipmentStatusColumn(
                    title = "MAINTENANCE", 
                    count = uiState.equipmentStatus.maintenance,
                    color = Color(0xFFFF9800),
                    statusText = "Amber",
                    onClick = { 
                        selectedEquipmentStatus = "MAINTENANCE"
                        showEquipmentStatusDialog = true
                    }
                )
                
                TechnicianEquipmentStatusColumn(
                    title = "BREAKDOWN",
                    count = uiState.equipmentStatus.breakdown,
                    color = Color(0xFFF44336),
                    statusText = "Red",
                    onClick = { 
                        selectedEquipmentStatus = "BREAKDOWN"
                        showEquipmentStatusDialog = true
                    }
                )
            }
        }
        
        // My Job Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Job Cards",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { navController.navigate("job_cards") }) {
                    Text("View All")
                }
            }
        }
        
        if (uiState.myJobCards.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Job Cards Assigned",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "You don't have any job cards assigned to you yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(uiState.myJobCards.take(3)) { jobCard ->
                JobCardItem(
                    jobCard = jobCard,
                    onClick = { navController.navigate("job_card_details/${jobCard.id}") }
                )
            }
        }
        
        // Recent Forms
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Forms",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { navController.navigate("my_reports") }) {
                    Text("View All")
                }
            }
        }
        
        if (uiState.recentForms.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Recent Forms",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Forms you create will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(uiState.recentForms.take(3)) { form ->
                FormItem(
                    form = form,
                    onClick = { navController.navigate("form_details/${form.id}") }
                )
            }
        }
        
        // Equipment List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Equipment List",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { navController.navigate("equipment_list") }) {
                    Text("View All")
                }
            }
        }
        
        if (uiState.isLoading) {
            item {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            items(uiState.equipmentList.take(5)) { equipment ->
                TechnicianEquipmentListItem(
                    equipment = equipment,
                    onClick = { navController.navigate("equipment_details/${equipment.id}") }
                )
            }
        }
    }
    
    // Equipment Status Dialog
    if (showEquipmentStatusDialog) {
        TechnicianEquipmentStatusDialog(
            status = selectedEquipmentStatus,
            equipmentList = when (selectedEquipmentStatus) {
                "OPERATIONAL" -> uiState.equipmentList.filter { it.status == EquipmentStatus.OPERATIONAL }
                "MAINTENANCE" -> uiState.equipmentList.filter { it.status == EquipmentStatus.MAINTENANCE }
                "BREAKDOWN" -> uiState.equipmentList.filter { it.status == EquipmentStatus.BREAKDOWN }
                else -> emptyList()
            },
            onDismiss = { showEquipmentStatusDialog = false },
            onEquipmentClick = { equipmentId ->
                navController.navigate("equipment_details/$equipmentId")
                showEquipmentStatusDialog = false
            }
        )
    }
}

@Composable
fun TechnicianEquipmentStatusColumn(
    title: String,
    count: Int,
    color: Color,
    statusText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = statusText,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TechnicianActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun JobCardItem(
    jobCard: JobCard,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                    text = jobCard.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                TechnicianJobCardStatusChip(status = jobCard.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = jobCard.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Equipment: ${jobCard.equipmentName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = technicianFormatTimestamp(jobCard.createdAt.toEpochSecond(java.time.ZoneOffset.UTC) * 1000),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TechnicianJobCardStatusChip(status: JobCardStatus) {
    val (backgroundColor, textColor) = when (status) {
        JobCardStatus.PENDING -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
        JobCardStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
        JobCardStatus.COMPLETED -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        JobCardStatus.CANCELLED -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
        JobCardStatus.ON_HOLD -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
    
    AssistChip(
        onClick = { },
        label = { 
            Text(
                text = status.name,
                fontSize = 10.sp
            ) 
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = backgroundColor.copy(alpha = 0.2f),
            labelColor = textColor
        )
    )
}

@Composable
fun FormItem(
    form: FormData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (form.formType) {
                    FormType.MAINTENANCE -> Icons.Default.Build
                    FormType.INSPECTION -> Icons.Default.CheckCircle
                    FormType.SAFETY -> Icons.Default.Security
                    else -> Icons.Default.Description
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = form.formType.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Report #${form.reportNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = technicianFormatTimestamp(form.createdAt.toEpochSecond(java.time.ZoneOffset.UTC) * 1000),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TechnicianEquipmentListItem(
    equipment: Equipment,
    onClick: () -> Unit
) {
    val (icon, color) = when (equipment.status) {
        EquipmentStatus.OPERATIONAL -> Icons.Default.CheckCircle to Color.Green
        EquipmentStatus.MAINTENANCE -> Icons.Default.Warning to Color.Yellow
        EquipmentStatus.BREAKDOWN -> Icons.Default.Error to Color.Red
        else -> Icons.Default.Build to Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Status",
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = equipment.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = equipment.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TechnicianEquipmentStatusDialog(
    status: String,
    equipmentList: List<Equipment>,
    onDismiss: () -> Unit,
    onEquipmentClick: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "$status Equipment",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn {
                    items(equipmentList) { equipment ->
                        TechnicianEquipmentListItem(
                            equipment = equipment,
                            onClick = { onEquipmentClick(equipment.id) }
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

// Helper functions
fun getTechnicianQuickActions(): List<QuickAction> {
    return listOf(
        QuickAction("Maintenance", Icons.Default.Build, "maintenance_form"),
        QuickAction("Inspection", Icons.Default.CheckCircle, "inspection_form"),
        QuickAction("Safety", Icons.Default.Security, "safety_form"),
        QuickAction("Incident", Icons.Default.Warning, "incident_form"),
                    QuickAction("Equipment", Icons.Default.Build, "equipment_list"),
        QuickAction("Job Cards", Icons.Filled.Assignment, "job_cards"),
        QuickAction("My Reports", Icons.Default.Description, "my_reports")
    )
}

// QuickAction is already defined in DashboardScreen.kt - avoiding redeclaration
// data class QuickAction(
//     val title: String,
//     val icon: ImageVector,
//     val route: String
// )

fun technicianFormatTimestamp(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    return java.time.Instant.ofEpochMilli(timestamp)
        .atZone(java.time.ZoneId.systemDefault())
        .format(formatter)
} 