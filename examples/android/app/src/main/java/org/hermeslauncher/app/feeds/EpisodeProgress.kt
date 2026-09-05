package org.hermeslauncher.app.feeds

object EpisodeProgress {
    fun encode(map: Map<String, Long>): String {
        return map.entries.joinToString("\n") { (id, ms) -> "$id\t$ms" }
    }

    fun decode(raw: String?): Map<String, Long> {
        if (raw.isNullOrBlank()) {
            return emptyMap()
        }
        return raw.lineSequence().mapNotNull { line ->
            val tab = line.indexOf('\t')
            if (tab <= 0) {
                return@mapNotNull null
            }
            val id = line.substring(0, tab)
            val ms = line.substring(tab + 1).toLongOrNull() ?: return@mapNotNull null
            id to ms
        }.toMap()
    }

    fun played(positionMs: Long, durationMs: Long): Boolean {
        if (durationMs <= 0L) {
            return false
        }
        return positionMs >= durationMs * 95L / 100L
    }
}
