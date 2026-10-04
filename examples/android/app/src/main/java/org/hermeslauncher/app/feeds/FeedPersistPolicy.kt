package org.hermeslauncher.app.feeds

object FeedPersistPolicy {
    const val MAX_ITEMS_PER_FEED: Int = 50
    const val MAX_XML_BYTES: Int = 2 * 1024 * 1024
    const val PREFETCH_NEWEST: Int = 20
    const val PROGRESS_EVERY_FEEDS: Int = 5

    fun capFeedItems(items: List<FeedItem>, max: Int = MAX_ITEMS_PER_FEED): List<FeedItem> {
        if (items.size <= max) {
            return items
        }
        return items.sortedByDescending { it.publishedAt }.take(max)
    }

    fun newestUnread(records: List<ArticleRecord>, max: Int = PREFETCH_NEWEST): List<ArticleRecord> {
        return records
            .filter { !it.read }
            .sortedByDescending { it.item.publishedAt }
            .take(max)
    }

    fun shouldRefreshOnOpen(lastRefreshAt: Long, now: Long, scanMinutes: Int): Boolean {
        val floorMin = scanMinutes.coerceAtLeast(15)
        if (lastRefreshAt <= 0L) {
            return true
        }
        return now - lastRefreshAt >= floorMin * 60_000L
    }
}
