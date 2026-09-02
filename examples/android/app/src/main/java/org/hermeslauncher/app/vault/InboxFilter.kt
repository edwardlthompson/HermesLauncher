package org.hermeslauncher.app.vault

enum class InboxChip {
    ALL,
    MESSAGES,
    UNREAD,
    PINNED,
}

enum class InboxLayout {
    APP,
    CATEGORY,
    TIME,
}

data class InboxQuery(
    val chip: InboxChip = InboxChip.ALL,
    val packageName: String? = null,
    val text: String = "",
    val layout: InboxLayout = InboxLayout.APP,
)

data class InboxAppGroup(
    val packageName: String,
    val items: List<VaultItem>,
    val displayLabel: String? = null,
)

object InboxFilter {
    fun unreadCount(items: List<VaultItem>): Int {
        return items.count { it.unread && !it.archived }
    }

    fun unreadLabel(count: Int): String {
        return if (count > 99) "99+" else count.toString()
    }

    fun unreadByPackage(items: List<VaultItem>): Map<String, Int> {
        return items
            .filter { it.unread && !it.archived && it.packageName.isNotBlank() }
            .groupingBy { it.packageName }
            .eachCount()
    }

    fun apply(items: List<VaultItem>, query: InboxQuery): List<VaultItem> {
        val needle = query.text.trim()
        return items.filter { item ->
            chipMatches(item, query.chip) &&
                (query.packageName == null || item.packageName == query.packageName) &&
                textMatches(item, needle)
        }
    }

    fun groups(items: List<VaultItem>): List<InboxAppGroup> {
        return items.groupBy { it.packageName }
            .map { (pkg, rows) -> InboxAppGroup(pkg, rows.sortedByDescending { it.postedAt }) }
            .sortedByDescending { group -> group.items.maxOf { it.postedAt } }
    }

    fun categoryGroups(
        items: List<VaultItem>,
        kindOf: (String) -> String,
    ): List<InboxAppGroup> {
        return items.groupBy { kindOf(it.packageName) }
            .map { (kind, rows) ->
                InboxAppGroup(
                    packageName = rows.firstOrNull()?.packageName.orEmpty(),
                    items = rows.sortedByDescending { it.postedAt },
                    displayLabel = kind,
                )
            }
            .sortedByDescending { group -> group.items.maxOf { it.postedAt } }
    }

    fun packages(items: List<VaultItem>): List<String> {
        return items.map { it.packageName }.distinct().sorted()
    }

    private fun chipMatches(item: VaultItem, chip: InboxChip): Boolean {
        return when (chip) {
            InboxChip.ALL -> true
            InboxChip.MESSAGES -> item.type == VaultItemType.MESSAGE
            InboxChip.UNREAD -> item.unread
            InboxChip.PINNED -> item.pinned
        }
    }

    private fun textMatches(item: VaultItem, needle: String): Boolean {
        if (needle.isEmpty()) {
            return true
        }
        val hay = listOf(item.title, item.text, item.conversationTitle)
            .joinToString(" ") { it.orEmpty() }
            .lowercase()
        return hay.contains(needle.lowercase())
    }
}
