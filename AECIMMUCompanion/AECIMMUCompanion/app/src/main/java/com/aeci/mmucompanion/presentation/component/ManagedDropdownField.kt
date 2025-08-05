package com.aeci.mmucompanion.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagedDropdownField(
    selectedValue: String,
    onValueSelected: (String) -> Unit,
    label: String,
    options: List<String>,
    onOptionsChanged: (List<String>) -> Unit,
    isRequired: Boolean = false,
    modifier: Modifier = Modifier,
    placeholder: String = "Select an option"
) {
    var expanded by remember { mutableStateOf(false) }
    var showManagement by remember { mutableStateOf(false) }
    
    Column(modifier = modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = { },
                label = { Text(label + if (isRequired) " *" else "") },
                readOnly = true,
                trailingIcon = {
                    Row {
                        IconButton(
                            onClick = { showManagement = true }
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Manage options"
                            )
                        }
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = if (isRequired && selectedValue.isBlank()) 
                        MaterialTheme.colorScheme.error else 
                        MaterialTheme.colorScheme.outline
                ),
                placeholder = { Text(placeholder) }
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueSelected(option)
                            expanded = false
                        }
                    )
                }
                
                if (options.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No options available") },
                        onClick = { },
                        enabled = false
                    )
                }
            }
        }
    }
    
    if (showManagement) {
        DropdownManagementDialog(
            title = "Manage $label Options",
            options = options,
            onOptionsChanged = onOptionsChanged,
            onDismiss = { showManagement = false }
        )
    }
}

@Composable
fun DropdownManagementDialog(
    title: String,
    options: List<String>,
    onOptionsChanged: (List<String>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mutableOptions by remember { mutableStateOf(options.toMutableList()) }
    var newOptionText by remember { mutableStateOf("") }
    var editingIndex by remember { mutableStateOf(-1) }
    var editingText by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Add new option section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newOptionText,
                        onValueChange = { newOptionText = it },
                        label = { Text("New Option") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            if (newOptionText.isNotBlank() && !mutableOptions.contains(newOptionText)) {
                                mutableOptions.add(newOptionText)
                                newOptionText = ""
                            }
                        },
                        enabled = newOptionText.isNotBlank() && !mutableOptions.contains(newOptionText)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add option")
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Existing options list
                Text(
                    text = "Current Options (${mutableOptions.size})",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, false)
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(mutableOptions.size) { index ->
                        OptionItem(
                            option = mutableOptions[index],
                            isEditing = editingIndex == index,
                            editingText = editingText,
                            onEditingTextChange = { editingText = it },
                            onStartEdit = {
                                editingIndex = index
                                editingText = mutableOptions[index]
                            },
                            onSaveEdit = {
                                if (editingText.isNotBlank() && !mutableOptions.contains(editingText)) {
                                    mutableOptions[index] = editingText
                                    editingIndex = -1
                                    editingText = ""
                                }
                            },
                            onCancelEdit = {
                                editingIndex = -1
                                editingText = ""
                            },
                            onDelete = {
                                mutableOptions.removeAt(index)
                                if (editingIndex == index) {
                                    editingIndex = -1
                                    editingText = ""
                                } else if (editingIndex > index) {
                                    editingIndex--
                                }
                            }
                        )
                    }
                }
                
                if (mutableOptions.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = "No options available. Add some options above.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Dialog actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    TextButton(
                        onClick = {
                            onOptionsChanged(mutableOptions.toList())
                            onDismiss()
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionItem(
    option: String,
    isEditing: Boolean,
    editingText: String,
    onEditingTextChange: (String) -> Unit,
    onStartEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEditing) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        if (isEditing) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                OutlinedTextField(
                    value = editingText,
                    onValueChange = onEditingTextChange,
                    label = { Text("Edit Option") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancelEdit) {
                        Text("Cancel")
                    }
                    
                    TextButton(
                        onClick = onSaveEdit,
                        enabled = editingText.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                
                Row {
                    IconButton(onClick = onStartEdit) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit option",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete option",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
