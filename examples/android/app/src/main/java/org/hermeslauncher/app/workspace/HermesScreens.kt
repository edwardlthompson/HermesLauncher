package org.hermeslauncher.app.workspace

/** Reserved Launcher3 screen IDs for Podcasts, News, and Inbox. */
object HermesScreens {
    const val PODCASTS: Int = -300
    const val NEWS: Int = -301
    const val INBOX: Int = -302

    fun isReserved(screenId: Int): Boolean {
        return screenId == PODCASTS || screenId == NEWS || screenId == INBOX
    }

    fun canDrop(screenId: Int): Boolean {
        return !isReserved(screenId)
    }

    fun homePageIndex(pageCount: Int): Int {
        if (pageCount <= 1) {
            return 0
        }
        if (pageCount == 2) {
            return 1
        }
        return 2
    }
}
