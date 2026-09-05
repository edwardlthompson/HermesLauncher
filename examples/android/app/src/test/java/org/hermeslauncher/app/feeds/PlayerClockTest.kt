package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerClockTest {
    @Test
    fun formatsMinutesAndHours() {
        assertEquals("0:00", PlayerClock.format(0))
        assertEquals("1:05", PlayerClock.format(65_000))
        assertEquals("1:02:03", PlayerClock.format(3_723_000))
    }

    @Test
    fun remainingAndSeekStayInRange() {
        assertEquals(5_000L, PlayerClock.remaining(5_000, 10_000))
        assertEquals(0L, PlayerClock.remaining(12_000, 10_000))
        assertEquals(0.5f, PlayerClock.progress(5_000, 10_000), 0.001f)
        assertEquals(0f, PlayerClock.progress(1_000, 0), 0.001f)
        assertEquals(7_500L, PlayerClock.seekMs(0.75f, 10_000))
        assertEquals(0L, PlayerClock.seekMs(0.5f, 0))
    }
}
