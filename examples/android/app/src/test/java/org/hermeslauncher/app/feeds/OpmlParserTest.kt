package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpmlParserTest {
    @Test
    fun parsesXmlUrlOutlines() {
        val xml = """
            <opml><body>
              <outline text="Show" xmlUrl="https://example.com/feed.xml" type="rss"/>
              <outline text="folder">
                <outline title="Nested" xmlUrl="https://example.com/n.xml"/>
              </outline>
            </body></opml>
        """.trimIndent()
        val outlines = OpmlParser.parse(xml)
        assertEquals(2, outlines.size)
        assertEquals("Show", outlines[0].title)
        assertEquals("https://example.com/feed.xml", outlines[0].xmlUrl)
        assertEquals("Nested", outlines[1].title)
    }

    @Test
    fun skipsOutlinesWithoutXmlUrl() {
        assertTrue(OpmlParser.parse("<opml><body><outline text='x'/></body></opml>").isEmpty())
    }
}
