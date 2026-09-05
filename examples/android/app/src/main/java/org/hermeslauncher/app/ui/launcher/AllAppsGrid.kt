package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.hermeslauncher.app.R
import org.hermeslauncher.app.icons.AllAppsIndex
import org.hermeslauncher.app.icons.DrawerPolicy
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
    columns: Int = AllAppsIndex.COLUMNS,
    listMode: Boolean = false,
    showRail: Boolean = true,
    dragEnabled: Boolean = false,
    onIconDragStart: (LaunchableApp, Offset) -> Unit = { _, _ -> },
    onIconDrag: (Offset) -> Unit = {},
    onIconDragEnd: () -> Unit = {},
) {
    val sections = remember(apps) { AllAppsIndex.sections(apps) }
    val rail = remember(sections) { AllAppsIndex.rail(sections) }
    val chunk = DrawerPolicy.chunkSize(listMode, columns)
    val keys = remember(sections, predicted, chunk) {
        AllAppsIndex.keys(sections, predicted.isNotEmpty(), chunk)
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var popup by remember { mutableStateOf<LaunchableApp?>(null) }
    Box(modifier = modifier.fillMaxSize()) {
        if (apps.isEmpty() && predicted.isEmpty()) {
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
                            dragEnabled = dragEnabled,
                            onIconDragStart = { app, window ->
                                popup = null
                                onIconDragStart(app, window)
                            },
                            onIconDrag = onIconDrag,
                            onIconDragEnd = onIconDragEnd,
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
                        rows.chunked(chunk).withIndex().toList(),
                        key = { (index, _) -> "r:$letter:$index" },
                    ) { (_, group) ->
                        IconRow(
                            apps = group,
                            pack = pack,
                            unreadByPackage = unreadByPackage,
                            showDots = showDots,
                            listMode = listMode,
                            onApp = onApp,
                            onLongPress = { popup = it },
                            dragEnabled = dragEnabled,
                            onIconDragStart = { app, window ->
                                popup = null
                                onIconDragStart(app, window)
                            },
                            onIconDrag = onIconDrag,
                            onIconDragEnd = onIconDragEnd,
                        )
                    }
                }
            }
            if (showRail) {
                LetterRail(
                    letters = rail,
                    onLetter = { ch ->
                        scope.launch { listState.animateScrollToItem(AllAppsIndex.indexOf(keys, ch)) }
                    },
                    modifier = Modifier.align(Alignment.CenterEnd),
                )
            }
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
    dragEnabled: Boolean,
    onIconDragStart: (LaunchableApp, Offset) -> Unit,
    onIconDrag: (Offset) -> Unit,
    onIconDragEnd: () -> Unit,
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
            listMode = false,
            onApp = onApp,
            onLongPress = onLongPress,
            dragEnabled = dragEnabled,
            onIconDragStart = onIconDragStart,
            onIconDrag = onIconDrag,
            onIconDragEnd = onIconDragEnd,
        )
    }
}

@Composable
private fun IconRow(
    apps: List<LaunchableApp>,
    pack: IconPackId,
    unreadByPackage: Map<String, Int>,
    showDots: Boolean,
    listMode: Boolean,
    onApp: (LaunchableApp) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    dragEnabled: Boolean,
    onIconDragStart: (LaunchableApp, Offset) -> Unit,
    onIconDrag: (Offset) -> Unit,
    onIconDragEnd: () -> Unit,
) {
    val arrangement = if (listMode) Arrangement.Start else Arrangement.SpaceEvenly
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = arrangement) {
        apps.forEach { app ->
            AppCell(
                app = app,
                pack = pack,
                unread = if (showDots) unreadByPackage[app.packageName] ?: 0 else 0,
                onApp = { onApp(app) },
                onLongPress = { onLongPress(app) },
                dragEnabled = dragEnabled,
                onIconDragStart = onIconDragStart,
                onIconDrag = onIconDrag,
                onIconDragEnd = onIconDragEnd,
            )
        }
    }
}

@Composable
private fun AppCell(
    app: LaunchableApp,
    pack: IconPackId,
    unread: Int,
    onApp: () -> Unit,
    onLongPress: () -> Unit,
    dragEnabled: Boolean,
    onIconDragStart: (LaunchableApp, Offset) -> Unit,
    onIconDrag: (Offset) -> Unit,
    onIconDragEnd: () -> Unit,
) {
    var coords by remember(app.packageName, app.activityName) { mutableStateOf<LayoutCoordinates?>(null) }
    Column(
        modifier = Modifier
            .width(64.dp)
            .onGloballyPositioned { coords = it }
            .pointerInput(app.packageName, app.activityName, dragEnabled) {
                if (dragEnabled) {
                    detectTapGestures(onTap = { onApp() })
                } else {
                    detectTapGestures(onTap = { onApp() }, onLongPress = { onLongPress() })
                }
            }
            .pointerInput(app.packageName, app.activityName, dragEnabled) {
                if (!dragEnabled) {
                    return@pointerInput
                }
                detectDragGesturesAfterLongPress(
                    onDragStart = { start ->
                        onIconDragStart(app, coords?.localToWindow(start) ?: Offset.Zero)
                    },
                    onDrag = { change, _ ->
                        onIconDrag(coords?.localToWindow(change.position) ?: Offset.Zero)
                        change.consume()
                    },
                    onDragEnd = onIconDragEnd,
                    onDragCancel = onIconDragEnd,
                )
            }
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
        IconLabel(text = app.label, maxLines = 2)
    }
}
