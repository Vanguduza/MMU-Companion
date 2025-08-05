package com.aeci.mmucompanion.presentation.screen.blast

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.aeci.mmucompanion.data.model.BlastReport
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlastReportScreen(
    navController: NavHostController,
    siteId: String = "site_001", // This would come from user session
    currentUser: String = "current_user", // This would come from user session
    viewModel: BlastReportViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val blastReports by viewModel.blastReports.collectAsState()

    LaunchedEffect(siteId) {
        viewModel.loadBlastReports(siteId)
    }

    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar or handle error
        }
    }

    // Show generated report dialog
    uiState.generatedReport?.let { report ->
        BlastReportDetailDialog(
            report = report,
            onDismiss = { viewModel.clearGeneratedReport() }
        )
    }

    // Show generation dialog
    if (uiState.showGenerateDialog) {
        BlastReportGenerationDialog(
            fromDate = uiState.generationState.fromDate,
            toDate = uiState.generationState.toDate,
            bcmBlasted = uiState.generationState.bcmBlasted,
            weighbridgeTickets = uiState.generationState.weighbridgeTickets,
            fallbackToBlastHoleLog = uiState.generationState.fallbackToBlastHoleLog,
            isGenerating = uiState.isGenerating,
            onDismiss = { viewModel.hideGenerateDialog() },
            onFromDateChange = { viewModel.updateFromDate(it) },
            onToDateChange = { viewModel.updateToDate(it) },
            onBcmChange = { viewModel.updateBcmBlasted(it) },
            onAddWeighbridgeTicket = { ticketNumber, weight, date ->
                viewModel.addWeighbridgeTicket(ticketNumber, weight, date)
            },
            onRemoveWeighbridgeTicket = { viewModel.removeWeighbridgeTicket(it) },
            onToggleFallback = { viewModel.toggleFallbackToBlastHoleLog() },
            onGenerate = { viewModel.generateBlastReport(siteId, currentUser) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with Generate Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Blast Reports",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            FloatingActionButton(
                onClick = { viewModel.showGenerateDialog() },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Generate Blast Report")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Loading state
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (blastReports.isEmpty()) {
            // Empty state
            EmptyBlastReportsState(
                onGenerateClick = { viewModel.showGenerateDialog() }
            )
        } else {
            // Reports list
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(blastReports) { report ->
                        BlastReportCard(
                            report = report,
                            onViewClick = { viewModel.clearGeneratedReport() }, // This would show detail
                            onDeleteClick = { viewModel.deleteBlastReport(report.id, siteId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyBlastReportsState(
    onGenerateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Assessment,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Blast Reports",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Generate your first blast report to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onGenerateClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Blast Report")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlastReportCard(
    report: BlastReport,
    onViewClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onViewClick
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
                        text = "Blast Report",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = report.dateRange,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                Row {
                    if (report.needsCalibration) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Needs Calibration",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Key metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem("BCM Blasted", "${report.bcmBlasted}")
                MetricItem("Total Holes", "${report.totalHolesCharged}")
                MetricItem("Powder Factor", String.format("%.2f", report.powderFactor))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem("Emulsion Used", "${report.totalEmulsionUsed} kg")
                MetricItem("Source", report.emulsionSource)
                MetricItem("Machine Hours", String.format("%.1f", report.machineHours))
            }
            
            // Calibration warning
            if (report.needsCalibration) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pump calibration required (${String.format("%.1f", report.discrepancyPercent ?: 0.0)}% discrepancy)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
