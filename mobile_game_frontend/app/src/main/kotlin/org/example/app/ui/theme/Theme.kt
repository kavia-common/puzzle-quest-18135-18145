package org.example.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Ocean Professional Theme for a light, modern appearance.
 * Applies the style guide colors, rounded shapes, and clean typography.
 * Simplified to avoid dynamic color and window insets to ensure compatibility in CI toolchains.
 */

// Light color scheme mapped to Ocean palette
private val LightOceanColorScheme: ColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = OnPrimary,
    secondary = AmberSecondary,
    onSecondary = OnSecondary,
    error = Error,
    onError = OnPrimary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline
)

// PUBLIC_INTERFACE
@Composable
fun OceanTheme(
    content: @Composable () -> Unit
) {
    /** Root MaterialTheme wrapper applying Ocean Professional design tokens (light only). */
    MaterialTheme(
        colorScheme = LightOceanColorScheme,
        typography = OceanTypography,
        shapes = OceanShapes,
        content = content
    )
}
