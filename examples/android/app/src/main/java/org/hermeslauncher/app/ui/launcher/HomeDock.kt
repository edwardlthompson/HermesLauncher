package org.hermeslauncher.app.ui.launcher

import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hermeslauncher.app.icons.AllAppsIndex
import org.hermeslauncher.app.icons.AppCatalog
import org.hermeslauncher.app.icons.DockLayout
import org.hermeslauncher.app.icons.DockMode
import org.hermeslauncher.app.icons.LaunchableApp
import org.hermeslauncher.app.icons.UsageRanker
import org.hermeslauncher.app.icons.UsageRow
import org.hermeslauncher.app.icons.UsageStatsQuery
import org.hermeslauncher.app.oem.LivePermissions

data class HomeDockSnapshot(
    val apps: List<LaunchableApp>,
    val dock: DockLayout,
    val predicted: List<LaunchableApp>,
    val usage: List<UsageRow> = emptyList(),
)

private data class DockLoad(
    val apps: List<LaunchableApp>,
    val ranked: List<LaunchableApp>,
    val predicted: List<LaunchableApp>,
    val usage: List<UsageRow>,
)

@Composable
fun rememberHomeDock(pm: PackageManager, stored: DockLayout, refresh: Int = 0): HomeDockSnapshot {
    val context = LocalContext.current
    var apps by remember { mutableStateOf(emptyList<LaunchableApp>()) }
    var usageApps by remember { mutableStateOf(emptyList<LaunchableApp>()) }
    var predicted by remember { mutableStateOf(emptyList<LaunchableApp>()) }
    var usageRows by remember { mutableStateOf(emptyList<UsageRow>()) }
    val usageOk = LivePermissions.usageGranted(context)
    LaunchedEffect(refresh, stored.mode, stored.slotCount, usageOk) {
        val loaded = withContext(Dispatchers.IO) {
            val launchables = AppCatalog.launchables(pm)
            val rows = if (usageOk) UsageStatsQuery.rows(context) else emptyList()
            val ranked = if (usageOk && stored.mode == DockMode.USAGE) {
                UsageRanker.rank(launchables, rows, stored.slotCount)
            } else {
                emptyList()
            }
            val top = if (usageOk) {
                UsageRanker.rank(launchables, rows, AllAppsIndex.COLUMNS)
            } else {
                emptyList()
            }
            DockLoad(launchables, ranked, top, rows)
        }
        apps = loaded.apps
        usageApps = loaded.ranked
        predicted = loaded.predicted
        usageRows = loaded.usage
    }
    val dock = remember(stored, usageApps, usageOk, apps) {
        AppCatalog.displayed(stored, usageApps, usageOk, apps)
    }
    return HomeDockSnapshot(apps, dock, predicted, usageRows)
}
