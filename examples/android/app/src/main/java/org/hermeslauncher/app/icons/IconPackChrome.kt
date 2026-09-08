package org.hermeslauncher.app.icons

import android.graphics.drawable.Drawable
import org.xmlpull.v1.XmlPullParser

data class IconPackChrome(
    val backs: List<String> = emptyList(),
    val masks: List<String> = emptyList(),
    val upons: List<String> = emptyList(),
    val scale: Float = DEFAULT_SCALE,
) {
    companion object {
        const val DEFAULT_SCALE: Float = 0.7f
        const val MIN_SCALE: Float = 0.4f
        const val MAX_SCALE: Float = 1f

        fun imgs(parser: XmlPullParser): List<String> {
            val out = ArrayList<String>()
            for (i in 0 until parser.attributeCount) {
                val name = parser.getAttributeName(i) ?: continue
                if (name != "img" && !name.startsWith("img") && name != "image" && name != "drawable") {
                    continue
                }
                val value = IconPackFilter.drawableName(parser.getAttributeValue(i))
                if (value.isNotBlank()) {
                    out.add(value)
                }
            }
            return out
        }

        fun scaleOf(parser: XmlPullParser): Float {
            val raw = parser.getAttributeValue(null, "factor")
                ?: parser.getAttributeValue(null, "value")
                ?: parser.getAttributeValue(null, "scale")
            val parsed = raw?.toFloatOrNull() ?: DEFAULT_SCALE
            return parsed.coerceIn(MIN_SCALE, MAX_SCALE)
        }
    }
}

data class IconPackLayers(
    val back: Drawable? = null,
    val mask: Drawable? = null,
    val upon: Drawable? = null,
    val scale: Float = IconPackChrome.DEFAULT_SCALE,
)
