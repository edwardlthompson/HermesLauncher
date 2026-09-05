package org.hermeslauncher.app.widgets

/** Pure drop gating so picker collapse cannot hide a miss. */
object DropPolicy {
    enum class Miss {
        WRONG_PAGE,
        NO_GRID,
        OFF_GRID,
    }

    fun miss(page: Int, hasGrid: Boolean, hit: Boolean): Miss? = when {
        page <= 0 -> Miss.WRONG_PAGE
        !hasGrid -> Miss.NO_GRID
        !hit -> Miss.OFF_GRID
        else -> null
    }

    fun cellTarget(
        page: Int,
        localX: Float,
        localY: Float,
        width: Float,
        height: Float,
        spec: WidgetGridSpec,
    ): Triple<Int, Int, Int>? {
        if (page <= 0) return null
        val cell = WidgetGrid.cellAt(localX, localY, width, height, spec) ?: return null
        return Triple(page, cell.first, cell.second)
    }
}
