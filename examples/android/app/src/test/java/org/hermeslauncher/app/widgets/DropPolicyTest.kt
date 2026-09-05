package org.hermeslauncher.app.widgets

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DropPolicyTest {
    private val spec = WidgetGridSpec.DEFAULT

    @Test
    fun inboxPageIsWrongPage() {
        assertEquals(DropPolicy.Miss.WRONG_PAGE, DropPolicy.miss(0, hasGrid = true, hit = true))
        assertNull(DropPolicy.cellTarget(0, 10f, 10f, 200f, 200f, spec))
    }

    @Test
    fun missingGridCoords() {
        assertEquals(DropPolicy.Miss.NO_GRID, DropPolicy.miss(1, hasGrid = false, hit = false))
    }

    @Test
    fun offGridIsMiss() {
        assertEquals(DropPolicy.Miss.OFF_GRID, DropPolicy.miss(1, hasGrid = true, hit = false))
        assertNull(DropPolicy.cellTarget(1, -4f, 0f, 200f, 200f, spec))
    }

    @Test
    fun desktopHitMapsCell() {
        assertNull(DropPolicy.miss(1, hasGrid = true, hit = true))
        assertEquals(Triple(2, 1, 2), DropPolicy.cellTarget(2, 60f, 90f, 200f, 200f, spec))
    }
}
