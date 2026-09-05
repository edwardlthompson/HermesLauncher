package org.hermeslauncher.app.feeds

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.HermesLauncherActivity
import org.hermeslauncher.app.R

class PodcastService : Service() {
    private var session: MediaSession? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        val player = (application as HermesApplication).player
        val media = MediaSession(this, "hermes-podcasts").apply {
            setCallback(
                object : MediaSession.Callback() {
                    override fun onPlay() = player.toggle()
                    override fun onPause() = player.pause()
                    override fun onStop() = player.stop()
                    override fun onSkipToNext() = player.skipBy(30_000L)
                    override fun onSkipToPrevious() = player.skipBy(-10_000L)
                    override fun onFastForward() = player.skipBy(30_000L)
                    override fun onRewind() = player.skipBy(-10_000L)
                },
            )
            isActive = true
        }
        session = media
        startNow()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startNow()
        return START_STICKY
    }

    override fun onDestroy() {
        session?.release()
        session = null
        instance = null
        super.onDestroy()
    }

    fun publish(playing: Boolean, title: String) {
        val state = PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_STOP or
                    PlaybackState.ACTION_SKIP_TO_NEXT or PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackState.ACTION_FAST_FORWARD or PlaybackState.ACTION_REWIND,
            )
            .setState(
                if (playing) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED,
                PlaybackState.PLAYBACK_POSITION_UNKNOWN,
                1f,
            )
            .build()
        session?.setPlaybackState(state)
        startNow(title)
    }

    private fun startNow(title: String = getString(R.string.player_untitled)) {
        ensureChannel()
        val open = PendingIntent.getActivity(
            this,
            0,
            Intent(this, HermesLauncherActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val note = Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(getString(R.string.player_play))
            .setContentIntent(open)
            .setOngoing(true)
            .setStyle(Notification.MediaStyle().setMediaSession(session?.sessionToken))
            .build()
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTE_ID, note, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTE_ID, note)
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        val mgr = getSystemService(NotificationManager::class.java) ?: return
        if (mgr.getNotificationChannel(CHANNEL) != null) {
            return
        }
        mgr.createNotificationChannel(NotificationChannel(CHANNEL, getString(R.string.player_channel), NotificationManager.IMPORTANCE_LOW))
    }

    companion object {
        const val CHANNEL: String = "hermes_podcasts"
        const val NOTE_ID: Int = 7100
        @Volatile var instance: PodcastService? = null

        fun start(context: Context) {
            val intent = Intent(context, PodcastService::class.java)
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
