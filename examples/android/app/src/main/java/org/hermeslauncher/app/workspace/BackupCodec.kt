package org.hermeslauncher.app.workspace

import org.hermeslauncher.app.widgets.WidgetBinding
import org.hermeslauncher.app.widgets.WidgetHostState
import org.hermeslauncher.app.widgets.WidgetPageState
import org.json.JSONArray
import org.json.JSONObject

data class HermesBackup(
    val version: Int = VERSION,
    val workspace: String = "",
    val desktop: String = "",
    val dock: String = "",
    val feedUrls: List<String> = emptyList(),
    val hiddenApps: List<String> = emptyList(),
    val blacklist: List<String> = emptyList(),
) {
    companion object {
        const val VERSION = 1
    }
}

sealed class BackupResult {
    data class Ok(
        val backup: HermesBackup,
        val remappedHost: WidgetHostState,
        val skippedMissing: Int,
    ) : BackupResult()

    data object Invalid : BackupResult()
}

/**
 * JSON Hermes backup. Widget [appWidgetId]s from foreign devices are never kept —
 * [allocate] must mint a new host id from [WidgetBinding.providerFlattened].
 */
object BackupCodec {
    fun encode(backup: HermesBackup): String {
        val root = JSONObject()
        root.put("version", backup.version)
        root.put("workspace", backup.workspace)
        root.put("desktop", backup.desktop)
        root.put("dock", backup.dock)
        root.put("feedUrls", JSONArray(backup.feedUrls))
        root.put("hiddenApps", JSONArray(backup.hiddenApps))
        root.put("blacklist", JSONArray(backup.blacklist))
        return root.toString()
    }

    fun decode(raw: String?): HermesBackup? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            val root = JSONObject(raw)
            HermesBackup(
                version = root.optInt("version", HermesBackup.VERSION),
                workspace = root.optString("workspace", ""),
                desktop = root.optString("desktop", ""),
                dock = root.optString("dock", ""),
                feedUrls = stringList(root.optJSONArray("feedUrls")),
                hiddenApps = stringList(root.optJSONArray("hiddenApps")),
                blacklist = stringList(root.optJSONArray("blacklist")),
            )
        }.getOrNull()
    }

    fun import(raw: String?, allocate: (providerFlattened: String) -> Int?): BackupResult {
        val backup = decode(raw) ?: return BackupResult.Invalid
        val host = runCatching {
            WorkspaceCodec.decode(backup.workspace).host
        }.getOrElse { return BackupResult.Invalid }
        val (remapped, skipped) = remapWidgets(host, allocate)
        return BackupResult.Ok(backup, remapped, skipped)
    }

    fun remapWidgets(
        host: WidgetHostState,
        allocate: (providerFlattened: String) -> Int?,
    ): Pair<WidgetHostState, Int> {
        var skipped = 0
        val pages = host.pages.map { page ->
            val bindings = buildList {
                for (binding in page.bindings) {
                    val provider = binding.providerFlattened?.takeIf { it.isNotBlank() }
                    if (provider == null) {
                        skipped += 1
                        continue
                    }
                    val newId = allocate(provider)
                    if (newId == null || newId <= 0) {
                        skipped += 1
                        continue
                    }
                    add(
                        WidgetBinding(
                            appWidgetId = newId,
                            providerFlattened = provider,
                            cellsW = binding.cellsW,
                            cellsH = binding.cellsH,
                            cellX = binding.cellX,
                            cellY = binding.cellY,
                        ),
                    )
                }
            }
            WidgetPageState(page.pageIndex, bindings)
        }
        return WidgetHostState(pages = pages, grid = host.grid) to skipped
    }

    private fun stringList(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val value = array.optString(i, "").trim()
                if (value.isNotEmpty()) add(value)
            }
        }
    }
}
