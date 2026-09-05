package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InoreaderSeedTest {
    @Test
    fun sonyStaysNewsEvenUnderPodcastsFolder() {
        val xml = """
            <opml><body>
              <outline text="Podcasts">
                <outline text="Sony" xmlUrl="http://sony.mediaroom.com/index.php?s=2429&amp;pagetemplate=rss"/>
                <outline text="Show" xmlUrl="https://feeds.example.com/show.xml"/>
              </outline>
              <outline text="News">
                <outline text="Sony dup" xmlUrl="https://sony.mediaroom.com/index.php?s=2429&amp;pagetemplate=rss"/>
              </outline>
            </body></opml>
        """.trimIndent()
        val subs = InoreaderSeed.subs(xml)
        assertEquals(2, subs.size)
        val sony = subs.first { it.url.contains("sony.mediaroom.com") }
        assertTrue(sony.url.startsWith("https://"))
        assertEquals(SubKind.NEWS, sony.kind)
        assertTrue(sony.prefetch)
        val show = subs.first { it.url.contains("feeds.example.com") }
        assertEquals(SubKind.PODCAST, show.kind)
        assertFalse(show.prefetch)
        assertEquals("Podcasts", show.tag)
    }

    @Test
    fun assetFileSeedsUniqueHttpsUrls() {
        val xml = java.io.File("src/main/assets/inoreader.opml").readText(Charsets.UTF_8)
        val subs = InoreaderSeed.subs(xml)
        assertTrue(subs.size >= 60)
        assertTrue(subs.all { it.url.startsWith("https://") })
        assertTrue(subs.any { it.kind == SubKind.PODCAST })
        assertTrue(subs.any { it.kind == SubKind.NEWS })
        assertEquals(SubKind.NEWS, subs.first { it.url.contains("sony.mediaroom.com") }.kind)
        assertFalse(xml.contains("inoreader.com", ignoreCase = true))
    }
}

class FeedOpmlTest {
    @Test
    fun exportSplitsByKind() {
        val subs = listOf(
            FeedSub("https://news.example/rss", title = "N", kind = SubKind.NEWS, tag = "News"),
            FeedSub("https://pod.example/rss", title = "P", kind = SubKind.PODCAST, tag = "Podcasts"),
        )
        val news = FeedOpml.outlines(subs, SubKind.NEWS)
        val pods = FeedOpml.outlines(subs, SubKind.PODCAST)
        assertEquals(listOf("https://news.example/rss"), news.map { it.xmlUrl })
        assertEquals(listOf("https://pod.example/rss"), pods.map { it.xmlUrl })
        val existing = listOf(FeedSub(InoreaderSeed.SONY, title = "Sony", kind = SubKind.NEWS, tag = "News"))
        val mixed = FeedOpml.imported(
            existing,
            listOf(OpmlOutline("Sony", InoreaderSeed.SONY, tag = "Podcasts")),
            SubKind.PODCAST,
        )
        assertEquals(SubKind.NEWS, mixed[0].kind)
    }
}
