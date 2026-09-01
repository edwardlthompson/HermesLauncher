package org.hermeslauncher.app.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawerStateTest {
    @Test
    fun openedAndClosedToggle() {
        val opened = DrawerState().opened().withQuery("maps")
        assertTrue(opened.open)
        assertEquals("maps", opened.query)
        val closed = opened.closed()
        assertFalse(closed.open)
        assertEquals("", closed.query)
    }
}
