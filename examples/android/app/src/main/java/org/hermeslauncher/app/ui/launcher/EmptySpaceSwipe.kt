package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import org.hermeslauncher.app.launcher.GestureSlot
import kotlin.math.abs

fun Modifier.emptySpaceSwipe(
    onSlot: (GestureSlot) -> Unit,
    thresholdPx: Float = 96f,
): Modifier = pointerInput(thresholdPx) {
    var total = 0f
    detectVerticalDragGestures(
        onDragStart = { total = 0f },
        onVerticalDrag = { _, dragAmount ->
            total += dragAmount
        },
        onDragEnd = {
            when {
                total < -thresholdPx -> onSlot(GestureSlot.SWIPE_UP)
                total > thresholdPx -> onSlot(GestureSlot.SWIPE_DOWN)
            }
            total = 0f
        },
        onDragCancel = { total = 0f },
    )
}

fun emptySpaceShouldFire(total: Float, thresholdPx: Float): GestureSlot? {
    if (abs(total) < thresholdPx) {
        return null
    }
    return if (total < 0f) GestureSlot.SWIPE_UP else GestureSlot.SWIPE_DOWN
}
