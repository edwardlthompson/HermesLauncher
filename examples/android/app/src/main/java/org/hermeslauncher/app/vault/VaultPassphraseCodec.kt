package org.hermeslauncher.app.vault

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object VaultPassphraseCodec {
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128

    fun wrap(key: SecretKey, raw: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        require(iv.size == IV_BYTES) { "vault_iv_len" }
        return iv + cipher.doFinal(raw)
    }

    fun unwrap(key: SecretKey, blob: ByteArray): ByteArray {
        require(blob.size > IV_BYTES) { "vault_blob_short" }
        val iv = blob.copyOfRange(0, IV_BYTES)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        return cipher.doFinal(blob.copyOfRange(IV_BYTES, blob.size))
    }
}
