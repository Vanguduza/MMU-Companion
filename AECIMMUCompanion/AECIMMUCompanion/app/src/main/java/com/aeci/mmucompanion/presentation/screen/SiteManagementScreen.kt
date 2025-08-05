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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.aeci.mmucompanion.domain.model.Site
import com.aeci.mmucompanion.domain.model.SiteWithStats
import com.aeci.mmucompanion.presentation.viewmodel.SiteManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteManagementScreen(
    navController: NavHostController,
    viewModel: SiteManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddSiteDialog by remember { mutableStateOf(false) }
    var selectedSite by remember { mutableStateOf<Site?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Site Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddSiteDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Site")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSiteDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Site")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.sites.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Sites Found",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Add your first site to get started",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.sites) { siteWithStats ->
                            SiteCard(
                                siteWithStats = siteWithStats,
                                onEdit = { selectedSite = it },
                                onToggleStatus = { viewModel.toggleSiteStatus(it.id) },
                                onDelete = { viewModel.deleteSite(it.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Site Dialog
    if (showAddSiteDialog || selectedSite != null) {
        AddEditSiteDialog(
            site = selectedSite,
            onDismiss = { 
                showAddSiteDialog = false
                selectedSite = null
            },
            onSave = { site ->
                if (selectedSite != null) {
                    viewModel.updateSite(site)
                } else {
                    viewModel.addSite(site)
                }
                showAddSiteDialog = false
                selectedSite = null
            }
        )
    }
}

@Composable
private fun SiteCard(
    siteWithStats: SiteWithStats,
    onEdit: (Site) -> Unit,
    onToggleStatus: (Site) -> Unit,
    onDelete: (Site) -> Unit
) {
    val site = siteWithStats.site
    
    Card(
        modifier = Modifier.fillMaxWidth()
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = site.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (site.isHeadOffice) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "HQ",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = "Code: ${site.code}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "${site.city}, ${site.province}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Status indicator
                Surface(
                    color = if (site.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (site.isActive) "Active" else "Inactive",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (site.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Equipment", siteWithStats.equipmentCount.toString())
                StatItem("Technicians", siteWithStats.technicianCount.toString())
                StatItem("Job Cards", siteWithStats.activeJobCards.toString())
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onEdit(site) }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                
                TextButton(onClick = { onToggleStatus(site) }) {
                    Icon(
                        if (site.isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (site.isActive) "Deactivate" else "Activate")
                }
                
                if (!site.isHeadOffice) {
                    TextButton(
                        onClick = { onDelete(site) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditSiteDialog(
    site: Site?,
    onDismiss: () -> Unit,
    onSave: (Site) -> Unit
) {
    var name by remember { mutableStateOf(site?.name ?: "") }
    var code by remember { mutableStateOf(site?.code ?: "") }
    var address by remember { mutableStateOf(site?.address ?: "") }
    var city by remember { mutableStateOf(site?.city ?: "") }
    var province by remember { mutableStateOf(site?.province ?: "") }
    var country by remember { mutableStateOf(site?.country ?: "South Africa") }
    var postalCode by remember { mutableStateOf(site?.postalCode ?: "") }
    var contactPerson by remember { mutableStateOf(site?.contactPerson ?: "") }
    var contactEmail by remember { mutableStateOf(site?.contactEmail ?: "") }
    var contactPhone by remember { mutableStateOf(site?.contactPhone ?: "") }
    var isHeadOffice by remember { mutableStateOf(site?.isHeadOffice ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (site != null) "Edit Site" else "Add New Site") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Site Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Site Code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            modifier = Modifier.weight(1f)
                        )
                        
                        OutlinedTextField(
                            value = province,
                            onValueChange = { province = it },
                            label = { Text("Province") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text("Country") },
                            modifier = Modifier.weight(2f)
                        )
                        
                        OutlinedTextField(
                            value = postalCode,
                            onValueChange = { postalCode = it },
                            label = { Text("Postal Code") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                item {
                    OutlinedTextField(
                        value = contactPerson,
                        onValueChange = { contactPerson = it },
                        label = { Text("Contact Person") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = contactEmail,
                        onValueChange = { contactEmail = it },
                        label = { Text("Contact Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Contact Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isHeadOffice,
                            onCheckedChange = { isHeadOffice = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Head Office")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newSite = Site(
                        id = site?.id ?: java.util.UUID.randomUUID().toString(),
                        name = name,
                        code = code,
                        address = address,
                        city = city,
                        province = province,
                        country = country,
                        postalCode = postalCode,
                        contactPerson = contactPerson,
                        contactEmail = contactEmail,
                        contactPhone = contactPhone,
                        isActive = site?.isActive ?: true,
                        isHeadOffice = isHeadOffice,
                        createdAt = site?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(newSite)
                },
                enabled = name.isNotBlank() && code.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
