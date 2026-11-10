package com.cintamani.capture.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.cintamani.capture.data.ThemeId

data class ThemePalette(
    val id: ThemeId,
    val displayName: String,
    val canvas: Color,
    val surface: Color,
    val border: Color,
    val text: Color,
    val accent1: Color,
    val accent2: Color,
    val isDark: Boolean
)

val ArchitectDraftPalette = ThemePalette(
    id = ThemeId.ARCHITECT_DRAFT,
    displayName = "Architect's Draft",
    canvas = Color(0xFFF4F4F0),
    surface = Color(0xFFFFFFFF),
    border = Color(0xFF111111),
    text = Color(0xFF111111),
    accent1 = Color(0xFF81A1C1),
    accent2 = Color(0xFFE5FE54),
    isDark = false
)

val CognitiveTerminalPalette = ThemePalette(
    id = ThemeId.COGNITIVE_TERMINAL,
    displayName = "Cognitive Terminal",
    canvas = Color(0xFF18181A),
    surface = Color(0xFF09090B),
    border = Color(0xFFF1F5F9),
    text = Color(0xFFF1F5F9),
    accent1 = Color(0xFF4ADE80),
    accent2 = Color(0xFFA78BFA),
    isDark = true
)

val IndustrialArchivePalette = ThemePalette(
    id = ThemeId.INDUSTRIAL_ARCHIVE,
    displayName = "Industrial Archive",
    canvas = Color(0xFFE8E4D9),
    surface = Color(0xFFF5F5F0),
    border = Color(0xFF1C1917),
    text = Color(0xFF1C1917),
    accent1 = Color(0xFFA3B18A),
    accent2 = Color(0xFFFF6B35),
    isDark = false
)

val MidnightBlueprintPalette = ThemePalette(
    id = ThemeId.MIDNIGHT_BLUEPRINT,
    displayName = "Midnight Blueprint",
    canvas = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    border = Color(0xFF94A3B8),
    text = Color(0xFFE2E8F0),
    accent1 = Color(0xFF22D3EE),
    accent2 = Color(0xFFFB7185),
    isDark = true
)

val LocalCintamaniTheme = staticCompositionLocalOf { CognitiveTerminalPalette }

fun getPaletteForTheme(themeId: ThemeId): ThemePalette {
    return when (themeId) {
        ThemeId.ARCHITECT_DRAFT -> ArchitectDraftPalette
        ThemeId.COGNITIVE_TERMINAL -> CognitiveTerminalPalette
        ThemeId.INDUSTRIAL_ARCHIVE -> IndustrialArchivePalette
        ThemeId.MIDNIGHT_BLUEPRINT -> MidnightBlueprintPalette
    }
}

@Composable
fun CintamaniTheme(
    themeId: ThemeId = ThemeId.COGNITIVE_TERMINAL,
    content: @Composable () -> Unit
) {
    val palette = getPaletteForTheme(themeId)

    val colorScheme = if (palette.isDark) {
        darkColorScheme(
            background = palette.canvas,
            surface = palette.surface,
            onBackground = palette.text,
            onSurface = palette.text,
            primary = palette.accent1,
            secondary = palette.accent2
        )
    } else {
        lightColorScheme(
            background = palette.canvas,
            surface = palette.surface,
            onBackground = palette.text,
            onSurface = palette.text,
            primary = palette.accent1,
            secondary = palette.accent2
        )
    }

    CompositionLocalProvider(LocalCintamaniTheme provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
