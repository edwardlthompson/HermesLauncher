package org.hermeslauncher.app.feeds

import android.content.Context
import android.util.Log
import org.hermeslauncher.app.HermesApplication

/** IO-bound refresh body: one persist, throttled UI updates, prefetch after encode. */
object FeedRefresh {
    private const val TAG = "HermesFeeds"

    suspend fun run(
        context: Context,
        store: FeedStore,
        articles: ArticleStore,
        publishItems: (List<FeedItem>) -> Unit,
        publishFailed: (Boolean) -> Unit,
    ) {
        ArticleThumb.purgeLegacyThumbs(context.filesDir)
        store.seedIfNeeded()
        val subs = store.snapshot().sortedBy { sub -> if (sub.kind == SubKind.PODCAST) 0 else 1 }
        val urls = subs.map { FeedDiscover.canonicalize(it.url) }.distinct().filter { it.isNotBlank() }
        if (urls.toSet() != store.snapshot().map { it.url }.toSet()) {
            store.replaceAll(urls)
        }
        val now = System.currentTimeMillis()
        val before = FeedFilter.purge(articles.snapshot(), now)
        val errors = mutableMapOf<String, String?>()
        val xmls = mutableMapOf<String, String?>()
        val fetched = mutableListOf<FeedItem>()
        var shown = before
        var feedIndex = 0
        for (url in urls) {
            val outcome = FeedFetch.items(url)
            errors[url] = outcome.error
            xmls[url] = outcome.xml
            fetched += FeedPersistPolicy.capFeedItems(outcome.items)
            shown = FeedFilter.merge(before, MixPolicy.withinWindow(fetched, now), now)
            feedIndex += 1
            if (feedIndex % FeedPersistPolicy.PROGRESS_EVERY_FEEDS == 0 || feedIndex == urls.size) {
                publishItems(shown.map { it.item })
            }
        }
        for (sub in store.snapshot()) {
            var next = sub
            if (sub.url in errors) {
                next = next.copy(lastError = errors[sub.url])
            }
            next = FeedKindSync.afterFetch(next, xmls[sub.url])
            if (next != sub) {
                store.upsert(next)
            }
        }
        val filled = ArticleEnrich.fillRecords(shown)
        val slim = stripHtmlToDisk(context, filled)
        FeedFull.deleteIds(context.filesDir, FeedFilter.droppedIds(before, slim))
        articles.replaceAll(slim)
        publishItems(slim.map { it.item })
        publishFailed(urls.isNotEmpty() && fetched.isEmpty())
        FeedRefreshClock.markRefreshed(context)
        prefetchAfter(context, store, before, slim)
        Log.i(TAG, "refresh urls=${urls.size} items=${slim.size}")
    }

    fun stripHtmlToDisk(context: Context, records: List<ArticleRecord>): List<ArticleRecord> {
        return records.map { rec ->
            val html = rec.item.html
            if (!html.isNullOrBlank()) {
                FeedFull.save(context.filesDir, rec.item.id, html)
            }
            if (html == null) rec else rec.copy(item = rec.item.copy(html = null))
        }
    }

    private suspend fun prefetchAfter(
        context: Context,
        store: FeedStore,
        before: List<ArticleRecord>,
        slim: List<ArticleRecord>,
    ) {
        val prefs = (context.applicationContext as? HermesApplication)?.readerPrefs?.settingsFirst()
            ?: ReaderSettings()
        val live = store.snapshot()
        runCatching {
            if (FeedSync.allowDownload(context, prefs)) {
                val prefetchSet = live.filter { it.prefetch }.map { it.url }.toSet()
                FeedFull.prefetch(
                    context.filesDir,
                    FeedPersistPolicy.newestUnread(slim),
                    true,
                    prefetchSet,
                )
                PodcastAudio.prefetch(
                    context.filesDir,
                    slim,
                    live,
                    FeedSync.allowImages(context, prefs),
                )
            }
        }.onFailure { Log.w(TAG, "prefetch failed after persist", it) }
        FeedNotify.post(
            context,
            FeedNotify.newUnread(before, slim, live.filter { it.notify }.map { it.url }.toSet()),
            live,
        )
    }
}
