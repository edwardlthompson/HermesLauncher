package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import org.hermeslauncher.app.feeds.FeedSubPolicy
import org.hermeslauncher.app.feeds.SubKind
import org.hermeslauncher.app.ui.theme.SpacingMd

@Composable
fun SettingsFeedSubs() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as HermesApplication
    val subs by app.feedStore.subs.collectAsStateWithLifecycle(emptyList())
    val news = remember(subs) { FeedSubPolicy.ofKind(subs, SubKind.NEWS) }
    val pods = remember(subs) { FeedSubPolicy.ofKind(subs, SubKind.PODCAST) }
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        SettingsSwitchRow(
            title = R.string.feed_notify_all,
            body = R.string.feed_notify_all_body,
            checked = FeedSubPolicy.allNotify(subs),
            onCheckedChange = { on ->
                scope.launch { app.feedStore.replaceSubs(FeedSubPolicy.setAllNotify(subs, on)) }
            },
        )
        SettingsSwitchRow(
            title = R.string.feed_prefetch_all,
            body = R.string.feed_prefetch_all_body,
            checked = FeedSubPolicy.allPrefetch(subs),
            onCheckedChange = { on ->
                scope.launch { app.feedStore.replaceSubs(FeedSubPolicy.setAllPrefetch(subs, on)) }
            },
        )
        SettingsExpander(
            title = stringResource(R.string.feed_subs_news, news.size),
            initiallyOpen = news.size in 1..8,
        ) {
            news.forEach { sub ->
                SubRow(
                    sub = sub,
                    onChange = { next -> scope.launch { app.feedStore.upsert(next) } },
                    onRemove = { scope.launch { app.feeds.unsubscribe(sub.url) } },
                )
            }
        }
        SettingsExpander(
            title = stringResource(R.string.feed_subs_podcasts, pods.size),
            initiallyOpen = pods.size in 1..8,
        ) {
            pods.forEach { sub ->
                SubRow(
                    sub = sub,
                    onChange = { next -> scope.launch { app.feedStore.upsert(next) } },
                    onRemove = { scope.launch { app.feeds.unsubscribe(sub.url) } },
                )
            }
        }
    }
}

@Composable
private fun SubRow(sub: FeedSub, onChange: (FeedSub) -> Unit, onRemove: () -> Unit) {
    var open by remember(sub.url) { mutableStateOf(false) }
    val label = sub.title.ifBlank { sub.url }
    ListItem(
        headlineContent = { Text(label, style = MaterialTheme.typography.titleSmall) },
        supportingContent = {
            Text(sub.tag.ifBlank { sub.url }, style = MaterialTheme.typography.bodySmall)
        },
        trailingContent = {
            TextButton(onClick = onRemove) {
                Text(stringResource(R.string.feed_unsubscribe))
            }
        },
        modifier = Modifier
            .clickable { open = !open }
            .semantics { contentDescription = label },
    )
    if (open) {
        OutlinedTextField(
            value = sub.tag,
            onValueChange = { onChange(sub.copy(tag = it)) },
            label = { Text(stringResource(R.string.feed_sub_tag)) },
            singleLine = true,
            modifier = Modifier.semantics { contentDescription = "Feed tag" },
        )
        SettingsSwitchRow(
            title = R.string.feed_sub_notify,
            checked = sub.notify,
            onCheckedChange = { on -> onChange(sub.copy(notify = on)) },
        )
        SettingsSwitchRow(
            title = R.string.feed_sub_prefetch,
            checked = sub.prefetch,
            onCheckedChange = { on -> onChange(sub.copy(prefetch = on)) },
        )
    }
}
