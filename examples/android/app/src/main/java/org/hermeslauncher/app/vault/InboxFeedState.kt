package org.hermeslauncher.app.vault

data class InboxFeedState(
    val items: List<VaultItem> = emptyList(),
) {
    fun dismissed(id: String): InboxFeedState {
        return copy(items = items.filterNot { it.id == id })
    }
}
