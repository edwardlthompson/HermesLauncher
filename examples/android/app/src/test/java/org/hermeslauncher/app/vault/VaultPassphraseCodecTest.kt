package org.hermeslauncher.app.vault

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import javax.crypto.KeyGenerator

class VaultPassphraseCodecTest {
    @Test
    fun wrapRoundTrip() {
        val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        val raw = ByteArray(32) { it.toByte() }
        val wrapped = VaultPassphraseCodec.wrap(key, raw)
        assertEquals(12 + raw.size + 16, wrapped.size)
        val restored = VaultPassphraseCodec.unwrap(key, wrapped)
        assertArrayEquals(raw, restored)
    }
}
