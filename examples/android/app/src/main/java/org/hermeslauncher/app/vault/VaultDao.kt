package org.hermeslauncher.app.vault

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPolicy(policy: AppStorePolicy)

    @Insert
    suspend fun insertPart(part: MessagePart)

    @Query("SELECT * FROM vault_items")
    suspend fun allItems(): List<VaultItem>

    @Query("SELECT * FROM app_store_policies")
    suspend fun allPolicies(): List<AppStorePolicy>

    @Query("SELECT * FROM message_parts")
    suspend fun allParts(): List<MessagePart>

    @Query("SELECT * FROM vault_items WHERE archived = 0 ORDER BY postedAt DESC")
    suspend fun visibleItems(): List<VaultItem>

    @Query("SELECT * FROM vault_items WHERE archived = 0 ORDER BY postedAt DESC")
    fun visibleItemsFlow(): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE archived = 1 ORDER BY postedAt DESC")
    fun archivedItemsFlow(): Flow<List<VaultItem>>

    @Query("SELECT * FROM message_parts WHERE itemId = :itemId ORDER BY timestamp ASC")
    suspend fun partsFor(itemId: String): List<MessagePart>

    @Query("DELETE FROM message_parts WHERE itemId = :itemId")
    suspend fun deleteParts(itemId: String)

    @Query("SELECT * FROM vault_items WHERE sbnKey = :key AND archived = 0")
    suspend fun visibleBySbnKey(key: String): List<VaultItem>

    @Query("SELECT * FROM vault_items WHERE id = :id LIMIT 1")
    suspend fun itemById(id: String): VaultItem?

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteItem(id: String)

    @Query("SELECT * FROM app_store_policies WHERE packageName = :packageName LIMIT 1")
    suspend fun policyFor(packageName: String): AppStorePolicy?

    @Update
    suspend fun updateItem(item: VaultItem)
}
