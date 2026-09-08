package org.hermeslauncher.app.ui.inbox

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow

object InboxStickPolicy {
    fun isAtTop(index: Int, offset: Int): Boolean = index == 0 && offset <= 0

    fun shouldPinToTop(stickToTop: Boolean, newestFirst: Boolean): Boolean {
        return stickToTop && newestFirst
    }
}

@Composable
fun InboxStickToTop(
    listState: LazyListState,
    newestFirst: Boolean,
    revision: Any,
) {
    var stickToTop by remember {
        mutableStateOf(
            InboxStickPolicy.isAtTop(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset,
            ),
        )
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.collect { dragging ->
            if (!dragging) {
                stickToTop = InboxStickPolicy.isAtTop(
                    listState.firstVisibleItemIndex,
                    listState.firstVisibleItemScrollOffset,
                )
            }
        }
    }
    LaunchedEffect(revision, newestFirst, stickToTop) {
        if (InboxStickPolicy.shouldPinToTop(stickToTop, newestFirst)) {
            listState.scrollToItem(0)
        }
    }
}
