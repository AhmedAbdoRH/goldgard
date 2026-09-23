package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// =========================================================================
// Dark Theme Colors (الهوية اللونية المحددة بالملي)
// =========================================================================
val PaletteBackgroundDark = Color(0xFF0A0E1A)      // خلفية #0a0e1a
val PaletteSurfaceDark = Color(0xFF1A2238)         // بطاقات #1a2238
val PaletteSurfaceElevatedDark = Color(0xFF222C46) // طبقة مرتفعة
val PaletteBorderColorDark = Color(0xFF2E3D5E)     // حدود

// Golden Accent Identity (#f4c542)
val PaletteGoldPrimary = Color(0xFFF4C542)         // ذهبي مميز #f4c542
val PaletteGoldLight = Color(0xFFFDE68A)           // ذهبي فاتح ناعم
val PaletteGoldDark = Color(0xFFB45309)            // ذهبي داكن دافئ
val PaletteGoldWarm = Color(0xFFF4C542)

// Text Colors (Dark Mode)
val PaletteTextPrimaryDark = Color(0xFFE8EDF6)     // نصوص #e8edf6
val PaletteTextSecondaryDark = Color(0xFF8EA0C2)   // ثانوية #8ea0c2
val PaletteTextMutedDark = Color(0xFF64748B)

// Functional Status Colors
val PaletteSuccessGreen = Color(0xFF2BD47D)        // أخضر للشراء #2bd47d
val PaletteDangerRed = Color(0xFFFF5A6A)           // أحمر للبيع #ff5a6a
val PaletteWarningAmber = Color(0xFFF59E0B)        // برتقالي كهرماني للتحذيرات

// =========================================================================
// Light Theme Colors (الوضع النهاري المريح غير الساطع)
// =========================================================================
val PaletteBackgroundLight = Color(0xFFF1F5F9)     // رمادي فاتح مريح للعين (ليس أبيض ساطع)
val PaletteSurfaceLight = Color(0xFFFFFFFF)        // بطاقات بيضاء نقية
val PaletteSurfaceElevatedLight = Color(0xFFE2E8F0)// طبقة رمادية فاتحة متناسقة
val PaletteBorderColorLight = Color(0xFFCBD5E1)    // حدود ناعمة

// Text Colors (Light Mode)
val PaletteTextPrimaryLight = Color(0xFF0F172A)    // نصوص كحلية داكنة فائقة الوضوح
val PaletteTextSecondaryLight = Color(0xFF475569)  // نصوص ثانوية واضحة
val PaletteGoldPrimaryLight = Color(0xFFB45309)    // ذهبي مائل للكهرمان للقراءة على خلفية فاتحة

// =========================================================================
// Centralized Design System Colors Contract
// =========================================================================
data class GoldThemeColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val goldPrimary: Color,
    val goldLight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val success: Color,
    val danger: Color,
    val warning: Color,
    val borderColor: Color,
    val isDark: Boolean
) {
    // Convenience aliases matching user request exactly
    val backgroundDark: Color get() = if (isDark) background else PaletteBackgroundDark
    val surfaceDark: Color get() = if (isDark) surface else PaletteSurfaceDark
    val textMuted: Color get() = textSecondary.copy(alpha = 0.7f)
}

// Spacing System (السابع عشر: المسافات والأحجام)
object GoldSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
}

// Corner Radius System
object GoldRadius {
    val sm = 8.dp
    val md = 14.dp
    val lg = 20.dp
}

// =========================================================================
// Backward Compatibility Bindings (Preventing breaks across existing screens)
// =========================================================================
val GoldPrimary = PaletteGoldPrimary
val GoldLight = PaletteGoldLight
val GoldDark = PaletteGoldDark
val GoldWarm = PaletteGoldWarm
val GoldAccent = PaletteGoldPrimary
val GoldShimmer = PaletteGoldPrimary

val AppBackground = PaletteBackgroundDark
val CardSurface = PaletteSurfaceDark
val DarkCanvasBackground = PaletteBackgroundDark
val DarkCardSurface = PaletteSurfaceDark
val DarkCardSurfaceElevated = PaletteSurfaceElevatedDark
val DarkBorderGold = PaletteGoldPrimary
val DarkBorderSubtle = PaletteBorderColorDark
val TextDark = PaletteTextPrimaryDark
val TextMuted = PaletteTextSecondaryDark
val TextLight = Color.White
val TextGold = PaletteGoldPrimary

val AlertRedBg = Color(0x20EF4444)
val AlertRedBorder = PaletteDangerRed
val AlertRedText = PaletteDangerRed

val SuccessGreenBg = Color(0x2010B981)
val SuccessGreenBorder = PaletteSuccessGreen
val SuccessGreenText = PaletteSuccessGreen

val SellResultPinkBg = Color(0x20EF4444)
val SellResultPinkBorder = PaletteDangerRed

val InfoCyanBg = Color(0x2038BDF8)
val InfoCyanBorder = Color(0xFF38BDF8)

val WarmOrange = PaletteGoldPrimary
val WarmOrangeDark = PaletteGoldDark
val WarmOrangeLight = PaletteGoldLight
val TextBrownDark = PaletteTextPrimaryDark
val TextBrownMedium = PaletteTextSecondaryDark
val TextBrownMuted = PaletteTextSecondaryDark

val BeigeCanvasBackground = PaletteBackgroundDark
val BeigeCardSurface = PaletteSurfaceDark
val BeigeCardSurfaceElevated = PaletteSurfaceElevatedDark
val BeigeCardAlt = PaletteSurfaceDark
val BeigeBorderSubtle = PaletteBorderColorDark

val BuyGreenStart = Color(0xFF059669)
val BuyGreenEnd = Color(0xFF10B981)
val SellRedStart = Color(0xFFB91C1C)
val SellRedEnd = Color(0xFFEF4444)

val HistoryCyan = Color(0xFF0EA5E9)
val PurpleGradientStart = PaletteSurfaceElevatedDark
val PurpleGradientEnd = PaletteSurfaceDark
val SettingsGray = Color(0xFF64748B)

val BuyGreenGradientBrush = Brush.linearGradient(listOf(BuyGreenStart, BuyGreenEnd))
val SellRedGradientBrush = Brush.linearGradient(listOf(SellRedStart, SellRedEnd))
val BuyBlueGradientBrush = Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF38BDF8)))
val SettingsGrayGradientBrush = Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF475569)))
val HistoryCyanGradientBrush = Brush.linearGradient(listOf(Color(0xFF0369A1), Color(0xFF0EA5E9)))
val ZakatGradientBrush = Brush.linearGradient(listOf(Color(0xFF047857), Color(0xFF10B981)))
val GoldGradientBrush = Brush.linearGradient(listOf(PaletteGoldLight, PaletteGoldPrimary, PaletteGoldDark))
val WarmOrangeGradientBrush = Brush.linearGradient(listOf(PaletteGoldLight, PaletteGoldPrimary, PaletteGoldDark))
val PurpleGradientBrush = Brush.linearGradient(listOf(PaletteSurfaceDark, PaletteSurfaceElevatedDark))
val DarkObsidianBrush = Brush.verticalGradient(listOf(PaletteBackgroundDark, PaletteSurfaceDark, PaletteBackgroundDark))
