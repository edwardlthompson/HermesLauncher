package org.hermeslauncher.app.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward30
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    extras: Boolean = false,
    speed: Float = 1f,
    sleepMinutes: Int = 0,
    positionMs: Long = 0L,
    durationMs: Long = 0L,
    onSeek: ((Long) -> Unit)? = null,
    onSkipBack: () -> Unit = {},
    onSkipForward: () -> Unit = {},
    onCycleSpeed: () -> Unit = {},
    onCycleSleep: () -> Unit = {},
    onNext: () -> Unit = {},
) {
    val episode = state.episode ?: return
    val canPlay = !episode.enclosureUrl.isNullOrBlank()
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(vertical = SpacingSm)) {
            Text(
                text = episode.title.ifBlank { stringResource(R.string.player_untitled) },
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingMd, vertical = SpacingSm),
            )
            if (onSeek != null) {
                MiniPlayerScrub(positionMs = positionMs, durationMs = durationMs, onSeek = onSeek)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingMd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                IconButton(onClick = onSkipBack, enabled = canPlay) {
                    Icon(Icons.Filled.Replay10, contentDescription = stringResource(R.string.player_skip_back))
                }
                FilledIconButton(
                    onClick = onToggle,
                    enabled = canPlay,
                    modifier = Modifier.size(56.dp),
                ) {
                    Icon(
                        imageVector = if (state.playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = stringResource(
                            if (state.playing) R.string.player_pause else R.string.player_play,
                        ),
                        modifier = Modifier.size(32.dp),
                    )
                }
                IconButton(onClick = onSkipForward, enabled = canPlay) {
                    Icon(Icons.Filled.Forward30, contentDescription = stringResource(R.string.player_skip_forward))
                }
                FilledTonalIconButton(onClick = onStop) {
                    Icon(Icons.Filled.Stop, contentDescription = stringResource(R.string.player_stop))
                }
            }
            if (extras) {
                MiniPlayerExtras(
                    speed = speed,
                    sleepMinutes = sleepMinutes,
                    onCycleSpeed = onCycleSpeed,
                    onCycleSleep = onCycleSleep,
                    onNext = onNext,
                )
            }
        }
    }
}
