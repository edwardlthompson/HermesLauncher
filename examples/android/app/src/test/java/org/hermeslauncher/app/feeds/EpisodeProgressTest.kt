package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class EpisodeProgressTest {
    @Test
    fun roundTripAndPlayedThreshold() {
        val raw = EpisodeProgress.encode(mapOf("ep1" to 12_000L))
        assertEquals(12_000L, EpisodeProgress.decode(raw)["ep1"])
        assertTrue(EpisodeProgress.played(95, 100))
        assertFalse(EpisodeProgress.played(94, 100))
        assertFalse(EpisodeProgress.played(10, 0))
    }
}

class PlayQueueTest {
    @Test
    fun enqueueThenNext() {
        val queued = PlayQueue().enqueue("a").enqueue("b").enqueue("a")
        assertEquals(listOf("a", "b"), queued.ids)
        val (rest, head) = queued.next()
        assertEquals("a", head)
        assertEquals(listOf("b"), rest.ids)
        val empty = PlayQueue.decode("")
        assertNull(empty.next().second)
    }
}

class SleepTimerTest {
    @Test
    fun deadlineAndCycle() {
        assertNull(SleepTimer.deadline(1_000L, 0))
        assertEquals(1_000L + 15 * 60_000L, SleepTimer.deadline(1_000L, 15))
        assertTrue(SleepTimer.expired(50L, 50L))
        assertFalse(SleepTimer.expired(null, 99L))
        assertEquals(15, SleepTimer.cycle(0))
        assertEquals(0, SleepTimer.cycle(45))
    }
}

class PodcastAudioTest {
    @Test
    fun policyFileNameAndLatest() {
        val dir = File.createTempFile("pod", "d").apply { delete(); mkdirs() }
        val name = PodcastAudio.fileName("ep-1")
        assertTrue(name.endsWith(".bin"))
        assertEquals(File(File(dir, PodcastAudio.DIR), name).path, PodcastAudio.file(dir, "ep-1").path)
        val items = listOf(
            FeedItem("a", "F", "A", enclosureUrl = "https://a.example/a.mp3", publishedAt = 2, sourceUrl = "https://f.example/rss"),
            FeedItem("b", "F", "B", enclosureUrl = "https://a.example/b.mp3", publishedAt = 1, sourceUrl = "https://f.example/rss"),
        )
        assertEquals(listOf("a"), PodcastAudio.latest(items, 1).map { it.id })
        PodcastAudio.prefetch(dir, emptyList(), emptyList(), allow = false)
        assertFalse(File(dir, PodcastAudio.DIR).exists())
        val item = items[0]
        assertEquals(item.enclosureUrl, PodcastAudio.playUri(dir, item))
    }
}
