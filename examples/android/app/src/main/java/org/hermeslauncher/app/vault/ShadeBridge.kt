package org.hermeslauncher.app.vault

import android.app.Notification
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

    fun open(sbnKey: String) {
        val sbn = find(sbnKey)
        if (sbn == null) {
            Log.w(TAG, "open miss key=$sbnKey")
            return
        }
        val notification = sbn.notification
        runCatching { notification.contentIntent?.send() }
            .onFailure { Log.w(TAG, "open fail key=$sbnKey") }
        val autoCancel = notification.flags and Notification.FLAG_AUTO_CANCEL != 0
        if (ShadePolicy.cancelAfterOpen(autoCancel, sbn.isOngoing)) {
            dismiss(sbnKey)
        }
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
        runCatching { action.actionIntent?.send() }
            .onFailure { Log.w(TAG, "action fail key=$sbnKey i=$index") }
        if (ShadePolicy.cancelAfterAction(hasRemoteInput(action))) {
            dismiss(sbnKey)
        }
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
