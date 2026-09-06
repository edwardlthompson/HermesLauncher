package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
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
import org.hermeslauncher.app.ui.theme.SpacingMd

@Composable
fun SettingsExpander(
    title: String,
    modifier: Modifier = Modifier,
    initiallyOpen: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var open by remember { mutableStateOf(initiallyOpen) }
    Column(modifier = modifier) {
        ListItem(
            headlineContent = { Text(title) },
            trailingContent = { Text(if (open) "▾" else "▸") },
            modifier = Modifier
                .clickable { open = !open }
                .semantics { contentDescription = title },
        )
        if (open) {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingMd), content = content)
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    body: Int? = null,
) {
    ListItem(
        headlineContent = { Text(stringResource(title)) },
        supportingContent = if (body == null) {
            null
        } else {
            { Text(stringResource(body)) }
        },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
        modifier = modifier,
    )
}
