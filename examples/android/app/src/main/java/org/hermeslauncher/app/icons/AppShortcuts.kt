package org.hermeslauncher.app.icons

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Process

object AppShortcuts {
    fun list(context: Context, packageName: String): List<ShortcutInfo> {
        if (packageName.isBlank()) {
            return emptyList()
        }
        return runCatching {
            val launcher = context.getSystemService(LauncherApps::class.java) ?: return emptyList()
            if (!launcher.hasShortcutHostPermission()) {
                return emptyList()
            }
            val query = LauncherApps.ShortcutQuery()
                .setPackage(packageName)
                .setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED,
                )
            launcher.getShortcuts(query, Process.myUserHandle()).orEmpty()
        }.getOrDefault(emptyList())
    }

    fun start(context: Context, shortcut: ShortcutInfo) {
        runCatching {
            context.getSystemService(LauncherApps::class.java)?.startShortcut(
                shortcut.`package`,
                shortcut.id,
                null,
                null,
                Process.myUserHandle(),
            )
        }
    }
}
