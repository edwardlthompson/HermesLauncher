package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OpmlExporterTest {
    @Test
    fun emptyBodyIsValid() {
        val xml = OpmlExporter.write(emptyList())
        assertTrue(xml.contains("<body>"))
        assertTrue(xml.contains("</body>"))
        assertTrue(OpmlParser.parse(xml).isEmpty())
    }

    @Test
    fun nestedTagRoundTrip() {
        val outlines = listOf(
            OpmlOutline("News", "https://example.com/rss.xml", tag = "News"),
            OpmlOutline("Pod", "https://example.com/pod.xml", tag = "Podcasts"),
        )
        val xml = OpmlExporter.write(outlines, "Hermes podcasts")
        assertTrue(xml.contains("<title>Hermes podcasts</title>"))
        val parsed = OpmlParser.parse(xml)
        assertEquals("News", parsed[0].tag)
        assertEquals("Podcasts", parsed[1].tag)
        assertEquals("https://example.com/rss.xml", parsed[0].xmlUrl)
    }

    @Test
    fun newsOnlyOmitsPodcastUrls() {
        val xml = OpmlExporter.write(
            listOf(OpmlOutline("News", "https://n.example/rss", tag = "News")),
            "Hermes feeds",
        )
        assertFalse(xml.contains("pod.example"))
        assertTrue(xml.contains("n.example/rss"))
    }
}
