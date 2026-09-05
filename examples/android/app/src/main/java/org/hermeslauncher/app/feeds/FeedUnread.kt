package org.hermeslauncher.app.feeds

object FeedUnread {
    fun count(records: List<ArticleRecord>): Int = records.count { !it.read }

    fun label(count: Int): String = if (count > 99) "99+" else count.toString()
}
