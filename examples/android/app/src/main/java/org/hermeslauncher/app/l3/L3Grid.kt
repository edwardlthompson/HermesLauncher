package org.hermeslauncher.app.l3

import kotlin.math.abs

data class GridChoice(
    val name: String,
    val columns: Int,
    val rows: Int,
)

/** Picks the closest Launcher3 grid option. Columns weigh more than rows. */
object L3Grid {
    fun pick(columns: Int, rows: Int, options: List<GridChoice>): GridChoice? {
        if (options.isEmpty()) {
            return null
        }
        return options.minBy { option ->
            abs(option.columns - columns) * 2 + abs(option.rows - rows)
        }
    }

    fun previewCap(appRowCap: Boolean, columns: Int): Int {
        if (!appRowCap) {
            return Int.MAX_VALUE
        }
        return columns.coerceAtLeast(1)
    }
}
