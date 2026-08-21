package dev.foss.goldenpath.about

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GithubRelease {
    data class Parsed(val htmlUrl: String, val assets: List<ProductUpdate.NamedAsset>)

    fun parse(json: String, fallbackHtml: String = ""): Parsed? {
        return try {
            val root = JSONObject(json)
            val htmlUrl = root.optString("html_url", fallbackHtml)
            val assets = mutableListOf<ProductUpdate.NamedAsset>()
            val arr = root.optJSONArray("assets") ?: return Parsed(htmlUrl, assets)
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val name = item.optString("name")
                val url = item.optString("browser_download_url")
                if (name.isNotBlank() && url.isNotBlank()) {
                    assets.add(ProductUpdate.NamedAsset(name, url))
                }
            }
            Parsed(htmlUrl, assets)
        } catch (_: Exception) {
            null
        }
    }

    fun fetchLatest(apiUrl: String, userAgent: String): Parsed? {
        if (apiUrl.isBlank()) return null
        val conn = URL(apiUrl).openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("User-Agent", userAgent)
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            if (conn.responseCode != HttpURLConnection.HTTP_OK) return null
            parse(conn.inputStream.bufferedReader().use { it.readText() })
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }
}
