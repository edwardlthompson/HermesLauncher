package org.hermeslauncher.app.launcher

/** Locked Sprint 1 drawer model. Search query is unused until package listing. */
data class DrawerState(
    val open: Boolean = false,
    val query: String = "",
) {
    fun opened(): DrawerState = copy(open = true)

    fun closed(): DrawerState = copy(open = false, query = "")

    fun withQuery(value: String): DrawerState = copy(query = value)
}
