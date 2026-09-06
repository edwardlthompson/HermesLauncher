package org.hermeslauncher.app.icons

import java.util.concurrent.ConcurrentHashMap

object LaunchRecency {
    private val times = ConcurrentHashMap<String, Long>()

    @Volatile
    var persist: ((String, Long) -> Unit)? = null

    fun lastUsed(packageName: String): Long = times[packageName.trim()] ?: 0L

    fun snapshot(): Map<String, Long> = HashMap(times)

    fun rows(): List<UsageRow> = times.map { (pkg, at) ->
        UsageRow(pkg, lastTimeUsed = at, totalTimeInForeground = 0L)
    }

    fun touch(packageName: String, at: Long = System.currentTimeMillis()) {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) {
            return
        }
        times[pkg] = at
        persist?.invoke(pkg, at)
    }

    fun replace(all: Map<String, Long>) {
        times.clear()
        all.forEach { (pkg, at) ->
            val key = pkg.trim()
            if (key.isNotEmpty() && at > 0L) {
                times[key] = at
            }
        }
    }

    fun mergeUsage(rows: List<UsageRow>) {
        rows.forEach { row ->
            val pkg = row.packageName.trim()
            if (pkg.isEmpty()) {
                return@forEach
            }
            val current = times[pkg] ?: 0L
            if (row.lastTimeUsed > current) {
                times[pkg] = row.lastTimeUsed
            }
        }
    }
}
