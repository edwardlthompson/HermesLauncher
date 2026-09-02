package org.hermeslauncher.app.ui.inbox

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm
import org.hermeslauncher.app.vault.PostedNotification

@Composable
fun GrantSheet(
    posted: PostedNotification,
    onKeep: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(modifier = Modifier.padding(SpacingMd)) {
            Text(
                text = stringResource(R.string.grant_title, posted.packageName),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = posted.text ?: posted.title ?: stringResource(R.string.inbox_untitled),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = SpacingSm),
            )
            Text(
                text = stringResource(R.string.grant_images_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = SpacingSm),
            )
            Button(onClick = onKeep, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.grant_text))
            }
            OutlinedButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.grant_skip))
            }
        }
    }
}
