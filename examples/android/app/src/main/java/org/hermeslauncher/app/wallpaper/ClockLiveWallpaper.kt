package org.hermeslauncher.app.wallpaper

import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.text.TextPaint
import android.view.SurfaceHolder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClockLiveWallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine = ClockEngine()

    private inner class ClockEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        private val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
        }
        private val tick = object : Runnable {
            override fun run() {
                drawFrame()
                handler.postDelayed(this, 1_000L)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            handler.removeCallbacks(tick)
            if (visible) {
                handler.post(tick)
            }
        }

        override fun onDestroy() {
            handler.removeCallbacks(tick)
            super.onDestroy()
        }

        private fun drawFrame() {
            val holder: SurfaceHolder = surfaceHolder
            val canvas: Canvas = holder.lockCanvas() ?: return
            try {
                canvas.drawColor(WallpaperTheme.scrim(this@ClockLiveWallpaper))
                paint.color = WallpaperTheme.onBackground(this@ClockLiveWallpaper)
                paint.textSize = canvas.width * 0.18f
                val text = timeFormat.format(Date())
                canvas.drawText(text, canvas.width / 2f, canvas.height / 2f, paint)
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }
}
