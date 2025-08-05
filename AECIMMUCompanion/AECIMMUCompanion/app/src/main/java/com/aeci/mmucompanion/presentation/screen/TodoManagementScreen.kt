package com.aeci.mmucompanion.presentation.screen

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.aeci.mmucompanion.domain.model.Todo
import com.aeci.mmucompanion.domain.model.TodoCategory
import com.aeci.mmucompanion.domain.model.TodoPriority
import com.aeci.mmucompanion.domain.model.TodoWithDetails
import com.aeci.mmucompanion.presentation.viewmodel.TodoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoManagementScreen(
    navController: NavHostController,
    viewModel: TodoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddTodoDialog by remember { mutableStateOf(false) }
    var selectedTodo by remember { mutableStateOf<Todo?>(null) }
    var filterCategory by remember { mutableStateOf<TodoCategory?>(null) }
    var showCompletedTasks by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks & To-Do") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCompletedTasks = !showCompletedTasks }
                    ) {
                        Icon(
                            if (showCompletedTasks) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showCompletedTasks) "Hide Completed" else "Show Completed"
                        )
                    }
                    IconButton(onClick = { showAddTodoDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTodoDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
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
                
                uiState.todos.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Task,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Tasks Found",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Add your first task to get started",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Filter chips
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    onClick = { filterCategory = null },
                                    label = { Text("All") },
                                    selected = filterCategory == null
                                )
                            }
                            items(TodoCategory.values()) { category ->
                                FilterChip(
                                    onClick = { filterCategory = if (filterCategory == category) null else category },
                                    label = { Text(category.displayName) },
                                    selected = filterCategory == category
                                )
                            }
                        }
                        
                        // Tasks list
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val filteredTodos = uiState.todos.filter { todoWithDetails ->
                                val categoryMatch = filterCategory == null || todoWithDetails.todo.category == filterCategory
                                val completedMatch = showCompletedTasks || !todoWithDetails.todo.isCompleted
                                categoryMatch && completedMatch
                            }
                            
                            items(filteredTodos) { todoWithDetails ->
                                TodoCard(
                                    todoWithDetails = todoWithDetails,
                                    onToggleComplete = { viewModel.toggleTodoComplete(it.id) },
                                    onEdit = { selectedTodo = it },
                                    onDelete = { viewModel.deleteTodo(it.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Todo Dialog
    if (showAddTodoDialog || selectedTodo != null) {
        AddEditTodoDialog(
            todo = selectedTodo,
            onDismiss = { 
                showAddTodoDialog = false
                selectedTodo = null
            },
            onSave = { todo ->
                if (selectedTodo != null) {
                    viewModel.updateTodo(todo)
                } else {
                    viewModel.addTodo(todo)
                }
                showAddTodoDialog = false
                selectedTodo = null
            }
        )
    }
}

@Composable
private fun TodoCard(
    todoWithDetails: TodoWithDetails,
    onToggleComplete: (Todo) -> Unit,
    onEdit: (Todo) -> Unit,
    onDelete: (Todo) -> Unit,
    modifier: Modifier = Modifier
) {
    val todo = todoWithDetails.todo
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = todo.isCompleted,
                        onCheckedChange = { onToggleComplete(todo) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = todo.title,
                            style = if (todo.isCompleted) {
                                MaterialTheme.typography.titleMedium.copy(
                                    textDecoration = TextDecoration.LineThrough
                                )
                            } else {
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            color = if (todo.isCompleted) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        
                        if (todo.description.isNotBlank()) {
                            Text(
                                text = todo.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Priority indicator
                Surface(
                    color = when (todo.priority) {
                        TodoPriority.LOW -> MaterialTheme.colorScheme.surfaceVariant
                        TodoPriority.MEDIUM -> MaterialTheme.colorScheme.primaryContainer
                        TodoPriority.HIGH -> MaterialTheme.colorScheme.secondaryContainer
                        TodoPriority.URGENT -> MaterialTheme.colorScheme.errorContainer
                        TodoPriority.CRITICAL -> MaterialTheme.colorScheme.error
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = todo.priority.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (todo.priority) {
                            TodoPriority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
                            TodoPriority.MEDIUM -> MaterialTheme.colorScheme.onPrimaryContainer
                            TodoPriority.HIGH -> MaterialTheme.colorScheme.onSecondaryContainer
                            TodoPriority.URGENT -> MaterialTheme.colorScheme.onErrorContainer
                            TodoPriority.CRITICAL -> MaterialTheme.colorScheme.onError
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Category and due date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = todo.category.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                todo.dueDate?.let { dueDate ->
                    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    Text(
                        text = "Due: ${formatter.format(Date(dueDate))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (dueDate < System.currentTimeMillis() && !todo.isCompleted) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onEdit(todo) }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                
                TextButton(
                    onClick = { onDelete(todo) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditTodoDialog(
    todo: Todo?,
    onDismiss: () -> Unit,
    onSave: (Todo) -> Unit
) {
    var title by remember { mutableStateOf(todo?.title ?: "") }
    var description by remember { mutableStateOf(todo?.description ?: "") }
    var priority by remember { mutableStateOf(todo?.priority ?: TodoPriority.MEDIUM) }
    var category by remember { mutableStateOf(todo?.category ?: TodoCategory.GENERAL) }
    var dueDate by remember { mutableStateOf(todo?.dueDate) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (todo != null) "Edit Task" else "Add New Task") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
                
                item {
                    Column {
                        Text(
                            text = "Priority",
                            style = MaterialTheme.typography.labelMedium
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(TodoPriority.values()) { priorityOption ->
                                FilterChip(
                                    onClick = { priority = priorityOption },
                                    label = { Text(priorityOption.displayName) },
                                    selected = priority == priorityOption
                                )
                            }
                        }
                    }
                }
                
                item {
                    Column {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.labelMedium
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(TodoCategory.values()) { categoryOption ->
                                FilterChip(
                                    onClick = { category = categoryOption },
                                    label = { Text(categoryOption.displayName) },
                                    selected = category == categoryOption
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newTodo = Todo(
                        id = todo?.id ?: java.util.UUID.randomUUID().toString(),
                        title = title,
                        description = description,
                        priority = priority,
                        category = category,
                        dueDate = dueDate,
                        isCompleted = todo?.isCompleted ?: false,
                        createdByUserId = todo?.createdByUserId ?: "current_user", // TODO: Get actual user ID
                        createdAt = todo?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(newTodo)
                },
                enabled = title.isNotBlank()
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
