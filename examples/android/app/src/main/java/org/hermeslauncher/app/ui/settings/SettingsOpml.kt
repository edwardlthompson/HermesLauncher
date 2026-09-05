package org.hermeslauncher.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.SubKind
import org.hermeslauncher.app.ui.theme.SpacingMd

@Composable
fun SettingsOpmlButtons() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as HermesApplication
    val newsPick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch { app.feeds.importOpml(uri, SubKind.NEWS) }
    }
    val newsExport = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/xml")) { uri ->
        if (uri != null) scope.launch { app.feeds.exportOpml(uri, SubKind.NEWS) }
    }
    val podPick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch { app.feeds.importOpml(uri, SubKind.PODCAST) }
    }
    val podExport = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/xml")) { uri ->
        if (uri != null) scope.launch { app.feeds.exportOpml(uri, SubKind.PODCAST) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Button(onClick = { newsPick.launch(arrayOf("text/xml", "application/xml", "*/*")) }) {
            Text(stringResource(R.string.feed_import_opml))
        }
        Button(onClick = { newsExport.launch("hermes-feeds.opml") }) {
            Text(stringResource(R.string.feed_export_opml))
        }
        Button(onClick = { podPick.launch(arrayOf("text/xml", "application/xml", "*/*")) }) {
            Text(stringResource(R.string.feed_import_podcast_opml))
        }
        Button(onClick = { podExport.launch("hermes-podcasts.opml") }) {
            Text(stringResource(R.string.feed_export_podcast_opml))
        }
    }
}
