package org.hermeslauncher.app.feeds

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FeedRefreshGateTest {
    @Test
    fun secondCallerSkipsWhileBusy() = runBlocking {
        val started = async {
            FeedRefreshGate.runOrSkip {
                delay(80)
                "first"
            }
        }
        delay(10)
        val second = FeedRefreshGate.runOrSkip { "second" }
        assertNull(second)
        assertEquals("first", started.await())
    }
}
