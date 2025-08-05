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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aeci.mmucompanion.presentation.model.FormMenuItem
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.presentation.viewmodel.TechnicianDashboardViewModel
import com.aeci.mmucompanion.presentation.util.getMaintenanceForms
import com.aeci.mmucompanion.presentation.util.getProductionForms
import com.aeci.mmucompanion.presentation.util.getSafetyForms
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import com.aeci.mmucompanion.presentation.component.AECIIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimplifiedTechnicianDashboardScreen(
    navController: NavHostController,
    viewModel: TechnicianDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showMaintenanceMenu by remember { mutableStateOf(false) }
    var showProductionMenu by remember { mutableStateOf(false) }
    var showSafetyMenu by remember { mutableStateOf(false) }
    var showTimesheetDialog by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
        // User Profile Header
        item {
            UserProfileHeader(
                user = uiState.currentUser,
                onProfileClick = { navController.navigate("profile") }
            )
        }

        // Quick Action Buttons
        item {
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
                
                // Timesheet Button
                QuickActionButton(
                    title = "Timesheet",
                    subtitle = "Track work hours and submit timesheets",
                    icon = AECIIcons.Timesheet,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = { showTimesheetDialog = true }
                )
            }
        }
        
        // My Job Cards Section
        item {
            MyJobCardsSection(
                jobCards = uiState.myJobCards,
                onViewAllClick = { navController.navigate("job_cards") },
                onJobCardClick = { jobCard ->
                    navController.navigate("job_cards/details/${jobCard.id}")
                }
            )
        }
        
        // Recent Forms Section
        item {
            RecentFormsSection(
                forms = uiState.recentForms,
                onViewAllClick = { navController.navigate("forms") },
                onFormClick = { form ->
                    navController.navigate("forms/view/${form.id}")
                }
            )
        }

        // Equipment Status Overview
        item {
            EquipmentStatusSection(
                equipmentList = uiState.equipmentList,
                onEquipmentClick = { equipment ->
                    navController.navigate("equipment/details/${equipment.id}")
                }
            )
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
    
    // Timesheet Dialog
    if (showTimesheetDialog) {
        TimesheetDialog(
            onDismiss = { showTimesheetDialog = false },
            onCreateTimesheet = { 
                navController.navigate("timesheet/create")
                showTimesheetDialog = false
            },
            onViewTimesheets = {
                navController.navigate("timesheet/list")
                showTimesheetDialog = false
            },
            onClockInOut = {
                navController.navigate("timesheet/clock")
                showTimesheetDialog = false
            }
        )
    }
}

@Composable
fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Open $title forms",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun FormsPopupMenu(
    title: String,
    forms: List<FormMenuItem>,
    onDismiss: () -> Unit,
    onFormSelected: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                forms.forEach { form ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onFormSelected(form.formType) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = form.title,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun RecentFormItem(
    form: FormData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = form.formType.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = form.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// New components for enhanced dashboard

@Composable
fun UserProfileHeader(
    user: User?,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .clickable { onProfileClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            AsyncImage(
                model = user?.profileImageUri ?: "https://via.placeholder.com/60x60?text=${user?.fullName?.first() ?: "U"}",
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Welcome back,",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = user?.fullName ?: "Unknown User",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${user?.role?.name?.lowercase()?.replaceFirstChar { it.uppercase() }} • ${user?.department}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "View Profile",
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun MyJobCardsSection(
    jobCards: List<JobCard>,
    onViewAllClick: () -> Unit,
    onJobCardClick: (JobCard) -> Unit
) {
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
                Text(
                    text = "My Job Cards",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onViewAllClick) {
                    Text("View All")
                }
            }
            
            if (jobCards.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    items(jobCards.take(5)) { jobCard ->
                        SimplifiedJobCardItem(
                            jobCard = jobCard,
                            onClick = { onJobCardClick(jobCard) }
                        )
                    }
                }
            } else {
                Text(
                    text = "No job cards assigned",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
fun SimplifiedJobCardItem(
    jobCard: JobCard,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when (jobCard.priority) {
                JobCardPriority.URGENT -> MaterialTheme.colorScheme.errorContainer
                JobCardPriority.HIGH -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = jobCard.priority.name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusBadge(status = jobCard.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = jobCard.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
            
            Text(
                text = jobCard.equipmentName,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            
            jobCard.dueDate?.let { dueDate ->
                Text(
                    text = "Due: ${dueDate.format(DateTimeFormatter.ofPattern("MMM dd"))}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: JobCardStatus) {
    val backgroundColor = when (status) {
        JobCardStatus.PENDING -> MaterialTheme.colorScheme.surface
        JobCardStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
        JobCardStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
        JobCardStatus.CANCELLED -> MaterialTheme.colorScheme.error
        JobCardStatus.ON_HOLD -> MaterialTheme.colorScheme.outline
    }
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = status.name.replace("_", " "),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun RecentFormsSection(
    forms: List<FormData>,
    onViewAllClick: () -> Unit,
    onFormClick: (FormData) -> Unit
) {
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
                Text(
                    text = "Recent Forms",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onViewAllClick) {
                    Text("View All")
                }
            }
            
            if (forms.isNotEmpty()) {
                forms.take(5).forEach { form ->
                    RecentFormItem(form = form) {
                        onFormClick(form)
                    }
                }
            } else {
                Text(
                    text = "No recent forms",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
fun EquipmentStatusSection(
    equipmentList: List<Equipment>,
    onEquipmentClick: (Equipment) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Equipment Status",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            if (equipmentList.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(equipmentList.take(10)) { equipment ->
                        EquipmentStatusCard(
                            equipment = equipment,
                            onClick = { onEquipmentClick(equipment) }
                        )
                    }
                }
            } else {
                Text(
                    text = "No equipment data available",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EquipmentStatusCard(
    equipment: Equipment,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when (equipment.statusIndicator) {
                EquipmentStatusIndicator.GREEN -> MaterialTheme.colorScheme.primaryContainer
                EquipmentStatusIndicator.AMBER -> MaterialTheme.colorScheme.tertiaryContainer
                EquipmentStatusIndicator.RED -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Equipment Image
            AsyncImage(
                model = equipment.imageUri ?: "https://via.placeholder.com/60x40?text=${equipment.type.name}",
                contentDescription = equipment.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = equipment.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            
            Text(
                text = equipment.status.name.replace("_", " "),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Show issues if any
            if (equipment.recordedIssues.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Issues",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${equipment.recordedIssues.size} issue(s)",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun TimesheetDialog(
    onDismiss: () -> Unit,
    onCreateTimesheet: () -> Unit,
    onViewTimesheets: () -> Unit,
    onClockInOut: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Timesheet Management",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Quick Clock In/Out
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClockInOut() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Clock In/Out",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Quick Clock In/Out",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Record your work time",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Create Timesheet
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCreateTimesheet() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Timesheet",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Create New Timesheet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Start a new weekly timesheet",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // View Timesheets
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewTimesheets() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "View Timesheets",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "View My Timesheets",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Review and submit timesheets",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
