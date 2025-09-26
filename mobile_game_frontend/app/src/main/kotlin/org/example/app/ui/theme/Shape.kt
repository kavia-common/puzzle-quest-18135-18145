package org.example.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Rounded corner shapes to deliver a modern, approachable feel across UI components.
 * - ExtraSmall: small chips and indicators
 * - Small: inputs, small buttons
 * - Medium: cards, modals, list items
 * - Large: large containers or sheets
 */
val OceanShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)
