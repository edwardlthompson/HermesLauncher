package org.hermeslauncher.app.feeds

import org.hermeslauncher.app.vault.VaultItem

object MixPolicy {
    const val WINDOW_MS: Long = 30L * 24 * 60 * 60 * 1000
    const val READ_TTL_MS: Long = 24L * 60 * 60 * 1000

    fun withinWindow(items: List<FeedItem>, now: Long): List<FeedItem> {
        val start = now - WINDOW_MS
        return items.filter { item -> item.publishedAt == 0L || item.publishedAt >= start }
    }

    fun merge(
        vault: List<VaultItem>,
        feeds: List<FeedItem>,
        newestFirst: Boolean = true,
    ): List<MixedEntry> {
        val vaultRows = vault.map { MixedEntry.Vault(it) to it.postedAt }
        val feedRows = feeds.map { item ->
            MixedEntry.Feed(item, FeedKindResolver.kindOf(item)) to item.publishedAt
        }
        val byTime = if (newestFirst) {
            compareByDescending<Pair<MixedEntry, Long>> { it.second }
        } else {
            compareBy<Pair<MixedEntry, Long>> { it.second }
        }
        return (vaultRows + feedRows)
            .sortedWith(byTime.thenBy { keyOf(it.first) })
            .map { it.first }
    }

    private fun keyOf(entry: MixedEntry): String {
        return when (entry) {
            is MixedEntry.Vault -> entry.item.id
            is MixedEntry.Feed -> entry.item.id
        }
    }
}
