package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class ArticleCodecTest {
    @Test
    fun encodeOmitsHtmlKey() {
        val rec = ArticleRecord(
            item = FeedItem(
                id = "aa-1",
                feedTitle = "Android Authority",
                title = "Story",
                link = "https://www.androidauthority.com/story/",
                publishedAt = 42L,
                html = "<p>${"x".repeat(20_000)}</p>",
                imageUrl = "https://cdn.example.com/hero.jpg",
                sourceUrl = "https://aa.example/feed",
            ),
            starred = true,
            read = true,
            firstSeen = 10L,
            readAt = 99L,
        )
        val encoded = ArticleCodec.encode(listOf(rec))
        assertFalse(encoded.contains("\"html\""))
        val decoded = ArticleCodec.decode(encoded)
        assertEquals(1, decoded.size)
        assertNull(decoded[0].item.html)
        assertEquals("https://cdn.example.com/hero.jpg", decoded[0].item.imageUrl)
        assertTrue(decoded[0].starred)
    }

    @Test
    fun emptyListEncodesArray() {
        assertEquals("[]", ArticleCodec.encode(emptyList()))
        assertTrue(ArticleCodec.decode("[]").isEmpty())
    }

    @Test
    fun blankAndCorruptDecodeEmpty() {
        assertTrue(ArticleCodec.decode(null).isEmpty())
        assertTrue(ArticleCodec.decode("").isEmpty())
        assertTrue(ArticleCodec.decode("not-json").isEmpty())
    }

    @Test
    fun legacyHtmlKeyIgnoredOnDecode() {
        val raw = """[{"id":"x","feedTitle":"F","title":"T","html":"<p>old</p>"}]"""
        val decoded = ArticleCodec.decode(raw)
        assertEquals(null, decoded[0].item.html)
        assertEquals(null, decoded[0].item.sourceUrl)
    }
}
