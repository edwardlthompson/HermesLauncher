package org.hermeslauncher.app.widgets

object IconOccupancy {
    fun slot(cellX: Int, cellY: Int): WidgetBinding {
        return WidgetBinding(
            appWidgetId = Int.MIN_VALUE,
            cellsW = 1,
            cellsH = 1,
            cellX = cellX,
            cellY = cellY,
        )
    }

    fun canPlace(
        existing: List<WidgetBinding>,
        cellX: Int,
        cellY: Int,
        spec: WidgetGridSpec = WidgetGridSpec.DEFAULT,
    ): Boolean {
        return WidgetGrid.canPlace(existing, slot(cellX, cellY), spec)
    }

    fun canPlace(
        widgets: List<WidgetBinding>,
        icons: List<Pair<Int, Int>>,
        cellX: Int,
        cellY: Int,
        spec: WidgetGridSpec = WidgetGridSpec.DEFAULT,
    ): Boolean {
        return WidgetGrid.canPlace(withIcons(widgets, icons), slot(cellX, cellY), spec)
    }

    fun firstFit(
        widgets: List<WidgetBinding>,
        icons: List<Pair<Int, Int>>,
        spec: WidgetGridSpec = WidgetGridSpec.DEFAULT,
    ): Pair<Int, Int>? {
        return WidgetGrid.firstFit(withIcons(widgets, icons), 1, 1, spec)
    }

    private fun withIcons(
        widgets: List<WidgetBinding>,
        icons: List<Pair<Int, Int>>,
    ): List<WidgetBinding> {
        val extra = icons.mapIndexed { index, cell ->
            slot(cell.first, cell.second).copy(appWidgetId = Int.MIN_VALUE + index + 1)
        }
        return widgets + extra
    }
}
