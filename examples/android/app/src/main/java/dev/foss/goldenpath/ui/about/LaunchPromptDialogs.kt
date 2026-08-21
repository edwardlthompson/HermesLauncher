package dev.foss.goldenpath.ui.about

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.about.AppUpdates

@Composable
fun LaunchPromptDialogs(
    prompt: AppUpdates.LaunchPrompt,
    onDonate: (Boolean) -> Unit,
    onUpdate: (Boolean) -> Unit,
) {
    when (prompt) {
        AppUpdates.LaunchPrompt.Donate -> AlertDialog(
            onDismissRequest = { onDonate(false) },
            title = { Text(stringResource(R.string.about_donate_nudge_title)) },
            text = { Text(stringResource(R.string.about_donate_nudge_body)) },
            confirmButton = {
                TextButton(onClick = { onDonate(true) }) {
                    Text(stringResource(R.string.about_donate))
                }
            },
            dismissButton = {
                TextButton(onClick = { onDonate(false) }) {
                    Text(stringResource(R.string.about_not_now))
                }
            },
        )
        is AppUpdates.LaunchPrompt.Update -> AlertDialog(
            onDismissRequest = { onUpdate(false) },
            title = { Text(stringResource(R.string.about_update_available, prompt.version)) },
            text = { Text(stringResource(R.string.about_update_prompt_body, prompt.version)) },
            confirmButton = {
                TextButton(onClick = { onUpdate(true) }) {
                    Text(stringResource(R.string.about_install))
                }
            },
            dismissButton = {
                TextButton(onClick = { onUpdate(false) }) {
                    Text(stringResource(R.string.about_later))
                }
            },
        )
    }
}
