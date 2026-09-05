package org.hermeslauncher.app.l3

import android.view.MotionEvent
import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherState
import com.android.launcher3.util.TouchController
import org.hermeslauncher.app.HermesWorkspace
import org.hermeslauncher.app.launcher.isInstant
import org.hermeslauncher.app.workspace.HermesScreens
import kotlin.math.abs

/** Lock / flashlight / shade from a vertical swipe (not All Apps). */
class L3InstantSwipe(private val launcher: Launcher) : TouchController {
    private var startY = 0f
    private var tracking = false
    private var fired = false

    override fun onControllerInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startY = ev.rawY
                fired = false
                tracking = canStart(ev)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!tracking || fired) {
                    return false
                }
                val dy = ev.rawY - startY
                val action = if (dy < 0) L3Caches.actionUp else L3Caches.actionDown
                if (!action.isInstant() || abs(dy) < L3Caches.sensitivity.emptySpacePx()) {
                    return false
                }
                fired = true
                tracking = false
                L3GestureHost.run(launcher, action)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> tracking = false
        }
        return false
    }

    override fun onControllerTouchEvent(ev: MotionEvent): Boolean = false

    private fun canStart(ev: MotionEvent): Boolean {
        if (!launcher.isInState(LauncherState.NORMAL)) {
            return false
        }
        if (AbstractFloatingView.getTopOpenView(launcher) != null) {
            return false
        }
        if (!L3Caches.actionUp.isInstant() && !L3Caches.actionDown.isInstant()) {
            return false
        }
        val workspace = launcher.workspace as? HermesWorkspace ?: return true
        val reserved = HermesScreens.isReserved(workspace.getScreenIdForPageIndex(workspace.nextPage))
        return !reserved || onHotseat(ev)
    }

    private fun onHotseat(ev: MotionEvent): Boolean {
        val hotseat = launcher.hotseat ?: return false
        val loc = IntArray(2)
        hotseat.getLocationOnScreen(loc)
        return ev.rawY >= loc[1]
    }
}
