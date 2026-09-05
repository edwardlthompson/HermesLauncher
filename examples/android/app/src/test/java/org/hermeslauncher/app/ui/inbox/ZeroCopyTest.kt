package org.hermeslauncher.app.ui.inbox

import org.hermeslauncher.app.R
import org.junit.Assert.assertEquals
import org.junit.Test

class ZeroCopyTest {
    @Test
    fun rotatesByDay() {
        assertEquals(R.string.zero_inbox_1, ZeroCopy.pick(ZeroKind.INBOX, 0))
        assertEquals(R.string.zero_inbox_2, ZeroCopy.pick(ZeroKind.INBOX, 1))
        assertEquals(R.string.zero_news_1, ZeroCopy.pick(ZeroKind.NEWS, 0))
        assertEquals(R.string.zero_podcast_3, ZeroCopy.pick(ZeroKind.PODCAST, 2))
    }

    @Test
    fun wrapsNegativeDay() {
        assertEquals(R.string.zero_inbox_3, ZeroCopy.pick(ZeroKind.INBOX, -1))
    }
}
