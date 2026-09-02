package org.hermeslauncher.app.vault

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class VaultPassphrase(private val context: Context) {
    fun loadOrCreate(): ByteArray {
        val key = secretKey()
        val file = File(context.noBackupFilesDir, FILE_NAME)
        if (file.exists()) {
            val existing = runCatching {
                VaultPassphraseCodec.unwrap(key, file.readBytes())
            }.getOrNull()
            if (existing != null) {
                return existing
            }
            file.delete()
        }
        val raw = ByteArray(32).also { SecureRandom().nextBytes(it) }
        file.writeBytes(VaultPassphraseCodec.wrap(key, raw))
        return raw
    }

    private fun secretKey(): SecretKey {
        val ks = KeyStore.getInstance(STORE).apply { load(null) }
        val existing = ks.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existing != null) {
            return existing.secretKey
        }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, STORE)
        gen.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return gen.generateKey()
    }

    companion object {
        private const val STORE = "AndroidKeyStore"
        private const val ALIAS = "hermes_vault"
        private const val FILE_NAME = "vault.pass"
    }
}
