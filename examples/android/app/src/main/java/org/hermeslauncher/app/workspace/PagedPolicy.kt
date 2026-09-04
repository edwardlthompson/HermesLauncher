package org.hermeslauncher.app.workspace

enum class ScrollMode {
    ADJACENT,
    CONTINUOUS,
    INVERSE,
}

enum class QsbPlacement {
    TOP,
    BOTTOM,
    NONE,
}

enum class PinchTarget {
    OVERVIEW,
    ALL_APPS,
}

data class LabsFlags(
    val wrap: Boolean = false,
    val overlap: Boolean = false,
)

object PagedPolicy {
    fun clampIndex(index: Int, count: Int, wrap: Boolean): Int {
        if (count < 2) {
            return 0
        }
        if (wrap) {
            return ((index % count) + count) % count
        }
        return index.coerceIn(0, count - 1)
    }

    fun canOverlap(labs: LabsFlags): Boolean = labs.overlap

    fun reverseLayout(mode: ScrollMode): Boolean = mode == ScrollMode.INVERSE
}
