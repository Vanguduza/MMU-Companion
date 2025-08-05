package com.aeci.mmucompanion.core.util

import android.content.Context
import com.aeci.mmucompanion.domain.model.FieldCoordinate
import com.aeci.mmucompanion.domain.model.FormFieldType
import com.aeci.mmucompanion.domain.model.FormTemplate
import com.aeci.mmucompanion.domain.model.FormSection
import com.aeci.mmucompanion.domain.model.FormField
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarkdownCoordinateParser @Inject constructor(private val context: Context) {

    fun parse(assetName: String): List<FormTemplate> {
        val templates = mutableListOf<FormTemplate>()
        val inputStream = context.assets.open(assetName)
        val reader = BufferedReader(InputStreamReader(inputStream))

        var currentTemplateName: String? = null
        var currentTemplateId: String? = null
        var currentPdfFileName: String? = null
        var currentSectionName: String? = null
        val sections = mutableListOf<FormSection>()
        val fields = mutableListOf<FormField>()

        reader.useLines { lines ->
            lines.forEach { line ->
                when {
                    line.startsWith("## Form") -> {
                        if (currentTemplateId != null && currentTemplateName != null && currentPdfFileName != null) {
                            val sectionName = currentSectionName ?: "Default"
                            sections.add(FormSection(id = nameToId(sectionName), title = sectionName, description = null, fields = fields.toList()))
                            templates.add(createTemplate(currentTemplateId!!, currentTemplateName!!, currentPdfFileName!!, sections.toList()))
                            fields.clear()
                            sections.clear()
                        }
                        val parts = line.split(":")
                        currentTemplateName = parts.getOrNull(1)?.trim()
                        currentTemplateId = currentTemplateName?.let { nameToId(it) }
                    }
                    line.startsWith("**Source PDF**") -> {
                        currentPdfFileName = line.substringAfter(":").trim().removePrefix("`").removeSuffix("`")
                    }
                    line.startsWith("###") -> {
                        if (currentSectionName != null) {
                            val sectionName = currentSectionName ?: "Default"
                            sections.add(FormSection(id = nameToId(sectionName), title = sectionName, description = null, fields = fields.toList()))
                            fields.clear()
                        }
                        currentSectionName = line.removePrefix("###").trim()
                    }
                    line.contains("FieldCoordinate") -> {
                        fields.add(parseFieldCoordinate(line))
                    }
                }
            }
        }
        // Add the last parsed template
        if (currentTemplateId != null) {
            val sectionName = currentSectionName ?: "Default"
            sections.add(FormSection(id = nameToId(sectionName), title = sectionName, description = null, fields = fields.toList()))
            templates.add(createTemplate(currentTemplateId!!, currentTemplateName!!, currentPdfFileName!!, sections.toList()))
        }

        return templates
    }

    private fun parseFieldCoordinate(line: String): FormField {
        val fieldName = extractValue(line, "fieldName")
        val x = extractValue(line, "x").toFloatOrNull() ?: 0f
        val y = extractValue(line, "y").toFloatOrNull() ?: 0f
        val width = extractValue(line, "width").toFloatOrNull() ?: 0f
        val height = extractValue(line, "height").toFloatOrNull() ?: 0f
        val fieldTypeStr = extractValue(line, "fieldType").substringAfter('.')
        val fieldType = try {
            FormFieldType.valueOf(fieldTypeStr)
        } catch (e: IllegalArgumentException) {
            FormFieldType.TEXT
        }
        val isRequired = extractValue(line, "isRequired").toBoolean()
        val placeholder = extractValue(line, "placeholder")

        return FormField(
            fieldName = fieldName,
            label = placeholder.ifEmpty { fieldName },
            fieldType = fieldType,
            isRequired = isRequired,
            placeholder = placeholder,
            x = x,
            y = y,
            width = width,
            height = height
        )
    }

    private fun extractValue(line: String, key: String): String {
        return line.split("$key =").getOrNull(1)?.split(",")?.get(0)?.trim()
            ?.removeSurrounding("\"")?.removeSuffix("f") ?: ""
    }
    
    private fun createTemplate(id: String, name: String, pdfFile: String, sections: List<FormSection>): FormTemplate {
        return FormTemplate(
            id = id,
            name = name,
            description = "Generated form template",
            formType = com.aeci.mmucompanion.domain.model.FormType.valueOf(name.replace(" ", "_").uppercase()),
            templateFile = pdfFile,
            pdfTemplate = pdfFile,
            fieldMappings = emptyList(),
            sections = sections,
            fields = sections.flatMap { it.fields },
            version = "1.0",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun nameToId(name: String): String {
        return name.lowercase().replace(" ", "_").replace("&", "and")
    }
} 