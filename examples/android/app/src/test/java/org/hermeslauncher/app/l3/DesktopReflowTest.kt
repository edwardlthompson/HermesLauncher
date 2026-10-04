package org.hermeslauncher.app.l3

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DesktopReflowTest {
    @Test
    fun outOfBoundsWidgetsMoveToNextScreen() {
        val items = listOf(
            DesktopCell(1, 0, 0, 0, 2, 2),
            DesktopCell(2, 0, 3, 4, 2, 2), // outside 4x5
        )
        val next = DesktopReflow.reflow(items, columns = 4, rows = 5)
        assertEquals(2, next.size)
        assertTrue(next.all { DesktopReflow.inBounds(it, 4, 5) })
        val moved = next.first { it.id == 2L }
        assertTrue(moved.screenId >= 0)
    }

    @Test
    fun countPreservedWhenShrinking() {
        val items = (1..8).map { n ->
            DesktopCell(n.toLong(), 0, (n - 1) % 5, (n - 1) / 5, 1, 1)
        }
        val next = DesktopReflow.reflow(items, columns = 4, rows = 5)
        assertEquals(items.size, next.size)
        assertEquals(items.map { it.id }.toSet(), next.map { it.id }.toSet())
    }

    @Test
    fun alreadyFittingUnchanged() {
        val items = listOf(DesktopCell(9, 0, 1, 1, 2, 2))
        val next = DesktopReflow.reflow(items, columns = 4, rows = 5)
        assertEquals(items, next)
    }
}
