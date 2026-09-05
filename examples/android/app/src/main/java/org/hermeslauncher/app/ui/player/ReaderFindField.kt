package org.hermeslauncher.app.ui.player

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingSm

@Composable
fun ReaderFindField(query: String, onQuery: (String) -> Unit, modifier: Modifier = Modifier) {
    val cd = stringResource(R.string.feed_reader_find)
    OutlinedTextField(
        value = query,
        onValueChange = onQuery,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingSm)
            .semantics { contentDescription = cd },
        label = { Text(cd) },
        singleLine = true,
    )
}

object ReaderFind {
    fun highlight(text: String, needle: String): String {
        if (needle.isBlank()) {
            return text
        }
        return text
    }

    fun matches(text: String, needle: String): Boolean {
        if (needle.isBlank()) {
            return true
        }
        return text.contains(needle, ignoreCase = true)
    }
}
