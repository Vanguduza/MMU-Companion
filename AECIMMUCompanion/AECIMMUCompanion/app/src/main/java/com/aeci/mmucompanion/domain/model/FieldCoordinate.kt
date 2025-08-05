package com.aeci.mmucompanion.domain.model

data class FieldCoordinate(
    val fieldName: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val fieldType: String,
    val isRequired: Boolean,
    val placeholder: String
)
