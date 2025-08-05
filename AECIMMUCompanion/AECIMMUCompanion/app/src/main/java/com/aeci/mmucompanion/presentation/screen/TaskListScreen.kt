package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aeci.mmucompanion.data.local.entity.Task
import com.aeci.mmucompanion.presentation.viewmodel.TaskViewModel

data class TaskInfo(
    val id: String,
    val title: String,
    val assignedTo: String,
    val status: String,
    val priority: String,
    val dueDate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    navController: NavController,
    mode: String, // "update_progress" or "remove"
    viewModel: TaskViewModel = hiltViewModel()
) {
    // State variables for dialogs
    var showProgressUpdateDialog by remember { mutableStateOf(false) }
    var showRemoveConfirmationDialog by remember { mutableStateOf(false) }
    var selectedTaskId by remember { mutableStateOf("") }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    
    val title = when (mode) {
        "update_progress" -> "Update Task Progress"
        "remove" -> "Remove Tasks"
        else -> "Manage Tasks"
    }
    
    // Mock data for demonstration
    val tasks = remember {
        listOf(
            TaskInfo("1", "Pump Maintenance - Unit A", "John Smith", "In Progress", "High", "2025-07-25"),
            TaskInfo("2", "Safety Inspection - Conveyor Belt", "Sarah Johnson", "Pending", "Medium", "2025-07-26"),
            TaskInfo("3", "Motor Replacement - Unit B", "Mike Wilson", "Completed", "High", "2025-07-24"),
            TaskInfo("4", "Routine Check - Filter System", "Emma Davis", "In Progress", "Low", "2025-07-25"),
            TaskInfo("5", "Electrical Maintenance", "John Smith", "Pending", "High", "2025-07-27")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Description text based on mode
        Text(
            text = when (mode) {
                "update_progress" -> "Select tasks to update their progress status"
                "remove" -> "Select tasks to remove from the system"
                else -> "Select an action for the task"
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Task Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskStatusChip("Pending", 2, MaterialTheme.colorScheme.errorContainer)
            TaskStatusChip("In Progress", 2, MaterialTheme.colorScheme.primaryContainer)
            TaskStatusChip("Completed", 1, MaterialTheme.colorScheme.secondaryContainer)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tasks List
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            items(tasks) { task ->
                TaskCard(
                    task = task,
                    mode = mode,
                    onAction = { taskId ->
                        // Handle the action based on mode
                        when (mode) {
                            "update_progress" -> {
                                // Show progress update dialog
                                selectedTaskId = taskId
                                selectedTask = tasks.find { it.id == taskId }?.let { taskInfo ->
                                    Task(
                                        id = taskInfo.id,
                                        title = taskInfo.title,
                                        description = "",
                                        status = taskInfo.status,
                                        priority = taskInfo.priority,
                                        assignedTo = taskInfo.assignedTo,
                                        completedDate = null,
                                        siteId = "default",
                                        createdAt = System.currentTimeMillis(),
                                        updatedAt = System.currentTimeMillis(),
                                        dueDate = null
                                    )
                                }
                                showProgressUpdateDialog = true
                            }
                            "remove" -> {
                                // Show confirmation dialog and remove
                                selectedTaskId = taskId
                                selectedTask = tasks.find { it.id == taskId }?.let { taskInfo ->
                                    Task(
                                        id = taskInfo.id,
                                        title = taskInfo.title,
                                        description = "",
                                        status = taskInfo.status,
                                        priority = taskInfo.priority,
                                        assignedTo = taskInfo.assignedTo,
                                        completedDate = null,
                                        siteId = "default",
                                        createdAt = System.currentTimeMillis(),
                                        updatedAt = System.currentTimeMillis(),
                                        dueDate = null
                                    )
                                }
                                showRemoveConfirmationDialog = true
                            }
                        }
                    }
                )
            }
        }
        }
    }
    
    // Progress Update Dialog
    if (showProgressUpdateDialog) {
        ProgressUpdateDialog(
            task = selectedTask,
            onDismiss = { 
                showProgressUpdateDialog = false
                selectedTask = null
                selectedTaskId = ""
            },
            onUpdateProgress = { progress ->
                // Update task progress
                selectedTask?.let { task ->
                    // TODO: Update task progress through repository
                    // This would involve calling the repository method to update the task
                }
                showProgressUpdateDialog = false
                selectedTask = null
                selectedTaskId = ""
            }
        )
    }
    
    // Remove Confirmation Dialog
    if (showRemoveConfirmationDialog) {
        RemoveConfirmationDialog(
            task = selectedTask,
            onDismiss = { 
                showRemoveConfirmationDialog = false
                selectedTask = null
                selectedTaskId = ""
            },
            onConfirmRemove = {
                // Remove task
                selectedTask?.let { task ->
                    // TODO: Remove task through repository
                    // This would involve calling the repository method to remove the task
                }
                showRemoveConfirmationDialog = false
                selectedTask = null
                selectedTaskId = ""
            }
        )
    }
}

@Composable
fun TaskStatusChip(status: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun TaskCard(
    task: TaskInfo,
    mode: String,
    onAction: (String) -> Unit
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Assigned to: ${task.assignedTo}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Due: ${task.dueDate}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (task.status) {
                                "Completed" -> MaterialTheme.colorScheme.secondaryContainer
                                "In Progress" -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.errorContainer
                            }
                        )
                    ) {
                        Text(
                            text = task.status,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { onAction(task.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (mode) {
                                "remove" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    ) {
                        Icon(
                            imageVector = when (mode) {
                                "update_progress" -> Icons.Default.Update
                                "remove" -> Icons.Default.Delete
                                else -> Icons.Default.MoreVert
                            },
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (mode) {
                                "update_progress" -> "Update"
                                "remove" -> "Remove"
                                else -> "Action"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressUpdateDialog(
    task: Task?,
    onDismiss: () -> Unit,
    onUpdateProgress: (Int) -> Unit
) {
    var progressValue by remember { mutableStateOf(0) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Update Progress",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Task: ${task?.title ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Current Progress: ${progressValue}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Slider(
                    value = progressValue.toFloat(),
                    onValueChange = { progressValue = it.toInt() },
                    valueRange = 0f..100f,
                    steps = 9,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0%", style = MaterialTheme.typography.bodySmall)
                    Text("50%", style = MaterialTheme.typography.bodySmall)
                    Text("100%", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onUpdateProgress(progressValue) }
            ) {
                Text("Update")
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
private fun RemoveConfirmationDialog(
    task: Task?,
    onDismiss: () -> Unit,
    onConfirmRemove: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Remove Task",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Are you sure you want to remove this task?",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Task: ${task?.title ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmRemove,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Remove")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
