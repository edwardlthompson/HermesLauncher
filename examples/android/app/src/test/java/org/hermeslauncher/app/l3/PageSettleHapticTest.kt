package org.hermeslauncher.app.l3

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageSettleHapticTest {
    @Test
    fun skipsFirstLayoutAndImmediate() {
        assertFalse(PageSettleHaptic.shouldTick(userPaged = true, immediate = false, settledPage = 1, lastPage = -1))
        assertFalse(PageSettleHaptic.shouldTick(userPaged = true, immediate = true, settledPage = 1, lastPage = 0))
        assertFalse(PageSettleHaptic.shouldTick(userPaged = false, immediate = false, settledPage = 1, lastPage = 0))
        assertFalse(PageSettleHaptic.shouldTick(userPaged = true, immediate = false, settledPage = 1, lastPage = 1))
    }

    @Test
    fun ticksUserPanToNewPage() {
        assertTrue(PageSettleHaptic.shouldTick(userPaged = true, immediate = false, settledPage = 2, lastPage = 1))
    }
}
