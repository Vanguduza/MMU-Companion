package com.aeci.mmucompanion.core.util

import android.content.Context
import android.util.Log
import com.itextpdf.forms.PdfAcroForm
import com.itextpdf.forms.fields.PdfFormField
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfName
import com.itextpdf.kernel.pdf.PdfReader
import java.io.InputStream

/**
 * A utility object to extract AcroForm field data from a PDF.
 * This is intended for developer use to generate the coordinate maps
 * required for the application's PDF export functionality.
 *
 * It can be called from a debug screen or temporary test to print
 * the coordinates and field details to the system log.
 */
object PdfFieldExtractor {

    private const val TAG = "PdfFieldExtractor"

    /**
     * Extracts form field information from a PDF provided as an asset.
     *
     * @param context The application context.
     * @param assetName The name of the PDF file in the assets folder.
     */
    fun extractFieldsFromAsset(context: Context, assetName: String) {
        try {
            Log.d(TAG, "--- Starting Field Extraction for: $assetName ---")
            val inputStream: InputStream = context.assets.open(assetName)
            val pdfDoc = PdfDocument(PdfReader(inputStream))
            val form = PdfAcroForm.getAcroForm(pdfDoc, false)

            if (form == null) {
                Log.w(TAG, "This PDF does not contain any AcroForm fields.")
                pdfDoc.close()
                return
            }

            val fields = form.getAllFormFields()
            if (fields.isEmpty()) {
                Log.w(TAG, "No form fields found in $assetName.")
            } else {
                fields.forEach { (name, field) ->
                    logFieldDetails(name, field)
                }
            }

            pdfDoc.close()
            Log.d(TAG, "--- Finished Field Extraction for: $assetName ---")
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting fields from asset: $assetName", e)
        }
    }

    private fun logFieldDetails(name: String, field: PdfFormField) {
        val widget = field.widgets.firstOrNull() ?: return
        val rect = widget.rectangle.toRectangle()
        val fieldType = getFieldTypeName(field.formType)

        val logMessage = "fieldName = \"$name\", " +
                "x = ${rect.x}f, y = ${rect.y}f, width = ${rect.width}f, height = ${rect.height}f, " +
                "fieldType = FieldType.$fieldType"

        Log.i(TAG, "FieldCoordinate($logMessage)")
    }

    private fun getFieldTypeName(formType: PdfName?): String {
        return when (formType) {
            PdfName.Tx -> "TEXT" // Text field
            PdfName.Btn -> "CHECKBOX" // Button, commonly a checkbox or radio button
            PdfName.Ch -> "DROPDOWN" // Choice field, a dropdown or list box
            PdfName.Sig -> "SIGNATURE" // Signature field
            else -> "UNKNOWN"
        }
    }
} 