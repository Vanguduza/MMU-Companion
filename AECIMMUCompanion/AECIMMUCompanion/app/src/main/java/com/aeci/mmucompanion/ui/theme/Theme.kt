package com.aeci.mmucompanion.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// AECI Brand Colors - Updated to match AECI Mining brand
private val AECIRedPrimary = Color(0xFFC8102E)
private val AECIRedDark = Color(0xFFA00D25)
private val AECIRedLight = Color(0xFFD63851)
private val AECIWhite = Color(0xFFFFFFFF)
private val AECIGreyLight = Color(0xFFD8D8D8)
private val AECIGreyMedium = Color(0xFF9E9E9E)
private val AECIGreyDark = Color(0xFF666666)
private val AECIBlack = Color(0xFF000000)

private val DarkColorScheme = darkColorScheme(
    primary = AECIRedLight,
    secondary = AECIGreyLight,
    tertiary = AECIRedPrimary,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = AECIWhite,
    onSecondary = AECIBlack,
    onTertiary = AECIWhite,
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
    error = Color(0xFFF44336),
    onError = AECIWhite
)

private val LightColorScheme = lightColorScheme(
    primary = AECIRedPrimary,
    secondary = AECIGreyMedium,
    tertiary = AECIRedDark,
    background = AECIWhite,
    surface = AECIWhite,
    onPrimary = AECIWhite,
    onSecondary = AECIBlack,
    onTertiary = AECIWhite,
    onBackground = AECIBlack,
    onSurface = AECIBlack,
    error = Color(0xFFB00020),
    onError = AECIWhite
)

@Composable
fun AECIMMUCompanionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use AECI brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}