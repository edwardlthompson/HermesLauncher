package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RssParserTest {
    @Test
    fun parsesEnclosureEpisode() {
        val xml = """
            <rss><channel>
              <title>Show</title>
              <item>
                <title>Ep 1</title>
                <guid>ep-1</guid>
                <link>https://example.com/1</link>
                <pubDate>Tue, 10 Jun 2003 04:00:00 GMT</pubDate>
                <enclosure url="https://example.com/1.mp3" type="audio/mpeg"/>
              </item>
            </channel></rss>
        """.trimIndent()
        val items = RssParser.parse(xml)
        assertEquals(1, items.size)
        assertEquals("Ep 1", items[0].title)
        assertEquals("https://example.com/1.mp3", items[0].enclosureUrl)
        assertEquals(FeedKind.EPISODE, FeedKindResolver.kindOf(items[0]))
        assertTrue(items[0].publishedAt > 0)
    }

    @Test
    fun malformedReturnsEmpty() {
        assertTrue(RssParser.parse("<not-rss").isEmpty())
    }
}
