package org.hermeslauncher.app.feeds

object SleepTimer {
    val OPTIONS: List<Int> = listOf(0, 15, 30, 45)

    fun clamp(minutes: Int): Int = OPTIONS.minByOrNull { kotlin.math.abs(it - minutes) } ?: 0

    fun deadline(nowMs: Long, minutes: Int): Long? {
        val clamped = clamp(minutes)
        if (clamped <= 0) {
            return null
        }
        return nowMs + clamped * 60_000L
    }

    fun expired(deadlineMs: Long?, nowMs: Long): Boolean {
        return deadlineMs != null && nowMs >= deadlineMs
    }

    fun cycle(current: Int): Int {
        val idx = OPTIONS.indexOf(clamp(current))
        return OPTIONS[(idx + 1) % OPTIONS.size]
    }
}
