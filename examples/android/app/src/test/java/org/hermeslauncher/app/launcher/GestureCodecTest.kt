package org.hermeslauncher.app.launcher

import org.junit.Assert.assertEquals
import org.junit.Test

class GestureCodecTest {
    @Test
    fun roundTripKeepsDefaults() {
        val encoded = GestureCodec.encodeMap(GestureMap.defaults())
        val decoded = GestureCodec.decodeMap(encoded)
        assertEquals(LauncherAction.DRAWER, decoded[GestureSlot.SWIPE_UP])
        assertEquals(LauncherAction.SEARCH, decoded[GestureSlot.SWIPE_DOWN])
    }

    @Test
    fun blankUsesDefaults() {
        assertEquals(GestureMap.defaults(), GestureCodec.decodeMap(null))
        assertEquals(GestureMap.defaults(), GestureCodec.decodeMap(""))
    }

    @Test
    fun overrideOneSlot() {
        val map = GestureCodec.decodeMap("SWIPE_UP=SEARCH")
        assertEquals(LauncherAction.SEARCH, map[GestureSlot.SWIPE_UP])
        assertEquals(LauncherAction.SEARCH, map[GestureSlot.SWIPE_DOWN])
    }

    @Test
    fun unknownActionIsNone() {
        val map = GestureCodec.decodeMap("PINCH=nope")
        assertEquals(LauncherAction.NONE, map[GestureSlot.PINCH])
    }
}
