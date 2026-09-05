package com.thackman.ai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    secondary = CyberGreen,
    tertiary = CyberPurple,
    background = CyberBlack,
    surface = CyberDark,
    onPrimary = CyberBlack,
    onSecondary = CyberBlack,
    onTertiary = CyberBlack,
    onBackground = CyberText,
    onSurface = CyberText
)

@Composable
fun THackmanTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
