package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleImagesTest {
    @Test
    fun looksTinyUrlSkipsIconsAndPixels() {
        assertTrue(ArticleImages.looksTinyUrl("https://cdn.example.com/favicon.ico"))
        assertTrue(ArticleImages.looksTinyUrl("https://x.com/icons/twitter.png"))
        assertTrue(ArticleImages.looksTinyUrl("https://example.com/pixel.gif"))
        assertTrue(ArticleImages.looksTinyUrl("data:image/gif;base64,xx"))
        assertFalse(ArticleImages.looksTinyUrl("https://cdn.example.com/hero-1200.jpg"))
    }

    @Test
    fun isTinyRequiresBothEdgesBelowMin() {
        assertTrue(ArticleImages.isTiny(16, 16))
        assertFalse(ArticleImages.isTiny(800, 16))
        assertFalse(ArticleImages.isTiny(0, 0))
        assertFalse(ArticleImages.isTiny(128, 128))
    }

    @Test
    fun fromRssPrefersThumbnailOverTinyHtmlIcon() {
        val html = """<img src="https://example.com/favicon.ico">"""
        val url = ArticleImages.fromRss(
            "https://cdn.example.com/hero.jpg",
            null,
            null,
            html,
        )
        assertEquals("https://cdn.example.com/hero.jpg", url)
    }

    @Test
    fun fromHtmlSkipsTinySrc() {
        val html = """
            <img src="https://example.com/icons/facebook.png">
            <img src="https://cdn.example.com/article.jpg">
        """.trimIndent()
        assertEquals(listOf("https://cdn.example.com/article.jpg"), ArticleImages.allFromHtml(html))
        assertNull(ArticleImages.fromRss(null, null, null, "<img src=\"https://example.com/favicon.ico\">"))
    }

    @Test
    fun allFromHtmlReadsOgSrcsetAndDataSrc() {
        val html = """
            <meta property="og:image" content="https://cdn.example.com/og.jpg">
            <img data-src="https://cdn.example.com/a.jpg">
            <img srcset="https://cdn.example.com/b.jpg 800w, https://cdn.example.com/c.jpg 1200w">
        """.trimIndent()
        assertEquals(
            listOf(
                "https://cdn.example.com/og.jpg",
                "https://cdn.example.com/a.jpg",
                "https://cdn.example.com/c.jpg",
            ),
            ArticleImages.allFromHtml(html),
        )
    }

    @Test
    fun canonicalHeroPicksLargestSrcsetThenMediaThenOg() {
        val html = """
            <meta property="og:image" content="https://cdn.example.com/og.jpg">
            <img srcset="https://cdn.example.com/b.jpg 800w, https://cdn.example.com/c.jpg 1200w">
        """.trimIndent()
        assertEquals(
            "https://cdn.example.com/c.jpg",
            ArticleImages.canonicalHero(null, null, null, html),
        )
        assertEquals(
            "https://cdn.example.com/media.jpg",
            ArticleImages.canonicalHero("https://cdn.example.com/media.jpg", null, null, "<meta property=\"og:image\" content=\"https://cdn.example.com/og.jpg\">"),
        )
        assertEquals(
            "https://cdn.example.com/og.jpg",
            ArticleImages.canonicalHero(null, null, null, "<meta property=\"og:image\" content=\"https://cdn.example.com/og.jpg\">"),
        )
    }
}
