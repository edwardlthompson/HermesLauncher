package org.hermeslauncher.app.feeds

data class PlayQueue(val ids: List<String> = emptyList()) {
    fun enqueue(id: String): PlayQueue {
        if (id.isBlank() || id in ids) {
            return this
        }
        return copy(ids = ids + id)
    }

    fun next(): Pair<PlayQueue, String?> {
        val head = ids.firstOrNull()
        return copy(ids = ids.drop(1)) to head
    }

    fun encode(): String = ids.joinToString("\n")

    companion object {
        fun decode(raw: String?): PlayQueue {
            if (raw.isNullOrBlank()) {
                return PlayQueue()
            }
            return PlayQueue(raw.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList())
        }
    }
}
