@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.aeci.mmucompanion.presentation.model.FormMenuItem
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.aeci.mmucompanion.domain.model.FormData
import com.aeci.mmucompanion.domain.model.FormType
import com.aeci.mmucompanion.presentation.util.getMaintenanceForms
import com.aeci.mmucompanion.presentation.util.getProductionForms
import com.aeci.mmucompanion.presentation.util.getSafetyForms
import com.aeci.mmucompanion.domain.model.Equipment
import com.aeci.mmucompanion.presentation.component.AECIIcons
import com.aeci.mmucompanion.presentation.component.ImagePickerComponent
import com.aeci.mmucompanion.presentation.viewmodel.DashboardViewModel
import java.time.format.DateTimeFormatter
import com.aeci.mmucompanion.presentation.component.ServerConnectionIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showEquipmentStatusDialog by remember { mutableStateOf(false) }
    var selectedEquipmentStatus by remember { mutableStateOf("") }
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 80.dp), // Add padding for bottom status key
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Welcome Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Welcome back,",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = uiState.currentUser?.fullName ?: "User",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = uiState.currentUser?.role?.name ?: "",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            item {
                // Equipment Status Overview - Three Column Layout
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
                    // Green - Operational Equipment
                    DashboardEquipmentStatusColumn(
                        title = "OPERATIONAL",
                        count = uiState.equipmentStatus.operational,
                        color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                        statusText = "Green",
                        onClick = { 
                            selectedEquipmentStatus = "OPERATIONAL"
                            showEquipmentStatusDialog = true
                        }
                    )
                    
                    // Amber - Warning Equipment
                    DashboardEquipmentStatusColumn(
                        title = "WARNING", 
                        count = uiState.equipmentStatus.warning,
                        color = androidx.compose.ui.graphics.Color(0xFFFF9800),
                        statusText = "Amber",
                        onClick = { 
                            selectedEquipmentStatus = "WARNING"
                            showEquipmentStatusDialog = true
                        }
                    )
                    
                    // Red - Critical Equipment
                    DashboardEquipmentStatusColumn(
                        title = "CRITICAL",
                        count = uiState.equipmentStatus.critical,
                        color = androidx.compose.ui.graphics.Color(0xFFF44336),
                        statusText = "Red",
                        onClick = { 
                            selectedEquipmentStatus = "CRITICAL"
                            showEquipmentStatusDialog = true
                        }
                    )
                }
            }
            
            item {
                // Form Categories - Two Column Layout (Production & Maintenance)
                Text(
                    text = "Form Categories",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Production Forms Column
                    FormCategoryCard(
                        modifier = Modifier.weight(1f),
                        title = "Production",
                        description = "Daily logs, quality reports, handovers",
                        icon = Icons.Default.Factory,
                        forms = uiState.productionForms,
                        onClick = { navController.navigate("forms/category/production") }
                    )
                    
                    // Maintenance Forms Column
                    FormCategoryCard(
                        modifier = Modifier.weight(1f),
                        title = "Maintenance",
                        description = "Inspections, pump checks, safety",
                        icon = Icons.Default.Build,
                        forms = uiState.maintenanceForms,
                        onClick = { navController.navigate("forms/category/maintenance") }
                    )
                }
            }
            
            item {
                // Quick Actions
                Text(
                    text = "Quick Actions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickActionPopupCard(
                        title = "Maintenance",
                        icon = Icons.Default.Build,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        forms = getMaintenanceForms(),
                        onFormSelected = { formType ->
                            navController.navigate("forms/create/$formType")
                        }
                    )
                    
                    QuickActionPopupCard(
                        title = "Production",
                        icon = Icons.Default.Factory,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        forms = getProductionForms(),
                        onFormSelected = { formType ->
                            navController.navigate("forms/create/$formType")
                        }
                    )
                    
                    QuickActionPopupCard(
                        title = "Safety",
                        icon = Icons.Default.Security,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        forms = getSafetyForms(),
                        onFormSelected = { formType ->
                            navController.navigate("forms/create/$formType")
                        }
                    )
                }
            }
            
            item {
                // Generate Blast Report Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("blast_reports") },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = AECIIcons.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Generate Blast Report",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Compile comprehensive blast analysis with emulsion calculations and pump calibration checks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            item {
                // Recent Forms
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
                    TextButton(
                        onClick = { navController.navigate("forms") }
                    ) {
                        Text("View All")
                    }
                }
            }
            
            items(uiState.recentForms.take(5)) { form ->
                FormCard(
                    form = form,
                    onClick = { navController.navigate("forms/edit/${form.id}") }
                )
            }
            
            // Recent Issues Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Issues",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = { navController.navigate("issues") }
                    ) {
                        Text("View All")
                    }
                }
            }
            
            items(uiState.recentIssues.take(3)) { issue ->
                IssueCard(
                    issue = issue,
                    onClick = { navController.navigate("issues/detail/${issue.id}") }
                )
            }
            
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            if (uiState.error != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.error ?: "An unknown error occurred",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        
        // Equipment Status Key at Bottom Center
        // REMOVE THIS BLOCK
        // Card(
        //     modifier = Modifier
        //         .align(Alignment.BottomCenter)
        //         .padding(16.dp)
        //         .fillMaxWidth(),
        //     colors = CardDefaults.cardColors(
        //         containerColor = MaterialTheme.colorScheme.surfaceVariant
        //     )
        // ) {
        //     Row(
        //         modifier = Modifier
        //             .fillMaxWidth()
        //             .padding(16.dp),
        //         horizontalArrangement = Arrangement.SpaceEvenly,
        //         verticalAlignment = Alignment.CenterVertically
        //     ) {
        //         DashboardStatusLegendItem(
        //             color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
        //             label = "Operational"
        //         )
        //         DashboardStatusLegendItem(
        //             color = androidx.compose.ui.graphics.Color(0xFFFF9800),
        //             label = "Warning"
        //         )
        //         DashboardStatusLegendItem(
        //             color = androidx.compose.ui.graphics.Color(0xFFF44336),
        //             label = "Critical"
        //         )
        //     }
        // }
    }
    
    // Equipment Status Dialog
    if (showEquipmentStatusDialog) {
        EquipmentStatusDialog(
            status = selectedEquipmentStatus,
            equipmentList = uiState.equipmentByStatus[selectedEquipmentStatus] ?: emptyList(),
            onDismiss = { showEquipmentStatusDialog = false },
            onEquipmentClick = { equipmentId ->
                showEquipmentStatusDialog = false
                navController.navigate("equipment/details/$equipmentId")
            }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DashboardActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(140.dp)
            .height(80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun FormCard(
    form: com.aeci.mmucompanion.domain.model.FormData,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = form.formType.displayName,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Text(
                        text = form.formType.name.replace("_", " "),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(status = form.status.name)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Last updated: ${form.updatedAt.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status) {
        "COMPLETED" -> MaterialTheme.colorScheme.tertiary
        "IN_PROGRESS" -> MaterialTheme.colorScheme.secondary
        "DRAFT" -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.outline
    }
    
    AssistChip(
        onClick = { },
        label = { 
            Text(
                text = status,
                fontSize = 10.sp
            ) 
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color
        )
    )
}

@Composable
fun DashboardEquipmentStatusColumn(
    title: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    statusText: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        // Status Color Circle
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Status Text
        Text(
            text = statusText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
        
        // Title
        Text(
            text = title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FormCategoryCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: ImageVector,
    forms: Int,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(140.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = forms.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    maxLines = 2
                )
            }
        }
    }
}

data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionPopupCard(
    title: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    forms: List<FormMenuItem>,
    onFormSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .width(110.dp)
            .height(100.dp),
        onClick = { showMenu = true },
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
    
    if (showMenu) {
        QuickActionFormDialog(
            title = "$title Forms",
            forms = forms,
            onFormSelected = onFormSelected,
            onDismiss = { showMenu = false }
        )
    }
}

@Composable
fun QuickActionFormDialog(
    title: String,
    forms: List<FormMenuItem>,
    onFormSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(forms) { form ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    onFormSelected(form.formType)
                                onDismiss()
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = form.icon,
                                    contentDescription = form.title,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = form.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = form.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Select",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

fun getQuickActions(): List<QuickAction> {
    return listOf(
        QuickAction("Daily Log", AECIIcons.Today, "forms/create/mmu_daily_log"),
        QuickAction("Quality Report", AECIIcons.Assessment, "forms/create/quality_report"),
        QuickAction("Pump Check", AECIIcons.Construction, "forms/create/pump_inspection"),
        QuickAction("Equipment", Icons.Default.Settings, "equipment"),
        QuickAction("Reports", AECIIcons.Analytics, "reports")
    )
}

fun dashboardFormatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> "${diff / 86400000}d ago"
    }
}

@Composable
fun DashboardStatusLegendItem(
    color: androidx.compose.ui.graphics.Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = color,
                    shape = CircleShape
                )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EquipmentStatusDialog(
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
                
                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(equipmentList) { equipment ->
                            DashboardEquipmentListItem(
                                equipment = equipment,
                                onClick = { onEquipmentClick(equipment.id) }
                            )
                        }
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

@Composable
fun DashboardEquipmentListItem(
    equipment: Equipment,
    onClick: () -> Unit
) {
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
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Equipment Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = equipment.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = equipment.location,
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
fun IssueCard(
    issue: Issue,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = issue.title,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Text(
                        text = issue.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                IssuePriorityChip(priority = issue.priority)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Equipment: ${issue.equipmentName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dashboardFormatTimestamp(issue.createdAt),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun IssuePriorityChip(priority: String) {
    val color = when (priority) {
        "HIGH" -> MaterialTheme.colorScheme.error
        "MEDIUM" -> MaterialTheme.colorScheme.secondary
        "LOW" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
    
    AssistChip(
        onClick = { },
        label = { 
            Text(
                text = priority,
                fontSize = 10.sp
            ) 
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.2f),
            labelColor = color
        )
    )
}

// Data class for Issue
data class Issue(
    val id: String,
    val title: String,
    val description: String,
    val priority: String,
    val equipmentName: String,
    val createdAt: Long
)
