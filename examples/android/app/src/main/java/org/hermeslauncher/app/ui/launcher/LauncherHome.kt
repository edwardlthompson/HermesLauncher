package org.hermeslauncher.app.ui.launcher

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.MiniPlayerState
import org.hermeslauncher.app.icons.AppCatalog
import org.hermeslauncher.app.icons.DockLayout
import org.hermeslauncher.app.icons.DockMode
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.launcher.DoubleTapAction
import org.hermeslauncher.app.launcher.DrawerState
import org.hermeslauncher.app.launcher.HomePagerState
import org.hermeslauncher.app.launcher.HomePulse
import org.hermeslauncher.app.launcher.HomePulseResult
import org.hermeslauncher.app.oem.HomeActions
import org.hermeslauncher.app.oem.LivePermissions
import org.hermeslauncher.app.oem.OemDetector
import org.hermeslauncher.app.oem.RepairPolicy
import org.hermeslauncher.app.ui.onboarding.GrantChrome
import org.hermeslauncher.app.ui.player.MiniPlayerBar
import org.hermeslauncher.app.ui.widgets.WidgetPage
import org.hermeslauncher.app.vault.InboxFilter
import org.hermeslauncher.app.widgets.WidgetChoice
import org.hermeslauncher.app.widgets.WidgetHostController
import org.hermeslauncher.app.widgets.WidgetHostState

@Composable
fun LauncherHome(
    widgetController: WidgetHostController,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    val pm = context.packageManager
    val items by app.vault.visibleItems.collectAsStateWithLifecycle(emptyList())
    val rebuild by app.vault.rebuildRequired.collectAsStateWithLifecycle()
    val feeds by app.feeds.feedItems.collectAsStateWithLifecycle()
    val widgets by app.widgetStore.state.collectAsStateWithLifecycle(WidgetHostState())
    val dockRaw by app.dockStore.layout.collectAsStateWithLifecycle(DockLayout())
    val pack by app.iconPackStore.pack.collectAsStateWithLifecycle(IconPackId())
    val showDots by app.homePrefs.showDots.collectAsStateWithLifecycle(true)
    val bannerGone by app.homePrefs.usageBannerDismissed.collectAsStateWithLifecycle(false)
    val doubleTap by app.homePrefs.doubleTap.collectAsStateWithLifecycle(DoubleTapAction.OFF)
    val picker by widgetController.picker.collectAsStateWithLifecycle()
    var resumeTick by remember { mutableIntStateOf(0) }
    val homeDock = rememberHomeDock(pm, dockRaw, resumeTick)
    val pageCount = HomePagerState.pageCountFor(widgets.pages.size)
    val pagerState = rememberPagerState(pageCount = { pageCount })
    var drawer by remember { mutableStateOf(DrawerState()) }
    var assignSlot by remember { mutableStateOf<Int?>(null) }
    var playerState by remember { mutableStateOf(MiniPlayerState()) }
    var snapshot by remember { mutableStateOf(LivePermissions.snapshot(context)) }
    var grantsWereGood by remember { mutableStateOf(!RepairPolicy.needsOverlay(snapshot)) }
    var dragging by remember { mutableStateOf(false) }
    var dragChoice by remember { mutableStateOf<WidgetChoice?>(null) }
    var dragWindow by remember { mutableStateOf(Offset.Zero) }
    var gridCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var rootCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var lastEdgeMs by remember { mutableLongStateOf(0L) }
    var homeMenu by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    val unread = remember(items) { InboxFilter.unreadByPackage(items) }
    val usageGranted = LivePermissions.usageGranted(context)
    val oem = remember { OemDetector.detect(Build.MANUFACTURER, Build.DISPLAY) }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val mediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { snapshot = LivePermissions.snapshot(context) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) HomeActions.toggleTorch(context) }
    fun onEmptyDoubleTap() {
        HomeActions.onDoubleTap(context, doubleTap) { cameraLauncher.launch(Manifest.permission.CAMERA) }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                snapshot = LivePermissions.snapshot(context)
                resumeTick += 1
                if (!RepairPolicy.needsOverlay(snapshot)) {
                    grantsWereGood = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) {
        val current = app.dockStore.layout.first()
        if (current.mode == DockMode.CUSTOM && current.assigned.isEmpty()) {
            app.dockStore.save(AppCatalog.seeded(pm).copy(mode = DockMode.CUSTOM))
        }
    }
    LaunchedEffect(pageCount) {
        val max = (pageCount - 1).coerceAtLeast(0)
        if (pagerState.currentPage > max) {
            pagerState.scrollToPage(max)
        }
    }
    LaunchedEffect(pack.packageName) { app.iconLoader.clear() }
    val searchNow = rememberUpdatedState(searchOpen)
    LaunchedEffect(app) {
        app.homePulse.collect {
            when (HomePulse.next(pagerState.currentPage, searchNow.value)) {
                HomePulseResult.SCROLL_INBOX -> pagerState.animateScrollToPage(0)
                HomePulseResult.OPEN_SEARCH -> {
                    drawer = drawer.closed()
                    searchOpen = true
                }
                HomePulseResult.CLOSE_SEARCH -> searchOpen = false
            }
        }
    }
    BackHandler(enabled = drawer.open) {
        assignSlot = null
        drawer = drawer.closed()
    }
    BackHandler(enabled = picker != null && dragChoice == null) {
        widgetController.cancelPick()
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().onGloballyPositioned { rootCoords = it },
    ) {
        val pageWidthPx = constraints.maxWidth.toFloat()
        Column(modifier = Modifier.fillMaxSize()) {
            if (rebuild) {
                Text(text = stringResource(R.string.vault_crypto_rebuild_body), color = MaterialTheme.colorScheme.onSurface)
            }
            GrantChrome(
                snapshot = snapshot,
                grantsWereGood = grantsWereGood,
                oem = oem,
                onNotification = { LivePermissions.startSafe(context, LivePermissions.listenerSettings()) },
                onBattery = { LivePermissions.startSafe(context, LivePermissions.batterySettings(context.packageName)) },
                onHome = { LivePermissions.startSafe(context, LivePermissions.homeRoleSettings()) },
                onPhotos = { mediaLauncher.launch(LivePermissions.mediaPermission()) },
                onRepair = { LivePermissions.startSafe(context, LivePermissions.listenerSettings()) },
            )
            HorizontalPager(state = pagerState, userScrollEnabled = !dragging, modifier = Modifier.weight(1f)) { page ->
                if (page == 0) {
                    FeedPage(
                        items = items,
                        feeds = feeds,
                        onDismiss = { id -> scope.launch { app.vault.archive(id) } },
                        onDismissGroup = { ids -> scope.launch(Dispatchers.IO) { ids.forEach { app.vault.archive(it) } } },
                        onOpen = { id -> scope.launch { app.vault.open(id) } },
                        onAction = { id, index -> scope.launch { app.vault.runAction(id, index) } },
                        onPin = { id -> scope.launch { app.vault.togglePin(id) } },
                        onPlay = { item ->
                            val url = item.enclosureUrl
                            if (!url.isNullOrBlank()) {
                                app.player.play(url)
                                playerState = playerState.load(item)
                            }
                        },
                        onLongPressHome = { homeMenu = true },
                        onDoubleTapHome = ::onEmptyDoubleTap,
                    )
                } else {
                    WidgetPage(
                        page = widgets.page(page),
                        host = app.widgetHost,
                        grid = widgets.grid,
                        dragging = dragging,
                        onLongPressEmpty = { homeMenu = true },
                        onDoubleTapEmpty = ::onEmptyDoubleTap,
                        onAdd = { widgetController.openPicker(page) },
                        onMove = { id, x, y -> widgetController.relocate(page, page, id, x, y) },
                        onSpan = { binding -> widgetController.applySpan(page, binding) },
                        onRemove = { id -> widgetController.remove(page, id) },
                        onGridPositioned = { gridCoords = it },
                    )
                }
            }
            MiniPlayerBar(
                state = playerState,
                onToggle = { app.player.toggle(); playerState = playerState.toggle() },
                onStop = { app.player.stop(); playerState = playerState.stop() },
            )
            UsageAccessBanner(
                visible = dockRaw.mode == DockMode.USAGE && !usageGranted && !bannerGone,
                onOpen = { LivePermissions.startSafe(context, LivePermissions.usageSettings()) },
                onDismiss = { scope.launch { app.homePrefs.setUsageBannerDismissed(true) } },
            )
            DockBar(
                layout = homeDock.dock, pack = pack, custom = dockRaw.mode == DockMode.CUSTOM,
                unreadByPackage = unread, showDots = showDots,
                onOpenDrawer = { drawer = drawer.opened() }, onOpenSettings = onOpenSettings,
                onLaunch = { AppCatalog.launch(context, it) },
                onAssign = { index -> assignSlot = index; drawer = drawer.opened() },
            )
        }
        if (drawer.open) {
            AppDrawer(
                apps = homeDock.apps,
                predicted = homeDock.predicted,
                assignMode = assignSlot != null,
                pack = pack,
                unreadByPackage = unread,
                showDots = showDots,
                onOpenSearch = { drawer = drawer.closed(); searchOpen = true },
                onApp = { chosen ->
                    val slot = assignSlot
                    if (slot != null) {
                        scope.launch { app.dockStore.save(homeDock.dock.withApp(slot, chosen)) }
                        assignSlot = null
                    } else {
                        AppCatalog.launch(context, chosen)
                    }
                    drawer = drawer.closed()
                },
                onAbout = { drawer = drawer.closed(); onOpenAbout() },
            )
        }
        HomeWidgetDrag(
            picker = picker,
            dragChoice = dragChoice,
            dragWindow = dragWindow,
            rootCoords = rootCoords,
            gridCoords = gridCoords,
            widgets = widgets,
            pagerState = pagerState,
            pageCount = pageCount,
            pageWidthPx = pageWidthPx,
            lastEdgeMs = lastEdgeMs,
            scope = scope,
            widgetController = widgetController,
            onDragStart = { choice, window -> dragging = true; dragChoice = choice; dragWindow = window },
            onDragWindow = { dragWindow = it },
            onEdgeMs = { lastEdgeMs = it },
            setDragging = { dragging = it },
            setDragChoice = { dragChoice = it },
        )
        HomeOptionsPopup(
            visible = homeMenu,
            onWidgets = { homeMenu = false; widgetController.openPicker(pagerState.currentPage.coerceAtLeast(1)) },
            onSettings = { homeMenu = false; onOpenSettings() },
            onDismiss = { homeMenu = false },
        )
        HomeSearchOverlay(
            visible = searchOpen,
            apps = homeDock.apps,
            predicted = homeDock.predicted,
            usage = homeDock.usage,
            inbox = items,
            feeds = feeds,
            pack = pack,
            onApp = { AppCatalog.launch(context, it); searchOpen = false },
            onInbox = { item -> scope.launch { app.vault.open(item.id) }; searchOpen = false },
            onFeed = { item ->
                item.enclosureUrl?.takeIf { it.isNotBlank() }?.let { url ->
                    app.player.play(url); playerState = playerState.load(item)
                }
                searchOpen = false
            },
            onClose = { searchOpen = false },
        )
    }
}
