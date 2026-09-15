package org.hermeslauncher.app.vault

import org.junit.Assert.assertEquals
import org.junit.Test

class InboxEmptyTest {
    @Test
    fun grantWhenListenerOffAndVaultEmpty() {
        assertEquals(
            InboxEmpty.Kind.GRANT,
            InboxEmpty.kind(
                listenerOn = false,
                itemsEmpty = true,
                liveEmpty = true,
                historyEmpty = true,
                hasVisibleFeeds = false,
            ),
        )
    }

    @Test
    fun leftoverVaultIsContentWhileListenerOff() {
        assertEquals(
            InboxEmpty.Kind.CONTENT,
            InboxEmpty.kind(
                listenerOn = false,
                itemsEmpty = false,
                liveEmpty = false,
                historyEmpty = true,
                hasVisibleFeeds = false,
            ),
        )
    }

    @Test
    fun zeroWhenListenerOnAndVaultEmpty() {
        assertEquals(
            InboxEmpty.Kind.ZERO,
            InboxEmpty.kind(
                listenerOn = true,
                itemsEmpty = true,
                liveEmpty = true,
                historyEmpty = true,
                hasVisibleFeeds = false,
            ),
        )
    }

    @Test
    fun filterWhenItemsExistButNoneVisible() {
        assertEquals(
            InboxEmpty.Kind.FILTER,
            InboxEmpty.kind(
                listenerOn = true,
                itemsEmpty = false,
                liveEmpty = true,
                historyEmpty = true,
                hasVisibleFeeds = false,
            ),
        )
    }

    @Test
    fun feedsKeepContent() {
        assertEquals(
            InboxEmpty.Kind.CONTENT,
            InboxEmpty.kind(
                listenerOn = false,
                itemsEmpty = true,
                liveEmpty = true,
                historyEmpty = true,
                hasVisibleFeeds = true,
            ),
        )
    }
}
