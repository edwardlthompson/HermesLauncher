package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.scroll.ScrubPolicy
import org.hermeslauncher.app.ui.theme.SpacingSm

@Composable
fun LetterRail(
    letters: List<Char>,
    onLetter: (Char) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (letters.isEmpty()) {
        return
    }
    var heightPx by remember { mutableIntStateOf(1) }
    var active by remember { mutableStateOf<Char?>(null) }
    fun pick(y: Float, dragging: Boolean) {
        val ch = ScrubPolicy.letterAt(y, heightPx, letters) ?: return
        active = if (dragging) ch else null
        onLetter(ch)
    }
    Box(modifier = modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(24.dp)
                .align(Alignment.CenterEnd)
                .onSizeChanged { heightPx = it.height.coerceAtLeast(1) }
                .pointerInput(letters) {
                    detectTapGestures { offset -> pick(offset.y, dragging = false) }
                }
                .pointerInput(letters) {
                    detectVerticalDragGestures(
                        onDragEnd = { active = null },
                        onDragCancel = { active = null },
                    ) { change, _ -> pick(change.position.y, dragging = true) }
                }
                .padding(vertical = SpacingSm),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            letters.forEach { ch ->
                val cd = stringResource(R.string.drawer_letter, ch.toString())
                Text(
                    text = ch.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (ch == active) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.semantics { contentDescription = cd },
                )
            }
        }
        val shown = active
        if (shown != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-36).dp)
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = shown.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}
