package org.hermeslauncher.app.icons

import android.app.usage.UsageStatsManager
import android.content.Context

object UsageStatsQuery {
    fun rows(context: Context): List<UsageRow> {
        return runCatching {
            val usm = context.getSystemService(UsageStatsManager::class.java) ?: return emptyList()
            val end = System.currentTimeMillis()
            usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, end - UsageRanker.WINDOW_MS, end)
                .orEmpty()
                .map { UsageRow(it.packageName, it.lastTimeUsed, it.totalTimeInForeground) }
        }.getOrDefault(emptyList())
    }

    fun rank(context: Context, apps: List<LaunchableApp>, slotCount: Int): List<LaunchableApp> {
        return UsageRanker.rank(apps, rows(context), slotCount)
    }
}
