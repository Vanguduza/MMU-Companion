package com.aeci.mmucompanion.core.util

import android.content.Context
import com.aeci.mmucompanion.domain.model.Form
import com.aeci.mmucompanion.domain.model.FormField
import com.aeci.mmucompanion.domain.model.FormSection
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExcelExporter @Inject constructor(
    private val context: Context
) {
    
    suspend fun exportFormToExcel(
        form: Form,
        sections: List<FormSection>,
        outputFile: File
    ): Result<String> {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Form Data")
            
            // Create header style
            val headerStyle = workbook.createCellStyle().apply {
                fillForegroundColor = IndexedColors.BLUE.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                val font = workbook.createFont().apply {
                    color = IndexedColors.WHITE.index
                    bold = true
                }
                setFont(font)
            }
            
            var rowIndex = 0
            
            // Add form metadata
            rowIndex = addFormMetadata(sheet, form, headerStyle, rowIndex)
            rowIndex++ // Empty row
            
            // Add form sections
            sections.forEach { section ->
                rowIndex = addSectionData(sheet, section, emptyMap(), headerStyle, rowIndex)
                rowIndex++ // Empty row between sections
            }
            
            // Auto-size columns
            repeat(10) { columnIndex ->
                sheet.autoSizeColumn(columnIndex)
            }
            
            // Write to file
            FileOutputStream(outputFile).use { fileOut ->
                workbook.write(fileOut)
            }
            workbook.close()
            
            Result.success(outputFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun exportMultipleFormsToExcel(
        forms: List<Form>,
        outputFile: File
    ): Result<String> {
        return try {
            val workbook = XSSFWorkbook()
            
            // Create summary sheet
            val summarySheet = workbook.createSheet("Summary")
            createSummarySheet(summarySheet, forms, workbook)
            
            // Create individual sheets for each form type
            val formsByType = forms.groupBy { it.formType }
            formsByType.forEach { (formType, typeForms) ->
                val sheet = workbook.createSheet(formType.name.take(31)) // Excel sheet name limit
                createFormTypeSheet(sheet, typeForms, workbook)
            }
            
            // Write to file
            FileOutputStream(outputFile).use { fileOut ->
                workbook.write(fileOut)
            }
            workbook.close()
            
            Result.success(outputFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun addFormMetadata(
        sheet: Sheet,
        form: Form,
        headerStyle: CellStyle,
        startRow: Int
    ): Int {
        var rowIndex = startRow
        
        // Title row
        val titleRow = sheet.createRow(rowIndex++)
        titleRow.createCell(0).apply {
            setCellValue("AECI MMU Companion - ${form.formType.name.replace("_", " ")}")
            cellStyle = headerStyle
        }
        
        // Metadata
        val metadata = listOf(
            "Form ID" to form.id,
            "Status" to form.status.name,
            "Created" to form.createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            "Updated" to form.updatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        )
        
        metadata.forEach { (label, value) ->
            val row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(label)
            row.createCell(1).setCellValue(value)
        }
        
        return rowIndex
    }
    
    private fun addSectionData(
        sheet: Sheet,
        section: FormSection,
        formData: Map<String, Any>,
        headerStyle: CellStyle,
        startRow: Int
    ): Int {
        var rowIndex = startRow
        
        // Section header
        val sectionRow = sheet.createRow(rowIndex++)
        sectionRow.createCell(0).apply {
            setCellValue(section.title)
            cellStyle = headerStyle
        }
        
        // Field headers
        val headerRow = sheet.createRow(rowIndex++)
        headerRow.createCell(0).setCellValue("Field")
        headerRow.createCell(1).setCellValue("Value")
        headerRow.createCell(2).setCellValue("Unit")
        
        // Field data
        section.fields.forEach { field ->
            val row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(field.label)
            
            val value = formData[field.fieldName]
            when (value) {
                is String -> row.createCell(1).setCellValue(value)
                is Number -> row.createCell(1).setCellValue(value.toDouble())
                is Boolean -> row.createCell(1).setCellValue(if (value) "Yes" else "No")
                null -> row.createCell(1).setCellValue("")
                else -> row.createCell(1).setCellValue(value.toString())
            }
            
            field.unit?.let { unit ->
                row.createCell(2).setCellValue(unit)
            }
        }
        
        return rowIndex
    }
    
    private fun createSummarySheet(sheet: Sheet, forms: List<Form>, workbook: Workbook) {
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            val font = workbook.createFont().apply {
                color = IndexedColors.WHITE.index
                bold = true
            }
            setFont(font)
        }
        
        var rowIndex = 0
        
        // Headers
        val headers = listOf("Form ID", "Type", "Status", "Created", "Updated", "User ID")
        val headerRow = sheet.createRow(rowIndex++)
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                cellStyle = headerStyle
            }
        }
        
        // Data rows
        forms.forEach { form ->
            val row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(form.id)
            row.createCell(1).setCellValue(form.formType.name)
            row.createCell(2).setCellValue(form.status.name)
            row.createCell(3).setCellValue(
                form.createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            )
            row.createCell(4).setCellValue(
                form.updatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            )
            row.createCell(5).setCellValue(form.createdBy)
        }
        
        // Auto-size columns
        repeat(headers.size) { columnIndex ->
            sheet.autoSizeColumn(columnIndex)
        }
    }
    
    private fun createFormTypeSheet(sheet: Sheet, forms: List<Form>, workbook: Workbook) {
        // Implementation depends on specific form structure
        // This is a simplified version
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREEN.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            val font = workbook.createFont().apply {
                color = IndexedColors.WHITE.index
                bold = true
            }
            setFont(font)
        }
        
        var rowIndex = 0
        
        if (forms.isNotEmpty()) {
            // Create basic header row for standard form fields
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("Form ID")
            headerRow.createCell(1).setCellValue("Form Type")
            headerRow.createCell(2).setCellValue("Status")
            headerRow.createCell(3).setCellValue("Created At")
            headerRow.createCell(4).setCellValue("Updated At")
            headerRow.createCell(5).setCellValue("Created By")
            headerRow.createCell(6).setCellValue("Site Location")
            
            // Create data rows
            var rowIndex = 1
            forms.forEach { form ->
                val row = sheet.createRow(rowIndex++)
                row.createCell(0).setCellValue(form.id)
                row.createCell(1).setCellValue(form.formType.name)
                row.createCell(2).setCellValue(form.status.name)
                row.createCell(3).setCellValue(
                    form.createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                )
                row.createCell(4).setCellValue(
                    form.updatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                )
                row.createCell(5).setCellValue(form.createdBy)
                row.createCell(6).setCellValue(form.siteLocation)
            }
            
            // Auto-size columns
            repeat(7) { columnIndex ->
                sheet.autoSizeColumn(columnIndex)
            }
        }
    }
}
