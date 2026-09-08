package org.hermeslauncher.app.ui.inbox

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InboxStickPolicyTest {
    @Test
    fun atTopOnlyWhenFirstItemAndNoOffset() {
        assertTrue(InboxStickPolicy.isAtTop(0, 0))
        assertFalse(InboxStickPolicy.isAtTop(0, 12))
        assertFalse(InboxStickPolicy.isAtTop(1, 0))
    }

    @Test
    fun pinOnlyWhenStuckToTopAndNewestFirst() {
        assertTrue(InboxStickPolicy.shouldPinToTop(stickToTop = true, newestFirst = true))
        assertFalse(InboxStickPolicy.shouldPinToTop(stickToTop = false, newestFirst = true))
        assertFalse(InboxStickPolicy.shouldPinToTop(stickToTop = true, newestFirst = false))
    }
}
