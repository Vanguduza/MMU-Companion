package com.aeci.mmucompanion.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.aeci.mmucompanion.presentation.component.AECIIcons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aeci.mmucompanion.domain.model.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.clickable
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class MotionEvent {
    Idle,
    Down,
    Move,
    Up
}

@Composable
fun DynamicFormRenderer(
    template: FormTemplate,
    formData: Map<String, Any>,
    onFieldValueChange: ((String, Any) -> Unit)? = null,
    onValidationError: ((String, String) -> Unit)? = null,
    readOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        items(template.sections) { section ->
            FormSectionCard(
                section = section,
                formData = formData,
                onFieldValueChange = onFieldValueChange,
                onValidationError = onValidationError,
                readOnly = readOnly
            )
        }
    }
    }
}

@Composable
fun DynamicFormRendererWithValidation(
    sections: List<FormSection>,
    formData: Map<String, Any>,
    validationErrors: Map<String, String>,
    onFieldValueChanged: (String, Any) -> Unit,
    onPhotoCapture: (String) -> Unit,
    onDatePicker: (String, Long?) -> Unit,
    onTimePicker: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(sections) { section ->
                FormSectionCard(
                    section = section,
                    formData = formData,
                    validationErrors = validationErrors,
                    onFieldValueChanged = onFieldValueChanged,
                    onPhotoCapture = onPhotoCapture,
                    onDatePicker = onDatePicker,
                    onTimePicker = onTimePicker
                )
            }
        }
    }
}

@Composable
fun FormSectionCard(
    section: FormSection,
    formData: Map<String, Any>,
    validationErrors: Map<String, String>,
    onFieldValueChanged: (String, Any) -> Unit,
    onPhotoCapture: (String) -> Unit,
    onDatePicker: (String, Long?) -> Unit,
    onTimePicker: (String, String?) -> Unit,
    readOnly: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Section Header
            Text(
                text = section.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            section.description?.let { description ->
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Section Fields
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                section.fields.forEach { field ->
                    DynamicFormField(
                        field = field,
                        value = formData[field.fieldName],
                        error = validationErrors[field.fieldName],
                        onValueChanged = { onFieldValueChanged(field.fieldName, it) },
                        onPhotoCapture = { onPhotoCapture(field.fieldName) },
                        onDatePicker = { onDatePicker(field.fieldName, it) },
                        onTimePicker = { onTimePicker(field.fieldName, it) }
                    )
                }
            }
        }
    }
}

@Composable
fun FormSectionCard(
    section: FormSection,
    formData: Map<String, Any>,
    onFieldValueChange: ((String, Any) -> Unit)? = null,
    onValidationError: ((String, String) -> Unit)? = null,
    readOnly: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Section Header
            Text(
                text = section.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            section.description?.let { description ->
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Section Fields
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                section.fields.forEach { field ->
                    // TODO: FormFieldRenderer will be implemented
                    Text(
                        text = "${field.label}: ${formData[field.fieldName] ?: "Not set"}",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DynamicFormField(
    field: FormField,
    value: Any?,
    error: String?,
    onValueChanged: (Any) -> Unit,
    onPhotoCapture: () -> Unit,
    onDatePicker: (Long?) -> Unit,
    onTimePicker: (String?) -> Unit
) {
    Column {
        when (field.fieldType) {
            FormFieldType.TEXT -> {
                OutlinedTextField(
                    value = value?.toString() ?: "",
                    onValueChange = onValueChanged,
                    label = { 
                        Row {
                            Text(field.label)
                            if (field.isRequired) {
                                Text(" *", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                )
            }
            
            FormFieldType.NUMBER, FormFieldType.INTEGER -> {
                OutlinedTextField(
                    value = value?.toString() ?: "",
                    onValueChange = { newValue ->
                        val numValue = if (field.fieldType == FormFieldType.INTEGER) {
                            newValue.toDoubleOrNull()
                        } else {
                            newValue.toIntOrNull()
                        }
                        if (numValue != null || newValue.isEmpty()) {
                            onValueChanged(newValue)
                        }
                    },
                    label = { 
                        Row {
                            Text(field.label)
                            if (field.isRequired) {
                                Text(" *", color = MaterialTheme.colorScheme.error)
                            }
                            field.unit?.let { unit ->
                                Text(" ($unit)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                )
            }
            
            FormFieldType.DATE -> {
                val dateString = if (value is String) value else ""
                PopupDatePicker(
                    selectedDate = dateString,
                    onDateSelected = { selectedDate ->
                        onValueChanged(selectedDate)
                    },
                    label = field.label,
                    isRequired = field.isRequired,
                    modifier = Modifier.fillMaxWidth()
                )
                
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
            
            FormFieldType.TIME -> {
                OutlinedTextField(
                    value = value?.toString() ?: "",
                    onValueChange = { },
                    label = { 
                        Row {
                            Text(field.label)
                            if (field.isRequired) {
                                Text(" *", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { onTimePicker(value?.toString()) }) {
                            Icon(AECIIcons.Schedule, contentDescription = "Select Time")
                        }
                    },
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                )
            }
            
            FormFieldType.DROPDOWN -> {
                // Create mutable state for managing dropdown options
                var dropdownOptions by remember { 
                    mutableStateOf(field.options ?: emptyList()) 
                }
                
                ManagedDropdownField(
                    selectedValue = value?.toString() ?: "",
                    onValueSelected = { selectedValue ->
                        onValueChanged(selectedValue)
                    },
                    label = field.label,
                    options = dropdownOptions,
                    onOptionsChanged = { newOptions ->
                        dropdownOptions = newOptions
                        // Optionally update the field template with new options
                        // This might require additional callback handling
                    },
                    isRequired = field.isRequired,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "Select an option"
                )
                
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
            
            FormFieldType.RADIO -> {
                Column {
                    Row {
                        Text(field.label)
                        if (field.isRequired) {
                            Text(" *", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    field.options?.forEach { option ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = value?.toString() == option,
                                onClick = { onValueChanged(option) }
                            )
                            Text(
                                text = option,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    
                    error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            FormFieldType.CHECKBOX -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = value as? Boolean ?: false,
                        onCheckedChange = onValueChanged
                    )
                    Text(
                        text = field.label,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    if (field.isRequired) {
                        Text(" *", color = MaterialTheme.colorScheme.error)
                    }
                }
                
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            FormFieldType.PHOTO -> {
                Column {
                    Row {
                        Text(field.label)
                        if (field.isRequired) {
                            Text(" *", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = onPhotoCapture,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            AECIIcons.Camera,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Capture Photo")
                    }
                    
                    value?.let { photoPath ->
                        Text(
                            text = "Photo captured: ${photoPath.toString().substringAfterLast("/")}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            FormFieldType.SIGNATURE -> {
                Column {
                    Row {
                        Text(field.label)
                        if (field.isRequired) {
                            Text(" *", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SignaturePad(
                        onSignatureChanged = onValueChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                    
                    error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            else -> {
                // Default text field for unsupported types
                OutlinedTextField(
                    value = value?.toString() ?: "",
                    onValueChange = onValueChanged,
                    label = { Text(field.label) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                )
            }
        }
    }
}

@Composable
fun SignaturePad(
    onSignatureChanged: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    var path by remember { mutableStateOf(Path()) }
    var motionEvent by remember { mutableStateOf(MotionEvent.Idle) }
    var currentPosition by remember { mutableStateOf(Offset.Unspecified) }

    val drawModifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { offset ->
                    currentPosition = offset
                    motionEvent = MotionEvent.Down
                },
                onDrag = { change, _ ->
                    currentPosition = change.position
                    motionEvent = MotionEvent.Move
                },
                onDragEnd = {
                    motionEvent = MotionEvent.Up
                }
            )
        }
        .background(Color.White)
        .border(1.dp, Color.Gray)

    Canvas(modifier = modifier.then(drawModifier)) {
        when (motionEvent) {
            MotionEvent.Down -> {
                path.moveTo(currentPosition.x, currentPosition.y)
            }
            MotionEvent.Move -> {
                if (currentPosition != Offset.Unspecified) {
                    path.lineTo(currentPosition.x, currentPosition.y)
                    // Consider capturing the bitmap here on every move if you need real-time updates,
                    // but it can be performance-intensive.
                }
            }
            MotionEvent.Up -> {
                // The signature is complete. Capture the final bitmap.
                // This is a simplified example. In a real app, you'd capture the drawing
                // into a Bitmap and pass that back via the onSignatureChanged callback.
                // For now, we don't have a direct way to capture the canvas content here.
                // A more complex solution involving view interoperability or a different
                // drawing library might be needed for a production-ready implementation.
            }
            else -> Unit
        }

        drawPath(
            path = path,
            color = Color.Black,
            style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

private fun dynamicFormFormatDate(timestamp: Long?): String {
    return if (timestamp != null) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
    } else {
        ""
    }
}

@Composable
fun DynamicFormRendererBasic(
    template: FormTemplate,
    formData: Map<String, Any>,
    validationErrors: Map<String, String>,
    onFieldValueChange: (String, Any) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(template.sections) { section ->
                Text(section.title, style = MaterialTheme.typography.titleMedium)
                section.fields.forEach { field ->
                    FormFieldComponent(
                        field = field,
                        value = formData[field.fieldName],
                        error = validationErrors[field.fieldName],
                        onValueChange = { value -> onFieldValueChange(field.fieldName, value) }
                    )
                }
            }
        }
    }
}
