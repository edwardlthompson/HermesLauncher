package org.hermeslauncher.app.workspace

sealed class DesktopItem {
    abstract val id: Long
    abstract val cellX: Int
    abstract val cellY: Int
    abstract val spanX: Int
    abstract val spanY: Int

    data class Shortcut(
        override val id: Long,
        val packageName: String,
        val activityName: String = "",
        val label: String = "",
        override val cellX: Int = 0,
        override val cellY: Int = 0,
        override val spanX: Int = 1,
        override val spanY: Int = 1,
    ) : DesktopItem()

    data class Widget(
        override val id: Long,
        val appWidgetId: Int,
        override val cellX: Int,
        override val cellY: Int,
        override val spanX: Int,
        override val spanY: Int,
    ) : DesktopItem()

    data class Folder(
        override val id: Long,
        val folderId: Long,
        override val cellX: Int = 0,
        override val cellY: Int = 0,
        override val spanX: Int = 1,
        override val spanY: Int = 1,
    ) : DesktopItem()
}

object DesktopItems {
    fun skipUnknown(items: List<DesktopItem>, folderIds: Set<Long>): List<DesktopItem> {
        return items.filter { item ->
            when (item) {
                is DesktopItem.Folder -> item.folderId in folderIds && item.folderId != 0L
                is DesktopItem.Shortcut ->
                    item.packageName.isNotBlank() && item.activityName.isNotBlank() && item.id != 0L
                is DesktopItem.Widget -> item.appWidgetId > 0 && item.id != 0L
            }
        }
    }
}
