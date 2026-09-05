package org.hermeslauncher.app.l3

import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.android.launcher3.Launcher
import org.hermeslauncher.app.launcher.GestureMap
import org.hermeslauncher.app.launcher.GestureSlot
import org.hermeslauncher.app.launcher.LauncherAction
import kotlin.math.abs
import kotlin.math.ln

/** Desktop pinch runs the Gestures → Pinch action. */
class L3Pinch(context: Context) {
    private var span = 1f
    private val detector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                span = 1f
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                span *= detector.scaleFactor
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                if (abs(ln(span.toDouble())) < 0.18) {
                    return
                }
                val launcher = Launcher.getLauncher(context)
                val action = GestureMap.action(GestureSlot.PINCH, L3Caches.gestureMap)
                if (action != LauncherAction.NONE) {
                    L3GestureHost.run(launcher, action)
                }
            }
        },
    )

    fun onTouch(ev: MotionEvent) {
        detector.onTouchEvent(ev)
    }
}
