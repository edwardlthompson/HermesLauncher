package org.hermeslauncher.app.vault

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class VaultPruneTest {
    @Test
    fun deletesExpiredArchived() {
        val old = sample("old", archived = true, postedAt = 1L)
        val fresh = sample("fresh", archived = true, postedAt = TimeUnit.DAYS.toMillis(40))
        val now = TimeUnit.DAYS.toMillis(41)
        val ids = VaultPrune.idsToDelete(listOf(old, fresh), now, maxItems = 2000, autoDeleteDays = 30)
        assertEquals(listOf("old"), ids)
    }

    @Test
    fun neverDeletesPinned() {
        val pinned = sample("pin", archived = true, pinned = true, postedAt = 1L)
        val ids = VaultPrune.idsToDelete(listOf(pinned), TimeUnit.DAYS.toMillis(40), autoDeleteDays = 30)
        assertTrue(ids.isEmpty())
    }

    @Test
    fun capsOldestArchivedFirst() {
        val a = sample("a", archived = true, postedAt = 1L)
        val b = sample("b", archived = true, postedAt = 2L)
        val live = sample("live", archived = false, postedAt = 3L)
        val ids = VaultPrune.idsToDelete(
            listOf(a, b, live),
            nowMs = TimeUnit.DAYS.toMillis(1),
            maxItems = 2,
            autoDelete = false,
        )
        assertEquals(listOf("a"), ids)
    }

    @Test
    fun throttleSkipsUntilIntervalUnlessForced() {
        assertEquals(true, VaultPrune.shouldRun(nowMs = 10L, lastRunMs = 0L, force = true))
        assertEquals(false, VaultPrune.shouldRun(nowMs = 10L, lastRunMs = 0L, force = false))
        assertEquals(true, VaultPrune.shouldRun(nowMs = VaultPrune.MIN_INTERVAL_MS, lastRunMs = 0L, force = false))
    }

    private fun sample(
        id: String,
        archived: Boolean,
        pinned: Boolean = false,
        postedAt: Long,
    ): VaultItem {
        return VaultItem(
            id = id,
            sbnKey = id,
            packageName = "com.chat",
            channelId = null,
            postedAt = postedAt,
            type = VaultItemType.OTHER,
            priority = 0,
            title = id,
            text = id,
            extrasJson = null,
            conversationTitle = null,
            pinned = pinned,
            archived = archived,
            unread = false,
            contentStored = true,
            imagesStored = false,
        )
    }
}
