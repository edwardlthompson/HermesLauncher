package org.hermeslauncher.app.workspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HermesDragPagesTest {
    private val extra = EmptyPagePolicy.EXTRA_EMPTY_SCREEN_ID
    private val screens = listOf(
        HermesScreens.PODCASTS,
        HermesScreens.NEWS,
        HermesScreens.INBOX,
        0,
        extra,
    )

    @Test
    fun dragSnapNeverUsesReservedPages() {
        assertEquals(4, HermesDragPages.droppable(true, screens, 0, extra))
        assertEquals(4, HermesDragPages.droppable(true, screens, 1, extra))
        assertEquals(4, HermesDragPages.droppable(true, screens, 2, extra))
        assertEquals(3, HermesDragPages.droppable(true, screens, 3, extra))
        assertEquals(0, HermesDragPages.droppable(false, screens, 0, extra))
        assertEquals(3, EmptyPagePolicy.step(screens, 3, -1, wrap = false))
        assertFalse(HermesScreens.canDrop(screens[0]))
        val afterRemove = listOf(HermesScreens.PODCASTS, HermesScreens.NEWS, HermesScreens.INBOX, 0)
        assertEquals(3, HermesDragPages.droppable(true, afterRemove, 0, extra))
        assertEquals(3, HermesDragPages.droppable(false, afterRemove, 4, extra))
    }
}
