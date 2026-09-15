package org.hermeslauncher.app.l3

/** What Settings copy may claim about L3 HOME vs reserved Compose chrome. */
object L3HomeEffect {
    fun dockCustomWritesFavorites(): Boolean = true

    fun labsOverlapSharesCells(): Boolean = false

    fun labsOverlapPeeksPages(): Boolean = true

    fun lookBadgePaintsDesktopIcons(): Boolean = L3Badge.paintsDesktopIcons()

    fun lookBadgeStylesReservedChrome(): Boolean = true
}
