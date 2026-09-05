package org.hermeslauncher.app.ui.inbox

import org.hermeslauncher.app.R

enum class ZeroKind {
    INBOX,
    NEWS,
    PODCAST,
}

object ZeroCopy {
    fun pick(kind: ZeroKind, day: Long): Int {
        val pool = when (kind) {
            ZeroKind.INBOX -> intArrayOf(
                R.string.zero_inbox_1,
                R.string.zero_inbox_2,
                R.string.zero_inbox_3,
            )
            ZeroKind.NEWS -> intArrayOf(
                R.string.zero_news_1,
                R.string.zero_news_2,
                R.string.zero_news_3,
            )
            ZeroKind.PODCAST -> intArrayOf(
                R.string.zero_podcast_1,
                R.string.zero_podcast_2,
                R.string.zero_podcast_3,
            )
        }
        val idx = (day % pool.size).toInt()
        return pool[if (idx < 0) idx + pool.size else idx]
    }
}
