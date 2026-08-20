package dev.foss.goldenpath.display

import androidx.compose.ui.FrameRateCategory
import androidx.compose.ui.Modifier
import androidx.compose.ui.preferredFrameRate

/** Vote HIGH so adaptive-refresh panels can ramp during scroll flings. */
fun Modifier.highRefreshScroll(): Modifier = preferredFrameRate(FrameRateCategory.High)
