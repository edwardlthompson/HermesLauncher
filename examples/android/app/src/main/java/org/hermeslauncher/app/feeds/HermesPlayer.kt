package org.hermeslauncher.app.feeds

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HermesPlayer(context: Context) {
    private val playingState = MutableStateFlow(false)
    val playing: StateFlow<Boolean> = playingState

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
            }
        })
    }

    fun play(url: String) {
        if (url.isBlank()) {
            return
        }
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun toggle() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
        playingState.value = false
    }

    fun release() {
        player.release()
    }
}
