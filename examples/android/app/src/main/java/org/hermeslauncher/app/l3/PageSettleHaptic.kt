package org.hermeslauncher.app.l3

import android.content.Context
import com.android.launcher3.util.VibratorWrapper

object PageSettleHaptic {
    class Session {
        var userPaged: Boolean = false
        var lastSettled: Int = -1
        var snapImmediate: Boolean = false

        fun onMove() {
            userPaged = true
        }

        fun onSnap(immediate: Boolean) {
            snapImmediate = immediate
        }

        fun onEnd(context: Context, page: Int, dragLock: Boolean) {
            if (shouldTick(userPaged && !dragLock, snapImmediate, page, lastSettled)) {
                tick(context)
            }
            lastSettled = page
            userPaged = false
            snapImmediate = false
        }
    }

    fun shouldTick(
        userPaged: Boolean,
        immediate: Boolean,
        settledPage: Int,
        lastPage: Int,
    ): Boolean {
        if (!userPaged || immediate) {
            return false
        }
        if (lastPage < 0) {
            return false
        }
        return settledPage != lastPage
    }

    fun tick(context: Context) {
        runCatching {
            VibratorWrapper.INSTANCE.get(context).vibrate(VibratorWrapper.EFFECT_CLICK)
        }
    }
}
