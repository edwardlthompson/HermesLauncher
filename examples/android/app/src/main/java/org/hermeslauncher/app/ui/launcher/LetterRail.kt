package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
    fun pick(y: Float) {
        val idx = (y / heightPx.coerceAtLeast(1) * letters.size).toInt().coerceIn(0, letters.lastIndex)
        onLetter(letters[idx])
    }
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(24.dp)
            .onSizeChanged { heightPx = it.height.coerceAtLeast(1) }
            .pointerInput(letters) {
                detectTapGestures { offset -> pick(offset.y) }
            }
            .pointerInput(letters) {
                detectVerticalDragGestures { change, _ -> pick(change.position.y) }
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { contentDescription = cd },
            )
        }
    }
}
