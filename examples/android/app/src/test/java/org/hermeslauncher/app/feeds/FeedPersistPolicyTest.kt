package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedPersistPolicyTest {
    @Test
    fun capFeedItemsKeepsNewest() {
        val items = (1..60).map { n ->
            FeedItem(id = "i$n", feedTitle = "F", title = "T$n", publishedAt = n.toLong())
        }
        val capped = FeedPersistPolicy.capFeedItems(items, max = 50)
        assertEquals(50, capped.size)
        assertEquals(60L, capped.first().publishedAt)
        assertEquals(11L, capped.last().publishedAt)
    }

    @Test
    fun newestUnreadRespectsCap() {
        val records = (1..30).map { n ->
            ArticleRecord(
                item = FeedItem(id = "i$n", feedTitle = "F", title = "T$n", publishedAt = n.toLong()),
                read = n % 2 == 0,
            )
        }
        val newest = FeedPersistPolicy.newestUnread(records, max = 5)
        assertEquals(5, newest.size)
        assertTrue(newest.all { !it.read })
        assertEquals(29L, newest.first().item.publishedAt)
    }

    @Test
    fun shouldRefreshOnOpenUsesScanFloor() {
        assertTrue(FeedPersistPolicy.shouldRefreshOnOpen(0L, 1_000L, 15))
        assertFalse(FeedPersistPolicy.shouldRefreshOnOpen(1_000L, 1_000L + 14 * 60_000L, 15))
        assertTrue(FeedPersistPolicy.shouldRefreshOnOpen(1_000L, 1_000L + 15 * 60_000L, 15))
        assertTrue(FeedPersistPolicy.shouldRefreshOnOpen(1_000L, 1_000L + 20 * 60_000L, 5))
    }
}
