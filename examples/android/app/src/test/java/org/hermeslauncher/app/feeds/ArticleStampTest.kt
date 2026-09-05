package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleStampTest {
    @Test
    fun blankWhenMissing() {
        assertEquals("", ArticleStamp.format(0))
        assertEquals("", ArticleStamp.format(-1))
    }

    @Test
    fun abbreviatedYyMmDd() {
        val stamp = ArticleStamp.format(1_700_000_000_000L)
        assertTrue(stamp.matches(Regex("""\d{2}/\d{2}/\d{2}""")))
    }
}
