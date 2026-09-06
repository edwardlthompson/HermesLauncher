package org.hermeslauncher.app.vault

object InboxDisplay {
    const val DEFAULT_CHARS: Int = 120
    const val MIN_CHARS: Int = 40
    const val MAX_CHARS: Int = 400
    const val MIN_IMAGE_PX: Int = 240
    val CHAR_CHOICES: List<Int> = listOf(80, 120, 160, 240)

    fun clampChars(raw: Int): Int = raw.coerceIn(MIN_CHARS, MAX_CHARS)

    fun truncate(text: String, maxChars: Int): String {
        if (maxChars <= 0 || text.length <= maxChars) {
            return text
        }
        return text.take(maxChars).trimEnd() + "…"
    }

    fun keepImage(
        width: Int,
        height: Int,
        fromLargeIcon: Boolean,
        hideSmall: Boolean,
        minPx: Int = MIN_IMAGE_PX,
    ): Boolean {
        if (!hideSmall) {
            return true
        }
        if (fromLargeIcon) {
            return false
        }
        val floor = minPx.coerceAtLeast(1)
        return width >= floor && height >= floor
    }
}
