package org.hermeslauncher.app.feeds

import org.hermeslauncher.app.vault.VaultItem

data class OpmlOutline(
    val title: String,
    val xmlUrl: String,
    val htmlUrl: String? = null,
    val type: String = "rss",
    val tag: String = "",
)

data class FeedItem(
    val id: String,
    val feedTitle: String,
    val title: String,
    val link: String? = null,
    val publishedAt: Long = 0,
    val enclosureUrl: String? = null,
    val enclosureMime: String? = null,
    val html: String? = null,
    val imageUrl: String? = null,
    val sourceUrl: String? = null,
) {
    fun articleUrl(): String? = link?.takeIf { FeedFetcher.isHttpUrl(it) }

    fun duplicateKey(): String = (articleUrl() ?: id).lowercase()

    fun fileEnclosure(): String? {
        val mime = enclosureMime.orEmpty().lowercase()
        if (enclosureUrl.isNullOrBlank() || mime.startsWith("audio/")) {
            return null
        }
        return enclosureUrl.takeIf { FeedFetcher.isHttpUrl(it) }
    }
}

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
