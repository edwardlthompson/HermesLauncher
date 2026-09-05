package org.hermeslauncher.app.workspace

import com.android.launcher3.Launcher
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.hermeslauncher.app.HermesApplication

object NovaImportApply {
    suspend fun auto(app: HermesApplication): Int? {
        if (app.homePrefs.novaImportDone.first()) {
            return null
        }
        val bytes = runCatching { NovaBackup.findLatest(app) }.getOrNull() ?: return null
        return applyStream(app, ByteArrayInputStream(bytes), requireLauncher = true)
    }

    suspend fun applyStream(
        app: HermesApplication,
        input: InputStream,
        requireLauncher: Boolean,
    ): Int? {
        val imported = NovaBackup.read(input) ?: return null
        app.desktopStore.save(imported.desktop)
        app.dockStore.save(imported.dock)
        val placed = withContext(Dispatchers.Main) {
            val launcher = Launcher.ACTIVITY_TRACKER.getCreatedActivity<Launcher>()
            when {
                launcher == null && requireLauncher -> null
                launcher == null -> imported.shortcuts
                else -> {
                    val count = org.hermeslauncher.app.l3.L3NovaImport.apply(launcher, imported)
                    if (requireLauncher && count < 0) null else count.coerceAtLeast(0)
                }
            }
        }
        if (placed == null) {
            return null
        }
        app.homePrefs.setNovaImportDone(true)
        return placed
    }
}
