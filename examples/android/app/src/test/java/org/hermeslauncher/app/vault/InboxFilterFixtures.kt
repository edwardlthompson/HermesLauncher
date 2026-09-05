package org.hermeslauncher.app.vault

internal fun filterSample(
    id: String,
    type: VaultItemType,
    unread: Boolean,
    pinned: Boolean,
    title: String,
    text: String,
    pkg: String = "com.chat",
    postedAt: Long = 1L,
    archived: Boolean = false,
    conversationTitle: String? = null,
): VaultItem {
    return VaultItem(
        id = id,
        sbnKey = id,
        packageName = pkg,
        channelId = null,
        postedAt = postedAt,
        type = type,
        priority = 0,
        title = title,
        text = text,
        extrasJson = null,
        conversationTitle = conversationTitle,
        pinned = pinned,
        unread = unread,
        archived = archived,
        contentStored = true,
        imagesStored = false,
    )
}
