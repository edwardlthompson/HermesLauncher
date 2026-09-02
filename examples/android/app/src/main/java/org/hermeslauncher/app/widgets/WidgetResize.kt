package org.hermeslauncher.app.widgets

enum class WidgetResizeEdge {
    LEFT,
    TOP,
    RIGHT,
    BOTTOM,
}

object WidgetResize {
    fun spansForDelta(
        binding: WidgetBinding,
        edge: WidgetResizeEdge,
        dxPx: Float,
        dyPx: Float,
        cellW: Float,
        cellH: Float,
        spec: WidgetGridSpec = WidgetGridSpec.DEFAULT,
    ): WidgetBinding {
        if (cellW <= 0f || cellH <= 0f) return binding
        val grid = spec.clamped()
        val dx = kotlin.math.round(dxPx / cellW).toInt()
        val dy = kotlin.math.round(dyPx / cellH).toInt()
        if (dx == 0 && dy == 0) return binding
        val min = WidgetBinding.MIN_CELLS
        val right = binding.cellX + binding.cellsW
        val bottom = binding.cellY + binding.cellsH
        return when (edge) {
            WidgetResizeEdge.LEFT -> {
                val width = (right - (binding.cellX + dx)).coerceIn(min, right.coerceAtMost(grid.columns))
                val x = (right - width).coerceIn(0, grid.columns - width)
                binding.copy(cellX = x, cellsW = width)
            }
            WidgetResizeEdge.RIGHT -> binding.copy(
                cellsW = (binding.cellsW + dx).coerceIn(min, grid.columns - binding.cellX),
            )
            WidgetResizeEdge.TOP -> {
                val height = (bottom - (binding.cellY + dy)).coerceIn(min, bottom.coerceAtMost(grid.rows))
                val y = (bottom - height).coerceIn(0, grid.rows - height)
                binding.copy(cellY = y, cellsH = height)
            }
            WidgetResizeEdge.BOTTOM -> binding.copy(
                cellsH = (binding.cellsH + dy).coerceIn(min, grid.rows - binding.cellY),
            )
        }
    }

    fun handleVisible(
        binding: WidgetBinding,
        edge: WidgetResizeEdge,
        spec: WidgetGridSpec = WidgetGridSpec.DEFAULT,
    ): Boolean {
        val grid = spec.clamped()
        val min = WidgetBinding.MIN_CELLS
        return when (edge) {
            WidgetResizeEdge.LEFT -> binding.cellX > 0 || binding.cellsW > min
            WidgetResizeEdge.RIGHT ->
                binding.cellX + binding.cellsW < grid.columns || binding.cellsW > min
            WidgetResizeEdge.TOP -> binding.cellY > 0 || binding.cellsH > min
            WidgetResizeEdge.BOTTOM ->
                binding.cellY + binding.cellsH < grid.rows || binding.cellsH > min
        }
    }
}
