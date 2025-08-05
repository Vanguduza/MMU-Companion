package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.presentation.viewmodel.JobCardIntegrationViewModel

/**
 * Comprehensive Job Card Form Integration Screen
 * Links job cards with forms, todos, and time tracking
 * Provides full Create/Edit/Submit workflow for job card forms
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobCardFormIntegrationScreen(
    jobCardId: String? = null,
    formType: FormType = FormType.JOB_CARD,
    onNavigateBack: () -> Unit,
    onNavigateToTodo: (String) -> Unit,
    onNavigateToTimeTracking: (String) -> Unit,
    viewModel: JobCardIntegrationViewModel = hiltViewModel()
) {
    var isCreateMode by remember { mutableStateOf(jobCardId == null) }
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(jobCardId) {
        if (jobCardId != null) {
            viewModel.loadJobCard(jobCardId)
        } else {
            viewModel.initializeNewJobCard(formType)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header Section
        JobCardFormHeader(
            isCreateMode = isCreateMode,
            jobCard = uiState.jobCard,
            onToggleMode = { isCreateMode = !isCreateMode }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Job Card Details Form
        JobCardDetailsForm(
            jobCard = uiState.jobCard,
            onFieldChange = viewModel::updateJobCardField,
            enabled = isCreateMode || uiState.jobCard?.status == JobCardStatus.PENDING
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Related Forms Section
        RelatedFormsSection(
            relatedForms = uiState.relatedForms,
            onCreateForm = viewModel::createRelatedForm,
            onOpenForm = { /* Navigate to form */ }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Todo Tasks Integration
        TodoTasksSection(
            relatedTodos = uiState.relatedTodos,
            onCreateTodo = viewModel::createTodoFromJobCard,
            onNavigateToTodo = onNavigateToTodo
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Time Tracking Section
        TimeTrackingSection(
            timeEntries = uiState.timeEntries,
            totalTime = uiState.totalTimeSpent,
            onStartTracking = viewModel::startTimeTracking,
            onStopTracking = viewModel::stopTimeTracking,
            onNavigateToTimeTracking = onNavigateToTimeTracking
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action Buttons
        JobCardActionButtons(
            isCreateMode = isCreateMode,
            jobCard = uiState.jobCard,
            onCreate = {
                viewModel.createJobCard()
                onNavigateBack()
            },
            onEdit = { isCreateMode = true },
            onSubmit = {
                viewModel.submitJobCard()
                onNavigateBack()
            },
            onCancel = onNavigateBack
        )
    }
}

@Composable
private fun JobCardFormHeader(
    isCreateMode: Boolean,
    jobCard: JobCard?,
    onToggleMode: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (isCreateMode) "Create Job Card" else "Job Card Details",
                style = MaterialTheme.typography.headlineSmall
            )
            
            jobCard?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Job #${it.id} - ${it.title}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Status: ${it.status.name}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            if (!isCreateMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onToggleMode,
                    enabled = jobCard?.status == JobCardStatus.PENDING
                ) {
                    Text("Edit")
                }
            }
        }
    }
}

@Composable
private fun JobCardDetailsForm(
    jobCard: JobCard?,
    onFieldChange: (String, Any) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Job Card Information",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title Field
            OutlinedTextField(
                value = jobCard?.title ?: "",
                onValueChange = { onFieldChange("title", it) },
                label = { Text("Job Title") },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Description Field
            OutlinedTextField(
                value = jobCard?.description ?: "",
                onValueChange = { onFieldChange("description", it) },
                label = { Text("Job Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = enabled
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Equipment Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = jobCard?.equipmentId ?: "",
                    onValueChange = { onFieldChange("equipmentId", it) },
                    label = { Text("Equipment ID") },
                    modifier = Modifier.weight(1f),
                    enabled = enabled
                )
                
                OutlinedTextField(
                    value = jobCard?.equipmentName ?: "",
                    onValueChange = { onFieldChange("equipmentName", it) },
                    label = { Text("Equipment Name") },
                    modifier = Modifier.weight(1f),
                    enabled = enabled
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Priority and Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Priority Dropdown
                var priorityExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = priorityExpanded,
                    onExpandedChange = { priorityExpanded = !priorityExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = jobCard?.priority?.name ?: "MEDIUM",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Priority") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                        modifier = Modifier.menuAnchor(),
                        enabled = enabled
                    )
                    ExposedDropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false }
                    ) {
                        JobCardPriority.values().forEach { priority ->
                            DropdownMenuItem(
                                text = { Text(priority.name) },
                                onClick = {
                                    onFieldChange("priority", priority)
                                    priorityExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Category Dropdown
                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = jobCard?.category?.name ?: "PREVENTIVE_MAINTENANCE",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor(),
                        enabled = enabled
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        JobCardCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name.replace("_", " ")) },
                                onClick = {
                                    onFieldChange("category", category)
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RelatedFormsSection(
    relatedForms: List<DigitalForm>,
    onCreateForm: (FormType) -> Unit,
    onOpenForm: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Related Forms",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Quick Form Creation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onCreateForm(FormType.TIMESHEET) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Timesheet")
                }
                
                Button(
                    onClick = { onCreateForm(FormType.FIRE_EXTINGUISHER_INSPECTION) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Inspection")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Related Forms List
            relatedForms.forEach { form ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { onOpenForm(form.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = form.formType.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Status: ${form.status.name}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Text(
                            text = form.status.name,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodoTasksSection(
    relatedTodos: List<Todo>,
    onCreateTodo: () -> Unit,
    onNavigateToTodo: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Related Tasks",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Button(onClick = onCreateTodo) {
                    Text("Add Task")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            relatedTodos.forEach { todo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { onNavigateToTodo(todo.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = todo.title,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Progress: ${todo.progressPercentage}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Text(
                            text = if (todo.isCompleted) "✓" else "○",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeTrackingSection(
    timeEntries: List<TaskTimeEntry>,
    totalTime: Long,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onNavigateToTimeTracking: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Time Tracking",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Total Time: ${totalTime / 60}h ${totalTime % 60}m",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartTracking,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Start")
                }
                
                Button(
                    onClick = onStopTracking,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Stop")
                }
                
                Button(
                    onClick = { onNavigateToTimeTracking("") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Details")
                }
            }
        }
    }
}

@Composable
private fun JobCardActionButtons(
    isCreateMode: Boolean,
    jobCard: JobCard?,
    onCreate: () -> Unit,
    onEdit: () -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f)
        ) {
            Text("Cancel")
        }
        
        if (isCreateMode) {
            Button(
                onClick = onCreate,
                modifier = Modifier.weight(1f)
            ) {
                Text("Create Job Card")
            }
        } else {
            when (jobCard?.status) {
                JobCardStatus.PENDING -> {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Edit")
                    }
                }
                JobCardStatus.IN_PROGRESS -> {
                    Button(
                        onClick = onSubmit,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Complete")
                    }
                }
                else -> {
                    Button(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("View Only")
                    }
                }
            }
        }
    }
}