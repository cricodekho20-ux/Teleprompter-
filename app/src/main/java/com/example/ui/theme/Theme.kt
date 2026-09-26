package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudioColorScheme = darkColorScheme(
    primary = StudioRed,
    onPrimary = StudioWhite,
    primaryContainer = StudioRedDark,
    onPrimaryContainer = StudioWhite,
    secondary = StudioGrayLight,
    onSecondary = StudioDarkBg,
    secondaryContainer = StudioCardBgElevated,
    onSecondaryContainer = StudioWhite,
    tertiary = AccentBlue,
    onTertiary = StudioWhite,
    background = StudioDarkBg,
    onBackground = StudioWhite,
    surface = StudioCardBg,
    onSurface = StudioWhite,
    surfaceVariant = StudioCardBgElevated,
    onSurfaceVariant = StudioGrayLight,
    outline = StudioBorder,
    outlineVariant = StudioBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Teleprompter always uses pure studio dark mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudioColorScheme,
        typography = Typography,
        content = content
    )
}
