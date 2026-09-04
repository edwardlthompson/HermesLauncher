package org.hermeslauncher.app.workspace

data class DesktopLayout(
    val byPage: Map<Int, List<DesktopItem.Shortcut>> = emptyMap(),
) {
    fun page(pageIndex: Int): List<DesktopItem.Shortcut> = byPage[pageIndex].orEmpty()

    fun withShortcut(pageIndex: Int, shortcut: DesktopItem.Shortcut): DesktopLayout {
        val next = page(pageIndex).filterNot { it.id == shortcut.id } + shortcut
        return copy(byPage = byPage + (pageIndex to next))
    }

    fun without(pageIndex: Int, id: Long): DesktopLayout {
        return copy(byPage = byPage + (pageIndex to page(pageIndex).filterNot { it.id == id }))
    }
}

object DesktopCodec {
    const val V1: String = "v1"

    fun encode(layout: DesktopLayout): String {
        val pages = layout.byPage.toSortedMap().entries.joinToString(";") { (page, items) ->
            val slots = items.joinToString(",") { item ->
                val pkg = item.packageName.replace("|", "/")
                val act = item.activityName.replace("|", "/")
                val label = item.label.replace("|", "/").replace(",", " ")
                "${item.id}:$pkg:$act:$label:${item.cellX}:${item.cellY}"
            }
            "$page=$slots"
        }
        return "$V1|$pages"
    }

    fun decode(raw: String): DesktopLayout {
        val trimmed = raw.trim()
        if (!trimmed.startsWith("$V1|")) {
            return DesktopLayout()
        }
        val body = trimmed.removePrefix("$V1|")
        if (body.isBlank()) {
            return DesktopLayout()
        }
        val byPage = body.split(";").mapNotNull { chunk -> parsePage(chunk) }.toMap()
        return DesktopLayout(byPage)
    }

    private fun parsePage(chunk: String): Pair<Int, List<DesktopItem.Shortcut>>? {
        val eq = chunk.indexOf('=')
        if (eq <= 0) {
            return null
        }
        val page = chunk.substring(0, eq).toIntOrNull() ?: return null
        if (page < 1) {
            return null
        }
        val items = chunk.substring(eq + 1).split(",").mapNotNull { parseItem(it) }
        return page to items
    }

    private fun parseItem(raw: String): DesktopItem.Shortcut? {
        val parts = raw.split(":")
        if (parts.size < 6) {
            return null
        }
        val id = parts[0].toLongOrNull() ?: return null
        val x = parts[4].toIntOrNull() ?: return null
        val y = parts[5].toIntOrNull() ?: return null
        if (parts[1].isBlank() || parts[2].isBlank()) {
            return null
        }
        return DesktopItem.Shortcut(id, parts[1], parts[2], parts[3], x, y)
    }
}
