package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.presentation.component.AECIIcons
import com.aeci.mmucompanion.presentation.viewmodel.AdminReportsViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    navController: NavHostController,
    viewModel: AdminReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showFilterDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedReport by remember { mutableStateOf<Report?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var reportToDelete by remember { mutableStateOf<Report?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadReports()
        viewModel.loadStatistics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = { viewModel.refreshReports() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Statistics Cards
            uiState.statistics?.let { statistics ->
                ReportStatisticsCards(
                    statistics = statistics,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Search reports...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )
            
            // Active Filters Display
            if (uiState.activeFilters.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(uiState.activeFilters) { filter ->
                        FilterChip(
                            onClick = { viewModel.removeFilter(filter) },
                            label = { Text(filter.displayName, fontSize = 12.sp) },
                            selected = true,
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove filter",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
            
            // Reports List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.reports.isEmpty()) {
                EmptyReportsState(
                    onGenerateReport = { navController.navigate("reports/generate") }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.reports) { report ->
                        AdminReportCard(
                            report = report,
                            onViewDetails = {
                                selectedReport = report
                                showReportDialog = true
                            },
                            onDownload = { viewModel.downloadReport(report.id) },
                            onDelete = {
                                reportToDelete = report
                                showDeleteDialog = true
                            },
                            isDownloading = uiState.downloadingReports.contains(report.id)
                        )
                    }
                }
                
                // Pagination
                if (uiState.pagination.totalPages > 1) {
                    PaginationControls(
                        currentPage = uiState.pagination.page,
                        totalPages = uiState.pagination.totalPages,
                        onPageChange = { viewModel.loadPage(it) },
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
    
    // Error Snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar with error
            viewModel.clearError()
        }
    }
    
    // Success Message
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar with success message
            viewModel.clearSuccessMessage()
        }
    }
    
    // Filter Dialog
    if (showFilterDialog) {
        ReportFilterDialog(
            currentFilter = uiState.filter,
            onDismiss = { showFilterDialog = false },
            onApplyFilter = { filter ->
                viewModel.applyFilter(filter)
                showFilterDialog = false
            }
        )
    }
    
    // Report Details Dialog
    if (showReportDialog && selectedReport != null) {
        ReportDetailsDialog(
            report = selectedReport!!,
            onDismiss = {
                showReportDialog = false
                selectedReport = null
            },
            onDownload = { viewModel.downloadReport(selectedReport!!.id) },
            onDelete = {
                reportToDelete = selectedReport
                showDeleteDialog = true
                showReportDialog = false
                selectedReport = null
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog && reportToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                reportToDelete = null
            },
            title = { Text("Delete Report") },
            text = { 
                Text("Are you sure you want to delete the report \"${reportToDelete!!.reportTitle}\"? This action cannot be undone.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReport(reportToDelete!!.id)
                        showDeleteDialog = false
                        reportToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        reportToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ReportStatisticsCards(
    statistics: ReportStatistics,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            title = "Total Reports",
            value = statistics.totalReports.toString(),
            icon = AECIIcons.Assessment,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Downloads",
            value = statistics.totalDownloads.toString(),
            icon = Icons.Default.Download,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "This Month",
            value = "0", // Calculate current month reports
            icon = Icons.Default.Today,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportCard(
    report: Report,
    onViewDetails: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    isDownloading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewDetails() },
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
                        text = report.reportTitle,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportTypeChip(report.reportType)
                        FormatChip(report.format)
                        StatusChip(report.status)
                    }
                }
                
                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onDownload,
                        enabled = !isDownloading
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "Download",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Report Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Generated by: ${report.generatedBy.fullName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Completed: ${report.completionDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatFileSize(report.fileSize),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${report.downloadCount} downloads",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ReportTypeChip(reportType: ReportType) {
    AssistChip(
        onClick = { },
        label = { 
            Text(
                text = reportType.displayName,
                fontSize = 10.sp
            ) 
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun FormatChip(format: ExportFormat) {
    AssistChip(
        onClick = { },
        label = { 
            Text(
                text = format.name,
                fontSize = 10.sp
            ) 
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}

@Composable
fun StatusChip(status: ReportStatus) {
    val color = when (status) {
        ReportStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        ReportStatus.GENERATING -> MaterialTheme.colorScheme.secondary
        ReportStatus.PENDING -> MaterialTheme.colorScheme.tertiary
        ReportStatus.FAILED -> MaterialTheme.colorScheme.error
        ReportStatus.EXPIRED -> MaterialTheme.colorScheme.outline
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
            containerColor = color.copy(alpha = 0.2f),
            labelColor = color
        )
    )
}

@Composable
fun EmptyReportsState(
    onGenerateReport: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            AECIIcons.Assessment,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No reports found",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Generate your first report to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onGenerateReport) {
            Text("Generate Report")
        }
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onPageChange(currentPage - 1) },
            enabled = currentPage > 1
        ) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
        }
        
        Text(
            text = "Page $currentPage of $totalPages",
            style = MaterialTheme.typography.bodyMedium
        )
        
        IconButton(
            onClick = { onPageChange(currentPage + 1) },
            enabled = currentPage < totalPages
        ) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFilterDialog(
    currentFilter: ReportFilter,
    onDismiss: () -> Unit,
    onApplyFilter: (ReportFilter) -> Unit
) {
    var reportType by remember { mutableStateOf(currentFilter.reportType) }
    var format by remember { mutableStateOf(currentFilter.format) }
    var status by remember { mutableStateOf(currentFilter.status) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
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
                
                // Report Type Filter
                Text("Report Type", fontWeight = FontWeight.Medium)
                // Add dropdown for report types
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Format Filter
                Text("Format", fontWeight = FontWeight.Medium)
                // Add dropdown for formats
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Status Filter
                Text("Status", fontWeight = FontWeight.Medium)
                // Add dropdown for status
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onApplyFilter(
                                ReportFilter(
                                    reportType = reportType,
                                    format = format,
                                    status = status
                                )
                            )
                        }
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
fun ReportDetailsDialog(
    report: Report,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Report Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Report details content
                AdminReportDetailRow("Title", report.reportTitle)
                AdminReportDetailRow("Type", report.reportType.displayName)
                AdminReportDetailRow("Format", report.format.name)
                AdminReportDetailRow("Status", report.status.name)
                AdminReportDetailRow("Generated by", report.generatedBy.fullName)
                AdminReportDetailRow("Completion Date", report.completionDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")))
                AdminReportDetailRow("File Size", formatFileSize(report.fileSize))
                AdminReportDetailRow("Downloads", report.downloadCount.toString())
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onDownload) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download")
                        }
                        Button(
                            onClick = onDelete,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminReportDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> "${(bytes / (1024 * 1024))} MB"
        bytes >= 1024 -> "${(bytes / 1024)} KB"
        else -> "$bytes bytes"
    }
}

data class ActiveFilter(
    val key: String,
    val displayName: String
) 