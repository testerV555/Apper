package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = DarkNavyBg,
    primaryContainer = DarkNavyCard,
    onPrimaryContainer = CyberCyan,
    secondary = NeonGreen,
    onSecondary = DarkNavyBg,
    secondaryContainer = DarkNavyCard,
    onSecondaryContainer = NeonGreen,
    tertiary = ElectricIndigo,
    onTertiary = TextPrimary,
    background = DarkNavyBg,
    onBackground = TextPrimary,
    surface = DarkNavySurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkNavyCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkNavyCardBorder
)

private val LightColorScheme = darkColorScheme( // Keep cyber dark palette as primary identity
    primary = CyberCyan,
    onPrimary = DarkNavyBg,
    primaryContainer = DarkNavyCard,
    onPrimaryContainer = CyberCyan,
    secondary = NeonGreen,
    onSecondary = DarkNavyBg,
    secondaryContainer = DarkNavyCard,
    onSecondaryContainer = NeonGreen,
    tertiary = ElectricIndigo,
    onTertiary = TextPrimary,
    background = DarkNavyBg,
    onBackground = TextPrimary,
    surface = DarkNavySurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkNavyCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkNavyCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek cyber dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DarkNavyBg.toArgb()
                window.navigationBarColor = DarkNavyBg.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
