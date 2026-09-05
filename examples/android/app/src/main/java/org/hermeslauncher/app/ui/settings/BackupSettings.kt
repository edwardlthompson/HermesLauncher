package org.hermeslauncher.app.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.R
import org.hermeslauncher.app.icons.DockCodec
import org.hermeslauncher.app.oem.LivePermissions
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.widgets.WidgetHostState
import org.hermeslauncher.app.workspace.BackupCodec
import org.hermeslauncher.app.workspace.BackupResult
import org.hermeslauncher.app.workspace.DesktopLayout
import org.hermeslauncher.app.workspace.DesktopCodec
import org.hermeslauncher.app.workspace.HermesBackup
import org.hermeslauncher.app.workspace.WidgetIdRemap
import org.hermeslauncher.app.workspace.WorkspaceBundle
import org.hermeslauncher.app.workspace.WorkspaceCodec
import org.hermeslauncher.app.workspace.WorkspaceModel

@Composable
fun BackupSettings() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as HermesApplication
    val export = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val json = withContext(Dispatchers.IO) { buildBackupJson(app) }
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(json.toByteArray(Charsets.UTF_8))
            }
        }
    }
    val import = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val raw = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
            }
            val result = BackupCodec.import(raw) { provider ->
                WidgetIdRemap.allocate(context, app.widgetHost, provider)
            }
            when (result) {
                is BackupResult.Invalid -> {
                    Toast.makeText(context, context.getString(R.string.backup_invalid), Toast.LENGTH_LONG).show()
                }
                is BackupResult.Ok -> {
                    withContext(Dispatchers.IO) { applyBackup(app, result) }
                    Toast.makeText(
                        context,
                        context.getString(R.string.backup_ok, result.skippedMissing),
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Button(onClick = { export.launch("hermes-backup.json") }) {
            Text(stringResource(R.string.backup_export))
        }
        Button(onClick = { import.launch(arrayOf("application/json", "*/*")) }) {
            Text(stringResource(R.string.backup_import))
        }
        Button(onClick = { context.startActivity(Intent(context, org.hermeslauncher.app.NovaImportActivity::class.java)) }) {
            Text(stringResource(R.string.backup_nova))
        }
        Text(stringResource(R.string.backup_reset_body), style = MaterialTheme.typography.bodySmall)
        Button(
            onClick = {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        app.widgetStore.save(WidgetHostState())
                        app.desktopStore.save(DesktopLayout())
                        org.hermeslauncher.app.l3.L3Reset.homeLayout(app)
                    }
                }
            },
        ) {
            Text(stringResource(R.string.backup_reset))
        }
        Button(onClick = { LivePermissions.startSafe(context, LivePermissions.homeRoleSettings()) }) {
            Text(stringResource(R.string.backup_default_home))
        }
        Text(stringResource(R.string.backup_labs_hint), style = MaterialTheme.typography.bodySmall)
    }
}

private suspend fun buildBackupJson(app: HermesApplication): String {
    val host = app.widgetStore.state.first()
    val model = WorkspaceModel.migrate(host)
    val desktop = app.desktopStore.layout.first()
    val dock = app.dockStore.layout.first()
    val feeds = app.feedStore.urls.first()
    val hidden = app.drawerPrefs.snapshot.first().hidden.toList()
    val blacklist = app.vault.policies.first()
        .filter { !it.storeContent }
        .map { it.packageName }
    return BackupCodec.encode(
        HermesBackup(
            workspace = WorkspaceCodec.encode(WorkspaceBundle(host, model)),
            desktop = DesktopCodec.encode(desktop),
            dock = DockCodec.encode(dock),
            feedUrls = feeds,
            hiddenApps = hidden,
            blacklist = blacklist,
        ),
    )
}

private suspend fun applyBackup(app: HermesApplication, result: BackupResult.Ok) {
    app.widgetStore.save(result.remappedHost)
    if (result.backup.desktop.isNotBlank()) {
        app.desktopStore.save(DesktopCodec.decode(result.backup.desktop))
    }
    result.backup.feedUrls.forEach { app.feedStore.addAll(listOf(it)) }
    result.backup.hiddenApps.forEach { app.drawerPrefs.hide(it) }
    result.backup.blacklist.forEach { app.vault.blacklist(it) }
}
