package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class JsonFeedParserTest {
    @Test
    fun parsesJsonFeedItems() {
        val json = """
            {"version":"https://jsonfeed.org/version/1.1","title":"Blog",
             "items":[{"id":"1","url":"https://example.com/a","title":"Hello",
             "content_html":"<p>Body</p>","date_published":"2024-01-15T12:00:00Z",
             "image":"https://cdn.example.com/h.jpg"}]}
        """.trimIndent()
        val items = JsonFeedParser.parse(json)
        assertEquals(1, items.size)
        assertEquals("Hello", items[0].title)
        assertEquals("https://example.com/a", items[0].link)
        assertTrue(items[0].publishedAt > 0)
        assertEquals("https://cdn.example.com/h.jpg", items[0].imageUrl)
    }

    @Test
    fun emptyAndJunk() {
        assertTrue(JsonFeedParser.parse("").isEmpty())
        assertTrue(JsonFeedParser.parse("not-json").isEmpty())
    }
}
