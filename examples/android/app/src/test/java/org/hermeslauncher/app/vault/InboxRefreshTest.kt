package org.hermeslauncher.app.vault

import org.junit.Assert.assertEquals
import org.junit.Test

class InboxRefreshTest {
    @Test
    fun listenerNameIsHermesNotificationListener() {
        assertEquals(
            "org.hermeslauncher.app.vault.HermesNotificationListener",
            InboxRefresh.listenerName(),
        )
    }
}
