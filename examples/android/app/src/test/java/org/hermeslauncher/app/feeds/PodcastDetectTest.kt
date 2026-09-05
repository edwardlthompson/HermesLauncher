package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PodcastDetectTest {
    @Test
    fun itunesChannelIsPodcast() {
        val xml = """
            <rss><channel>
              <itunes:author>Host</itunes:author>
              <item><title>One</title></item>
            </channel></rss>
        """.trimIndent()
        assertTrue(PodcastDetect.fromXml(xml))
    }

    @Test
    fun sonyNewsroomIsNotPodcast() {
        val xml = """
            <rss><channel><title>Sony</title>
              <item><title>Headset</title><link>https://sony.example/a</link></item>
              <item><title>Camera</title><link>https://sony.example/b</link></item>
            </channel></rss>
        """.trimIndent()
        assertFalse(PodcastDetect.fromXml(xml))
    }

    @Test
    fun majorityAudioEnclosuresCount() {
        val xml = """
            <rss><channel>
              <item><title>A</title><enclosure url="https://cdn.example/a.mp3" type="audio/mpeg"/></item>
              <item><title>B</title><enclosure url="https://cdn.example/b.m4a" type="audio/mp4"/></item>
              <item><title>C</title><link>https://blog.example/c</link></item>
            </channel></rss>
        """.trimIndent()
        assertTrue(PodcastDetect.fromXml(xml))
    }

    @Test
    fun malformedXmlIsNotPodcast() {
        assertFalse(PodcastDetect.fromXml("<not-xml"))
    }
}

class FeedKindSyncTest {
    @Test
    fun promoteNewsWhenDetectTrue() {
        val xml = "<rss><channel><itunes:author>x</itunes:author></channel></rss>"
        val next = FeedKindSync.afterFetch(FeedSub(url = "https://a.example/f"), xml)
        assertEquals(SubKind.PODCAST, next.kind)
        assertFalse(next.prefetch)
    }

    @Test
    fun demotePodcastWhenZeroAudio() {
        val xml = "<rss><channel><item><title>News</title></item></channel></rss>"
        val next = FeedKindSync.afterFetch(
            FeedSub(url = "https://sony.example/rss", kind = SubKind.PODCAST, prefetch = false),
            xml,
        )
        assertEquals(SubKind.NEWS, next.kind)
        assertTrue(next.prefetch)
    }
}
