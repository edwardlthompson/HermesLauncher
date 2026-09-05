package org.hermeslauncher.app.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm

@Composable
fun DefaultHomeReminder(
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Column(modifier = Modifier.padding(SpacingMd)) {
            Text(
                text = stringResource(R.string.backup_default_home),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.backup_default_home_body),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = SpacingSm),
            )
            Button(onClick = onHome, modifier = Modifier.padding(top = SpacingMd)) {
                Text(stringResource(R.string.backup_default_home))
            }
        }
    }
}
