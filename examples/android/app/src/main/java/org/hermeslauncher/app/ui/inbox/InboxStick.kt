package org.hermeslauncher.app.ui.inbox

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.distinctUntilChanged
import org.hermeslauncher.app.ui.theme.MotionPrefs

object InboxStickPolicy {
    fun isAtTop(index: Int, offset: Int): Boolean = index == 0 && offset <= 0

    fun shouldPinToTop(stickToTop: Boolean, newestFirst: Boolean): Boolean {
        return stickToTop && newestFirst
    }

    /** Clear stick as soon as the list leaves the top, even while a fling is in progress. */
    fun nextStick(
        stickToTop: Boolean,
        index: Int,
        offset: Int,
        isScrollInProgress: Boolean,
    ): Boolean {
        if (!isAtTop(index, offset)) {
            return false
        }
        if (!isScrollInProgress) {
            return true
        }
        return stickToTop
    }

    fun shouldScrollToTop(
        stickToTop: Boolean,
        newestFirst: Boolean,
        isScrollInProgress: Boolean,
    ): Boolean {
        return shouldPinToTop(stickToTop, newestFirst) && !isScrollInProgress
    }
}

@Composable
fun InboxStickToTop(
    listState: LazyListState,
    newestFirst: Boolean,
    revision: Any,
) {
    val reduced = MotionPrefs.reduced(LocalContext.current)
    var stickToTop by remember {
        mutableStateOf(
            InboxStickPolicy.isAtTop(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset,
            ),
        )
    }
    LaunchedEffect(listState) {
        snapshotFlow {
            Triple(
                listState.isScrollInProgress,
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset,
            )
        }.distinctUntilChanged().collect { (scrolling, index, offset) ->
            stickToTop = InboxStickPolicy.nextStick(stickToTop, index, offset, scrolling)
        }
    }
    LaunchedEffect(revision, newestFirst, stickToTop, reduced, listState.isScrollInProgress) {
        if (InboxStickPolicy.shouldScrollToTop(stickToTop, newestFirst, listState.isScrollInProgress)) {
            if (MotionPrefs.animateScroll(reduced)) {
                listState.animateScrollToItem(0)
            } else {
                listState.scrollToItem(0)
            }
        }
    }
}
