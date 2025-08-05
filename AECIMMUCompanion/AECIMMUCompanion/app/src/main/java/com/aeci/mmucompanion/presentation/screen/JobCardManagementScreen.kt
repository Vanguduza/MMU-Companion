@file:OptIn(ExperimentalMaterial3Api::class)

package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.presentation.component.DatePickerComponent
import com.aeci.mmucompanion.presentation.viewmodel.JobCardManagementViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobCardManagementScreen(
    navController: NavHostController,
    viewModel: JobCardManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showFilters by remember { mutableStateOf(false) }
    var showBulkActions by remember { mutableStateOf(false) }
    var selectedJobCards by remember { mutableStateOf(setOf<String>()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Card Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters")
                    }
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { showBulkActions = !showBulkActions }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Search job cards...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )
            
            // Filters Section
            if (showFilters) {
                FiltersSection(
                    filters = uiState.filters,
                    onFiltersChanged = { viewModel.updateFilters(it) },
                    onClearFilters = { viewModel.clearFilters() }
                )
            }
            
            // Statistics Cards
            StatisticsSection(statistics = uiState.statistics)
            
            // Job Cards List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (uiState.jobCards.isEmpty()) {
                    item {
                        EmptyStateCard()
                    }
                } else {
                    items(uiState.jobCards) { jobCard ->
                        JobCardListItem(
                            jobCard = jobCard,
                            isSelected = selectedJobCards.contains(jobCard.id),
                            onSelectionChanged = { isSelected ->
                                selectedJobCards = if (isSelected) {
                                    selectedJobCards + jobCard.id
                                } else {
                                    selectedJobCards - jobCard.id
                                }
                            },
                            onClick = { navController.navigate("job_card_details/${jobCard.id}") }
                        )
                    }
                }
            }
        }
        
        // Bulk Actions Dialog
        if (showBulkActions) {
            BulkActionsDialog(
                selectedCount = selectedJobCards.size,
                onAssign = { assignedTo ->
                    viewModel.bulkAssignJobCards(selectedJobCards.toList(), assignedTo)
                    showBulkActions = false
                    selectedJobCards = emptySet()
                },
                onUpdateStatus = { status ->
                    viewModel.bulkUpdateStatus(selectedJobCards.toList(), status)
                    showBulkActions = false
                    selectedJobCards = emptySet()
                },
                onDelete = {
                    viewModel.bulkDeleteJobCards(selectedJobCards.toList())
                    showBulkActions = false
                    selectedJobCards = emptySet()
                },
                onExport = {
                    viewModel.exportJobCards(selectedJobCards.toList())
                    showBulkActions = false
                    selectedJobCards = emptySet()
                },
                onDismiss = { showBulkActions = false }
            )
        }
    }
}

@Composable
fun FiltersSection(
    filters: JobCardFilters,
    onFiltersChanged: (JobCardFilters) -> Unit,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClearFilters) {
                    Text("Clear All")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status Filter
            var statusExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded }
            ) {
                OutlinedTextField(
                    value = filters.status.joinToString(", ") { it.name },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statusExpanded) },
                    modifier = Modifier
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    JobCardStatus.values().forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name) },
                            onClick = {
                                val newStatuses = if (filters.status.contains(status)) {
                                    filters.status - status
                                } else {
                                    filters.status + status
                                }
                                onFiltersChanged(filters.copy(status = newStatuses))
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Priority Filter
            var priorityExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = priorityExpanded,
                onExpandedChange = { priorityExpanded = !priorityExpanded }
            ) {
                OutlinedTextField(
                    value = filters.priority.joinToString(", ") { it.name },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Priority") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(priorityExpanded) },
                    modifier = Modifier
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = priorityExpanded,
                    onDismissRequest = { priorityExpanded = false }
                ) {
                    JobCardPriority.values().forEach { priority ->
                        DropdownMenuItem(
                            text = { Text(priority.name) },
                            onClick = {
                                val newPriorities = if (filters.priority.contains(priority)) {
                                    filters.priority - priority
                                } else {
                                    filters.priority + priority
                                }
                                onFiltersChanged(filters.copy(priority = newPriorities))
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Category Filter
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = filters.category.joinToString(", ") { it.name },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                    modifier = Modifier
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    JobCardCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                val newCategories = if (filters.category.contains(category)) {
                                    filters.category - category
                                } else {
                                    filters.category + category
                                }
                                onFiltersChanged(filters.copy(category = newCategories))
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Date Range
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DatePickerComponent(
                    selectedDate = filters.dateFrom,
                    onDateSelected = { date ->
                        onFiltersChanged(filters.copy(dateFrom = date))
                    },
                    label = "From Date",
                    modifier = Modifier.weight(1f)
                )
                
                DatePickerComponent(
                    selectedDate = filters.dateTo,
                    onDateSelected = { date ->
                        onFiltersChanged(filters.copy(dateTo = date))
                    },
                    label = "To Date",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatisticsSection(statistics: JobCardStatistics) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            StatCard(
                title = "Total",
                value = statistics.totalJobCards.toString(),
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            StatCard(
                title = "Pending",
                value = statistics.pendingJobCards.toString(),
                color = MaterialTheme.colorScheme.secondary
            )
        }
        item {
            StatCard(
                title = "In Progress",
                value = statistics.inProgressJobCards.toString(),
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        item {
            StatCard(
                title = "Completed",
                value = statistics.completedJobCards.toString(),
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            StatCard(
                title = "Overdue",
                value = statistics.overdueJobCards.toString(),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun JobCardListItem(
    jobCard: JobCard,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChanged
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = jobCard.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
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
                        text = formatTimestamp(jobCard.createdAt.toEpochSecond(java.time.ZoneOffset.UTC) * 1000),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                JobCardStatusChipMain(status = jobCard.status)
                Spacer(modifier = Modifier.height(4.dp))
                JobCardPriorityChip(priority = jobCard.priority)
            }
        }
    }
}

@Composable
fun JobCardStatusChipMain(status: JobCardStatus) {
    val (backgroundColor, textColor) = when (status) {
        JobCardStatus.PENDING -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
        JobCardStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
        JobCardStatus.COMPLETED -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        JobCardStatus.CANCELLED -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
        JobCardStatus.ON_HOLD -> MaterialTheme.colorScheme.outline to MaterialTheme.colorScheme.onSurfaceVariant
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
fun JobCardPriorityChip(priority: JobCardPriority) {
    val (backgroundColor, textColor) = when (priority) {
        JobCardPriority.LOW -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
        JobCardPriority.MEDIUM -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
        JobCardPriority.HIGH -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
        JobCardPriority.URGENT -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
    }
    
    AssistChip(
        onClick = { },
        label = { 
            Text(
                text = priority.name,
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
fun EmptyStateCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
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
                text = "No Job Cards Found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Try adjusting your filters or search criteria.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun BulkActionsDialog(
    selectedCount: Int,
    onAssign: (String) -> Unit,
    onUpdateStatus: (JobCardStatus) -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onDismiss: () -> Unit
) {
    var showAssignDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var assignedTo by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bulk Actions ($selectedCount selected)") },
        text = {
            Column {
                TextButton(
                    onClick = { showAssignDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Assign to Technician")
                }
                
                TextButton(
                    onClick = { showStatusDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Update, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Update Status")
                }
                
                TextButton(
                    onClick = onExport,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Selected")
                }
                
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Selected")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    if (showAssignDialog) {
        AlertDialog(
            onDismissRequest = { showAssignDialog = false },
            title = { Text("Assign Job Cards") },
            text = {
                OutlinedTextField(
                    value = assignedTo,
                    onValueChange = { assignedTo = it },
                    label = { Text("Technician ID") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (assignedTo.isNotBlank()) {
                            onAssign(assignedTo)
                            showAssignDialog = false
                        }
                    }
                ) {
                    Text("Assign")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAssignDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Update Status") },
            text = {
                Column {
                    JobCardStatus.values().forEach { status ->
                        TextButton(
                            onClick = {
                                onUpdateStatus(status)
                                showStatusDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(status.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun formatTimestamp(timestamp: Long): String {
    val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    return java.time.Instant.ofEpochMilli(timestamp)
        .atZone(java.time.ZoneId.systemDefault())
        .format(formatter)
} 