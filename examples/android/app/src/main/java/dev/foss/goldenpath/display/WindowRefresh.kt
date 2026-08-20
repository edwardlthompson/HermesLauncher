package dev.foss.goldenpath.display

import android.view.Display
import android.view.Window

object WindowRefresh {
    /** Request the display's fastest mode that keeps the current pixel size. */
    fun applyFastestSameResolution(window: Window, display: Display?) {
        if (display == null) return
        val currentMode = display.mode
        val current = DisplayModeChoice(
            modeId = currentMode.modeId,
            widthPx = currentMode.physicalWidth,
            heightPx = currentMode.physicalHeight,
            refreshHz = currentMode.refreshRate,
        )
        val modes = display.supportedModes.map { mode ->
            DisplayModeChoice(
                modeId = mode.modeId,
                widthPx = mode.physicalWidth,
                heightPx = mode.physicalHeight,
                refreshHz = mode.refreshRate,
            )
        }
        val fastest = DisplayModeSelector.fastestSameResolution(modes, current) ?: return
        val attrs = window.attributes
        attrs.preferredDisplayModeId = fastest.modeId
        window.attributes = attrs
    }
}
