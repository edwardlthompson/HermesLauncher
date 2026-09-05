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
        assertTrue(HomePulse.isHome("android.intent.action.MAIN", emptySet()))
        assertTrue(HomePulse.isHome("android.intent.action.MAIN", null))
        assertFalse(HomePulse.isHome("android.intent.action.VIEW", setOf("android.intent.category.HOME")))
        assertFalse(HomePulse.isHome("android.intent.action.MAIN", setOf("android.intent.category.DEFAULT")))
    }

    @Test
    fun extraHomeScrollsThenOpensThenToggles() {
        assertEquals(HomePulseResult.SCROLL_INBOX, HomePulse.next(page = 1, searchOpen = false))
        assertEquals(HomePulseResult.OPEN_SEARCH, HomePulse.next(page = 0, searchOpen = false))
        assertEquals(HomePulseResult.CLOSE_SEARCH, HomePulse.next(page = 0, searchOpen = true))
    }

    @Test
    fun homePulseSnapsToHomeScreenIndex() {
        assertEquals(HomePulseResult.SCROLL_INBOX, HomePulse.next(page = 0, searchOpen = false, homeIndex = 1))
        assertEquals(HomePulseResult.OPEN_SEARCH, HomePulse.next(page = 1, searchOpen = false, homeIndex = 1))
        assertEquals(HomePulseResult.CLOSE_SEARCH, HomePulse.next(page = 1, searchOpen = true, homeIndex = 1))
        assertEquals(HomePulseResult.SCROLL_INBOX, HomePulse.next(page = 2, searchOpen = false, homeIndex = 1))
    }
}
