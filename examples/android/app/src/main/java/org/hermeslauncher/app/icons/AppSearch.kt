package org.hermeslauncher.app.icons

object AppSearch {
    fun filter(apps: List<LaunchableApp>, query: String): List<LaunchableApp> {
        val needle = query.trim()
        if (needle.isEmpty()) {
            return apps
        }
        val lower = needle.lowercase()
        return apps.filter { it.label.lowercase().contains(lower) }
    }
}
