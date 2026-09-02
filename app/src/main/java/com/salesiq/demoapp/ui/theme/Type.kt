package com.salesiq.demoapp.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

/**
 * Type scale generated from design-tokens.json (typography.scale).
 * Two weights only: regular (400) and medium/semibold (600). System font
 * stack; monospace system stack for [AppType.mono].
 */
@Immutable
data class AppTypeStyle(
    val style: TextStyle,
    val uppercase: Boolean = false,
)

object AppType {
    private val Regular = FontWeight.Normal      // 400
    private val Medium = FontWeight.SemiBold      // 600

    val largeTitle = TextStyle(fontSize = 28.sp, fontWeight = Medium, letterSpacing = (-0.6).sp)
    val title = TextStyle(fontSize = 20.sp, fontWeight = Medium, letterSpacing = (-0.3).sp)
    val headline = TextStyle(fontSize = 16.sp, fontWeight = Medium, letterSpacing = 0.sp)
    val body = TextStyle(fontSize = 15.sp, fontWeight = Regular, letterSpacing = 0.sp)
    val subhead = TextStyle(fontSize = 13.sp, fontWeight = Regular, letterSpacing = 0.sp)
    val caption = TextStyle(fontSize = 12.sp, fontWeight = Regular, letterSpacing = 0.sp)
    val sectionLabel = TextStyle(fontSize = 11.sp, fontWeight = Medium, letterSpacing = 1.0.sp)
    val mono = TextStyle(
        fontSize = 12.sp,
        fontWeight = Regular,
        letterSpacing = 0.sp,
        fontFamily = FontFamily.Monospace,
    )
}
