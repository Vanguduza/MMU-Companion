package com.aeci.mmucompanion.presentation.component

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun ImagePicker(
    currentImageUri: String?,
    onImageSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isCircular: Boolean = false,
    placeholder: ImageVector = Icons.Default.Person,
    contentDescription: String = "Select image"
) {
    val context = LocalContext.current
    var showOptions by remember { mutableStateOf(false) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Camera image will be saved to the URI we provide
            // Implementation would depend on how you handle file storage
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            onImageSelected(it.toString())
        }
    }

    Box(
        modifier = modifier
            .size(120.dp)
            .clickable { showOptions = true },
        contentAlignment = Alignment.Center
    ) {
        if (currentImageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(currentImageUri)
                        .build()
                ),
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isCircular) Modifier.clip(CircleShape)
                        else Modifier.clip(RoundedCornerShape(8.dp))
                    ),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = if (isCircular) CircleShape else RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = placeholder,
                        contentDescription = contentDescription,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add Photo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Add button overlay
        FloatingActionButton(
            onClick = { showOptions = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(32.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = if (currentImageUri != null) Icons.Default.PhotoCamera else Icons.Default.Add,
                contentDescription = "Change photo",
                modifier = Modifier.size(16.dp)
            )
        }
    }

    if (showOptions) {
        AlertDialog(
            onDismissRequest = { showOptions = false },
            title = { Text("Select Photo Source") },
            text = { Text("Choose how you want to add a photo") },
            confirmButton = {
                TextButton(
                    onClick = {
                        galleryLauncher.launch("image/*")
                        showOptions = false
                    }
                ) {
                    Text("Gallery")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // For camera, you'd need to create a file URI first
                        // cameraLauncher.launch(createImageUri(context))
                        showOptions = false
                    }
                ) {
                    Text("Camera")
                }
            }
        )
    }
}

@Composable
fun CompactImagePicker(
    currentImageUri: String?,
    onImageSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 60.dp,
    placeholder: ImageVector = Icons.Default.Person
) {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            onImageSelected(it.toString())
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clickable { galleryLauncher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        if (currentImageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(currentImageUri),
                contentDescription = "Selected image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = placeholder,
                    contentDescription = "Select image",
                    modifier = Modifier.size(size * 0.6f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
