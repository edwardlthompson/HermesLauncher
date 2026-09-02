package org.hermeslauncher.app.ui.launcher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.FeedItem
import org.hermeslauncher.app.icons.HomeSearchRank
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.icons.LaunchableApp
import org.hermeslauncher.app.icons.UsageRow
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.vault.VaultItem

@Composable
fun HomeSearchOverlay(
    visible: Boolean,
    apps: List<LaunchableApp>,
    predicted: List<LaunchableApp>,
    usage: List<UsageRow>,
    inbox: List<VaultItem>,
    feeds: List<FeedItem>,
    pack: IconPackId,
    onApp: (LaunchableApp) -> Unit,
    onInbox: (VaultItem) -> Unit,
    onFeed: (FeedItem) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!visible) {
        return
    }
    var query by remember { mutableStateOf("") }
    val focus = LocalFocusManager.current
    val requester = remember { FocusRequester() }
    val hits = remember(query, apps, predicted, usage, inbox, feeds) {
        HomeSearchRank.query(query, apps, usage, inbox, feeds, predicted)
    }
    LaunchedEffect(Unit) { requester.requestFocus() }
    BackHandler { onClose() }
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(SpacingMd)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().focusRequester(requester),
                label = { Text(stringResource(R.string.home_search_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                trailingIcon = {
                    IconButton(onClick = {
                        if (query.isNotEmpty()) {
                            query = ""
                        } else {
                            onClose()
                        }
                    }) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.filter_close))
                    }
                },
            )
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (hits.apps.isNotEmpty()) {
                    Text(stringResource(R.string.home_search_apps), style = MaterialTheme.typography.titleSmall)
                    hits.apps.forEach { app ->
                        ListItem(
                            headlineContent = { Text(app.label) },
                            leadingContent = {
                                AppIconImage(app = app, pack = pack, modifier = Modifier.size(40.dp))
                            },
                            modifier = Modifier.clickable { onApp(app) },
                        )
                    }
                }
                if (hits.inbox.isNotEmpty()) {
                    HorizontalDivider()
                    Text(stringResource(R.string.home_search_inbox), style = MaterialTheme.typography.titleSmall)
                    hits.inbox.forEach { item ->
                        ListItem(
                            headlineContent = { Text(item.title ?: stringResource(R.string.inbox_untitled)) },
                            supportingContent = { Text(item.text.orEmpty()) },
                            modifier = Modifier.clickable { onInbox(item) },
                        )
                    }
                }
                if (hits.feeds.isNotEmpty()) {
                    HorizontalDivider()
                    Text(stringResource(R.string.home_search_feeds), style = MaterialTheme.typography.titleSmall)
                    hits.feeds.forEach { item ->
                        ListItem(
                            headlineContent = { Text(item.title) },
                            supportingContent = { Text(item.feedTitle) },
                            modifier = Modifier.clickable { onFeed(item) },
                        )
                    }
                }
                if (hits.apps.isEmpty() && hits.inbox.isEmpty() && hits.feeds.isEmpty()) {
                    Text(stringResource(R.string.home_search_empty), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
