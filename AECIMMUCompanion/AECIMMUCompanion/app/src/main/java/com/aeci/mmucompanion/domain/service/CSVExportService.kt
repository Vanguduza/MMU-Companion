package com.aeci.mmucompanion.domain.service

import android.content.Context
import com.aeci.mmucompanion.domain.model.FormSection
import java.io.File
import java.io.FileWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CSVExportService @Inject constructor(
    private val context: Context
) {

    suspend fun generateCSV(
        formId: String,
        formSections: List<FormSection>,
        formData: Map<String, String>
    ): Result<String> {
        return try {
            val fileName = "form_${formId}_${System.currentTimeMillis()}.csv"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            FileWriter(file).use { writer ->
                // Write header
                writer.append("Section,Field Name,Field Label,Value\n")
                
                // Write data
                formSections.forEach { section ->
                    section.fields.forEach { field ->
                        val value = formData[field.fieldName] ?: ""
                        writer.append("${escapeCsvValue(section.title)},")
                        writer.append("${escapeCsvValue(field.fieldName)},")
                        writer.append("${escapeCsvValue(field.label)},")
                        writer.append("${escapeCsvValue(value)}\n")
                    }
                }
            }
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun escapeCsvValue(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\"" 
        } else {
            value
        }
    }
}
