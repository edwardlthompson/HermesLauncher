package org.hermeslauncher.app.icons

import org.hermeslauncher.app.feeds.FeedItem
import org.hermeslauncher.app.vault.VaultItem

data class HomeSearchHit(
    val apps: List<LaunchableApp>,
    val inbox: List<VaultItem>,
    val feeds: List<FeedItem>,
    val contacts: List<String> = emptyList(),
)

object HomeSearchRank {
    const val LIMIT: Int = 8
    const val APP_ROW: Int = 5

    fun contacts(granted: Boolean, hits: List<String>): List<String> {
        if (!granted) {
            return emptyList()
        }
        return hits.filter { it.isNotBlank() }
    }

    fun query(
        needle: String,
        apps: List<LaunchableApp>,
        usage: List<UsageRow>,
        inbox: List<VaultItem>,
        feeds: List<FeedItem>,
        predicted: List<LaunchableApp>,
        limit: Int = LIMIT,
        appCap: Int = LIMIT,
        contactsGranted: Boolean = false,
        contactHits: List<String> = emptyList(),
    ): HomeSearchHit {
        val cap = limit.coerceAtLeast(0)
        val appsCap = appCap.coerceAtLeast(0)
        val trimmed = needle.trim()
        if (trimmed.isEmpty()) {
            val unread = inbox.filter { it.unread && !it.archived }
                .sortedByDescending { it.postedAt }
                .take(cap)
            return HomeSearchHit(predicted.take(cap), unread, emptyList(), emptyList())
        }
        val stats = UsageRanker.merge(usage)
        val matchedApps = apps.filter { it.label.contains(trimmed, ignoreCase = true) }
            .sortedWith(
                compareByDescending<LaunchableApp> { stats[it.packageName]?.lastTimeUsed ?: 0L }
                    .thenBy { it.label.lowercase() },
            )
            .take(appsCap)
        val matchedInbox = inbox.filter { item ->
            !item.archived && textOf(item).contains(trimmed, ignoreCase = true)
        }.sortedByDescending { it.postedAt }.take(cap)
        val matchedFeeds = feeds.filter { item ->
            item.title.contains(trimmed, ignoreCase = true) ||
                item.feedTitle.contains(trimmed, ignoreCase = true)
        }.sortedByDescending { it.publishedAt }.take(cap)
        return HomeSearchHit(
            matchedApps,
            matchedInbox,
            matchedFeeds,
            contacts(contactsGranted, contactHits),
        )
    }

    private fun textOf(item: VaultItem): String {
        return listOf(item.title, item.text, item.conversationTitle).joinToString(" ") { it.orEmpty() }
    }
}
