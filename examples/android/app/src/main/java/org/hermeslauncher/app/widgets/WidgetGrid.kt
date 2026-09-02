package org.hermeslauncher.app.widgets

object WidgetGrid {
    const val MAX_WIDGET_PAGES: Int = 20

    fun canPlace(
        existing: List<WidgetBinding>,
        candidate: WidgetBinding,
        spec: WidgetGridSpec = WidgetGridSpec.DEFAULT,
    ): Boolean {
        val grid = spec.clamped()
        if (candidate.cellX < 0 || candidate.cellY < 0) return false
        if (candidate.cellsW < 1 || candidate.cellsH < 1) return false
        if (candidate.cellX + candidate.cellsW > grid.columns) return false
        if (candidate.cellY + candidate.cellsH > grid.rows) return false
        val taken = existing.filterNot { it.appWidgetId == candidate.appWidgetId }
            .flatMap { cellsOf(it) }.toSet()
        return cellsOf(candidate).none { it in taken }
    }

    fun firstFit(
        existing: List<WidgetBinding>,
        cellsW: Int,
        cellsH: Int,
        spec: WidgetGridSpec = WidgetGridSpec.DEFAULT,
    ): Pair<Int, Int>? {
        val grid = spec.clamped()
        if (cellsW < 1 || cellsH < 1 || cellsW > grid.columns || cellsH > grid.rows) {
            return null
        }
        for (y in 0..(grid.rows - cellsH)) {
            for (x in 0..(grid.columns - cellsW)) {
                val probe = WidgetBinding(Int.MIN_VALUE, cellsW = cellsW, cellsH = cellsH, cellX = x, cellY = y)
                if (canPlace(existing, probe, grid)) return x to y
            }
        }
        return null
    }

    fun snapOrigin(
        cellX: Int,
        cellY: Int,
        cellsW: Int,
        cellsH: Int,
        spec: WidgetGridSpec = WidgetGridSpec.DEFAULT,
    ): Pair<Int, Int> {
        val grid = spec.clamped()
        val maxX = (grid.columns - cellsW).coerceAtLeast(0)
        val maxY = (grid.rows - cellsH).coerceAtLeast(0)
        return cellX.coerceIn(0, maxX) to cellY.coerceIn(0, maxY)
    }

    fun dropCandidate(
        provider: String,
        cellX: Int,
        cellY: Int,
        spec: WidgetGridSpec = WidgetGridSpec.DEFAULT,
    ): WidgetBinding {
        val w = WidgetBinding.PLACE_CELLS
        val h = WidgetBinding.PLACE_CELLS_H
        val origin = snapOrigin(cellX, cellY, w, h, spec)
        return WidgetBinding(Int.MAX_VALUE, provider, w, h, origin.first, origin.second)
    }

    fun stackedFromV2(bindings: List<WidgetBinding>): List<WidgetBinding> {
        val placed = ArrayList<WidgetBinding>(bindings.size)
        for (binding in bindings) {
            val origin = firstFit(placed, binding.cellsW, binding.cellsH) ?: continue
            placed += binding.copy(cellX = origin.first, cellY = origin.second)
        }
        return placed
    }

    fun fitToSpec(bindings: List<WidgetBinding>, spec: WidgetGridSpec): List<WidgetBinding> {
        val grid = spec.clamped()
        val placed = ArrayList<WidgetBinding>(bindings.size)
        for (binding in bindings) {
            val fitted = fitOne(placed, binding, grid) ?: continue
            placed += fitted
        }
        return placed
    }

    fun applyGrid(state: WidgetHostState, spec: WidgetGridSpec): WidgetHostState {
        val grid = spec.clamped()
        val pages = state.pages.map { page -> page.copy(bindings = fitToSpec(page.bindings, grid)) }
        return withTrailingEmpty(state.copy(grid = grid, pages = pages))
    }

    fun cellAt(
        xPx: Float,
        yPx: Float,
        widthPx: Float,
        heightPx: Float,
        spec: WidgetGridSpec = WidgetGridSpec.DEFAULT,
    ): Pair<Int, Int>? {
        val grid = spec.clamped()
        if (widthPx <= 0f || heightPx <= 0f || xPx < 0f || yPx < 0f) return null
        val x = (xPx / (widthPx / grid.columns)).toInt()
        val y = (yPx / (heightPx / grid.rows)).toInt()
        if (x !in 0 until grid.columns || y !in 0 until grid.rows) return null
        return x to y
    }

    fun withTrailingEmpty(state: WidgetHostState): WidgetHostState {
        val occupiedMax = state.pages.filter { it.bindings.isNotEmpty() }.maxOfOrNull { it.pageIndex } ?: 0
        val target = when {
            occupiedMax <= 0 -> 1
            occupiedMax >= MAX_WIDGET_PAGES -> MAX_WIDGET_PAGES
            else -> occupiedMax + 1
        }
        val byIndex = state.pages.associateBy { it.pageIndex }
        val pages = (1..target).map { index -> byIndex[index] ?: WidgetPageState(index) }
        return state.copy(pages = pages)
    }

    private fun fitOne(
        placed: List<WidgetBinding>,
        binding: WidgetBinding,
        spec: WidgetGridSpec,
    ): WidgetBinding? {
        val w = binding.cellsW.coerceIn(WidgetBinding.MIN_CELLS, spec.columns)
        val h = binding.cellsH.coerceIn(WidgetBinding.MIN_CELLS, spec.rows)
        val origin = snapOrigin(binding.cellX, binding.cellY, w, h, spec)
        val candidate = binding.copy(cellsW = w, cellsH = h, cellX = origin.first, cellY = origin.second)
        if (canPlace(placed, candidate, spec)) return candidate
        val fit = firstFit(placed, w, h, spec) ?: return null
        return candidate.copy(cellX = fit.first, cellY = fit.second)
    }

    private fun cellsOf(binding: WidgetBinding): List<Pair<Int, Int>> {
        return (0 until binding.cellsW).flatMap { dx ->
            (0 until binding.cellsH).map { dy -> binding.cellX + dx to binding.cellY + dy }
        }
    }
}
