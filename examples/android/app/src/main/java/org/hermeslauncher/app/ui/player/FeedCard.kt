package org.hermeslauncher.app.ui.player

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.FeedItem
import org.hermeslauncher.app.feeds.FeedKind
import org.hermeslauncher.app.ui.theme.SpacingMd

@Composable
fun FeedCard(
    item: FeedItem,
    kind: FeedKind,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canPlay = kind == FeedKind.EPISODE && !item.enclosureUrl.isNullOrBlank()
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        ),
    ) {
        Text(
            text = item.title.ifBlank { stringResource(R.string.player_untitled) },
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(SpacingMd),
        )
        Text(
            text = item.feedTitle,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = SpacingMd),
        )
        TextButton(onClick = onPlay, enabled = canPlay) {
            Text(
                stringResource(if (canPlay) R.string.player_play else R.string.player_no_audio),
            )
        }
    }
}
