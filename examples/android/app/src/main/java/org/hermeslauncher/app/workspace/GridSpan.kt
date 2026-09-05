package org.hermeslauncher.app.workspace

import org.hermeslauncher.app.widgets.WidgetGridSpec

object GridSpan {
    fun map(widgetCells: Int, iconCount: Int, widgetCount: Int): Int {
        if (widgetCells < 1 || iconCount < 1 || widgetCount < 1) {
            return 0
        }
        return (widgetCells * iconCount + widgetCount - 1) / widgetCount
    }

    fun mapSize(
        cellsW: Int,
        cellsH: Int,
        icons: WidgetGridSpec,
        widgets: WidgetGridSpec,
    ): Pair<Int, Int> {
        val icon = icons.clamped()
        val widget = widgets.clamped()
        return map(cellsW, icon.columns, widget.columns) to map(cellsH, icon.rows, widget.rows)
    }
}
