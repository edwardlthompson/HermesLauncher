package org.hermeslauncher.app.icons

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HotseatPolicyTest {
    @Test
    fun clampDoesNotWrapAndIgnoresWorkspaceIndex() {
        assertEquals(0, HotseatPolicy.clampPage(-1, 2))
        assertEquals(1, HotseatPolicy.clampPage(8, 2))
        assertEquals(0, HotseatPolicy.clampPage(3, 1))
        assertTrue(HotseatPolicy.nestedSwipeKeepsWorkspace(2, 2))
        assertFalse(HotseatPolicy.nestedSwipeKeepsWorkspace(2, 3))
    }

    @Test
    fun pageCountCoerces() {
        assertEquals(1, HotseatPolicy.pageCount(0))
        assertEquals(4, HotseatPolicy.pageCount(9))
    }
}
