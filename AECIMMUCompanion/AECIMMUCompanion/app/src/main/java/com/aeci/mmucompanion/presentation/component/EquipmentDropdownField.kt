package com.aeci.mmucompanion.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aeci.mmucompanion.domain.model.Equipment
import com.aeci.mmucompanion.presentation.viewmodel.EquipmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentDropdownField(
    selectedEquipment: Equipment?,
    onEquipmentSelected: (Equipment) -> Unit,
    onEquipmentDataAutoFilled: (Map<String, String>) -> Unit,
    label: String = "Select Equipment",
    isRequired: Boolean = true,
    modifier: Modifier = Modifier,
    equipmentViewModel: EquipmentViewModel = hiltViewModel()
) {
    val equipmentList by equipmentViewModel.equipmentList.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        equipmentViewModel.loadEquipment()
    }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedEquipment?.let { "${it.id} - ${it.name}" } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { 
                Text(label + if (isRequired) " *" else "")
            },
            trailingIcon = { 
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) 
            },
            modifier = Modifier
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = if (isRequired && selectedEquipment == null) 
                    MaterialTheme.colorScheme.error else 
                    MaterialTheme.colorScheme.outline
            )
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            equipmentList.forEach { equipment ->
                DropdownMenuItem(
                    text = { 
                        Column {
                            Text(
                                text = "${equipment.id} - ${equipment.name}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Location: ${equipment.location} | Status: ${equipment.status.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onEquipmentSelected(equipment)
                        
                        // Auto-fill related equipment data
                        val autoFilledData = mapOf(
                            "equipment_id" to equipment.id,
                            "equipment_name" to equipment.name,
                            "equipment_description" to equipment.conditionDescription,
                            "equipment_location" to equipment.location,
                            "equipment_type" to equipment.type.name,
                            "equipment_model" to equipment.model,
                            "equipment_serial_number" to equipment.serialNumber,
                            "equipment_manufacturer" to equipment.manufacturer,
                            "last_maintenance_date" to (equipment.lastMaintenanceDate?.toString() ?: ""),
                            "next_maintenance_date" to (equipment.nextMaintenanceDate?.toString() ?: ""),
                            "operating_parameters" to equipment.operatingParameters.toString(),
                            "installation_date" to equipment.installationDate.toString()
                        )
                        
                        onEquipmentDataAutoFilled(autoFilledData)
                        expanded = false
                    }
                )
            }
            
            if (equipmentList.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No equipment available") },
                    onClick = { }
                )
            }
        }
    }
}

@Composable
fun EquipmentInfoCard(
    equipment: Equipment?,
    modifier: Modifier = Modifier
) {
    if (equipment != null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Equipment Information",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        InfoRow("ID:", equipment.id)
                        InfoRow("Type:", equipment.type.name)
                        InfoRow("Location:", equipment.location)
                        InfoRow("Status:", equipment.status.name)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        InfoRow("Model:", equipment.model)
                        InfoRow("Serial:", equipment.serialNumber)
                        InfoRow("Parameters:", equipment.operatingParameters.size.toString() + " items")
                        InfoRow("Manufacturer:", equipment.manufacturer)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
