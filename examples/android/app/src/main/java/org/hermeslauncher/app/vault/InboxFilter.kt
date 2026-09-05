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
    val newestFirst: Boolean = true,
)

data class InboxAppGroup(
    val packageName: String,
    val items: List<VaultItem>,
    val displayLabel: String? = null,
)

object InboxFilter {
    fun presentable(item: VaultItem): Boolean {
        return ShadePolicy.hasSubject(item.title, item.conversationTitle)
    }

    fun unreadCount(items: List<VaultItem>): Int {
        return items.count { presentable(it) && it.unread && !it.archived }
    }

    fun unreadLabel(count: Int): String {
        return if (count > 99) "99+" else count.toString()
    }

    fun unreadByPackage(items: List<VaultItem>): Map<String, Int> {
        return items
            .filter {
                presentable(it) && it.unread && !it.archived && it.packageName.isNotBlank()
            }
            .groupingBy { it.packageName }
            .eachCount()
    }

    fun apply(items: List<VaultItem>, query: InboxQuery): List<VaultItem> {
        val needle = query.text.trim()
        return items.filter { item ->
            presentable(item) &&
                chipMatches(item, query.chip) &&
                (query.packageName == null || item.packageName == query.packageName) &&
                textMatches(item, needle)
        }
    }

    fun sortChronological(items: List<VaultItem>, newestFirst: Boolean): List<VaultItem> {
        return if (newestFirst) {
            items.sortedByDescending { it.postedAt }
        } else {
            items.sortedBy { it.postedAt }
        }
    }

    fun groups(items: List<VaultItem>, newestFirst: Boolean = true): List<InboxAppGroup> {
        val grouped = items.groupBy { it.packageName }.map { (pkg, rows) ->
            InboxAppGroup(pkg, sortChronological(rows, newestFirst))
        }
        return orderGroups(grouped, newestFirst)
    }

    fun categoryGroups(
        items: List<VaultItem>,
        newestFirst: Boolean = true,
        kindOf: (String) -> String,
    ): List<InboxAppGroup> {
        val grouped = items.groupBy { kindOf(it.packageName) }.map { (kind, rows) ->
            InboxAppGroup(
                packageName = rows.firstOrNull()?.packageName.orEmpty(),
                items = sortChronological(rows, newestFirst),
                displayLabel = kind,
            )
        }
        return orderGroups(grouped, newestFirst)
    }

    private fun orderGroups(groups: List<InboxAppGroup>, newestFirst: Boolean): List<InboxAppGroup> {
        return if (newestFirst) {
            groups.sortedByDescending { group -> group.items.maxOf { it.postedAt } }
        } else {
            groups.sortedBy { group -> group.items.minOf { it.postedAt } }
        }
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
