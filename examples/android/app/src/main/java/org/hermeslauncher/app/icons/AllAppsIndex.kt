package org.hermeslauncher.app.icons

object AllAppsIndex {
    const val COLUMNS: Int = DrawerPolicy.COLUMNS_DEFAULT

    fun letter(label: String): Char {
        val ch = label.trim().firstOrNull()?.uppercaseChar() ?: '#'
        return if (ch in 'A'..'Z') ch else '#'
    }

    fun sections(apps: List<LaunchableApp>): List<Pair<Char, List<LaunchableApp>>> {
        val grouped = apps.groupBy { letter(it.label) }.mapValues { (_, rows) ->
            rows.sortedBy { it.label.lowercase() }
        }
        val letters = grouped.keys.filter { it != '#' }.sorted() + grouped.keys.filter { it == '#' }
        return letters.mapNotNull { ch -> grouped[ch]?.let { ch to it } }
    }

    fun rail(sections: List<Pair<Char, List<LaunchableApp>>>): List<Char> {
        return sections.map { it.first }
    }

    fun keys(
        sections: List<Pair<Char, List<LaunchableApp>>>,
        predicted: Boolean,
        columns: Int = COLUMNS,
    ): List<String> {
        val out = mutableListOf<String>()
        val chunk = columns.coerceAtLeast(1)
        if (predicted) {
            out += "pred"
        }
        sections.forEach { (letter, apps) ->
            out += "h:$letter"
            apps.chunked(chunk).forEachIndexed { index, _ ->
                out += "r:$letter:$index"
            }
        }
        return out
    }

    fun indexOf(keys: List<String>, letter: Char): Int {
        val idx = keys.indexOf("h:$letter")
        return idx.coerceAtLeast(0)
    }
}
