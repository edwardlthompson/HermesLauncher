package org.hermeslauncher.app.vault

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class VaultItemType {
    MESSAGE,
    SYSTEM,
    MEDIA,
    OTHER,
}

@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey val id: String,
    val sbnKey: String,
    val packageName: String,
    val channelId: String?,
    val postedAt: Long,
    val removedAt: Long? = null,
    val type: VaultItemType,
    val priority: Int,
    val title: String?,
    val text: String?,
    val extrasJson: String?,
    val conversationTitle: String?,
    val pinned: Boolean = false,
    val archived: Boolean = false,
    val unread: Boolean = true,
    val contentStored: Boolean,
    val imagesStored: Boolean,
)

@Entity(
    tableName = "message_parts",
    foreignKeys = [
        ForeignKey(
            entity = VaultItem::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("itemId")],
)
data class MessagePart(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: String,
    val sender: String?,
    val text: String?,
    val timestamp: Long,
    val imageRef: String?,
)

@Entity(tableName = "app_store_policies")
data class AppStorePolicy(
    @PrimaryKey val packageName: String,
    val storeContent: Boolean = false,
    val storeImages: Boolean = false,
)
