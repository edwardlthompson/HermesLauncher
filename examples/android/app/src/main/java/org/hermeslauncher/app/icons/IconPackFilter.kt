package org.hermeslauncher.app.icons

import android.content.Context
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.util.concurrent.ConcurrentHashMap

data class IconPackMap(
    val byComponent: Map<String, String>,
    val byPackage: Map<String, String>,
    val chrome: IconPackChrome = IconPackChrome(),
)

object IconPackFilter {
    private val cache = ConcurrentHashMap<String, IconPackMap>()

    fun mapsFor(context: Context, packPkg: String): IconPackMap {
        return cache.getOrPut(packPkg) {
            runCatching { load(context, packPkg) }.getOrDefault(emptyMaps())
        }
    }

    fun nameFor(context: Context, packPkg: String, app: LaunchableApp): String {
        val maps = mapsFor(context, packPkg)
        val exact = componentKey(app.packageName, app.activityName)
        val raw = maps.byComponent[exact]
            ?: maps.byPackage[app.packageName]
            ?: IconPackResources.drawableName(app)
        return drawableName(raw)
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

    internal fun drawableName(raw: String): String {
        return raw.trim().substringAfterLast('/').substringBefore('.').lowercase()
    }

    private fun emptyMaps(): IconPackMap = IconPackMap(emptyMap(), emptyMap(), IconPackChrome())

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
        for (asset in listOf("appfilter.xml", "xml/appfilter.xml", "app_filter.xml")) {
            val parsed = runCatching {
                res.assets.open(asset).use { stream ->
                    val parser = Xml.newPullParser()
                    parser.setInput(stream, Charsets.UTF_8.name())
                    parse(parser)
                }
            }.getOrNull()
            if (parsed != null) {
                return parsed
            }
        }
        return emptyMaps()
    }

    private fun parse(parser: XmlPullParser): IconPackMap {
        val byComponent = LinkedHashMap<String, String>()
        val byPackage = LinkedHashMap<String, String>()
        val backs = ArrayList<String>()
        val masks = ArrayList<String>()
        val upons = ArrayList<String>()
        var scale = IconPackChrome.DEFAULT_SCALE
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "item", "icon" -> {
                        val key = attr(parser, "component")?.let { normalizeComponent(it) }
                        val drawable = attr(parser, "drawable")?.let { drawableName(it) }
                        if (key != null && !drawable.isNullOrBlank()) {
                            byComponent.putIfAbsent(key, drawable)
                            byPackage.putIfAbsent(key.substringBefore('/'), drawable)
                        }
                    }
                    "iconback" -> backs.addAll(IconPackChrome.imgs(parser))
                    "iconmask" -> masks.addAll(IconPackChrome.imgs(parser))
                    "iconupon" -> upons.addAll(IconPackChrome.imgs(parser))
                    "scale" -> scale = IconPackChrome.scaleOf(parser)
                }
            }
            event = parser.next()
        }
        return IconPackMap(byComponent, byPackage, IconPackChrome(backs, masks, upons, scale))
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
