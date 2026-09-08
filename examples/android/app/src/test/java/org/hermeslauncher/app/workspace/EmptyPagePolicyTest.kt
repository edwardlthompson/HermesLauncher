package org.hermeslauncher.app.workspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmptyPagePolicyTest {
    @Test
    fun reservedOnlyIsTrapped() {
        val screens = listOf(HermesScreens.PODCASTS, HermesScreens.NEWS, HermesScreens.INBOX)
        assertTrue(EmptyPagePolicy.isTrapped(screens))
        assertTrue(EmptyPagePolicy.needsTrailingEmpty(screens))
    }

    @Test
    fun extraEmptyUntraps() {
        val screens = listOf(
            HermesScreens.PODCASTS,
            HermesScreens.NEWS,
            HermesScreens.INBOX,
            EmptyPagePolicy.EXTRA_EMPTY_SCREEN_ID,
        )
        assertFalse(EmptyPagePolicy.isTrapped(screens))
        assertFalse(EmptyPagePolicy.needsTrailingEmpty(screens))
        assertTrue(HermesScreens.canDrop(EmptyPagePolicy.EXTRA_EMPTY_SCREEN_ID))
    }

    @Test
    fun widgetPageIsDroppable() {
        val screens = listOf(HermesScreens.INBOX, 0)
        assertFalse(EmptyPagePolicy.isTrapped(screens))
        assertTrue(EmptyPagePolicy.needsTrailingEmpty(screens))
    }

    @Test
    fun reservedHoverLandsOnExtraEmpty() {
        val extra = EmptyPagePolicy.EXTRA_EMPTY_SCREEN_ID
        assertEquals(extra, EmptyPagePolicy.landingScreenId(HermesScreens.INBOX, extra))
        assertEquals(extra, EmptyPagePolicy.landingScreenId(HermesScreens.NEWS, extra))
        assertEquals(0, EmptyPagePolicy.landingScreenId(0, extra))
        assertTrue(HermesScreens.canDrop(extra))
        assertFalse(HermesScreens.canDrop(HermesScreens.PODCASTS))
    }

    @Test
    fun wrapAndResizeSkipReservedPages() {
        val extra = EmptyPagePolicy.EXTRA_EMPTY_SCREEN_ID
        val screens = listOf(
            HermesScreens.PODCASTS,
            HermesScreens.NEWS,
            HermesScreens.INBOX,
            0,
            extra,
        )
        assertEquals(4, EmptyPagePolicy.step(screens, 3, -1, wrap = true))
        assertEquals(3, EmptyPagePolicy.step(screens, 4, 1, wrap = true))
        assertEquals(3, EmptyPagePolicy.step(screens, 3, -1, wrap = false))
        assertEquals(4, EmptyPagePolicy.nearestDroppable(screens, 0, extra))
        assertEquals(3, EmptyPagePolicy.nearestDroppable(screens, 3, extra))
        assertEquals(0, EmptyPagePolicy.landingScreenId(HermesScreens.INBOX, 0, extra))
        assertEquals(extra, EmptyPagePolicy.landingScreenId(HermesScreens.PODCASTS, HermesScreens.INBOX, extra))
        val afterRemove = listOf(HermesScreens.PODCASTS, HermesScreens.NEWS, HermesScreens.INBOX, 0)
        assertEquals(3, EmptyPagePolicy.stayOnDroppable(afterRemove, 4))
        assertEquals(3, EmptyPagePolicy.stayOnDroppable(afterRemove, 0))
        assertEquals(3, EmptyPagePolicy.stayOnDroppable(afterRemove, 3))
        assertEquals(3, HermesDragPages.droppable(false, afterRemove, 4, extra))
        assertEquals(0, HermesDragPages.droppable(false, screens, 0, extra))
    }
}
