package org.hermeslauncher.app.launcher

import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherActionTest {
    @Test
    fun defaultsSwipeUpDrawerSwipeDownSearch() {
        val map = GestureMap.defaults()
        assertEquals(LauncherAction.DRAWER, map[GestureSlot.SWIPE_UP])
        assertEquals(LauncherAction.SEARCH, map[GestureSlot.SWIPE_DOWN])
        assertEquals(LauncherAction.DRAWER, map[GestureSlot.PINCH])
        assertEquals(LauncherAction.NONE, map[GestureSlot.TWO_FINGER])
    }

    @Test
    fun unknownActionIsNone() {
        assertEquals(LauncherAction.NONE, GestureMap.parse(null))
        assertEquals(LauncherAction.NONE, GestureMap.parse("nope"))
        assertEquals(LauncherAction.LOCK, GestureMap.parse("lock"))
        assertEquals("SHADE", GestureMap.encode(LauncherAction.SHADE))
    }

    @Test
    fun overrideBeatsDefault() {
        val over = mapOf(GestureSlot.SWIPE_UP to LauncherAction.SEARCH)
        assertEquals(LauncherAction.SEARCH, GestureMap.action(GestureSlot.SWIPE_UP, over))
        assertEquals(LauncherAction.SEARCH, GestureMap.action(GestureSlot.SWIPE_DOWN, over))
    }
}
