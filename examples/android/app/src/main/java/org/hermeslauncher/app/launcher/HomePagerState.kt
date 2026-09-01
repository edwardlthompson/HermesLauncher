package org.hermeslauncher.app.launcher

/** Locked Sprint 1 pager model: page 0 is the feed, the rest are widget hosts. */
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
    }
}
