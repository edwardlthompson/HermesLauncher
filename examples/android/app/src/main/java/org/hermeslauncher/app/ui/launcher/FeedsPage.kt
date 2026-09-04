package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.FeedItem
import org.hermeslauncher.app.feeds.FeedKindResolver
import org.hermeslauncher.app.ui.player.FeedCard
import org.hermeslauncher.app.ui.theme.SpacingMd

@Composable
fun FeedsPage(
    feeds: List<FeedItem>,
    onPlay: (FeedItem) -> Unit,
    onLongPressHome: () -> Unit,
    onDoubleTapHome: () -> Unit = {},
    onAddFeed: suspend (String) -> Boolean = { false },
    modifier: Modifier = Modifier,
) {
    var adding by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPressHome() },
                    onDoubleTap = { onDoubleTapHome() },
                )
            },
    ) {
        if (feeds.isEmpty()) {
            Text(
                text = stringResource(R.string.workspace_feeds_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.padding(SpacingMd),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(SpacingMd),
            ) {
                items(feeds, key = { it.id }) { item ->
                    FeedCard(
                        item = item,
                        kind = FeedKindResolver.kindOf(item),
                        onPlay = { onPlay(item) },
                        modifier = Modifier.padding(horizontal = SpacingMd),
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = { adding = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(SpacingMd),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.feeds_add_title),
            )
        }
        if (adding) {
            FeedAddDialog(
                onDismiss = { adding = false },
                onAdd = onAddFeed,
            )
        }
    }
}
