package com.example.wavora.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WavoraDarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = OnAccent,
    secondary = AccentSecondary,
    onSecondary = OnAccent,
    tertiary = AccentTertiary,
    onTertiary = OnAccent,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = ChipBg,
    onSurfaceVariant = TextSecondary,
    outline = Divider,
    outlineVariant = BorderLight,
    error = Color(0xFFEF4444),
    onError = Color.White,
)

@Composable
fun WavoraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WavoraDarkColorScheme,
        typography = Typography,
        content = content,
    )
}