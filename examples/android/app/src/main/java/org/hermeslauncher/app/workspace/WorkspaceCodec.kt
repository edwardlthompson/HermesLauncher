package org.hermeslauncher.app.workspace

import org.hermeslauncher.app.widgets.WidgetHostCodec
import org.hermeslauncher.app.widgets.WidgetHostState

object WorkspaceCodec {
    const val V5: String = "v5"

    fun encode(bundle: WorkspaceBundle): String {
        val screens = bundle.model.screens.joinToString(",") { screen ->
            "${screen.id}:${screen.kind.name}"
        }
        val inner = WidgetHostCodec.encode(bundle.host).removePrefix("v4|")
        return "$V5|${bundle.model.homeScreenId}|$screens|$inner"
    }

    fun decode(raw: String): WorkspaceBundle {
        val trimmed = raw.trim()
        if (!trimmed.startsWith("$V5|")) {
            val host = WidgetHostCodec.decode(trimmed)
            return WorkspaceBundle(host, WorkspaceModel.migrate(host))
        }
        val body = trimmed.removePrefix("$V5|")
        val first = body.indexOf('|')
        val second = body.indexOf('|', first + 1)
        if (first <= 0 || second <= first) {
            return WorkspaceBundle(WidgetHostState(), WorkspaceModel.defaults())
        }
        val home = body.substring(0, first).toLongOrNull()
        val screens = parseScreens(body.substring(first + 1, second))
        val host = WidgetHostCodec.decode("v4|" + body.substring(second + 1))
        return WorkspaceBundle(host, resolve(home, screens, host))
    }

    private fun parseScreens(packed: String): List<WorkspaceScreen> {
        if (packed.isBlank()) {
            return emptyList()
        }
        return packed.split(",").mapNotNull { chunk ->
            val colon = chunk.indexOf(':')
            if (colon <= 0) {
                return@mapNotNull null
            }
            val id = chunk.substring(0, colon).toLongOrNull() ?: return@mapNotNull null
            val kind = runCatching {
                WorkspaceKind.valueOf(chunk.substring(colon + 1))
            }.getOrNull() ?: return@mapNotNull null
            WorkspaceScreen(id, kind)
        }
    }

    private fun resolve(
        home: Long?,
        screens: List<WorkspaceScreen>,
        host: WidgetHostState,
    ): WorkspaceModel {
        if (screens.isEmpty()) {
            return WorkspaceModel.migrate(host)
        }
        val ids = screens.map { it.id }
        val inbox = screens.firstOrNull { it.kind == WorkspaceKind.INBOX }?.id
        val homeId = when {
            home != null && home in ids -> home
            inbox != null -> inbox
            else -> ids.first()
        }
        return WorkspaceModel(screenIds = ids, homeScreenId = homeId, screens = screens)
    }
}
