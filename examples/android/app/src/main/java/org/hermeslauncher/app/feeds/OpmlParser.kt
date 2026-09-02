package org.hermeslauncher.app.feeds

import org.w3c.dom.Element

object OpmlParser {
    fun parse(xml: String): List<OpmlOutline> {
        val doc = XmlDocuments.parse(xml) ?: return emptyList()
        val outlines = doc.getElementsByTagName("outline")
        return buildList {
            for (i in 0 until outlines.length) {
                val node = outlines.item(i) as? Element ?: continue
                val xmlUrl = node.getAttribute("xmlUrl").ifBlank { node.getAttribute("xmlurl") }
                if (xmlUrl.isBlank()) {
                    continue
                }
                val title = node.getAttribute("title").ifBlank {
                    node.getAttribute("text")
                }.ifBlank { xmlUrl }
                add(
                    OpmlOutline(
                        title = title,
                        xmlUrl = xmlUrl,
                        htmlUrl = node.getAttribute("htmlUrl").takeIf { it.isNotBlank() },
                        type = node.getAttribute("type").ifBlank { "rss" },
                    ),
                )
            }
        }
    }
}
