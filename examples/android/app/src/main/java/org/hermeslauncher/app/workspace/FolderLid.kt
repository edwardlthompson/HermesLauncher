package org.hermeslauncher.app.workspace

import org.hermeslauncher.app.icons.LaunchableApp

enum class FolderPreviewKind {
    STACK,
    GRID,
    FAN,
}

data class FolderInfo(
    val id: Long,
    val title: String = "",
    val contents: List<LaunchableApp> = emptyList(),
    val fullscreen: Boolean = false,
    val preview: FolderPreviewKind = FolderPreviewKind.STACK,
)

object FolderLid {
    const val PREVIEW_MAX: Int = 4

    fun previewCap(kind: FolderPreviewKind): Int = when (kind) {
        FolderPreviewKind.STACK -> 1
        FolderPreviewKind.FAN -> 3
        FolderPreviewKind.GRID -> PREVIEW_MAX
    }

    fun preview(apps: List<LaunchableApp>, limit: Int = PREVIEW_MAX): List<LaunchableApp> {
        val cap = limit.coerceAtLeast(0)
        return apps.filter { it.packageName.isNotBlank() && it.activityName.isNotBlank() }.take(cap)
    }

    fun badge(unreadByPackage: Map<String, Int>, contents: List<LaunchableApp>): Int {
        return contents.sumOf { app -> (unreadByPackage[app.packageName] ?: 0).coerceAtLeast(0) }
    }
}
