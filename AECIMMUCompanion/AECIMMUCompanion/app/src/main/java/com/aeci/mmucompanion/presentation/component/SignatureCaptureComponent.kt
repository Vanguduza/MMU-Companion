package com.aeci.mmucompanion.presentation.component

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

data class PathPoint(
    val offset: Offset,
    val pressure: Float = 1f
)

@Composable
fun SignatureCaptureField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "Click to add signature",
    modifier: Modifier = Modifier,
    error: String? = null,
    isRequired: Boolean = false
) {
    var showSignatureDialog by remember { mutableStateOf(false) }
    val hasSignature = value.isNotEmpty()
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = if (hasSignature) "Signature captured" else "",
            onValueChange = { },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            isError = error != null,
            singleLine = true,
            readOnly = true,
            trailingIcon = {
                Row {
                    if (hasSignature) {
                        IconButton(onClick = { onValueChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Signature")
                        }
                    }
                    IconButton(onClick = { showSignatureDialog = true }) {
                        Icon(Icons.Default.Draw, contentDescription = "Capture Signature")
                    }
                }
            },
            supportingText = if (error != null) {
                { Text(error, color = MaterialTheme.colorScheme.error) }
            } else null
        )
        
        if (showSignatureDialog) {
            SignatureCaptureDialog(
                onSignatureCaptured = { signaturePath ->
                    onValueChange(signaturePath)
                    showSignatureDialog = false
                },
                onDismiss = { showSignatureDialog = false }
            )
        }
    }
}

@Composable
fun SignatureCaptureDialog(
    onSignatureCaptured: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    
    var paths by remember { mutableStateOf(listOf<List<PathPoint>>()) }
    var currentPath by remember { mutableStateOf(listOf<PathPoint>()) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Capture Signature",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Signature Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPath = listOf(PathPoint(offset))
                                },
                                onDrag = { _, dragAmount ->
                                    val lastPoint = currentPath.lastOrNull()
                                    if (lastPoint != null) {
                                        val newPoint = PathPoint(
                                            lastPoint.offset + dragAmount,
                                            1f
                                        )
                                        currentPath = currentPath + newPoint
                                    }
                                },
                                onDragEnd = {
                                    if (currentPath.isNotEmpty()) {
                                        paths = paths + listOf(currentPath)
                                        currentPath = emptyList()
                                    }
                                }
                            )
                        }
                ) {
                    // Draw all completed paths
                    paths.forEach { path ->
                        drawSignaturePath(path)
                    }
                    
                    // Draw current path being drawn
                    if (currentPath.isNotEmpty()) {
                        drawSignaturePath(currentPath)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = {
                            paths = emptyList()
                            currentPath = emptyList()
                        }
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear")
                    }
                    
                    Button(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            if (paths.isNotEmpty()) {
                                // Convert signature to bitmap and save
                                val signaturePath = saveSignatureToFile(paths, context)
                                onSignatureCaptured(signaturePath)
                            }
                        },
                        enabled = paths.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawSignaturePath(path: List<PathPoint>) {
    if (path.size < 2) return
    
    val pathData = Path()
    pathData.moveTo(path.first().offset.x, path.first().offset.y)
    
    for (i in 1 until path.size) {
        val currentPoint = path[i]
        val previousPoint = path[i - 1]
        
        // Create smooth curves between points
        val controlPointX = (previousPoint.offset.x + currentPoint.offset.x) / 2
        val controlPointY = (previousPoint.offset.y + currentPoint.offset.y) / 2
        
        pathData.quadraticTo(
            previousPoint.offset.x,
            previousPoint.offset.y,
            controlPointX,
            controlPointY
        )
    }
    
    drawPath(
        path = pathData,
        color = androidx.compose.ui.graphics.Color.Black,
        style = Stroke(
            width = 3.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

private fun saveSignatureToFile(paths: List<List<PathPoint>>, context: android.content.Context): String {
    return try {
        // Create bitmap
        val bitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        
        val paint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 6f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }
        
        // Draw paths on bitmap
        paths.forEach { path ->
            if (path.size >= 2) {
                val androidPath = android.graphics.Path()
                androidPath.moveTo(path.first().offset.x, path.first().offset.y)
                
                for (i in 1 until path.size) {
                    val point = path[i]
                    androidPath.lineTo(point.offset.x, point.offset.y)
                }
                
                canvas.drawPath(androidPath, paint)
            }
        }
        
        // Save bitmap to file
        val filename = "signature_${System.currentTimeMillis()}.png"
        val file = File(context.filesDir, filename)
        
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        
        file.absolutePath
    } catch (e: Exception) {
        ""
    }
}
