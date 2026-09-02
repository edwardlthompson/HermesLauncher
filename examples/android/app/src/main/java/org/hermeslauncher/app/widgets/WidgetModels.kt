package org.hermeslauncher.app.widgets

data class WidgetBinding(
    val appWidgetId: Int,
    val providerFlattened: String? = null,
    val cellsW: Int = PLACE_CELLS,
    val cellsH: Int = PLACE_CELLS_H,
    val cellX: Int = 0,
    val cellY: Int = 0,
) {
    fun resized(width: Int, height: Int, spec: WidgetGridSpec = WidgetGridSpec.DEFAULT): WidgetBinding {
        val grid = spec.clamped()
        return copy(
            cellsW = width.coerceIn(MIN_CELLS, grid.columns),
            cellsH = height.coerceIn(MIN_CELLS, grid.rows),
        )
    }

    companion object {
        const val MIN_CELLS: Int = 1
        const val PLACE_CELLS: Int = 2
        const val PLACE_CELLS_H: Int = 2
        const val DEFAULT_CELLS: Int = PLACE_CELLS
        const val DEFAULT_CELLS_H: Int = PLACE_CELLS_H
    }
}

data class WidgetPageState(
    val pageIndex: Int,
    val bindings: List<WidgetBinding> = emptyList(),
) {
    init {
        require(pageIndex >= 1) { "widget pageIndex must be at least 1" }
    }

    fun withBinding(
        binding: WidgetBinding,
        spec: WidgetGridSpec = WidgetGridSpec.DEFAULT,
    ): WidgetPageState {
        if (!WidgetBindPolicy.canRecord(binding.appWidgetId)) {
            return this
        }
        val without = bindings.filterNot { it.appWidgetId == binding.appWidgetId }
        val placed = if (WidgetGrid.canPlace(without, binding, spec)) {
            binding
        } else {
            val fit = WidgetGrid.firstFit(without, binding.cellsW, binding.cellsH, spec) ?: return this
            binding.copy(cellX = fit.first, cellY = fit.second)
        }
        return copy(bindings = without + placed)
    }

    fun withoutWidget(appWidgetId: Int): WidgetPageState {
        return copy(bindings = bindings.filterNot { it.appWidgetId == appWidgetId })
    }
}

data class WidgetHostState(
    val pages: List<WidgetPageState> = listOf(WidgetPageState(1)),
    val grid: WidgetGridSpec = WidgetGridSpec.DEFAULT,
) {
    fun page(pageIndex: Int): WidgetPageState {
        return pages.firstOrNull { it.pageIndex == pageIndex } ?: WidgetPageState(pageIndex)
    }

    fun withGrid(spec: WidgetGridSpec): WidgetHostState = WidgetGrid.applyGrid(this, spec)

    fun withBinding(pageIndex: Int, binding: WidgetBinding): WidgetHostState {
        return WidgetGrid.withTrailingEmpty(mutate(pageIndex) { it.withBinding(binding, grid) })
    }

    fun withoutWidget(pageIndex: Int, appWidgetId: Int): WidgetHostState {
        return WidgetGrid.withTrailingEmpty(mutate(pageIndex) { it.withoutWidget(appWidgetId) })
    }

    fun resized(
        pageIndex: Int,
        appWidgetId: Int,
        width: Int,
        height: Int,
        minW: Int = WidgetBinding.MIN_CELLS,
        minH: Int = WidgetBinding.MIN_CELLS,
    ): WidgetHostState {
        return mutate(pageIndex) { page ->
            val next = page.bindings.map { binding ->
                if (binding.appWidgetId != appWidgetId) {
                    return@map binding
                }
                val candidate = binding.resized(width.coerceAtLeast(minW), height.coerceAtLeast(minH), grid)
                val others = page.bindings.filterNot { it.appWidgetId == appWidgetId }
                if (WidgetGrid.canPlace(others, candidate, grid)) candidate else binding
            }
            page.copy(bindings = next)
        }
    }

    fun applySpan(pageIndex: Int, next: WidgetBinding): WidgetHostState {
        return mutate(pageIndex) { page ->
            val others = page.bindings.filterNot { it.appWidgetId == next.appWidgetId }
            if (!WidgetGrid.canPlace(others, next, grid)) page else page.copy(bindings = others + next)
        }
    }

    fun relocate(
        fromPage: Int,
        toPage: Int,
        appWidgetId: Int,
        cellX: Int,
        cellY: Int,
    ): WidgetHostState {
        val binding = page(fromPage).bindings.firstOrNull { it.appWidgetId == appWidgetId } ?: return this
        val origin = WidgetGrid.snapOrigin(cellX, cellY, binding.cellsW, binding.cellsH, grid)
        val moved = binding.copy(cellX = origin.first, cellY = origin.second)
        val dest = if (fromPage == toPage) {
            page(fromPage).bindings.filterNot { it.appWidgetId == appWidgetId }
        } else {
            page(toPage).bindings
        }
        if (!WidgetGrid.canPlace(dest, moved, grid)) {
            return this
        }
        return withoutWidget(fromPage, appWidgetId).withBinding(toPage, moved)
    }

    private fun mutate(
        pageIndex: Int,
        transform: (WidgetPageState) -> WidgetPageState,
    ): WidgetHostState {
        val existing = pages.associateBy { it.pageIndex }.toMutableMap()
        val current = existing[pageIndex] ?: WidgetPageState(pageIndex)
        existing[pageIndex] = transform(current)
        return copy(pages = existing.values.sortedBy { it.pageIndex })
    }

    companion object {
        const val DEFAULT_WIDGET_PAGES: Int = 1
    }
}

object WidgetHostIds {
    const val HOST_ID: Int = 1024
}

object WidgetBindPolicy {
    fun canRecord(appWidgetId: Int): Boolean {
        return appWidgetId > 0
    }
}
