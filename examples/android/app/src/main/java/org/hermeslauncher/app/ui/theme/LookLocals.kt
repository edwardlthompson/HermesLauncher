package org.hermeslauncher.app.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalIconShape = compositionLocalOf { IconShape.SYSTEM }
val LocalBadgeStyle = compositionLocalOf { BadgeStyle.COUNTS }
val LocalBadgeColorArgb = compositionLocalOf<Int?> { null }
val LocalLabelShadow = compositionLocalOf { true }

fun badgeColorOrNull(argb: Int?): Color? = argb?.let { Color(it) }
