package org.hermeslauncher.app.workspace

import org.hermeslauncher.app.widgets.WidgetHostState
import org.hermeslauncher.app.widgets.WidgetPageState

enum class WorkspaceKind {
    PODCASTS,
    INBOX,
    FEEDS,
    DESKTOP,
}

data class WorkspaceScreen(
    val id: Long,
    val kind: WorkspaceKind,
)

data class WorkspaceModel(
    val screenIds: List<Long>,
    val homeScreenId: Long,
    val screens: List<WorkspaceScreen>,
) {
    fun pagerIndex(id: Long): Int {
        val idx = screenIds.indexOf(id)
        if (idx >= 0) {
            return idx
        }
        return homePagerIndex()
    }

    fun homePagerIndex(): Int {
        val idx = screenIds.indexOf(homeScreenId)
        if (idx >= 0) {
            return idx
        }
        val inbox = screens.firstOrNull { it.kind == WorkspaceKind.INBOX }?.id
        val fallback = inbox?.let { screenIds.indexOf(it) } ?: -1
        return fallback.coerceAtLeast(0)
    }

    companion object {
        const val FEEDS_SCREEN_ID: Long = 1L
        const val INBOX_SCREEN_ID: Long = 2L
        const val PODCASTS_SCREEN_ID: Long = 3L
        const val DESKTOP_ID_BASE: Long = 1000L

        fun desktopId(pageIndex: Int): Long = DESKTOP_ID_BASE + pageIndex.toLong()

        fun defaults(): WorkspaceModel {
            return migrate(WidgetHostState(pages = listOf(WidgetPageState(1))))
        }

        fun migrate(host: WidgetHostState): WorkspaceModel {
            val desktop = host.pages
                .map { page -> WorkspaceScreen(desktopId(page.pageIndex), WorkspaceKind.DESKTOP) }
                .sortedBy { it.id }
            val screens = listOf(
                WorkspaceScreen(PODCASTS_SCREEN_ID, WorkspaceKind.PODCASTS),
                WorkspaceScreen(FEEDS_SCREEN_ID, WorkspaceKind.FEEDS),
                WorkspaceScreen(INBOX_SCREEN_ID, WorkspaceKind.INBOX),
            ) + desktop
            val ids = screens.map { it.id }
            return WorkspaceModel(
                screenIds = ids,
                homeScreenId = INBOX_SCREEN_ID,
                screens = screens,
            )
        }
    }

    fun desktopPageIndex(screenId: Long): Int? {
        val screen = screens.firstOrNull { it.id == screenId } ?: return null
        if (screen.kind != WorkspaceKind.DESKTOP) {
            return null
        }
        return (screenId - DESKTOP_ID_BASE).toInt().takeIf { it >= 1 }
    }

    fun widgetPageAt(pagerPage: Int): Int {
        val id = screens.getOrNull(pagerPage)?.id ?: return 0
        return desktopPageIndex(id) ?: 0
    }
}

data class WorkspaceBundle(
    val host: WidgetHostState,
    val model: WorkspaceModel,
)
