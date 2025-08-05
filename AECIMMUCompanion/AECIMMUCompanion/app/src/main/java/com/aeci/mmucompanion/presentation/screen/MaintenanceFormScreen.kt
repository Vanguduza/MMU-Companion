package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.presentation.component.DatePickerComponent
import com.aeci.mmucompanion.presentation.component.ImagePickerComponent
import com.aeci.mmucompanion.presentation.viewmodel.MaintenanceFormViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceFormScreen(
    navController: NavHostController,
    viewModel: MaintenanceFormViewModel,
    equipmentId: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var reportNumber by remember { mutableStateOf("") }
    var equipmentName by remember { mutableStateOf("") }
    var equipmentModel by remember { mutableStateOf("") }
    var equipmentSerial by remember { mutableStateOf("") }
    var equipmentLocation by remember { mutableStateOf("") }
    var equipmentHours by remember { mutableStateOf("") }
    var maintenanceType by remember { mutableStateOf(MaintenanceType.PREVENTIVE) }
    var workDescription by remember { mutableStateOf("") }
    var laborHours by remember { mutableStateOf("") }
    var maintenanceDate by remember { mutableStateOf(LocalDate.now()) }
    var completionDate by remember { mutableStateOf<LocalDate?>(null) }
    var nextMaintenanceDate by remember { mutableStateOf<LocalDate?>(null) }
    var technicianName by remember { mutableStateOf("") }
    var technicianId by remember { mutableStateOf("") }
    var supervisorName by remember { mutableStateOf("") }
    var supervisorApproval by remember { mutableStateOf(false) }
    var preMaintenanceCondition by remember { mutableStateOf(ConditionRating.GOOD) }
    var postMaintenanceCondition by remember { mutableStateOf(ConditionRating.GOOD) }
    var issuesFound by remember { mutableStateOf(listOf<String>()) }
    var newIssue by remember { mutableStateOf("") }
    var recommendations by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var siteLocation by remember { mutableStateOf("") }
    var partsUsed by remember { mutableStateOf(listOf<PartUsed>()) }
    var photos by remember { mutableStateOf(listOf<String>()) }
    
    LaunchedEffect(equipmentId) {
        equipmentId?.let { viewModel.loadEquipmentData(it) }
    }
    
    LaunchedEffect(uiState.savedForm) {
        uiState.savedForm?.let {
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Maintenance Report") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val form = MaintenanceReportForm(
                                id = UUID.randomUUID().toString(),
                                createdAt = LocalDateTime.now(),
                                updatedAt = LocalDateTime.now(),
                                createdBy = uiState.currentUser?.id ?: "",
                                status = FormStatus.DRAFT,
                                equipmentId = equipmentId,
                                siteLocation = siteLocation,
                                reportNumber = reportNumber,
                                equipmentName = equipmentName,
                                equipmentModel = equipmentModel,
                                equipmentSerial = equipmentSerial,
                                equipmentLocation = equipmentLocation,
                                equipmentHours = equipmentHours.toDoubleOrNull(),
                                maintenanceType = maintenanceType,
                                workDescription = workDescription,
                                partsUsed = partsUsed,
                                laborHours = laborHours.toDoubleOrNull() ?: 0.0,
                                maintenanceDate = maintenanceDate,
                                completionDate = completionDate,
                                nextMaintenanceDate = nextMaintenanceDate,
                                technicianName = technicianName,
                                technicianId = technicianId,
                                supervisorName = supervisorName,
                                supervisorApproval = supervisorApproval,
                                preMaintenanceCondition = preMaintenanceCondition,
                                postMaintenanceCondition = postMaintenanceCondition,
                                issuesFound = issuesFound,
                                recommendations = recommendations,
                                photos = photos,
                                notes = notes
                            )
                            viewModel.saveForm(form)
                        }
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Header Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Report Information",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = reportNumber,
                            onValueChange = { reportNumber = it },
                            label = { Text("Report Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = siteLocation,
                            onValueChange = { siteLocation = it },
                            label = { Text("Site Location") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Equipment Information
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Equipment Information",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = equipmentName,
                            onValueChange = { equipmentName = it },
                            label = { Text("Equipment Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = equipmentModel,
                                onValueChange = { equipmentModel = it },
                                label = { Text("Model") },
                                modifier = Modifier.weight(1f)
                            )
                            
                            OutlinedTextField(
                                value = equipmentSerial,
                                onValueChange = { equipmentSerial = it },
                                label = { Text("Serial Number") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = equipmentLocation,
                                onValueChange = { equipmentLocation = it },
                                label = { Text("Equipment Location") },
                                modifier = Modifier.weight(1f)
                            )
                            
                            OutlinedTextField(
                                value = equipmentHours,
                                onValueChange = { equipmentHours = it },
                                label = { Text("Operating Hours") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Maintenance Details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Maintenance Details",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Maintenance Type Dropdown
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = maintenanceType.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Maintenance Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                    .fillMaxWidth()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                MaintenanceType.values().forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.name) },
                                        onClick = {
                                            maintenanceType = type
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = workDescription,
                            onValueChange = { workDescription = it },
                            label = { Text("Work Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = laborHours,
                            onValueChange = { laborHours = it },
                            label = { Text("Labor Hours") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Dates
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DatePickerComponent(
                                selectedDate = maintenanceDate,
                                onDateSelected = { maintenanceDate = it },
                                label = "Maintenance Date",
                                modifier = Modifier.weight(1f)
                            )
                            
                            DatePickerComponent(
                                selectedDate = completionDate,
                                onDateSelected = { completionDate = it },
                                label = "Completion Date",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        DatePickerComponent(
                            selectedDate = nextMaintenanceDate,
                            onDateSelected = { nextMaintenanceDate = it },
                            label = "Next Maintenance Date",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Personnel Information
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Personnel Information",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = technicianName,
                                onValueChange = { technicianName = it },
                                label = { Text("Technician Name") },
                                modifier = Modifier.weight(1f)
                            )
                            
                            OutlinedTextField(
                                value = technicianId,
                                onValueChange = { technicianId = it },
                                label = { Text("Technician ID") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = supervisorName,
                            onValueChange = { supervisorName = it },
                            label = { Text("Supervisor Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = supervisorApproval,
                                onCheckedChange = { supervisorApproval = it }
                            )
                            Text("Supervisor Approval")
                        }
                    }
                }
            }
            
            // Condition Assessment
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Condition Assessment",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Condition Rating Dropdowns
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Pre-maintenance condition
                            var preExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = preExpanded,
                                onExpandedChange = { preExpanded = !preExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = preMaintenanceCondition.name,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Pre-Maintenance Condition") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(preExpanded) },
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = preExpanded,
                                    onDismissRequest = { preExpanded = false }
                                ) {
                                    ConditionRating.values().forEach { rating ->
                                        DropdownMenuItem(
                                            text = { Text(rating.name) },
                                            onClick = {
                                                preMaintenanceCondition = rating
                                                preExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            // Post-maintenance condition
                            var postExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = postExpanded,
                                onExpandedChange = { postExpanded = !postExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = postMaintenanceCondition.name,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Post-Maintenance Condition") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(postExpanded) },
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = postExpanded,
                                    onDismissRequest = { postExpanded = false }
                                ) {
                                    ConditionRating.values().forEach { rating ->
                                        DropdownMenuItem(
                                            text = { Text(rating.name) },
                                            onClick = {
                                                postMaintenanceCondition = rating
                                                postExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Issues Found
                        Text(
                            text = "Issues Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newIssue,
                                onValueChange = { newIssue = it },
                                label = { Text("Add Issue") },
                                modifier = Modifier.weight(1f)
                            )
                            
                            Button(
                                onClick = {
                                    if (newIssue.isNotBlank()) {
                                        issuesFound = issuesFound + newIssue
                                        newIssue = ""
                                    }
                                }
                            ) {
                                Text("Add")
                            }
                        }
                        
                        // Issues List
                        issuesFound.forEachIndexed { index, issue ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = issue,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        issuesFound = issuesFound.filterIndexed { i, _ -> i != index }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = recommendations,
                            onValueChange = { recommendations = it },
                            label = { Text("Recommendations") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }
            }
            
            // Parts Used
            item {
                PartsUsedSection(
                    partsUsed = partsUsed,
                    onPartsUsedChange = { partsUsed = it }
                )
            }
            
            // Documentation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Documentation",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Photo section
                        Text(
                            text = "Photos",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        ImagePickerComponent(
                            imageUris = photos,
                            onImagesSelected = { selectedPhotos ->
                                photos = selectedPhotos
                            },
                            onImageRemoved = { removedPhoto ->
                                photos = photos.filter { it != removedPhoto }
                            }
                        )
                        
                        // Display selected photos
                        if (photos.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                photos.forEach { photo ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = photo.substringAfterLast("/"),
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                photos = photos.filter { it != photo }
                                            }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Additional Notes") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
fun PartsUsedSection(
    partsUsed: List<PartUsed>,
    onPartsUsedChange: (List<PartUsed>) -> Unit
) {
    var showAddPartDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Parts Used",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = { showAddPartDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Part")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (partsUsed.isEmpty()) {
                Text(
                    text = "No parts used",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                partsUsed.forEachIndexed { index, part ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = part.partName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Part Number: ${part.partNumber}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Quantity: ${part.quantity} | Cost: $${part.unitCost}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                part.supplier?.let { supplier ->
                                    Text(
                                        text = "Supplier: $supplier",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            IconButton(
                                onClick = {
                                    onPartsUsedChange(partsUsed.filterIndexed { i, _ -> i != index })
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove")
                            }
                        }
                    }
                    
                    if (index < partsUsed.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
    
    if (showAddPartDialog) {
        AddPartDialog(
            onDismiss = { showAddPartDialog = false },
            onAddPart = { part ->
                onPartsUsedChange(partsUsed + part)
                showAddPartDialog = false
            }
        )
    }
}

@Composable
fun AddPartDialog(
    onDismiss: () -> Unit,
    onAddPart: (PartUsed) -> Unit
) {
    var partName by remember { mutableStateOf("") }
    var partNumber by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unitCost by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Part") },
        text = {
            Column {
                OutlinedTextField(
                    value = partName,
                    onValueChange = { partName = it },
                    label = { Text("Part Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = partNumber,
                    onValueChange = { partNumber = it },
                    label = { Text("Part Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = unitCost,
                    onValueChange = { unitCost = it },
                    label = { Text("Unit Cost") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Supplier (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (partName.isNotBlank() && partNumber.isNotBlank() && 
                        quantity.isNotBlank() && unitCost.isNotBlank()) {
                        onAddPart(
                            PartUsed(
                                partName = partName,
                                partNumber = partNumber,
                                quantity = quantity.toIntOrNull() ?: 0,
                                unitCost = unitCost.toDoubleOrNull() ?: 0.0,
                                supplier = supplier.takeIf { it.isNotBlank() }
                            )
                        )
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 