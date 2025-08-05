package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aeci.mmucompanion.presentation.component.ImagePickerComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentAddScreen(navController: NavController) {
    var equipmentName by remember { mutableStateOf("") }
    var equipmentType by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedSite by remember { mutableStateOf<String?>(null) }
    var siteExpanded by remember { mutableStateOf(false) }
    var manufacturer by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var installationDate by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var equipmentImageUri by remember { mutableStateOf<String?>(null) }
    var defectImageUri by remember { mutableStateOf<String?>(null) }
    var recordedIssues by remember { mutableStateOf("") }
    
    val equipmentTypes = listOf(
        "Pump", "Motor", "Conveyor", "Crusher", 
        "Separator", "Filter", "Mixer", "Tank",
        "Compressor", "Generator", "Transformer"
    )
    
    // Mock site data - in real app this would come from SiteViewModel
    val availableSites = listOf(
        "Head Office - Johannesburg",
        "Mine Site A - Witbank", 
        "Processing Plant - Cape Town",
        "Distribution Center - Durban",
        "Maintenance Depot - Port Elizabeth"
    )
    
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
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
                text = "Add New Equipment",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Equipment Name
        OutlinedTextField(
            value = equipmentName,
            onValueChange = { equipmentName = it },
            label = { Text("Equipment Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Equipment Type Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded && !isLoading }
        ) {
            OutlinedTextField(
                value = equipmentType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Equipment Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                enabled = !isLoading
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                equipmentTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            equipmentType = type
                            expanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Serial Number
        OutlinedTextField(
            value = serialNumber,
            onValueChange = { serialNumber = it },
            label = { Text("Serial Number") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Site Selection Dropdown
        ExposedDropdownMenuBox(
            expanded = siteExpanded,
            onExpandedChange = { siteExpanded = !siteExpanded && !isLoading }
        ) {
            OutlinedTextField(
                value = selectedSite ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Site Location") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = siteExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                enabled = !isLoading,
                placeholder = { Text("Select site location") }
            )
            ExposedDropdownMenu(
                expanded = siteExpanded,
                onDismissRequest = { siteExpanded = false }
            ) {
                availableSites.forEach { site ->
                    DropdownMenuItem(
                        text = { Text(site) },
                        onClick = {
                            selectedSite = site
                            location = site // Keep location for backward compatibility
                            siteExpanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Manufacturer
        OutlinedTextField(
            value = manufacturer,
            onValueChange = { manufacturer = it },
            label = { Text("Manufacturer") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Model
        OutlinedTextField(
            value = model,
            onValueChange = { model = it },
            label = { Text("Model") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Installation Date
        OutlinedTextField(
            value = installationDate,
            onValueChange = { installationDate = it },
            label = { Text("Installation Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            placeholder = { Text("2025-01-01") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Equipment Image
        Text(
            text = "Equipment Image",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        ImagePickerComponent(
            imageUris = if (equipmentImageUri != null) listOf(equipmentImageUri!!) else emptyList(),
            onImagesSelected = { uris -> equipmentImageUri = uris.firstOrNull() },
            onImageRemoved = { _ -> equipmentImageUri = null },
            maxImages = 1,
            allowMultiple = false
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Recorded Issues
        OutlinedTextField(
            value = recordedIssues,
            onValueChange = { recordedIssues = it },
            label = { Text("Equipment Issues/Notes") },
            placeholder = { Text("Describe any known issues or maintenance notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Defect Evidence Image
        Text(
            text = "Issue Evidence Photo (Optional)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        ImagePickerComponent(
            imageUris = if (defectImageUri != null) listOf(defectImageUri!!) else emptyList(),
            onImagesSelected = { uris -> defectImageUri = uris.firstOrNull() },
            onImageRemoved = { _ -> defectImageUri = null },
            maxImages = 1,
            allowMultiple = false
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = {
                    if (equipmentName.isNotBlank() && equipmentType.isNotBlank()) {
                        isLoading = true
                        // TODO: Implement actual equipment creation logic
                        // For now, just simulate success
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading && equipmentName.isNotBlank() && equipmentType.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add Equipment")
                }
            }
        }
    }
}
