package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hermeslauncher.app.vault.InboxFilter

@Composable
fun UnreadDot(
    count: Int,
    description: String,
    modifier: Modifier = Modifier,
) {
    if (count <= 0) {
        return
    }
    Box(
        modifier = modifier
            .size(16.dp)
            .background(MaterialTheme.colorScheme.error, CircleShape)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = InboxFilter.unreadLabel(count),
            color = MaterialTheme.colorScheme.onError,
            fontSize = 8.sp,
            modifier = Modifier.padding(0.dp),
        )
    }
}
