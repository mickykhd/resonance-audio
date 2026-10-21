package com.resonance.audio.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

object Palette {
    val obsidian = Color(0xFF0D0F14)
    val panel = Color(0xFF151922)
    val floatingCard = Color(0xFF1E2330)

    val background = Color(0xFF111318)
    val surfaceLowest = Color(0xFF0C0E13)
    val surfaceLow = Color(0xFF1A1B21)
    val surfaceContainer = Color(0xFF1E1F25)
    val surfaceHigh = Color(0xFF282A2F)
    val surfaceHighest = Color(0xFF33353A)
    val surfaceBright = Color(0xFF37393F)
    val surfaceVariant = Color(0xFF33353A)

    val onSurface = Color(0xFFE2E2E9)
    val onSurfaceVariant = Color(0xFFCBC3D7)
    val outline = Color(0xFF958EA0)
    val outlineVariant = Color(0xFF494454)

    val primary = Color(0xFFD0BCFF)
    val onPrimary = Color(0xFF3C0091)
    val primaryContainer = Color(0xFFA078FF)
    val onPrimaryContainer = Color(0xFF340080)
    val primaryContainerSoft = Color(0x33A078FF)

    val secondary = Color(0xFF4CD7F6)
    val onSecondary = Color(0xFF003640)
    val secondaryContainer = Color(0xFF03B5D3)
    val secondarySoft = Color(0x264CD7F6)

    val tertiary = Color(0xFFFFB2B7)
    val tertiaryContainer = Color(0xFFFF516A)

    val error = Color(0xFFFFB4AB)
    val errorContainer = Color(0xFF93000A)
    val onErrorContainer = Color(0xFFFFDAD6)

    val emerald = Color(0xFF10B981)
    val neonCyan = Color(0xFF06B6D4)
    val mutedSlate = Color(0xFF64748B)
    val accentViolet = Color(0xFF8B5CF6)
    val accentRose = Color(0xFFF43F5E)

    val glass = Color(0xA81E2330)
    val sheetGlass = Color(0xF0151922)
}

object Shapes {
    val chip = RoundedCornerShape(50)
    val cardXl = RoundedCornerShape(16)
    val cardMd = RoundedCornerShape(12)
    val cardLg = RoundedCornerShape(20)
    val sheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 0.dp, bottomStart = 0.dp)
    val art = RoundedCornerShape(16)
    val square = RoundedCornerShape(8)
}

class AppTypography(
    val displaySpace: FontFamily,
    val bodyJakarta: FontFamily,
)

@Suppress("CompositionLocalAllowlist")
val LocalTypography = staticCompositionLocalOf { error("Typography not provided") }

@Composable
fun ObsidianTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Palette.primary,
            onPrimary = Palette.onPrimary,
            primaryContainer = Palette.primaryContainer,
            onPrimaryContainer = Palette.onPrimaryContainer,
            secondary = Palette.secondary,
            onSecondary = Palette.onSecondary,
            secondaryContainer = Palette.secondaryContainer,
            tertiary = Palette.tertiary,
            tertiaryContainer = Palette.tertiaryContainer,
            error = Palette.error,
            errorContainer = Palette.errorContainer,
            surface = Palette.background,
            onSurface = Palette.onSurface,
            surfaceVariant = Palette.surfaceVariant,
            onSurfaceVariant = Palette.onSurfaceVariant,
            outline = Palette.outline,
            outlineVariant = Palette.outlineVariant,
            background = Palette.background,
            onBackground = Palette.onSurface,
        ),
        content = content,
    )
}
