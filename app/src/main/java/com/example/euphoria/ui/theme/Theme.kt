package com.example.euphoria.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.euphoria.theme.BuiltInThemes
import com.example.euphoria.theme.EuphoriaThemeData

@Composable
fun EuphoriaTheme(
    themeData: EuphoriaThemeData = BuiltInThemes.MidnightPurple,
    content: @Composable () -> Unit
) {
    val colorScheme = if (themeData.isDark) {
        darkColorScheme(
            primary = themeData.accentColor,
            onPrimary = themeData.textPrimary,
            primaryContainer = themeData.cardColor,
            onPrimaryContainer = themeData.textPrimary,
            secondary = themeData.secondaryAccentColor,
            onSecondary = themeData.textPrimary,
            background = themeData.primaryBg,
            onBackground = themeData.textPrimary,
            surface = themeData.surfaceColor,
            onSurface = themeData.textPrimary,
            surfaceVariant = themeData.cardColor,
            onSurfaceVariant = themeData.textSecondary
        )
    } else {
        lightColorScheme(
            primary = themeData.accentColor,
            onPrimary = themeData.textPrimary,
            primaryContainer = themeData.cardColor,
            onPrimaryContainer = themeData.textPrimary,
            secondary = themeData.secondaryAccentColor,
            onSecondary = themeData.textPrimary,
            background = themeData.primaryBg,
            onBackground = themeData.textPrimary,
            surface = themeData.surfaceColor,
            onSurface = themeData.textPrimary,
            surfaceVariant = themeData.cardColor,
            onSurfaceVariant = themeData.textSecondary
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
