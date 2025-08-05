package com.aeci.mmucompanion.domain.model

import com.aeci.mmucompanion.domain.model.FormFieldType

import com.aeci.mmucompanion.domain.model.FormType

import java.time.LocalDateTime

// Core interfaces and data classes for comprehensive form system with complete PDF coordinate mapping

/**
 * Logo coordinate specification for exact PDF positioning
 */
data class LogoCoordinate(
    val logoType: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val imagePath: String = "assets/images/aeci-logo.png",
    val scalingMode: ScalingMode = ScalingMode.MAINTAIN_ASPECT_RATIO
)

enum class ScalingMode {
    MAINTAIN_ASPECT_RATIO,
    STRETCH_TO_FIT,
    CENTER_CROP
}

/**
 * Static text positioning for form headers and labels
 */
data class StaticTextCoordinate(
    val text: String,
    val x: Float,
    val y: Float,
    val fontSize: Float = 12f,
    val fontWeight: String = "normal",
    val fontColor: String = "#000000",
    val alignment: TextAlignment = TextAlignment.LEFT
)

/**
 * Header coordinates for form metadata
 */
data class HeaderCoordinate(
    val label: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

enum class TextAlignment {
    LEFT, CENTER, RIGHT, JUSTIFY
}

/**
 * Form coordinate mapping for precise field positioning
 */
data class FormCoordinate(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

/**
 * PDF field mapping for output generation
 */
data class PdfFieldMapping(
    val fieldName: String,
    val pdfFieldName: String,
    val coordinate: FormCoordinate,
    val fieldType: FormFieldType,
    val formatting: FieldFormatting? = null
)

data class FieldFormatting(
    val dateFormat: String? = null,
    val numberFormat: String? = null,
    val textTransform: TextTransform? = null,
    val alignment: TextAlignment = TextAlignment.LEFT
)

enum class TextTransform {
    UPPERCASE, LOWERCASE, CAPITALIZE
}

/**
 * Form relationships for data integration
 */
data class FormRelationship(
    val sourceField: String,
    val targetForm: FormType,
    val targetField: String,
    val relationshipType: RelationshipType
)

enum class RelationshipType {
    LOOKUP, UPDATE, VALIDATE, CASCADE
}

/**
 * Form relationship updates for automated workflows
 */
data class FormRelationshipUpdate(
    val targetFormType: FormType,
    val fieldMappings: Map<String, String>,
    val updateCondition: String? = null
)

/**
 * Validation rules for form fields
 */
data class ValidationRule(
    val fieldName: String,
    val ruleName: String,
    val expression: String,
    val errorMessage: String
)

// Form structure data classes
data class FormDefinition(
    val id: String,
    val name: String,
    val description: String,
    val sections: List<FormSection>
)

// Use FormSection, FormFieldType, FormType, and FormStatus from their respective dedicated files
// Import these from FormField.kt and FormModels.kt to avoid duplication

/**
 * Enhanced digital form template interface - UNIFIED VERSION
 */
interface DigitalFormTemplate {
    val templateId: String
    val title: String
    val version: String
    val formType: FormType
    val pdfFileName: String
    
    // Logo and branding coordinates  
    val logoCoordinates: List<LogoCoordinate>
    val staticTextCoordinates: List<StaticTextCoordinate>
    val headerCoordinates: List<HeaderCoordinate>
    
    // Form relationships
    val formRelationships: List<FormRelationship>
    
    fun getFormTemplate(): FormDefinition
    fun getValidationRules(): List<ValidationRule>
    fun getRelatedFormUpdates(): List<FormRelationshipUpdate>
    fun getPdfFieldMappings(): Map<String, PdfFieldMapping>
}

// FormStatus is defined in FormModels.kt

