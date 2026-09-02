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
import org.hermeslauncher.app.oem.PermissionSnapshot
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm

@Composable
fun FirstRunOverlay(
    snapshot: PermissionSnapshot,
    onNotification: () -> Unit,
    onBattery: () -> Unit,
    onHome: () -> Unit,
    onPhotos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(modifier = Modifier.padding(SpacingMd)) {
            Text(
                text = stringResource(R.string.home_setup_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.home_setup_query_all),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = SpacingSm),
            )
            Text(
                text = stringResource(R.string.home_setup_photos),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = SpacingSm, bottom = SpacingMd),
            )
            if (!snapshot.notificationListenerEnabled) {
                Button(onClick = onNotification, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.home_setup_notifications))
                }
            }
            if (!snapshot.batteryUnrestricted) {
                Button(onClick = onBattery, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.home_setup_battery))
                }
            }
            if (!snapshot.homeRoleHeld) {
                Button(onClick = onHome, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.home_setup_home))
                }
            }
            if (!snapshot.mediaGranted) {
                Button(onClick = onPhotos, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.home_setup_media))
                }
            }
        }
    }
}
