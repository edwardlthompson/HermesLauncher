package org.hermeslauncher.app.ui.theme

/**
 * Optional night window that forces dark when [enabled] and [nowMinute] is inside
 * [startMinute]..[endMinute] (wrapping midnight when start > end).
 * Invalid times → [OFF] (follow [ThemeMode] / system).
 */
data class NightSchedule(
    val enabled: Boolean = false,
    val startMinute: Int = DEFAULT_START,
    val endMinute: Int = DEFAULT_END,
) {
    fun contains(nowMinute: Int): Boolean {
        val now = nowMinute.coerceIn(0, MINUTES_PER_DAY - 1)
        val start = startMinute.coerceIn(0, MINUTES_PER_DAY - 1)
        val end = endMinute.coerceIn(0, MINUTES_PER_DAY - 1)
        return if (start <= end) {
            now in start until end
        } else {
            now >= start || now < end
        }
    }

    companion object {
        const val MINUTES_PER_DAY = 24 * 60
        const val DEFAULT_START = 22 * 60
        const val DEFAULT_END = 7 * 60
        val OFF = NightSchedule(enabled = false)

        /** Parses `HH:mm` or `H:mm`; blank/invalid → null. */
        fun parseTime(raw: String?): Int? {
            if (raw.isNullOrBlank()) return null
            val parts = raw.trim().split(':')
            if (parts.size != 2) return null
            val hour = parts[0].toIntOrNull() ?: return null
            val minute = parts[1].toIntOrNull() ?: return null
            if (hour !in 0..23 || minute !in 0..59) return null
            return hour * 60 + minute
        }

        fun formatTime(minuteOfDay: Int): String {
            val m = minuteOfDay.coerceIn(0, MINUTES_PER_DAY - 1)
            return "%02d:%02d".format(m / 60, m % 60)
        }

        fun parse(enabled: Boolean, startRaw: String?, endRaw: String?): NightSchedule {
            val start = parseTime(startRaw) ?: return OFF
            val end = parseTime(endRaw) ?: return OFF
            return NightSchedule(enabled = enabled, startMinute = start, endMinute = end)
        }
    }
}

object ThemeResolve {
    /** Schedule window forces dark when enabled; otherwise [ThemeMode] / system. */
    fun isDark(
        mode: ThemeMode,
        schedule: NightSchedule,
        systemDark: Boolean,
        nowMinute: Int,
    ): Boolean {
        if (schedule.enabled && schedule.contains(nowMinute)) {
            return true
        }
        return when (mode) {
            ThemeMode.System -> systemDark
            ThemeMode.Light -> false
            ThemeMode.Dark -> true
        }
    }
}
