package org.hermeslauncher.app.ui.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.android.launcher3.LauncherPrefs
import com.android.launcher3.SessionCommitReceiver
import org.hermeslauncher.app.ui.theme.SpacingMd

/** L3 switches that used to live only in SettingsActivity. */
@Composable
fun L3HomeSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { LauncherPrefs.getPrefs(context) }
    var addIcon by remember {
        mutableStateOf(prefs.getBoolean(SessionCommitReceiver.ADD_ICON_PREFERENCE_KEY, true))
    }
    var allowRotation by remember {
        mutableStateOf(LauncherPrefs.get(context).get(LauncherPrefs.ALLOW_ROTATION))
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        ListItem(
            headlineContent = {
                Text(stringResource(com.android.launcher3.R.string.notification_dots_title))
            },
            supportingContent = {
                Text(stringResource(com.android.launcher3.R.string.notification_dots_service_title))
            },
            modifier = Modifier.clickable {
                context.startActivity(Intent("android.settings.NOTIFICATION_SETTINGS"))
            },
        )
        ListItem(
            headlineContent = {
                Text(stringResource(com.android.launcher3.R.string.auto_add_shortcuts_label))
            },
            supportingContent = {
                Text(stringResource(com.android.launcher3.R.string.auto_add_shortcuts_description))
            },
            trailingContent = {
                Switch(
                    checked = addIcon,
                    onCheckedChange = { on ->
                        addIcon = on
                        prefs.edit().putBoolean(SessionCommitReceiver.ADD_ICON_PREFERENCE_KEY, on)
                            .apply()
                    },
                )
            },
        )
        ListItem(
            headlineContent = {
                Text(stringResource(com.android.launcher3.R.string.allow_rotation_title))
            },
            supportingContent = {
                Text(stringResource(com.android.launcher3.R.string.allow_rotation_desc))
            },
            trailingContent = {
                Switch(
                    checked = allowRotation,
                    onCheckedChange = { on ->
                        allowRotation = on
                        LauncherPrefs.get(context).put(LauncherPrefs.ALLOW_ROTATION, on)
                    },
                )
            },
        )
    }
}
