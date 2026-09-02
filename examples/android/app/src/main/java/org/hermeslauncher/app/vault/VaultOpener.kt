package org.hermeslauncher.app.vault

import android.content.Context
import android.util.Log
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

sealed class VaultOpenResult {
    data class Ok(val db: VaultDatabase) : VaultOpenResult()
    data class Rebuild(val db: VaultDatabase) : VaultOpenResult()
}

object VaultOpener {
    const val PLAIN_NAME: String = "hermes-vault.db"
    const val CIPHER_NAME: String = "hermes-vault-cipher.db"

    fun open(context: Context): VaultOpenResult {
        return openCipher(context).getOrElse { first ->
            Log.w(VaultImageStore.TAG, "vault open failed, wiping", first)
            wipe(context)
            openCipher(context).getOrElse { second ->
                Log.e(VaultImageStore.TAG, "vault file recover failed", second)
                VaultOpenResult.Rebuild(memoryDb(context))
            }
        }
    }

    private fun openCipher(context: Context): Result<VaultOpenResult.Ok> {
        return runCatching {
            System.loadLibrary("sqlcipher")
            val passphrase = VaultPassphrase(context).loadOrCreate()
            migratePlaintext(context, passphrase)
            val db = cipherDb(context, passphrase)
            db.query("SELECT 1", null).use { cursor ->
                check(cursor.moveToFirst()) { "vault_probe" }
            }
            Log.i(VaultImageStore.TAG, "vault open ok")
            VaultOpenResult.Ok(db)
        }
    }

    private fun cipherDb(context: Context, passphrase: ByteArray): VaultDatabase {
        val factory = SupportOpenHelperFactory(passphrase)
        return Room.databaseBuilder(context, VaultDatabase::class.java, CIPHER_NAME)
            .openHelperFactory(factory)
            .allowMainThreadQueries()
            .build()
    }

    private fun memoryDb(context: Context): VaultDatabase {
        return Room.inMemoryDatabaseBuilder(context, VaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    private fun migratePlaintext(context: Context, passphrase: ByteArray) {
        val plainFile = context.getDatabasePath(PLAIN_NAME)
        val cipherFile = context.getDatabasePath(CIPHER_NAME)
        if (!plainFile.exists() || cipherFile.exists()) {
            return
        }
        val src = Room.databaseBuilder(context, VaultDatabase::class.java, PLAIN_NAME).build()
        val dest = cipherDb(context, passphrase)
        runBlocking {
            val dao = src.vaultDao()
            val out = dest.vaultDao()
            dao.allPolicies().forEach { out.insertPolicy(it) }
            dao.allItems().forEach { out.insertItem(it) }
            dao.allParts().forEach { out.insertPart(it) }
        }
        src.close()
        dest.close()
        context.deleteDatabase(PLAIN_NAME)
        Log.i(VaultImageStore.TAG, "migrated plaintext vault")
    }

    private fun wipe(context: Context) {
        context.deleteDatabase(PLAIN_NAME)
        context.deleteDatabase(CIPHER_NAME)
        runCatching { context.noBackupFilesDir.resolve("vault.pass").delete() }
    }
}
