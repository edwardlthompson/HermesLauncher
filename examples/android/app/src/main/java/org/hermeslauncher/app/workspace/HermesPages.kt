package org.hermeslauncher.app.workspace

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.launcher3.Launcher
import com.android.launcher3.Workspace
import com.android.launcher3.celllayout.CellLayoutLayoutParams
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.launcher.HermesInboxPage
import org.hermeslauncher.app.ui.launcher.HermesNewsPage
import org.hermeslauncher.app.ui.launcher.HermesPageTheme
import org.hermeslauncher.app.ui.launcher.HermesPodcastsPage
import org.hermeslauncher.app.ui.launcher.OverlayBackHost

/** Inserts Podcasts, News, then Inbox CellLayouts at the start of Workspace. */
object HermesPages {
    fun ensure(workspace: Workspace<*>) {
        val launcher = Launcher.getLauncher(workspace.context)
        attach(workspace, launcher, HermesScreens.PODCASTS, 0, R.id.page_podcasts, WorkspaceKind.PODCASTS)
        attach(workspace, launcher, HermesScreens.NEWS, 1, R.id.page_news, WorkspaceKind.FEEDS)
        attach(workspace, launcher, HermesScreens.INBOX, 2, R.id.page_inbox, WorkspaceKind.INBOX)
    }

    private fun attach(
        workspace: Workspace<*>,
        launcher: Launcher,
        screenId: Int,
        insertIndex: Int,
        viewId: Int,
        kind: WorkspaceKind,
    ) {
        var screen = workspace.getScreenWithId(screenId)
        if (screen == null) {
            val index = insertIndex.coerceAtMost(workspace.childCount)
            screen = workspace.insertNewWorkspaceScreen(screenId, index)
        }
        if (screen.getShortcutsAndWidgets().findViewById<View>(viewId) != null) {
            return
        }
        val inv = launcher.deviceProfile.inv
        val lp = CellLayoutLayoutParams(
            0,
            0,
            inv.numColumns.coerceAtLeast(1),
            inv.numRows.coerceAtLeast(1),
        )
        lp.canReorder = false
        val compose = ComposeView(launcher).apply {
            id = viewId
            fitsSystemWindows = false
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, _ -> WindowInsetsCompat.CONSUMED }
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                HermesPageTheme(launcher) {
                    OverlayBackHost {
                        val openMenu = { launcher.showDefaultOptions(-1f, -1f) }
                        when (kind) {
                            WorkspaceKind.PODCASTS -> HermesPodcastsPage(onLongPressHome = openMenu)
                            WorkspaceKind.FEEDS -> HermesNewsPage(onLongPressHome = openMenu)
                            else -> HermesInboxPage(onLongPressHome = openMenu)
                        }
                    }
                }
            }
        }
        val host = HermesPageHost(launcher)
        host.fitsSystemWindows = false
        ViewCompat.setOnApplyWindowInsetsListener(host) { _, _ -> WindowInsetsCompat.CONSUMED }
        host.addView(
            compose,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        screen.addViewToCellLayout(host, 0, viewId, lp, true)
    }
}
