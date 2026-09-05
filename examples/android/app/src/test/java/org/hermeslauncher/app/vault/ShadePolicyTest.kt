package org.hermeslauncher.app.vault

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShadePolicyTest {
    @Test
    fun openCancelsAutoCancelNotOngoing() {
        assertTrue(ShadePolicy.cancelAfterOpen(autoCancel = true, ongoing = false))
        assertFalse(ShadePolicy.cancelAfterOpen(autoCancel = true, ongoing = true))
        assertFalse(ShadePolicy.cancelAfterOpen(autoCancel = false, ongoing = false))
    }

    @Test
    fun actionKeepsReplyWithRemoteInput() {
        assertFalse(ShadePolicy.cancelAfterAction(hasRemoteInput = true))
        assertTrue(ShadePolicy.cancelAfterAction(hasRemoteInput = false))
    }

    @Test
    fun blankTitleIsNotPresentedUnlessConversation() {
        assertFalse(ShadePolicy.hasSubject(title = null, conversationTitle = null))
        assertFalse(ShadePolicy.hasSubject(title = "  ", conversationTitle = ""))
        assertTrue(ShadePolicy.hasSubject(title = null, conversationTitle = "Ada"))
        assertFalse(ShadePolicy.shouldPresent(groupSummary = true, title = "5 new", conversationTitle = null))
        assertTrue(ShadePolicy.shouldPresent(groupSummary = false, title = "Hello", conversationTitle = null))
    }
}
