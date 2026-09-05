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

    @Test
    fun parsesAtomEntry() {
        val xml = """
            <feed xmlns="http://www.w3.org/2005/Atom">
              <title>Blog</title>
              <entry>
                <id>a-1</id>
                <title>Hello</title>
                <link href="https://example.com/hello" rel="alternate"/>
                <published>2024-01-15T12:00:00Z</published>
                <content>Hello body</content>
              </entry>
            </feed>
        """.trimIndent()
        val items = RssParser.parse(xml)
        assertEquals(1, items.size)
        assertEquals("Hello", items[0].title)
        assertEquals("https://example.com/hello", items[0].link)
        assertTrue(items[0].publishedAt > 0)
        assertEquals("https://example.com/hello", items[0].articleUrl())
        assertEquals("Hello body", items[0].html)
    }

    @Test
    fun parsesContentEncoded() {
        val xml = """
            <rss><channel>
              <title>AA</title>
              <item>
                <title>Story</title>
                <link>https://www.androidauthority.com/story/</link>
                <description>Teaser</description>
                <content:encoded><![CDATA[<p>Full piece &amp; more.</p>]]></content:encoded>
              </item>
            </channel></rss>
        """.trimIndent()
        val items = RssParser.parse(xml)
        assertEquals("<p>Full piece &amp; more.</p>", items[0].html)
    }

    @Test
    fun articleUrlRejectsNonHttp() {
        val item = FeedItem(id = "x", feedTitle = "F", title = "T", link = "javascript:alert(1)")
        assertEquals(null, item.articleUrl())
    }

    @Test
    fun parsesMediaThumbnailSkippingFaviconInHtml() {
        val xml = """
            <rss><channel>
              <title>AA</title>
              <item>
                <title>Photo story</title>
                <guid>photo-1</guid>
                <link>https://example.com/photo</link>
                <media:thumbnail url="https://cdn.example.com/hero.jpg"/>
                <description><![CDATA[<img src="https://example.com/favicon.ico">]]></description>
              </item>
            </channel></rss>
        """.trimIndent()
        val items = RssParser.parse(xml)
        assertEquals("https://cdn.example.com/hero.jpg", items[0].imageUrl)
    }

    @Test
    fun parsesMediaContentUrl() {
        val xml = """
            <rss><channel>
              <title>AA</title>
              <item>
                <title>Photo story</title>
                <guid>photo-2</guid>
                <link>https://example.com/photo2</link>
                <media:content url="https://cdn.example.com/full.jpg" medium="image"/>
              </item>
            </channel></rss>
        """.trimIndent()
        val items = RssParser.parse(xml)
        assertEquals("https://cdn.example.com/full.jpg", items[0].imageUrl)
    }
}
