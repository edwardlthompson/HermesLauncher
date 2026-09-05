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

    @Test
    fun mergeDropsDupesAndNonPositive() {
        assertArrayEquals(
            intArrayOf(3, 7),
            WidgetHostTick.merge(intArrayOf(3, 0), intArrayOf(7, 3, -1)),
        )
        assertArrayEquals(
            intArrayOf(2, 4),
            org.hermeslauncher.app.l3.L3WidgetTick.ids(intArrayOf(2), intArrayOf(4, 2)),
        )
    }
}
