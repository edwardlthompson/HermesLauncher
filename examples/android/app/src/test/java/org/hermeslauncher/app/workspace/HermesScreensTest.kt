package org.hermeslauncher.app.workspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HermesScreensTest {
    @Test
    fun reservedIdsArePodcastsNewsInbox() {
        assertEquals(-300, HermesScreens.PODCASTS)
        assertEquals(-301, HermesScreens.NEWS)
        assertEquals(-302, HermesScreens.INBOX)
        assertTrue(HermesScreens.isReserved(HermesScreens.PODCASTS))
        assertTrue(HermesScreens.isReserved(HermesScreens.NEWS))
        assertTrue(HermesScreens.isReserved(HermesScreens.INBOX))
        assertFalse(HermesScreens.isReserved(0))
        assertFalse(HermesScreens.isReserved(-201))
    }

    @Test
    fun homeIndexIsInboxWhenReservedPagesExist() {
        assertEquals(0, HermesScreens.homePageIndex(0))
        assertEquals(0, HermesScreens.homePageIndex(1))
        assertEquals(1, HermesScreens.homePageIndex(2))
        assertEquals(2, HermesScreens.homePageIndex(3))
        assertEquals(2, HermesScreens.homePageIndex(5))
    }

    @Test
    fun reservedPagesRejectDrops() {
        assertFalse(HermesScreens.canDrop(HermesScreens.PODCASTS))
        assertFalse(HermesScreens.canDrop(HermesScreens.NEWS))
        assertFalse(HermesScreens.canDrop(HermesScreens.INBOX))
        assertTrue(HermesScreens.canDrop(0))
    }
}
