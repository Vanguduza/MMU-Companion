package com.aeci.mmucompanion.data.export

import android.graphics.BitmapFactory
import android.util.Log
import com.itextpdf.forms.PdfAcroForm
import com.itextpdf.forms.fields.PdfButtonFormField
import com.itextpdf.forms.fields.PdfFormField
import com.itextpdf.kernel.pdf.annot.PdfAnnotation
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.DeviceGray
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.VerticalAlignment
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import com.itextpdf.layout.element.List as PdfList
import com.itextpdf.layout.Document

class PdfExportEngine @Inject constructor() {
    
    companion object {
        private const val TAG = "PdfExportEngine"
    }

    fun createBlastReportSummary(summaryData: Map<String, String>, outputPath: String) {
        try {
            val writer = PdfWriter(outputPath)
            val pdfDoc = PdfDocument(writer)
            val document = Document(pdfDoc)

            // Add Header
            document.add(Paragraph("Blast Report Summary")
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(20f)
                .setMarginBottom(20f))

            // Add Summary Data
            val list = PdfList().setSymbolIndent(12f).setListSymbol("•")
            summaryData.forEach { (key, value) ->
                list.add("$key: $value")
            }
            document.add(list)

            document.close()
            Log.i(TAG, "Successfully created blast report summary: $outputPath")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating blast report summary", e)
        }
    }

    fun mergePdfs(pdfFilePaths: List<String>, outputPath: String) {
        try {
            val writer = PdfWriter(outputPath)
            val mergedPdf = PdfDocument(writer)

            for (pdfPath in pdfFilePaths) {
                val pdfToMerge = PdfDocument(PdfReader(pdfPath))
                pdfToMerge.copyPagesTo(1, pdfToMerge.numberOfPages, mergedPdf)
                pdfToMerge.close()
            }

            mergedPdf.close()
            Log.i(TAG, "Successfully merged ${pdfFilePaths.size} PDFs into: $outputPath")
        } catch (e: Exception) {
            Log.e(TAG, "Error merging PDFs", e)
        }
    }

    fun fillFormTemplate(templatePath: String, outputPath: String, formData: Map<String, Any>) {
        try {
        val reader = PdfReader(templatePath)
        val writer = PdfWriter(FileOutputStream(outputPath))
        val pdfDoc = PdfDocument(reader, writer)
        val form = PdfAcroForm.getAcroForm(pdfDoc, true)
        val fields = form?.getAllFormFields() ?: emptyMap()
        
        formData.forEach { (fieldName, value) ->
            val field = fields[fieldName]
            if (field == null) {
                Log.w(TAG, "No PDF form field found for data key: $fieldName")
                return@forEach
            }

            try {
                when (value) {
                    is String -> {
                        if (fieldName.startsWith("image_") || fieldName.startsWith("signature_")) {
                            handleImagePlacement(pdfDoc, field, value)
                        } else {
                            field.setValue(value)
                        }
                    }
                    is Boolean -> {
                        if (field is PdfButtonFormField) {
                            val appearanceState = if (value) "Yes" else "Off"
                            field.setValue(appearanceState)
                        } else {
                            field.setValue(value.toString())
                        }
                    }
                    is Number -> field.setValue(value.toString())
                    else -> {
                        Log.w(TAG, "Unsupported data type for field '$fieldName': ${value::class.java.simpleName}")
                        field.setValue(value.toString())
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set value for field: $fieldName", e)
            }
        }
            
            form?.flattenFields()
        pdfDoc.close()
            Log.i(TAG, "Successfully filled and flattened PDF: $outputPath")
        } catch (e: Exception) {
            Log.e(TAG, "Error processing PDF template: $templatePath", e)
        }
    }

    private fun handleImagePlacement(pdfDoc: PdfDocument, field: PdfFormField, imagePath: String) {
        if (imagePath.isBlank() || !File(imagePath).exists()) {
            Log.w(TAG, "Image path is empty or invalid for field: ${field.fieldName}")
            return
        }

        try {
            val fieldRect = field.widgets.first().rectangle.toRectangle()
            val page = field.widgets.first().page

            // Hide the form field by setting its appearance to empty
            field.setValue("")

            val imageData = ImageDataFactory.create(imagePath)
            val image = Image(imageData)

            // Calculate aspect ratio to fit image within the field boundaries
            val imageAspectRatio = image.imageWidth / image.imageHeight
            val fieldAspectRatio = fieldRect.width / fieldRect.height

            val scaledWidth: Float
            val scaledHeight: Float

            if (imageAspectRatio > fieldAspectRatio) {
                scaledWidth = fieldRect.width
                scaledHeight = scaledWidth / imageAspectRatio
            } else {
                scaledHeight = fieldRect.height
                scaledWidth = scaledHeight * imageAspectRatio
            }

            val x = fieldRect.x + (fieldRect.width - scaledWidth) / 2
            val y = fieldRect.y + (fieldRect.height - scaledHeight) / 2
            
            image.setFixedPosition(x, y, scaledWidth)

            val canvas = PdfCanvas(page)
            val layoutCanvas = Canvas(canvas, fieldRect)
            layoutCanvas.add(image)
            layoutCanvas.close()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to place image in field: ${field.fieldName}", e)
        }
    }
}
