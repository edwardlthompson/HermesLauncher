package org.hermeslauncher.app.launcher

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SwipePolicyTest {
    @Test
    fun cardsDoNotConsumeHorizontalSwipe() {
        assertFalse(SwipePolicy.consumesHorizontalSwipe(SwipeTarget.Card))
    }

    @Test
    fun pagerConsumesHorizontalSwipe() {
        assertTrue(SwipePolicy.consumesHorizontalSwipe(SwipeTarget.Pager))
    }
}
