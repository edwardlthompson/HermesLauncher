package org.hermeslauncher.app.ui.feedback

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import org.hermeslauncher.app.R
import org.hermeslauncher.app.display.highRefreshScroll
import org.hermeslauncher.app.feedback.FeedbackPreview
import org.hermeslauncher.app.githubfeedback.IssueFormUrl
import org.hermeslauncher.app.ui.insets.bottomInsetPadding
import org.hermeslauncher.app.ui.theme.SpacingMd

@Composable
fun FeedbackScreen(
    kind: String,
    releaseRepo: String,
    stack: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var description by remember { mutableStateOf("") }
    val preview = FeedbackPreview.text(kind, description, stack)
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val canSubmit = FeedbackPreview.canSubmit(description, stack)
    Column(
        modifier = modifier
            .highRefreshScroll()
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(
            text = stringResource(
                if (kind == "feature") R.string.feedback_feature_title else R.string.feedback_bug_title,
            ),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(text = stringResource(R.string.feedback_clipboard_hint))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(R.string.feedback_description)) },
        )
        Text(text = preview, style = MaterialTheme.typography.bodySmall)
        Button(onClick = { clipboard.setText(AnnotatedString(preview)) }) {
            Text(stringResource(R.string.feedback_copy))
        }
        Button(
            enabled = canSubmit,
            onClick = {
                val template = if (kind == "feature") "product_idea.yml" else "bug_report.yml"
                val fields = if (kind == "feature") {
                    mapOf("problem" to description, "solution" to preview, "title" to "[feat]: ")
                } else {
                    mapOf("description" to description, "reproduction" to preview, "title" to "[bug]: ")
                }
                val built = IssueFormUrl.build(releaseRepo, template, fields)
                if (built.bodyTooLarge) {
                    clipboard.setText(AnnotatedString(built.clipboardMarkdown ?: preview))
                }
                val url = built.url
                if (url.startsWith("https://")) {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                }
            },
        ) {
            Text(stringResource(R.string.feedback_open))
        }
        Button(
            onClick = {
                clipboard.setText(AnnotatedString(""))
                onBack()
            },
            modifier = Modifier.bottomInsetPadding(),
        ) {
            Text(stringResource(R.string.feedback_discard))
        }
    }
}
