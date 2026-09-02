package org.hermeslauncher.app.widgets

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetHostCodecTest {
    @Test
    fun roundTripPreservesBindings() {
        val state = WidgetHostState()
            .withBinding(1, WidgetBinding(9, "org.example/.Clock", cellX = 1, cellY = 2))
        val restored = WidgetHostCodec.decode(WidgetHostCodec.encode(state))
        assertEquals(state.page(1).bindings, restored.page(1).bindings)
        assertTrue(restored.page(2).bindings.isEmpty())
    }

    @Test
    fun v3RoundTripKeepsSpanAndOrigin() {
        val state = WidgetHostState()
            .withBinding(1, WidgetBinding(3, "org.example/.W", 2, 3, 1, 1))
        val restored = WidgetHostCodec.decode(WidgetHostCodec.encode(state))
        val slot = restored.page(1).bindings[0]
        assertEquals(2, slot.cellsW)
        assertEquals(3, slot.cellsH)
        assertEquals(1, slot.cellX)
        assertEquals(1, slot.cellY)
        assertTrue(WidgetHostCodec.encode(state).startsWith("v4|"))
        assertEquals(state.grid, restored.grid)
    }

    @Test
    fun v4RoundTripKeepsCustomGrid() {
        val state = WidgetHostState(grid = WidgetGridSpec(6, 6))
            .withBinding(1, WidgetBinding(3, "org.example/.W", 2, 2, 2, 1))
        val restored = WidgetHostCodec.decode(WidgetHostCodec.encode(state))
        assertEquals(6, restored.grid.columns)
        assertEquals(6, restored.grid.rows)
        assertEquals(2 to 1, restored.page(1).bindings[0].cellX to restored.page(1).bindings[0].cellY)
    }

    @Test
    fun v2PayloadMigratesWithoutOverlap() {
        val restored = WidgetHostCodec.decode("v2|1=1:org.example/.A:4:2,2:org.example/.B:4:2")
        val bindings = restored.page(1).bindings
        assertEquals(2, bindings.size)
        assertEquals(0 to 0, bindings[0].cellX to bindings[0].cellY)
        assertEquals(0 to 2, bindings[1].cellX to bindings[1].cellY)
    }

    @Test
    fun v1PayloadStillLoads() {
        val restored = WidgetHostCodec.decode("v1|1=9:org.example/.Clock")
        assertEquals(9, restored.page(1).bindings[0].appWidgetId)
        assertEquals(WidgetBinding.DEFAULT_CELLS, restored.page(1).bindings[0].cellsW)
    }

    @Test
    fun corruptInputYieldsDefaults() {
        val restored = WidgetHostCodec.decode("not-a-payload")
        assertEquals(WidgetHostState.DEFAULT_WIDGET_PAGES, restored.pages.size)
        assertTrue(restored.page(1).bindings.isEmpty())
    }

    @Test
    fun truncatedBodyYieldsDefaults() {
        val restored = WidgetHostCodec.decode("v3|")
        assertEquals(1, restored.pages.size)
        assertTrue(restored.page(1).bindings.isEmpty())
    }
}
