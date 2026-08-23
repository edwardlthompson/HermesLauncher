package dev.foss.goldenpath.privacyreport

object SanitizeReport {
    const val MAX_BODY_BYTES = 8192
    const val MAX_STACK_LINES = 200

    private val pem = Regex(
        "-----BEGIN [A-Z ]*PRIVATE KEY-----\\s*[\\s\\S]*?-----END [A-Z ]*PRIVATE KEY-----",
    )
    private val github = Regex("\\b(?:ghp|gho|github_pat)_[A-Za-z0-9_]+")
    private val bearer = Regex("Bearer\\s+\\S+", RegexOption.IGNORE_CASE)
    private val jwt = Regex("\\beyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+")
    private val aws = Regex("\\bAKIA[0-9A-Z]{16}\\b")
    private val api = Regex("(?:api[_-]?key|token)\\s*[:=]\\s*\\S+", RegexOption.IGNORE_CASE)
    private val email = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
    private val winHome = Regex("C:\\\\Users\\\\[^\\\\]+\\\\", RegexOption.IGNORE_CASE)
    private val unixHome = Regex("/(?:home|Users)/[^/\\s]+/")
    private val unc = Regex("\\\\\\\\[^\\\\\\s]+\\\\[^\\\\\\s]+\\\\")
    private val ipv4 = Regex("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b")
    private val ipv6 = Regex("\\b(?:[0-9a-f]{1,4}:){2,7}[0-9a-f]{1,4}\\b", RegexOption.IGNORE_CASE)
    private val urlQ = Regex("([?&])(token|key|code|access_token)=[^&\\s]+", RegexOption.IGNORE_CASE)

    fun text(raw: String?, stack: Boolean = false): String {
        if (raw == null) return ""
        var out = raw
        out = pem.replace(out, "<redacted-secret>")
        out = github.replace(out, "<redacted-secret>")
        out = bearer.replace(out, "<redacted-secret>")
        out = jwt.replace(out, "<redacted-secret>")
        out = aws.replace(out, "<redacted-secret>")
        out = api.replace(out, "<redacted-secret>")
        out = email.replace(out, "<redacted-email>")
        out = winHome.replace(out, "<redacted-home>")
        out = unixHome.replace(out, "<redacted-home>/")
        out = unc.replace(out, "<redacted-unc>")
        out = ipv4.replace(out, "<redacted-ip>")
        out = ipv6.replace(out, "<redacted-ip>")
        out = urlQ.replace(out, "$1$2=<redacted-secret>")
        if (stack) {
            out = out.lineSequence().take(MAX_STACK_LINES).joinToString("\n")
        }
        return capWholeLines(out)
    }

    private fun capWholeLines(text: String): String {
        val bytes = text.toByteArray(Charsets.UTF_8)
        if (bytes.size <= MAX_BODY_BYTES) return text
        val kept = ArrayList<String>()
        var size = 0
        for (line in text.lineSequence()) {
            val add = line.toByteArray(Charsets.UTF_8).size + 1
            if (size + add > MAX_BODY_BYTES) break
            kept.add(line)
            size += add
        }
        return kept.joinToString("\n")
    }
}
