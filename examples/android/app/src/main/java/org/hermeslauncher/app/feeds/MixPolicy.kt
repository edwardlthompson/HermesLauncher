package org.hermeslauncher.app.feeds

import org.hermeslauncher.app.vault.VaultItem

object MixPolicy {
    fun merge(vault: List<VaultItem>, feeds: List<FeedItem>): List<MixedEntry> {
        val vaultRows = vault.map { MixedEntry.Vault(it) to it.postedAt }
        val feedRows = feeds.map { item ->
            MixedEntry.Feed(item, FeedKindResolver.kindOf(item)) to item.publishedAt
        }
        return (vaultRows + feedRows)
            .sortedWith(compareByDescending<Pair<MixedEntry, Long>> { it.second }.thenBy { keyOf(it.first) })
            .map { it.first }
    }

    private fun keyOf(entry: MixedEntry): String {
        return when (entry) {
            is MixedEntry.Vault -> entry.item.id
            is MixedEntry.Feed -> entry.item.id
        }
    }
}
