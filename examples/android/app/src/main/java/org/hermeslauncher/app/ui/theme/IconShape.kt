package org.hermeslauncher.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Adaptive-icon clip shapes (Nova/AOSP-style). SYSTEM leaves the bitmap unclipped. */
enum class IconShape {
    SYSTEM,
    CIRCLE,
    SQUIRCLE,
    SQUARE,
    TEARDROP,
    ;

    fun asComposeShape(): Shape = when (this) {
        SYSTEM -> RectangleShape
        CIRCLE -> CircleShape
        SQUIRCLE -> RoundedCornerShape(28)
        SQUARE -> RoundedCornerShape(4.dp)
        TEARDROP -> RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp, bottomStart = 48.dp, bottomEnd = 12.dp)
    }

    companion object {
        fun parse(raw: String?): IconShape {
            if (raw.isNullOrBlank()) return SYSTEM
            return entries.firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) } ?: SYSTEM
        }
    }
}
