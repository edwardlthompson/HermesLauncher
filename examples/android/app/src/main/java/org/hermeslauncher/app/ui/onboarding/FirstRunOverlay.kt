package org.hermeslauncher.app.ui.onboarding

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    onUsage: () -> Unit,
    onPost: () -> Unit,
    onAllGrants: () -> Unit,
    onLater: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
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
            if (!snapshot.usageGranted) {
                Button(onClick = onUsage, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.home_setup_usage))
                }
            }
            if (!snapshot.postNotificationsGranted) {
                Button(onClick = onPost, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.home_setup_alerts))
                }
            }
            Button(onClick = onAllGrants, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.grant_open_settings))
            }
            Button(
                onClick = {
                    context.startActivity(Intent(context, org.hermeslauncher.app.NovaImportActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.backup_nova))
            }
            Button(onClick = onLater, modifier = Modifier.fillMaxWidth().padding(top = SpacingSm)) {
                Text(stringResource(R.string.home_setup_later))
            }
        }
    }
}
