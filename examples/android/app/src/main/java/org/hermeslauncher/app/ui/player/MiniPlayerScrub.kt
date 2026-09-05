package org.hermeslauncher.app.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.PlayerClock
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm

@Composable
fun MiniPlayerScrub(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragging by remember { mutableStateOf(false) }
    var slide by remember { mutableFloatStateOf(0f) }
    val shown = if (dragging) slide else PlayerClock.progress(positionMs, durationMs)
    val cursor = if (dragging) PlayerClock.seekMs(slide, durationMs) else positionMs
    val elapsed = PlayerClock.format(cursor)
    val remain = PlayerClock.format(PlayerClock.remaining(cursor, durationMs))
    val total = if (durationMs > 0L) PlayerClock.format(durationMs) else "0:00"
    val seekLabel = stringResource(R.string.player_seek)
    val elapsedLabel = stringResource(R.string.player_elapsed, elapsed)
    val remainLabel = stringResource(R.string.player_remaining, remain)
    val totalLabel = stringResource(R.string.player_total, total)
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = SpacingMd)) {
        Slider(
            value = shown,
            onValueChange = {
                dragging = true
                slide = it
            },
            onValueChangeFinished = {
                dragging = false
                onSeek(PlayerClock.seekMs(slide, durationMs))
            },
            enabled = durationMs > 0L,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = seekLabel },
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = SpacingSm),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TimeLabel(elapsed, elapsedLabel)
            TimeLabel("−$remain", remainLabel)
            TimeLabel(total, totalLabel)
        }
    }
}

@Composable
private fun TimeLabel(text: String, description: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.semantics { contentDescription = description },
    )
}
