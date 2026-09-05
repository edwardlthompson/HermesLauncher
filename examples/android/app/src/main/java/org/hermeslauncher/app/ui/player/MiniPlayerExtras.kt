package org.hermeslauncher.app.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm

@Composable
fun MiniPlayerExtras(
    speed: Float,
    sleepMinutes: Int,
    onCycleSpeed: () -> Unit,
    onCycleSleep: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingMd, vertical = SpacingSm),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = speed != 1f,
            onClick = onCycleSpeed,
            label = { Text(stringResource(R.string.player_speed, speed)) },
        )
        FilterChip(
            selected = sleepMinutes > 0,
            onClick = onCycleSleep,
            label = {
                Text(
                    if (sleepMinutes <= 0) {
                        stringResource(R.string.player_sleep_off)
                    } else {
                        stringResource(R.string.player_sleep_on, sleepMinutes)
                    },
                )
            },
        )
        FilterChip(
            selected = false,
            onClick = onNext,
            label = { Text(stringResource(R.string.player_play_next)) },
            leadingIcon = {
                Icon(Icons.Filled.SkipNext, contentDescription = null)
            },
        )
    }
}
