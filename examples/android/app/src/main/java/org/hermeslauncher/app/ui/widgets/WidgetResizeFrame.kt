package org.hermeslauncher.app.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.RadiusMd
import org.hermeslauncher.app.widgets.WidgetBinding
import org.hermeslauncher.app.widgets.WidgetGridSpec
import org.hermeslauncher.app.widgets.WidgetResize
import org.hermeslauncher.app.widgets.WidgetResizeEdge

@Composable
fun WidgetResizeFrame(
    binding: WidgetBinding,
    spec: WidgetGridSpec,
    cellWPx: Float,
    cellHPx: Float,
    onPreview: (WidgetBinding) -> Unit,
    onCommit: (WidgetBinding) -> Unit,
    onMove: (Offset) -> Unit,
    onMoveEnd: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var moveBox by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val resized = stringResource(R.string.widget_resized, binding.cellsW, binding.cellsH)
    Box(
        modifier = modifier
            .fillMaxSize()
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(RadiusMd))
            .semantics {
                contentDescription = resized
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp)
                .onGloballyPositioned { moveBox = it }
                .pointerInput(binding.appWidgetId) {
                    var totalX = 0f
                    var totalY = 0f
                    detectDragGestures(
                        onDragEnd = { onMoveEnd(totalX, totalY) },
                        onDrag = { change, drag ->
                            change.consume()
                            totalX += drag.x
                            totalY += drag.y
                            onMove(moveBox?.localToWindow(change.position) ?: Offset.Zero)
                        },
                    )
                },
        )
        EdgeHandle(
            modifier = Modifier.align(Alignment.CenterStart),
            edge = WidgetResizeEdge.LEFT,
            visible = WidgetResize.handleVisible(binding, WidgetResizeEdge.LEFT, spec),
            description = stringResource(R.string.widget_resize_left),
            binding = binding,
            spec = spec,
            cellWPx = cellWPx,
            cellHPx = cellHPx,
            onPreview = onPreview,
            onCommit = onCommit,
        )
        EdgeHandle(
            modifier = Modifier.align(Alignment.TopCenter),
            edge = WidgetResizeEdge.TOP,
            visible = WidgetResize.handleVisible(binding, WidgetResizeEdge.TOP, spec),
            description = stringResource(R.string.widget_resize_top),
            binding = binding,
            spec = spec,
            cellWPx = cellWPx,
            cellHPx = cellHPx,
            onPreview = onPreview,
            onCommit = onCommit,
        )
        EdgeHandle(
            modifier = Modifier.align(Alignment.CenterEnd),
            edge = WidgetResizeEdge.RIGHT,
            visible = WidgetResize.handleVisible(binding, WidgetResizeEdge.RIGHT, spec),
            description = stringResource(R.string.widget_resize_right),
            binding = binding,
            spec = spec,
            cellWPx = cellWPx,
            cellHPx = cellHPx,
            onPreview = onPreview,
            onCommit = onCommit,
        )
        EdgeHandle(
            modifier = Modifier.align(Alignment.BottomCenter),
            edge = WidgetResizeEdge.BOTTOM,
            visible = WidgetResize.handleVisible(binding, WidgetResizeEdge.BOTTOM, spec),
            description = stringResource(R.string.widget_resize_bottom),
            binding = binding,
            spec = spec,
            cellWPx = cellWPx,
            cellHPx = cellHPx,
            onPreview = onPreview,
            onCommit = onCommit,
        )
    }
}

@Composable
private fun EdgeHandle(
    edge: WidgetResizeEdge,
    visible: Boolean,
    description: String,
    binding: WidgetBinding,
    spec: WidgetGridSpec,
    cellWPx: Float,
    cellHPx: Float,
    onPreview: (WidgetBinding) -> Unit,
    onCommit: (WidgetBinding) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    Box(
        modifier = modifier
            .size(48.dp)
            .semantics { contentDescription = description }
            .pointerInput(binding, edge) {
                var accX = 0f
                var accY = 0f
                detectDragGestures(
                    onDragStart = {
                        accX = 0f
                        accY = 0f
                    },
                    onDragEnd = {
                        onCommit(
                            WidgetResize.spansForDelta(
                                binding, edge, accX, accY, cellWPx, cellHPx, spec,
                            ),
                        )
                    },
                    onDrag = { change, drag ->
                        change.consume()
                        accX += drag.x
                        accY += drag.y
                        onPreview(
                            WidgetResize.spansForDelta(
                                binding, edge, accX, accY, cellWPx, cellHPx, spec,
                            ),
                        )
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
        )
    }
}
