package org.hermeslauncher.app.widgets

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetHostTickTest {
    @Test
    fun positiveIdsDropsNonPositive() {
        assertArrayEquals(intArrayOf(5, 9), WidgetHostTick.positiveIds(intArrayOf(0, -1, 5, 9)))
        assertEquals(0, WidgetHostTick.positiveIds(intArrayOf()).size)
    }
}
