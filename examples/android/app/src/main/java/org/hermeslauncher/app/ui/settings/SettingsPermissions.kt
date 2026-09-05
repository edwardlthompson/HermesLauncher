package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.hermeslauncher.app.R
import org.hermeslauncher.app.oem.GrantCatalog
import org.hermeslauncher.app.oem.GrantKind
import org.hermeslauncher.app.oem.LivePermissions
import org.hermeslauncher.app.ui.theme.SpacingMd

@Composable
fun SettingsPermissionsPane(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var snap by remember { mutableStateOf(LivePermissions.snapshot(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                snap = LivePermissions.snapshot(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(
            text = stringResource(R.string.settings_section_permissions_body),
            style = MaterialTheme.typography.bodySmall,
        )
        GrantKind.entries.forEach { kind ->
            val on = GrantCatalog.granted(kind, snap)
            ListItem(
                headlineContent = { Text(stringResource(GrantCatalog.titleRes(kind))) },
                supportingContent = { Text(stringResource(GrantCatalog.bodyRes(kind))) },
                trailingContent = {
                    if (on) {
                        Text(stringResource(R.string.grant_status_on))
                    } else {
                        Button(onClick = { GrantCatalog.open(context, kind) }) {
                            Text(stringResource(R.string.grant_status_fix))
                        }
                    }
                },
            )
        }
    }
}
