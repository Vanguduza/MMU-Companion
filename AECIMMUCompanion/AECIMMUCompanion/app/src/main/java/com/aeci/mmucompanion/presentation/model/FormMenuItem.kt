package com.aeci.mmucompanion.presentation.model

import androidx.compose.ui.graphics.vector.ImageVector

data class FormMenuItem(
    val title: String,
    val formType: String,
    val description: String = "",
    val icon: ImageVector
)
