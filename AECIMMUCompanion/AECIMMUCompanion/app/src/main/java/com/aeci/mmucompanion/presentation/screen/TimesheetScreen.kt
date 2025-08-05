package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.aeci.mmucompanion.presentation.viewmodel.FormViewModel
import com.aeci.mmucompanion.domain.model.FormType
import com.aeci.mmucompanion.presentation.component.DynamicFormRenderer
import com.aeci.mmucompanion.presentation.component.DynamicFormRendererWithValidation

@Composable
fun TimesheetScreen(
    formViewModel: FormViewModel = hiltViewModel()
) {
    val uiState by formViewModel.uiState.collectAsState()
    val formTemplate by formViewModel.formTemplate.collectAsState()
    val formData by formViewModel.formData.collectAsState()
    val validationErrors by formViewModel.validationErrors.collectAsState()
    
    // Initialize form if not already done
    LaunchedEffect(Unit) {
        formViewModel.initializeForm(FormType.TIMESHEET)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        formTemplate?.let { template ->
            DynamicFormRendererWithValidation(
                sections = template.sections,
                formData = formData,
                validationErrors = validationErrors.associate { it.field to it.message },
                onFieldValueChanged = { fieldId, value ->
                    formViewModel.updateField(fieldId, value)
                },
                onPhotoCapture = { fieldId -> 
                    // TODO: Implement photo capture
                },
                onDatePicker = { fieldId, date ->
                    // TODO: Implement date picker
                },
                onTimePicker = { fieldId, time ->
                    // TODO: Implement time picker
                }
            )
        }
    }
} 