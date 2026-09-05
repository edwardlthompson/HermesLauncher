package org.hermeslauncher.app.l3

import com.android.launcher3.Launcher
import org.hermeslauncher.app.launcher.GestureRunner
import org.hermeslauncher.app.launcher.LauncherAction
import org.hermeslauncher.app.oem.HomeActions

object L3GestureHost {
    fun onDoubleTap(launcher: Launcher) {
        when (L3Caches.doubleTap) {
            org.hermeslauncher.app.launcher.DoubleTapAction.OFF -> Unit
            org.hermeslauncher.app.launcher.DoubleTapAction.LOCK -> HomeActions.lockOrPrompt(launcher)
            org.hermeslauncher.app.launcher.DoubleTapAction.FLASHLIGHT -> HomeActions.toggleTorch(launcher)
        }
    }

    fun afterAllApps(launcher: Launcher) {
        if (!L3Caches.pendingSearchFocus) {
            return
        }
        L3Caches.pendingSearchFocus = false
        HomeAgainSearch.focus(launcher)
    }

    fun run(launcher: Launcher, action: LauncherAction) {
        GestureRunner.run(
            launcher,
            action,
            onDrawer = { launcher.stateManager.goToState(com.android.launcher3.LauncherState.ALL_APPS) },
            onSearch = { HomeAgainSearch.show(launcher) },
            requestCamera = { },
        )
    }
}
