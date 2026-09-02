package org.hermeslauncher.app.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeGestureTest {
    @Test
    fun codecRoundTripAndUnknownIsOff() {
        assertEquals("LOCK", DoubleTapCodec.encode(DoubleTapAction.LOCK))
        assertEquals(DoubleTapAction.FLASHLIGHT, DoubleTapCodec.parse("flashlight"))
        assertEquals(DoubleTapAction.OFF, DoubleTapCodec.parse(null))
        assertEquals(DoubleTapAction.OFF, DoubleTapCodec.parse("nope"))
    }

    @Test
    fun homePulseRequiresMainPlusHomeOrLauncher() {
        assertTrue(HomePulse.isHome("android.intent.action.MAIN", setOf("android.intent.category.HOME")))
        assertTrue(HomePulse.isHome("android.intent.action.MAIN", setOf("android.intent.category.LAUNCHER")))
        assertFalse(HomePulse.isHome("android.intent.action.VIEW", setOf("android.intent.category.HOME")))
        assertFalse(HomePulse.isHome("android.intent.action.MAIN", emptySet()))
    }

    @Test
    fun extraHomeScrollsThenOpensThenToggles() {
        assertEquals(HomePulseResult.SCROLL_INBOX, HomePulse.next(page = 1, searchOpen = false))
        assertEquals(HomePulseResult.OPEN_SEARCH, HomePulse.next(page = 0, searchOpen = false))
        assertEquals(HomePulseResult.CLOSE_SEARCH, HomePulse.next(page = 0, searchOpen = true))
    }
}
