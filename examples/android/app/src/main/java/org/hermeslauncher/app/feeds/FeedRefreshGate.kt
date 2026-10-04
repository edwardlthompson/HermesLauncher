package org.hermeslauncher.app.feeds

import kotlinx.coroutines.sync.Mutex

/** Single-flight gate: second caller skips instead of stacking refreshes. */
object FeedRefreshGate {
    private val mutex = Mutex()

    suspend fun <T> runOrSkip(block: suspend () -> T): T? {
        if (!mutex.tryLock()) {
            return null
        }
        return try {
            block()
        } finally {
            mutex.unlock()
        }
    }

    fun tryBegin(): Boolean = mutex.tryLock()

    fun end() {
        if (mutex.isLocked) {
            mutex.unlock()
        }
    }
}
