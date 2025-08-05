@file:OptIn(ExperimentalMaterial3Api::class)

package com.aeci.mmucompanion.presentation.screen

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.aeci.mmucompanion.domain.model.ExportFormat
import com.aeci.mmucompanion.presentation.viewmodel.ExportHistoryItem
import com.aeci.mmucompanion.presentation.viewmodel.ExportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    navController: NavHostController,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Data") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Export Status
            if (uiState.isExporting) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Exporting Data...",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = uiState.exportProgress,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
            
            // Error state
            uiState.error?.let { error ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            
            // Success message
            uiState.successMessage?.let { message ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            }
            
            // Export Options Header
            item {
                Text(
                    text = "Export Options",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Export Categories
            items(getExportCategories()) { category ->
                ExportCategoryCard(
                    category = category,
                    onExport = { format ->
                        when (category.type) {
                            ExportType.FORMS -> viewModel.exportForms(format)
                            ExportType.EQUIPMENT -> viewModel.exportEquipment(format)
                            ExportType.USERS -> viewModel.exportUsers(format)
                            ExportType.REPORTS -> viewModel.exportReports(format)
                            ExportType.MAINTENANCE -> viewModel.exportMaintenance(format)
                            ExportType.AUDIT_LOG -> viewModel.exportAuditLog(format)
                        }
                    }
                )
            }
            
            // Export History
            item {
                Text(
                    text = "Recent Exports",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(uiState.exportHistory) { export ->
                ExportHistoryRow(
                    export = export,
                    onDownload = { viewModel.downloadExport(export.id) },
                    onShare = { viewModel.shareExport(export.id) }
                )
            }
            
            if (uiState.exportHistory.isEmpty()) {
                item {
                    Card {
                        Text(
                            text = "No recent exports",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportCategoryCard(
    category: ExportCategory,
    onExport: (ExportFormat) -> Unit
) {
    var showFormats by remember { mutableStateOf(false) }
    
    Card(
        onClick = { showFormats = !showFormats },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = category.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${category.recordCount} records",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Icon(
                    if (showFormats) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            
            if (showFormats) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Export Formats",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportFormatButton(
                        format = ExportFormat.PDF,
                        icon = Icons.Default.PictureAsPdf,
                        label = "PDF",
                        color = Color(0xFFE57373),
                        onClick = { onExport(ExportFormat.PDF) }
                    )
                    
                    ExportFormatButton(
                        format = ExportFormat.EXCEL,
                        icon = Icons.Default.TableChart,
                        label = "Excel",
                        color = Color(0xFF4CAF50),
                        onClick = { onExport(ExportFormat.EXCEL) }
                    )
                    
                    ExportFormatButton(
                        format = ExportFormat.CSV,
                        icon = Icons.Default.Description,
                        label = "CSV",
                        color = Color(0xFF2196F3),
                        onClick = { onExport(ExportFormat.CSV) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportFormatButton(
    format: ExportFormat,
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = color
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label)
    }
}

@Composable
private fun ExportHistoryRow(
    export: ExportHistoryItem,
    onDownload: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, iconTint) = when (export.format.uppercase()) {
                "PDF" -> Pair(Icons.Default.PictureAsPdf, Color(0xFFE57373))
                "EXCEL" -> Pair(Icons.Default.TableChart, Color(0xFF4CAF50))
                "CSV" -> Pair(Icons.Default.Description, Color(0xFF2196F3))
                "JSON" -> Pair(Icons.Default.Code, Color(0xFF9C27B0))
                else -> Pair(Icons.Default.Description, Color(0xFF9E9E9E))
            }
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = export.filename,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = export.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Created: ${export.createdDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onDownload) {
                Icon(Icons.Default.Download, contentDescription = "Download")
            }
            
            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
        }
    }
}

// Data classes for export functionality
private data class ExportCategory(
    val title: String,
    val description: String,
    val recordCount: Int,
    val icon: ImageVector,
    val type: ExportType
)

private enum class ExportType {
    FORMS,
    EQUIPMENT,
    USERS,
    REPORTS,
    MAINTENANCE,
    AUDIT_LOG
}

private fun getExportCategories(): List<ExportCategory> {
    // This data would come from the ViewModel, representing counts of exportable items
    return listOf(
        ExportCategory("Forms & Checklists", "Export completed forms, checklists, and reports.", 128, Icons.Default.Description, ExportType.FORMS),
        ExportCategory("Equipment Data", "Export equipment details, status, and history.", 32, Icons.Default.Construction, ExportType.EQUIPMENT),
        ExportCategory("User Management", "Export user lists, roles, and permissions.", 15, Icons.Default.People, ExportType.USERS),
        ExportCategory("System Reports", "Generate and export system-wide reports.", 8, Icons.Default.Assessment, ExportType.REPORTS),
        ExportCategory("Maintenance Logs", "Export all maintenance records and schedules.", 45, Icons.Default.Build, ExportType.MAINTENANCE),
        ExportCategory("Audit Log", "Export system audit trail for security and compliance.", 5, Icons.Default.Security, ExportType.AUDIT_LOG)
    )
}
