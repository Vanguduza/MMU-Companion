package com.aeci.mmucompanion.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ExportFormat {
    PDF,
    EXCEL,
    CSV,
    JSON
}

val ExportFormat.icon: ImageVector
    get() = when (this) {
        ExportFormat.PDF -> Icons.Default.PictureAsPdf
        ExportFormat.EXCEL -> Icons.Default.TableChart
        ExportFormat.CSV -> Icons.Default.Description
        ExportFormat.JSON -> Icons.Default.Code
    }

val ExportFormat.color: Color
    get() = when (this) {
        ExportFormat.PDF -> Color(0xFFE57373)
        ExportFormat.EXCEL -> Color(0xFF4CAF50)
        ExportFormat.CSV -> Color(0xFF2196F3)
        ExportFormat.JSON -> Color(0xFF9C27B0)
    }
