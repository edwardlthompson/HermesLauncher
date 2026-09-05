package org.hermeslauncher.app.feeds

/** Strip HTML into reading-mode plain text. Prefers RSS body, then page article/p tags. */
sealed class ArticleBlock {
    data class Text(val value: String) : ArticleBlock()
    data class Image(val url: String) : ArticleBlock()
}

object ArticleExtract {
    private val script = Regex("(?is)<script\\b[^>]*>.*?</script>")
    private val style = Regex("(?is)<style\\b[^>]*>.*?</style>")
    private val noscript = Regex("(?is)<noscript\\b[^>]*>.*?</noscript>")
    private val tags = Regex("<[^>]+>")
    private val space = Regex("[ \\t\\x0B\\f\\r]+")
    private val breaks = Regex("\\n{3,}")
    private val article = Regex("(?is)<article\\b[^>]*>(.*?)</article>")
    private val para = Regex("(?is)<p\\b[^>]*>(.*?)</p>")

    fun fromRss(content: String?, description: String?): String {
        val raw = sequenceOf(content, description).firstOrNull { !it.isNullOrBlank() }.orEmpty()
        return htmlToText(raw)
    }

    fun fromPage(html: String): String {
        val nested = article.find(html)?.groupValues?.get(1)
        if (!nested.isNullOrBlank()) {
            val text = htmlToText(nested)
            if (text.length >= 80) {
                return text
            }
        }
        return para.findAll(html)
            .map { htmlToText(it.groupValues[1]) }
            .filter { it.length >= 40 }
            .joinToString("\n\n")
    }

    fun blocks(html: String): List<ArticleBlock> {
        val nested = article.find(html)?.groupValues?.get(1)
        val source = if (!nested.isNullOrBlank() && nested.length >= 80) nested else html
        val out = mutableListOf<ArticleBlock>()
        val seen = mutableSetOf<String>()
        var cursor = 0
        val img = Regex("""<img\b[^>]*>""", RegexOption.IGNORE_CASE)
        for (match in img.findAll(source)) {
            htmlToText(source.substring(cursor, match.range.first)).takeIf { it.isNotBlank() }?.let {
                out.add(ArticleBlock.Text(it))
            }
            val url = ArticleImages.bestFromImgTag(match.value)
            if (url != null && seen.add(ArticleImages.photoKey(url))) {
                out.add(ArticleBlock.Image(url))
            }
            cursor = match.range.last + 1
        }
        htmlToText(source.substring(cursor)).takeIf { it.isNotBlank() }?.let {
            out.add(ArticleBlock.Text(it))
        }
        ArticleImages.allFromHtml(html).forEach { url ->
            if (seen.add(ArticleImages.photoKey(url))) {
                out.add(ArticleBlock.Image(url))
            }
        }
        if (out.isEmpty()) {
            htmlToText(html).takeIf { it.isNotBlank() }?.let { out.add(ArticleBlock.Text(it)) }
        }
        return out
    }

    fun htmlToText(html: String): String {
        var out = script.replace(html, " ")
        out = style.replace(out, " ")
        out = noscript.replace(out, " ")
        out = out.replace(Regex("(?i)<br\\s*/?>"), "\n")
        out = out.replace(Regex("(?i)</(p|div|h[1-6]|li)>"), "\n\n")
        out = tags.replace(out, " ")
        out = decode(out)
        out = space.replace(out, " ").replace(Regex(" *\\n *"), "\n")
        return breaks.replace(out, "\n\n").trim()
    }

    private fun decode(raw: String): String {
        return raw
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
    }
}
