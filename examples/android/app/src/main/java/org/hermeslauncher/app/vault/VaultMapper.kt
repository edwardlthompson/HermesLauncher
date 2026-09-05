package org.hermeslauncher.app.vault

object VaultMapper {
    fun decide(
        posted: PostedNotification,
        policy: AppStorePolicy?,
        ignoreOngoing: Boolean = false,
        storePhotos: Boolean = false,
    ): PersistDecision {
        if (ignoreOngoing && posted.ongoing) {
            return PersistDecision(PersistAction.SKIP, skipImageReason = "ongoing")
        }
        if (!ShadePolicy.shouldPresent(posted.groupSummary, posted.title, posted.conversationTitle)) {
            val reason = if (posted.groupSummary) "group_summary" else "no_subject"
            return PersistDecision(PersistAction.SKIP, skipImageReason = reason)
        }
        if (policy != null && !policy.storeContent) {
            return PersistDecision(PersistAction.SKIP)
        }
        val allowImages = storePhotos || (policy?.storeImages == true)
        if (!allowImages || posted.imageByteSize <= 0) {
            return PersistDecision(PersistAction.PERSIST_TEXT)
        }
        if (posted.imageByteSize > ImageLimits.ORIGINAL_MAX_BYTES) {
            return PersistDecision(
                action = PersistAction.PERSIST_TEXT,
                skipImageReason = "original_over_cap",
            )
        }
        return PersistDecision(PersistAction.PERSIST_TEXT_AND_IMAGES)
    }

    fun toItem(posted: PostedNotification, decision: PersistDecision): VaultItem? {
        if (decision.action == PersistAction.SKIP) {
            return null
        }
        val storeImages = decision.action == PersistAction.PERSIST_TEXT_AND_IMAGES
        return VaultItem(
            id = itemId(posted),
            sbnKey = posted.sbnKey,
            packageName = posted.packageName,
            channelId = posted.channelId,
            postedAt = posted.postedAt,
            type = posted.type,
            priority = posted.priority,
            title = posted.title,
            text = posted.text,
            extrasJson = posted.extrasJson,
            conversationTitle = posted.conversationTitle,
            contentStored = true,
            imagesStored = storeImages,
        )
    }

    fun itemId(posted: PostedNotification): String {
        return "${posted.sbnKey}:${posted.postedAt}"
    }

    fun toParts(posted: PostedNotification, itemId: String): List<MessagePart> {
        return posted.messageParts.map { part ->
            MessagePart(
                itemId = itemId,
                sender = part.sender,
                text = part.text,
                timestamp = part.timestamp,
                imageRef = null,
            )
        }
    }
}
