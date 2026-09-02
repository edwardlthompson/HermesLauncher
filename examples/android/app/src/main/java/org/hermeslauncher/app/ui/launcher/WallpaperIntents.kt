package org.hermeslauncher.app.ui.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.service.wallpaper.WallpaperService
import android.app.WallpaperManager
import android.widget.Toast
import org.hermeslauncher.app.R

object WallpaperIntents {
    val GOOGLE_PACKAGES: Set<String> = setOf(
        "com.google.android.apps.wallpaper",
        "com.google.android.apps.photos",
    )
    val AOSP_PACKAGES: List<String> = listOf(
        "com.android.wallpaper",
        "com.android.wallpaperpicker",
        "org.lineageos.wallpaper",
        "org.lineageos.backgrounds",
    )

    fun picker(): Intent {
        return Intent(Intent.ACTION_SET_WALLPAPER)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }

    fun picker(pm: PackageManager): Intent {
        val intent = picker()
        val preferred = preferredStatic(pm) ?: return intent
        if (preferred.packageName in GOOGLE_PACKAGES) {
            return intent
        }
        intent.component = preferred
        return intent
    }

    fun preferredStatic(pm: PackageManager): ComponentName? {
        val query = Intent(Intent.ACTION_SET_WALLPAPER)
        val hits = runCatching { pm.queryIntentActivities(query, 0) }.getOrDefault(emptyList())
        return hits.map { ComponentName(it.activityInfo.packageName, it.activityInfo.name) }
            .firstOrNull { it.packageName in AOSP_PACKAGES }
    }

    fun liveChooser(): Intent {
        return Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun changeLive(component: ComponentName): Intent {
        return Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun hermesLive(applicationId: String): List<ComponentName> {
        return listOf(
            ComponentName(applicationId, "org.hermeslauncher.app.wallpaper.GradientLiveWallpaper"),
            ComponentName(applicationId, "org.hermeslauncher.app.wallpaper.ClockLiveWallpaper"),
        )
    }

    fun pinsGoogle(intent: Intent): Boolean {
        val pkg = intent.`package` ?: intent.component?.packageName
        return pkg in GOOGLE_PACKAGES
    }

    fun start(context: Context): Boolean {
        val intent = picker(context.packageManager)
        if (pinsGoogle(intent)) return false
        if (intent.resolveActivity(context.packageManager) == null) return false
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun startLive(context: Context): Boolean {
        val chooser = liveChooser()
        if (chooser.resolveActivity(context.packageManager) == null) return false
        return runCatching {
            context.startActivity(chooser)
            true
        }.getOrDefault(false)
    }

    fun startOrToast(context: Context) {
        if (!start(context)) {
            Toast.makeText(context, R.string.wallpaper_picker_missing, Toast.LENGTH_SHORT).show()
        }
    }

    fun startLiveOrToast(context: Context) {
        if (!startLive(context)) {
            Toast.makeText(context, R.string.live_wallpaper_missing, Toast.LENGTH_SHORT).show()
        }
    }

    fun wallpaperServiceAction(): String = WallpaperService.SERVICE_INTERFACE
}
