package org.hermeslauncher.app.ui.launcher

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.FeedQuery
import org.hermeslauncher.app.feeds.MiniPlayerState
import org.hermeslauncher.app.feeds.PodcastPlayback
import org.hermeslauncher.app.feeds.ReaderSettings
import org.hermeslauncher.app.feeds.SubKind
import org.hermeslauncher.app.feeds.SubKindFilter
import org.hermeslauncher.app.l3.L3GestureHost
import org.hermeslauncher.app.ui.player.FeedReader
import org.hermeslauncher.app.ui.player.MiniPlayerBar

@Composable
fun HermesPodcastsPage(
    onLongPressHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    val records by app.feeds.articleRows.collectAsStateWithLifecycle(emptyList())
    val refreshFailed by app.feeds.refreshFailed.collectAsStateWithLifecycle(false)
    val refreshing by app.feeds.refreshing.collectAsStateWithLifecycle(false)
    val prefs by app.readerPrefs.settings.collectAsStateWithLifecycle(ReaderSettings())
    val subs by app.feedStore.subs.collectAsStateWithLifecycle(emptyList())
    val playing by app.player.playing.collectAsStateWithLifecycle(false)
    val episode by app.player.episode.collectAsStateWithLifecycle(null)
    val speed by app.player.speed.collectAsStateWithLifecycle(1f)
    val sleep by app.player.sleepMinutes.collectAsStateWithLifecycle(0)
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf(FeedQuery(newestFirst = prefs.newestFirst, chip = prefs.chip)) }
    var notesId by remember { mutableStateOf<String?>(null) }
    var positionMs by remember { mutableStateOf(0L) }
    var durationMs by remember { mutableStateOf(0L) }
    val pageRecords = remember(records, subs) { SubKindFilter.records(records, subs, SubKind.PODCAST) }
    val notes = pageRecords.firstOrNull { it.item.id == notesId }
    val barItem = episode ?: notes?.item
    LaunchedEffect(barItem?.id, playing) {
        while (barItem != null) {
            positionMs = app.player.positionMs()
            durationMs = app.player.durationMs()
            if (playing) {
                app.player.tickSleep()
                PodcastPlayback.saveResume(app)
            }
            delay(if (playing) 250L else 500L)
        }
        positionMs = 0L
        durationMs = 0L
    }
    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                if (notes != null) {
                    FeedReader(
                        item = notes.item,
                        starred = notes.starred,
                        onClose = { notesId = null },
                        onStar = { scope.launch { app.feeds.toggleStar(notes.item.id) } },
                        onUnread = { scope.launch { app.feeds.markUnread(notes.item.id) } },
                        onViewed = { },
                        downloadImages = false,
                        bodyScale = prefs.bodyScale,
                        onBodyScale = { scale -> scope.launch { app.readerPrefs.setBodyScale(scale) } },
                    )
                } else {
                    FeedsPage(
                        records = pageRecords,
                        onPlay = { item -> scope.launch { PodcastPlayback.play(app, item) } },
                        onPlayNext = { item -> scope.launch { PodcastPlayback.playNext(app, item.id) } },
                        onLongPressHome = onLongPressHome,
                        onDoubleTapHome = { L3GestureHost.onDoubleTap(com.android.launcher3.Launcher.getLauncher(context)) },
                        onAddFeed = { url -> app.feeds.addFromLink(url, SubKind.PODCAST) },
                        onOpen = { rec -> notesId = rec.item.id },
                        query = query,
                        onQuery = { query = it },
                        refreshFailed = refreshFailed,
                        onRetry = { scope.launch { app.feeds.refresh() } },
                        onRefresh = { scope.launch { app.feeds.refresh() } },
                        refreshing = refreshing,
                        showThumbs = prefs.showThumbs,
                        downloadThumbs = false,
                        onMarkAllRead = { scope.launch { app.feeds.markAllRead(pageRecords.map { it.item.id }.toSet()) } },
                        lastError = subs.filter { it.kind == SubKind.PODCAST }.firstOrNull { !it.lastError.isNullOrBlank() }?.lastError,
                        tags = SubKindFilter.tags(subs, SubKind.PODCAST),
                        onStar = { id -> scope.launch { app.feeds.toggleStar(id) } },
                        onToggleRead = { rec ->
                            scope.launch {
                                if (rec.read) app.feeds.markUnread(rec.item.id) else app.feeds.markRead(rec.item.id)
                            }
                        },
                        onShare = { rec ->
                            rec.item.articleUrl()?.let { url ->
                                org.hermeslauncher.app.feeds.FeedShare.intent(rec.item.title, url)?.let { send ->
                                    context.startActivity(Intent.createChooser(send, context.getString(R.string.feed_reader_share)))
                                }
                            }
                        },
                        emptyKind = org.hermeslauncher.app.ui.inbox.ZeroKind.PODCAST,
                        modifier = Modifier
                            .fillMaxSize()
                            .semantics {
                                contentDescription = context.getString(R.string.launcher_page_podcasts)
                            },
                    )
                }
            }
            MiniPlayerBar(
                state = MiniPlayerState(episode = barItem, playing = playing),
                speed = speed,
                sleepMinutes = sleep,
                extras = true,
                positionMs = positionMs,
                durationMs = durationMs,
                onSeek = { ms ->
                    positionMs = ms
                    app.player.seekTo(ms)
                },
                onToggle = {
                    val item = app.player.episode.value ?: notes?.item
                    if (item != null && app.player.episode.value == null) {
                        scope.launch { PodcastPlayback.play(app, item) }
                    } else {
                        app.player.toggle()
                    }
                },
                onStop = { app.player.stop() },
                onSkipBack = { app.player.skipBy(-10_000L) },
                onSkipForward = { app.player.skipBy(30_000L) },
                onCycleSpeed = { app.player.cycleSpeed() },
                onCycleSleep = { app.player.cycleSleep() },
                onNext = {
                    scope.launch {
                        PodcastPlayback.saveResume(app)
                        val (queue, nextId) = app.podcastStore.snapshotQueue().next()
                        app.podcastStore.saveQueue(queue)
                        nextId?.let { app.feeds.itemById(it) }?.let { PodcastPlayback.play(app, it) }
                    }
                },
            )
        }
    }
}
