@file:OptIn(ExperimentalMaterial3Api::class)

package com.aeci.mmucompanion.presentation.screen.reports

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.runtime.rememberCoroutineScope
import com.aeci.mmucompanion.core.util.FileShareManager
import com.aeci.mmucompanion.presentation.component.AECIIcons
import kotlinx.coroutines.launch

@Composable
fun ReportGenerationScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fileShareManager = remember { FileShareManager(context) }
    
    var selectedReportType by remember { mutableStateOf("Form Summary") }
    var selectedDateRange by remember { mutableStateOf("Last 30 Days") }
    var selectedFormat by remember { mutableStateOf("PDF") }
    var includeCharts by remember { mutableStateOf(true) }
    var includeRawData by remember { mutableStateOf(false) }
    var customStartDate by remember { mutableStateOf("") }
    var customEndDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "Generate Report",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Report Type Selection
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Report Type",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        val reportTypes = listOf(
                            "Form Summary", "Equipment Status", "Maintenance Records", 
                            "User Activity", "Compliance Report", "Production Summary",
                            "Safety Incidents", "System Health", "Custom Report"
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(reportTypes) { reportType ->
                                FilterChip(
                                    selected = selectedReportType == reportType,
                                    onClick = { selectedReportType = reportType },
                                    label = { Text(reportType) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Date Range Selection
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Date Range",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        val dateRanges = listOf(
                            "Today", "Last 7 Days", "Last 30 Days", 
                            "Last 3 Months", "Last Year", "Custom Range"
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(dateRanges) { range ->
                                FilterChip(
                                    selected = selectedDateRange == range,
                                    onClick = { 
                                        selectedDateRange = range
                                        if (range == "Custom Range") {
                                            showDatePicker = true
                                        }
                                    },
                                    label = { Text(range) }
                                )
                            }
                        }
                        
                        if (selectedDateRange == "Custom Range") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = customStartDate,
                                    onValueChange = { customStartDate = it },
                                    label = { Text("Start Date") },
                                    placeholder = { Text("YYYY-MM-DD") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = customEndDate,
                                    onValueChange = { customEndDate = it },
                                    label = { Text("End Date") },
                                    placeholder = { Text("YYYY-MM-DD") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
            
            // Format and Options
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Format & Options",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        // Format Selection
                        Text(
                            text = "Export Format",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val formats = listOf("PDF", "Excel", "CSV", "JSON")
                            formats.forEach { format ->
                                FilterChip(
                                    selected = selectedFormat == format,
                                    onClick = { selectedFormat = format },
                                    label = { Text(format) }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Additional Options
                        Text(
                            text = "Include in Report",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Charts and Graphs")
                            Switch(
                                checked = includeCharts,
                                onCheckedChange = { includeCharts = it }
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Raw Data Tables")
                            Switch(
                                checked = includeRawData,
                                onCheckedChange = { includeRawData = it }
                            )
                        }
                    }
                }
            }
            
            // Generate Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        isGenerating = true
                        
                        // Generate and share the report
                        coroutineScope.launch {
                            try {
                                val dateRangeForFile = if (selectedDateRange == "Custom Range") {
                                    "${customStartDate}_to_${customEndDate}"
                                } else {
                                    selectedDateRange.replace(" ", "_")
                                }
                                
                                val reportFile = fileShareManager.generateReportFile(
                                    reportType = selectedReportType,
                                    format = selectedFormat,
                                    dateRange = dateRangeForFile,
                                    includeCharts = includeCharts,
                                    includeRawData = includeRawData
                                )
                                
                                // Simulate generation time
                                kotlinx.coroutines.delay(2000)
                                
                                val success = fileShareManager.saveAndShareReport(reportFile)
                                if (success) {
                                    android.util.Log.i("ReportGeneration", "Report generated and shared successfully")
                                }
                                
                            } catch (e: Exception) {
                                android.util.Log.e("ReportGeneration", "Error generating report", e)
                                android.widget.Toast.makeText(
                                    context, 
                                    "Error generating report: ${e.message}", 
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            } finally {
                                isGenerating = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generating...")
                    } else {
                        Icon(AECIIcons.Assessment, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate $selectedFormat Report")
                    }
                }
                
                if (isGenerating) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        }
    }
    
    // Simulate report generation completion
    LaunchedEffect(isGenerating) {
        if (isGenerating) {
            kotlinx.coroutines.delay(3000) // Simulate generation time
            isGenerating = false
        }
    }
} 