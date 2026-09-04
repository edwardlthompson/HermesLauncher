package org.hermeslauncher.app.ui.workspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingSm
import org.hermeslauncher.app.workspace.QsbPlacement

@Composable
fun QsbBar(
    placement: QsbPlacement,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (placement == QsbPlacement.NONE) {
        return
    }
    val label = stringResource(R.string.paged_qsb)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingSm, vertical = SpacingSm)
            .semantics { contentDescription = label }
            .clickable(onClick = onOpen),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(SpacingSm),
        )
    }
}
