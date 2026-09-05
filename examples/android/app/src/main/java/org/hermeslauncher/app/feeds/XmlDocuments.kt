package org.hermeslauncher.app.feeds

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

internal object XmlDocuments {
    fun parse(xml: String): Document? {
        return runCatching {
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = false
            factory.isExpandEntityReferences = false
            runCatching {
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            }
            factory.newDocumentBuilder().parse(InputSource(StringReader(xml)))
        }.getOrNull()
    }

    fun firstChildText(element: Element, vararg tags: String): String? {
        for (tag in tags) {
            val text = elementsNamed(element, tag).firstOrNull()?.textContent?.trim().orEmpty()
            if (text.isNotEmpty()) {
                return text
            }
        }
        return null
    }

    fun childText(element: Element, tag: String): String? {
        val nodes = element.getElementsByTagName(tag)
        if (nodes.length == 0) {
            return null
        }
        val text = nodes.item(0).textContent?.trim().orEmpty()
        return text.takeIf { it.isNotEmpty() }
    }

    fun attr(element: Element, tag: String, name: String): String? {
        val node = elementsNamed(element, tag).firstOrNull()
            ?: (element.getElementsByTagName(tag).item(0) as? Element)
        return node?.getAttribute(name)?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun elementsNamed(element: Element, tag: String): List<Element> {
        val nodes = element.getElementsByTagName(tag)
        return buildList {
            for (i in 0 until nodes.length) {
                val node = nodes.item(i)
                if (node is Element && node.parentNode == element) {
                    add(node)
                }
            }
        }
    }
}
