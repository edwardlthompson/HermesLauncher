package org.hermeslauncher.app.wallpaper

import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.core.graphics.ColorUtils

class GradientLiveWallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine = DriftEngine()

    private inner class DriftEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val tick = object : Runnable {
            override fun run() {
                drawFrame()
                handler.postDelayed(this, 80L)
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
                val t = (System.currentTimeMillis() % 20_000L) / 20_000f
                val from = WallpaperTheme.primary(this@GradientLiveWallpaper)
                val to = WallpaperTheme.secondary(this@GradientLiveWallpaper)
                canvas.drawColor(ColorUtils.blendARGB(from, to, t))
                paint.color = WallpaperTheme.onPrimary(this@GradientLiveWallpaper)
                paint.alpha = 40
                canvas.drawCircle(
                    canvas.width * (0.3f + 0.4f * t),
                    canvas.height * (0.4f + 0.2f * (1f - t)),
                    canvas.width * 0.35f,
                    paint,
                )
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }
}
