package org.hermeslauncher.app.vault

enum class PersistAction {
    SKIP,
    PERSIST_TEXT,
    PERSIST_TEXT_AND_IMAGES,
}

data class PersistDecision(
    val action: PersistAction,
    val skipImageReason: String? = null,
)
