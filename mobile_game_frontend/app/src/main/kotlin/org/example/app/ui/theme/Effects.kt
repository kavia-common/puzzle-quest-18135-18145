package org.example.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.sp

/**
 * Helpers for gradients and subtle shadows consistent with the Ocean Professional theme.
 */
object OceanEffects {
    // PUBLIC_INTERFACE
    fun oceanBackgroundGradient(): Brush {
        /** Returns a top-to-bottom gradient for app backgrounds. */
        return Brush.verticalGradient(
            colors = listOf(GradientStart, GradientEnd),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )
    }

    // PUBLIC_INTERFACE
    fun subtleTextShadow(): TextStyle {
        /** Returns a subtle shadow to add depth to large title text. */
        return TextStyle(
            shadow = Shadow(
                color = Color(0x14000000), // ~8% black
                offset = Offset(0f, 1.5f),
                blurRadius = 3f
            ),
            fontSize = 16.sp
        )
    }
}
