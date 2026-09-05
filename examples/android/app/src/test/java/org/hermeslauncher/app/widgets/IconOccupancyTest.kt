package org.hermeslauncher.app.widgets

import org.hermeslauncher.app.icons.LaunchableApp
import org.hermeslauncher.app.workspace.DesktopLayout
import org.hermeslauncher.app.workspace.DesktopPin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IconOccupancyTest {
    @Test
    fun oneByOneFitsBesideTwoByTwoOnFourByFive() {
        val widget = WidgetBinding(3, "org.example/.W", 2, 2, 0, 0)
        assertTrue(IconOccupancy.canPlace(listOf(widget), 2, 0))
        assertFalse(IconOccupancy.canPlace(listOf(widget), 0, 0))
        assertFalse(IconOccupancy.canPlace(listOf(widget), 1, 1))
    }

    @Test
    fun firstFitSkipsOccupiedWidgetCells() {
        val widget = WidgetBinding(3, "org.example/.W", 2, 2, 0, 0)
        assertEquals(2 to 0, IconOccupancy.firstFit(listOf(widget), emptyList()))
        assertEquals(3 to 0, IconOccupancy.firstFit(listOf(widget), listOf(2 to 0)))
    }
}

class DesktopPinTest {
    @Test
    fun placesBesideTwoByTwoWidget() {
        val host = WidgetHostState().withBinding(1, WidgetBinding(3, "org.example/.W", 2, 2, 0, 0))
        val app = LaunchableApp("org.example", ".Main", "Ex")
        val next = DesktopPin.place(DesktopLayout(), host, 1, app)
        assertEquals(2, next?.layout?.page(1)?.single()?.cellX)
        assertEquals(0, next?.layout?.page(1)?.single()?.cellY)
        assertEquals(1, next?.pageIndex)
    }

    @Test
    fun fullPageUsesNextEmptyPage() {
        val spec = WidgetGridSpec(8, 8)
        val host = WidgetHostState(
            pages = listOf(
                WidgetPageState(1, listOf(WidgetBinding(1, "org.example/.A", 8, 4, 0, 0), WidgetBinding(2, "org.example/.B", 8, 4, 0, 4))),
                WidgetPageState(2),
            ),
            grid = spec,
        )
        val app = LaunchableApp("org.example", ".Main", "Ex")
        val next = DesktopPin.place(DesktopLayout(), host, 1, app)
        assertEquals(2, next?.pageIndex)
        assertEquals(0, next?.layout?.page(2)?.single()?.cellX)
        assertEquals(0, next?.layout?.page(2)?.single()?.cellY)
    }

    @Test
    fun dropUsesFingerCellWhenFree() {
        val host = WidgetHostState().withBinding(1, WidgetBinding(3, "org.example/.W", 2, 2, 0, 0))
        val app = LaunchableApp("org.example", ".Main", "Ex")
        val next = DesktopPin.drop(DesktopLayout(), host, 1, app, 3, 2)
        assertEquals(3, next?.layout?.page(1)?.single()?.cellX)
        assertEquals(2, next?.layout?.page(1)?.single()?.cellY)
    }

    @Test
    fun dropFallsBackWhenCellOccupied() {
        val host = WidgetHostState().withBinding(1, WidgetBinding(3, "org.example/.W", 2, 2, 0, 0))
        val app = LaunchableApp("org.example", ".Main", "Ex")
        val next = DesktopPin.drop(DesktopLayout(), host, 1, app, 0, 0)
        assertEquals(2, next?.layout?.page(1)?.single()?.cellX)
        assertEquals(0, next?.layout?.page(1)?.single()?.cellY)
    }

    @Test
    fun dropIgnoresBlankPackage() {
        val host = WidgetHostState()
        val next = DesktopPin.drop(DesktopLayout(), host, 1, LaunchableApp("  ", ".Main", "Ex"), 0, 0)
        assertEquals(null, next)
    }

    @Test
    fun dropOnInboxPagePlacesOnFirstDesktop() {
        val host = WidgetHostState()
        val app = LaunchableApp("org.example", ".Main", "Ex")
        val next = DesktopPin.drop(DesktopLayout(), host, 0, app, 3, 2)
        assertEquals(1, next?.pageIndex)
        assertEquals(0, next?.layout?.page(1)?.single()?.cellX)
        assertEquals(0, next?.layout?.page(1)?.single()?.cellY)
    }

    @Test
    fun relocateKeepsIdAndMovesCell() {
        val host = WidgetHostState()
        val app = LaunchableApp("org.example", ".Main", "Ex")
        val placed = DesktopPin.place(DesktopLayout(), host, 1, app)!!
        val id = placed.layout.page(1).single().id
        val moved = DesktopPin.relocate(placed.layout, host, 1, id, 3, 4)
        assertEquals(id, moved?.layout?.page(1)?.single()?.id)
        assertEquals(3, moved?.layout?.page(1)?.single()?.cellX)
        assertEquals(4, moved?.layout?.page(1)?.single()?.cellY)
        assertEquals(1, moved?.layout?.page(1)?.size)
    }

    @Test
    fun relocateMissingIdIsNull() {
        assertEquals(null, DesktopPin.relocate(DesktopLayout(), WidgetHostState(), 1, 9L, 0, 0))
    }

    @Test
    fun relocateOccupiedCellKeepsIcon() {
        val widget = WidgetBinding(3, "org.example/.W", 2, 2, 0, 0)
        val host = WidgetHostState().withBinding(1, widget)
        val app = LaunchableApp("org.example", ".Main", "Ex")
        val placed = DesktopPin.place(DesktopLayout(), host, 1, app)!!
        val id = placed.layout.page(1).single().id
        val originX = placed.layout.page(1).single().cellX
        val moved = DesktopPin.relocate(placed.layout, host, 1, id, 0, 0)
        assertEquals(id, moved?.layout?.page(1)?.single()?.id)
        assertEquals(1, moved?.layout?.page(1)?.size)
        assertTrue(moved?.layout?.page(1)?.single()?.cellX == originX || moved?.layout?.page(1)?.single()?.cellX == 2)
    }
}
