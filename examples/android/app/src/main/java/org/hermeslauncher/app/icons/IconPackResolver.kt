package org.hermeslauncher.app.icons

object IconPackResolver {
    const val SYSTEM_PACK: String = "system"

    fun componentKey(pack: IconPackId, app: LaunchableApp): String {
        val packId = pack.packageName?.takeIf { it.isNotBlank() } ?: SYSTEM_PACK
        return "$packId/${app.packageName}/${app.activityName}"
    }
}
