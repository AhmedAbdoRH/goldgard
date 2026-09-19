package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Centralized Dark Theme instance (الهوية القديمة)
val DarkGoldColors = GoldThemeColors(
    background = PaletteBackgroundDark,
    surface = PaletteSurfaceDark,
    surfaceElevated = PaletteSurfaceElevatedDark,
    goldPrimary = PaletteGoldPrimary,
    goldLight = PaletteGoldLight,
    textPrimary = PaletteTextPrimaryDark,
    textSecondary = PaletteTextSecondaryDark,
    success = PaletteSuccessGreen,
    danger = PaletteDangerRed,
    warning = PaletteWarningAmber,
    borderColor = PaletteBorderColorDark,
    isDark = true
)

// Centralized Light Theme instance (الوضع النهاري المريح)
val LightGoldColors = GoldThemeColors(
    background = PaletteBackgroundLight,
    surface = PaletteSurfaceLight,
    surfaceElevated = PaletteSurfaceElevatedLight,
    goldPrimary = PaletteGoldPrimaryLight,
    goldLight = PaletteGoldPrimary,
    textPrimary = PaletteTextPrimaryLight,
    textSecondary = PaletteTextSecondaryLight,
    success = Color(0xFF059669),
    danger = Color(0xFFDC2626),
    warning = Color(0xFFD97706),
    borderColor = PaletteBorderColorLight,
    isDark = false
)

val LocalGoldColors = staticCompositionLocalOf { DarkGoldColors }

object GoldTheme {
    val colors: GoldThemeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGoldColors.current

    val spacing: GoldSpacing get() = GoldSpacing
    val radius: GoldRadius get() = GoldRadius
}

private val DarkMaterialColorScheme = darkColorScheme(
    primary = PaletteGoldPrimary,
    onPrimary = Color.Black,
    primaryContainer = PaletteSurfaceElevatedDark,
    onPrimaryContainer = PaletteGoldLight,
    secondary = PaletteGoldWarm,
    onSecondary = Color.Black,
    secondaryContainer = PaletteSurfaceDark,
    onSecondaryContainer = Color.White,
    background = PaletteBackgroundDark,
    onBackground = PaletteTextPrimaryDark,
    surface = PaletteSurfaceDark,
    onSurface = PaletteTextPrimaryDark,
    surfaceVariant = PaletteSurfaceElevatedDark,
    onSurfaceVariant = PaletteTextSecondaryDark,
    error = PaletteDangerRed,
    onError = Color.White
)

private val LightMaterialColorScheme = lightColorScheme(
    primary = PaletteGoldPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = PaletteTextPrimaryLight,
    secondary = PaletteGoldPrimary,
    onSecondary = Color.White,
    secondaryContainer = PaletteSurfaceElevatedLight,
    onSecondaryContainer = PaletteTextPrimaryLight,
    background = PaletteBackgroundLight,
    onBackground = PaletteTextPrimaryLight,
    surface = PaletteSurfaceLight,
    onSurface = PaletteTextPrimaryLight,
    surfaceVariant = PaletteSurfaceElevatedLight,
    onSurfaceVariant = PaletteTextSecondaryLight,
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    isDarkMode: Boolean = true,
    content: @Composable () -> Unit
) {
    val goldColors = if (isDarkMode) DarkGoldColors else LightGoldColors
    val materialScheme = if (isDarkMode) DarkMaterialColorScheme else LightMaterialColorScheme

    CompositionLocalProvider(
        LocalGoldColors provides goldColors
    ) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = Typography,
            content = content
        )
    }
}
