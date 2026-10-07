package org.hermeslauncher.app.feeds

import android.content.Intent
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.HermesWorkspace

/** Resolve feed-notification extras into a workspace page index. */
object FeedOpenRoute {
    fun accept(app: HermesApplication, intent: Intent?): SubKind? {
        val id = intent?.getStringExtra(FeedNotify.EXTRA_ARTICLE_ID) ?: return null
        val kind = FeedNotify.parseKind(intent.getStringExtra(FeedNotify.EXTRA_SUB_KIND))
        app.pendingFeedOpen.value = PendingFeedOpen(id, kind)
        return kind
    }

    fun pageIndex(ws: HermesWorkspace?, kind: SubKind?): Int {
        return when (kind) {
            SubKind.PODCAST -> ws?.podcastIndex() ?: 0
            SubKind.NEWS -> ws?.newsIndex() ?: 1
            null -> ws?.homeIndex() ?: 0
        }
    }
}
