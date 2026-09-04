package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedDiscoverTest {
    @Test
    fun normalizeAddsHttps() {
        assertEquals("https://example.com/feed.xml", FeedDiscover.normalize("example.com/feed.xml"))
        assertEquals("https://example.com/feed.xml", FeedDiscover.normalize("https://example.com/feed.xml"))
    }

    @Test
    fun alternateHrefReadsRssLink() {
        val html = """
            <html><head>
              <link rel="alternate" type="application/rss+xml" href="/rss.xml">
            </head></html>
        """.trimIndent()
        assertEquals("/rss.xml", FeedDiscover.alternateHref(html))
        assertEquals(
            "https://example.com/rss.xml",
            FeedDiscover.absolute("https://example.com/blog", "/rss.xml"),
        )
    }

    @Test
    fun alternateHrefIgnoresStylesheet() {
        assertNull(FeedDiscover.alternateHref("""<link rel="stylesheet" href="/app.css">"""))
    }

    @Test
    fun looksLikeFeedDetectsRssAndAtom() {
        assertTrue(FeedFetcher.looksLikeFeed("<rss version=\"2.0\"><channel></channel></rss>"))
        assertTrue(FeedFetcher.looksLikeFeed("<feed xmlns=\"http://www.w3.org/2005/Atom\"></feed>"))
        assertTrue(!FeedFetcher.looksLikeFeed("<html><title>Blog</title></html>"))
    }
}
