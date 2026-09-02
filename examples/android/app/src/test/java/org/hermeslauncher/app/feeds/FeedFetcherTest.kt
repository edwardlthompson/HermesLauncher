package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedFetcherTest {
    @Test
    fun rejectsNonHttp() {
        assertFalse(FeedFetcher.isHttpUrl("javascript:alert(1)"))
        assertFalse(FeedFetcher.isHttpUrl("file:///tmp/x.xml"))
        assertFalse(FeedFetcher.isHttpUrl(""))
    }

    @Test
    fun acceptsHttps() {
        assertTrue(FeedFetcher.isHttpUrl("https://example.com/feed.xml"))
    }

    @Test
    fun parseFailureYieldsEmpty() {
        assertEquals(emptyList<FeedItem>(), FeedFetcher.itemsFromXml("not-xml"))
    }
}
