package org.hermeslauncher.app.ui.workspace

import org.junit.Assert.assertEquals
import org.junit.Test

class PageIndicatorStyleTest {
    @Test
    fun threeStylesMatchNovaPageIndicator() {
        assertEquals(3, PageIndicatorStyle.entries.size)
        assertEquals(PageIndicatorStyle.DOTS, PageIndicatorStyle.valueOf("DOTS"))
    }
}
