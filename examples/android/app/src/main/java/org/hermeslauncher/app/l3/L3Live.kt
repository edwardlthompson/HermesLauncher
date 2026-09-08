package org.hermeslauncher.app.l3

import android.content.res.Configuration
import com.android.launcher3.Launcher
import com.android.launcher3.icons.IconCache
import com.android.launcher3.util.Executors
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.HermesLauncherActivity
import org.hermeslauncher.app.icons.IconPackFilter
import org.hermeslauncher.app.icons.IconPlate
import org.hermeslauncher.app.ui.theme.LookPrefs
import org.hermeslauncher.app.ui.theme.ThemePreferences
import org.hermeslauncher.app.ui.theme.ThemeResolve
import java.lang.ref.WeakReference

object L3Live {
    private var job: Job? = null
    private var lastGrid: String = ""
    private var lastHidden: Set<String> = emptySet()
    private var lastStamp: String = "\u0000"

    fun attach(launcher: HermesLauncherActivity) {
        job?.cancel()
        lastStamp = "\u0000"
        val app = launcher.application as HermesApplication
        val look = LookPrefs(launcher)
        val theme = ThemePreferences(launcher)
        val ref = WeakReference(launcher)
        job = app.vaultScope.launch {
            launch { app.gesturePrefs.sensitivity.collect { L3Caches.sensitivity = it; paint(ref) } }
            launch { app.gesturePrefs.map.collect { L3Caches.gestureMap = it } }
            launch { app.homePrefs.doubleTap.collect { L3Caches.doubleTap = it } }
            launch { app.homePrefs.showLabels.collect { L3Caches.showLabels = it; paint(ref) } }
            launch { app.homePrefs.showDots.collect { L3Caches.showDots = it; paint(ref) } }
            launch { app.pagedPrefs.labs.collect { L3Caches.labs = it; paint(ref) } }
            launch { app.pagedPrefs.scrollMode.collect { L3Caches.scrollMode = it; paint(ref) } }
            launch { app.pagedPrefs.snapSpeed.collect { L3Caches.snapSpeed = it; paint(ref) } }
            launch { app.drawerPrefs.snapshot.collect { L3Caches.drawer = it; paint(ref) } }
            launch { app.dockStore.layout.collect { L3Caches.dock = it; paint(ref) } }
            launch { app.folderPrefs.snapshot.collect { L3Caches.folder = it; paint(ref) } }
            launch { app.searchPrefs.appRowCap.collect { L3Caches.appRowCap = it; paint(ref) } }
            launch { app.widgetStore.state.collect { L3Caches.grid = it.grid.clamped(); paint(ref) } }
            launch { app.iconPackStore.pack.collect { L3Caches.iconPack = it; paint(ref) } }
            launch { look.iconShape.collect { L3Caches.iconShape = it; paint(ref) } }
            launch { look.nightSchedule.collect { L3Caches.night = it; paint(ref) } }
            launch { theme.themeMode.collect { L3Caches.themeMode = it; paint(ref) } }
            launch { look.badgeStyle.collect { L3Caches.badgeStyle = it; paint(ref) } }
            launch { look.badgeColorArgb.collect { L3Caches.badgeColor = it; paint(ref) } }
            launch { look.labelShadow.collect { L3Caches.labelShadow = it; paint(ref) } }
            launch { look.wallpaperPalette.collect { L3Caches.wallpaperPalette = it; paint(ref) } }
        }
    }

    fun detach() {
        job?.cancel()
        job = null
    }

    private fun paint(ref: WeakReference<HermesLauncherActivity>) {
        val launcher = ref.get() ?: return
        Executors.MAIN_EXECUTOR.execute { apply(launcher) }
    }

    fun apply(launcher: Launcher) {
        L3Chrome.apply(
            launcher,
            L3Caches.showLabels,
            L3Caches.showDots,
            L3Caches.labs,
            L3Caches.scrollMode,
            L3Caches.snapSpeed,
            L3Caches.labelShadow,
        )
        L3Drawer.apply(launcher, L3Caches.drawer, L3Caches.appRowCap, L3Caches.folder)
        if (L3Caches.drawer.hidden != lastHidden) {
            lastHidden = L3Caches.drawer.hidden
            launcher.model.forceReload()
        }
        val nightNow = launcher.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        IconPlate.apply(
            ThemeResolve.isDark(
                L3Caches.themeMode,
                L3Caches.night,
                nightNow == Configuration.UI_MODE_NIGHT_YES,
                L3NightMode.nowMinute(),
            ),
        )
        L3Look.applyShape(launcher, L3Caches.iconShape)
        L3Look.applyPack(launcher, L3Caches.iconPack)
        val stamp = IconCache.sPackStamp
        if (stamp != lastStamp) {
            lastStamp = stamp
            IconPackFilter.forget()
            L3Look.reloadIcons(launcher)
        }
        L3Look.applyThemedIcons(launcher, L3Caches.wallpaperPalette)
        L3NightMode.apply(
            L3Caches.themeMode,
            L3Caches.night,
            launcher.getSystemService(android.app.UiModeManager::class.java),
        )
        L3Dock.apply(launcher, L3Caches.dock)
        val gridKey = L3Caches.grid.encoded()
        if (gridKey != lastGrid) {
            lastGrid = gridKey
            L3Look.applyGrid(launcher, L3Caches.grid)
        }
    }
}
