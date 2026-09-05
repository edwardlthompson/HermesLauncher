package org.hermeslauncher.app.feeds

import org.json.JSONArray
import org.json.JSONObject

enum class SubKind {
    NEWS,
    PODCAST,
}

data class FeedSub(
    val url: String,
    val title: String = "",
    val tag: String = "",
    val kind: SubKind = SubKind.NEWS,
    val notify: Boolean = false,
    val prefetch: Boolean = true,
    val lastError: String? = null,
)

object FeedSubCodec {
    fun encode(subs: List<FeedSub>): String {
        val arr = JSONArray()
        for (sub in subs) {
            arr.put(
                JSONObject()
                    .put("url", sub.url)
                    .put("title", sub.title)
                    .put("tag", sub.tag)
                    .put("kind", sub.kind.name)
                    .put("notify", sub.notify)
                    .put("prefetch", sub.prefetch)
                    .put("lastError", sub.lastError ?: JSONObject.NULL),
            )
        }
        return arr.toString()
    }

    fun decode(raw: String?): List<FeedSub> {
        if (raw.isNullOrBlank()) {
            return emptyList()
        }
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val url = obj.optString("url")
                    if (!FeedFetcher.isHttpUrl(url)) {
                        continue
                    }
                    add(
                        FeedSub(
                            url = url,
                            title = obj.optString("title"),
                            tag = obj.optString("tag"),
                            kind = runCatching { SubKind.valueOf(obj.optString("kind", SubKind.NEWS.name)) }
                                .getOrDefault(SubKind.NEWS),
                            notify = obj.optBoolean("notify"),
                            prefetch = obj.optBoolean("prefetch", true),
                            lastError = obj.optString("lastError").takeIf { it.isNotBlank() },
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun fromUrls(urls: Collection<String>): List<FeedSub> {
        return urls.filter { FeedFetcher.isHttpUrl(it) }.distinct().map { FeedSub(url = it) }
    }
}
