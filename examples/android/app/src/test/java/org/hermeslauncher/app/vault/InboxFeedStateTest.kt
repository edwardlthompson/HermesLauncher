package org.hermeslauncher.app.vault

import org.junit.Assert.assertEquals
import org.junit.Test

class InboxFeedStateTest {
    @Test
    fun dismissRemovesOnlyThatItem() {
        val keep = sampleItem("keep")
        val drop = sampleItem("drop")
        val next = InboxFeedState(listOf(keep, drop)).dismissed("drop")
        assertEquals(listOf(keep), next.items)
    }

    private fun sampleItem(id: String): VaultItem {
        return VaultItem(
            id = id,
            sbnKey = id,
            packageName = "com.example.chat",
            channelId = null,
            postedAt = 1L,
            type = VaultItemType.MESSAGE,
            priority = 0,
            title = id,
            text = id,
            extrasJson = null,
            conversationTitle = null,
            contentStored = true,
            imagesStored = false,
        )
    }
}
