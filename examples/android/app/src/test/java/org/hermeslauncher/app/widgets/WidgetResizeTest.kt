package org.hermeslauncher.app.widgets

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetResizeTest {
    private val spec = WidgetGridSpec(4, 5)
    private val start = WidgetBinding(5, cellsW = 2, cellsH = 2, cellX = 1, cellY = 1)

    @Test
    fun zeroDeltaReturnsSameBinding() {
        val next = WidgetResize.spansForDelta(
            start, WidgetResizeEdge.RIGHT, 0f, 0f, 50f, 50f, spec,
        )
        assertEquals(start, next)
    }

    @Test
    fun nullCellSizeReturnsSameBinding() {
        val next = WidgetResize.spansForDelta(
            start, WidgetResizeEdge.RIGHT, 80f, 0f, 0f, 50f, spec,
        )
        assertEquals(start, next)
    }

    @Test
    fun rightHandleGrowsWidth() {
        val next = WidgetResize.spansForDelta(
            start, WidgetResizeEdge.RIGHT, 50f, 0f, 50f, 50f, spec,
        )
        assertEquals(3, next.cellsW)
        assertEquals(1, next.cellX)
    }

    @Test
    fun leftHandleShiftsOrigin() {
        val next = WidgetResize.spansForDelta(
            start, WidgetResizeEdge.LEFT, -50f, 0f, 50f, 50f, spec,
        )
        assertEquals(0, next.cellX)
        assertEquals(3, next.cellsW)
        assertEquals(1, next.cellY)
    }

    @Test
    fun leftHandleClampStopsAtOriginZero() {
        val flush = start.copy(cellX = 0)
        val next = WidgetResize.spansForDelta(
            flush, WidgetResizeEdge.LEFT, -50f, 0f, 50f, 50f, spec,
        )
        assertEquals(0, next.cellX)
        assertEquals(2, next.cellsW)
    }

    @Test
    fun topHandleShiftsOrigin() {
        val next = WidgetResize.spansForDelta(
            start, WidgetResizeEdge.TOP, 0f, -50f, 50f, 50f, spec,
        )
        assertEquals(0, next.cellY)
        assertEquals(3, next.cellsH)
    }

    @Test
    fun clampsToSpec() {
        val next = WidgetResize.spansForDelta(
            start, WidgetResizeEdge.RIGHT, 500f, 0f, 50f, 50f, spec,
        )
        assertEquals(3, next.cellsW)
        assertEquals(1, next.cellX)
    }

    @Test
    fun hidesLeftHandleInCornerMinSize() {
        val corner = WidgetBinding(1, cellsW = 1, cellsH = 1, cellX = 0, cellY = 0)
        assertFalse(WidgetResize.handleVisible(corner, WidgetResizeEdge.LEFT, spec))
        assertFalse(WidgetResize.handleVisible(corner, WidgetResizeEdge.TOP, spec))
        assertTrue(WidgetResize.handleVisible(corner, WidgetResizeEdge.RIGHT, spec))
        assertTrue(WidgetResize.handleVisible(corner, WidgetResizeEdge.BOTTOM, spec))
    }
}
