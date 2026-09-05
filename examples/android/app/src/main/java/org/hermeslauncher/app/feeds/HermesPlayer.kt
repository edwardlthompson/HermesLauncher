package org.hermeslauncher.app.feeds

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HermesPlayer(private val context: Context) {
    private val playingState = MutableStateFlow(false)
    val playing: StateFlow<Boolean> = playingState
    private val episodeState = MutableStateFlow<FeedItem?>(null)
    val episode: StateFlow<FeedItem?> = episodeState
    private val speedState = MutableStateFlow(1f)
    val speed: StateFlow<Float> = speedState
    private val sleepState = MutableStateFlow(0)
    val sleepMinutes: StateFlow<Int> = sleepState
    var onEnded: (FeedItem) -> Unit = {}
    var sleepUntilMs: Long? = null
        private set

    private val player: ExoPlayer = ExoPlayer.Builder(context.applicationContext).build().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true,
        )
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playingState.value = isPlaying
                publish()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    episodeState.value?.let(onEnded)
                }
            }
        })
    }

    fun play(url: String) {
        if (url.isBlank()) {
            return
        }
        play(FeedItem(id = url, feedTitle = "", title = "", enclosureUrl = url))
    }

    fun play(item: FeedItem, startMs: Long = 0L, localPath: String? = null) {
        val url = localPath ?: item.enclosureUrl ?: return
        if (url.isBlank()) {
            return
        }
        episodeState.value = item
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        if (startMs > 0L) {
            player.seekTo(startMs)
        }
        player.setPlaybackSpeed(speedState.value)
        player.play()
        PodcastService.start(context)
        publish()
    }

    fun pause() = player.pause()

    fun toggle() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
        playingState.value = false
        episodeState.value = null
        publish()
    }

    fun skipBy(deltaMs: Long) {
        val next = (player.currentPosition + deltaMs).coerceAtLeast(0L)
        player.seekTo(next)
    }

    fun seekTo(positionMs: Long) {
        val cap = player.duration.takeIf { it > 0L } ?: Long.MAX_VALUE
        player.seekTo(positionMs.coerceIn(0L, cap))
    }

    fun cycleSpeed() {
        val next = SPEEDS[(SPEEDS.indexOfFirst { it == speedState.value }.coerceAtLeast(0) + 1) % SPEEDS.size]
        speedState.value = next
        player.setPlaybackSpeed(next)
    }

    fun cycleSleep(nowMs: Long = System.currentTimeMillis()) {
        val next = SleepTimer.cycle(sleepState.value)
        sleepState.value = next
        sleepUntilMs = SleepTimer.deadline(nowMs, next)
    }

    fun tickSleep(nowMs: Long = System.currentTimeMillis()) {
        if (SleepTimer.expired(sleepUntilMs, nowMs)) {
            pause()
            sleepState.value = 0
            sleepUntilMs = null
        }
    }

    fun positionMs(): Long = player.currentPosition

    fun durationMs(): Long = player.duration.coerceAtLeast(0L)

    fun release() = player.release()

    private fun publish() {
        val title = episodeState.value?.title.orEmpty()
        PodcastService.instance?.publish(playingState.value, title)
    }

    companion object {
        val SPEEDS: List<Float> = listOf(0.8f, 1.0f, 1.2f, 1.5f, 2.0f)
    }
}
