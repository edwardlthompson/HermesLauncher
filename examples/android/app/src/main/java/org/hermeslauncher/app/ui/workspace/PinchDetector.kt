package org.hermeslauncher.app.ui.workspace

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.pinchAction(onPinch: () -> Unit): Modifier {
    return pointerInput(onPinch) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            var zoom = 1f
            do {
                val event = awaitPointerEvent()
                if (event.changes.size >= 2) {
                    zoom *= event.calculateZoom()
                    event.changes.forEach { change -> change.consume() }
                    if (zoom < 0.92f || zoom > 1.08f) {
                        onPinch()
                        return@awaitEachGesture
                    }
                }
            } while (event.changes.any { it.pressed })
        }
    }
}
