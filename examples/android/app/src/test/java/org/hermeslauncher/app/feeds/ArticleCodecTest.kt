package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class ArticleCodecTest {
    @Test
    fun roundTripKeepsStarReadAndImage() {
        val rec = ArticleRecord(
            item = FeedItem(
                id = "aa-1",
                feedTitle = "Android Authority",
                title = "Story",
                link = "https://www.androidauthority.com/story/",
                publishedAt = 42L,
                html = "<p>Body</p>",
                imageUrl = "https://cdn.example.com/hero.jpg",
                sourceUrl = "https://aa.example/feed",
            ),
            starred = true,
            read = true,
            firstSeen = 10L,
            readAt = 99L,
        )
        val decoded = ArticleCodec.decode(ArticleCodec.encode(listOf(rec)))
        assertEquals(1, decoded.size)
        assertEquals("aa-1", decoded[0].item.id)
        assertEquals("https://cdn.example.com/hero.jpg", decoded[0].item.imageUrl)
        assertEquals("https://aa.example/feed", decoded[0].item.sourceUrl)
        assertTrue(decoded[0].starred)
        assertTrue(decoded[0].read)
        assertEquals(10L, decoded[0].firstSeen)
        assertEquals(99L, decoded[0].readAt)
    }

    @Test
    fun blankAndCorruptDecodeEmpty() {
        assertTrue(ArticleCodec.decode(null).isEmpty())
        assertTrue(ArticleCodec.decode("").isEmpty())
        assertTrue(ArticleCodec.decode("not-json").isEmpty())
    }

    @Test
    fun missingSourceUrlStaysNull() {
        val raw = """[{"id":"x","feedTitle":"F","title":"T"}]"""
        val decoded = ArticleCodec.decode(raw)
        assertEquals(null, decoded[0].item.sourceUrl)
    }
}
