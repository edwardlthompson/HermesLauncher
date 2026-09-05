package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import org.hermeslauncher.app.feeds.FeedItem
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.ui.workspace.DesktopPage
import org.hermeslauncher.app.vault.VaultItem
import org.hermeslauncher.app.widgets.HermesAppWidgetHost
import org.hermeslauncher.app.widgets.WidgetBinding
import org.hermeslauncher.app.widgets.WidgetHostState
import org.hermeslauncher.app.workspace.DesktopItem
import org.hermeslauncher.app.workspace.DesktopLayout
import org.hermeslauncher.app.workspace.WorkspaceKind
import org.hermeslauncher.app.workspace.WorkspaceModel

@Composable
fun WorkspacePager(
    model: WorkspaceModel,
    widgets: WidgetHostState,
    desktop: DesktopLayout,
    host: HermesAppWidgetHost,
    pagerState: PagerState,
    items: List<VaultItem>,
    feeds: List<FeedItem>,
    pack: IconPackId,
    showLabels: Boolean,
    dragging: Boolean,
    reverseLayout: Boolean,
    onDismiss: (String) -> Unit,
    onDismissGroup: (List<String>) -> Unit,
    onPin: (String) -> Unit,
    onPlay: (FeedItem) -> Unit,
    onLongPressHome: () -> Unit,
    onDoubleTapHome: () -> Unit,
    onAddWidget: (Int) -> Unit,
    onMove: (Int, Int, Int, Int) -> Unit,
    onSpan: (Int, WidgetBinding) -> Unit,
    onRemove: (Int, Int) -> Unit,
    onGridPositioned: (LayoutCoordinates) -> Unit,
    onLaunchIcon: (DesktopItem.Shortcut) -> Unit,
    onRemoveIcon: (Int, Long) -> Unit,
    onMoveIcon: (Int, Long, Int, Int) -> Unit,
    onAddFeed: suspend (String) -> Boolean,
    onEmptySwipe: (org.hermeslauncher.app.launcher.GestureSlot) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    HorizontalPager(
        state = pagerState,
        userScrollEnabled = !dragging,
        reverseLayout = reverseLayout,
        beyondViewportPageCount = 1,
        modifier = modifier,
    ) { page ->
        val screen = model.screens.getOrNull(page)
        when (screen?.kind) {
            WorkspaceKind.PODCASTS -> HermesPodcastsPage(
                onLongPressHome = onLongPressHome,
            )
            WorkspaceKind.FEEDS -> HermesNewsPage(
                onLongPressHome = onLongPressHome,
            )
            WorkspaceKind.INBOX -> FeedPage(
                items = items,
                feeds = emptyList(),
                onDismiss = onDismiss,
                onDismissGroup = onDismissGroup,
                onPin = onPin,
                onPlay = onPlay,
                onLongPressHome = onLongPressHome,
                onDoubleTapHome = onDoubleTapHome,
                onEmptySwipe = onEmptySwipe,
            )
            WorkspaceKind.DESKTOP -> {
                val widgetPage = model.desktopPageIndex(screen.id) ?: 1
                DesktopPage(
                    page = widgets.page(widgetPage),
                    shortcuts = desktop.page(widgetPage),
                    host = host,
                    grid = widgets.grid,
                    pack = pack,
                    showLabels = showLabels,
                    dragging = dragging,
                    onLongPressEmpty = onLongPressHome,
                    onDoubleTapEmpty = onDoubleTapHome,
                    onAdd = { onAddWidget(widgetPage) },
                    onMove = { id, x, y -> onMove(widgetPage, id, x, y) },
                    onSpan = { binding -> onSpan(widgetPage, binding) },
                    onRemove = { id -> onRemove(widgetPage, id) },
                    onGridPositioned = onGridPositioned,
                    onLaunchIcon = onLaunchIcon,
                    onRemoveIcon = { id -> onRemoveIcon(widgetPage, id) },
                    onMoveIcon = { id, x, y -> onMoveIcon(widgetPage, id, x, y) },
                    onEmptySwipe = onEmptySwipe,
                )
            }
            null -> Unit
        }
    }
}
