package org.hermeslauncher.app.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.MiniPlayerState
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm

@Composable
fun MiniPlayerBar(
    state: MiniPlayerState,
    onToggle: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val episode = state.episode ?: return
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingMd, vertical = SpacingSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = episode.title.ifBlank { stringResource(R.string.player_untitled) },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onToggle, enabled = !episode.enclosureUrl.isNullOrBlank()) {
                Text(
                    text = stringResource(
                        if (state.playing) R.string.player_pause else R.string.player_play,
                    ),
                )
            }
            TextButton(onClick = onStop) {
                Text(stringResource(R.string.player_stop))
            }
        }
    }
}
