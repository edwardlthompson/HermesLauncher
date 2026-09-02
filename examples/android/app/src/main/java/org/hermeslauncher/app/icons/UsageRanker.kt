package org.hermeslauncher.app.icons

data class UsageRow(
    val packageName: String,
    val lastTimeUsed: Long,
    val totalTimeInForeground: Long,
)

object UsageRanker {
    const val WINDOW_MS: Long = 7L * 24 * 60 * 60 * 1000

    fun rank(apps: List<LaunchableApp>, rows: List<UsageRow>, slotCount: Int): List<LaunchableApp> {
        val stats = merge(rows)
        return apps.distinctBy { it.packageName }
            .sortedWith(
                compareByDescending<LaunchableApp> { stats[it.packageName]?.lastTimeUsed ?: 0L }
                    .thenByDescending { stats[it.packageName]?.totalTimeInForeground ?: 0L }
                    .thenBy { it.label.lowercase() },
            )
            .take(slotCount.coerceAtLeast(0))
    }

    fun merge(rows: List<UsageRow>): Map<String, UsageRow> {
        return rows.groupBy { it.packageName }.mapValues { (pkg, list) ->
            UsageRow(
                packageName = pkg,
                lastTimeUsed = list.maxOf { it.lastTimeUsed },
                totalTimeInForeground = list.sumOf { it.totalTimeInForeground },
            )
        }
    }
}
