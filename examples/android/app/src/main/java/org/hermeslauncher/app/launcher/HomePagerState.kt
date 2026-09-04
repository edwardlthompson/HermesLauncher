package org.hermeslauncher.app.launcher

import org.hermeslauncher.app.widgets.WidgetGrid

/** Pages 0–1 are feeds and inbox; the rest are widget host grids. */
data class HomePagerState(
    val pageCount: Int = DEFAULT_PAGE_COUNT,
    val currentPage: Int = 0,
) {
    init {
        require(pageCount >= 1) { "pageCount must be at least 1" }
    }

    fun withPage(page: Int): HomePagerState {
        return copy(currentPage = page.coerceIn(0, pageCount - 1))
    }

    val isFeed: Boolean
        get() = currentPage == 0

    companion object {
        const val DEFAULT_PAGE_COUNT: Int = 3

        fun pageCountFor(widgetPageCount: Int): Int {
            return 2 + widgetPageCount.coerceIn(1, WidgetGrid.MAX_WIDGET_PAGES)
        }
    }
}
