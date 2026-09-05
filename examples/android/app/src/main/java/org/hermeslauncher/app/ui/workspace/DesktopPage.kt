package org.hermeslauncher.app.ui.workspace

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.R
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.icons.LaunchableApp
import org.hermeslauncher.app.ui.launcher.AppIconImage
import org.hermeslauncher.app.ui.launcher.HomeIconDrag
import org.hermeslauncher.app.ui.launcher.IconLabel
import org.hermeslauncher.app.ui.launcher.emptySpaceSwipe
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.widgets.WidgetPage
import org.hermeslauncher.app.ui.widgets.dropCell
import org.hermeslauncher.app.ui.widgets.hitRemoveWell
import org.hermeslauncher.app.widgets.HermesAppWidgetHost
import org.hermeslauncher.app.widgets.WidgetBinding
import org.hermeslauncher.app.widgets.WidgetGridSpec
import org.hermeslauncher.app.widgets.WidgetPageState
import org.hermeslauncher.app.workspace.DesktopItem

@Composable
fun DesktopPage(
    page: WidgetPageState,
    shortcuts: List<DesktopItem.Shortcut>,
    host: HermesAppWidgetHost,
    grid: WidgetGridSpec,
    pack: IconPackId,
    showLabels: Boolean,
    dragging: Boolean,
    onLongPressEmpty: () -> Unit,
    onDoubleTapEmpty: () -> Unit,
    onAdd: () -> Unit,
    onMove: (Int, Int, Int) -> Unit,
    onSpan: (WidgetBinding) -> Unit,
    onRemove: (Int) -> Unit,
    onGridPositioned: (LayoutCoordinates) -> Unit,
    onLaunchIcon: (DesktopItem.Shortcut) -> Unit,
    onRemoveIcon: (Long) -> Unit,
    onMoveIcon: (Long, Int, Int) -> Unit,
    onEmptySwipe: (org.hermeslauncher.app.launcher.GestureSlot) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var moving by remember { mutableStateOf<DesktopItem.Shortcut?>(null) }
    var dragWindow by remember { mutableStateOf(Offset.Zero) }
    var gridCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var rootCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var wellCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var overWell by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .emptySpaceSwipe(onEmptySwipe)
            .onGloballyPositioned { rootCoords = it },
    ) {
        WidgetPage(
            page = page,
            host = host,
            grid = grid,
            dragging = dragging || moving != null,
            onLongPressEmpty = onLongPressEmpty,
            onDoubleTapEmpty = onDoubleTapEmpty,
            onAdd = onAdd,
            onMove = onMove,
            onSpan = onSpan,
            onRemove = onRemove,
            onGridPositioned = {
                gridCoords = it
                onGridPositioned(it)
            },
            showEmpty = shortcuts.isEmpty(),
            modifier = Modifier.fillMaxSize(),
        )
        CellLayoutGrid(
            spec = grid,
            items = shortcuts,
            modifier = Modifier.fillMaxSize(),
        ) { item, cell ->
            val shortcut = item as DesktopItem.Shortcut
            DesktopIconCell(
                shortcut = shortcut,
                pack = pack,
                showLabels = showLabels,
                hidden = moving?.id == shortcut.id,
                modifier = cell,
                onLaunch = { onLaunchIcon(shortcut) },
                onDragStart = { window ->
                    moving = shortcut
                    dragWindow = window
                    overWell = false
                },
                onDrag = { window ->
                    dragWindow = window
                    overWell = hitRemoveWell(wellCoords, window)
                },
                onDragEnd = {
                    val held = moving
                    val window = dragWindow
                    moving = null
                    overWell = false
                    when {
                        held == null -> Unit
                        hitRemoveWell(wellCoords, window) -> onRemoveIcon(held.id)
                        else -> {
                            val target = dropCell(gridCoords, window, page.pageIndex, grid)
                            if (target != null) {
                                onMoveIcon(held.id, target.second, target.third)
                            }
                        }
                    }
                },
            )
        }
        val held = moving
        if (held != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(SpacingMd)
                    .onGloballyPositioned { wellCoords = it },
                color = if (overWell) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(SpacingMd),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.widget_remove_well))
                    Text(
                        text = stringResource(R.string.widget_remove_well),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = SpacingMd),
                    )
                }
            }
            HomeIconDrag(
                app = LaunchableApp(held.packageName, held.activityName, held.label),
                pack = pack,
                dragWindow = dragWindow,
                rootCoords = rootCoords,
            )
        }
    }
}

@Composable
private fun DesktopIconCell(
    shortcut: DesktopItem.Shortcut,
    pack: IconPackId,
    showLabels: Boolean,
    hidden: Boolean,
    modifier: Modifier,
    onLaunch: () -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
) {
    var coords by remember(shortcut.id) { mutableStateOf<LayoutCoordinates?>(null) }
    Column(
        modifier = modifier
            .graphicsLayer { alpha = if (hidden) 0f else 1f }
            .semantics { contentDescription = shortcut.label }
            .onGloballyPositioned { coords = it }
            .pointerInput(shortcut.id) {
                detectTapGestures(onTap = { onLaunch() })
            }
            .pointerInput(shortcut.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { start ->
                        onDragStart(coords?.localToWindow(start) ?: Offset.Zero)
                    },
                    onDrag = { change, _ ->
                        onDrag(coords?.localToWindow(change.position) ?: Offset.Zero)
                        change.consume()
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd,
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppIconImage(
            app = LaunchableApp(shortcut.packageName, shortcut.activityName, shortcut.label),
            pack = pack,
            modifier = Modifier.size(48.dp),
        )
        if (showLabels) {
            IconLabel(
                text = shortcut.label.ifBlank { shortcut.packageName },
                modifier = Modifier.padding(horizontal = 2.dp),
            )
        }
    }
}
