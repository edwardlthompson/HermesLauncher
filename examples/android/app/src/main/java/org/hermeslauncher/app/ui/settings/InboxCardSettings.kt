package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.vault.InboxDisplay

@Composable
fun InboxCardSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as HermesApplication
    val truncate by app.inboxPrefs.truncateBody.collectAsStateWithLifecycle(true)
    val maxChars by app.inboxPrefs.bodyMaxChars.collectAsStateWithLifecycle(InboxDisplay.DEFAULT_CHARS)
    val hideSmall by app.inboxPrefs.hideSmallImages.collectAsStateWithLifecycle(true)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        SettingsSwitchRow(
            title = R.string.inbox_truncate,
            body = R.string.inbox_truncate_body,
            checked = truncate,
            onCheckedChange = { on -> scope.launch { app.inboxPrefs.setTruncateBody(on) } },
        )
        if (truncate) {
            SettingsDropdown(
                title = stringResource(R.string.inbox_truncate),
                options = InboxDisplay.CHAR_CHOICES,
                selected = maxChars,
                labelOf = { chars -> stringResource(R.string.inbox_truncate_chars, chars) },
                onSelect = { chars -> scope.launch { app.inboxPrefs.setBodyMaxChars(chars) } },
            )
        }
        SettingsSwitchRow(
            title = R.string.inbox_hide_small_images,
            body = R.string.inbox_hide_small_images_body,
            checked = hideSmall,
            onCheckedChange = { on -> scope.launch { app.inboxPrefs.setHideSmallImages(on) } },
        )
    }
}
