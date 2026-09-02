package org.hermeslauncher.app.oem

import android.Manifest
import android.app.AppOpsManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object LivePermissions {
    fun snapshot(context: Context): PermissionSnapshot {
        val listeners = NotificationManagerCompat.getEnabledListenerPackages(context)
        val battery = context.getSystemService(PowerManager::class.java)
            ?.isIgnoringBatteryOptimizations(context.packageName)
            ?: true
        val home = if (Build.VERSION.SDK_INT >= 29) {
            context.getSystemService(RoleManager::class.java)
                ?.isRoleHeld(RoleManager.ROLE_HOME)
                ?: true
        } else {
            true
        }
        return PermissionSnapshot(
            notificationListenerEnabled = listeners.contains(context.packageName),
            batteryUnrestricted = battery,
            homeRoleHeld = home,
            mediaGranted = mediaGranted(context),
        )
    }

    fun mediaGranted(context: Context): Boolean {
        if (ContextCompat.checkSelfPermission(context, mediaPermission()) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return Build.VERSION.SDK_INT >= 34 &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
            ) == PackageManager.PERMISSION_GRANTED
    }

    fun mediaPermission(): String {
        return if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    fun listenerSettings(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    }

    fun batterySettings(packageName: String): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = android.net.Uri.parse("package:$packageName")
        }
    }

    fun homeRoleSettings(): Intent {
        return Intent(Settings.ACTION_HOME_SETTINGS)
    }

    fun usageGranted(context: Context): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java)
        val mode = appOps?.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun usageSettings(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }

    fun startSafe(context: Context, intent: Intent) {
        if (intent.resolveActivity(context.packageManager) != null) {
            runCatching { context.startActivity(intent) }
        }
    }
}
