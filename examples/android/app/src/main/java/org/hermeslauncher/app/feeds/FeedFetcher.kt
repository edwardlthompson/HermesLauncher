package org.hermeslauncher.app.feeds

import java.net.HttpURLConnection
import java.net.URI

object FeedFetcher {
    const val CONNECT_TIMEOUT_MS: Int = 10_000
    const val READ_TIMEOUT_MS: Int = 15_000
    const val USER_AGENT: String = "HermesLauncher/1.0 (Android; RSS)"

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
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.setRequestProperty(
                "Accept",
                "application/rss+xml, application/atom+xml, application/feed+json, application/json, application/xml, text/xml, text/html;q=0.8,*/*;q=0.5",
            )
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                throw java.io.IOException("http_$code")
            }
            body
        } finally {
            connection.disconnect()
        }
    }

    fun itemsFromXml(xml: String): List<FeedItem> {
        val trimmed = xml.trimStart()
        if (trimmed.startsWith("{")) {
            return runCatching { JsonFeedParser.parse(xml) }.getOrDefault(emptyList())
        }
        return runCatching { RssParser.parse(xml) }.getOrDefault(emptyList())
    }

    fun looksLikeFeed(body: String): Boolean {
        val head = body.trimStart().take(800).lowercase()
        if ("<rss" in head || "<feed" in head || "<rdf:rdf" in head) {
            return true
        }
        return head.startsWith("{") && ("jsonfeed.org" in head || "\"items\"" in head)
    }

    fun resolve(raw: String): String? {
        val url = FeedDiscover.normalize(raw)
        if (!isHttpUrl(url)) {
            return null
        }
        val body = runCatching { fetchXml(url) }.getOrNull()
        if (body != null && looksLikeFeed(body)) {
            return FeedDiscover.canonicalize(url)
        }
        val href = body?.let { FeedDiscover.alternateHref(it) }
        val fromLink = href?.let { FeedDiscover.absolute(url, it) }
        if (fromLink != null && isHttpUrl(fromLink) &&
            runCatching { looksLikeFeed(fetchXml(fromLink)) }.getOrDefault(false)
        ) {
            return FeedDiscover.canonicalize(fromLink)
        }
        return FeedDiscover.guesses(url).firstOrNull { candidate ->
            isHttpUrl(candidate) && runCatching { looksLikeFeed(fetchXml(candidate)) }.getOrDefault(false)
        }?.let { FeedDiscover.canonicalize(it) }
    }

    fun fetchBytes(url: String, maxBytes: Int = 1_500_000): ByteArray {
        require(isHttpUrl(url)) { "not_http" }
        val connection = URI(url.trim()).toURL().openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.instanceFollowRedirects = true
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.setRequestProperty("Accept", "image/*,*/*;q=0.5")
            val code = connection.responseCode
            if (code !in 200..299) {
                throw java.io.IOException("http_$code")
            }
            val length = connection.contentLength
            if (length in 1 until 400) {
                throw java.io.IOException("tiny_body")
            }
            if (length > maxBytes) {
                throw java.io.IOException("too_large")
            }
            val stream = connection.inputStream
            val bytes = stream.readBytes()
            if (bytes.size > maxBytes) {
                throw java.io.IOException("too_large")
            }
            bytes
        } finally {
            connection.disconnect()
        }
    }
}
