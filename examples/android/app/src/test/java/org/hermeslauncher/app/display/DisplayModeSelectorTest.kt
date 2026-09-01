package org.hermeslauncher.app.display

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DisplayModeSelectorTest {
    private val qhd60 = DisplayModeChoice(1, 1440, 3200, 60f)
    private val qhd120 = DisplayModeChoice(2, 1440, 3200, 120f)
    private val fhd144 = DisplayModeChoice(3, 1080, 2400, 144f)

    @Test
    fun picksFastestSameResolution() {
        val pick = DisplayModeSelector.fastestSameResolution(listOf(qhd60, qhd120, fhd144), qhd60)
        assertEquals(qhd120, pick)
    }

    @Test
    fun ignoresHigherRefreshAtOtherResolution() {
        val pick = DisplayModeSelector.fastestSameResolution(listOf(qhd60, fhd144), qhd60)
        assertEquals(qhd60, pick)
    }

    @Test
    fun emptyModesReturnsNull() {
        assertNull(DisplayModeSelector.fastestSameResolution(emptyList(), qhd60))
    }
}
