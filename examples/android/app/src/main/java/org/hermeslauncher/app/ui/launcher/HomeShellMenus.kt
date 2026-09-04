package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.feeds.FeedItem
import org.hermeslauncher.app.feeds.MiniPlayerState
import org.hermeslauncher.app.icons.AppCatalog
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.vault.ShadeBridge
import org.hermeslauncher.app.vault.VaultItem
import org.hermeslauncher.app.widgets.WidgetHostController
import org.hermeslauncher.app.workspace.WorkspaceModel

@Composable
fun HomeShellMenus(
    homeMenu: Boolean,
    searchOpen: Boolean,
    homeDock: HomeDockSnapshot,
    items: List<VaultItem>,
    feeds: List<FeedItem>,
    pack: IconPackId,
    workspace: WorkspaceModel,
    pagerState: PagerState,
    widgetController: WidgetHostController,
    playerState: MiniPlayerState,
    onHomeMenu: (Boolean) -> Unit,
    onPinDesktop: () -> Unit,
    onOpenSettings: () -> Unit,
    onSearch: (Boolean) -> Unit,
    onPlayer: (MiniPlayerState) -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    HomeOptionsPopup(
        visible = homeMenu,
        onWidgets = {
            onHomeMenu(false)
            widgetController.openPicker(workspace.widgetPageAt(pagerState.currentPage).coerceAtLeast(1))
        },
        onAddIcon = {
            onHomeMenu(false)
            onPinDesktop()
        },
        onSettings = { onHomeMenu(false); onOpenSettings() },
        onDismiss = { onHomeMenu(false) },
    )
    HomeSearchOverlay(
        visible = searchOpen,
        apps = homeDock.apps,
        predicted = homeDock.predicted,
        usage = homeDock.usage,
        inbox = items,
        feeds = feeds,
        pack = pack,
        onApp = { AppCatalog.launch(context, it); onSearch(false) },
        onInbox = { item -> ShadeBridge.open(item, context); onSearch(false) },
        onFeed = { item ->
            item.enclosureUrl?.takeIf { it.isNotBlank() }?.let { url ->
                app.player.play(url)
                onPlayer(playerState.load(item))
            }
            onSearch(false)
        },
        onClose = { onSearch(false) },
    )
}
