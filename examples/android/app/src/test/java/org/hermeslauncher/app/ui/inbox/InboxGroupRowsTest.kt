package org.hermeslauncher.app.ui.inbox

import org.hermeslauncher.app.vault.InboxAppGroup
import org.hermeslauncher.app.vault.VaultItem
import org.hermeslauncher.app.vault.VaultItemType
import org.junit.Assert.assertEquals
import org.junit.Test

class InboxGroupRowsTest {
    private fun item(id: String) = VaultItem(
        id = id,
        sbnKey = id,
        packageName = "a.b",
        channelId = null,
        postedAt = id.toLongOrNull() ?: 0L,
        type = VaultItemType.MESSAGE,
        priority = 0,
        title = id,
        text = null,
        extrasJson = null,
        conversationTitle = null,
        contentStored = true,
        imagesStored = false,
    )

    @Test
    fun flattenEmitsChildrenOnlyWhenExpanded() {
        val group = InboxAppGroup(
            packageName = "a.b",
            items = listOf(item("1"), item("2")),
        )
        val collapsed = InboxGroupRows.flatten(listOf(group), emptySet())
        assertEquals(1, collapsed.size)
        val expanded = InboxGroupRows.flatten(listOf(group), setOf("a.b"))
        assertEquals(3, expanded.size)
        assertEquals("1", (expanded[1] as InboxGroupRow.Child).item.id)
    }
}
