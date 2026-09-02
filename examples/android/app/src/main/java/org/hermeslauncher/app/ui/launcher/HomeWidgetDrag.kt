package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.hermeslauncher.app.ui.widgets.WidgetDragLayer
import org.hermeslauncher.app.ui.widgets.dropCell
import org.hermeslauncher.app.ui.widgets.edgePageDelta
import org.hermeslauncher.app.widgets.WidgetBinding
import org.hermeslauncher.app.widgets.WidgetChoice
import org.hermeslauncher.app.widgets.WidgetGrid
import org.hermeslauncher.app.widgets.WidgetHostController
import org.hermeslauncher.app.widgets.WidgetHostState

@Composable
fun BoxScope.HomeWidgetDrag(
    picker: List<WidgetChoice>?,
    dragChoice: WidgetChoice?,
    dragWindow: Offset,
    rootCoords: LayoutCoordinates?,
    gridCoords: LayoutCoordinates?,
    widgets: WidgetHostState,
    pagerState: PagerState,
    pageCount: Int,
    pageWidthPx: Float,
    lastEdgeMs: Long,
    scope: CoroutineScope,
    widgetController: WidgetHostController,
    onDragStart: (WidgetChoice, Offset) -> Unit,
    onDragWindow: (Offset) -> Unit,
    onEdgeMs: (Long) -> Unit,
    setDragging: (Boolean) -> Unit,
    setDragChoice: (WidgetChoice?) -> Unit,
) {
    WidgetDragLayer(
        choices = picker,
        dragChoice = dragChoice,
        dragWindow = dragWindow,
        rootCoords = rootCoords,
        onCancel = widgetController::cancelPick,
        onPick = { choice ->
            val page = pagerState.currentPage
            val origin = WidgetGrid.firstFit(
                widgets.page(page).bindings,
                WidgetBinding.PLACE_CELLS,
                WidgetBinding.PLACE_CELLS_H,
                widgets.grid,
            )
            if (page > 0 && origin != null) {
                widgetController.drop(choice, page, origin.first, origin.second)
            }
        },
        onDragStart = onDragStart,
        onDrag = { window ->
            onDragWindow(window)
            val now = System.currentTimeMillis()
            if (now - lastEdgeMs >= 700L) {
                val delta = edgePageDelta(window.x, pageWidthPx, pagerState.currentPage, pageCount)
                if (delta != 0) {
                    onEdgeMs(now)
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + delta) }
                }
            }
        },
        onDragEnd = {
            val choice = dragChoice
            val target = dropCell(gridCoords, dragWindow, pagerState.currentPage, widgets.grid)
            setDragging(false)
            setDragChoice(null)
            if (choice != null && target != null) {
                widgetController.drop(choice, target.first, target.second, target.third)
            }
        },
    )
}
