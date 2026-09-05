package org.hermeslauncher.app.feeds

/** Pick article images; skip tracking pixels and social icons. */
object ArticleImages {
    const val MIN_EDGE: Int = 64
    private val imgTag = Regex("""<img\b[^>]*>""", RegexOption.IGNORE_CASE)
    private val srcAttr = Regex(
        """(?:src|data-src|data-original|data-lazy-src)\s*=\s*["']([^"']+)["']""",
        RegexOption.IGNORE_CASE,
    )
    private val srcset = Regex("""srcset\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val og = Regex(
        """<meta\b[^>]+(?:property|name)\s*=\s*["'](?:og:image|twitter:image)["'][^>]*>""",
        RegexOption.IGNORE_CASE,
    )
    private val ogFlip = Regex(
        """<meta\b[^>]+content\s*=\s*["']([^"']+)["'][^>]+(?:property|name)\s*=\s*["'](?:og:image|twitter:image)["'][^>]*>""",
        RegexOption.IGNORE_CASE,
    )
    private val content = Regex("""content\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val tinyHint = Regex(
        """favicon|sprite|pixel|1x1|tracking|emoji|badge|icon[-_/]|/icons?/|social|share|facebook|twitter|linkedin|whatsapp|telegram""",
        RegexOption.IGNORE_CASE,
    )

    fun looksTinyUrl(url: String): Boolean {
        val lower = url.lowercase()
        return tinyHint.containsMatchIn(lower) || lower.startsWith("data:")
    }

    fun isTiny(width: Int, height: Int): Boolean {
        if (width <= 0 || height <= 0) {
            return false
        }
        return width < MIN_EDGE && height < MIN_EDGE
    }

    fun absUrl(raw: String): String? {
        val trimmed = raw.trim()
        val url = if (trimmed.startsWith("//")) "https:$trimmed" else trimmed
        return url.takeIf { FeedFetcher.isHttpUrl(it) && !looksTinyUrl(it) }
    }

    fun photoKey(url: String): String {
        val path = url.trim().substringBefore('?').substringBefore('#').lowercase()
        return path
            .replace(Regex("""-\d+x\d+(?=\.[a-z0-9]+$)"""), "")
            .replace(Regex("""-scaled(?=\.[a-z0-9]+$)"""), "")
    }

    fun bestFromImgTag(tag: String): String? {
        var best: String? = null
        var bestW = -1
        val set = srcset.find(tag)?.groupValues?.get(1).orEmpty()
        for (part in set.split(",")) {
            val bits = part.trim().split(Regex("\\s+"))
            val url = absUrl(bits.firstOrNull().orEmpty()) ?: continue
            val w = bits.getOrNull(1)?.removeSuffix("w")?.toIntOrNull() ?: 0
            if (w > bestW || best == null) {
                bestW = w
                best = url
            }
        }
        return best ?: srcAttr.findAll(tag).mapNotNull { absUrl(it.groupValues[1]) }.firstOrNull()
    }

    fun fromImgTag(tag: String): List<String> = listOfNotNull(bestFromImgTag(tag))

    fun allFromHtml(html: String): List<String> {
        val meta = og.findAll(html).mapNotNull { content.find(it.value)?.groupValues?.get(1) } +
            ogFlip.findAll(html).map { it.groupValues[1] }
        val imgs = imgTag.findAll(html).mapNotNull { bestFromImgTag(it.value) }
        return (meta.mapNotNull { absUrl(it) } + imgs).distinctBy { photoKey(it) }.toList()
    }

    fun firstFromHtml(html: String): String? = allFromHtml(html).firstOrNull()

    fun largestSrcset(html: String): String? {
        var bestUrl: String? = null
        var bestW = -1
        for (match in srcset.findAll(html)) {
            for (part in match.groupValues[1].split(",")) {
                val bits = part.trim().split(Regex("\\s+"))
                val url = absUrl(bits.firstOrNull().orEmpty()) ?: continue
                val w = bits.getOrNull(1)?.removeSuffix("w")?.toIntOrNull() ?: 0
                if (w > bestW || bestUrl == null) {
                    bestW = w
                    bestUrl = url
                }
            }
        }
        return bestUrl
    }

    fun canonicalHero(
        thumbnailUrl: String?,
        enclosureUrl: String?,
        enclosureMime: String?,
        html: String?,
    ): String? {
        largestSrcset(html.orEmpty())?.let { return it }
        val mime = enclosureMime.orEmpty().lowercase()
        val fromEnc = enclosureUrl?.takeIf { mime.startsWith("image/") }
        return sequenceOf(thumbnailUrl, fromEnc)
            .mapNotNull { it?.let { url -> absUrl(url) } }
            .firstOrNull()
            ?: firstFromHtml(html.orEmpty())
    }

    fun fromRss(
        thumbnailUrl: String?,
        enclosureUrl: String?,
        enclosureMime: String?,
        html: String?,
    ): String? = canonicalHero(thumbnailUrl, enclosureUrl, enclosureMime, html)
}
