package org.hermeslauncher.app.feeds

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.podcastDataStore by preferencesDataStore(name = "podcast_player")
private val PROGRESS = stringPreferencesKey("progress")
private val QUEUE = stringPreferencesKey("queue")

class PodcastPlayerStore(private val context: Context) {
    suspend fun position(id: String): Long {
        return snapshotProgress()[id] ?: 0L
    }

    suspend fun snapshotProgress(): Map<String, Long> {
        return context.podcastDataStore.data.map { EpisodeProgress.decode(it[PROGRESS]) }.first()
    }

    suspend fun save(id: String, positionMs: Long) {
        if (id.isBlank()) {
            return
        }
        context.podcastDataStore.edit { prefs ->
            val next = EpisodeProgress.decode(prefs[PROGRESS]).toMutableMap()
            next[id] = positionMs
            prefs[PROGRESS] = EpisodeProgress.encode(next)
        }
    }

    suspend fun snapshotQueue(): PlayQueue {
        return context.podcastDataStore.data.map { PlayQueue.decode(it[QUEUE]) }.first()
    }

    suspend fun saveQueue(queue: PlayQueue) {
        context.podcastDataStore.edit { prefs ->
            prefs[QUEUE] = queue.encode()
        }
    }
}
