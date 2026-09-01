package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingMd

@Composable
fun AppDrawerStub(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = SpacingMd,
    ) {
        Column(modifier = Modifier.padding(SpacingMd)) {
            Text(
                text = stringResource(R.string.launcher_drawer_title),
                style = MaterialTheme.typography.titleLarge,
            )
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.launcher_drawer_close))
            }
        }
    }
}
