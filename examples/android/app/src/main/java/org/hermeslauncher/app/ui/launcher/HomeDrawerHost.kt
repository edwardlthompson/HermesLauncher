package org.hermeslauncher.app.ui.launcher

import android.content.Context
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.R
import org.hermeslauncher.app.icons.AppCatalog
import org.hermeslauncher.app.icons.DrawerPolicy
import org.hermeslauncher.app.icons.DrawerSnapshot
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.icons.LaunchableApp
import org.hermeslauncher.app.ui.widgets.dropCell
import org.hermeslauncher.app.ui.widgets.edgePageDelta
import org.hermeslauncher.app.widgets.WidgetHostState
import org.hermeslauncher.app.workspace.DesktopPin
import org.hermeslauncher.app.workspace.WorkspaceKind
import org.hermeslauncher.app.workspace.WorkspaceModel

@Composable
fun HomeDrawerHost(
    open: Boolean,
    homeDock: HomeDockSnapshot,
    pack: IconPackId,
    unread: Map<String, Int>,
    showDots: Boolean,
    assignSlot: Int?,
    pinDesktop: Boolean,
    workspace: WorkspaceModel,
    pagerState: PagerState,
    scope: CoroutineScope,
    widgets: WidgetHostState,
    gridCoords: LayoutCoordinates?,
    rootCoords: LayoutCoordinates?,
    pageWidthPx: Float,
    pageCount: Int,
    lastEdgeMs: Long,
    onDragging: (Boolean) -> Unit,
    onEdgeMs: (Long) -> Unit,
    onClose: () -> Unit,
    onOpenSearch: () -> Unit,
    onAbout: () -> Unit,
    onAssignConsumed: () -> Unit,
    onPinConsumed: () -> Unit,
) {
    var iconDrag by remember { mutableStateOf<LaunchableApp?>(null) }
    var dragWindow by remember { mutableStateOf(Offset.Zero) }
    if (!open && iconDrag == null) {
        return
    }
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    val snapshot by app.drawerPrefs.snapshot.collectAsStateWithLifecycle(DrawerSnapshot())
    val listing = if (assignSlot != null || pinDesktop) {
        homeDock.apps
    } else {
        DrawerPolicy.visible(homeDock.apps, snapshot.hidden)
    }
    AppDrawer(
        apps = listing,
        predicted = homeDock.predicted,
        assignMode = assignSlot != null || pinDesktop,
        pack = pack,
        unreadByPackage = unread,
        showDots = showDots,
        snapshot = snapshot,
        draggingOut = iconDrag != null,
        onOpenSearch = onOpenSearch,
        assignTitle = if (pinDesktop) R.string.desktop_pin_title else R.string.dock_assign_title,
        onIconDragStart = { chosen, window ->
            iconDrag = chosen
            dragWindow = window
            onDragging(true)
            ensureDesktopPage(workspace, pagerState, scope)
        },
        onIconDrag = { window ->
            dragWindow = window
            val now = System.currentTimeMillis()
            if (now - lastEdgeMs >= 700L) {
                val delta = edgePageDelta(window.x, pageWidthPx, pagerState.currentPage, pageCount)
                if (delta != 0) {
                    onEdgeMs(now)
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + delta) }
                }
            }
        },
        onIconDragEnd = {
            val chosen = iconDrag
            iconDrag = null
            onDragging(false)
            if (chosen != null) {
                dropDrawerIcon(app, workspace, pagerState, scope, chosen, gridCoords, dragWindow, widgets)
            }
            onClose()
        },
        onApp = { chosen ->
            placeDrawerPick(
                app = app,
                context = context,
                homeDock = homeDock,
                workspace = workspace,
                pagerState = pagerState,
                scope = scope,
                chosen = chosen,
                assignSlot = assignSlot,
                pinDesktop = pinDesktop,
                onAssignConsumed = onAssignConsumed,
                onPinConsumed = onPinConsumed,
            )
            onClose()
        },
        onAbout = onAbout,
    )
    HomeIconDrag(app = iconDrag, pack = pack, dragWindow = dragWindow, rootCoords = rootCoords)
}

private fun ensureDesktopPage(
    workspace: WorkspaceModel,
    pagerState: PagerState,
    scope: CoroutineScope,
) {
    if (workspace.widgetPageAt(pagerState.currentPage) >= 1) {
        return
    }
    val idx = workspace.screens.indexOfFirst { it.kind == WorkspaceKind.DESKTOP }
    if (idx >= 0) {
        scope.launch { pagerState.scrollToPage(idx) }
    }
}

private fun dropDrawerIcon(
    app: HermesApplication,
    workspace: WorkspaceModel,
    pagerState: PagerState,
    scope: CoroutineScope,
    chosen: LaunchableApp,
    gridCoords: LayoutCoordinates?,
    dragWindow: Offset,
    widgets: WidgetHostState,
) {
    val page = workspace.widgetPageAt(pagerState.currentPage)
    val target = dropCell(gridCoords, dragWindow, page, widgets.grid)
    scope.launch {
        val latest = app.desktopStore.layout.first()
        val host = app.widgetStore.state.first()
        val result = if (target != null) {
            DesktopPin.drop(latest, host, target.first, chosen, target.second, target.third)
        } else if (page <= 0) {
            DesktopPin.place(latest, host, 1, chosen)
        } else {
            null
        }
        result?.let {
            app.desktopStore.save(it.layout)
            val idx = workspace.screens.indexOfFirst { screen ->
                workspace.desktopPageIndex(screen.id) == it.pageIndex
            }
            if (idx >= 0) {
                pagerState.scrollToPage(idx)
            }
        }
    }
}

private fun placeDrawerPick(
    app: HermesApplication,
    context: Context,
    homeDock: HomeDockSnapshot,
    workspace: WorkspaceModel,
    pagerState: PagerState,
    scope: CoroutineScope,
    chosen: LaunchableApp,
    assignSlot: Int?,
    pinDesktop: Boolean,
    onAssignConsumed: () -> Unit,
    onPinConsumed: () -> Unit,
) {
    when {
        assignSlot != null -> {
            scope.launch { app.dockStore.save(homeDock.dock.withApp(assignSlot, chosen)) }
            onAssignConsumed()
        }
        pinDesktop -> {
            val widgetPage = workspace.widgetPageAt(pagerState.currentPage).coerceAtLeast(1)
            scope.launch {
                val latest = app.desktopStore.layout.first()
                val host = app.widgetStore.state.first()
                DesktopPin.place(latest, host, widgetPage, chosen)?.let { result ->
                    app.desktopStore.save(result.layout)
                    val idx = workspace.screens.indexOfFirst {
                        workspace.desktopPageIndex(it.id) == result.pageIndex
                    }
                    if (idx >= 0) {
                        pagerState.scrollToPage(idx)
                    }
                }
            }
            onPinConsumed()
        }
        else -> AppCatalog.launch(context, chosen)
    }
}
