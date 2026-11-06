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
fun WavoraTheme(
    accentColor: Color = AccentBlue,
    bgColor: Color = DarkBg,
    content: @Composable () -> Unit
) {
    val dynamicScheme = darkColorScheme(
        primary = accentColor,
        onPrimary = OnAccent,
        secondary = accentColor.copy(alpha = 0.2f),
        onSecondary = OnAccent,
        tertiary = accentColor.copy(alpha = 0.4f),
        onTertiary = OnAccent,
        background = bgColor,
        onBackground = TextPrimary,
        surface = if (bgColor == Color.Black) Color(0xFF0D0D0D) else DarkSurface,
        onSurface = TextPrimary,
        surfaceVariant = if (bgColor == Color.Black) Color(0xFF161616) else ChipBg,
        onSurfaceVariant = TextSecondary,
        outline = Divider,
        outlineVariant = BorderLight,
        error = Color(0xFFEF4444),
        onError = Color.White,
    )

    MaterialTheme(
        colorScheme = dynamicScheme,
        typography = Typography,
        content = content,
    )
}