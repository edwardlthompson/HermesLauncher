package dev.foss.goldenpath.crashcapture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PendingCrashTest {
    @Test
    fun encodeSanitizesBeforePersist() {
        val encoded = PendingCrashStore.encode(
            PendingCrash("boom ghp_abcdefghijklmnopqrstuvwxyz012345", "at C:\\Users\\Ada\\x.kt"),
        )
        assertFalse(encoded.contains("ghp_"))
        assertFalse(encoded.contains("Ada"))
        val decoded = PendingCrashStore.decode(encoded)
        assertTrue(decoded.message.contains("<redacted-secret>") || decoded.message.contains("boom"))
    }
}
