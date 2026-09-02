package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MiniPlayerStateTest {
    private val episode = FeedItem(
        id = "ep",
        feedTitle = "Show",
        title = "Ep 1",
        enclosureUrl = "https://example.com/1.mp3",
        enclosureMime = "audio/mpeg",
    )

    @Test
    fun loadStartsPlaying() {
        val state = MiniPlayerState().load(episode)
        assertEquals(episode, state.episode)
        assertTrue(state.playing)
    }

    @Test
    fun togglePausesThenResumes() {
        val paused = MiniPlayerState().load(episode).toggle()
        assertFalse(paused.playing)
        assertTrue(paused.toggle().playing)
    }

    @Test
    fun toggleWithoutEpisodeIsNoop() {
        assertFalse(MiniPlayerState().toggle().playing)
    }
}
