package org.hermeslauncher.app.oem

import android.content.Context
import org.hermeslauncher.app.R

enum class GrantKind {
    LISTENER,
    BATTERY,
    HOME,
    MEDIA,
    USAGE,
    POST,
}

object GrantCatalog {
    fun granted(kind: GrantKind, snap: PermissionSnapshot): Boolean = when (kind) {
        GrantKind.LISTENER -> snap.notificationListenerEnabled
        GrantKind.BATTERY -> snap.batteryUnrestricted
        GrantKind.HOME -> snap.homeRoleHeld
        GrantKind.MEDIA -> snap.mediaGranted
        GrantKind.USAGE -> snap.usageGranted
        GrantKind.POST -> snap.postNotificationsGranted
    }

    fun titleRes(kind: GrantKind): Int = when (kind) {
        GrantKind.LISTENER -> R.string.grant_listener_title
        GrantKind.BATTERY -> R.string.grant_battery_title
        GrantKind.HOME -> R.string.grant_home_title
        GrantKind.MEDIA -> R.string.grant_media_title
        GrantKind.USAGE -> R.string.grant_usage_title
        GrantKind.POST -> R.string.grant_post_title
    }

    fun bodyRes(kind: GrantKind): Int = when (kind) {
        GrantKind.LISTENER -> R.string.grant_listener_body
        GrantKind.BATTERY -> R.string.grant_battery_body
        GrantKind.HOME -> R.string.grant_home_body
        GrantKind.MEDIA -> R.string.grant_media_body
        GrantKind.USAGE -> R.string.grant_usage_body
        GrantKind.POST -> R.string.grant_post_body
    }

    fun open(context: Context, kind: GrantKind) {
        when (kind) {
            GrantKind.LISTENER -> LivePermissions.startSafe(context, LivePermissions.listenerSettings())
            GrantKind.BATTERY -> LivePermissions.startSafe(
                context,
                LivePermissions.batterySettings(context.packageName),
            )
            GrantKind.HOME -> LivePermissions.startSafe(context, LivePermissions.homeRoleSettings())
            GrantKind.MEDIA -> LivePermissions.requestMedia(context)
            GrantKind.USAGE -> LivePermissions.startSafe(context, LivePermissions.usageSettings())
            GrantKind.POST -> LivePermissions.requestPost(context)
        }
    }
}
