package org.hermeslauncher.app.ui.scroll

object ScrubPolicy {
    fun canScrub(contentPx: Int, viewportPx: Int): Boolean = contentPx > viewportPx

    fun canScrubRange(maxScroll: Int): Boolean = maxScroll > 0

    fun thumbOffset(scroll: Int, maxScroll: Int, track: Int, thumb: Int): Int {
        if (maxScroll <= 0 || track <= thumb) {
            return 0
        }
        val range = track - thumb
        return ((scroll.toFloat() / maxScroll) * range).toInt().coerceIn(0, range)
    }

    fun scrollAt(y: Float, track: Int, thumb: Int, maxScroll: Int): Int {
        if (track <= thumb || maxScroll <= 0) {
            return 0
        }
        val range = (track - thumb).coerceAtLeast(1)
        val t = ((y - thumb / 2f) / range).coerceIn(0f, 1f)
        return (t * maxScroll).toInt()
    }

    fun letterAt(y: Float, height: Int, letters: List<Char>): Char? {
        if (letters.isEmpty() || height <= 0) {
            return null
        }
        val idx = (y / height * letters.size).toInt().coerceIn(0, letters.lastIndex)
        return letters[idx]
    }

    fun indexAt(y: Float, height: Int, count: Int): Int {
        if (count <= 1 || height <= 0) {
            return 0
        }
        return indexAtFraction(y / height, count)
    }

    fun indexAtFraction(fraction: Float, count: Int): Int {
        if (count <= 1) {
            return 0
        }
        return (fraction.coerceIn(0f, 1f) * (count - 1)).toInt().coerceIn(0, count - 1)
    }
}
