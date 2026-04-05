package com.espimsystems.visionlab.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VisionLabDarkColorScheme = darkColorScheme(
    primary = VisionLabPrimary,
    onPrimary = VisionLabOnPrimary,
    primaryContainer = VisionLabPrimaryContainer,
    onPrimaryContainer = VisionLabOnPrimaryContainer,
    secondary = VisionLabSecondary,
    onSecondary = VisionLabOnSecondary,
    secondaryContainer = VisionLabSecondaryContainer,
    onSecondaryContainer = VisionLabOnSecondaryContainer,
    background = VisionLabBackground,
    onBackground = VisionLabOnBackground,
    surface = VisionLabSurface,
    onSurface = VisionLabOnSurface,
    error = VisionLabError,
    onError = VisionLabOnError,
)

@Composable
fun VisionLabTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = VisionLabDarkColorScheme,
        typography = VisionLabTypography,
        content = content,
    )
}