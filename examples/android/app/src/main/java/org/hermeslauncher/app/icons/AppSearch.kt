package org.hermeslauncher.app.icons

object AppSearch {
    fun filter(
        apps: List<LaunchableApp>,
        query: String,
        lastUsed: Map<String, Long> = emptyMap(),
    ): List<LaunchableApp> {
        val needle = query.trim()
        val matched = if (needle.isEmpty()) {
            apps
        } else {
            val lower = needle.lowercase()
            apps.filter { matches(it, lower) }
        }
        return matched.sortedWith(
            compareByDescending<LaunchableApp> { lastUsed[it.packageName] ?: 0L }
                .thenBy { it.label.lowercase() },
        )
    }

    private fun matches(app: LaunchableApp, needle: String): Boolean {
        if (app.label.lowercase().startsWith(needle)) {
            return true
        }
        val pkg = app.packageName.lowercase()
        return (needle.contains('.') || needle.length >= 3) && pkg.contains(needle)
    }
}
