package org.hermeslauncher.app.feeds

/** Inverse of [OpmlParser] for SAF export. Empty outlines → valid empty body. Groups by [OpmlOutline.tag]. */
object OpmlExporter {
    fun write(outlines: List<OpmlOutline>, title: String = "Hermes feeds"): String {
        val grouped = linkedMapOf<String, MutableList<OpmlOutline>>()
        for (outline in outlines) {
            if (outline.xmlUrl.isBlank()) {
                continue
            }
            grouped.getOrPut(outline.tag) { mutableListOf() }.add(outline)
        }
        return buildString {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            append("<opml version=\"2.0\">\n")
            append("<head><title>")
            append(escape(title))
            append("</title></head>\n<body>\n")
            for ((tag, rows) in grouped) {
                if (tag.isNotBlank()) {
                    append("<outline text=\"")
                    append(escape(tag))
                    append("\" title=\"")
                    append(escape(tag))
                    append("\">\n")
                }
                for (outline in rows) {
                    appendLeaf(outline)
                }
                if (tag.isNotBlank()) {
                    append("</outline>\n")
                }
            }
            append("</body>\n</opml>\n")
        }
    }

    private fun StringBuilder.appendLeaf(outline: OpmlOutline) {
        append("<outline type=\"")
        append(escape(outline.type.ifBlank { "rss" }))
        append("\" text=\"")
        append(escape(outline.title.ifBlank { outline.xmlUrl }))
        append("\" title=\"")
        append(escape(outline.title.ifBlank { outline.xmlUrl }))
        append("\" xmlUrl=\"")
        append(escape(outline.xmlUrl))
        if (!outline.htmlUrl.isNullOrBlank()) {
            append("\" htmlUrl=\"")
            append(escape(outline.htmlUrl))
        }
        append("\"/>\n")
    }

    private fun escape(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }
}
