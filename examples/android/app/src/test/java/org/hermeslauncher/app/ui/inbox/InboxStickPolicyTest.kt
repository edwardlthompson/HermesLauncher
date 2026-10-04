package org.hermeslauncher.app.ui.inbox

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InboxStickPolicyTest {
    @Test
    fun leaveTopWhileScrollingClearsStick() {
        assertFalse(InboxStickPolicy.nextStick(true, index = 1, offset = 0, isScrollInProgress = true))
        assertFalse(InboxStickPolicy.nextStick(true, index = 0, offset = 12, isScrollInProgress = true))
    }

    @Test
    fun atTopAfterScrollSettlesKeepsStick() {
        assertTrue(InboxStickPolicy.nextStick(false, index = 0, offset = 0, isScrollInProgress = false))
    }

    @Test
    fun noPinWhileScrolling() {
        assertFalse(InboxStickPolicy.shouldScrollToTop(true, newestFirst = true, isScrollInProgress = true))
        assertTrue(InboxStickPolicy.shouldScrollToTop(true, newestFirst = true, isScrollInProgress = false))
    }
}
