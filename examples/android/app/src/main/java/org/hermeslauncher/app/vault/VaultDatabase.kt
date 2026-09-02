package org.hermeslauncher.app.vault

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [VaultItem::class, MessagePart::class, AppStorePolicy::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(VaultConverters::class)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultDao(): VaultDao
}
