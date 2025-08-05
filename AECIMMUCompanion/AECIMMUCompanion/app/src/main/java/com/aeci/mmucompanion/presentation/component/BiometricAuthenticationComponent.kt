package com.aeci.mmucompanion.presentation.component

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aeci.mmucompanion.presentation.component.AECIIcons

@Composable
fun BiometricAuthenticationComponent(
    onAuthenticationSuccess: (String) -> Unit,
    onAuthenticationError: (String) -> Unit,
    onAuthenticationFailed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var biometricAvailability by remember { mutableStateOf(BiometricAvailabilityStatus.UNKNOWN) }
    var showBiometricPrompt by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Check biometric availability
    LaunchedEffect(Unit) {
        biometricAvailability = checkBiometricAvailability(context)
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (biometricAvailability) {
            BiometricAvailabilityStatus.AVAILABLE -> {
                BiometricAuthButton(
                    onClick = { showBiometricPrompt = true },
                    isLoading = isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            BiometricAvailabilityStatus.NONE_ENROLLED -> {
                BiometricUnavailableCard(
                    title = "No Biometric Enrolled",
                    description = "Please set up fingerprint or face recognition in your device settings to use biometric authentication.",
                    icon = Icons.Default.Warning
                )
            }
            BiometricAvailabilityStatus.HARDWARE_UNAVAILABLE -> {
                BiometricUnavailableCard(
                    title = "Biometric Hardware Unavailable",
                    description = "Your device doesn't support biometric authentication or the hardware is currently unavailable.",
                    icon = Icons.Default.Error
                )
            }
            BiometricAvailabilityStatus.UNAVAILABLE -> {
                BiometricUnavailableCard(
                    title = "Biometric Authentication Unavailable",
                    description = "Biometric authentication is not available on this device.",
                    icon = Icons.Default.Error
                )
            }
            BiometricAvailabilityStatus.UNKNOWN -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
    
    // Show biometric prompt
    if (showBiometricPrompt) {
        BiometricPromptComponent(
            onSuccess = { result ->
                showBiometricPrompt = false
                isLoading = false
                onAuthenticationSuccess(result)
            },
            onError = { error ->
                showBiometricPrompt = false
                isLoading = false
                onAuthenticationError(error)
            },
            onFailed = {
                showBiometricPrompt = false
                isLoading = false
                onAuthenticationFailed()
            },
            onCancel = {
                showBiometricPrompt = false
                isLoading = false
            }
        )
        
        LaunchedEffect(Unit) {
            isLoading = true
        }
    }
}

@Composable
fun BiometricAuthButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = !isLoading,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Authenticating...")
            } else {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Fingerprint",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Use Biometric Authentication",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun BiometricUnavailableCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BiometricPromptComponent(
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    onFailed: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    LaunchedEffect(Unit) {
        val activity = context as? FragmentActivity
        if (activity != null) {
            showBiometricPrompt(
                activity = activity,
                onSuccess = onSuccess,
                onError = onError,
                onFailed = onFailed,
                onCancel = onCancel
            )
        } else {
            onError("Unable to access biometric authentication")
        }
    }
}

private fun showBiometricPrompt(
    activity: FragmentActivity,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    onFailed: () -> Unit,
    onCancel: () -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    
    val biometricPrompt = BiometricPrompt(
        activity as androidx.fragment.app.FragmentActivity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || 
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    onCancel()
                } else {
                    onError("Authentication error: $errString")
                }
            }
            
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess("biometric_auth_success")
            }
            
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }
        }
    )
    
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric Authentication")
        .setSubtitle("Use your biometric credential to sign in")
        .setDescription("Place your finger on the sensor or look at the camera")
        .setNegativeButtonText("Cancel")
        .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
        .build()
    
    biometricPrompt.authenticate(promptInfo)
}

private fun checkBiometricAvailability(context: Context): BiometricAvailabilityStatus {
    val biometricManager = BiometricManager.from(context)
    
    return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)) {
        BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailabilityStatus.AVAILABLE
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailabilityStatus.HARDWARE_UNAVAILABLE
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailabilityStatus.HARDWARE_UNAVAILABLE
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailabilityStatus.NONE_ENROLLED
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailabilityStatus.UNAVAILABLE
        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricAvailabilityStatus.UNAVAILABLE
        BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricAvailabilityStatus.UNKNOWN
        else -> BiometricAvailabilityStatus.UNAVAILABLE
    }
}

enum class BiometricAvailabilityStatus {
    AVAILABLE,
    NONE_ENROLLED,
    HARDWARE_UNAVAILABLE,
    UNAVAILABLE,
    UNKNOWN
}

@Composable
fun BiometricQuickAuth(
    onAuthenticationSuccess: (String) -> Unit,
    onAuthenticationError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var biometricAvailability by remember { mutableStateOf(BiometricAvailabilityStatus.UNKNOWN) }
    var showPrompt by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        biometricAvailability = checkBiometricAvailability(context)
    }
    
    if (biometricAvailability == BiometricAvailabilityStatus.AVAILABLE) {
        IconButton(
            onClick = { showPrompt = true },
            modifier = modifier
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    CircleShape
                )
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Quick Biometric Auth",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }
        
        if (showPrompt) {
            BiometricPromptComponent(
                onSuccess = { result ->
                    showPrompt = false
                    onAuthenticationSuccess(result)
                },
                onError = { error ->
                    showPrompt = false
                    onAuthenticationError(error)
                },
                onFailed = {
                    showPrompt = false
                    onAuthenticationError("Authentication failed")
                },
                onCancel = {
                    showPrompt = false
                }
            )
        }
    }
} 