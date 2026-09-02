package com.salesiq.demoapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/** Appearance override selectable in Settings; System follows the OS. */
enum class ThemeMode { LIGHT, DARK, SYSTEM }

/** Access the token-driven palette anywhere: `LocalAppColors.current`. */
val LocalAppColors = staticCompositionLocalOf { LightAppColors }

/**
 * App-wide theme. Builds a Material3 [androidx.compose.material3.ColorScheme]
 * from the design tokens (so SDK-agnostic M3 components pick up brand colors)
 * and also exposes the full [AppColors] token set via [LocalAppColors].
 */
@Composable
fun MobilistenTheme(
    mode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val appColors = if (dark) DarkAppColors else LightAppColors

    val colorScheme = if (dark) {
        darkColorScheme(
            primary = appColors.primary,
            onPrimary = appColors.onPrimary,
            secondary = appColors.secondary,
            background = appColors.page,
            onBackground = appColors.textPrimary,
            surface = appColors.card,
            onSurface = appColors.textPrimary,
            error = appColors.danger,
            outline = appColors.border,
        )
    } else {
        lightColorScheme(
            primary = appColors.primary,
            onPrimary = appColors.onPrimary,
            secondary = appColors.secondary,
            background = appColors.page,
            onBackground = appColors.textPrimary,
            surface = appColors.card,
            onSurface = appColors.textPrimary,
            error = appColors.danger,
            outline = appColors.border,
        )
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}
