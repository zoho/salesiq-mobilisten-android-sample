package com.salesiq.demoapp.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * App color palette — generated 1:1 from the canonical
 * design-tokens.json (color.light / color.dark). Never hardcode a color
 * elsewhere; pull it from [AppColors] via [LocalAppColors].
 */
@Immutable
data class AppColors(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val accent: Color,
    val danger: Color,
    val success: Color,
    val warning: Color,
    val page: Color,
    val card: Color,
    val cardAlt: Color,
    val border: Color,
    val borderSubtle: Color,
    val hairline: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val tintPrimary: Color,
    val tintSecondary: Color,
    val tintAccent: Color,
    val tintDanger: Color,
    val segmentTrack: Color,
    val segmentThumb: Color,
    val switchOn: Color,
    val switchOff: Color,
    val resultBlockBg: Color,
    val isDark: Boolean,
)

val LightAppColors = AppColors(
    primary = Color(0xFF2E63F6),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF12A06E),
    accent = Color(0xFFD8722F),
    danger = Color(0xFFE24B4A),
    success = Color(0xFF12A06E),
    warning = Color(0xFFD8722F),
    page = Color(0xFFFBFBFD),
    card = Color(0xFFFFFFFF),
    cardAlt = Color(0xFFF6F7F9),
    border = Color(0xFFECEEF2),
    borderSubtle = Color(0xFFF1F2F5),
    hairline = Color(0xFFEFF1F5),
    textPrimary = Color(0xFF0B1220),
    textSecondary = Color(0xFF6E7683),
    textTertiary = Color(0xFF9098A3),
    tintPrimary = Color(0xFFEAF0FF),
    tintSecondary = Color(0xFFE7F7F0),
    tintAccent = Color(0xFFFBEFE6),
    tintDanger = Color(0xFFFCEBEB),
    segmentTrack = Color(0xFFEFF1F5),
    segmentThumb = Color(0xFFFFFFFF),
    switchOn = Color(0xFF12A06E),
    switchOff = Color(0xFFECEEF2),
    resultBlockBg = Color(0xFFF6F7F9),
    isDark = false,
)

val DarkAppColors = AppColors(
    primary = Color(0xFF5A86FF),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF35C79A),
    accent = Color(0xFFF0955A),
    danger = Color(0xFFF0706F),
    success = Color(0xFF35C79A),
    warning = Color(0xFFF0955A),
    page = Color(0xFF0B1220),
    card = Color(0xFF151B2B),
    cardAlt = Color(0xFF1B2233),
    border = Color(0xFF232B3E),
    borderSubtle = Color(0xFF1E2536),
    hairline = Color(0xFF1E2537),
    textPrimary = Color(0xFFF4F6FA),
    textSecondary = Color(0xFFA0A8B8),
    textTertiary = Color(0xFF6E7788),
    tintPrimary = Color(0xFF17233F),
    tintSecondary = Color(0xFF123027),
    tintAccent = Color(0xFF33251A),
    tintDanger = Color(0xFF3A1D1D),
    segmentTrack = Color(0xFF1B2233),
    segmentThumb = Color(0xFF2B344B),
    switchOn = Color(0xFF35C79A),
    switchOff = Color(0xFF232B3E),
    resultBlockBg = Color(0xFF1B2233),
    isDark = true,
)
