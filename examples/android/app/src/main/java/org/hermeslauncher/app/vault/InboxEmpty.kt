package org.hermeslauncher.app.vault

object InboxEmpty {
    enum class Kind { GRANT, FILTER, ZERO, CONTENT }

    fun kind(
        listenerOn: Boolean,
        itemsEmpty: Boolean,
        liveEmpty: Boolean,
        historyEmpty: Boolean,
        hasVisibleFeeds: Boolean,
    ): Kind {
        if (!liveEmpty || !historyEmpty || hasVisibleFeeds) {
            return Kind.CONTENT
        }
        if (!itemsEmpty) {
            return Kind.FILTER
        }
        if (!listenerOn) {
            return Kind.GRANT
        }
        return Kind.ZERO
    }
}
