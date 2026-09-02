package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class OpmlImporterTest {
    @Test
    fun keepsHttpOutlinesOnly() {
        val xml = """
            <opml><body>
              <outline text="ok" xmlUrl="https://example.com/feed.xml"/>
              <outline text="bad" xmlUrl="file:///tmp/x.xml"/>
            </body></opml>
        """.trimIndent()
        val outlines = OpmlImporter.read(ByteArrayInputStream(xml.toByteArray()))
        assertEquals(1, outlines.size)
        assertEquals("https://example.com/feed.xml", outlines[0].xmlUrl)
    }

    @Test
    fun malformedYieldsEmpty() {
        assertTrue(OpmlImporter.read(ByteArrayInputStream("nope".toByteArray())).isEmpty())
    }
}
