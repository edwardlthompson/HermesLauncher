package org.hermeslauncher.app.icons

import android.content.Context
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.util.concurrent.ConcurrentHashMap

data class IconPackMap(
    val byComponent: Map<String, String>,
    val byPackage: Map<String, String>,
)

object IconPackFilter {
    private val cache = ConcurrentHashMap<String, IconPackMap>()

    fun nameFor(context: Context, packPkg: String, app: LaunchableApp): String {
        val maps = cache.getOrPut(packPkg) {
            runCatching { load(context, packPkg) }.getOrDefault(emptyMaps())
        }
        val exact = componentKey(app.packageName, app.activityName)
        return maps.byComponent[exact]
            ?: maps.byPackage[app.packageName]
            ?: IconPackResources.drawableName(app)
    }

    fun forget(packPkg: String? = null) {
        if (packPkg == null) {
            cache.clear()
        } else {
            cache.remove(packPkg)
        }
    }

    fun parseXml(xml: String): IconPackMap {
        val parser = Xml.newPullParser()
        parser.setInput(xml.reader())
        return parse(parser)
    }

    fun normalizeComponent(raw: String): String? {
        val inner = raw.trim()
            .removePrefix("ComponentInfo{")
            .removeSuffix("}")
            .trim()
        val slash = inner.indexOf('/')
        if (slash <= 0 || inner.startsWith(":")) {
            return null
        }
        val pkg = inner.substring(0, slash)
        var cls = inner.substring(slash + 1)
        if (cls.startsWith(".")) {
            cls = pkg + cls
        }
        return "$pkg/$cls"
    }

    private fun emptyMaps(): IconPackMap = IconPackMap(emptyMap(), emptyMap())

    private fun load(context: Context, packPkg: String): IconPackMap {
        val res = context.packageManager.getResourcesForApplication(packPkg)
        for (xmlName in listOf("appfilter", "app_filter")) {
            val id = res.getIdentifier(xmlName, "xml", packPkg)
            if (id != 0) {
                val parser = res.getXml(id)
                try {
                    return parse(parser)
                } finally {
                    parser.close()
                }
            }
        }
        res.assets.open("appfilter.xml").use { stream ->
            val parser = Xml.newPullParser()
            parser.setInput(stream, Charsets.UTF_8.name())
            return parse(parser)
        }
    }

    private fun parse(parser: XmlPullParser): IconPackMap {
        val byComponent = LinkedHashMap<String, String>()
        val byPackage = LinkedHashMap<String, String>()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && parser.name == "item") {
                val key = attr(parser, "component")?.let { normalizeComponent(it) }
                val drawable = attr(parser, "drawable")
                if (key != null && !drawable.isNullOrBlank()) {
                    byComponent.putIfAbsent(key, drawable)
                    byPackage.putIfAbsent(key.substringBefore('/'), drawable)
                }
            }
            event = parser.next()
        }
        return IconPackMap(byComponent, byPackage)
    }

    private fun attr(parser: XmlPullParser, name: String): String? {
        parser.getAttributeValue(null, name)?.let { return it }
        for (i in 0 until parser.attributeCount) {
            if (parser.getAttributeName(i) == name) {
                return parser.getAttributeValue(i)
            }
        }
        return null
    }

    private fun componentKey(packageName: String, activityName: String): String {
        val cls = if (activityName.startsWith(".")) packageName + activityName else activityName
        return "$packageName/$cls"
    }
}
