package org.hermeslauncher.app.vault

data class PostedMessagePart(
    val sender: String? = null,
    val text: String? = null,
    val timestamp: Long = 0,
)

/** Fixture-friendly listener input. Adapters map StatusBarNotification later. */
data class PostedNotification(
    val sbnKey: String,
    val packageName: String,
    val channelId: String? = null,
    val postedAt: Long,
    val title: String? = null,
    val text: String? = null,
    val extrasJson: String? = null,
    val conversationTitle: String? = null,
    val type: VaultItemType = VaultItemType.OTHER,
    val priority: Int = 0,
    val imageByteSize: Long = 0,
    val imageBytes: ByteArray = byteArrayOf(),
    val messageParts: List<PostedMessagePart> = emptyList(),
    val ongoing: Boolean = false,
)
