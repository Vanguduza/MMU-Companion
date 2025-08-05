package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aeci.mmucompanion.domain.model.EquipmentStatus

@Composable
fun EquipmentDetailsScreen(equipmentId: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Equipment Details Section
        item {
            EquipmentDetailsSectionHeader("Equipment Details")
            Text("ID: $equipmentId")
            Text("Status: ${EquipmentStatus.OPERATIONAL}") // Placeholder
            Text("Model: MMU-X-GEN-2") // Placeholder
            Text("Last Service: 2023-10-26") // Placeholder
        }

        // Maintenance Reports Section
        item {
            EquipmentDetailsSectionHeader("Maintenance Reports")
            val reports = listOf("90-Day Pump Inspection", "Monthly Process Maintenance") // Placeholder
            reports.forEach { reportName ->
                Text(reportName, modifier = Modifier.padding(bottom = 8.dp))
            }
        }

        // Issues & Tasks Section
        item {
            EquipmentDetailsSectionHeader("Issues & Tasks")
            val tasks = listOf(
                "Task: Replace hydraulic filter - PENDING",
                "Issue: Low pressure on Pump A - NOTED",
                "Task: Calibrate auger speed - COMPLETED"
            ) // Placeholder
            tasks.forEach { task ->
                Text(task, modifier = Modifier.padding(bottom = 8.dp))
            }
        }
    }
    }
}

@Composable
private fun EquipmentDetailsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
} 