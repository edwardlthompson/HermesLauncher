package org.hermeslauncher.app.push

/**
 * FOSS push hook: register with a UnifiedPush distributor, never FCM / Play Services.
 * Disabled until a distributor is present on the device.
 */
object UnifiedPushConfig {
    const val CONNECTOR_ACTION = "org.unifiedpush.android.connector.MESSAGE"

    fun endpointUrl(): String? = null

    fun usesProprietaryPush(): Boolean = false
}
