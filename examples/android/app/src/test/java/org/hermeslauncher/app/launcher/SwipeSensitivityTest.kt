package org.hermeslauncher.app.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SwipeSensitivityTest {
    @Test
    fun defaultIsMediumAndLowIsHarderThanHigh() {
        assertEquals(SwipeSensitivity.MEDIUM, SwipeSensitivity.parse(null))
        assertEquals(SwipeSensitivity.LOW, SwipeSensitivity.parse("low"))
        assertEquals(SwipeSensitivity.HIGH, SwipeSensitivity.parse("HIGH"))
        assertEquals(SwipeSensitivity.MEDIUM, SwipeSensitivity.parse("nope"))
        assertEquals(true, SwipeSensitivity.LOW.slopMultiplier() > SwipeSensitivity.HIGH.slopMultiplier())
        assertEquals(true, SwipeSensitivity.LOW.emptySpacePx() > SwipeSensitivity.HIGH.emptySpacePx())
    }
}
