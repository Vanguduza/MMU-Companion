package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentReportsScreen(navController: NavController) {
    // State variables
    var showGenerateReportDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showReportViewer by remember { mutableStateOf(false) }
    var selectedReportId by remember { mutableStateOf("") }
    var downloadProgress by remember { mutableStateOf(0f) }
    var isDownloading by remember { mutableStateOf(false) }
    
    // Mock data for demonstration
    val reports = remember {
        listOf(
            EquipmentReport("1", "Equipment Status Report", "2025-07-25", "Monthly", "PDF"),
            EquipmentReport("2", "Maintenance Schedule", "2025-07-24", "Weekly", "Excel"),
            EquipmentReport("3", "Equipment Efficiency Report", "2025-07-23", "Daily", "PDF"),
            EquipmentReport("4", "Equipment Downtime Analysis", "2025-07-22", "Monthly", "PDF"),
            EquipmentReport("5", "Parts Inventory Report", "2025-07-21", "Weekly", "Excel"),
            EquipmentReport("6", "Equipment Utilization", "2025-07-20", "Daily", "PDF")
        )
    }
    
    // Download function
    fun downloadReport(reportId: String) {
        // Simulate download - in real implementation, this would use a download manager
        // and update the UI through a state variable or callback
        println("Downloading report: $reportId")
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
                text = "Equipment Reports",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Reports",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${reports.size}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "This Month",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "6",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Filter/Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { 
                    // Generate new equipment report
                    showGenerateReportDialog = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Generate Report")
            }
            
            OutlinedButton(
                onClick = { 
                    // Show filter dialog
                    showFilterDialog = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Filter")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Reports List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(reports) { report ->
                EquipmentReportCard(
                    report = report,
                    onView = { reportId ->
                        // Navigate to report detail view
                        showReportViewer = true
                        selectedReportId = reportId
                    },
                    onDownload = { reportId ->
                        // Start download process
                        downloadReport(reportId)
                    }
                )
            }
        }
    }
    
    // Generate Report Dialog
    if (showGenerateReportDialog) {
        GenerateReportDialog(
            onDismiss = { showGenerateReportDialog = false },
            onGenerate = { reportType, dateRange, format ->
                showGenerateReportDialog = false
                // TODO: Generate report with specified parameters
            }
        )
    }
    
    // Filter Dialog
    if (showFilterDialog) {
        FilterReportsDialog(
            onDismiss = { showFilterDialog = false },
            onApplyFilter = { filterCriteria ->
                showFilterDialog = false
                // TODO: Apply filter criteria
            }
        )
    }
    
    // Report Viewer Dialog
    if (showReportViewer) {
        ReportViewerDialog(
            reportId = selectedReportId,
            onDismiss = { showReportViewer = false }
        )
    }
    
    // Download Progress Dialog
    if (isDownloading) {
        DownloadProgressDialog(
            progress = downloadProgress,
            onCancel = { isDownloading = false }
        )
    }
}

@Composable
fun EquipmentReportCard(
    report: EquipmentReport,
    onView: (String) -> Unit,
    onDownload: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        text = report.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Generated: ${report.date}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Type: ${report.type} • Format: ${report.format}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { onView(report.id) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "View Report"
                            )
                        }
                        IconButton(
                            onClick = { onDownload(report.id) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Report"
                            )
                        }
                    }
                }
            }
        }
    }
}

data class EquipmentReport(
    val id: String,
    val title: String,
    val date: String,
    val type: String,
    val format: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateReportDialog(
    onDismiss: () -> Unit,
    onGenerate: (String, String, String) -> Unit
) {
    var selectedReportType by remember { mutableStateOf("Equipment Status") }
    var selectedDateRange by remember { mutableStateOf("Last 7 Days") }
    var selectedFormat by remember { mutableStateOf("PDF") }
    
    val reportTypes = listOf("Equipment Status", "Maintenance Schedule", "Efficiency Report", "Downtime Analysis", "Parts Inventory")
    val dateRanges = listOf("Last 7 Days", "Last 30 Days", "Last 3 Months", "Last 6 Months", "Custom Range")
    val formats = listOf("PDF", "Excel", "CSV")
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Generate Equipment Report",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Report Type Selection
                Text(
                    text = "Report Type",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                reportTypes.forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReportType = type }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedReportType == type,
                            onClick = { selectedReportType = type }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = type)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date Range Selection
                Text(
                    text = "Date Range",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                var dateRangeExpanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = dateRangeExpanded,
                    onExpandedChange = { dateRangeExpanded = !dateRangeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedDateRange,
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dateRangeExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = dateRangeExpanded,
                        onDismissRequest = { dateRangeExpanded = false }
                    ) {
                        dateRanges.forEach { range ->
                            DropdownMenuItem(
                                text = { Text(range) },
                                onClick = {
                                    selectedDateRange = range
                                    dateRangeExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Format Selection
                Text(
                    text = "Format",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    formats.forEach { format ->
                        FilterChip(
                            selected = selectedFormat == format,
                            onClick = { selectedFormat = format },
                            label = { Text(format) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onGenerate(selectedReportType, selectedDateRange, selectedFormat) }
                    ) {
                        Text("Generate")
                    }
                }
            }
        }
    }
}

@Composable
fun FilterReportsDialog(
    onDismiss: () -> Unit,
    onApplyFilter: (Map<String, String>) -> Unit
) {
    var selectedType by remember { mutableStateOf("All") }
    var selectedFormat by remember { mutableStateOf("All") }
    var selectedDateRange by remember { mutableStateOf("All Time") }
    
    val types = listOf("All", "Equipment Status", "Maintenance", "Efficiency", "Downtime", "Inventory")
    val formats = listOf("All", "PDF", "Excel", "CSV")
    val dateRanges = listOf("All Time", "Last 7 Days", "Last 30 Days", "Last 3 Months")
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Filter Reports",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Type Filter
                Text(
                    text = "Report Type",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(types) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type) }
                        )
                    }
                }
                
                // Format Filter
                Text(
                    text = "Format",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(formats) { format ->
                        FilterChip(
                            selected = selectedFormat == format,
                            onClick = { selectedFormat = format },
                            label = { Text(format) }
                        )
                    }
                }
                
                // Date Range Filter
                Text(
                    text = "Date Range",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    items(dateRanges) { range ->
                        FilterChip(
                            selected = selectedDateRange == range,
                            onClick = { selectedDateRange = range },
                            label = { Text(range) }
                        )
                    }
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val filters = mapOf(
                                "type" to selectedType,
                                "format" to selectedFormat,
                                "dateRange" to selectedDateRange
                            )
                            onApplyFilter(filters)
                        }
                    ) {
                        Text("Apply Filter")
                    }
                }
            }
        }
    }
}

@Composable
fun ReportViewerDialog(
    reportId: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Report Viewer",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                HorizontalDivider()
                
                // Content area - would show actual report content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Report ID: $reportId",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Report content would be displayed here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadProgressDialog(
    progress: Float,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = { }) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Downloading Report",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        }
    }
}
