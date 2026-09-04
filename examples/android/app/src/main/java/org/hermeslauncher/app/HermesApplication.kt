package org.hermeslauncher.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.hermeslauncher.app.feeds.FeedRepository
import org.hermeslauncher.app.feeds.FeedStore
import org.hermeslauncher.app.feeds.HermesPlayer
import org.hermeslauncher.app.icons.DockStore
import org.hermeslauncher.app.icons.DrawerPrefs
import org.hermeslauncher.app.icons.IconBitmapLoader
import org.hermeslauncher.app.icons.IconPackStore
import org.hermeslauncher.app.launcher.HomePrefs
import org.hermeslauncher.app.launcher.PagedPrefs
import org.hermeslauncher.app.launcher.SearchPrefs
import org.hermeslauncher.app.vault.InboxPrefs
import org.hermeslauncher.app.vault.VaultRepository
import org.hermeslauncher.app.widgets.HermesAppWidgetHost
import org.hermeslauncher.app.widgets.WidgetHostStore
import org.hermeslauncher.app.workspace.DesktopStore
import org.hermeslauncher.app.workspace.FolderPrefs

class HermesApplication : Application() {
    val vaultScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    lateinit var inboxPrefs: InboxPrefs
        private set
    lateinit var homePrefs: HomePrefs
        private set
    lateinit var pagedPrefs: PagedPrefs
        private set
    lateinit var drawerPrefs: DrawerPrefs
        private set
    lateinit var searchPrefs: SearchPrefs
        private set
    lateinit var vault: VaultRepository
        private set
    val iconLoader = IconBitmapLoader<Bitmap>()
    val homePulse = MutableSharedFlow<Unit>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    lateinit var widgetHost: HermesAppWidgetHost
        private set
    lateinit var widgetStore: WidgetHostStore
        private set
    lateinit var desktopStore: DesktopStore
        private set
    lateinit var folderPrefs: FolderPrefs
        private set
    lateinit var dockStore: DockStore
        private set
    lateinit var iconPackStore: IconPackStore
        private set
    lateinit var feedStore: FeedStore
        private set
    lateinit var feeds: FeedRepository
        private set
    val player: HermesPlayer by lazy { HermesPlayer(this) }

    override fun onCreate() {
        super.onCreate()
        inboxPrefs = InboxPrefs(this)
        homePrefs = HomePrefs(this)
        pagedPrefs = PagedPrefs(this)
        drawerPrefs = DrawerPrefs(this)
        searchPrefs = SearchPrefs(this)
        vault = VaultRepository(this, inboxPrefs)
        vaultScope.launch {
            delay(2_000)
            vault.prune(force = true)
        }
        widgetHost = HermesAppWidgetHost(this)
        runCatching { widgetHost.startListening() }
        widgetStore = WidgetHostStore(this)
        desktopStore = DesktopStore(this)
        folderPrefs = FolderPrefs(this)
        dockStore = DockStore(this)
        iconPackStore = IconPackStore(this)
        feedStore = FeedStore(this)
        feeds = FeedRepository(this, feedStore)
    }
}
