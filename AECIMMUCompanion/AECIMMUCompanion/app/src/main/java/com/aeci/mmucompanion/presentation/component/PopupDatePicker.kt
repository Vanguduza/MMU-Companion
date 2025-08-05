package com.aeci.mmucompanion.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopupDatePicker(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    label: String,
    isRequired: Boolean = false,
    modifier: Modifier = Modifier,
    dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    
    // Convert string date to display format
    val displayDate = try {
        if (selectedDate.isNotBlank()) {
            LocalDate.parse(selectedDate, dateFormat).format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        } else ""
    } catch (e: Exception) {
        selectedDate
    }
    
    OutlinedTextField(
        value = displayDate,
        onValueChange = { },
        label = { 
            Text(label + if (isRequired) " *" else "")
        },
        readOnly = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Select date",
                modifier = Modifier.clickable { showDatePicker = true }
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = if (isRequired && selectedDate.isBlank()) 
                MaterialTheme.colorScheme.error else 
                MaterialTheme.colorScheme.outline
        )
    )
    
    if (showDatePicker) {
        Dialog(
            onDismissRequest = { showDatePicker = false }
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Select $label",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = true
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showDatePicker = false }
                        ) {
                            Text("Cancel")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val selectedLocalDate = java.time.Instant
                                        .ofEpochMilli(millis)
                                        .atZone(java.time.ZoneId.systemDefault())
                                        .toLocalDate()
                                    onDateSelected(selectedLocalDate.format(dateFormat))
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactDatePicker(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    label: String,
    isRequired: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = label + if (isRequired) " *" else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (selectedDate.isNotBlank()) {
                        try {
                            LocalDate.parse(selectedDate).format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                        } catch (e: Exception) {
                            selectedDate.ifBlank { "Select date" }
                        }
                    } else "Select date",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedDate.isNotBlank()) 
                        MaterialTheme.colorScheme.onSurface else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Select date",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
    
    if (showDatePicker) {
        PopupDatePicker(
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            label = label,
            isRequired = isRequired
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    selectedDate: String,
    selectedTime: String,
    onDateSelected: (String) -> Unit,
    onTimeSelected: (String) -> Unit,
    dateLabel: String,
    timeLabel: String,
    isRequired: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PopupDatePicker(
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            label = dateLabel,
            isRequired = isRequired,
            modifier = Modifier.weight(1f)
        )
        
        TimePickerField(
            selectedTime = selectedTime,
            onTimeSelected = onTimeSelected,
            label = timeLabel,
            isRequired = isRequired,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    selectedTime: String,
    onTimeSelected: (String) -> Unit,
    label: String,
    isRequired: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()
    
    OutlinedTextField(
        value = selectedTime,
        onValueChange = { },
        label = { 
            Text(label + if (isRequired) " *" else "")
        },
        readOnly = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange, // You might want to use a clock icon
                contentDescription = "Select time",
                modifier = Modifier.clickable { showTimePicker = true }
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable { showTimePicker = true },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = if (isRequired && selectedTime.isBlank()) 
                MaterialTheme.colorScheme.error else 
                MaterialTheme.colorScheme.outline
        )
    )
    
    if (showTimePicker) {
        Dialog(
            onDismissRequest = { showTimePicker = false }
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Select $label",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    TimePicker(
                        state = timePickerState
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showTimePicker = false }
                        ) {
                            Text("Cancel")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        TextButton(
                            onClick = {
                                val hour = String.format("%02d", timePickerState.hour)
                                val minute = String.format("%02d", timePickerState.minute)
                                onTimeSelected("$hour:$minute")
                                showTimePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}
