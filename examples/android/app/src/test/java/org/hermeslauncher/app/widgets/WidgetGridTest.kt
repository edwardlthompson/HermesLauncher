package org.hermeslauncher.app.widgets

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetGridTest {
    @Test
    fun overlapIsRejected() {
        val first = WidgetBinding(1, cellsW = 2, cellsH = 2, cellX = 0, cellY = 0)
        val overlap = WidgetBinding(2, cellsW = 2, cellsH = 2, cellX = 1, cellY = 1)
        assertFalse(WidgetGrid.canPlace(listOf(first), overlap))
    }

    @Test
    fun outOfBoundsIsRejected() {
        val oob = WidgetBinding(1, cellsW = 2, cellsH = 1, cellX = 3, cellY = 0)
        assertFalse(WidgetGrid.canPlace(emptyList(), oob))
    }

    @Test
    fun firstFitSkipsBlockedOrigin() {
        val blocker = WidgetBinding(1, cellsW = 2, cellsH = 2, cellX = 0, cellY = 0)
        assertEquals(2 to 0, WidgetGrid.firstFit(listOf(blocker), 2, 2))
    }

    @Test
    fun firstFitNullWhenFull() {
        val full = WidgetBinding(1, cellsW = 4, cellsH = 5, cellX = 0, cellY = 0)
        assertNull(WidgetGrid.firstFit(listOf(full), 1, 1))
    }

    @Test
    fun cellAtMapsPixels() {
        assertEquals(1 to 2, WidgetGrid.cellAt(60f, 90f, 200f, 200f))
        assertNull(WidgetGrid.cellAt(-1f, 0f, 200f, 200f))
    }

    @Test
    fun snapOriginKeepsWideWidgetInBounds() {
        assertEquals(0 to 1, WidgetGrid.snapOrigin(1, 1, 4, 2))
        assertEquals(2 to 3, WidgetGrid.snapOrigin(3, 4, 2, 2))
    }

    @Test
    fun sixBySixHoldsTwoPlaceSizes() {
        val spec = WidgetGridSpec(6, 6)
        val first = WidgetGrid.dropCandidate("a", 0, 0, spec)
        val second = WidgetGrid.dropCandidate("b", 2, 0, spec)
        assertEquals(2, first.cellsW)
        assertEquals(2, first.cellsH)
        assertTrue(WidgetGrid.canPlace(listOf(first.copy(appWidgetId = 1)), second.copy(appWidgetId = 2), spec))
    }

    @Test
    fun applyGridShrinksOverflow() {
        val wide = WidgetHostState(grid = WidgetGridSpec(8, 8))
            .withBinding(1, WidgetBinding(1, cellsW = 8, cellsH = 8, cellX = 0, cellY = 0))
        val shrunk = wide.withGrid(WidgetGridSpec(4, 5))
        val slot = shrunk.page(1).bindings[0]
        assertEquals(4, slot.cellsW)
        assertEquals(5, slot.cellsH)
        assertEquals(4, shrunk.grid.columns)
    }

    @Test
    fun specClampRejectsTinyAndHuge() {
        assertEquals(3, WidgetGridSpec(1, 99).clamped().columns)
        assertEquals(12, WidgetGridSpec(1, 99).clamped().rows)
        assertEquals(WidgetGridSpec(6, 6), WidgetGridSpec.parse("6x6"))
    }

    @Test
    fun stackedFromV2DoesNotOverlap() {
        val stacked = WidgetGrid.stackedFromV2(
            listOf(
                WidgetBinding(1, cellsW = 4, cellsH = 2),
                WidgetBinding(2, cellsW = 4, cellsH = 2),
            ),
        )
        assertEquals(0 to 0, stacked[0].cellX to stacked[0].cellY)
        assertEquals(0 to 2, stacked[1].cellX to stacked[1].cellY)
        assertTrue(WidgetGrid.canPlace(listOf(stacked[0]), stacked[1]))
    }

    @Test
    fun trailingEmptyDoesNotExceedMaxPages() {
        var state = WidgetHostState()
        for (page in 1..WidgetGrid.MAX_WIDGET_PAGES) {
            state = state.withBinding(
                page,
                WidgetBinding(page, cellsW = 1, cellsH = 1, cellX = 0, cellY = 0),
            )
        }
        assertEquals(WidgetGrid.MAX_WIDGET_PAGES, state.pages.size)
        assertEquals(WidgetGrid.MAX_WIDGET_PAGES, state.pages.maxOf { it.pageIndex })
    }

    @Test
    fun removingLastWidgetCollapsesToOneEmptyPage() {
        val state = WidgetHostState()
            .withBinding(1, WidgetBinding(5, cellsW = 2, cellsH = 2))
        assertEquals(2, state.pages.size)
        val cleared = state.withoutWidget(1, 5)
        assertEquals(1, cleared.pages.size)
        assertTrue(cleared.page(1).bindings.isEmpty())
    }

    @Test
    fun stackedFromV2DiffersFromSharedOrigin() {
        val raw = listOf(WidgetBinding(1), WidgetBinding(2))
        val stacked = WidgetGrid.stackedFromV2(raw)
        assertEquals(0 to 0, stacked[0].cellX to stacked[0].cellY)
        assertEquals(2 to 0, stacked[1].cellX to stacked[1].cellY)
    }
}
