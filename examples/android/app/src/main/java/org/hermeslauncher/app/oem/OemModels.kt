package org.hermeslauncher.app.oem

enum class OemFamily {
    ONEPLUS,
    SAMSUNG,
    XIAOMI,
    PIXEL,
    LINEAGE,
    OTHER,
}

data class PermissionSnapshot(
    val notificationListenerEnabled: Boolean,
    val batteryUnrestricted: Boolean,
    val homeRoleHeld: Boolean = true,
    val mediaGranted: Boolean = true,
)

object OemDetector {
    fun detect(manufacturer: String, display: String): OemFamily {
        val displayLower = display.lowercase()
        if (displayLower.contains("lineage")) {
            return OemFamily.LINEAGE
        }
        val maker = manufacturer.lowercase()
        return when {
            maker.contains("oneplus") || maker == "oppo" -> OemFamily.ONEPLUS
            maker.contains("samsung") -> OemFamily.SAMSUNG
            maker.contains("xiaomi") || maker.contains("redmi") || maker.contains("poco") ->
                OemFamily.XIAOMI
            maker.contains("google") -> OemFamily.PIXEL
            else -> OemFamily.OTHER
        }
    }
}

object RepairPolicy {
    fun needsBanner(snapshot: PermissionSnapshot): Boolean {
        return !snapshot.notificationListenerEnabled ||
            !snapshot.batteryUnrestricted ||
            !snapshot.homeRoleHeld
    }

    fun needsOverlay(snapshot: PermissionSnapshot): Boolean {
        return needsBanner(snapshot)
    }

    fun primaryStep(oem: OemFamily): String {
        return when (oem) {
            OemFamily.ONEPLUS, OemFamily.LINEAGE -> "listener_then_battery"
            OemFamily.SAMSUNG -> "samsung_never_sleeping"
            OemFamily.XIAOMI -> "xiaomi_autostart"
            OemFamily.PIXEL, OemFamily.OTHER -> "listener_then_battery"
        }
    }
}
