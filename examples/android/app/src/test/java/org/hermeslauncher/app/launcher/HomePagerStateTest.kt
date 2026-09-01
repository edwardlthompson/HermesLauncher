package org.hermeslauncher.app.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomePagerStateTest {
    @Test
    fun defaultIsFeedPage() {
        val state = HomePagerState()
        assertTrue(state.isFeed)
        assertEquals(3, state.pageCount)
    }

    @Test
    fun withPageClampsToRange() {
        val state = HomePagerState()
        assertEquals(2, state.withPage(99).currentPage)
        assertEquals(0, state.withPage(-4).currentPage)
        assertFalse(state.withPage(1).isFeed)
    }
}
