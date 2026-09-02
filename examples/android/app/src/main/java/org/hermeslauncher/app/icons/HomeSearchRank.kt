package org.hermeslauncher.app.icons

import org.hermeslauncher.app.feeds.FeedItem
import org.hermeslauncher.app.vault.VaultItem

data class HomeSearchHit(
    val apps: List<LaunchableApp>,
    val inbox: List<VaultItem>,
    val feeds: List<FeedItem>,
)

object HomeSearchRank {
    const val LIMIT: Int = 8

    fun query(
        needle: String,
        apps: List<LaunchableApp>,
        usage: List<UsageRow>,
        inbox: List<VaultItem>,
        feeds: List<FeedItem>,
        predicted: List<LaunchableApp>,
        limit: Int = LIMIT,
    ): HomeSearchHit {
        val cap = limit.coerceAtLeast(0)
        val trimmed = needle.trim()
        if (trimmed.isEmpty()) {
            val unread = inbox.filter { it.unread && !it.archived }
                .sortedByDescending { it.postedAt }
                .take(cap)
            return HomeSearchHit(predicted.take(cap), unread, emptyList())
        }
        val stats = UsageRanker.merge(usage)
        val matchedApps = apps.filter { it.label.contains(trimmed, ignoreCase = true) }
            .sortedWith(
                compareByDescending<LaunchableApp> { stats[it.packageName]?.lastTimeUsed ?: 0L }
                    .thenBy { it.label.lowercase() },
            )
            .take(cap)
        val matchedInbox = inbox.filter { item ->
            !item.archived && textOf(item).contains(trimmed, ignoreCase = true)
        }.sortedByDescending { it.postedAt }.take(cap)
        val matchedFeeds = feeds.filter { item ->
            item.title.contains(trimmed, ignoreCase = true) ||
                item.feedTitle.contains(trimmed, ignoreCase = true)
        }.sortedByDescending { it.publishedAt }.take(cap)
        return HomeSearchHit(matchedApps, matchedInbox, matchedFeeds)
    }

    private fun textOf(item: VaultItem): String {
        return listOf(item.title, item.text, item.conversationTitle).joinToString(" ") { it.orEmpty() }
    }
}
