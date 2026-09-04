package org.hermeslauncher.app.icons

object HotseatPolicy {
    const val MIN_PAGES: Int = 1
    const val MAX_PAGES: Int = 4

    fun pageCount(raw: Int): Int = raw.coerceIn(MIN_PAGES, MAX_PAGES)

    fun clampPage(index: Int, pageCount: Int): Int {
        val pages = pageCount(pageCount)
        if (pages <= 1) {
            return 0
        }
        return index.coerceIn(0, pages - 1)
    }

    fun nestedSwipeKeepsWorkspace(workspaceBefore: Int, workspaceAfter: Int): Boolean {
        return workspaceBefore == workspaceAfter
    }
}
