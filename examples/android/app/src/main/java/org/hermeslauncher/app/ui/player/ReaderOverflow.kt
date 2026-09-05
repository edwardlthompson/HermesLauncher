package org.hermeslauncher.app.ui.player

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.hermeslauncher.app.R

@Composable
fun ReaderOverflow(
    onTts: () -> Unit,
    ttsPlaying: Boolean,
    onFind: () -> Unit,
    onScale: () -> Unit,
    onEnclosure: (() -> Unit)?,
) {
    var open by remember { mutableStateOf(false) }
    val more = stringResource(R.string.feed_reader_more)
    IconButton(onClick = { open = true }, modifier = Modifier.semantics { contentDescription = more }) {
        Icon(Icons.Filled.MoreVert, contentDescription = more)
    }
    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        DropdownMenuItem(
            text = { Text(stringResource(if (ttsPlaying) R.string.feed_reader_tts_stop else R.string.feed_reader_tts)) },
            onClick = { open = false; onTts() },
            modifier = Modifier.semantics { contentDescription = "Read aloud" },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.feed_reader_find)) },
            onClick = { open = false; onFind() },
            modifier = Modifier.semantics { contentDescription = "Find in article" },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.feed_reader_scale)) },
            onClick = { open = false; onScale() },
            modifier = Modifier.semantics { contentDescription = "Text size" },
        )
        if (onEnclosure != null) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.feed_reader_open_file)) },
                onClick = { open = false; onEnclosure() },
                modifier = Modifier.semantics { contentDescription = "Open file" },
            )
        }
    }
}
