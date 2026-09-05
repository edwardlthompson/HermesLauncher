package org.hermeslauncher.app.feeds

object PlayerClock {
    fun format(ms: Long): String {
        val totalSec = (ms.coerceAtLeast(0L) / 1000L).toInt()
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        val seconds = totalSec % 60
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%d:%02d".format(minutes, seconds)
        }
    }

    fun remaining(positionMs: Long, durationMs: Long): Long {
        return (durationMs - positionMs).coerceAtLeast(0L)
    }

    fun progress(positionMs: Long, durationMs: Long): Float {
        if (durationMs <= 0L) {
            return 0f
        }
        return (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    }

    fun seekMs(fraction: Float, durationMs: Long): Long {
        if (durationMs <= 0L) {
            return 0L
        }
        return (fraction.coerceIn(0f, 1f) * durationMs).toLong()
    }
}
