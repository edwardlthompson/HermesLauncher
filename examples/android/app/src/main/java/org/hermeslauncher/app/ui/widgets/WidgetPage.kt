package org.hermeslauncher.app.ui.widgets

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.widgets.HermesAppWidgetHost
import org.hermeslauncher.app.widgets.WidgetBinding
import org.hermeslauncher.app.widgets.WidgetCatalog
import org.hermeslauncher.app.widgets.WidgetGrid
import org.hermeslauncher.app.widgets.WidgetGridSpec
import org.hermeslauncher.app.widgets.WidgetPageState
import kotlin.math.roundToInt

@Composable
fun WidgetPage(
    page: WidgetPageState,
    host: HermesAppWidgetHost,
    grid: WidgetGridSpec,
    dragging: Boolean,
    onLongPressEmpty: () -> Unit,
    onDoubleTapEmpty: () -> Unit = {},
    onAdd: () -> Unit,
    onMove: (Int, Int, Int) -> Unit,
    onSpan: (WidgetBinding) -> Unit,
    onRemove: (Int) -> Unit,
    onGridPositioned: (LayoutCoordinates) -> Unit,
    showEmpty: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var editId by remember(page.pageIndex) { mutableStateOf<Int?>(null) }
    var live by remember(page.pageIndex) { mutableStateOf<WidgetBinding?>(null) }
    var wellCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var overWell by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val editing = editId != null
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned(onGridPositioned)
            .pointerInput(page.bindings, dragging, grid) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        val cell = WidgetGrid.cellAt(
                            offset.x, offset.y, size.width.toFloat(), size.height.toFloat(), grid,
                        )
                        val hit = if (cell == null) {
                            null
                        } else {
                            page.bindings.firstOrNull { covers(it, cell.first, cell.second) }
                        }
                        if (hit == null) {
                            onDoubleTapEmpty()
                        }
                    },
                    onLongPress = { offset ->
                        val cell = WidgetGrid.cellAt(
                            offset.x, offset.y, size.width.toFloat(), size.height.toFloat(), grid,
                        )
                        val hit = if (cell == null) {
                            null
                        } else {
                            page.bindings.firstOrNull { covers(it, cell.first, cell.second) }
                        }
                        if (hit != null) {
                            editId = hit.appWidgetId
                            live = null
                        } else {
                            editId = null
                            live = null
                            onLongPressEmpty()
                        }
                    },
                    onTap = {
                        editId = null
                        live = null
                    },
                )
            },
    ) {
        val cellW = maxWidth / grid.columns
        val cellH = maxHeight / grid.rows
        val cellWPx = with(density) { cellW.toPx() }
        val cellHPx = with(density) { cellH.toPx() }
        page.bindings.forEach { binding ->
            val shown = if (binding.appWidgetId == editId) live ?: binding else binding
            WidgetHostTile(
                binding = shown,
                host = host,
                cellWidth = cellW,
                cellHeight = cellH,
                hostEnabled = !dragging && editId == null,
                onEdit = {
                    editId = binding.appWidgetId
                    live = null
                    Log.i(WidgetCatalog.TAG, "edit id=${binding.appWidgetId}")
                },
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (cellW * shown.cellX).roundToPx(),
                            (cellH * shown.cellY).roundToPx(),
                        )
                    }
                    .size(cellW * shown.cellsW, cellH * shown.cellsH),
            )
        }
        if (editing) {
            val base = page.bindings.firstOrNull { it.appWidgetId == editId }
            if (base != null) {
                val shown = live ?: base
                WidgetResizeFrame(
                    binding = base,
                    spec = grid,
                    cellWPx = cellWPx,
                    cellHPx = cellHPx,
                    onPreview = { live = it },
                    onCommit = { next ->
                        onSpan(next)
                        live = null
                    },
                    onMove = { window ->
                        overWell = hitRemoveWell(wellCoords, window)
                    },
                    onMoveEnd = { dx, dy ->
                        val id = editId
                        if (id != null && overWell) {
                            editId = null
                            live = null
                            overWell = false
                            onRemove(id)
                        } else {
                            val nx = base.cellX + (dx / cellWPx).roundToInt()
                            val ny = base.cellY + (dy / cellHPx).roundToInt()
                            onMove(base.appWidgetId, nx, ny)
                            overWell = false
                        }
                    },
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (cellW * shown.cellX).roundToPx(),
                                (cellH * shown.cellY).roundToPx(),
                            )
                        }
                        .size(cellW * shown.cellsW, cellH * shown.cellsH),
                )
            }
            RemoveWell(
                armed = overWell,
                onPositioned = { wellCoords = it },
                onClick = {
                    val id = editId ?: return@RemoveWell
                    editId = null
                    live = null
                    onRemove(id)
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(SpacingMd),
            )
        }
        if (showEmpty && page.bindings.isEmpty()) {
            Text(
                text = stringResource(R.string.widget_page_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(SpacingMd),
            )
            Button(
                onClick = onAdd,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(SpacingMd),
            ) {
                Text(stringResource(R.string.widget_add))
            }
        }
    }
}

@Composable
private fun RemoveWell(
    armed: Boolean,
    onPositioned: (LayoutCoordinates) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(R.string.widget_remove_well)
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned(onPositioned),
        color = if (armed) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = if (armed) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        tonalElevation = if (armed) 3.dp else 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Delete, contentDescription = label)
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = SpacingMd),
            )
        }
    }
}

private fun covers(binding: WidgetBinding, x: Int, y: Int): Boolean {
    return x >= binding.cellX && x < binding.cellX + binding.cellsW &&
        y >= binding.cellY && y < binding.cellY + binding.cellsH
}
