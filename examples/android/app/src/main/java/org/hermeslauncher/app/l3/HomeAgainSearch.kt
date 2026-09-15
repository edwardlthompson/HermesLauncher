package org.hermeslauncher.app.l3

import android.content.Intent
import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherState
import org.hermeslauncher.app.HermesWorkspace
import org.hermeslauncher.app.launcher.HomePulse
import org.hermeslauncher.app.launcher.HomePulseResult

/** Second Home on Inbox opens search; Home from other pages snaps to Inbox. */
object HomeAgainSearch {
    fun shouldOpen(
        alreadyOnHome: Boolean,
        inNormal: Boolean,
        onInbox: Boolean,
        floatingOpen: Boolean,
        actionMain: Boolean,
    ): Boolean {
        if (!alreadyOnHome || !inNormal || !onInbox || floatingOpen || !actionMain) {
            return false
        }
        return true
    }

    fun shouldSnap(actionMain: Boolean, onInbox: Boolean, openSearch: Boolean): Boolean {
        return actionMain && !onInbox && !openSearch
    }

    fun plan(launcher: Launcher, intent: Intent): HomePress {
        val actionMain = Intent.ACTION_MAIN == intent.action
        val onInbox = onInbox(launcher)
        val openSearch = shouldOpen(
            alreadyOnHome = alreadyOnHome(launcher, intent),
            inNormal = launcher.isInState(LauncherState.NORMAL),
            onInbox = onInbox,
            floatingOpen = AbstractFloatingView.getTopOpenView(launcher) != null,
            actionMain = actionMain,
        )
        return HomePress(openSearch, shouldSnap(actionMain, onInbox, openSearch))
    }

    fun apply(launcher: Launcher, press: HomePress) {
        if (press.openSearch) {
            show(launcher)
            return
        }
        if (!press.snap) return
        val ws = launcher.workspace as? HermesWorkspace ?: return
        if (ws.nextPage != ws.homeIndex()) ws.snapToPage(ws.homeIndex())
    }

    fun onInbox(launcher: Launcher): Boolean {
        if (!launcher.isInState(LauncherState.NORMAL)) {
            return false
        }
        val workspace = launcher.workspace as? HermesWorkspace ?: return false
        return workspace.nextPage == workspace.homeIndex()
    }

    fun fromPulse(page: Int, homeIndex: Int): Boolean {
        return HomePulse.next(page, searchOpen = false, homeIndex = homeIndex) ==
            HomePulseResult.OPEN_SEARCH
    }

    fun alreadyOnHome(launcher: Launcher, _intent: Intent): Boolean {
        return launcher.hasWindowFocus()
    }

    fun show(launcher: Launcher) {
        L3Caches.pendingSearchFocus = true
        if (launcher.isInState(LauncherState.ALL_APPS)) {
            focus(launcher)
            return
        }
        launcher.stateManager.goToState(LauncherState.ALL_APPS)
    }

    fun focus(launcher: Launcher) {
        val edit = launcher.appsView.searchUiManager.editText ?: return
        edit.requestFocus()
        edit.showKeyboard()
    }
}

class HomePress(val openSearch: Boolean, val snap: Boolean)
