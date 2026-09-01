package org.hermeslauncher.app.about

import android.content.Context
import org.json.JSONObject

data class DonationLink(val label: String, val url: String)

data class DonationsConfig(
    val enabled: Boolean,
    val message: String,
    val links: List<DonationLink>,
)

object DonationsLoader {
    const val DEFAULT_VENMO_URL = "https://venmo.com/code?user_id=1857304970395648420"

    fun primaryUrl(config: DonationsConfig): String =
        config.links.firstOrNull()?.url?.ifBlank { null } ?: DEFAULT_VENMO_URL

    fun load(context: Context): DonationsConfig {
        return try {
            val json = context.assets.open("donations.json").bufferedReader().use { it.readText() }
            val root = JSONObject(json)
            val enabled = root.optBoolean("enabled", false)
            val message = root.optString("message", "")
            val links = mutableListOf<DonationLink>()
            val arr = root.optJSONArray("links")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    links.add(DonationLink(item.optString("label"), item.optString("url")))
                }
            }
            DonationsConfig(enabled && links.isNotEmpty(), message, links)
        } catch (_: Exception) {
            DonationsConfig(enabled = false, message = "", links = emptyList())
        }
    }
}
