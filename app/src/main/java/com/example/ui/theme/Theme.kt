package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ColorError = androidx.compose.ui.graphics.Color(0xFFFF5A79)

private val RedtorDarkColorScheme = darkColorScheme(
    primary = RubyCrimson,
    onPrimary = TextPrimary,
    primaryContainer = RubyDeep,
    onPrimaryContainer = RoseGoldAccent,
    secondary = RubyGlow,
    onSecondary = ObsidianVoid,
    secondaryContainer = ObsidianElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = RoseGoldAccent,
    background = ObsidianVoid,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianElevated,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorderSpecular,
    error = ColorError
)

val RedtorShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

@Composable
fun RedtorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RedtorDarkColorScheme,
        typography = RedtorTypography,
        shapes = RedtorShapes,
        content = content
    )
}
