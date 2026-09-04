package org.hermeslauncher.app.feeds

import java.net.HttpURLConnection
import java.net.URI

object FeedFetcher {
    const val CONNECT_TIMEOUT_MS: Int = 10_000
    const val READ_TIMEOUT_MS: Int = 15_000

    fun isHttpUrl(raw: String): Boolean {
        val uri = runCatching { URI(raw.trim()) }.getOrNull() ?: return false
        val scheme = uri.scheme?.lowercase() ?: return false
        return (scheme == "http" || scheme == "https") && !uri.host.isNullOrBlank()
    }

    fun fetchXml(url: String): String {
        require(isHttpUrl(url)) { "not_http" }
        val connection = URI(url.trim()).toURL().openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.instanceFollowRedirects = true
            connection.requestMethod = "GET"
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    fun itemsFromXml(xml: String): List<FeedItem> {
        return runCatching { RssParser.parse(xml) }.getOrDefault(emptyList())
    }

    fun looksLikeFeed(body: String): Boolean {
        val head = body.trimStart().take(800).lowercase()
        return "<rss" in head || "<feed" in head || "<rdf:rdf" in head
    }

    fun resolve(raw: String): String? {
        val url = FeedDiscover.normalize(raw)
        if (!isHttpUrl(url)) {
            return null
        }
        val body = fetchXml(url)
        if (looksLikeFeed(body)) {
            return url
        }
        val href = FeedDiscover.alternateHref(body) ?: return null
        val abs = FeedDiscover.absolute(url, href) ?: return null
        if (!isHttpUrl(abs)) {
            return null
        }
        return abs.takeIf { looksLikeFeed(fetchXml(abs)) }
    }
}
