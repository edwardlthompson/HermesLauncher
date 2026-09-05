package org.hermeslauncher.app.ui.launcher

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import org.hermeslauncher.app.icons.LaunchableApp
import org.hermeslauncher.app.launcher.DoubleTapAction
import org.hermeslauncher.app.launcher.DrawerState
import org.hermeslauncher.app.launcher.GestureMap
import org.hermeslauncher.app.launcher.GestureSlot
import org.hermeslauncher.app.launcher.HomePulse
import org.hermeslauncher.app.launcher.HomePulseResult
import org.hermeslauncher.app.oem.HomeActions
import org.hermeslauncher.app.oem.LivePermissions
import org.hermeslauncher.app.oem.OemDetector
import org.hermeslauncher.app.oem.RepairPolicy
import org.hermeslauncher.app.ui.onboarding.HomeGrantChrome
import org.hermeslauncher.app.ui.player.MiniPlayerBar
import org.hermeslauncher.app.ui.workspace.PageIndicator
import org.hermeslauncher.app.ui.workspace.QsbBar
import org.hermeslauncher.app.ui.workspace.pinchAction
import org.hermeslauncher.app.vault.InboxFilter
import org.hermeslauncher.app.widgets.WidgetChoice
import org.hermeslauncher.app.widgets.WidgetHostController
import org.hermeslauncher.app.widgets.WidgetHostState
import org.hermeslauncher.app.workspace.DesktopLayout
import org.hermeslauncher.app.workspace.DesktopPin
import org.hermeslauncher.app.workspace.PagedPolicy
import org.hermeslauncher.app.workspace.PinchTarget
import org.hermeslauncher.app.workspace.QsbPlacement
import org.hermeslauncher.app.workspace.ScrollMode
import org.hermeslauncher.app.workspace.WorkspaceModel

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
    val desktop by app.desktopStore.layout.collectAsStateWithLifecycle(DesktopLayout())
    val showDots by app.homePrefs.showDots.collectAsStateWithLifecycle(true)
    val showLabels by app.homePrefs.showLabels.collectAsStateWithLifecycle(true)
    val bannerGone by app.homePrefs.usageBannerDismissed.collectAsStateWithLifecycle(false)
    val doubleTap by app.homePrefs.doubleTap.collectAsStateWithLifecycle(DoubleTapAction.OFF)
    val qsb by app.pagedPrefs.qsb.collectAsStateWithLifecycle(QsbPlacement.NONE)
    val scrollMode by app.pagedPrefs.scrollMode.collectAsStateWithLifecycle(ScrollMode.ADJACENT)
    val pinch by app.pagedPrefs.pinch.collectAsStateWithLifecycle(PinchTarget.ALL_APPS)
    val gestureMap by app.gesturePrefs.map.collectAsStateWithLifecycle(GestureMap.defaults())
    val picker by widgetController.picker.collectAsStateWithLifecycle()
    var resumeTick by remember { mutableIntStateOf(0) }
    val homeDock = rememberHomeDock(pm, dockRaw, resumeTick)
    val workspace = remember(widgets) { WorkspaceModel.migrate(widgets) }
    val pageCount = workspace.screenIds.size
    val homeIndex = workspace.homePagerIndex()
    val pagerState = rememberPagerState(initialPage = homeIndex, pageCount = { pageCount })
    var drawer by remember { mutableStateOf(DrawerState()) }
    var assignSlot by remember { mutableStateOf<Int?>(null) }
    var pinDesktop by remember { mutableStateOf(false) }
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
    fun fireGesture(slot: GestureSlot) = fireHomeGesture(
        context, slot, gestureMap, pinch, cameraLauncher,
        openDrawer = { drawer = drawer.opened() },
        openSearch = { searchOpen = true },
        openOverview = { homeMenu = true },
    )
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                snapshot = LivePermissions.snapshot(context); resumeTick += 1
                if (!RepairPolicy.needsOverlay(snapshot)) grantsWereGood = true
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
    val pageNow = rememberUpdatedState(pagerState.currentPage)
    val homeNow = rememberUpdatedState(homeIndex)
    LaunchedEffect(app) {
        app.homePulse.collect {
            when (HomePulse.next(pageNow.value, searchNow.value, homeNow.value)) {
                HomePulseResult.SCROLL_INBOX -> pagerState.animateScrollToPage(homeNow.value)
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
        pinDesktop = false
        drawer = drawer.closed()
    }
    BackHandler(enabled = picker != null && dragChoice == null) {
        widgetController.cancelPick()
    }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pinchAction { fireGesture(GestureSlot.PINCH) }
            .onGloballyPositioned { rootCoords = it },
    ) {
        val pageWidthPx = constraints.maxWidth.toFloat()
        Column(modifier = Modifier.fillMaxSize()) {
            if (rebuild) Text(stringResource(R.string.vault_crypto_rebuild_body), color = MaterialTheme.colorScheme.onSurface)
            HomeGrantChrome(
                snapshot = snapshot,
                grantsWereGood = grantsWereGood,
                oem = oem,
                onPhotos = { mediaLauncher.launch(LivePermissions.mediaPermission()) },
                onLater = { grantsWereGood = true },
            )
            if (qsb == QsbPlacement.TOP) QsbBar(placement = qsb, onOpen = { searchOpen = true })
            WorkspacePager(
                model = workspace,
                widgets = widgets,
                desktop = desktop,
                host = app.widgetHost,
                pagerState = pagerState,
                items = items,
                feeds = feeds,
                pack = pack,
                showLabels = showLabels,
                dragging = dragging,
                reverseLayout = PagedPolicy.reverseLayout(scrollMode),
                onDismiss = { id -> scope.launch { app.vault.archive(id) } },
                onDismissGroup = { ids -> scope.launch(Dispatchers.IO) { ids.forEach { app.vault.archive(it) } } },
                onPin = { id -> scope.launch { app.vault.togglePin(id) } },
                onPlay = { item ->
                    item.enclosureUrl?.takeIf { it.isNotBlank() }?.let { url ->
                        app.player.play(url); playerState = playerState.load(item)
                    }
                },
                onLongPressHome = { homeMenu = true },
                onDoubleTapHome = ::onEmptyDoubleTap,
                onAddWidget = { page -> widgetController.openPicker(page) },
                onMove = { page, id, x, y -> widgetController.relocate(page, page, id, x, y) },
                onSpan = { page, binding -> widgetController.applySpan(page, binding) },
                onRemove = { page, id -> widgetController.remove(page, id) },
                onGridPositioned = { gridCoords = it },
                onLaunchIcon = { AppCatalog.launch(context, LaunchableApp(it.packageName, it.activityName, it.label)) },
                onRemoveIcon = { page, id ->
                    scope.launch { app.desktopStore.save(app.desktopStore.layout.first().without(page, id)) }
                },
                onMoveIcon = { page, id, x, y ->
                    scope.launch {
                        DesktopPin.relocate(app.desktopStore.layout.first(), widgets, page, id, x, y)?.let {
                            app.desktopStore.save(it.layout)
                        }
                    }
                },
                onAddFeed = { url -> app.feeds.addFromLink(url) },
                onEmptySwipe = ::fireGesture,
                modifier = Modifier.weight(1f),
            )
            if (qsb == QsbPlacement.BOTTOM) QsbBar(placement = qsb, onOpen = { searchOpen = true })
            PageIndicator(count = pageCount, current = pagerState.currentPage)
            MiniPlayerBar(
                state = playerState,
                onToggle = { app.player.toggle(); playerState = playerState.toggle() },
                onStop = { app.player.stop(); playerState = playerState.stop() },
                onSkipBack = { app.player.skipBy(-10_000L) }, onSkipForward = { app.player.skipBy(30_000L) },
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
                onSwipeUp = { fireGesture(GestureSlot.SWIPE_UP) },
            )
        }
        HomeDrawerHost(
            open = drawer.open, homeDock = homeDock, pack = pack, unread = unread, showDots = showDots,
            assignSlot = assignSlot, pinDesktop = pinDesktop, workspace = workspace,
            pagerState = pagerState, scope = scope, widgets = widgets, gridCoords = gridCoords,
            rootCoords = rootCoords, pageWidthPx = pageWidthPx, pageCount = pageCount, lastEdgeMs = lastEdgeMs,
            onDragging = { dragging = it }, onEdgeMs = { lastEdgeMs = it },
            onClose = { drawer = drawer.closed() },
            onOpenSearch = { drawer = drawer.closed(); searchOpen = true },
            onAbout = { drawer = drawer.closed(); onOpenAbout() },
            onAssignConsumed = { assignSlot = null }, onPinConsumed = { pinDesktop = false },
        )
        HomeWidgetDrag(
            picker = picker, dragChoice = dragChoice, dragWindow = dragWindow,
            rootCoords = rootCoords, gridCoords = gridCoords, widgets = widgets, model = workspace,
            pagerState = pagerState, pageCount = pageCount, pageWidthPx = pageWidthPx, lastEdgeMs = lastEdgeMs,
            scope = scope, widgetController = widgetController,
            onDragStart = { choice, window -> dragging = true; dragChoice = choice; dragWindow = window },
            onDragWindow = { dragWindow = it }, onEdgeMs = { lastEdgeMs = it },
            setDragging = { dragging = it }, setDragChoice = { dragChoice = it },
        )
        HomeShellMenus(
            homeMenu = homeMenu, searchOpen = searchOpen, homeDock = homeDock, items = items,
            feeds = feeds, pack = pack, workspace = workspace, pagerState = pagerState,
            widgetController = widgetController, playerState = playerState,
            onHomeMenu = { homeMenu = it },
            onPinDesktop = { pinDesktop = true; drawer = drawer.opened() },
            onOpenSettings = onOpenSettings, onSearch = { searchOpen = it },
            onPlayer = { playerState = it },
        )
    }
}
