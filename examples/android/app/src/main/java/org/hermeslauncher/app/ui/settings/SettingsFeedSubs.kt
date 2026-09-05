package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.FeedSub
import org.hermeslauncher.app.ui.theme.SpacingMd

@Composable
fun SettingsFeedSubs() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as HermesApplication
    val subs by app.feedStore.subs.collectAsStateWithLifecycle(emptyList())
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = stringResource(R.string.feed_subs_title))
        subs.forEach { sub ->
            SubRow(
                sub = sub,
                onChange = { next -> scope.launch { app.feedStore.upsert(next) } },
            )
        }
    }
}

@Composable
private fun SubRow(sub: FeedSub, onChange: (FeedSub) -> Unit) {
    Text(text = sub.title.ifBlank { sub.url }, style = MaterialTheme.typography.titleSmall)
    OutlinedTextField(
        value = sub.tag,
        onValueChange = { onChange(sub.copy(tag = it)) },
        label = { Text(stringResource(R.string.feed_sub_tag)) },
        singleLine = true,
        modifier = Modifier.semantics { contentDescription = "Feed tag" },
    )
    Row(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = stringResource(R.string.feed_sub_notify))
        Switch(
            checked = sub.notify,
            onCheckedChange = { on -> onChange(sub.copy(notify = on)) },
            modifier = Modifier.semantics { contentDescription = "Notify" },
        )
        Text(text = stringResource(R.string.feed_sub_prefetch))
        Switch(
            checked = sub.prefetch,
            onCheckedChange = { on -> onChange(sub.copy(prefetch = on)) },
        )
    }
}
