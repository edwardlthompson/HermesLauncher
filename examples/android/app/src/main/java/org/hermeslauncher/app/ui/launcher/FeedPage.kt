package org.hermeslauncher.app.ui.launcher

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.FeedItem
import org.hermeslauncher.app.icons.AppCategory
import org.hermeslauncher.app.ui.inbox.FilterBar
import org.hermeslauncher.app.ui.inbox.FilterMenu
import org.hermeslauncher.app.ui.inbox.InboxFeed
import org.hermeslauncher.app.vault.InboxFilter
import org.hermeslauncher.app.vault.InboxLayout
import org.hermeslauncher.app.vault.InboxQuery
import org.hermeslauncher.app.vault.ShadeBridge
import org.hermeslauncher.app.vault.VaultItem

@Composable
fun FeedPage(
    items: List<VaultItem>,
    feeds: List<FeedItem>,
    onDismiss: (String) -> Unit,
    onDismissGroup: (List<String>) -> Unit,
    onPin: (String) -> Unit,
    onPlay: (FeedItem) -> Unit,
    onLongPressHome: () -> Unit,
    onDoubleTapHome: () -> Unit = {},
    onEmptySwipe: (org.hermeslauncher.app.launcher.GestureSlot) -> Unit = {},
    onSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    val scope = rememberCoroutineScope()
    val filesDir = context.applicationContext.filesDir
    val storedLayout by app.inboxPrefs.layout.collectAsStateWithLifecycle(InboxLayout.APP)
    val storedNewest by app.inboxPrefs.newestFirst.collectAsStateWithLifecycle(true)
    var query by remember { mutableStateOf(InboxQuery()) }
    LaunchedEffect(storedLayout, storedNewest) {
        query = query.copy(layout = storedLayout, newestFirst = storedNewest)
    }
    val searching = query.text.isNotBlank()
    val archivedFlow = remember(searching) {
        if (searching) app.vault.archivedItems else flowOf(emptyList())
    }
    val archived by archivedFlow.collectAsStateWithLifecycle(emptyList())
    val policies by app.vault.policies.collectAsStateWithLifecycle(emptyList())
    val ignored = remember(policies) { InboxFilter.ignoredPackages(policies) }
    val live = remember(items, query, ignored) { InboxFilter.apply(items, query, ignored) }
    val history = remember(archived, query, ignored) {
        if (query.text.isBlank()) emptyList() else InboxFilter.apply(archived, query, ignored)
    }
    val kindOf = remember {
        val pm = context.packageManager
        { pkg: String ->
            val cat = runCatching { pm.getApplicationInfo(pkg, 0).category }.getOrDefault(-1)
            AppCategory.labelKey(AppCategory.kindOf(pkg, cat))
        }
    }
    LaunchedEffect(query.layout, query.newestFirst) {
        Log.i("HermesInbox", "layout=${query.layout} newestFirst=${query.newestFirst}")
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPressHome() },
                    onDoubleTap = { onDoubleTapHome() },
                )
            },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FilterBar(
                unread = InboxFilter.unreadCount(items, ignored),
                searchText = query.text,
                onSearchText = { query = query.copy(text = it) },
                filterMenu = { expanded, onDismiss ->
                    FilterMenu(
                        expanded = expanded,
                        query = query,
                        onDismiss = onDismiss,
                        onQuery = {
                            query = it
                            onDismiss()
                            scope.launch { app.inboxPrefs.setFilter(it.layout, it.newestFirst) }
                        },
                    )
                },
                onSettings = onSettings,
                settingsLabel = stringResource(R.string.settings_open),
            )
            InboxFeed(
                query = query,
                live = live,
                history = history,
                feeds = feeds,
                kindOf = kindOf,
                onDismiss = onDismiss,
                onDismissGroup = onDismissGroup,
                onOpen = { id ->
                    ShadeBridge.open(items.firstOrNull { it.id == id } ?: archived.firstOrNull { it.id == id }, context)
                },
                onAction = { id, index ->
                    (items.firstOrNull { it.id == id } ?: archived.firstOrNull { it.id == id })?.let {
                        ShadeBridge.runAction(it.sbnKey, index)
                    }
                },
                onPin = onPin,
                onPlay = onPlay,
                imageDir = filesDir,
                itemsEmpty = items.isEmpty(),
            )
        }
    }
}
