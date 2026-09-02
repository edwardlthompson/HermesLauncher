package org.hermeslauncher.app.vault

import android.util.Log
import java.io.File
import java.util.concurrent.TimeUnit

object VaultPrune {
    const val DEFAULT_MAX: Int = 2000
    const val DEFAULT_DAYS: Int = 30
    const val MIN_INTERVAL_MS: Long = 60_000L

    fun shouldRun(nowMs: Long, lastRunMs: Long, force: Boolean): Boolean {
        if (force) {
            return true
        }
        return nowMs - lastRunMs >= MIN_INTERVAL_MS
    }

    fun idsToDelete(
        items: List<VaultItem>,
        nowMs: Long,
        maxItems: Int = DEFAULT_MAX,
        autoDeleteDays: Int = DEFAULT_DAYS,
        autoDelete: Boolean = true,
    ): List<String> {
        val cap = maxItems.coerceAtLeast(1)
        val cutoff = nowMs - TimeUnit.DAYS.toMillis(autoDeleteDays.coerceAtLeast(1).toLong())
        val archived = items.filter { it.archived && !it.pinned }.sortedBy { it.postedAt }
        val expired = if (autoDelete) archived.filter { it.postedAt < cutoff } else emptyList()
        val remaining = items.size - expired.size
        val extra = if (remaining > cap) {
            val keepIds = expired.map { it.id }.toSet()
            archived.filterNot { it.id in keepIds }.take(remaining - cap)
        } else {
            emptyList()
        }
        return (expired + extra).map { it.id }.distinct()
    }

    suspend fun run(
        dao: VaultDao,
        filesDir: File,
        maxItems: Int,
        autoDeleteDays: Int,
        autoDelete: Boolean,
        nowMs: Long = System.currentTimeMillis(),
    ) {
        val started = System.currentTimeMillis()
        val doomed = idsToDelete(dao.allItems(), nowMs, maxItems, autoDeleteDays, autoDelete)
        doomed.forEach { id ->
            dao.deleteParts(id)
            dao.deleteItem(id)
            VaultImageStore.delete(filesDir, id)
        }
        Log.i(VaultImageStore.TAG, "prune deleted=${doomed.size} ms=${System.currentTimeMillis() - started}")
    }
}
