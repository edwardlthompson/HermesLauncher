package org.hermeslauncher.app.workspace

import org.hermeslauncher.app.icons.LaunchableApp
import org.hermeslauncher.app.widgets.IconOccupancy
import org.hermeslauncher.app.widgets.WidgetHostState

data class PinResult(
    val layout: DesktopLayout,
    val pageIndex: Int,
)

object DesktopPin {
    fun place(
        layout: DesktopLayout,
        widgets: WidgetHostState,
        pageIndex: Int,
        app: LaunchableApp,
    ): PinResult? {
        val ready = sanitize(app) ?: return null
        val pages = linkedSetOf(pageIndex.coerceAtLeast(1))
        widgets.pages.forEach { page -> pages.add(page.pageIndex) }
        val id = nextId(layout)
        for (page in pages) {
            if (page < 1) continue
            val cell = IconOccupancy.firstFit(
                widgets.page(page).bindings,
                layout.page(page).map { it.cellX to it.cellY },
                widgets.grid,
            ) ?: continue
            return PinResult(layout.withShortcut(page, shortcut(id, ready, cell)), page)
        }
        return null
    }

    fun drop(
        layout: DesktopLayout,
        widgets: WidgetHostState,
        pageIndex: Int,
        app: LaunchableApp,
        cellX: Int,
        cellY: Int,
    ): PinResult? {
        val ready = sanitize(app) ?: return null
        if (pageIndex < 1) {
            return place(layout, widgets, 1, ready)
        }
        val icons = layout.page(pageIndex).map { it.cellX to it.cellY }
        val bindings = widgets.page(pageIndex).bindings
        val spec = widgets.grid
        val cell = if (IconOccupancy.canPlace(bindings, icons, cellX, cellY, spec)) {
            cellX to cellY
        } else {
            IconOccupancy.firstFit(bindings, icons, spec)
        }
        if (cell != null) {
            return PinResult(
                layout.withShortcut(pageIndex, shortcut(nextId(layout), ready, cell)),
                pageIndex,
            )
        }
        return place(layout, widgets, pageIndex, ready)
    }

    fun relocate(
        layout: DesktopLayout,
        widgets: WidgetHostState,
        pageIndex: Int,
        id: Long,
        cellX: Int,
        cellY: Int,
    ): PinResult? {
        val item = layout.page(pageIndex).firstOrNull { it.id == id } ?: return null
        if (pageIndex < 1) {
            return PinResult(layout, pageIndex)
        }
        val others = layout.page(pageIndex).filterNot { it.id == id }.map { it.cellX to it.cellY }
        val bindings = widgets.page(pageIndex).bindings
        val spec = widgets.grid
        val cell = if (IconOccupancy.canPlace(bindings, others, cellX, cellY, spec)) {
            cellX to cellY
        } else {
            IconOccupancy.firstFit(bindings, others, spec) ?: (item.cellX to item.cellY)
        }
        return PinResult(
            layout.withShortcut(pageIndex, item.copy(cellX = cell.first, cellY = cell.second)),
            pageIndex,
        )
    }

    private fun sanitize(app: LaunchableApp): LaunchableApp? {
        if (app.packageName.isBlank() || app.activityName.isBlank()) {
            return null
        }
        return app
    }

    private fun nextId(layout: DesktopLayout): Long {
        return (layout.byPage.values.flatten().maxOfOrNull { it.id } ?: 0L) + 1L
    }

    private fun shortcut(id: Long, app: LaunchableApp, cell: Pair<Int, Int>): DesktopItem.Shortcut {
        return DesktopItem.Shortcut(id, app.packageName, app.activityName, app.label, cell.first, cell.second)
    }
}
