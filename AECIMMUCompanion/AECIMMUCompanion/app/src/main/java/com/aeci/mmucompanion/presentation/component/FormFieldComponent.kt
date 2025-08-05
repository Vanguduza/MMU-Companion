package com.aeci.mmucompanion.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.aeci.mmucompanion.domain.model.FormField

@Composable
fun FormFieldComponent(
    field: FormField,
    value: Any?,
    onValueChange: (Any) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    readOnly: Boolean = false
) {
    // Implementation will be added based on field type
    when (field.fieldType) {
        // Add implementations for each field type
        else -> Text(text = "Unsupported field type: ${field.fieldType}")
    }
} 