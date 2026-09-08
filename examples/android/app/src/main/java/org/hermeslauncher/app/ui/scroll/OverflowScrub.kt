package org.hermeslauncher.app.ui.scroll

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.hermeslauncher.app.R

private val TrackWidth = 22.dp
private val ThumbWidth = 4.dp
private val ThumbHeight = 36.dp

@Composable
fun OverflowScrubBar(scroll: ScrollState, modifier: Modifier = Modifier) {
    if (!ScrubPolicy.canScrubRange(scroll.maxValue)) {
        return
    }
    val scope = rememberCoroutineScope()
    val max = scroll.maxValue.coerceAtLeast(1)
    ScrubTrack(
        fraction = scroll.value / max.toFloat(),
        onFraction = { t -> scope.launch { scroll.scrollTo((t * scroll.maxValue).toInt()) } },
        modifier = modifier,
    )
}

@Composable
fun LazyScrubBar(state: LazyListState, modifier: Modifier = Modifier) {
    val count = state.layoutInfo.totalItemsCount
    if (count <= 1 || !(state.canScrollForward || state.canScrollBackward)) {
        return
    }
    val scope = rememberCoroutineScope()
    val denom = (count - 1).coerceAtLeast(1)
    ScrubTrack(
        fraction = state.firstVisibleItemIndex / denom.toFloat(),
        onFraction = { t ->
            scope.launch { state.scrollToItem(ScrubPolicy.indexAtFraction(t, count)) }
        },
        modifier = modifier,
    )
}

@Composable
private fun ScrubTrack(
    fraction: Float,
    onFraction: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var heightPx by remember { mutableIntStateOf(1) }
    val label = stringResource(R.string.scrub_scrollbar)
    fun pick(y: Float) {
        onFraction((y / heightPx.coerceAtLeast(1)).coerceIn(0f, 1f))
    }
    BoxWithConstraints(
        modifier = modifier
            .width(TrackWidth)
            .fillMaxHeight()
            .onSizeChanged { heightPx = it.height.coerceAtLeast(1) }
            .pointerInput(Unit) { detectTapGestures { pick(it.y) } }
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, _ -> pick(change.position.y) }
            }
            .semantics { contentDescription = label },
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxHeight()
                .width(ThumbWidth)
                .background(
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    RoundedCornerShape(2.dp),
                ),
        )
        val travel = (maxHeight - ThumbHeight).coerceAtLeast(0.dp)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = travel * fraction.coerceIn(0f, 1f))
                .width(ThumbWidth)
                .height(ThumbHeight)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)),
        )
    }
}

fun Modifier.scrubGutter(): Modifier = this.padding(end = TrackWidth)
