package org.hermeslauncher.app.l3

import com.android.launcher3.Launcher
import com.android.launcher3.LauncherSettings.Favorites
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.model.data.WorkspaceItemInfo
import org.hermeslauncher.app.workspace.DesktopItem
import org.hermeslauncher.app.workspace.HermesScreens
import org.hermeslauncher.app.workspace.NovaImport

object L3NovaImport {
    fun apply(launcher: Launcher, imported: NovaImport): Int {
        val store = launcher.appsView?.appsStore?.apps?.toList().orEmpty()
        if (store.isEmpty()) {
            return -1
        }
        var placed = 0
        imported.desktop.byPage.forEach { (page, items) ->
            val screenId = (page - 1).coerceAtLeast(0)
            if (HermesScreens.isReserved(screenId)) {
                return@forEach
            }
            ensureScreen(launcher, screenId)
            items.forEach { shortcut ->
                if (placeDesktop(launcher, store, screenId, shortcut)) {
                    placed += 1
                }
            }
        }
        imported.dock.assigned.forEach { (rank, app) ->
            val info = match(store, app.packageName, app.activityName) ?: return@forEach
            L3Dock.pinApp(launcher, info, rank)
            placed += 1
        }
        launcher.model.forceReload()
        return placed
    }

    private fun placeDesktop(
        launcher: Launcher,
        store: List<AppInfo>,
        screenId: Int,
        shortcut: DesktopItem.Shortcut,
    ): Boolean {
        if (occupied(launcher, screenId, shortcut.cellX, shortcut.cellY)) {
            return false
        }
        val info = match(store, shortcut.packageName, shortcut.activityName) ?: return false
        val item = WorkspaceItemInfo(info)
        launcher.modelWriter.addItemToDatabase(
            item,
            Favorites.CONTAINER_DESKTOP,
            screenId,
            shortcut.cellX,
            shortcut.cellY,
        )
        return true
    }

    private fun match(store: List<AppInfo>, pkg: String, act: String): AppInfo? {
        return store.firstOrNull { info ->
            info.componentName.packageName == pkg && info.componentName.className == act
        } ?: store.firstOrNull { info -> info.componentName.packageName == pkg }
    }

    private fun ensureScreen(launcher: Launcher, screenId: Int) {
        if (launcher.workspace.getScreenWithId(screenId) != null) {
            return
        }
        launcher.workspace.insertNewWorkspaceScreen(screenId, launcher.workspace.childCount)
    }

    private fun occupied(launcher: Launcher, screenId: Int, cellX: Int, cellY: Int): Boolean {
        val screen = launcher.workspace.getScreenWithId(screenId) ?: return false
        val container = screen.shortcutsAndWidgets ?: return false
        for (i in 0 until container.childCount) {
            val tag = container.getChildAt(i).tag as? ItemInfo ?: continue
            if (tag.cellX == cellX && tag.cellY == cellY) {
                return true
            }
        }
        return false
    }
}
