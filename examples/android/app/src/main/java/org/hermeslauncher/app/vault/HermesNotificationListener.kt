package org.hermeslauncher.app.vault

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication

class HermesNotificationListener : NotificationListenerService() {
    override fun onListenerConnected() {
        ShadeBridge.listener = this
        val active = runCatching { activeNotifications.orEmpty() }.getOrDefault(emptyArray())
        ShadeBridge.replaceAll(active)
        Log.i(VaultImageStore.TAG, "listener connected active=${active.size}")
        active.forEach { posted -> onNotificationPosted(posted) }
    }

    override fun onListenerDisconnected() {
        if (ShadeBridge.listener === this) {
            ShadeBridge.listener = null
            ShadeBridge.replaceAll(emptyArray())
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        ShadeBridge.upsert(sbn)
        val app = application as? HermesApplication ?: return
        app.vaultScope.launch {
            app.vault.onPosted(this@HermesNotificationListener, sbn)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        ShadeBridge.drop(sbn.key)
        val app = application as? HermesApplication ?: return
        app.vaultScope.launch {
            app.vault.archiveBySbnKey(sbn.key)
        }
    }
}
