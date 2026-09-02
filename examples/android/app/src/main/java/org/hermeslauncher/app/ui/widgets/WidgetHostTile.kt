package org.hermeslauncher.app.ui.widgets

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import org.hermeslauncher.app.R
import org.hermeslauncher.app.widgets.HermesAppWidgetHost
import org.hermeslauncher.app.widgets.WidgetBinding
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun WidgetHostTile(
    binding: WidgetBinding,
    host: HermesAppWidgetHost,
    cellWidth: Dp,
    cellHeight: Dp,
    hostEnabled: Boolean,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                EditArmFrame(ctx).apply {
                    addView(
                        hostViewOrStub(ctx, host, binding),
                        FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        ),
                    )
                }
            },
            update = { view ->
                val frame = view as EditArmFrame
                frame.onLongPress = onEdit
                val inner = frame.getChildAt(0) ?: return@AndroidView
                inner.isEnabled = hostEnabled
                (inner as? AppWidgetHostView)?.let { hostView ->
                    hostView.isEnabled = hostEnabled
                    val widthPx = with(density) { (cellWidth * binding.cellsW).toPx() }
                    val heightPx = with(density) { (cellHeight * binding.cellsH).toPx() }
                    val widthDp = (widthPx / hostView.resources.displayMetrics.density).roundToInt().coerceAtLeast(1)
                    val heightDp = (heightPx / hostView.resources.displayMetrics.density).roundToInt().coerceAtLeast(1)
                    runCatching { hostView.updateAppWidgetSize(null, widthDp, heightDp, widthDp, heightDp) }
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

internal class EditArmFrame(context: Context) : FrameLayout(context) {
    var onLongPress: () -> Unit = {}
    private val slop = ViewConfiguration.get(context).scaledTouchSlop
    private var downX = 0f
    private var downY = 0f
    private val fire = Runnable {
        val now = SystemClock.uptimeMillis()
        val cancel = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0f, 0f, 0)
        super.dispatchTouchEvent(cancel)
        cancel.recycle()
        onLongPress()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                handler.removeCallbacks(fire)
                handler.postDelayed(fire, ViewConfiguration.getLongPressTimeout().toLong())
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(ev.x - downX) > slop || abs(ev.y - downY) > slop) {
                    handler.removeCallbacks(fire)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> handler.removeCallbacks(fire)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacks(fire)
        super.onDetachedFromWindow()
    }
}

internal fun hostViewOrStub(
    context: Context,
    host: HermesAppWidgetHost,
    binding: WidgetBinding,
): View {
    val created = runCatching {
        val info = AppWidgetManager.getInstance(context).getAppWidgetInfo(binding.appWidgetId)
        host.createView(context, binding.appWidgetId, info)
    }.getOrNull()
    if (created != null) {
        return created
    }
    return TextView(context).apply {
        text = binding.providerFlattened ?: context.getString(R.string.widget_slot_untitled, binding.appWidgetId)
    }
}
