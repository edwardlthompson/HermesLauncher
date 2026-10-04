package org.hermeslauncher.app.widgets

object WidgetHostIds {
    /** Compose MainActivity host (secondary). Launcher3 HOME stays on [L3_HOST_ID]. */
    const val HOST_ID: Int = 2048
    const val L3_HOST_ID: Int = 1024
    const val LEGACY_COMPOSE_HOST_ID: Int = 1024
}

object WidgetBindPolicy {
    fun canRecord(appWidgetId: Int): Boolean = appWidgetId > 0
}
