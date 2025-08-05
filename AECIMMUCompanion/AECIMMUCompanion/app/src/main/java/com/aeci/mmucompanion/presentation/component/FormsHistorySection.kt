package com.aeci.mmucompanion.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class FormHistoryItem(
    val id: String,
    val formType: String,
    val formTitle: String,
    val createdDate: LocalDateTime,
    val equipmentId: String? = null,
    val equipmentName: String? = null,
    val operator: String,
    val status: String = "Completed",
    val filePath: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormsHistorySection(
    formType: String,
    historyItems: List<FormHistoryItem>,
    onDownload: (FormHistoryItem) -> Unit,
    onSearch: (String) -> Unit,
    onFilter: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedReport by remember { mutableStateOf<FormHistoryItem?>(null) }
    
    val filteredItems = remember(historyItems, searchQuery, selectedFilter) {
        historyItems.filter { item ->
            val matchesSearch = searchQuery.isBlank() || 
                item.formTitle.contains(searchQuery, ignoreCase = true) ||
                item.operator.contains(searchQuery, ignoreCase = true) ||
                item.equipmentName?.contains(searchQuery, ignoreCase = true) == true
            
            val matchesFilter = selectedFilter == "All" || 
                when(selectedFilter) {
                    "Today" -> item.createdDate.toLocalDate() == LocalDateTime.now().toLocalDate()
                    "This Week" -> item.createdDate.isAfter(LocalDateTime.now().minusWeeks(1))
                    "This Month" -> item.createdDate.isAfter(LocalDateTime.now().minusMonths(1))
                    "Equipment Forms" -> item.equipmentId != null
                    "Completed" -> item.status == "Completed"
                    else -> true
                }
            
            matchesSearch && matchesFilter
        }.sortedByDescending { it.createdDate }
    }
    
    Column(modifier = modifier.fillMaxWidth()) {
        // Header with search and filter
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$formType Forms History",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "${filteredItems.size} forms",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            onSearch(it)
                        },
                        label = { Text("Search forms...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    OutlinedButton(
                        onClick = { showFilterDialog = true }
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(selectedFilter)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // History items list
        if (filteredItems.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.FilePresent,
                        contentDescription = "No forms",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (searchQuery.isNotBlank() || selectedFilter != "All") 
                            "No forms match your search criteria" else 
                            "No $formType forms have been created yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (searchQuery.isNotBlank() || selectedFilter != "All") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Try adjusting your search or filter",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems) { item ->
                        FormHistoryCard(
                            item = item,
                            onDownload = { 
                                selectedReport = item
                                showReportDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Filter dialog
    if (showFilterDialog) {
        FilterDialog(
            currentFilter = selectedFilter,
            onFilterSelected = { 
                selectedFilter = it
                onFilter(it)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
    
    // Report Action Dialog
    selectedReport?.let { report ->
        if (showReportDialog) {
            ReportActionDialog(
                reportPath = report.filePath,
                reportName = report.formTitle,
                onDismiss = {
                    showReportDialog = false
                    selectedReport = null
                }
            )
        }
    }
}

@Composable
private fun FormHistoryCard(
    item: FormHistoryItem,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        text = item.formTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = item.createdDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text(item.status) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when(item.status) {
                                "Completed" -> MaterialTheme.colorScheme.primaryContainer
                                "Draft" -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(onClick = onDownload) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Download form",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            if (item.equipmentName != null || item.operator.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (item.equipmentName != null) {
                        Column {
                            Text(
                                text = "Equipment",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${item.equipmentName} (${item.equipmentId})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    if (item.operator.isNotBlank()) {
                        Column {
                            Text(
                                text = "Operator",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = item.operator,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterDialog(
    currentFilter: String,
    onFilterSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val filterOptions = listOf(
        "All",
        "Today",
        "This Week", 
        "This Month",
        "Equipment Forms",
        "Completed"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Forms") },
        text = {
            Column {
                filterOptions.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentFilter == option,
                            onClick = { onFilterSelected(option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
