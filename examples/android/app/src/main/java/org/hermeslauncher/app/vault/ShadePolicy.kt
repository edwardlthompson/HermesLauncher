package org.hermeslauncher.app.vault

/** When Hermes should also clear the status-bar notification. */
object ShadePolicy {
    fun cancelAfterOpen(autoCancel: Boolean, ongoing: Boolean): Boolean {
        return autoCancel && !ongoing
    }

    fun cancelAfterAction(hasRemoteInput: Boolean): Boolean {
        return !hasRemoteInput
    }
}
