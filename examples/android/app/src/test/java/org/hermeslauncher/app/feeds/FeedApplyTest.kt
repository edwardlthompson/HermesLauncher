package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Test

class FeedApplyTest {
    @Test
    fun dropSourceRemovesMatchingFeedKeepsStarred() {
        val rows = listOf(
            rec("a", source = "https://keep.example/f"),
            rec("b", source = "https://gone.example/feed"),
            rec("c", source = "https://gone.example/feed/", starred = true),
        )
        val next = FeedApply.dropSource(rows, "https://gone.example/feed")
        assertEquals(listOf("a", "c"), next.map { it.item.id })
    }

    @Test
    fun dropSourceBlankIsNoOpAndCanDropStarred() {
        val rows = listOf(rec("a", source = "https://gone.example/f", starred = true))
        assertEquals(rows, FeedApply.dropSource(rows, ""))
        assertEquals(emptyList<String>(), FeedApply.dropSource(rows, "https://gone.example/f", keepStarred = false).map { it.item.id })
    }

    private fun rec(id: String, source: String, starred: Boolean = false): ArticleRecord {
        return ArticleRecord(
            item = FeedItem(id = id, feedTitle = "F", title = id, sourceUrl = source),
            starred = starred,
        )
    }
}
