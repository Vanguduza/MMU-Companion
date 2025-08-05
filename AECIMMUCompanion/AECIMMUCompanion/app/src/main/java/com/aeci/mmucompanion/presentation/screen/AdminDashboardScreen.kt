package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.aeci.mmucompanion.presentation.viewmodel.AdminDashboardViewModel
import com.aeci.mmucompanion.domain.model.User
import com.aeci.mmucompanion.domain.model.Equipment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavHostController,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Technician Management
        item {
            AdminSectionHeader("Technician Management")
            AdminButton("Add New Technician", onClick = { viewModel.onShowCreateTechnicianDialog() })
            AdminButton("Edit Technician Profile", onClick = { navController.navigate("technician_list?mode=edit") })
            AdminButton("Remove Technician", onClick = { navController.navigate("technician_list?mode=remove") })
            AdminButton("Reset Technician Password", onClick = { navController.navigate("technician_list?mode=reset_password") })
        }

        // Equipment Management
        item {
            AdminSectionHeader("Equipment Management")
            AdminButton("Manage Equipment", onClick = { navController.navigate("equipment_list") })
            AdminButton("Add New Equipment", onClick = { navController.navigate("equipment_add") })
            AdminButton("Equipment Reports", onClick = { navController.navigate("equipment_reports") })
        }

        // Site Management
        item {
            AdminSectionHeader("Site Management")
            AdminButton("Manage Sites", onClick = { navController.navigate("site_management") })
            AdminButton("Add New Site", onClick = { navController.navigate("site_management?mode=add") })
            AdminButton("Edit Site Details", onClick = { navController.navigate("site_management?mode=edit") })
            AdminButton("Site Activity Reports", onClick = { navController.navigate("site_management?mode=reports") })
        }

        // Task & Job Card Management
        item {
            AdminSectionHeader("Task & Job Card Management")
            AdminButton("Create New Task/Job Card", onClick = { viewModel.onShowCreateTaskDialog() })
            AdminButton("Manage Job Cards", onClick = { navController.navigate("job_card_management") })
            AdminButton("Update Task Progress", onClick = { navController.navigate("task_list?mode=update_progress") })
            AdminButton("Remove Task", onClick = { navController.navigate("task_list?mode=remove") })
        }
        
        // Equipment Status Management
            item {
            AdminSectionHeader("Equipment Status")
            AdminButton("Update Equipment Status", onClick = { navController.navigate("equipment_list?mode=update_status") })
        }
        
        // Report Management
        item {
            AdminSectionHeader("Report Management")
            AdminButton("View All Reports", onClick = { navController.navigate("admin/reports") })
            AdminButton("Generate Report", onClick = { navController.navigate("reports/generate") })
            AdminButton("Download History", onClick = { navController.navigate("admin/reports?filter=downloads") })
            AdminButton("Report Statistics", onClick = { navController.navigate("admin/reports?tab=statistics") })
        }
        
        // Technician Operations (Admin can also perform technician tasks)
        // REMOVE Technician Operations section from here
    }

    if (uiState.showCreateTaskDialog) {
        CreateTaskDialog(
            uiState = uiState,
            onDismiss = viewModel::onDismissCreateTaskDialog,
            onSiteSelected = viewModel::onSiteSelected,
            onCreateTask = viewModel::createTask
        )
    }

    if (uiState.showCreateTechnicianDialog) {
        CreateTechnicianDialog(
            uiState = uiState,
            onDismiss = viewModel::onDismissCreateTechnicianDialog,
            onCreateTechnician = viewModel::createTechnician
        )
    }
}

@Composable
private fun AdminButton(text: String, onClick: () -> Unit) {
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
private fun AdminSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun CreateTaskDialog(
    uiState: com.aeci.mmucompanion.presentation.viewmodel.AdminDashboardUiState,
    onDismiss: () -> Unit,
    onSiteSelected: (String) -> Unit,
    onCreateTask: (String, String, String, String) -> Unit
) {
    var selectedTechnician by remember { mutableStateOf<com.aeci.mmucompanion.domain.model.User?>(null) }
    var selectedEquipment by remember { mutableStateOf<com.aeci.mmucompanion.domain.model.Equipment?>(null) }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = { if (!uiState.isTaskCreationLoading) onDismiss() },
        title = { Text("Create New Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (uiState.isTaskCreationLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Creating task...")
                    }
                } else {
                    // Site Dropdown
                    SiteDropDown(
                        sites = uiState.sites,
                        selectedSite = uiState.selectedSite,
                        onSiteSelected = onSiteSelected
                    )
                    
                    // Technician Dropdown
                    TechnicianDropDown(
                        label = "Assign To",
                        items = uiState.technicians,
                        selectedItem = selectedTechnician,
                        onItemSelected = { selectedTechnician = it },
                        enabled = uiState.selectedSite != null
                    )
                    
                    // Equipment Dropdown
                    EquipmentDropDown(
                        label = "Equipment",
                        items = uiState.equipment,
                        selectedItem = selectedEquipment,
                        onItemSelected = { selectedEquipment = it },
                        enabled = uiState.selectedSite != null
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Task Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreateTask(uiState.selectedSite!!, selectedTechnician!!.id, selectedEquipment!!.id, description)
                },
                enabled = !uiState.isTaskCreationLoading && 
                         uiState.selectedSite != null && 
                         selectedTechnician != null && 
                         selectedEquipment != null && 
                         description.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !uiState.isTaskCreationLoading
            ) { 
                Text("Cancel") 
            }
        }
    )
}

@Composable
private fun CreateTechnicianDialog(
    uiState: com.aeci.mmucompanion.presentation.viewmodel.AdminDashboardUiState,
    onDismiss: () -> Unit,
    onCreateTechnician: (String, String, String, String, String, String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordsMatch by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = { if (!uiState.isTechnicianCreationLoading) onDismiss() },
        title = { Text("Add New Technician") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (uiState.isTechnicianCreationLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Creating technician...")
                    }
                } else {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = employeeId,
                        onValueChange = { employeeId = it },
                        label = { Text("Employee ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { 
                            confirmPassword = it
                            passwordsMatch = password == it
                        },
                        label = { Text("Confirm Password") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = !passwordsMatch,
                        supportingText = if (!passwordsMatch) {
                            { Text("Passwords do not match") }
                        } else null
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreateTechnician(fullName, email, employeeId, username, password, confirmPassword)
                },
                enabled = !uiState.isTechnicianCreationLoading && 
                         fullName.isNotBlank() && 
                         email.isNotBlank() && 
                         employeeId.isNotBlank() && 
                         username.isNotBlank() && 
                         password.isNotBlank() && 
                         confirmPassword.isNotBlank() && 
                         passwordsMatch
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !uiState.isTechnicianCreationLoading
            ) { 
                Text("Cancel") 
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SiteDropDown(
    sites: List<String>,
    selectedSite: String?,
    onSiteSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedSite ?: "Select a Site",
            onValueChange = {},
            readOnly = true,
            label = { Text("Site/Location") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            sites.forEach { site ->
                DropdownMenuItem(
                    text = { Text(site) },
                    onClick = {
                        onSiteSelected(site)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TechnicianDropDown(
    label: String,
    items: List<com.aeci.mmucompanion.domain.model.User>,
    selectedItem: com.aeci.mmucompanion.domain.model.User?,
    onItemSelected: (com.aeci.mmucompanion.domain.model.User) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedItem?.fullName ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
            enabled = enabled
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.fullName) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EquipmentDropDown(
    label: String,
    items: List<com.aeci.mmucompanion.domain.model.Equipment>,
    selectedItem: com.aeci.mmucompanion.domain.model.Equipment?,
    onItemSelected: (com.aeci.mmucompanion.domain.model.Equipment) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedItem?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
            enabled = enabled
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.name) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
