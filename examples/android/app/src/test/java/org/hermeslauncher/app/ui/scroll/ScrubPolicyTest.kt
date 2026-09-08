package org.hermeslauncher.app.ui.scroll

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrubPolicyTest {
    @Test
    fun canScrubWhenContentExceedsViewport() {
        assertTrue(ScrubPolicy.canScrub(200, 100))
        assertFalse(ScrubPolicy.canScrub(100, 100))
        assertFalse(ScrubPolicy.canScrub(80, 100))
        assertTrue(ScrubPolicy.canScrubRange(12))
        assertFalse(ScrubPolicy.canScrubRange(0))
    }

    @Test
    fun thumbStaysOnTrack() {
        assertEquals(0, ScrubPolicy.thumbOffset(0, 100, 50, 10))
        assertEquals(40, ScrubPolicy.thumbOffset(100, 100, 50, 10))
        assertEquals(0, ScrubPolicy.thumbOffset(50, 0, 50, 10))
        assertEquals(0, ScrubPolicy.thumbOffset(50, 100, 8, 10))
    }

    @Test
    fun scrollAtMapsTouchToRange() {
        assertEquals(0, ScrubPolicy.scrollAt(0f, 50, 10, 100))
        assertEquals(100, ScrubPolicy.scrollAt(50f, 50, 10, 100))
        assertEquals(0, ScrubPolicy.scrollAt(20f, 8, 10, 100))
    }

    @Test
    fun letterAndIndexFollowFraction() {
        val letters = listOf('A', 'M', 'Z')
        assertEquals('A', ScrubPolicy.letterAt(0f, 30, letters))
        assertEquals('Z', ScrubPolicy.letterAt(29f, 30, letters))
        assertNull(ScrubPolicy.letterAt(10f, 30, emptyList()))
        assertEquals(0, ScrubPolicy.indexAtFraction(0f, 10))
        assertEquals(9, ScrubPolicy.indexAtFraction(1f, 10))
        assertEquals(0, ScrubPolicy.indexAtFraction(0.5f, 1))
        assertEquals(4, ScrubPolicy.indexAt(50f, 100, 10))
    }
}
