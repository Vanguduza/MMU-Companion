@file:OptIn(ExperimentalMaterial3Api::class)

package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.aeci.mmucompanion.R
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.presentation.component.DynamicFormRenderer
import com.aeci.mmucompanion.presentation.viewmodel.FormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkingFormCreationScreen(
    navController: NavHostController,
    formType: String?,
    modifier: Modifier = Modifier,
    viewModel: FormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formTemplate by viewModel.formTemplate.collectAsState()
    val formData by viewModel.formData.collectAsState()
    val validationErrors by viewModel.validationErrors.collectAsState()
    
    LaunchedEffect(formType) {
        formType?.let { type ->
            val formTypeEnum = try {
                // Map navigation form type strings to enum values
                val mappedType = when (type.uppercase()) {
                    "SAFETY_FORM" -> "SAFETY"
                    "MAINTENANCE_FORM" -> "MAINTENANCE"
                    "INSPECTION_FORM" -> "INSPECTION"
                    "INCIDENT_FORM" -> "INCIDENT"
                    else -> type.uppercase()
                }
                FormType.valueOf(mappedType)
            } catch (e: Exception) {
                FormType.MMU_DAILY_LOG // Default fallback
            }
            viewModel.initializeForm(formTypeEnum)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = formTemplate?.name ?: "Create Form",
                        color = MaterialTheme.colorScheme.onPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveDraft() }
                    ) {
                        Text(
                            text = "Save Draft",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.error != null -> {
                    ErrorScreen(
                        error = uiState.error!!,
                        onRetry = { 
                            formType?.let { type ->
                                val formTypeEnum = try {
                                    FormType.valueOf(type.uppercase())
                                } catch (e: Exception) {
                                    FormType.MMU_DAILY_LOG
                                }
                                viewModel.initializeForm(formTypeEnum)
                            }
                        },
                        onBack = { navController.navigateUp() }
                    )
                }
                
                formTemplate != null -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Logo at the top
                        item {
                            Image(
                                painter = painterResource(id = R.drawable.aeci_logo),
                                contentDescription = "AECI Logo",
                                modifier = Modifier
                                    .height(48.dp)
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                alignment = Alignment.Center
                            )
                        }
                        // Form Header
                        item {
                            FormHeaderCard(
                                title = formTemplate!!.name,
                                description = formTemplate!!.description ?: "",
                                type = formTemplate!!.formType.name
                            )
                        }
                        
                        // Dynamic Form Renderer - Individual Sections
                        formTemplate!!.sections.forEach { section ->
                            item {
                                FormSectionCard(
                                    section = section,
                                    formData = formData,
                                    validationErrors = validationErrors.associate { it.field to it.message },
                                    onFieldValueChange = { fieldId, value ->
                                        viewModel.updateField(fieldId, value)
                                    }
                                )
                            }
                        }
                        
                        // Action Buttons
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Create Data Capture Form Button
                                OutlinedButton(
                                    onClick = { 
                                        // Navigate to data capture form for this form type
                                        navController.navigate("form_data_capture/${formTemplate!!.formType.name}")
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Create Form")
                                }
                                
                                // Submit Template Button
                                Button(
                                    onClick = { viewModel.submitForm() },
                                    modifier = Modifier.weight(1f),
                                    enabled = !uiState.isSubmitting && validationErrors.isEmpty()
                                ) {
                                    if (uiState.isSubmitting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text("Submit Template")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(80.dp)) // Extra space at bottom
                        }
                    }
                    }
                }
                
                else -> {
                    // No template loaded yet
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading form template...",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Handle successful submission
    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            navController.navigateUp()
        }
    }
}

@Composable
private fun ErrorScreen(
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error Loading Form",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                OutlinedButton(onClick = onBack) {
                    Text("Go Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun FormHeaderCard(
    title: String,
    description: String?,
    type: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            if (!description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Text(
                    text = type.replace("_", " "),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun FormSectionCard(
    section: FormSection,
    formData: Map<String, Any>,
    validationErrors: Map<String, String>,
    onFieldValueChange: (String, Any) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (!section.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = section.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Form Fields
            section.fields.forEach { field ->
                FormFieldComponent(
                    field = field,
                    value = formData[field.fieldName],
                    error = validationErrors[field.fieldName],
                    onValueChange = { value -> onFieldValueChange(field.fieldName, value) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun FormFieldComponent(
    field: FormField,
    value: Any?,
    error: String?,
    onValueChange: (Any) -> Unit
) {
    Column {
        when (field.fieldType) {
            FormFieldType.TEXT -> {
                OutlinedTextField(
                    value = value?.toString() ?: "",
                    onValueChange = onValueChange,
                    label = { Text(field.label) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                )
            }
            
            FormFieldType.MULTILINE_TEXT -> {
                OutlinedTextField(
                    value = value?.toString() ?: "",
                    onValueChange = onValueChange,
                    label = { Text(field.label) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                )
            }
            
            FormFieldType.NUMBER -> {
                OutlinedTextField(
                    value = value?.toString() ?: "",
                    onValueChange = { newValue ->
                        newValue.toDoubleOrNull()?.let { onValueChange(it) }
                    },
                    label = { Text(field.label) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    suffix = field.unit?.let { { Text(it) } }
                )
            }
            
            FormFieldType.INTEGER -> {
                OutlinedTextField(
                    value = value?.toString() ?: "",
                    onValueChange = { newValue ->
                        newValue.toIntOrNull()?.let { onValueChange(it) }
                    },
                    label = { Text(field.label) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                )
            }
            
            FormFieldType.DROPDOWN -> {
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = value?.toString() ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(field.label) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth(),
                        isError = error != null,
                        supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
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
            
            FormFieldType.CHECKBOX -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = value as? Boolean ?: false,
                        onCheckedChange = onValueChange
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = value as? Boolean ?: false,
                        onCheckedChange = onValueChange
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
            
            FormFieldType.DATE -> {
                OutlinedTextField(
                    value = value?.toString() ?: "",
                    onValueChange = { },
                    label = { Text(field.label) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { 
                            // TODO: Implement date picker
                            val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                            onValueChange(currentDate)
                        }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    },
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                )
            }
            
            FormFieldType.TIME -> {
                OutlinedTextField(
                    value = value?.toString() ?: "",
                    onValueChange = { },
                    label = { Text(field.label) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { 
                            // TODO: Implement time picker
                            val currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                            onValueChange(currentTime)
                        }) {
                            Icon(Icons.Default.AccessTime, contentDescription = "Select Time")
                        }
                    },
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                )
            }
            
            FormFieldType.PHOTO -> {
                OutlinedButton(
                    onClick = { 
                        // TODO: Implement camera functionality
                        onValueChange("photo_placeholder.jpg")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${field.label} - ${if (value != null) "Photo Taken" else "Take Photo"}")
                }
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            FormFieldType.SIGNATURE -> {
                OutlinedButton(
                    onClick = { 
                        // TODO: Implement signature pad
                        onValueChange("signature_placeholder.png")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Draw, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${field.label} - ${if (value != null) "Signed" else "Add Signature"}")
                }
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            else -> {
                // Fallback for unsupported field types
                OutlinedTextField(
                    value = value?.toString() ?: "",
                    onValueChange = onValueChange,
                    label = { Text(field.label) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                )
            }
        }
    }
}
