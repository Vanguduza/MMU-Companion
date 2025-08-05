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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aeci.mmucompanion.data.model.BlastReport
import java.time.format.DateTimeFormatter

@Composable
fun BlastReportDetailDialog(
    report: BlastReport,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 24.dp, 24.dp, 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Blast Report",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = report.dateRange,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    
                    Row {
                        IconButton(onClick = { /* Export to PDF */ }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF")
                        }
                        IconButton(onClick = { /* Share */ }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Calibration Warning (if needed)
                    if (report.needsCalibration) {
                        item {
                            CalibrationWarningCard(report.discrepancyPercent ?: 0.0)
                        }
                    }

                    // Overview Section
                    item {
                        ReportSection(title = "Overview") {
                            OverviewContent(report)
                        }
                    }

                    // Drilling Parameters
                    item {
                        ReportSection(title = "Drilling Parameters") {
                            DrillingParametersContent(report)
                        }
                    }

                    // Emulsion Data
                    item {
                        ReportSection(title = "Emulsion Data") {
                            EmulsionDataContent(report)
                        }
                    }

                    // Machine Performance
                    item {
                        ReportSection(title = "Machine Performance") {
                            MachinePerformanceContent(report)
                        }
                    }

                    // Breakdown Notes (if any)
                    if (report.breakdownNotes.isNotEmpty()) {
                        item {
                            ReportSection(title = "Breakdown Notes") {
                                BreakdownNotesContent(report.breakdownNotes)
                            }
                        }
                    }

                    // Report Metadata
                    item {
                        ReportSection(title = "Report Information") {
                            ReportMetadataContent(report)
                        }
                    }

                    // Bottom padding
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalibrationWarningCard(discrepancyPercent: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Pump Calibration Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Discrepancy of ${String.format("%.1f", discrepancyPercent)}% detected between weighbridge and blast hole log data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun ReportSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun OverviewContent(report: BlastReport) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DataRow("BCM Blasted", "${report.bcmBlasted} m³")
        DataRow("Total Holes Charged", "${report.totalHolesCharged}")
        DataRow("Total Emulsion Used", "${report.totalEmulsionUsed} kg")
        DataRow("Powder Factor", String.format("%.2f kg/m³", report.powderFactor))
        DataRow("Emulsion Source", report.emulsionSource)
    }
}

@Composable
private fun DrillingParametersContent(report: BlastReport) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            DataRow("Average Hole Depth", String.format("%.1f m", report.averageHoleDepth))
            DataRow("Average Burden", String.format("%.1f m", report.averageBurden))
            DataRow("Average Spacing", String.format("%.1f m", report.averageSpacing))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            DataRow("Average Stemming", String.format("%.1f m", report.averageStemmingLength))
            DataRow("Average Flow Rate", String.format("%.1f L/min", report.averageFlowRate))
            DataRow("Average Delivery Rate", String.format("%.1f kg/min", report.averageDeliveryRate))
        }
    }
}

@Composable
private fun EmulsionDataContent(report: BlastReport) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DataRow("Average Final Cup Density", String.format("%.2f g/cm³", report.averageFinalCupDensity))
        DataRow("Average Pumping Pressure", String.format("%.1f bar", report.averagePumpingPressure))
        
        if (report.weighbridgeTotal != null && report.blastHoleTotal != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            
            DataRow("Weighbridge Total", "${report.weighbridgeTotal} kg")
            DataRow("Blast Hole Log Total", "${report.blastHoleTotal} kg")
            
            report.discrepancyPercent?.let { discrepancy ->
                DataRow(
                    "Discrepancy", 
                    "${String.format("%.1f", discrepancy)}%",
                    valueColor = if (discrepancy > 3.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun MachinePerformanceContent(report: BlastReport) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DataRow("Total Machine Hours", String.format("%.1f hours", report.machineHours))
        
        val efficiency = if (report.machineHours > 0) {
            (report.bcmBlasted / report.machineHours)
        } else 0.0
        
        DataRow("Production Rate", String.format("%.1f m³/hour", efficiency))
    }
}

@Composable
private fun BreakdownNotesContent(breakdownNotes: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        breakdownNotes.forEach { note ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = null,
                    modifier = Modifier.size(8.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ReportMetadataContent(report: BlastReport) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DataRow("Generated By", report.generatedBy)
        DataRow("Generated At", 
            java.time.Instant.ofEpochMilli(report.generatedAt)
                .atZone(java.time.ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        )
        DataRow("Report ID", report.id)
    }
}

@Composable
private fun DataRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}
