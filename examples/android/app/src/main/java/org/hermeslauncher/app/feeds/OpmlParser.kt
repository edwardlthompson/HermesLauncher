package org.hermeslauncher.app.feeds

import org.w3c.dom.Element
import org.w3c.dom.Node

object OpmlParser {
    fun parse(xml: String): List<OpmlOutline> {
        val doc = XmlDocuments.parse(xml) ?: return emptyList()
        val body = doc.getElementsByTagName("body").item(0) as? Element
            ?: doc.documentElement
        return walk(body, parentTag = "")
    }

    private fun walk(parent: Element, parentTag: String): List<OpmlOutline> {
        return buildList {
            val children = parent.childNodes
            for (i in 0 until children.length) {
                val node = children.item(i)
                if (node.nodeType != Node.ELEMENT_NODE) {
                    continue
                }
                val element = node as? Element ?: continue
                if (!element.tagName.equals("outline", ignoreCase = true)) {
                    continue
                }
                val xmlUrl = element.getAttribute("xmlUrl").ifBlank { element.getAttribute("xmlurl") }
                val title = element.getAttribute("title").ifBlank {
                    element.getAttribute("text")
                }.ifBlank { xmlUrl }
                val folder = element.getAttribute("text").ifBlank { element.getAttribute("title") }
                if (xmlUrl.isNotBlank()) {
                    add(
                        OpmlOutline(
                            title = title,
                            xmlUrl = xmlUrl,
                            htmlUrl = element.getAttribute("htmlUrl").takeIf { it.isNotBlank() },
                            type = element.getAttribute("type").ifBlank { "rss" },
                            tag = parentTag,
                        ),
                    )
                }
                val nestedTag = if (xmlUrl.isBlank()) folder else parentTag
                addAll(walk(element, nestedTag))
            }
        }
    }
}
