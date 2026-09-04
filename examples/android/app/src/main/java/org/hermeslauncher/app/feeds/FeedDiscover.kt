package org.hermeslauncher.app.feeds

import java.net.URI

object FeedDiscover {
    private val linkTag = Regex("""<link\b[^>]*>""", RegexOption.IGNORE_CASE)
    private val hrefAttr = Regex("""href\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)

    fun normalize(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty() || FeedFetcher.isHttpUrl(trimmed)) {
            return trimmed
        }
        val guessed = "https://$trimmed"
        return if (FeedFetcher.isHttpUrl(guessed)) guessed else trimmed
    }

    fun alternateHref(html: String): String? {
        for (tag in linkTag.findAll(html)) {
            val lower = tag.value.lowercase()
            if (!lower.contains("alternate")) {
                continue
            }
            val rss = lower.contains("application/rss+xml")
            val atom = lower.contains("application/atom+xml")
            if (!rss && !atom) {
                continue
            }
            val href = hrefAttr.find(tag.value)?.groupValues?.get(1)?.trim().orEmpty()
            if (href.isNotEmpty()) {
                return href
            }
        }
        return null
    }

    fun absolute(base: String, href: String): String? {
        return runCatching { URI(base).resolve(href.trim()).toString() }.getOrNull()
    }
}
