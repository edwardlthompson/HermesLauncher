package org.hermeslauncher.app.vault

import android.app.ActivityOptions
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.StatusBarNotification
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

data class ShadeAction(
    val title: String,
    val index: Int,
)

object ShadeBridge {
    const val TAG: String = "HermesVault"

    @Volatile
    var listener: HermesNotificationListener? = null

    private val live = ConcurrentHashMap<String, StatusBarNotification>()

    fun upsert(sbn: StatusBarNotification) {
        live[sbn.key] = sbn
    }

    fun drop(key: String) {
        live.remove(key)
    }

    fun replaceAll(notifications: Array<out StatusBarNotification>) {
        live.clear()
        notifications.forEach { live[it.key] = it }
    }

    fun dismiss(sbnKey: String) {
        if (sbnKey.isBlank()) {
            return
        }
        val svc = listener
        if (svc == null) {
            Log.w(TAG, "cancel skipped no listener")
            return
        }
        runCatching { svc.cancelNotification(sbnKey) }
            .onSuccess { Log.i(TAG, "cancel key=$sbnKey") }
            .onFailure { Log.w(TAG, "cancel fail key=$sbnKey") }
    }

    fun open(item: VaultItem?, context: Context) {
        if (item == null) {
            Log.w(TAG, "open miss item")
            return
        }
        open(item.sbnKey, context, item.packageName)
    }

    fun open(sbnKey: String, context: Context? = null, packageName: String = "") {
        val sbn = find(sbnKey)
        val pending = sbn?.notification?.contentIntent
        if (pending != null && sendPending(pending, context)) {
            Log.i(TAG, "open ok key=$sbnKey")
            val autoCancel = sbn.notification.flags and Notification.FLAG_AUTO_CANCEL != 0
            if (ShadePolicy.cancelAfterOpen(autoCancel, sbn.isOngoing)) {
                dismiss(sbnKey)
            }
            return
        }
        Log.w(TAG, "open miss key=$sbnKey pending=${pending != null}")
        launchPackage(context, sbn?.packageName ?: packageName)
    }

    fun actions(sbnKey: String): List<ShadeAction> {
        val acts = find(sbnKey)?.notification?.actions ?: return emptyList()
        return acts.mapIndexedNotNull { index, action ->
            val title = action.title?.toString()?.trim().orEmpty()
            if (title.isEmpty()) null else ShadeAction(title, index)
        }
    }

    fun runAction(sbnKey: String, index: Int) {
        val sbn = find(sbnKey)
        if (sbn == null) {
            Log.w(TAG, "action miss key=$sbnKey i=$index")
            return
        }
        val action = sbn.notification.actions?.getOrNull(index) ?: return
        if (!sendPending(action.actionIntent, listener)) {
            Log.w(TAG, "action fail key=$sbnKey i=$index")
            return
        }
        if (ShadePolicy.cancelAfterAction(hasRemoteInput(action))) {
            dismiss(sbnKey)
        }
    }

    private fun sendPending(pending: PendingIntent?, context: Context?): Boolean {
        if (pending == null) {
            return false
        }
        val sender = context ?: listener
        return runCatching {
            if (Build.VERSION.SDK_INT >= 34) {
                val opts = ActivityOptions.makeBasic()
                opts.setPendingIntentBackgroundActivityStartMode(
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED,
                )
                pending.send(sender, 0, null, null, null, null, opts.toBundle())
            } else {
                pending.send()
            }
        }.onFailure { Log.w(TAG, "send fail ${it.javaClass.simpleName}") }.isSuccess
    }

    private fun launchPackage(context: Context?, pkg: String) {
        if (context == null || pkg.isBlank()) {
            return
        }
        val launch = context.packageManager.getLaunchIntentForPackage(pkg) ?: return
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(launch) }
            .onSuccess { Log.i(TAG, "open launch pkg=$pkg") }
            .onFailure { Log.w(TAG, "open launch fail pkg=$pkg") }
    }

    private fun find(key: String): StatusBarNotification? {
        if (key.isBlank()) {
            return null
        }
        return live[key]
    }

    private fun hasRemoteInput(action: Notification.Action): Boolean {
        if (action.remoteInputs?.isNotEmpty() == true) {
            return true
        }
        return Build.VERSION.SDK_INT >= 26 && action.dataOnlyRemoteInputs?.isNotEmpty() == true
    }
}
