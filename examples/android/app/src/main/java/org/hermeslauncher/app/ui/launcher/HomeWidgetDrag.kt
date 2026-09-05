package org.hermeslauncher.app.ui.launcher

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.widgets.WidgetDragLayer
import org.hermeslauncher.app.ui.widgets.dropCell
import org.hermeslauncher.app.ui.widgets.edgePageDelta
import org.hermeslauncher.app.widgets.DropPolicy
import org.hermeslauncher.app.widgets.WidgetBinding
import org.hermeslauncher.app.widgets.WidgetCatalog
import org.hermeslauncher.app.widgets.WidgetChoice
import org.hermeslauncher.app.widgets.WidgetGrid
import org.hermeslauncher.app.widgets.WidgetHostController
import org.hermeslauncher.app.widgets.WidgetHostState
import org.hermeslauncher.app.workspace.WorkspaceModel

@Composable
fun BoxScope.HomeWidgetDrag(
    picker: List<WidgetChoice>?,
    dragChoice: WidgetChoice?,
    dragWindow: Offset,
    rootCoords: LayoutCoordinates?,
    gridCoords: LayoutCoordinates?,
    widgets: WidgetHostState,
    model: WorkspaceModel,
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
    val context = LocalContext.current
    fun toast(res: Int) {
        Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
    }
    WidgetDragLayer(
        choices = picker,
        dragChoice = dragChoice,
        dragWindow = dragWindow,
        rootCoords = rootCoords,
        onCancel = widgetController::cancelPick,
        onPick = { choice ->
            val page = model.widgetPageAt(pagerState.currentPage)
            val origin = WidgetGrid.firstFit(
                widgets.page(page).bindings,
                WidgetBinding.PLACE_CELLS,
                WidgetBinding.PLACE_CELLS_H,
                widgets.grid,
            )
            when {
                page < 1 -> {
                    Log.i(WidgetCatalog.TAG, "tap miss wrong page=$page")
                    toast(R.string.widget_drop_wrong_page)
                }
                origin == null -> {
                    Log.i(WidgetCatalog.TAG, "tap miss no space page=$page")
                    toast(R.string.widget_drop_no_space)
                }
                else -> widgetController.drop(choice, page, origin.first, origin.second)
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
            val page = model.widgetPageAt(pagerState.currentPage)
            val target = dropCell(gridCoords, dragWindow, page, widgets.grid)
            setDragging(false)
            setDragChoice(null)
            val miss = DropPolicy.miss(page, gridCoords != null, target != null)
            when {
                choice == null -> Unit
                miss != null -> {
                    Log.i(WidgetCatalog.TAG, "drop miss $miss page=$page")
                    toast(
                        when (miss) {
                            DropPolicy.Miss.WRONG_PAGE -> R.string.widget_drop_wrong_page
                            DropPolicy.Miss.NO_GRID -> R.string.widget_drop_no_grid
                            DropPolicy.Miss.OFF_GRID -> R.string.widget_drop_miss
                        },
                    )
                }
                target != null -> widgetController.drop(choice, target.first, target.second, target.third)
            }
        },
    )
}
