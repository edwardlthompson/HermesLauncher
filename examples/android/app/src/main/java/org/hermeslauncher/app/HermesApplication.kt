package org.hermeslauncher.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableSharedFlow
import org.hermeslauncher.app.feeds.FeedRepository
import org.hermeslauncher.app.feeds.FeedStore
import org.hermeslauncher.app.feeds.HermesPlayer
import org.hermeslauncher.app.icons.DockStore
import org.hermeslauncher.app.icons.IconBitmapLoader
import org.hermeslauncher.app.icons.IconPackStore
import org.hermeslauncher.app.launcher.HomePrefs
import org.hermeslauncher.app.vault.InboxPrefs
import org.hermeslauncher.app.vault.VaultRepository
import org.hermeslauncher.app.widgets.HermesAppWidgetHost
import org.hermeslauncher.app.widgets.WidgetHostStore

class HermesApplication : Application() {
    val vaultScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    lateinit var inboxPrefs: InboxPrefs
        private set
    lateinit var homePrefs: HomePrefs
        private set
    lateinit var vault: VaultRepository
        private set
    val iconLoader = IconBitmapLoader<Bitmap>()
    val homePulse = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    lateinit var widgetHost: HermesAppWidgetHost
        private set
    lateinit var widgetStore: WidgetHostStore
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
        vault = VaultRepository(this, inboxPrefs)
        vaultScope.launch {
            delay(2_000)
            vault.prune(force = true)
        }
        widgetHost = HermesAppWidgetHost(this)
        widgetStore = WidgetHostStore(this)
        dockStore = DockStore(this)
        iconPackStore = IconPackStore(this)
        feedStore = FeedStore(this)
        feeds = FeedRepository(this, feedStore)
    }
}
