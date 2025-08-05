package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.aeci.mmucompanion.domain.model.FormField
import com.aeci.mmucompanion.domain.model.FormFieldType
import com.aeci.mmucompanion.presentation.viewmodel.FormDataEntryViewModel
import com.aeci.mmucompanion.presentation.component.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDataEntryScreen(
    formType: String,
    viewModel: FormDataEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(formType) {
        viewModel.loadForm(formType)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = uiState.formTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                Button(
                    onClick = { viewModel.saveForm() },
                    enabled = !uiState.isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = { viewModel.exportPDF() },
                    enabled = !uiState.isLoading && uiState.canExport
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Export PDF",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export PDF")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Form Content
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                // Form sections
                uiState.formSections.forEach { section ->
                    item {
                        FormSectionHeader(section.title)
                    }
                    
                    items(section.fields) { field ->
                        FormFieldComponent(
                            field = field,
                            value = uiState.formData[field.fieldName] ?: "",
                            onValueChange = { newValue ->
                                viewModel.updateFieldValue(field.fieldName, newValue)
                            },
                            error = uiState.fieldErrors[field.fieldName]
                        )
                    }
                }
            }
        }
        }
    }
    
    // Error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar
        }
    }
}

@Composable
private fun FormSectionHeader(title: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormFieldComponent(
    field: FormField,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        when (field.fieldType) {
            FormFieldType.TEXT -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(field.label) },
                    placeholder = { Text(field.placeholder ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    singleLine = true,
                    supportingText = if (error != null) {
                        { Text(error, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
            }
            
            FormFieldType.MULTILINE_TEXT -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(field.label) },
                    placeholder = { Text(field.placeholder ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    minLines = 3,
                    maxLines = 5,
                    supportingText = if (error != null) {
                        { Text(error, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
            }
            
            FormFieldType.NUMBER -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(field.label) },
                    placeholder = { Text(field.placeholder ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    supportingText = if (error != null) {
                        { Text(error, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
            }
            
            FormFieldType.INTEGER -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(field.label) },
                    placeholder = { Text(field.placeholder ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    supportingText = if (error != null) {
                        { Text(error, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
            }
            
            FormFieldType.DATE -> {
                DatePickerField(
                    value = value,
                    onValueChange = onValueChange,
                    label = field.label,
                    modifier = Modifier.fillMaxWidth(),
                    error = error,
                    isRequired = field.isRequired
                )
            }
            
            FormFieldType.TIME -> {
                TimePickerField(
                    value = value,
                    onValueChange = onValueChange,
                    label = field.label,
                    modifier = Modifier.fillMaxWidth(),
                    error = error,
                    isRequired = field.isRequired
                )
            }
            
            FormFieldType.DATETIME -> {
                DateTimePickerField(
                    value = value,
                    onValueChange = onValueChange,
                    label = field.label,
                    modifier = Modifier.fillMaxWidth(),
                    error = error,
                    isRequired = field.isRequired
                )
            }
            
            FormFieldType.CHECKBOX -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = value.toBoolean(),
                        onCheckedChange = { onValueChange(it.toString()) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = field.label,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
            
            FormFieldType.BOOLEAN -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = value.toBoolean(),
                        onCheckedChange = { onValueChange(it.toString()) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = field.label,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
            
            FormFieldType.RADIO -> {
                Column {
                    Text(
                        text = field.label,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    field.options?.forEach { option ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = value == option,
                                onClick = { onValueChange(option) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(option)
                        }
                    }
                    if (error != null) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            FormFieldType.DROPDOWN -> {
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(field.label) },
                        placeholder = { Text(field.placeholder ?: "Select option") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth(),
                        isError = error != null,
                        supportingText = if (error != null) {
                            { Text(error, color = MaterialTheme.colorScheme.error) }
                        } else null
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        field.options?.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    onValueChange(option)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            FormFieldType.SIGNATURE -> {
                SignatureCaptureField(
                    value = value,
                    onValueChange = onValueChange,
                    label = field.label,
                    modifier = Modifier.fillMaxWidth(),
                    error = error,
                    isRequired = field.isRequired
                )
            }
            
            FormFieldType.PHOTO -> {
                CameraField(
                    value = if (value.isEmpty()) emptyList() else value.split(","),
                    onValueChange = { photos -> onValueChange(photos.joinToString(",")) },
                    label = field.label,
                    modifier = Modifier.fillMaxWidth(),
                    error = error,
                    maxPhotos = 5,
                    isRequired = field.isRequired
                )
            }
            
            FormFieldType.BARCODE -> {
                Button(
                    onClick = { /* TODO: Open barcode scanner */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan Barcode")
                }
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            FormFieldType.EQUIPMENT_ID -> {
                EquipmentPickerField(
                    value = value,
                    onValueChange = onValueChange,
                    label = field.label,
                    placeholder = field.placeholder ?: "Select Equipment",
                    modifier = Modifier.fillMaxWidth(),
                    error = error,
                    equipmentList = listOf("MMU-001", "MMU-002", "Pump-A1", "Crusher-B2", "Conveyor-C3"),
                    onEquipmentListRequest = { /* TODO: Load equipment from repository */ }
                )
            }
            
            FormFieldType.SITE_CODE -> {
                SitePickerField(
                    value = value,
                    onValueChange = onValueChange,
                    label = field.label,
                    placeholder = field.placeholder ?: "Select Site",
                    modifier = Modifier.fillMaxWidth(),
                    error = error,
                    siteList = listOf("Site A", "Site B", "Main Plant", "Processing Unit", "Storage Area"),
                    onSiteListRequest = { /* TODO: Load sites from repository */ }
                )
            }
            
            FormFieldType.EMPLOYEE_ID -> {
                EmployeePickerField(
                    value = value,
                    onValueChange = onValueChange,
                    label = field.label,
                    placeholder = field.placeholder ?: "Select Employee",
                    modifier = Modifier.fillMaxWidth(),
                    error = error,
                    employeeList = listOf("John Smith", "Jane Doe", "Mike Johnson", "Sarah Wilson", "Dave Brown"),
                    onEmployeeListRequest = { /* TODO: Load employees from repository */ }
                )
            }
        }
    }
} 