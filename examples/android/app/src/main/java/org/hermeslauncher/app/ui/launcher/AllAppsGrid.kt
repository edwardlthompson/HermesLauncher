package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.hermeslauncher.app.R
import org.hermeslauncher.app.icons.AllAppsIndex
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.icons.LaunchableApp
import org.hermeslauncher.app.ui.theme.SpacingSm

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AllAppsGrid(
    apps: List<LaunchableApp>,
    predicted: List<LaunchableApp>,
    pack: IconPackId,
    onApp: (LaunchableApp) -> Unit,
    modifier: Modifier = Modifier,
    unreadByPackage: Map<String, Int> = emptyMap(),
    showDots: Boolean = true,
) {
    val sections = remember(apps) { AllAppsIndex.sections(apps) }
    val rail = remember(sections) { AllAppsIndex.rail(sections) }
    val keys = remember(sections, predicted) { AllAppsIndex.keys(sections, predicted.isNotEmpty()) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var popup by remember { mutableStateOf<LaunchableApp?>(null) }
    Box(modifier = modifier.fillMaxSize()) {
        if (apps.isEmpty()) {
            Text(
                text = stringResource(R.string.drawer_empty),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(SpacingSm),
            )
        } else {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(end = 28.dp)) {
                if (predicted.isNotEmpty()) {
                    item(key = "pred") {
                        PredictedRow(
                            apps = predicted,
                            pack = pack,
                            unreadByPackage = unreadByPackage,
                            showDots = showDots,
                            onApp = onApp,
                            onLongPress = { popup = it },
                        )
                    }
                }
                sections.forEach { (letter, rows) ->
                    stickyHeader(key = "h:$letter") {
                        Text(
                            text = letter.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = SpacingSm, vertical = SpacingSm),
                        )
                    }
                    items(
                        rows.chunked(AllAppsIndex.COLUMNS).withIndex().toList(),
                        key = { (index, _) -> "r:$letter:$index" },
                    ) { (_, chunk) ->
                        IconRow(
                            apps = chunk,
                            pack = pack,
                            unreadByPackage = unreadByPackage,
                            showDots = showDots,
                            onApp = onApp,
                            onLongPress = { popup = it },
                        )
                    }
                }
            }
            LetterRail(
                letters = rail,
                onLetter = { ch ->
                    scope.launch { listState.animateScrollToItem(AllAppsIndex.indexOf(keys, ch)) }
                },
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
        ShortcutPopup(app = popup, onDismiss = { popup = null })
    }
}

@Composable
private fun PredictedRow(
    apps: List<LaunchableApp>,
    pack: IconPackId,
    unreadByPackage: Map<String, Int>,
    showDots: Boolean,
    onApp: (LaunchableApp) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
) {
    Column(modifier = Modifier.padding(SpacingSm)) {
        Text(
            text = stringResource(R.string.drawer_predicted),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        IconRow(
            apps = apps.take(AllAppsIndex.COLUMNS),
            pack = pack,
            unreadByPackage = unreadByPackage,
            showDots = showDots,
            onApp = onApp,
            onLongPress = onLongPress,
        )
    }
}

@Composable
private fun IconRow(
    apps: List<LaunchableApp>,
    pack: IconPackId,
    unreadByPackage: Map<String, Int>,
    showDots: Boolean,
    onApp: (LaunchableApp) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        apps.forEach { app ->
            AppCell(
                app = app,
                pack = pack,
                unread = if (showDots) unreadByPackage[app.packageName] ?: 0 else 0,
                onApp = { onApp(app) },
                onLongPress = { onLongPress(app) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppCell(
    app: LaunchableApp,
    pack: IconPackId,
    unread: Int,
    onApp: () -> Unit,
    onLongPress: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(64.dp)
            .combinedClickable(onClick = onApp, onLongClick = onLongPress)
            .padding(vertical = SpacingSm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            AppIconImage(app = app, pack = pack, modifier = Modifier.size(48.dp))
            UnreadDot(
                count = unread,
                description = stringResource(R.string.drawer_unread_count, unread),
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
        Text(
            text = app.label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
