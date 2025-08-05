@file:OptIn(ExperimentalMaterial3Api::class)

package com.aeci.mmucompanion.presentation.screen.forms

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aeci.mmucompanion.domain.model.LegacyForm
import com.aeci.mmucompanion.domain.model.FormType
import com.aeci.mmucompanion.domain.model.FormStatus
import com.aeci.mmucompanion.presentation.component.AECIIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormsListScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") }
    
    val tabs = listOf("All Forms", "My Forms", "Drafts", "Completed")
    val categories = listOf("All", "Safety", "Maintenance", "Inspection", "Incident")
    val statuses = listOf("All", "Draft", "Pending", "Approved", "Rejected")
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = "Forms Management",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Search and Filters
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search forms...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Category Filter
            item {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }
            
            // Status Filter
            item {
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(statuses) { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status },
                            label = { Text(status) }
                        )
                    }
                }
            }
            
            // Forms List
            item {
                Text(
                    text = "Forms",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Mock forms data
            val mockForms = getMockForms()
            items(mockForms) { form ->
                FormListItem(
                    form = form,
                    onFormClick = { 
                        navController.navigate("form_details/${form.id}")
                    },
                    onEditClick = {
                        navController.navigate("form_edit/${form.id}")
                    }
                )
            }
        }
        }
        
        // Floating Action Button for creating new forms
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { navController.navigate("form_creation") },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Form")
            }
        }
    }
}

@Composable
private fun FormListItem(
    form: LegacyForm,
    onFormClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onFormClick
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
                    text = form.data["title"]?.toString() ?: "Untitled Form",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }
            }
            
            Text(
                text = form.data["description"]?.toString() ?: "No description",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category: ${form.type.displayName}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Status: ${form.status.name}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Mock data function
private fun getMockForms(): List<LegacyForm> {
    return listOf(
        LegacyForm(
            id = "1",
            type = FormType.SAFETY,
            templateId = "safety_template",
            userId = "user1",
            equipmentId = "equip1",
            shiftId = null,
            locationId = "loc1",
            status = FormStatus.DRAFT,
            data = mapOf("title" to "Daily Safety Inspection", "description" to "Daily safety checklist for all equipment"),
            createdAt = java.time.LocalDateTime.now(),
            updatedAt = java.time.LocalDateTime.now()
        ),
        LegacyForm(
            id = "2",
            type = FormType.MAINTENANCE,
            templateId = "maintenance_template",
            userId = "user1",
            equipmentId = "equip2",
            shiftId = null,
            locationId = "loc2",
            status = FormStatus.SUBMITTED,
            data = mapOf("title" to "Maintenance Request", "description" to "Request form for equipment maintenance"),
            createdAt = java.time.LocalDateTime.now(),
            updatedAt = java.time.LocalDateTime.now()
        ),
        LegacyForm(
            id = "3",
            type = FormType.INCIDENT,
            templateId = "incident_template",
            userId = "user1",
            equipmentId = null,
            shiftId = null,
            locationId = "loc3",
            status = FormStatus.APPROVED,
            data = mapOf("title" to "Incident Report", "description" to "Report safety incidents and near misses"),
            createdAt = java.time.LocalDateTime.now(),
            updatedAt = java.time.LocalDateTime.now()
        )
    )
} 