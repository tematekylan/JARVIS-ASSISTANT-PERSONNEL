package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisDarkColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = JarvisBgVoid,
    primaryContainer = JarvisDeepBlue,
    onPrimaryContainer = JarvisCyanGlow,
    secondary = JarvisBlue,
    onSecondary = Color.White,
    secondaryContainer = JarvisBgCard,
    onSecondaryContainer = JarvisBlueGlow,
    tertiary = JarvisEmerald,
    onTertiary = JarvisBgVoid,
    tertiaryContainer = Color(0xFF003822),
    onTertiaryContainer = Color(0xFF6DFFC4),
    background = JarvisBgVoid,
    onBackground = JarvisTextPrimary,
    surface = JarvisBgSurface,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisBgCard,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisBorderGlow,
    outlineVariant = JarvisBorderBright,
    error = JarvisCrimson,
    onError = Color.White
)

@Composable
fun JarvisTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisDarkColorScheme,
        typography = Typography,
        content = content
    )
}
