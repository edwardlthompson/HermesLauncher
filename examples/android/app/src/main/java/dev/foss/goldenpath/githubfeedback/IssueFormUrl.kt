package dev.foss.goldenpath.githubfeedback

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object IssueFormUrl {
    const val MAX_QUERY_CHARS = 1800

    data class Built(
        val url: String,
        val clipboardMarkdown: String? = null,
        val bodyTooLarge: Boolean = false,
    )

    fun isPlaceholderRepo(repo: String): Boolean {
        val trimmed = repo.trim()
        return trimmed.isEmpty() || trimmed.equals("OWNER/REPO", ignoreCase = true)
    }

    fun crashTitle(fingerprint: String, exceptionType: String): String {
        val fp = fingerprint.filter { it.isLetterOrDigit() }.take(12).lowercase()
        val kind = exceptionType.split(Regex("[^A-Za-z0-9_.$]")).firstOrNull().orEmpty().ifEmpty { "Error" }
        return "[crash] $fp $kind"
    }

    fun build(repo: String, template: String, fields: Map<String, String>): Built {
        if (isPlaceholderRepo(repo)) return Built("")
        val base = "https://github.com/${repo.trim()}/issues/new"
        val params = linkedMapOf<String, String>()
        params["template"] = template
        fields["title"]?.let { params["title"] = it }
        fields["labels"]?.let { params["labels"] = it }
        fields.forEach { (key, value) ->
            if (key != "title" && key != "labels" && value.isNotEmpty()) params[key] = value
        }
        val full = base + "?" + encode(params)
        if (full.length <= MAX_QUERY_CHARS) return Built(full)
        val short = linkedMapOf("template" to template)
        fields["title"]?.let { short["title"] = it }
        val markdown = fields["stack"] ?: fields["description"] ?: fields["reproduction"] ?: fields["problem"] ?: ""
        return Built(base + "?" + encode(short), markdown, true)
    }

    private fun encode(params: Map<String, String>): String =
        params.entries.joinToString("&") { (k, v) ->
            enc(k) + "=" + enc(v)
        }

    private fun enc(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
}
