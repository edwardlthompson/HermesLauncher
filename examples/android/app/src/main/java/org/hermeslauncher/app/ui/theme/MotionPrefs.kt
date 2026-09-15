package org.hermeslauncher.app.ui.theme

import android.content.Context
import android.provider.Settings

object MotionPrefs {
    fun reduced(animatorDurationScale: Float): Boolean = animatorDurationScale == 0f

    fun scale(context: Context): Float = runCatching {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        )
    }.getOrDefault(1f)

    fun reduced(context: Context): Boolean = reduced(scale(context))

    fun animateScroll(reduced: Boolean): Boolean = !reduced

    fun animateSize(reduced: Boolean): Boolean = !reduced

    fun animateDismiss(reduced: Boolean): Boolean = !reduced
}
