package org.hermeslauncher.app.workspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PagedPolicyTest {
    @Test
    fun wrapOffStopsAtLastDesktop() {
        assertEquals(2, PagedPolicy.clampIndex(3, 3, wrap = false))
        assertEquals(0, PagedPolicy.clampIndex(-1, 3, wrap = false))
    }

    @Test
    fun wrapOnModuloWhenAtLeastTwoScreens() {
        assertEquals(0, PagedPolicy.clampIndex(3, 3, wrap = true))
        assertEquals(2, PagedPolicy.clampIndex(-1, 3, wrap = true))
        assertEquals(0, PagedPolicy.clampIndex(5, 1, wrap = true))
    }

    @Test
    fun labsDefaultsDenyOverlapAndInverseOff() {
        val labs = LabsFlags()
        assertFalse(PagedPolicy.canOverlap(labs))
        assertFalse(PagedPolicy.reverseLayout(ScrollMode.ADJACENT))
        assertTrue(PagedPolicy.reverseLayout(ScrollMode.INVERSE))
        assertTrue(PagedPolicy.canOverlap(LabsFlags(overlap = true)))
    }
}
