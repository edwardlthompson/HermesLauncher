package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Test

class ArticleEnrichTest {
    @Test
    fun fromItemUsesHtmlBeforeNetwork() {
        val item = FeedItem(
            id = "1",
            feedTitle = "F",
            title = "T",
            html = """<img src="https://cdn.example.com/hero.jpg">""",
        )
        assertEquals("https://cdn.example.com/hero.jpg", ArticleEnrich.fromItem(item))
    }

    @Test
    fun fillRecordsSkipsRowsThatAlreadyHaveThumbs() {
        val rec = ArticleRecord(
            item = FeedItem(
                id = "1",
                feedTitle = "F",
                title = "T",
                imageUrl = "https://cdn.example.com/kept.jpg",
            ),
        )
        val filled = ArticleEnrich.fillRecords(listOf(rec))
        assertEquals("https://cdn.example.com/kept.jpg", filled[0].item.imageUrl)
    }
}
