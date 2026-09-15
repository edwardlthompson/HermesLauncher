package org.hermeslauncher.app.vault

import android.content.ComponentName
import android.content.Context
import android.service.notification.NotificationListenerService

object InboxRefresh {
    fun listenerName(): String = HermesNotificationListener::class.java.name

    fun rebind(context: Context) {
        NotificationListenerService.requestRebind(
            ComponentName(context, HermesNotificationListener::class.java),
        )
    }
}
