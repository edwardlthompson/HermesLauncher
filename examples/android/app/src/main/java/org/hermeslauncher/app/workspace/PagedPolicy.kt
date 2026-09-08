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

enum class PageSnapSpeed(val durationMs: Int) {
    FAST(200),
    NORMAL(350),
    SLOW(750),
    ;

    companion object {
        val DEFAULT: PageSnapSpeed = FAST

        fun fromName(raw: String?): PageSnapSpeed {
            return entries.firstOrNull { it.name == raw } ?: DEFAULT
        }
    }
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
