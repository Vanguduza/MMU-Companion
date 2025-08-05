package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aeci.mmucompanion.domain.model.Equipment
import com.aeci.mmucompanion.domain.model.EquipmentStatus
import com.aeci.mmucompanion.presentation.viewmodel.MillwrightDashboardViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.aeci.mmucompanion.domain.model.Form
import com.aeci.mmucompanion.domain.model.FormData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MillwrightDashboardScreen(
    viewModel: MillwrightDashboardViewModel = hiltViewModel(),
    onNavigateToEquipmentDetails: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
            .padding(16.dp)
        ) {
        item {
            MillwrightSectionHeader("Equipment List")
        }

        if (uiState.isLoading) {
            item {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        }

        uiState.error?.let {
            item {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }

        items(uiState.equipmentList) { equipment ->
            MillwrightEquipmentListItem(
                equipment = equipment,
                onClick = { onNavigateToEquipmentDetails(equipment.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            MillwrightSectionHeader("Additional Reports")
        }

            item {
            ReportButton(text = "Generate Timesheet", onClick = { viewModel.onGenerateTimesheetClicked() })
            ReportButton(text = "Generate UOR", onClick = { viewModel.onGenerateUorClicked() })
            ReportButton(text = "Generate Blast Report", onClick = { viewModel.onGenerateBlastReportClicked() })
        }
    }

    if (uiState.showBlastReportDialog) {
        BlastReportSelectionDialog(
            uiState = uiState,
            onDismiss = { viewModel.onDismissBlastReportDialog() },
            onGenerate = { blastHoleLogId, dailyLogId, qualityReportId, pretaskId ->
                viewModel.generateBlastReport(blastHoleLogId, dailyLogId, qualityReportId, pretaskId)
                        }
        )
    }
}

@Composable
private fun MillwrightSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun MillwrightEquipmentListItem(
    equipment: Equipment,
    onClick: () -> Unit
) {
    val (icon, color) = when (equipment.status) {
        EquipmentStatus.OPERATIONAL -> Icons.Default.CheckCircle to Color.Green
        EquipmentStatus.MAINTENANCE -> Icons.Default.Warning to Color.Yellow
        else -> Icons.Default.Build to Color.Red // BREAKDOWN, OFFLINE, STANDBY
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = "Status", tint = color)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = equipment.name, fontSize = 18.sp)
        }
    }
}

@Composable
private fun ReportButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text)
    }
}

@Composable
private fun BlastReportSelectionDialog(
    uiState: com.aeci.mmucompanion.presentation.viewmodel.MillwrightDashboardUiState,
    onDismiss: () -> Unit,
    onGenerate: (String, String, String, String) -> Unit
) {
    var selectedBlastHoleLog by remember { mutableStateOf<FormData?>(null) }
    var selectedDailyLog by remember { mutableStateOf<FormData?>(null) }
    var selectedQualityReport by remember { mutableStateOf<FormData?>(null) }
    var selectedPretask by remember { mutableStateOf<FormData?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate Blast Report") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (uiState.isBlastDialogLoading) {
                    CircularProgressIndicator()
                } else {
                    FormDropDown(
                        label = "Blast Hole Log",
                        items = uiState.blastHoleLogs,
                        selectedItem = selectedBlastHoleLog,
                        onItemSelected = { selectedBlastHoleLog = it }
                    )
                    FormDropDown(
                        label = "Production Daily Log",
                        items = uiState.dailyLogs,
                        selectedItem = selectedDailyLog,
                        onItemSelected = { selectedDailyLog = it }
                    )
                    FormDropDown(
                        label = "Quality Report",
                        items = uiState.qualityReports,
                        selectedItem = selectedQualityReport,
                        onItemSelected = { selectedQualityReport = it }
                    )
                    FormDropDown(
                        label = "Pre-Task Assessment",
                        items = uiState.pretasks,
                        selectedItem = selectedPretask,
                        onItemSelected = { selectedPretask = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGenerate(
                        selectedBlastHoleLog!!.id,
                        selectedDailyLog!!.id,
                        selectedQualityReport!!.id,
                        selectedPretask!!.id
                    )
                },
                enabled = selectedBlastHoleLog != null && selectedDailyLog != null && selectedQualityReport != null && selectedPretask != null
            ) {
                Text("Generate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun FormDropDown(
    label: String,
    items: List<FormData>,
    selectedItem: FormData?,
    onItemSelected: (FormData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selectedItem?.formType?.displayName ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.formType.displayName) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
