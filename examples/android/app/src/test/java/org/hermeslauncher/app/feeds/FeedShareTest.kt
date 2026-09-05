package org.hermeslauncher.app.feeds

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class FeedShareTest {
    @Test
    fun intentSendsTitleAndUrl() {
        val intent = FeedShare.intent("Story", "https://example.com/a")
        requireNotNull(intent)
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("text/plain", intent.type)
        assertEquals("Story", intent.getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals("Story\nhttps://example.com/a", intent.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    fun intentNullWhenUrlNotHttp() {
        assertNull(FeedShare.intent("X", "javascript:alert(1)"))
        assertNull(FeedShare.intent("X", ""))
    }
}
