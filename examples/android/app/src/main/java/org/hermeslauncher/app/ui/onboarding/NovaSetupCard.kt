package org.hermeslauncher.app.ui.onboarding

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.NovaImportActivity
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm

@Composable
fun NovaSetupCard(
    onLater: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 3.dp,
    ) {
        Column(modifier = Modifier.padding(SpacingMd)) {
            Text(
                text = stringResource(R.string.home_setup_nova),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.home_setup_nova_body),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = SpacingSm, bottom = SpacingMd),
            )
            Button(
                onClick = {
                    context.startActivity(Intent(context, NovaImportActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.backup_nova))
            }
            TextButton(onClick = onLater, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.home_setup_later))
            }
        }
    }
}
