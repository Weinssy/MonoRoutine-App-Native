package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = MonoBlack,
    primaryContainer = MonoDark,
    onPrimaryContainer = Color.White,
    secondary = BrandRed,
    onSecondary = Color.White,
    background = MonoBlack,
    onBackground = Color.White,
    surface = MonoDark,
    onSurface = Color.White,
    surfaceVariant = MonoCardDark,
    onSurfaceVariant = Color(0xFFA3A3A3),
    outline = Color(0xFF333333)
)

private val LightColorScheme = lightColorScheme(
    primary = MonoBlack,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5E5E5),
    onPrimaryContainer = MonoBlack,
    secondary = BrandRed,
    onSecondary = Color.White,
    background = MonoBackground,
    onBackground = MonoBlack,
    surface = MonoSurface,
    onSurface = MonoBlack,
    surfaceVariant = Color(0xFFF2F2F2),
    onSurfaceVariant = MonoGray,
    outline = MonoBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
