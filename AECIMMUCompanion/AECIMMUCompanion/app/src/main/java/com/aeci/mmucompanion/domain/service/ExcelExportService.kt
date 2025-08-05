package com.aeci.mmucompanion.domain.service

import android.content.Context
import com.aeci.mmucompanion.domain.model.FormSection
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExcelExportService @Inject constructor(
    private val context: Context
) {

    suspend fun generateExcel(
        formId: String,
        formSections: List<FormSection>,
        formData: Map<String, String>
    ): Result<String> {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Form Data")
            
            // Create header row
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("Field Name")
            headerRow.createCell(1).setCellValue("Field Label") 
            headerRow.createCell(2).setCellValue("Value")
            
            // Style header row
            val headerStyle = workbook.createCellStyle()
            val headerFont = workbook.createFont()
            headerFont.bold = true
            headerStyle.setFont(headerFont)
            
            for (i in 0..2) {
                headerRow.getCell(i).cellStyle = headerStyle
            }
            
            var rowNum = 1
            
            // Add form data
            formSections.forEach { section ->
                // Add section header
                val sectionRow = sheet.createRow(rowNum++)
                sectionRow.createCell(0).setCellValue("SECTION")
                sectionRow.createCell(1).setCellValue(section.title)
                sectionRow.createCell(2).setCellValue("")
                
                // Style section row
                val sectionStyle = workbook.createCellStyle()
                val sectionFont = workbook.createFont()
                sectionFont.bold = true
                sectionStyle.setFont(sectionFont)
                sectionRow.getCell(0).cellStyle = sectionStyle
                sectionRow.getCell(1).cellStyle = sectionStyle
                
                // Add fields
                section.fields.forEach { field ->
                    val dataRow = sheet.createRow(rowNum++)
                    dataRow.createCell(0).setCellValue(field.fieldName)
                    dataRow.createCell(1).setCellValue(field.label)
                    dataRow.createCell(2).setCellValue(formData[field.fieldName] ?: "")
                }
                
                // Add empty row after section
                rowNum++
            }
            
            // Auto-size columns
            for (i in 0..2) {
                sheet.autoSizeColumn(i)
            }
            
            // Save to file
            val fileName = "form_${formId}_${System.currentTimeMillis()}.xlsx"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
