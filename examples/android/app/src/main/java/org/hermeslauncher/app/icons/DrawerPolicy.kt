package org.hermeslauncher.app.icons

data class DrawerSnapshot(
    val hidden: Set<String> = emptySet(),
    val columns: Int = DrawerPolicy.COLUMNS_DEFAULT,
    val listMode: Boolean = false,
    val showRail: Boolean = true,
)

object DrawerPolicy {
    const val COLUMNS_MIN: Int = 4
    const val COLUMNS_MAX: Int = 6
    const val COLUMNS_DEFAULT: Int = 5

    fun columns(raw: Int): Int = raw.coerceIn(COLUMNS_MIN, COLUMNS_MAX)

    fun chunkSize(listMode: Boolean, columns: Int): Int {
        return if (listMode) 1 else columns(columns)
    }

    fun visible(apps: List<LaunchableApp>, hidden: Set<String>): List<LaunchableApp> {
        if (hidden.isEmpty()) {
            return apps
        }
        return apps.filter { it.packageName !in hidden }
    }

    fun hide(hidden: Set<String>, packageName: String): Set<String> {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) {
            return hidden
        }
        return hidden + pkg
    }

    fun show(hidden: Set<String>, packageName: String): Set<String> {
        return hidden - packageName.trim()
    }

    fun picks(
        apps: List<LaunchableApp>,
        query: String,
        exclude: Set<String>,
        limit: Int = 8,
    ): List<LaunchableApp> {
        if (query.trim().isEmpty()) {
            return emptyList()
        }
        return AppSearch.filter(apps, query)
            .filter { it.packageName !in exclude }
            .take(limit)
    }
}

object DrawerCodec {
    fun encodeHidden(hidden: Set<String>): String {
        return hidden.map { it.trim() }.filter { it.isNotEmpty() }.sorted().joinToString("\n")
    }

    fun decodeHidden(raw: String?): Set<String> {
        return raw.orEmpty().lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }
}
