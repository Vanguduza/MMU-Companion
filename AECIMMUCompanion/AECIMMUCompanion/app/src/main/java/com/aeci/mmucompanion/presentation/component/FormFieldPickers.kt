package com.aeci.mmucompanion.presentation.component

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Date picker component that opens Android's native date picker
 */
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "DD/MM/YYYY",
    modifier: Modifier = Modifier,
    error: String? = null,
    isRequired: Boolean = false
) {
    val context = LocalContext.current
    
    OutlinedTextField(
        value = value,
        onValueChange = { /* Read-only, handled by picker */ },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        isError = error != null,
        singleLine = true,
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = {
                val calendar = Calendar.getInstance()
                
                // Parse existing date if available
                try {
                    if (value.isNotEmpty()) {
                        val parts = value.split("/")
                        if (parts.size == 3) {
                            calendar.set(Calendar.DAY_OF_MONTH, parts[0].toInt())
                            calendar.set(Calendar.MONTH, parts[1].toInt() - 1)
                            calendar.set(Calendar.YEAR, parts[2].toInt())
                        }
                    }
                } catch (e: Exception) {
                    // Use current date if parsing fails
                }
                
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                        onValueChange(formattedDate)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
            }
        },
        supportingText = if (error != null) {
            { Text(error, color = MaterialTheme.colorScheme.error) }
        } else null
    )
}

/**
 * Time picker component that opens Android's native time picker
 */
@Composable
fun TimePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "HH:MM",
    modifier: Modifier = Modifier,
    error: String? = null,
    isRequired: Boolean = false
) {
    val context = LocalContext.current
    
    OutlinedTextField(
        value = value,
        onValueChange = { /* Read-only, handled by picker */ },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        isError = error != null,
        singleLine = true,
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = {
                val calendar = Calendar.getInstance()
                
                // Parse existing time if available
                try {
                    if (value.isNotEmpty()) {
                        val parts = value.split(":")
                        if (parts.size >= 2) {
                            calendar.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                            calendar.set(Calendar.MINUTE, parts[1].toInt())
                        }
                    }
                } catch (e: Exception) {
                    // Use current time if parsing fails
                }
                
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                        onValueChange(formattedTime)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true // 24-hour format
                ).show()
            }) {
                Icon(Icons.Default.AccessTime, contentDescription = "Select Time")
            }
        },
        supportingText = if (error != null) {
            { Text(error, color = MaterialTheme.colorScheme.error) }
        } else null
    )
}

/**
 * Date and time picker component
 */
@Composable
fun DateTimePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "DD/MM/YYYY HH:MM",
    modifier: Modifier = Modifier,
    error: String? = null,
    isRequired: Boolean = false
) {
    val context = LocalContext.current
    
    OutlinedTextField(
        value = value,
        onValueChange = { /* Read-only, handled by picker */ },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        isError = error != null,
        singleLine = true,
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = {
                val calendar = Calendar.getInstance()
                
                // Parse existing datetime if available
                try {
                    if (value.isNotEmpty()) {
                        val parts = value.split(" ")
                        if (parts.size >= 2) {
                            val dateParts = parts[0].split("/")
                            val timeParts = parts[1].split(":")
                            
                            if (dateParts.size == 3 && timeParts.size >= 2) {
                                calendar.set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
                                calendar.set(Calendar.MONTH, dateParts[1].toInt() - 1)
                                calendar.set(Calendar.YEAR, dateParts[2].toInt())
                                calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                                calendar.set(Calendar.MINUTE, timeParts[1].toInt())
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Use current datetime if parsing fails
                }
                
                // First show date picker
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        // Then show time picker
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                val formattedDateTime = String.format(
                                    "%02d/%02d/%04d %02d:%02d",
                                    dayOfMonth, month + 1, year, hourOfDay, minute
                                )
                                onValueChange(formattedDateTime)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }) {
                Icon(Icons.Default.Schedule, contentDescription = "Select Date and Time")
            }
        },
        supportingText = if (error != null) {
            { Text(error, color = MaterialTheme.colorScheme.error) }
        } else null
    )
}

/**
 * Equipment picker component
 */
@Composable
fun EquipmentPickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "Select Equipment",
    modifier: Modifier = Modifier,
    error: String? = null,
    equipmentList: List<String> = emptyList(),
    onEquipmentListRequest: () -> Unit = {}
) {
    var showDropdown by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        onEquipmentListRequest()
    }
    
    OutlinedTextField(
        value = value,
        onValueChange = { },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        isError = error != null,
        singleLine = true,
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDropdown = !showDropdown }) {
                Icon(
                    if (showDropdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Select Equipment"
                )
            }
        },
        supportingText = if (error != null) {
            { Text(error, color = MaterialTheme.colorScheme.error) }
        } else null
    )
    
    DropdownMenu(
        expanded = showDropdown,
        onDismissRequest = { showDropdown = false }
    ) {
        if (equipmentList.isEmpty()) {
            DropdownMenuItem(
                text = { Text("Loading equipment...") },
                onClick = { }
            )
        } else {
            equipmentList.forEach { equipment ->
                DropdownMenuItem(
                    text = { Text(equipment) },
                    onClick = {
                        onValueChange(equipment)
                        showDropdown = false
                    }
                )
            }
        }
    }
}

/**
 * Site picker component
 */
@Composable
fun SitePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "Select Site",
    modifier: Modifier = Modifier,
    error: String? = null,
    siteList: List<String> = emptyList(),
    onSiteListRequest: () -> Unit = {}
) {
    var showDropdown by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        onSiteListRequest()
    }
    
    OutlinedTextField(
        value = value,
        onValueChange = { },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        isError = error != null,
        singleLine = true,
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDropdown = !showDropdown }) {
                Icon(
                    if (showDropdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Select Site"
                )
            }
        },
        supportingText = if (error != null) {
            { Text(error, color = MaterialTheme.colorScheme.error) }
        } else null
    )
    
    DropdownMenu(
        expanded = showDropdown,
        onDismissRequest = { showDropdown = false }
    ) {
        if (siteList.isEmpty()) {
            DropdownMenuItem(
                text = { Text("Loading sites...") },
                onClick = { }
            )
        } else {
            siteList.forEach { site ->
                DropdownMenuItem(
                    text = { Text(site) },
                    onClick = {
                        onValueChange(site)
                        showDropdown = false
                    }
                )
            }
        }
    }
}

/**
 * Employee picker component
 */
@Composable
fun EmployeePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "Select Employee",
    modifier: Modifier = Modifier,
    error: String? = null,
    employeeList: List<String> = emptyList(),
    onEmployeeListRequest: () -> Unit = {}
) {
    var showDropdown by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        onEmployeeListRequest()
    }
    
    OutlinedTextField(
        value = value,
        onValueChange = { },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        isError = error != null,
        singleLine = true,
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDropdown = !showDropdown }) {
                Icon(
                    if (showDropdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Select Employee"
                )
            }
        },
        supportingText = if (error != null) {
            { Text(error, color = MaterialTheme.colorScheme.error) }
        } else null
    )
    
    DropdownMenu(
        expanded = showDropdown,
        onDismissRequest = { showDropdown = false }
    ) {
        if (employeeList.isEmpty()) {
            DropdownMenuItem(
                text = { Text("Loading employees...") },
                onClick = { }
            )
        } else {
            employeeList.forEach { employee ->
                DropdownMenuItem(
                    text = { Text(employee) },
                    onClick = {
                        onValueChange(employee)
                        showDropdown = false
                    }
                )
            }
        }
    }
}
