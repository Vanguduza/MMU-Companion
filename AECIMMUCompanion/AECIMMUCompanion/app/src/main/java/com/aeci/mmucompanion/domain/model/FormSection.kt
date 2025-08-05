package com.aeci.mmucompanion.domain.model

data class FormSection(
    val id: String,
    val title: String,
    val description: String? = null,
    val fields: List<FormField> = emptyList(),
    val isRequired: Boolean = false,
    val displayOrder: Int = 0
)
