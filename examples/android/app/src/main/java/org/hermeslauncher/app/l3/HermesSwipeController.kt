package org.hermeslauncher.app.l3

import android.view.MotionEvent
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherState
import com.android.launcher3.touch.AllAppsSwipeController
import org.hermeslauncher.app.HermesWorkspace
import org.hermeslauncher.app.launcher.LauncherAction
import org.hermeslauncher.app.launcher.opensApps
import org.hermeslauncher.app.workspace.HermesScreens

/**
 * Drawer swipe with adjustable slop. Inbox/News vertical drags stay with Compose
 * unless the pointer starts on the dock.
 */
class HermesSwipeController(launcher: Launcher) : AllAppsSwipeController(launcher) {
    override fun canInterceptTouch(ev: MotionEvent): Boolean {
        mDetector.setTouchSlopMultiplier(L3Caches.sensitivity.slopMultiplier())
        if (!super.canInterceptTouch(ev)) {
            return false
        }
        if (mLauncher.isInState(LauncherState.ALL_APPS)) {
            return true
        }
        val workspace = mLauncher.workspace as? HermesWorkspace ?: return true
        val screen = workspace.getScreenIdForPageIndex(workspace.nextPage)
        val reserved = HermesScreens.isReserved(screen)
        val opens = swipeUpOpensApps() || swipeDownOpensApps()
        return HermesSwipeGate.intercept(reserved, isOnHotseat(ev), opens)
    }

    override fun getTargetState(fromState: LauncherState, isDragTowardPositive: Boolean): LauncherState {
        val target = when {
            fromState == LauncherState.NORMAL && isDragTowardPositive && swipeUpOpensApps() ->
                LauncherState.ALL_APPS
            fromState == LauncherState.NORMAL && !isDragTowardPositive && swipeDownOpensApps() ->
                LauncherState.ALL_APPS
            else -> super.getTargetState(fromState, isDragTowardPositive)
        }
        if (target == LauncherState.ALL_APPS && fromState == LauncherState.NORMAL) {
            val action = if (isDragTowardPositive) L3Caches.actionUp else L3Caches.actionDown
            L3Caches.pendingSearchFocus = action == LauncherAction.SEARCH
        }
        return target
    }

    private fun swipeUpOpensApps(): Boolean = L3Caches.actionUp.opensApps()

    private fun swipeDownOpensApps(): Boolean = L3Caches.actionDown.opensApps()

    private fun isOnHotseat(ev: MotionEvent): Boolean {
        val hotseat = mLauncher.hotseat ?: return false
        val loc = IntArray(2)
        hotseat.getLocationOnScreen(loc)
        return ev.rawY >= loc[1]
    }
}

object HermesSwipeGate {
    fun intercept(reserved: Boolean, onHotseat: Boolean, opensApps: Boolean): Boolean {
        if (!opensApps) {
            return false
        }
        return !reserved || onHotseat
    }
}
