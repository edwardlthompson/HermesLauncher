package org.hermeslauncher.app.l3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.HermesLauncherActivity
import org.hermeslauncher.app.widgets.WidgetHostTick

/** TIME_TICK poke for Launcher3-hosted widgets (Compose MainActivity is not HOME). */
object L3WidgetTick {
    private var receiver: BroadcastReceiver? = null

    fun attach(activity: HermesLauncherActivity) {
        if (receiver == null) {
            val tick = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == Intent.ACTION_TIME_TICK) {
                        poke(activity)
                    }
                }
            }
            ContextCompat.registerReceiver(
                activity,
                tick,
                IntentFilter(Intent.ACTION_TIME_TICK),
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            receiver = tick
        }
        poke(activity)
    }

    fun detach(activity: HermesLauncherActivity) {
        val tick = receiver ?: return
        runCatching { activity.unregisterReceiver(tick) }
        receiver = null
    }

    fun ids(l3: IntArray, compose: IntArray): IntArray = WidgetHostTick.merge(l3, compose)

    fun poke(activity: HermesLauncherActivity) {
        val l3 = runCatching { activity.appWidgetHolder.appWidgetIds }.getOrDefault(intArrayOf())
        val compose = runCatching {
            (activity.application as HermesApplication).widgetHost.appWidgetIds
        }.getOrDefault(intArrayOf())
        WidgetHostTick.poke(activity, ids(l3, compose))
    }
}
