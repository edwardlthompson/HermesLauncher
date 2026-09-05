package org.hermeslauncher.app.l3

import com.android.launcher3.Launcher
import com.android.launcher3.LauncherSettings.Favorites
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.model.data.WorkspaceItemInfo
import org.hermeslauncher.app.icons.AppCatalog
import org.hermeslauncher.app.icons.DockLayout
import org.hermeslauncher.app.icons.DockMode
import org.hermeslauncher.app.icons.UsageStatsQuery
import org.hermeslauncher.app.oem.LivePermissions

object L3Dock {
    private var lastUsageKey: String = ""

    fun forgetUsage() {
        lastUsageKey = ""
    }

    fun hotseatIcons(pageCount: Int): Int = (4 + pageCount).coerceIn(4, 8)

    fun apply(launcher: Launcher, layout: DockLayout) {
        val count = hotseatIcons(layout.pageCount)
        val profile = launcher.deviceProfile
        if (profile.numShownHotseatIcons != count) {
            profile.numShownHotseatIcons = count
            profile.recalculateHotseatWidthAndBorderSpace()
            launcher.hotseat.setGridSize(count, 1)
            launcher.hotseat.requestLayout()
        }
        val usage = layout.mode == DockMode.USAGE && LivePermissions.usageGranted(launcher)
        val key = "${layout.mode}:$count:$usage"
        if (!usage) {
            lastUsageKey = key
            return
        }
        if (key == lastUsageKey) {
            return
        }
        if (pinUsage(launcher, count)) {
            lastUsageKey = key
        }
    }

    private fun pinUsage(launcher: Launcher, count: Int): Boolean {
        val ranked = UsageStatsQuery.rank(launcher, AppCatalog.launchables(launcher.packageManager), count)
        if (ranked.isEmpty()) {
            return false
        }
        val store = launcher.appsView.appsStore.apps.toList()
        if (store.isEmpty()) {
            return false
        }
        ranked.forEachIndexed { rank, app ->
            val info = store.firstOrNull { match ->
                match.componentName.packageName == app.packageName
            } ?: return@forEachIndexed
            bindHotseat(launcher, info, rank)
        }
        return true
    }

    fun pinApp(launcher: Launcher, info: AppInfo, rank: Int) {
        bindHotseat(launcher, info, rank)
    }

    private fun bindHotseat(launcher: Launcher, info: AppInfo, rank: Int) {
        val existing = hotseatAt(launcher, rank)
        if (existing != null) {
            launcher.modelWriter.deleteItemFromDatabase(existing, "hermes-usage")
        }
        val item = WorkspaceItemInfo(info)
        launcher.modelWriter.addItemToDatabase(item, Favorites.CONTAINER_HOTSEAT, rank, rank, 0)
    }

    private fun hotseatAt(launcher: Launcher, rank: Int): ItemInfo? {
        val container = launcher.hotseat.shortcutsAndWidgets ?: return null
        for (i in 0 until container.childCount) {
            val tag = container.getChildAt(i).tag as? ItemInfo ?: continue
            if (tag.screenId == rank || tag.cellX == rank) {
                return tag
            }
        }
        return null
    }
}
