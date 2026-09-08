package org.hermeslauncher.app.workspace

/** Trailing extra-empty page so reserved News/Inbox never trap drops. */
object EmptyPagePolicy {
    const val EXTRA_EMPTY_SCREEN_ID: Int = -201

    fun hasDroppablePage(screenIds: List<Int>): Boolean {
        return screenIds.any { HermesScreens.canDrop(it) }
    }

    fun needsTrailingEmpty(screenIds: List<Int>): Boolean {
        return EXTRA_EMPTY_SCREEN_ID !in screenIds
    }

    fun isTrapped(screenIds: List<Int>): Boolean {
        return !hasDroppablePage(screenIds)
    }

    fun landingScreenId(hovered: Int, extraEmpty: Int): Int {
        return if (HermesScreens.canDrop(hovered)) hovered else extraEmpty
    }

    fun landingScreenId(hovered: Int, current: Int, extraEmpty: Int): Int {
        if (HermesScreens.canDrop(hovered)) return hovered
        return if (HermesScreens.canDrop(current)) current else extraEmpty
    }

    fun nearestDroppable(screenIds: List<Int>, from: Int, extraEmpty: Int): Int {
        if (from in screenIds.indices && HermesScreens.canDrop(screenIds[from])) {
            return from
        }
        val extra = screenIds.indexOf(extraEmpty)
        if (extra >= 0) {
            return extra
        }
        val drop = screenIds.indexOfFirst { HermesScreens.canDrop(it) }
        return if (drop >= 0) drop else from.coerceIn(0, (screenIds.size - 1).coerceAtLeast(0))
    }

    fun stayOnDroppable(screenIds: List<Int>, page: Int): Int {
        return nearestDroppable(screenIds, page, EXTRA_EMPTY_SCREEN_ID)
    }

    fun step(screenIds: List<Int>, from: Int, delta: Int, wrap: Boolean): Int {
        val n = screenIds.size
        if (n == 0 || delta == 0) {
            return nearestDroppable(screenIds, from, EXTRA_EMPTY_SCREEN_ID)
        }
        var i = from
        repeat(n) {
            i += delta
            if (wrap) {
                i = ((i % n) + n) % n
            } else if (i !in screenIds.indices) {
                return from
            }
            if (HermesScreens.canDrop(screenIds[i])) {
                return i
            }
        }
        return from
    }
}
