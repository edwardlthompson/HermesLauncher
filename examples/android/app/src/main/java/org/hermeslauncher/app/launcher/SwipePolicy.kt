package org.hermeslauncher.app.launcher

/** Horizontal swipe belongs to the pager only — never to a feed card. */
enum class SwipeTarget {
    Card,
    Pager,
}

object SwipePolicy {
    fun consumesHorizontalSwipe(target: SwipeTarget): Boolean {
        return target == SwipeTarget.Pager
    }
}
