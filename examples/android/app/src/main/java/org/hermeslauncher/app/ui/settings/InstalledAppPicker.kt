package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.hermeslauncher.app.icons.LaunchableApp
import org.hermeslauncher.app.ui.theme.SpacingSm

@Composable
fun InstalledAppPicker(
    query: String,
    onQueryChange: (String) -> Unit,
    matches: List<LaunchableApp>,
    onPick: (LaunchableApp) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        matches.forEach { app ->
            ListItem(
                headlineContent = { Text(app.label) },
                supportingContent = { Text(app.packageName) },
                modifier = Modifier.clickable { onPick(app) },
            )
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text(label) },
            singleLine = true,
        )
    }
}
