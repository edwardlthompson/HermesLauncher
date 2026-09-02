package org.hermeslauncher.app.widgets

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetHostStateTest {
    @Test
    fun defaultHasOneEmptyPage() {
        val state = WidgetHostState()
        assertEquals(1, state.pages.size)
        assertTrue(state.page(1).bindings.isEmpty())
    }

    @Test
    fun recordsBindingOnPageOneOnly() {
        val binding = WidgetBinding(5, "pkg/cls", cellX = 0, cellY = 0)
        val next = WidgetHostState().withBinding(1, binding)
        assertEquals(listOf(binding), next.page(1).bindings)
        assertTrue(next.page(2).bindings.isEmpty())
        assertEquals(2, next.pages.size)
    }

    @Test
    fun rejectsNonPositiveIds() {
        val next = WidgetHostState().withBinding(1, WidgetBinding(0))
        assertTrue(next.page(1).bindings.isEmpty())
    }

    @Test
    fun removeClearsOneId() {
        val state = WidgetHostState()
            .withBinding(1, WidgetBinding(5, cellsW = 1, cellsH = 1, cellX = 0, cellY = 0))
            .withBinding(1, WidgetBinding(6, cellsW = 1, cellsH = 1, cellX = 1, cellY = 0))
            .withoutWidget(1, 5)
        assertEquals(
            listOf(WidgetBinding(6, cellsW = 1, cellsH = 1, cellX = 1, cellY = 0)),
            state.page(1).bindings,
        )
    }

    @Test
    fun resizeClampsToMinimum() {
        val state = WidgetHostState()
            .withBinding(1, WidgetBinding(5, cellsW = 2, cellsH = 2))
            .resized(1, 5, 0, 0)
        assertEquals(1, state.page(1).bindings[0].cellsW)
        assertEquals(1, state.page(1).bindings[0].cellsH)
    }

    @Test
    fun twoWidgetsShareCustomGrid() {
        val state = WidgetHostState(grid = WidgetGridSpec(6, 6))
            .withBinding(1, WidgetBinding(5, cellsW = 2, cellsH = 2, cellX = 0, cellY = 0))
            .withBinding(1, WidgetBinding(6, cellsW = 2, cellsH = 2, cellX = 2, cellY = 0))
        assertEquals(2, state.page(1).bindings.size)
        assertEquals(6, state.grid.columns)
    }

    @Test
    fun relocateRejectsOccupiedCell() {
        val state = WidgetHostState()
            .withBinding(1, WidgetBinding(5, cellsW = 2, cellsH = 2, cellX = 0, cellY = 0))
            .withBinding(1, WidgetBinding(6, cellsW = 2, cellsH = 2, cellX = 2, cellY = 0))
        val blocked = state.relocate(1, 1, 6, 0, 0)
        assertEquals(2, blocked.page(1).bindings.first { it.appWidgetId == 6 }.cellX)
    }

    @Test
    fun applySpanShiftsOrigin() {
        val state = WidgetHostState()
            .withBinding(1, WidgetBinding(5, cellsW = 2, cellsH = 2, cellX = 1, cellY = 1))
        val next = state.applySpan(
            1,
            WidgetBinding(5, cellsW = 3, cellsH = 2, cellX = 0, cellY = 1),
        )
        assertEquals(0, next.page(1).bindings[0].cellX)
        assertEquals(3, next.page(1).bindings[0].cellsW)
    }

    @Test
    fun applySpanRejectsOverlap() {
        val state = WidgetHostState()
            .withBinding(1, WidgetBinding(5, cellsW = 2, cellsH = 2, cellX = 0, cellY = 0))
            .withBinding(1, WidgetBinding(6, cellsW = 2, cellsH = 2, cellX = 2, cellY = 0))
        val blocked = state.applySpan(
            1,
            WidgetBinding(6, cellsW = 3, cellsH = 2, cellX = 0, cellY = 0),
        )
        assertEquals(2, blocked.page(1).bindings.first { it.appWidgetId == 6 }.cellX)
    }
}
