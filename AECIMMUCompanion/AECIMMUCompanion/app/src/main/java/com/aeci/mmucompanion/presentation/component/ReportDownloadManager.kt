package com.aeci.mmucompanion.presentation.component

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun ReportDownloadManager(
    downloads: List<DownloadItem>,
    onDismiss: () -> Unit,
    onCancelDownload: (String) -> Unit,
    onOpenFile: (String) -> Unit
) {
    val context = LocalContext.current
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Downloads",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (downloads.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active downloads",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    downloads.forEach { download ->
                        DownloadItemCard(
                            download = download,
                            onCancel = { onCancelDownload(download.id) },
                            onOpen = { onOpenFile(download.filePath) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadItemCard(
    download: DownloadItem,
    onCancel: () -> Unit,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = download.fileName,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = download.reportTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                when (download.status) {
                    DownloadStatus.DOWNLOADING -> {
                        IconButton(onClick = onCancel) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = "Cancel",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    DownloadStatus.COMPLETED -> {
                        IconButton(onClick = onOpen) {
                            Icon(
                                Icons.Default.OpenInNew,
                                contentDescription = "Open",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    DownloadStatus.FAILED -> {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            when (download.status) {
                DownloadStatus.DOWNLOADING -> {
                    LinearProgressIndicator(
                        progress = { download.progress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${download.progress}% • ${formatFileSize(download.downloadedBytes)} / ${formatFileSize(download.totalBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DownloadStatus.COMPLETED -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Download completed • ${formatFileSize(download.totalBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                DownloadStatus.FAILED -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Failed",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Download failed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadProgressIndicator(
    fileName: String,
    progress: Int,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Downloading...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$fileName ($progress%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// Data classes
data class DownloadItem(
    val id: String,
    val reportId: String,
    val reportTitle: String,
    val fileName: String,
    val filePath: String,
    val status: DownloadStatus,
    val progress: Int = 0,
    val downloadedBytes: Long = 0,
    val totalBytes: Long = 0,
    val downloadUrl: String? = null,
    val startTime: Long = System.currentTimeMillis()
)

enum class DownloadStatus {
    DOWNLOADING,
    COMPLETED,
    FAILED
}

// Utility functions
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> "${(bytes / (1024 * 1024))} MB"
        bytes >= 1024 -> "${(bytes / 1024)} KB"
        else -> "$bytes bytes"
    }
}

class ReportDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            // Handle download completion
        }
    }
}

@Composable
fun rememberDownloadManager(context: Context = LocalContext.current): DownloadManagerState {
    return remember { DownloadManagerState(context) }
}

class DownloadManagerState(private val context: Context) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val _downloads = mutableStateListOf<DownloadItem>()
    val downloads: List<DownloadItem> = _downloads
    
    fun startDownload(
        reportId: String,
        reportTitle: String,
        fileName: String,
        downloadUrl: String
    ): String {
        val downloadId = generateDownloadId()
        
        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle("Report Download")
            .setDescription("Downloading $fileName")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        
        val systemDownloadId = downloadManager.enqueue(request)
        
        val downloadItem = DownloadItem(
            id = downloadId,
            reportId = reportId,
            reportTitle = reportTitle,
            fileName = fileName,
            filePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName).absolutePath,
            status = DownloadStatus.DOWNLOADING,
            downloadUrl = downloadUrl
        )
        
        _downloads.add(downloadItem)
        return downloadId
    }
    
    fun cancelDownload(downloadId: String) {
        _downloads.removeAll { it.id == downloadId }
    }
    
    fun updateDownloadProgress(downloadId: String, progress: Int, downloadedBytes: Long, totalBytes: Long) {
        val index = _downloads.indexOfFirst { it.id == downloadId }
        if (index != -1) {
            _downloads[index] = _downloads[index].copy(
                progress = progress,
                downloadedBytes = downloadedBytes,
                totalBytes = totalBytes
            )
        }
    }
    
    fun markDownloadCompleted(downloadId: String) {
        val index = _downloads.indexOfFirst { it.id == downloadId }
        if (index != -1) {
            _downloads[index] = _downloads[index].copy(
                status = DownloadStatus.COMPLETED,
                progress = 100
            )
        }
    }
    
    fun markDownloadFailed(downloadId: String) {
        val index = _downloads.indexOfFirst { it.id == downloadId }
        if (index != -1) {
            _downloads[index] = _downloads[index].copy(
                status = DownloadStatus.FAILED
            )
        }
    }
    
    private fun generateDownloadId(): String {
        return "download_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
} 