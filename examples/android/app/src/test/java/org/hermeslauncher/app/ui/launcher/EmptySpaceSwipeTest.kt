package org.hermeslauncher.app.ui.launcher

import org.hermeslauncher.app.launcher.GestureSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EmptySpaceSwipeTest {
    @Test
    fun belowThresholdDoesNotFire() {
        assertNull(emptySpaceShouldFire(40f, 96f))
        assertNull(emptySpaceShouldFire(-40f, 96f))
    }

    @Test
    fun pastThresholdPicksDirection() {
        assertEquals(GestureSlot.SWIPE_UP, emptySpaceShouldFire(-97f, 96f))
        assertEquals(GestureSlot.SWIPE_DOWN, emptySpaceShouldFire(160f, 96f))
    }
}
