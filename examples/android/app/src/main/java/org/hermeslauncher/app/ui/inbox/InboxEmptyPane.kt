package org.hermeslauncher.app.ui.inbox

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.oem.LivePermissions
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.vault.InboxEmpty

@Composable
fun InboxEmptyPane(kind: InboxEmpty.Kind, modifier: Modifier = Modifier) {
    val day = rememberSaveable { java.time.LocalDate.now().toEpochDay() }
    val copy = when (kind) {
        InboxEmpty.Kind.GRANT -> stringResource(R.string.settings_listener_body)
        InboxEmpty.Kind.FILTER -> stringResource(R.string.filter_empty)
        InboxEmpty.Kind.ZERO -> stringResource(ZeroCopy.pick(ZeroKind.INBOX, day))
        InboxEmpty.Kind.CONTENT -> return
    }
    val context = LocalContext.current
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.padding(SpacingMd),
    ) {
        Column(modifier = Modifier.padding(SpacingMd)) {
            Text(
                text = copy,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (kind == InboxEmpty.Kind.GRANT) {
                Button(
                    onClick = { LivePermissions.startSafe(context, LivePermissions.listenerSettings()) },
                    modifier = Modifier.padding(top = SpacingMd),
                ) {
                    Text(stringResource(R.string.settings_listener_open))
                }
            }
        }
    }
}
