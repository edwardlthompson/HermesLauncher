package org.hermeslauncher.app.feeds

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication

object PodcastPlayback {
    fun bind(app: HermesApplication) {
        app.player.onEnded = { item ->
            app.vaultScope.launch(Dispatchers.Main.immediate) { finish(app, item) }
        }
    }

    suspend fun play(app: HermesApplication, item: FeedItem) {
        val start = app.podcastStore.position(item.id)
        val path = PodcastAudio.playUri(app.filesDir, item)
        app.player.play(item, start, path)
    }

    suspend fun playNext(app: HermesApplication, id: String) {
        val queue = app.podcastStore.snapshotQueue().enqueue(id)
        app.podcastStore.saveQueue(queue)
    }

    suspend fun finish(app: HermesApplication, item: FeedItem) {
        app.feeds.markRead(item.id)
        app.podcastStore.save(item.id, 0L)
        val (queue, nextId) = app.podcastStore.snapshotQueue().next()
        app.podcastStore.saveQueue(queue)
        val next = nextId?.let { app.feeds.itemById(it) }
        if (next != null) {
            play(app, next)
        }
    }

    suspend fun saveResume(app: HermesApplication) {
        val item = app.player.episode.value ?: return
        app.podcastStore.save(item.id, app.player.positionMs())
        val duration = app.player.durationMs()
        if (EpisodeProgress.played(app.player.positionMs(), duration)) {
            app.feeds.markRead(item.id)
        }
    }
}
