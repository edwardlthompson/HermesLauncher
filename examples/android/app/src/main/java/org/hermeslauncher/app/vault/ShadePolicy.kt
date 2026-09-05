package org.hermeslauncher.app.vault

/** When Hermes should also clear the status-bar notification. */
object ShadePolicy {
    fun hasSubject(title: String?, conversationTitle: String?): Boolean {
        return !title.isNullOrBlank() || !conversationTitle.isNullOrBlank()
    }

    fun shouldPresent(
        groupSummary: Boolean,
        title: String?,
        conversationTitle: String?,
    ): Boolean {
        return !groupSummary && hasSubject(title, conversationTitle)
    }

    fun cancelAfterOpen(autoCancel: Boolean, ongoing: Boolean): Boolean {
        return autoCancel && !ongoing
    }

    fun cancelAfterAction(hasRemoteInput: Boolean): Boolean {
        return !hasRemoteInput
    }
}
