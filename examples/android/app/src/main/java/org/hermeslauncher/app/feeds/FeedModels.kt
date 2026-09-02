package org.hermeslauncher.app.feeds

import org.hermeslauncher.app.vault.VaultItem

data class OpmlOutline(
    val title: String,
    val xmlUrl: String,
    val htmlUrl: String? = null,
    val type: String = "rss",
)

data class FeedItem(
    val id: String,
    val feedTitle: String,
    val title: String,
    val link: String? = null,
    val publishedAt: Long = 0,
    val enclosureUrl: String? = null,
    val enclosureMime: String? = null,
)

enum class FeedKind {
    ARTICLE,
    EPISODE,
}

sealed class MixedEntry {
    data class Vault(val item: VaultItem) : MixedEntry()
    data class Feed(val item: FeedItem, val kind: FeedKind) : MixedEntry()
}

object FeedKindResolver {
    fun kindOf(item: FeedItem): FeedKind {
        val mime = item.enclosureMime.orEmpty().lowercase()
        return if (item.enclosureUrl != null && mime.startsWith("audio/")) {
            FeedKind.EPISODE
        } else {
            FeedKind.ARTICLE
        }
    }
}
