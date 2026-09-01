package org.hermeslauncher.app.feedback

import org.hermeslauncher.app.privacyreport.ReportMarkdown
import org.hermeslauncher.app.privacyreport.SanitizeReport

object FeedbackPreview {
    fun text(kind: String, description: String?, stack: String?): String =
        ReportMarkdown.build(kind = kind, description = description, stack = stack)

    fun canSubmit(description: String?, stack: String?): Boolean =
        SanitizeReport.text(description).isNotBlank() || SanitizeReport.text(stack, stack = true).isNotBlank()
}
