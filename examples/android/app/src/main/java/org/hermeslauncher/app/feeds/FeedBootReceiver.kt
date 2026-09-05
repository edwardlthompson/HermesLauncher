package org.hermeslauncher.app.feeds

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FeedBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }
        runCatching { FeedWork.enqueue(context, 60) }
    }
}
