package org.hermeslauncher.app.workspace

import org.hermeslauncher.app.icons.DockLayout
import org.hermeslauncher.app.icons.DockMode
import org.hermeslauncher.app.icons.LaunchableApp

data class NovaFavorite(
    val title: String,
    val intent: String,
    val container: Int,
    val screen: Int,
    val cellX: Int,
    val cellY: Int,
    val itemType: Int,
)

object NovaLayout {
    const val CONTAINER_DESKTOP: Int = -100
    const val CONTAINER_HOTSEAT: Int = -101
    const val TYPE_APP: Int = 0
    const val TYPE_SHORTCUT: Int = 1

    fun componentOf(intent: String): Pair<String, String>? {
        val marker = "component="
        val start = intent.indexOf(marker)
        if (start < 0) {
            return null
        }
        val rest = intent.substring(start + marker.length)
        val end = rest.indexOfFirst { ch -> ch == ';' || ch == '#' }
        val raw = (if (end < 0) rest else rest.substring(0, end)).trim()
        val slash = raw.indexOf('/')
        if (slash <= 0 || slash == raw.lastIndex) {
            return null
        }
        val pkg = raw.substring(0, slash)
        var act = raw.substring(slash + 1)
        if (act.startsWith(".")) {
            act = pkg + act
        }
        if (pkg.isBlank() || act.isBlank()) {
            return null
        }
        return pkg to act
    }

    fun desktop(rows: List<NovaFavorite>): DesktopLayout {
        val byPage = linkedMapOf<Int, MutableList<DesktopItem.Shortcut>>()
        var id = 1L
        rows.filter { row ->
            (row.itemType == TYPE_APP || row.itemType == TYPE_SHORTCUT) &&
                row.container == CONTAINER_DESKTOP
        }.sortedWith(compareBy({ it.screen }, { it.cellY }, { it.cellX })).forEach { row ->
            val comp = componentOf(row.intent) ?: return@forEach
            val page = (row.screen + 1).coerceAtLeast(1)
            val item = DesktopItem.Shortcut(
                id = id,
                packageName = comp.first,
                activityName = comp.second,
                label = row.title,
                cellX = row.cellX.coerceAtLeast(0),
                cellY = row.cellY.coerceAtLeast(0),
            )
            byPage.getOrPut(page) { mutableListOf() }.add(item)
            id += 1
        }
        return DesktopLayout(byPage.mapValues { it.value.toList() })
    }

    fun dock(rows: List<NovaFavorite>): DockLayout {
        val apps = rows.filter { row ->
            (row.itemType == TYPE_APP || row.itemType == TYPE_SHORTCUT) &&
                row.container == CONTAINER_HOTSEAT
        }.sortedBy { it.screen.coerceAtLeast(it.cellX) }.mapNotNull { row ->
            val comp = componentOf(row.intent) ?: return@mapNotNull null
            LaunchableApp(comp.first, comp.second, row.title.ifBlank { comp.first })
        }
        if (apps.isEmpty()) {
            return DockLayout()
        }
        return DockLayout(
            slotCount = apps.size.coerceIn(DockLayout.MIN_SLOTS, DockLayout.MAX_SLOTS),
            assigned = apps.mapIndexed { index, app -> index to app }.toMap(),
            mode = DockMode.CUSTOM,
        )
    }
}
