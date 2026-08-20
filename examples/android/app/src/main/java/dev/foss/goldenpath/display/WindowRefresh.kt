package dev.foss.goldenpath.display

import android.app.Activity
import android.os.Build
import android.view.Display
import android.view.Window

object WindowRefresh {
    /** Resolve a display after the window is attached (decorView.display is often null in onCreate). */
    fun applyTo(activity: Activity) {
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.display
        } else {
            @Suppress("DEPRECATION")
            activity.windowManager.defaultDisplay
        }
        applyFastestSameResolution(activity.window, display)
    }

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
