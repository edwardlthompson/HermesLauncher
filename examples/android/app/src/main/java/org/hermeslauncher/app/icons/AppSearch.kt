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
            apps.filter {
                it.label.lowercase().contains(lower) || it.packageName.lowercase().contains(lower)
            }
        }
        return matched.sortedWith(
            compareByDescending<LaunchableApp> { lastUsed[it.packageName] ?: 0L }
                .thenBy { it.label.lowercase() },
        )
    }
}
