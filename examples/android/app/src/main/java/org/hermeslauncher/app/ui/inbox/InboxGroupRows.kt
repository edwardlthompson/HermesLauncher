package org.hermeslauncher.app.ui.inbox

import org.hermeslauncher.app.vault.InboxAppGroup
import org.hermeslauncher.app.vault.VaultItem

sealed class InboxGroupRow {
    data class Header(val group: InboxAppGroup, val expanded: Boolean) : InboxGroupRow()
    data class Child(val groupKey: String, val item: VaultItem) : InboxGroupRow()
}

object InboxGroupRows {
    fun flatten(
        groups: List<InboxAppGroup>,
        expandedKeys: Set<String>,
    ): List<InboxGroupRow> {
        val out = ArrayList<InboxGroupRow>()
        for (group in groups) {
            val key = groupKey(group)
            val expanded = key in expandedKeys
            out += InboxGroupRow.Header(group, expanded)
            if (expanded) {
                for (item in group.items) {
                    out += InboxGroupRow.Child(key, item)
                }
            }
        }
        return out
    }

    fun groupKey(group: InboxAppGroup): String {
        return group.displayLabel ?: group.packageName
    }
}
