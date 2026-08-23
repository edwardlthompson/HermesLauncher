package dev.foss.goldenpath.privacyreport

object ReportMarkdown {
    private val kinds = setOf("crash", "bug", "feature")

    fun build(
        kind: String,
        description: String?,
        stack: String? = null,
        exceptionType: String? = null,
        fingerprint: String? = null,
        appVersion: String? = null,
        osFamily: String? = null,
    ): String {
        val reportKind = if (kind in kinds) kind else "bug"
        val desc = SanitizeReport.text(description)
        val stackS = SanitizeReport.text(stack, stack = true)
        val parts = mutableListOf(
            "## What happened",
            desc.ifEmpty { "(no description)" },
            "",
            "## Kind",
            reportKind,
        )
        if (!fingerprint.isNullOrBlank()) {
            parts.addAll(listOf("", "## Fingerprint", "`${SanitizeReport.text(fingerprint)}`"))
        }
        if (!exceptionType.isNullOrBlank()) {
            parts.addAll(listOf("", "## Exception", SanitizeReport.text(exceptionType)))
        }
        if (!appVersion.isNullOrBlank()) {
            parts.addAll(listOf("", "## App version", SanitizeReport.text(appVersion)))
        }
        if (!osFamily.isNullOrBlank()) {
            parts.addAll(listOf("", "## OS family", SanitizeReport.text(osFamily)))
        }
        if (stackS.isNotEmpty()) {
            parts.addAll(listOf("", "## Stack", "```", stackS, "```"))
        }
        return parts.joinToString("\n").trim() + "\n"
    }
}
