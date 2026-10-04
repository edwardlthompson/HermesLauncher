package org.hermeslauncher.app.l3

data class DesktopCell(
    val id: Long,
    val screenId: Int,
    val cellX: Int,
    val cellY: Int,
    val spanX: Int,
    val spanY: Int,
)

/**
 * Pure reflow for Launcher3 favorites cells when the Hermes grid shrinks.
 * Never drops items — overflow goes to a new trailing screen.
 */
object DesktopReflow {
    fun reflow(items: List<DesktopCell>, columns: Int, rows: Int): List<DesktopCell> {
        val cols = columns.coerceAtLeast(1)
        val rowsN = rows.coerceAtLeast(1)
        if (items.isEmpty()) {
            return emptyList()
        }
        val byScreen = items.groupBy { it.screenId }.toSortedMap()
        val out = ArrayList<DesktopCell>(items.size)
        var overflowScreen = (byScreen.keys.maxOrNull() ?: 0) + 1
        for ((screenId, screenItems) in byScreen) {
            val placed = ArrayList<DesktopCell>()
            val deferred = ArrayList<DesktopCell>()
            for (item in screenItems.sortedWith(compareBy({ it.cellY }, { it.cellX }, { it.id }))) {
                val spanX = item.spanX.coerceIn(1, cols)
                val spanY = item.spanY.coerceIn(1, rowsN)
                val preferred = item.copy(screenId = screenId, spanX = spanX, spanY = spanY)
                when {
                    fits(placed, preferred, cols, rowsN) -> placed += preferred
                    else -> {
                        val slot = firstFit(placed, spanX, spanY, cols, rowsN, screenId)
                        if (slot != null) {
                            placed += preferred.copy(cellX = slot.first, cellY = slot.second)
                        } else {
                            deferred += preferred
                        }
                    }
                }
            }
            out += placed
            for (item in deferred) {
                var screen = overflowScreen
                var placedItem: DesktopCell? = null
                while (placedItem == null) {
                    val onScreen = out.filter { it.screenId == screen }
                    placedItem = tryPlace(onScreen, item, screen, cols, rowsN)
                    if (placedItem == null) {
                        screen += 1
                    }
                }
                out += placedItem
                overflowScreen = maxOf(overflowScreen, screen)
            }
        }
        check(out.size == items.size) { "reflow dropped items ${items.size}->${out.size}" }
        return out.sortedWith(compareBy({ it.screenId }, { it.cellY }, { it.cellX }, { it.id }))
    }

    fun inBounds(item: DesktopCell, columns: Int, rows: Int): Boolean {
        return item.cellX >= 0 && item.cellY >= 0 &&
            item.cellX + item.spanX <= columns &&
            item.cellY + item.spanY <= rows
    }

    fun fits(occupied: List<DesktopCell>, candidate: DesktopCell, columns: Int, rows: Int): Boolean {
        if (!inBounds(candidate, columns, rows)) {
            return false
        }
        return occupied.none { it.screenId == candidate.screenId && overlaps(it, candidate) }
    }

    fun firstFit(
        occupied: List<DesktopCell>,
        spanX: Int,
        spanY: Int,
        columns: Int,
        rows: Int,
        screenId: Int = occupied.firstOrNull()?.screenId ?: 0,
    ): Pair<Int, Int>? {
        if (spanX > columns || spanY > rows) {
            return null
        }
        for (y in 0..(rows - spanY)) {
            for (x in 0..(columns - spanX)) {
                val probe = DesktopCell(-1, screenId, x, y, spanX, spanY)
                if (fits(occupied, probe, columns, rows)) {
                    return x to y
                }
            }
        }
        return null
    }

    fun overlaps(a: DesktopCell, b: DesktopCell): Boolean {
        return a.cellX < b.cellX + b.spanX &&
            a.cellX + a.spanX > b.cellX &&
            a.cellY < b.cellY + b.spanY &&
            a.cellY + a.spanY > b.cellY
    }

    private fun tryPlace(
        occupied: List<DesktopCell>,
        item: DesktopCell,
        screenId: Int,
        cols: Int,
        rowsN: Int,
    ): DesktopCell? {
        val spanX = item.spanX.coerceIn(1, cols)
        val spanY = item.spanY.coerceIn(1, rowsN)
        val slot = firstFit(occupied, spanX, spanY, cols, rowsN, screenId) ?: return null
        return item.copy(screenId = screenId, cellX = slot.first, cellY = slot.second, spanX = spanX, spanY = spanY)
    }
}
