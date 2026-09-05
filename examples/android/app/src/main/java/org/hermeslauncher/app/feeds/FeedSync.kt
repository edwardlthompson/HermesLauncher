package org.hermeslauncher.app.feeds

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.hermeslauncher.app.HermesApplication

object FeedSync {
    fun allowAuto(context: Context, prefs: ReaderSettings): Boolean {
        val net = snapshot(context)
        return FeedSyncPolicy.allowAuto(
            online = net.online,
            allowMobile = prefs.mobileData,
            cellular = net.cellular,
            metered = net.metered,
            onlyCharging = prefs.onlyWhenCharging,
            charging = net.charging,
        )
    }

    fun allowDownload(context: Context, prefs: ReaderSettings): Boolean {
        val net = snapshot(context)
        return FeedSyncPolicy.allowDownload(
            online = net.online,
            allowMobile = prefs.mobileData,
            cellular = net.cellular,
            metered = net.metered,
        )
    }

    fun allowImages(context: Context, prefs: ReaderSettings): Boolean {
        val net = snapshot(context)
        return FeedSyncPolicy.allowImages(net.online, prefs.imagePolicy, net.cellular, net.metered)
    }

    suspend fun loop(app: HermesApplication) {
        app.readerPrefs.settings.collectLatest { prefs ->
            val minutes = prefs.scanMinutes
            runCatching { FeedWork.enqueue(app, minutes) }
            app.feeds.expire()
            while (true) {
                val waitMs = if (minutes <= 0) 60L * 60_000L else minutes * 60_000L
                delay(waitMs)
                app.feeds.expire()
                if (FeedWork.loopShouldRefresh(minutes, FeedWork.registered) && allowAuto(app, prefs)) {
                    app.feeds.refresh()
                }
            }
        }
    }

    private fun snapshot(context: Context): NetSnap {
        return runCatching {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val caps = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
            val battery = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            NetSnap(
                online = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true,
                cellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true,
                metered = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) != true,
                charging = battery.isCharging,
            )
        }.getOrDefault(NetSnap())
    }

    private data class NetSnap(
        val online: Boolean = false,
        val cellular: Boolean = false,
        val metered: Boolean = true,
        val charging: Boolean = false,
    )
}
